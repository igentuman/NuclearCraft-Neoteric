package igentuman.nc.block_entity.particle;

import igentuman.nc.api.particle.IParticleHandler;
import igentuman.nc.api.particle.ParticleAction;
import igentuman.nc.api.particle.ParticleDefinition;
import igentuman.nc.api.particle.ParticleStack;
import igentuman.nc.block.UniversalProcessorBlock;
import igentuman.nc.block_entity.MultiblockPortBE;
import igentuman.nc.block_entity.UniversalProcessorBE;
import igentuman.nc.config.Multiblocks;
import igentuman.nc.container.CreativeParticleSourceContainer;
import igentuman.nc.multiblock.accelerator.LinearParticleProcessor;
import igentuman.nc.particle.BeamConnection;
import igentuman.nc.particle.BeamConnectionResolver;
import igentuman.nc.particle.ParticleStorage;
import igentuman.nc.particle.ParticleTransfer;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.setup.Registers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CreativeParticleSourceBE extends UniversalProcessorBE {

    private static final long BATCH_SIZE = 10_000;
    private static final long[] ENERGY_MULTIPLIERS = {1L, 1_000L, 1_000_000L, 1_000_000_000L};

    @Nullable
    private ResourceLocation particleId;
    private double focus = 1D;
    private double energyValue = 1D;
    private int energyScale = 1;
    private BeamConnection outputConnection;

    public CreativeParticleSourceBE(BlockPos pos, BlockState state, String name) {
        super(pos, state, name);
    }

    @Nullable
    public ResourceLocation particleId() {
        return particleId;
    }

    public double focus() {
        return focus;
    }

    public double energyValue() {
        return energyValue;
    }

    public int energyScale() {
        return energyScale;
    }

    public long energyKeV() {
        return energyKeV(energyValue, energyScale);
    }

    public static long energyKeV(double value, int scale) {
        if (!Double.isFinite(value) || value < 0 || scale < 0 || scale >= ENERGY_MULTIPLIERS.length) {
            throw new IllegalArgumentException("Invalid creative particle source energy");
        }
        double energy = value * ENERGY_MULTIPLIERS[scale];
        if (!Double.isFinite(energy) || energy > Long.MAX_VALUE) {
            throw new ArithmeticException("Creative particle source energy exceeds the supported range");
        }
        return (long) energy;
    }

    public boolean updateSettings(ResourceLocation newParticleId, double newFocus,
                                  double newEnergyValue, int newEnergyScale) {
        if (newParticleId == null || !Registers.PARTICLE_DEFINITION_REGISTRY.containsKey(newParticleId)
                || !Double.isFinite(newFocus) || newFocus < 0
                || newEnergyScale < 0 || newEnergyScale >= ENERGY_MULTIPLIERS.length) return false;
        try {
            energyKeV(newEnergyValue, newEnergyScale);
        } catch (ArithmeticException | IllegalArgumentException ignored) {
            return false;
        }
        particleId = newParticleId;
        focus = newFocus;
        energyValue = newEnergyValue;
        energyScale = newEnergyScale;
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        return true;
    }

    @Override
    public void serverTick() {
        if (!(level instanceof ServerLevel serverLevel) || !ModEntries.isEnabled(name)) return;
        boolean sent = sendParticle(serverLevel);
        BlockState state = getBlockState();
        if (state.hasProperty(UniversalProcessorBlock.POWERED)
                && state.getValue(UniversalProcessorBlock.POWERED) != sent) {
            serverLevel.setBlock(worldPosition, state.setValue(UniversalProcessorBlock.POWERED, sent), Block.UPDATE_CLIENTS);
        }
    }

    private boolean sendParticle(ServerLevel serverLevel) {
        if (particleId == null) return false;
        ParticleDefinition definition = Registers.PARTICLE_DEFINITION_REGISTRY.get(particleId);
        if (definition == null || !getBlockState().hasProperty(BlockStateProperties.HORIZONTAL_FACING)) return false;
        Direction direction = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite();
        outputConnection = BeamConnectionResolver.resolve(serverLevel, outputConnection, worldPosition, direction,
                Multiblocks.particleMachines().accelerator().beamConnectionReach());
        if (outputConnection == null) return false;
        IParticleHandler destination = BeamConnectionResolver.destination(serverLevel, outputConnection);
        if (destination == null) return false;

        ParticleStack packet;
        try {
            packet = new ParticleStack(particleId, BATCH_SIZE, energyKeV(), focus);
        } catch (ArithmeticException | IllegalArgumentException ignored) {
            return false;
        }
        ParticleStorage source = new ParticleStorage(1, BATCH_SIZE);
        source.insert(0, packet, ParticleAction.EXECUTE);
        int corridorLength = BeamConnectionResolver.corridorLength(outputConnection);
        ParticleTransfer.Result result;
        try {
            result = ParticleTransfer.execute(new ParticleTransfer.Request(source, 0, destination, 0,
                    List.of(serverLevel.dimension().location(), worldPosition.immutable()), BATCH_SIZE,
                    stack -> LinearParticleProcessor.attenuateConnection(stack, definition,
                            Multiblocks.particleMachines().accelerator(), corridorLength)));
        } catch (ArithmeticException | IllegalArgumentException ignored) {
            return false;
        }
        if (result.transferred().isEmpty()) return false;
        BlockEntity destinationEntity = serverLevel.getBlockEntity(outputConnection.destination());
        if (destinationEntity instanceof MultiblockPortBE port) {
            port.recordParticleTransfer(result.transferred(), serverLevel.getGameTime(), direction);
        }
        return true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (particleId != null) tag.putString("particleId", particleId.toString());
        tag.putDouble("particleFocus", focus);
        tag.putDouble("particleEnergyValue", energyValue);
        tag.putInt("particleEnergyScale", energyScale);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ResourceLocation savedId = ResourceLocation.tryParse(tag.getString("particleId"));
        double savedFocus = tag.getDouble("particleFocus");
        double savedValue = tag.getDouble("particleEnergyValue");
        int savedScale = tag.getInt("particleEnergyScale");
        if (savedId != null && Double.isFinite(savedFocus) && savedFocus >= 0 && savedValue >= 0
                && savedScale >= 0 && savedScale < ENERGY_MULTIPLIERS.length) {
            try {
                energyKeV(savedValue, savedScale);
                particleId = savedId;
                focus = savedFocus;
                energyValue = savedValue;
                energyScale = savedScale;
            } catch (ArithmeticException | IllegalArgumentException ignored) {
            }
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.nuclearcraft.creative_particle_source");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new CreativeParticleSourceContainer(containerId, playerInventory, this, containerData);
    }
}
