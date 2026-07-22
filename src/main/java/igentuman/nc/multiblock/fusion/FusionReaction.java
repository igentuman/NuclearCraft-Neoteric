package igentuman.nc.multiblock.fusion;

import igentuman.nc.block_entity.fusion.FusionReactorControllerBE;
import igentuman.nc.handler.energy.LargeEnergyStorage;
import igentuman.nc.handler.fluid.FluidStackHandler;
import igentuman.nc.recipe.fusion.FusionCoolantRecipe;
import igentuman.nc.recipe.fusion.FusionRecipe;
import igentuman.nc.recipe.fusion.FusionRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;
import java.util.Random;

import static net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE;

/** Runs the fusion reactor's per-tick reaction: charge, plasma amplification, energy output, cooling, and meltdown. */
public class FusionReaction {

    private static final double RF_AMPLIFICATION_MULTIPLIER = 5.0;
    private static final double PLASMA_TO_ENERGY_CONVERTION = 1.0;
    private static final int MINIMAL_MAGNETIC_FIELD = 8;
    private static final double EXPLOSION_RADIUS = 2.0;
    private static final long CHARGE_TARGET_FACTOR = 7;
    private static final long PLASMA_MELTDOWN_THRESHOLD = 10000;
    private static final long PLASMA_ENERGY_THRESHOLD = 1_000_000;

    private static final int TANK_FUEL_A = 0;
    private static final int TANK_FUEL_B = 1;
    private static final int TANK_COOLANT = 2;
    private static final int TANK_PRODUCT_FIRST = 3;
    private static final int TANK_HOT_COOLANT = 7;

    private final Random random = new Random();

    private FusionRecipe currentRecipe;
    private FusionCoolantRecipe coolantRecipe;
    private double ticksProcessed;
    private boolean fuelConsumed;
    private long prevAmplification;
    private double lastHeatDeviationMult = 1;
    private double lastKnownOptimalTemp = 1_000_000;

    public void tick(FusionReactorControllerBE be, FusionReactorCache fc) {
        Level level = be.getLevel();
        double ratio = be.rfAmplifierRatio();
        int effRfPower = (int) (fc.rfAmplifiersPower * ratio);
        int effRfAmplification = (int) (fc.rfAmplification * ratio);
        int magnetsPower = fc.magnetsPower;
        be.maxHeat = Math.max(fc.minRFAmplifiersTemp * 2.0, fc.maxMagnetsTemp * 2.0);

        updateCharge(be, effRfPower, magnetsPower);

        if (be.functionalBlocksCharge >= 100) {
            FusionRecipe recipe = (fuelConsumed && currentRecipe != null) ? currentRecipe : matchRecipe(be, level);
            if (recipe != null) {
                currentRecipe = recipe;
                double optimalTemp = recipe.optimalTemperature() > 0 ? recipe.optimalTemperature() : lastKnownOptimalTemp;
                lastKnownOptimalTemp = optimalTemp;

                if (!fuelConsumed) {
                    fuelConsumed = consumeInputs(be, recipe);
                }

                if (fuelConsumed) {
                    double cpe = controlPartsEfficiency(be, fc, optimalTemp);
                    amplifyPlasma(be, fc, effRfAmplification, ratio, optimalTemp, cpe);
                    plasmaToEnergyExchange(be, fc, recipe, optimalTemp, cpe);
                    heatLossExchange(be, fc, cpe);
                    be.efficiency = calculateEfficiency(be, optimalTemp);
                    be.maxPlasmaTemperature = (long) optimalTemp * 2;
                    be.running = be.energyPerTick > 0 && be.plasmaTemperature > 0 && be.efficiency > 0;

                    ticksProcessed += Math.max(0, be.efficiency * 4);
                    if (recipe.processTime() > 0 && ticksProcessed >= recipe.processTime()) {
                        produceOutput(be, recipe);
                        ticksProcessed = 0;
                        fuelConsumed = false;
                        currentRecipe = null;
                    }
                } else {
                    idlePlasma(be);
                }
            } else {
                currentRecipe = null;
                idlePlasma(be);
            }
        } else {
            idlePlasma(be);
        }

        coolDown(be, fc);
        meltdown(be, fc, level);
        be.markDirty();
    }

    /** Not formed or disabled: bleed plasma, clear display. */
    public void idle(FusionReactorControllerBE be) {
        idlePlasma(be);
        be.reactorHeat = Math.max(0, be.reactorHeat - 1000);
        fuelConsumed = false;
        currentRecipe = null;
        ticksProcessed = 0;
    }

    private void idlePlasma(FusionReactorControllerBE be) {
        if (be.plasmaTemperature > 0) {
            be.plasmaTemperature = Math.max(0, (long) (be.plasmaTemperature / 1.2 - 10000));
        }
        be.energyPerTick = 0;
        be.running = false;
    }

    private void updateCharge(FusionReactorControllerBE be, int effRfPower, int magnetsPower) {
        long target = (effRfPower + magnetsPower) * CHARGE_TARGET_FACTOR;
        if (target <= 0) {
            be.functionalBlocksCharge = 0;
            return;
        }
        if (be.chargeAmount < target && be.energyStorage != null) {
            be.chargeAmount += be.energyStorage.extractEnergy((effRfPower + magnetsPower) / 2, false);
        }
        be.functionalBlocksCharge = (int) Math.min(be.chargeAmount * 100 / target, 100);
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
        double denom = (be.reactorHeat + fc.minRFAmplifiersTemp) / 2.0;
        double f = denom > 0 ? Math.min(fc.minRFAmplifiersTemp / denom, 1) : 1;
        return fc.rfEfficiency * f;
    }

    private double magnetsEfficiency(FusionReactorControllerBE be, FusionReactorCache fc) {
        double denom = (be.reactorHeat + fc.maxMagnetsTemp) / 2.0;
        double f = denom > 0 ? Math.min(fc.maxMagnetsTemp / denom, 1) : 1;
        return fc.magnetsEfficiency * f;
    }

    private double controlPartsEfficiency(FusionReactorControllerBE be, FusionReactorCache fc, double optimalTemp) {
        int size = Math.max(1, fc.size);
        double overall = fc.magneticFieldStrength / size;
        if (overall <= 0) return 0.1;
        double minimal = MINIMAL_MAGNETIC_FIELD * (be.plasmaTemperature / optimalTemp) * size;
        double denom = (magnetsEfficiency(be, fc) / 100.0) * (minimal / overall);
        if (denom <= 0) return 1.9;
        return Math.min(1.9, Math.max(0.1, 2.0 / denom));
    }

    private double calculateEfficiency(FusionReactorControllerBE be, double optimalTemp) {
        double te = be.plasmaTemperature / optimalTemp;
        if (be.plasmaTemperature > optimalTemp) {
            te = optimalTemp / (double) be.plasmaTemperature + 0.1;
        }
        return te;
    }

    private void amplifyPlasma(FusionReactorControllerBE be, FusionReactorCache fc,
                               int effRfAmplification, double ratio, double optimalTemp, double cpe) {
        double sizeFactor = Math.log(Math.pow(fc.size + 1, 5)) / 10.0;
        double pRatio = Math.log(optimalTemp / 150_000_000.0);
        double sq = pRatio > 0 ? Math.sqrt(pRatio) : 0;
        double plasmaHeatScale = (pRatio - sq + 5) / 7.0;
        double amplificationVolume = (double) effRfAmplification * sizeFactor;
        double amplification = amplificationVolume
                * plasmaHeatScale
                * ratio
                * heatDeviationMultiplier(be, fc, cpe)
                * RF_AMPLIFICATION_MULTIPLIER;
        prevAmplification += (long) (amplification / 1000);
        prevAmplification = (long) Math.min(prevAmplification, amplification);
        changePlasma(be, prevAmplification);
    }

    private void plasmaToEnergyExchange(FusionReactorControllerBE be, FusionReactorCache fc,
                                        FusionRecipe recipe, double optimalTemp, double cpe) {
        double sizeFactor = Math.log(Math.pow(fc.size + 1, 8)) / 8.0;
        double recipeEnergy = Math.max(1, recipe.energy());
        double ept;
        if (be.plasmaTemperature < optimalTemp) {
            ept = be.plasmaTemperature / optimalTemp * recipeEnergy;
        } else {
            ept = optimalTemp / (double) be.plasmaTemperature * recipeEnergy;
        }
        if (ept > 0) {
            changePlasma(be, -(long) (be.plasmaTemperature / (150.0 * ept / recipeEnergy)));
        }
        ept = ept * calculateEfficiency(be, optimalTemp) * fc.size * sizeFactor * PLASMA_TO_ENERGY_CONVERTION;
        if (be.plasmaTemperature < PLASMA_ENERGY_THRESHOLD) {
            ept = 0;
        }
        ept = ept * cpe;
        be.energyPerTick = (int) Math.max(0, Math.min(Integer.MAX_VALUE, ept));
        addEnergy(be.energyStorage, be.energyPerTick);
    }

    private void heatLossExchange(FusionReactorControllerBE be, FusionReactorCache fc, double cpe) {
        double sizeFactor = Math.log(Math.pow(fc.size + 2, 2)) / 100.0;
        changePlasma(be, -(long) ((be.plasmaTemperature / Math.pow(cpe, 2)) * sizeFactor));
        changeHeat(be, Math.min(be.plasmaTemperature, 100_000_000) / (10000.0 * Math.log(fc.size + 4) * cpe));
    }

    private void coolDown(FusionReactorControllerBE be, FusionReactorCache fc) {
        changeHeat(be, -1000 * Math.log(fc.size + 4));
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
        double coolingRate = Math.max(1, recipe.coolingRate());
        if (be.reactorHeat <= coolingRate) return;

        int coolantPerOp = recipe.input().amount();
        if (coolantPerOp <= 0) return;
        int possibleOps = coolant.getAmount() / coolantPerOp;
        int coolantNeeded = (int) (be.reactorHeat / coolingRate);
        int actualOps = Math.min(possibleOps, coolantNeeded * coolantPerOp);
        if (actualOps <= 0) return;

        changeHeat(be, -(coolingRate / Math.max(1, fc.size)) * actualOps);
        tanks.drainTank(TANK_COOLANT, coolantPerOp * actualOps, EXECUTE);
        FluidStack out = recipe.output().resolve();
        if (!out.isEmpty()) {
            tanks.fillTank(TANK_HOT_COOLANT, new FluidStack(out.getFluid(), out.getAmount() * actualOps), EXECUTE);
        }
    }

    private void meltdown(FusionReactorControllerBE be, FusionReactorCache fc, Level level) {
        if (be.maxHeat <= 0) return;
        if (be.reactorHeat > be.maxHeat && be.plasmaTemperature > PLASMA_MELTDOWN_THRESHOLD) {
            if (level instanceof ServerLevel sl && EXPLOSION_RADIUS > 0) {
                BlockPos p = ringPos(be, fc, sl);
                sl.explode(null, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5,
                        (float) EXPLOSION_RADIUS, Level.ExplosionInteraction.TNT);
            }
            be.plasmaTemperature /= 5;
            be.reactorHeat /= 2;
        }
    }

    private BlockPos ringPos(FusionReactorControllerBE be, FusionReactorCache fc, ServerLevel sl) {
        int size = Math.max(1, fc.size);
        int x = (sl.getRandom().nextBoolean() ? 1 : -1) * size;
        int z = (sl.getRandom().nextBoolean() ? 1 : -1) * size;
        return be.getBlockPos().offset(x, 1, z);
    }

    private FusionRecipe matchRecipe(FusionReactorControllerBE be, Level level) {
        if (!(level instanceof ServerLevel sl)) return currentRecipe;
        FluidStackHandler tanks = be.fluidTanks();
        if (tanks == null) return null;
        FluidStack a = tanks.getFluidInTank(TANK_FUEL_A);
        FluidStack b = tanks.getFluidInTank(TANK_FUEL_B);
        if (a.isEmpty() || b.isEmpty()) return null;
        for (RecipeHolder<FusionRecipe> holder : sl.getRecipeManager().getAllRecipesFor(FusionRecipes.FUSION_TYPE.get())) {
            FusionRecipe r = holder.value();
            if (r.inputA().test(a) && r.inputB().test(b)) return r;
        }
        return null;
    }

    private boolean consumeInputs(FusionReactorControllerBE be, FusionRecipe recipe) {
        FluidStackHandler tanks = be.fluidTanks();
        if (tanks == null) return false;
        int aAmt = recipe.inputA().amount();
        int bAmt = recipe.inputB().amount();
        if (tanks.getFluidInTank(TANK_FUEL_A).getAmount() < aAmt) return false;
        if (tanks.getFluidInTank(TANK_FUEL_B).getAmount() < bAmt) return false;
        tanks.drainTank(TANK_FUEL_A, aAmt, EXECUTE);
        tanks.drainTank(TANK_FUEL_B, bAmt, EXECUTE);
        return true;
    }

    private void produceOutput(FusionReactorControllerBE be, FusionRecipe recipe) {
        FluidStackHandler tanks = be.fluidTanks();
        if (tanks == null) return;
        List<?> outputs = recipe.outputs();
        int count = Math.min(outputs.size(), TANK_HOT_COOLANT - TANK_PRODUCT_FIRST);
        for (int i = 0; i < count; i++) {
            FluidStack out = recipe.outputs().get(i).resolve();
            if (out.isEmpty()) continue;
            int tank = TANK_PRODUCT_FIRST + i;
            FluidStack cur = tanks.getFluidInTank(tank);
            if (!cur.isEmpty() && cur.getFluid() != out.getFluid()) continue;
            tanks.fillTank(tank, out.copy(), EXECUTE);
        }
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

    private void changePlasma(FusionReactorControllerBE be, long amount) {
        be.plasmaTemperature = Math.max(0, be.plasmaTemperature + amount);
    }

    private void changeHeat(FusionReactorControllerBE be, double amount) {
        be.reactorHeat = Math.max(0, be.reactorHeat + amount);
    }

    public void save(CompoundTag tag) {
        tag.putDouble("ticksProcessed", ticksProcessed);
        tag.putBoolean("fuelConsumed", fuelConsumed);
        tag.putLong("prevAmplification", prevAmplification);
        tag.putDouble("lastKnownOptimalTemp", lastKnownOptimalTemp);
        tag.putDouble("lastHeatDeviationMult", lastHeatDeviationMult);
    }

    public void load(CompoundTag tag) {
        ticksProcessed = tag.getDouble("ticksProcessed");
        fuelConsumed = tag.getBoolean("fuelConsumed");
        prevAmplification = tag.getLong("prevAmplification");
        if (tag.contains("lastKnownOptimalTemp")) lastKnownOptimalTemp = tag.getDouble("lastKnownOptimalTemp");
        if (tag.contains("lastHeatDeviationMult")) lastHeatDeviationMult = tag.getDouble("lastHeatDeviationMult");
    }
}
