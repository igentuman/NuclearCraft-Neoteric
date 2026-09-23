package igentuman.nc.block_entity.particle;

import igentuman.nc.api.particle.IParticleHandler;
import igentuman.nc.api.particle.ParticleStack;
import igentuman.nc.block.accelerator.BeamPortMode;
import igentuman.nc.block.particle.ParticleChamberBeamPortBlock;
import igentuman.nc.block_entity.IBeamPort;
import igentuman.nc.block_entity.MultiblockControllerBE;
import igentuman.nc.block_entity.ParticleBeamPortState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.world.level.block.Block.UPDATE_CLIENTS;

public class ParticleChamberBeamPortBE extends ParticleChamberPortBE implements IBeamPort {

    private final ParticleBeamPortState particlePort = new ParticleBeamPortState();
    protected ParticleStack lastTransferSnapshot = ParticleStack.EMPTY;
    protected long lastTransferGameTime;
    protected Direction lastTransferDirection;

    public ParticleChamberBeamPortBE(BlockEntityType<?> type, BlockPos pos, BlockState state, String name) {
        super(type, pos, state, name);
    }

    @Override
    public void configureFromController(MultiblockControllerBE controller) {
        super.configureFromController(controller);
        particlePort.configure(controller::rolePositions, worldPosition);
        updateModeBlockState();
        markDirty();
    }

    @Override
    public void clearStructureConfiguration() {
        particlePort.detach();
        updateModeBlockState();
        markDirty();
    }

    @Override
    public BeamPortMode beamPortMode() {
        return particlePort.mode();
    }

    @Override
    public void configureMode(BeamPortMode mode, int channel) {
        if (particlePort.mode() == mode && particlePort.channel() == channel) {
            return;
        }
        particlePort.set(mode, channel);
        updateModeBlockState();
        markDirty();
    }

    @Nullable
    @Override
    public IParticleHandler getParticleHandler(@Nullable Direction side) {
        if (particlePort.mode() == BeamPortMode.DISABLED || side == null
                || !getBlockState().hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                || getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING) != side) return null;
        MultiblockControllerBE controller = controller();
        if (controller == null || !controller.structureFormed()) return null;
        return controller.getParticleHandler(worldPosition, particlePort.mode(), particlePort.channel());
    }

    @Override
    public void serverTick() {
        super.serverTick();
        ensureConfigured();
        updateModeBlockState();
    }

    @Override
    public void recordParticleTransfer(ParticleStack stack, long gameTime, Direction direction) {
        lastTransferSnapshot = stack;
        lastTransferGameTime = gameTime;
        lastTransferDirection = direction;
        markDirty();
    }

    private void ensureConfigured() {
        if (particlePort.isConfigured()) return;
        MultiblockControllerBE controller = controller();
        if (controller != null && controller.structureFormed()) configureFromController(controller);
    }

    private void updateModeBlockState() {
        if (level == null || level.isClientSide || !getBlockState().hasProperty(ParticleChamberBeamPortBlock.PORT_MODE)
                || getBlockState().getValue(ParticleChamberBeamPortBlock.PORT_MODE) == particlePort.mode()) return;
        level.setBlock(worldPosition, getBlockState().setValue(ParticleChamberBeamPortBlock.PORT_MODE,
                particlePort.mode()), UPDATE_CLIENTS);
        level.invalidateCapabilities(worldPosition);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("particlePortMode", particlePort.mode().getSerializedName());
        tag.putInt("particlePortChannel", particlePort.channel());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        particlePort.load(tag.getString("particlePortMode"), tag.getInt("particlePortChannel"));
    }
}
