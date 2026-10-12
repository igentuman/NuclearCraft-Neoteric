package igentuman.nc.multiblock.fusion;

import igentuman.nc.api.multiblock.AbstractMultiblockLogic;
import igentuman.nc.block_entity.fusion.FusionReactorControllerBE;
import igentuman.nc.handler.energy.LargeEnergyStorage;
import igentuman.nc.handler.fluid.FluidStackHandler;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.recipe.fusion.FusionCoolantRecipe;
import igentuman.nc.recipe.fusion.FusionRecipe;
import igentuman.nc.recipe.fusion.FusionRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;
import java.util.Random;

import static net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE;

/** Runs the fusion reactor's per-tick reaction: charge, plasma amplification, energy output, cooling, and meltdown. */
public class FusionReactorLogic extends AbstractMultiblockLogic<FusionReactorCache> {

    private static final double RF_AMPLIFICATION_MULTIPLIER = 5.0;
    private static final double PLASMA_TO_ENERGY_CONVERTION = 1.0;
    private static final int MINIMAL_MAGNETIC_FIELD = 8;
    private static final double EXPLOSION_RADIUS = 2.0;
    private static final long CHARGE_TARGET_FACTOR = 7;
    private static final long PLASMA_MELTDOWN_THRESHOLD = 10000;
    private static final long PLASMA_BREAK_EXPLOSION_THRESHOLD = 100000;
    private static final long PLASMA_ENERGY_THRESHOLD = 1_000_000;

    private static final int TANK_FUEL_A = 0;
    private static final int TANK_FUEL_B = 1;
    private static final int TANK_COOLANT = 2;
    private static final int TANK_PRODUCT_FIRST = 3;
    private static final int TANK_HOT_COOLANT = 7;

    private final Random random = new Random();

    private FuelMatch currentMatch;
    private FusionCoolantRecipe coolantRecipe;
    private double ticksProcessed;
    private boolean fuelConsumed;
    private ResourceLocation pendingRecipeId;
    private boolean ignitionComplete;
    private long prevAmplification;
    private double lastHeatDeviationMult = 1;
    private double lastKnownOptimalTemp = 1_000_000;
    private double reactorHeat;
    private long plasmaTemperature;
    private long chargeAmount;
    private boolean runtimeLoaded;

    @Override
    public void tickServer(ServerLevel level, BlockPos controllerPos, FusionReactorCache cache) {
        BlockEntity blockEntity = level.getBlockEntity(controllerPos);
        if (!(blockEntity instanceof FusionReactorControllerBE be)) return;
        seedRuntime(be);
        be.updateStructureDisplay(cache);
        be.updateRedstoneInput(level);
        if (be.hasRedstoneSignal() && !be.forceShutdown) {
            tick(be, cache);
            if (be.running && be.energyPerTick > 0) be.renderBeam(level, cache.size);
        } else {
            idle(be);
        }
    }

    private void seedRuntime(FusionReactorControllerBE be) {
        if (runtimeLoaded) return;
        reactorHeat = Math.max(0, be.reactorHeat);
        plasmaTemperature = Math.max(0, be.plasmaTemperature);
        chargeAmount = Math.max(0, be.chargeAmount);
        runtimeLoaded = true;
    }

    private void tick(FusionReactorControllerBE be, FusionReactorCache fc) {
        Level level = be.getLevel();
        double ratio = ignitionComplete ? be.rfAmplifierRatio() : 1.0;
        int effRfPower = (int) (fc.rfAmplifiersPower * ratio);
        int magnetsPower = fc.magnetsPower;
        be.maxHeat = Math.max(fc.minRFAmplifiersTemp * 2.0, fc.maxMagnetsTemp * 2.0);

        updateCharge(be, effRfPower, magnetsPower);

        if (be.functionalBlocksCharge >= 100) {
            restoreActiveRecipe(be);
            FuelMatch match = (fuelConsumed && currentMatch != null) ? currentMatch : matchRecipe(be, level);
            if (match != null) {
                FusionRecipe recipe = match.recipe();
                currentMatch = match;
                double optimalTemp = recipe.optimalTemperature() > 0 ? recipe.optimalTemperature() : lastKnownOptimalTemp;
                lastKnownOptimalTemp = optimalTemp;

                if (!fuelConsumed) {
                    fuelConsumed = consumeInputs(be, match);
                }

                if (fuelConsumed) {
                    double cpe = controlPartsEfficiency(be, fc, optimalTemp);
                    amplifyPlasma(be, fc, ratio, optimalTemp, cpe);
                    plasmaToEnergyExchange(be, fc, recipe, optimalTemp, cpe);
                    heatLossExchange(be, fc, cpe);
                    if (plasmaTemperature >= optimalTemp) ignitionComplete = true;
                    be.efficiency = calculateEfficiency(be, optimalTemp);
                    be.maxPlasmaTemperature = (long) optimalTemp * 2;
                    be.running = be.energyPerTick > 0 && plasmaTemperature > 0 && be.efficiency > 0;

                    ticksProcessed += Math.max(0, be.efficiency * 4);
                    if (recipe.processTime() > 0 && ticksProcessed >= recipe.processTime()
                            && canFitOutputs(be, recipe)) {
                        produceOutputs(be, recipe);
                        ticksProcessed = 0;
                        fuelConsumed = false;
                        currentMatch = null;
                    }
                } else {
                    idlePlasma(be);
                }
            } else {
                currentMatch = null;
                idlePlasma(be);
            }
        } else {
            idlePlasma(be);
        }

        coolDown(be, fc);
        meltdown(be, fc, level);
        be.updateReactionDisplay(reactorHeat, plasmaTemperature, chargeAmount);
        be.captureReactionRuntime(this);
    }

    public void idle(FusionReactorControllerBE be) {
        seedRuntime(be);
        idlePlasma(be);
        reactorHeat = Math.max(0, reactorHeat - 1000);
        resetRuntime();
        be.updateReactionDisplay(reactorHeat, plasmaTemperature, chargeAmount);
        be.captureReactionRuntime(this);
    }

    @Override
    public void onBroken(ServerLevel level, BlockPos controllerPos, FusionReactorCache cache) {
        super.onBroken(level, controllerPos, cache);
        if (plasmaTemperature <= PLASMA_BREAK_EXPLOSION_THRESHOLD) return;
        MultiblockHandler.MultiblockInstance instance = MultiblockHandler.getInstance(level, controllerPos);
        BlockPos pos = instance != null && instance.lastResult != null ? instance.lastResult.errorPos() : null;
        if (pos == null) return;
        plasmaTemperature = 0;
        level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                (float) EXPLOSION_RADIUS, Level.ExplosionInteraction.TNT);
    }

    @Override
    public void resetRuntime() {
        fuelConsumed = false;
        currentMatch = null;
        pendingRecipeId = null;
        ticksProcessed = 0;
    }

    private void idlePlasma(FusionReactorControllerBE be) {
        if (plasmaTemperature > 0) {
            plasmaTemperature = Math.max(0, (long) (plasmaTemperature / 1.2 - 10000));
        }
        be.energyPerTick = 0;
        be.running = false;
        if (plasmaTemperature < PLASMA_ENERGY_THRESHOLD) ignitionComplete = false;
    }

    private void updateCharge(FusionReactorControllerBE be, int effRfPower, int magnetsPower) {
        long target = (effRfPower + magnetsPower) * CHARGE_TARGET_FACTOR;
        if (target <= 0) {
            be.functionalBlocksCharge = 0;
            return;
        }
        if (chargeAmount < target && be.energyStorage != null) {
            chargeAmount += be.energyStorage.extractEnergy((effRfPower + magnetsPower) / 2, false);
        }
        be.functionalBlocksCharge = (int) Math.min(chargeAmount * 100 / target, 100);
    }

    private double heatDeviationMultiplier(FusionReactorControllerBE be, FusionReactorCache fc, double cpe) {
        double mEff = (magnetsEfficiency(be, fc) - 50) / 100.0;
        double rEff = (rfEfficiency(be, fc) - 50) / 100.0;
        double plasmaStability = (1 + cpe) / 2;
        double minMult = 0.2 * plasmaStability + (mEff + rEff) / 2;
        double maxMult = 1.7 * cpe - (mEff + rEff) / 2;
        double rand = (random.nextDouble() + 4) / 5;
        lastHeatDeviationMult = (lastHeatDeviationMult + rand * (maxMult - minMult) + minMult) / 2;
        return lastHeatDeviationMult;
    }

    private double rfEfficiency(FusionReactorControllerBE be, FusionReactorCache fc) {
        double denom = (reactorHeat + fc.minRFAmplifiersTemp) / 2.0;
        double f = denom > 0 ? Math.min(fc.minRFAmplifiersTemp / denom, 1) : 1;
        return fc.rfEfficiency * f;
    }

    private double magnetsEfficiency(FusionReactorControllerBE be, FusionReactorCache fc) {
        double denom = (reactorHeat + fc.maxMagnetsTemp) / 2.0;
        double f = denom > 0 ? Math.min(fc.maxMagnetsTemp / denom, 1) : 1;
        return fc.magnetsEfficiency * f;
    }

    private double controlPartsEfficiency(FusionReactorControllerBE be, FusionReactorCache fc, double optimalTemp) {
        int size = Math.max(1, fc.size);
        double overall = fc.magneticFieldStrength / size;
        if (overall <= 0) return 0.1;
        double minimal = MINIMAL_MAGNETIC_FIELD * (plasmaTemperature / optimalTemp) * size;
        double denom = (magnetsEfficiency(be, fc) / 100.0) * (minimal / overall);
        if (denom <= 0) return 1.9;
        return Math.min(1.9, Math.max(0.1, 2.0 / denom));
    }

    private double calculateEfficiency(FusionReactorControllerBE be, double optimalTemp) {
        double te = plasmaTemperature / optimalTemp;
        if (plasmaTemperature > optimalTemp) {
            te = optimalTemp / (double) plasmaTemperature + 0.1;
        }
        return te;
    }

    private void amplifyPlasma(FusionReactorControllerBE be, FusionReactorCache fc,
                               double ratio, double optimalTemp, double cpe) {
        double sizeFactor = Math.log(Math.pow(fc.size + 1, 5)) / 10.0;
        double pRatio = Math.log(optimalTemp / 150_000_000.0);
        double sq = pRatio > 0 ? Math.sqrt(pRatio) : 0;
        double plasmaHeatScale = (pRatio - sq + 5) / 7.0;
        double amplificationVolume = (double) fc.rfAmplification * sizeFactor;
        double amplification = amplificationVolume
                * plasmaHeatScale
                * ratio
                * heatDeviationMultiplier(be, fc, cpe)
                * RF_AMPLIFICATION_MULTIPLIER;
        prevAmplification += (long) (amplification / 1000);
        prevAmplification = (long) Math.min(prevAmplification, amplification);
        changePlasma(prevAmplification);
    }

    private void plasmaToEnergyExchange(FusionReactorControllerBE be, FusionReactorCache fc,
                                        FusionRecipe recipe, double optimalTemp, double cpe) {
        double sizeFactor = Math.log(Math.pow(fc.size + 1, 8)) / 8.0;
        double recipeEnergy = Math.max(1, recipe.energy());
        double ept;
        if (plasmaTemperature < optimalTemp) {
            ept = plasmaTemperature / optimalTemp * recipeEnergy;
        } else {
            ept = optimalTemp / (double) plasmaTemperature * recipeEnergy;
        }
        if (ept > 0) {
            changePlasma(-(long) (plasmaTemperature / (150.0 * ept / recipeEnergy)));
        }
        ept = ept * calculateEfficiency(be, optimalTemp) * fc.size * sizeFactor * PLASMA_TO_ENERGY_CONVERTION;
        if (plasmaTemperature < PLASMA_ENERGY_THRESHOLD) {
            ept = 0;
        }
        ept = ept * cpe;
        be.energyPerTick = (int) Math.max(0, Math.min(Integer.MAX_VALUE, ept));
        addEnergy(be.energyStorage, be.energyPerTick);
    }

    private void heatLossExchange(FusionReactorControllerBE be, FusionReactorCache fc, double cpe) {
        double sizeFactor = Math.log(Math.pow(fc.size + 2, 2)) / 100.0;
        changePlasma(-(long) ((plasmaTemperature / Math.pow(cpe, 2)) * sizeFactor));
        changeHeat(Math.min(plasmaTemperature, 100_000_000) / (10000.0 * Math.log(fc.size + 4) * cpe));
    }

    private void coolDown(FusionReactorControllerBE be, FusionReactorCache fc) {
        changeHeat(-1000 * Math.log(fc.size + 4));
        coolantCoolDown(be, fc);
    }

    private void coolantCoolDown(FusionReactorControllerBE be, FusionReactorCache fc) {
        FluidStackHandler tanks = be.fluidTanks();
        if (tanks == null) return;
        FluidStack coolant = tanks.getFluidInTank(TANK_COOLANT);
        if (coolant.isEmpty()) {
            coolantRecipe = null;
            return;
        }
        FusionCoolantRecipe recipe = findCoolant(be, coolant);
        if (recipe == null) return;
        int coolantPerOp = recipe.input().amount();
        FluidStack output = recipe.output().resolve();
        if (coolantPerOp <= 0 || output.isEmpty() || output.getAmount() <= 0) return;

        double coolingPerOp = Math.max(1, recipe.coolingRate()) / Math.max(1.0, fc.size);
        if (reactorHeat <= 0 || coolingPerOp <= 0) return;

        int possibleOps = coolant.getAmount() / coolantPerOp;
        int heatOps = (int) Math.min(Integer.MAX_VALUE, Math.ceil(reactorHeat / coolingPerOp));
        int outputOps = availableOutputOperations(tanks, TANK_HOT_COOLANT, output);
        int actualOps = Math.min(possibleOps, Math.min(heatOps, outputOps));
        if (actualOps <= 0) return;

        changeHeat(-coolingPerOp * actualOps);
        tanks.drainTank(TANK_COOLANT, coolantPerOp * actualOps, EXECUTE);
        tanks.fillTank(TANK_HOT_COOLANT, output.copyWithAmount(output.getAmount() * actualOps), EXECUTE);
    }

    private void meltdown(FusionReactorControllerBE be, FusionReactorCache fc, Level level) {
        if (be.maxHeat <= 0) return;
        if (reactorHeat > be.maxHeat && plasmaTemperature > PLASMA_MELTDOWN_THRESHOLD) {
            if (level instanceof ServerLevel sl && EXPLOSION_RADIUS > 0) {
                BlockPos p = ringPos(be, fc, sl);
                sl.explode(null, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5,
                        (float) EXPLOSION_RADIUS, Level.ExplosionInteraction.TNT);
            }
            plasmaTemperature /= 5;
            reactorHeat /= 2;
        }
    }

    private BlockPos ringPos(FusionReactorControllerBE be, FusionReactorCache fc, ServerLevel sl) {
        int size = Math.max(1, fc.size);
        int x = (sl.getRandom().nextBoolean() ? 1 : -1) * size;
        int z = (sl.getRandom().nextBoolean() ? 1 : -1) * size;
        return be.getBlockPos().offset(x, 1, z);
    }

    private record FuelMatch(FusionRecipe recipe, boolean swapped, ResourceLocation id) {}

    private void restoreActiveRecipe(FusionReactorControllerBE be) {
        if (!fuelConsumed || currentMatch != null) return;
        if (be.getLevel() instanceof ServerLevel level) {
            // Older saves only recorded the active recipe's optimal temperature.
            RecipeHolder<FusionRecipe> legacyMatch = null;
            for (RecipeHolder<FusionRecipe> holder : level.getRecipeManager().getAllRecipesFor(FusionRecipes.FUSION_TYPE.get())) {
                if (pendingRecipeId != null && holder.id().equals(pendingRecipeId)) {
                    currentMatch = new FuelMatch(holder.value(), false, holder.id());
                    break;
                }
                if (pendingRecipeId == null && holder.value().optimalTemperature() == lastKnownOptimalTemp) {
                    if (legacyMatch != null) {
                        legacyMatch = null;
                        break;
                    }
                    legacyMatch = holder;
                }
            }
            if (currentMatch == null && legacyMatch != null) {
                currentMatch = new FuelMatch(legacyMatch.value(), false, legacyMatch.id());
            }
        }
        pendingRecipeId = null;
        if (currentMatch == null) fuelConsumed = false;
    }

    private FuelMatch matchRecipe(FusionReactorControllerBE be, Level level) {
        if (!(level instanceof ServerLevel sl)) return currentMatch;
        FluidStackHandler tanks = be.fluidTanks();
        if (tanks == null) return null;
        FluidStack a = tanks.getFluidInTank(TANK_FUEL_A);
        FluidStack b = tanks.getFluidInTank(TANK_FUEL_B);
        if (a.isEmpty() || b.isEmpty()) return null;
        for (RecipeHolder<FusionRecipe> holder : sl.getRecipeManager().getAllRecipesFor(FusionRecipes.FUSION_TYPE.get())) {
            FusionRecipe r = holder.value();
            if (r.inputA().test(a) && r.inputB().test(b)) {
                if (canFitOutputs(be, r)) return new FuelMatch(r, false, holder.id());
            } else if (r.inputA().test(b) && r.inputB().test(a) && canFitOutputs(be, r)) {
                return new FuelMatch(r, true, holder.id());
            }
        }
        return null;
    }

    private boolean consumeInputs(FusionReactorControllerBE be, FuelMatch match) {
        FluidStackHandler tanks = be.fluidTanks();
        if (tanks == null) return false;
        FusionRecipe recipe = match.recipe();
        int aAmt = match.swapped() ? recipe.inputB().amount() : recipe.inputA().amount();
        int bAmt = match.swapped() ? recipe.inputA().amount() : recipe.inputB().amount();
        if (tanks.getFluidInTank(TANK_FUEL_A).getAmount() < aAmt) return false;
        if (tanks.getFluidInTank(TANK_FUEL_B).getAmount() < bAmt) return false;
        tanks.drainTank(TANK_FUEL_A, aAmt, EXECUTE);
        tanks.drainTank(TANK_FUEL_B, bAmt, EXECUTE);
        return true;
    }

    private boolean canFitOutputs(FusionReactorControllerBE be, FusionRecipe recipe) {
        FluidStackHandler tanks = be.fluidTanks();
        if (tanks == null) return false;
        List<?> outputs = recipe.outputs();
        if (outputs.isEmpty() || outputs.size() > TANK_HOT_COOLANT - TANK_PRODUCT_FIRST) return false;
        int count = Math.min(outputs.size(), TANK_HOT_COOLANT - TANK_PRODUCT_FIRST);
        for (int i = 0; i < count; i++) {
            FluidStack out = recipe.outputs().get(i).resolve();
            if (out.isEmpty() || out.getAmount() <= 0) return false;
            int tank = TANK_PRODUCT_FIRST + i;
            if (availableOutputOperations(tanks, tank, out) < 1) return false;
        }
        return true;
    }

    private void produceOutputs(FusionReactorControllerBE be, FusionRecipe recipe) {
        FluidStackHandler tanks = be.fluidTanks();
        if (tanks == null) return;
        int count = Math.min(recipe.outputs().size(), TANK_HOT_COOLANT - TANK_PRODUCT_FIRST);
        for (int i = 0; i < count; i++) {
            FluidStack out = recipe.outputs().get(i).resolve();
            int tank = TANK_PRODUCT_FIRST + i;
            tanks.fillTank(tank, out.copy(), EXECUTE);
        }
    }

    private int availableOutputOperations(FluidStackHandler tanks, int tank, FluidStack output) {
        FluidStack current = tanks.getFluidInTank(tank);
        if (!current.isEmpty() && !FluidStack.isSameFluidSameComponents(current, output)) return 0;
        int stored = current.isEmpty() ? 0 : current.getAmount();
        int available = tanks.getTankCapacity(tank) - stored;
        return Math.max(0, available / output.getAmount());
    }

    private FusionCoolantRecipe findCoolant(FusionReactorControllerBE be, FluidStack coolant) {
        if (coolantRecipe != null && coolantRecipe.input().test(coolant)) return coolantRecipe;
        if (!(be.getLevel() instanceof ServerLevel sl)) return null;
        for (RecipeHolder<FusionCoolantRecipe> holder : sl.getRecipeManager().getAllRecipesFor(FusionRecipes.COOLANT_TYPE.get())) {
            if (holder.value().input().test(coolant)) {
                coolantRecipe = holder.value();
                return coolantRecipe;
            }
        }
        coolantRecipe = null;
        return null;
    }

    private void addEnergy(LargeEnergyStorage es, int amount) {
        if (es == null || amount <= 0) return;
        int add = Math.min(amount, es.getMaxEnergyStored() - es.getEnergyStored());
        if (add > 0) es.setEnergyStored(es.getEnergyStored() + add);
    }

    private void changePlasma(long amount) {
        plasmaTemperature = Math.max(0, plasmaTemperature + amount);
    }

    private void changeHeat(double amount) {
        reactorHeat = Math.max(0, reactorHeat + amount);
    }

    @Override
    public void saveRuntime(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putDouble("reactorHeat", reactorHeat);
        tag.putLong("plasmaTemperature", plasmaTemperature);
        tag.putLong("chargeAmount", chargeAmount);
        tag.putDouble("ticksProcessed", ticksProcessed);
        tag.putBoolean("fuelConsumed", fuelConsumed);
        ResourceLocation activeRecipeId = currentMatch != null ? currentMatch.id() : pendingRecipeId;
        if (fuelConsumed && activeRecipeId != null) tag.putString("activeRecipe", activeRecipeId.toString());
        tag.putBoolean("ignitionComplete", ignitionComplete);
        tag.putLong("prevAmplification", prevAmplification);
        tag.putDouble("lastKnownOptimalTemp", lastKnownOptimalTemp);
        tag.putDouble("lastHeatDeviationMult", lastHeatDeviationMult);
    }

    @Override
    public void loadRuntime(CompoundTag tag, HolderLookup.Provider registries) {
        if (tag.contains("reactorHeat")) {
            reactorHeat = Math.max(0, tag.getDouble("reactorHeat"));
            plasmaTemperature = Math.max(0, tag.getLong("plasmaTemperature"));
            chargeAmount = Math.max(0, tag.getLong("chargeAmount"));
            runtimeLoaded = true;
        }
        ticksProcessed = tag.getDouble("ticksProcessed");
        fuelConsumed = tag.getBoolean("fuelConsumed");
        pendingRecipeId = tag.contains("activeRecipe") ? ResourceLocation.tryParse(tag.getString("activeRecipe")) : null;
        if (tag.contains("lastKnownOptimalTemp")) lastKnownOptimalTemp = tag.getDouble("lastKnownOptimalTemp");
        ignitionComplete = tag.contains("ignitionComplete") ? tag.getBoolean("ignitionComplete")
                : plasmaTemperature >= lastKnownOptimalTemp;
        prevAmplification = tag.getLong("prevAmplification");
        if (tag.contains("lastHeatDeviationMult")) lastHeatDeviationMult = tag.getDouble("lastHeatDeviationMult");
    }
}
