package igentuman.nc.block_entity.turbine;

import igentuman.nc.block_entity.MultiblockControllerBE;
import igentuman.nc.handler.fluid.FluidStackHandler;
import igentuman.nc.handler.sided.FluidCapabilityHandler;
import igentuman.nc.multiblock.turbine.TurbineCache;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TurbineControllerBE extends MultiblockControllerBE {

    @NBTField(syncToClient = true)
    public float rotationSpeed = 0f;
    @NBTField(syncToClient = true)
    public Direction orientation = Direction.NORTH;
    @NBTField(syncToClient = true)
    public BlockPos bearingPos1;
    @NBTField(syncToClient = true)
    public BlockPos bearingPos2;
    @NBTField(syncToClient = true)
    public int width = 0;
    @NBTField(syncToClient = true)
    public int height = 0;
    @NBTField(syncToClient = true)
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
        if (mbInstance != null && mbInstance.formed) return;
        if (rotationSpeed != 0f || flow != 0 || bladeCount != 0 || activeCoils != 0
                || coilEfficiency != 0 || realFlow != 0 || maxFlow != 0 || flowRatio != 0
                || energyPerTick != 0 || maxEnergyGen != 0) {
            rotationSpeed = 0f;
            flow = 0;
            bladeCount = 0;
            activeCoils = 0;
            coilEfficiency = 0;
            realFlow = 0;
            maxFlow = 0;
            flowRatio = 0;
            energyPerTick = 0;
            maxEnergyGen = 0;
            markDirty();
        }
    }

    public void updateRuntimeDisplay(TurbineCache tc, float speed, int real, int max, int genPerTick, int maxGen) {
        int ratio = max > 0 ? (int) ((float) real / max * 100) : 0;
        Direction newOrientation = tc.axis == null ? orientation
                : Direction.fromAxisAndDirection(tc.axis, Direction.AxisDirection.POSITIVE);
        boolean changed = Math.abs(rotationSpeed - speed) > 0.001f || (speed == 0f && rotationSpeed != 0f)
                || flow != (int) tc.flow || bladeCount != tc.bladeCount || activeCoils != tc.activeCoils
                || coilEfficiency != (int) tc.coilsEfficiency || realFlow != real || maxFlow != max
                || flowRatio != ratio || energyPerTick != genPerTick || maxEnergyGen != maxGen
                || orientation != newOrientation || !Objects.equals(bearingPos1, tc.bearingPos1)
                || !Objects.equals(bearingPos2, tc.bearingPos2)
                || width != tc.width || height != tc.height || depth != tc.depth;
        rotationSpeed = speed;
        flow = (int) tc.flow;
        bladeCount = tc.bladeCount;
        activeCoils = tc.activeCoils;
        coilEfficiency = (int) tc.coilsEfficiency;
        realFlow = real;
        maxFlow = max;
        flowRatio = ratio;
        energyPerTick = genPerTick;
        maxEnergyGen = maxGen;
        orientation = newOrientation;
        bearingPos1 = tc.bearingPos1;
        bearingPos2 = tc.bearingPos2;
        width = tc.width;
        height = tc.height;
        depth = tc.depth;
        if (changed) markDirty();
    }

    @Nullable
    public FluidStackHandler fluidTanks() {
        FluidCapabilityHandler fh = contentHandler.getFluidHandler();
        return fh != null ? fh.getInternalHandler() : null;
    }

    @Override
    protected CompoundTag legacyRuntime(CompoundTag tag) {
        return tag.contains("rotationSpeed") ? tag : null;
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
