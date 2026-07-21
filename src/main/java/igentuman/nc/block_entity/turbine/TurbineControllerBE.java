package igentuman.nc.block_entity.turbine;

import igentuman.nc.block_entity.MultiblockControllerBE;
import igentuman.nc.config.Multiblocks;
import igentuman.nc.handler.fluid.FluidStackHandler;
import igentuman.nc.handler.sided.FluidCapabilityHandler;
import igentuman.nc.multiblock.turbine.TurbineCache;
import igentuman.nc.recipe.turbine.TurbineRecipe;
import igentuman.nc.recipe.turbine.TurbineRecipes;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE;

public class TurbineControllerBE extends MultiblockControllerBE {

    @NBTField
    public float rotationSpeed = 0f;
    @NBTField
    public Direction orientation = Direction.NORTH;
    @NBTField
    public BlockPos bearingPos1;
    @NBTField
    public BlockPos bearingPos2;
    @NBTField
    public int width = 0;
    @NBTField
    public int height = 0;
    @NBTField
    public int depth = 0;
    @NBTField(syncToClient = true)
    public int flow = 0;
    @NBTField(syncToClient = true)
    public int bladeCount = 0;
    @NBTField(syncToClient = true)
    public int activeCoils = 0;
    @NBTField(syncToClient = true)
    public int coilEfficiency = 0;
    @NBTField(syncToClient = true)
    public int realFlow = 0;
    @NBTField(syncToClient = true)
    public int maxFlow = 0;
    @NBTField(syncToClient = true)
    public int flowRatio = 0;
    @NBTField(syncToClient = true)
    public int energyPerTick = 0;
    @NBTField(syncToClient = true)
    public int maxEnergyGen = 0;

    public TurbineControllerBE(BlockPos pos, BlockState state, String name) {
        super(ModEntries.get(name).blockEntity().get(), pos, state, name);
    }

    @Override
    public void serverTick() {
        super.serverTick();
        if (!(level instanceof ServerLevel)) return;
        if (mbInstance == null || !mbInstance.formed || !(mbInstance.cache instanceof TurbineCache tc)) {
            if (rotationSpeed != 0f || flow != 0 || bladeCount != 0 || activeCoils != 0
                    || coilEfficiency != 0 || realFlow != 0 || maxFlow != 0 || flowRatio != 0
                    || energyPerTick != 0 || maxEnergyGen != 0) {
                rotationSpeed = 0f;
                resetStats();
                markDirty();
            }
            return;
        }

        runTurbine(tc);
        syncFromCache(tc);
    }

    private void runTurbine(TurbineCache tc) {
        double realFlowVal = 0;
        double maxFlowVal = 0;
        int genPerTick = 0;
        int maxGen = 0;
        double powerMod = 1.0;

        FluidStackHandler tanks = fluidTanks();
        if (tc.flow > 0 && tc.bladeCount > 0) {
            double flowD = Math.max(1, tc.flow);
            double bladeFlow = Multiblocks.turbineBladeFlow;
            maxFlowVal = flowD * bladeFlow * Math.pow(Math.log10(flowD), 2.8);
            double coilsDrag = tc.coilsEfficiency > 0
                    ? Math.max(1, 100.0 / tc.coilsEfficiency * Math.log(Math.log10(tc.activeCoils + 4) + 2))
                    : 1;

            FluidStack in = tanks != null ? tanks.getFluidInTank(0) : FluidStack.EMPTY;
            TurbineRecipe recipe = in.isEmpty() ? null : findRecipe(in);
            if (recipe != null && in.getAmount() > 0) {
                powerMod = recipe.powerModifier();
                double cleanFlow = Math.min(maxFlowVal, in.getAmount());
                realFlowVal = cleanFlow / coilsDrag;
                if (realFlowVal > 0) {
                    genPerTick = generateEnergy(tc, realFlowVal, powerMod);
                    int drained = tanks.drainTank(0, (int) realFlowVal, EXECUTE).getAmount();
                    if (drained > 0) {
                        FluidStack out = recipe.output().resolve();
                        if (!out.isEmpty()) {
                            tanks.fillTank(1, new FluidStack(out.getFluid(), drained), EXECUTE);
                        }
                    }
                }
            }
            maxGen = (int) computeEnergy(tc, maxFlowVal, powerMod);
        }

        updateStats(tc, realFlowVal, maxFlowVal, genPerTick, maxGen);

        double flowForSpin = Math.max(1, tc.flow);
        float newSpeed = (float) ((tc.rotationSpeed * 4 + realFlowVal / (flowForSpin * Multiblocks.turbineBladeFlow)) / 5f);
        tc.rotationSpeed = newSpeed < 0.001f ? 0f : newSpeed;
    }

    private double computeEnergy(TurbineCache tc, double flowValue, double powerModifier) {
        if (tc.activeCoils < 1 || tc.coilsEfficiency <= 0 || tc.bladeCount <= 0) return 0;
        double bladesEfficiency = tc.flow / tc.bladeCount;
        double efficiencyRate = Math.log10(tc.activeCoils) * tc.coilsEfficiency * bladesEfficiency / 1000.0;
        if (efficiencyRate <= 0) return 0;
        return Math.sqrt((flowValue + 1) * (flowValue + 2) / 2.0)
                * Multiblocks.turbineEnergyGen * efficiencyRate * powerModifier * 4;
    }

    private int generateEnergy(TurbineCache tc, double realFlowVal, double powerModifier) {
        double energy = computeEnergy(tc, realFlowVal, powerModifier);
        if (energy <= 0 || energyStorage == null) return 0;
        int add = (int) Math.min(energy, energyStorage.getMaxEnergyStored() - energyStorage.getEnergyStored());
        if (add > 0) energyStorage.setEnergyStored(energyStorage.getEnergyStored() + add);
        return (int) energy;
    }

    private void resetStats() {
        flow = 0;
        bladeCount = 0;
        activeCoils = 0;
        coilEfficiency = 0;
        realFlow = 0;
        maxFlow = 0;
        flowRatio = 0;
        energyPerTick = 0;
        maxEnergyGen = 0;
    }

    private void updateStats(TurbineCache tc, double realFlowVal, double maxFlowVal, int genPerTick, int maxGen) {
        int rf = (int) Math.min(realFlowVal, maxFlowVal);
        int mf = (int) maxFlowVal;
        int ratio = mf > 0 ? (int) ((float) rf / mf * 100) : 0;
        boolean changed = flow != (int) tc.flow || bladeCount != tc.bladeCount || activeCoils != tc.activeCoils
                || coilEfficiency != (int) tc.coilsEfficiency || realFlow != rf || maxFlow != mf
                || flowRatio != ratio || energyPerTick != genPerTick || maxEnergyGen != maxGen;
        flow = (int) tc.flow;
        bladeCount = tc.bladeCount;
        activeCoils = tc.activeCoils;
        coilEfficiency = (int) tc.coilsEfficiency;
        realFlow = rf;
        maxFlow = mf;
        flowRatio = ratio;
        energyPerTick = genPerTick;
        maxEnergyGen = maxGen;
        if (changed) markDirty();
    }

    private TurbineRecipe findRecipe(FluidStack in) {
        if (!(level instanceof ServerLevel sl)) return null;
        for (RecipeHolder<TurbineRecipe> holder : sl.getRecipeManager().getAllRecipesFor(TurbineRecipes.TURBINE_TYPE.get())) {
            if (holder.value().input().test(in)) return holder.value();
        }
        return null;
    }

    private FluidStackHandler fluidTanks() {
        FluidCapabilityHandler fh = contentHandler.getFluidHandler();
        return fh != null ? fh.getInternalHandler() : null;
    }

    private void syncFromCache(TurbineCache tc) {
        boolean changed = false;
        if (tc.axis != null) {
            Direction newOrientation = Direction.fromAxisAndDirection(tc.axis, Direction.AxisDirection.POSITIVE);
            if (newOrientation != orientation) {
                orientation = newOrientation;
                changed = true;
            }
        }
        BlockPos b1 = tc.bearingPos1 == Long.MIN_VALUE ? null : BlockPos.of(tc.bearingPos1);
        BlockPos b2 = tc.bearingPos2 == Long.MIN_VALUE ? null : BlockPos.of(tc.bearingPos2);
        if (!Objects.equals(b1, bearingPos1)) {
            bearingPos1 = b1;
            changed = true;
        }
        if (!Objects.equals(b2, bearingPos2)) {
            bearingPos2 = b2;
            changed = true;
        }
        if (width != tc.width) {
            width = tc.width;
            changed = true;
        }
        if (height != tc.height) {
            height = tc.height;
            changed = true;
        }
        if (depth != tc.depth) {
            depth = tc.depth;
            changed = true;
        }
        if (Math.abs(rotationSpeed - tc.rotationSpeed) > 0.001f) {
            rotationSpeed = tc.rotationSpeed;
            changed = true;
        }
        if (changed) markDirty();
    }

    @Override
    public void clientTick() {
        super.clientTick();
        if (formed && rotationSpeed > 0f) {
            spawnSteamParticles();
        }
    }

    private void spawnSteamParticles() {
        if (level == null || bearingPos1 == null || bearingPos2 == null) return;

        int interval = (int) Math.ceil(Math.log(1.0 / (rotationSpeed + 0.001)) + 1);
        if (interval < 1) interval = 1;
        if (level.getGameTime() % interval != 0) return;

        Direction.Axis axis = orientation.getAxis();
        int axisDim = switch (axis) {
            case X -> width;
            case Y -> height;
            default -> depth;
        };
        float mag = 0.05f + Math.max(0, axisDim - 1) * 0.03f;

        int c1 = axis.choose(bearingPos1.getX(), bearingPos1.getY(), bearingPos1.getZ());
        int c2 = axis.choose(bearingPos2.getX(), bearingPos2.getY(), bearingPos2.getZ());
        Direction positive = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE);
        Direction vent1 = c1 >= c2 ? positive : positive.getOpposite();

        switch (orientation) {
            case NORTH, SOUTH -> emitSteam(bearingPos1.relative(orientation, -2), axis, vent1.getOpposite(), mag);
            case EAST, WEST -> emitSteam(bearingPos2.relative(orientation, -2), axis, vent1, mag);
            case UP, DOWN -> emitSteam(bearingPos2.relative(orientation, 2), axis, vent1, mag);
        }
        //emitSteam(bearingPos2, axis, vent1, mag);
    }

    private void emitSteam(BlockPos bearing, Direction.Axis axis, Direction vent, float mag) {
        BlockPos center = bearing.relative(vent.getOpposite());
        double vx = axis == Direction.Axis.X ? mag * vent.getStepX() : 0;
        double vy = axis == Direction.Axis.Y ? mag * vent.getStepY() : 0;
        double vz = axis == Direction.Axis.Z ? mag * vent.getStepZ() : 0;
        for (BlockPos corner : cornerOffsets(center, axis)) {
            for (int i = 0; i < 2; i++) {
                double x = corner.getX() + 0.5 + level.random.nextGaussian() * 0.2;
                double y = corner.getY() + 0.5 + level.random.nextGaussian() * 0.2;
                double z = corner.getZ() + 0.5 + level.random.nextGaussian() * 0.2;
                level.addParticle(ParticleTypes.CLOUD, x, y, z, vx, vy, vz);
            }
        }
    }

    private List<BlockPos> cornerOffsets(BlockPos pos, Direction.Axis axis) {
        List<BlockPos> positions = new ArrayList<>();
        switch (axis) {
            case X -> {
                positions.add(pos.offset(0, -1, -1));
                positions.add(pos.offset(0, -1, 1));
                positions.add(pos.offset(0, 1, 1));
                positions.add(pos.offset(0, 1, -1));
            }
            case Y -> {
                positions.add(pos.offset(-1, 0, -1));
                positions.add(pos.offset(-1, 0, 1));
                positions.add(pos.offset(1, 0, 1));
                positions.add(pos.offset(1, 0, -1));
            }
            case Z -> {
                positions.add(pos.offset(-1, -1, 0));
                positions.add(pos.offset(1, -1, 0));
                positions.add(pos.offset(1, 1, 0));
                positions.add(pos.offset(-1, 1, 0));
            }
        }
        return positions;
    }
}
