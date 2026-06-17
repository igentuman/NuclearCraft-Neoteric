package igentuman.nc.block.accelerator.entity;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.MultiblockPortBE;
import igentuman.nc.block.target_chamber.entity.TargetChamberBeamPortBE;
import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.multiblock.AbstractMultiblock;
import igentuman.nc.multiblock.accelerator.AbstractAcceleratorMultiblock;
import igentuman.nc.util.Equations;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicBoolean;

import static igentuman.nc.NuclearCraft.currentTick;
import static igentuman.nc.content.particles.CapabilityParticleStackHandler.PARTICLE_HANDLER_CAPABILITY;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_BE;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_BLOCKS;

public class AcceleratorBeamPortBE extends MultiblockPortBE {

    public static String NAME = "accelerator_beam_port";

    protected AbstractAcceleratorMultiblock multiblock;
    @NBTField
    public ParticleStack clientParticle;
    public LinearAcceleratorControllerBE controller;

    public AcceleratorBeamPortBE(BlockPos pPos, BlockState pBlockState) {
        super(ACCELERATOR_BE.get(NAME).get(), pPos, pBlockState);
    }

    @Override
    public void setMultiblock(AbstractMultiblock multiblock) {
        if(multiblock == this.multiblock) return;
        this.multiblock = (AbstractAcceleratorMultiblock) multiblock;
        if (this.multiblock != null) {
            controllerPos = this.multiblock.controller().controllerBE().getBlockPos();
            controller = (LinearAcceleratorControllerBE) this.multiblock.controller().controllerBE();
            markDirty();
        }
    }

    @Override
    public AbstractAcceleratorMultiblock getMultiblock() {
        return multiblock;
    }

    @Override
    public void tickServer() {
        if(lastTickTime == currentTick || NuclearCraft.instance.isNcBeStopped || isRemoved()) return;
        lastTickTime = currentTick;
        boolean updated = updateController();
        if(!isConnectedToController()) return;
        updateIfNeeded(updated);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(isConnectedToController()) return super.getCapability(cap, side);
        if (cap == PARTICLE_HANDLER_CAPABILITY) {
            return controller().getCapability(cap, side);
        }
        return LazyOptional.empty();
    }

    @Override
    public AbstractAcceleratorControllerBE controller() {
        return (AbstractAcceleratorControllerBE) super.controller();
    }

    public boolean extractParticle(ParticleStack particleStack) {
        if(controller() == null || particleStack == null || particleStack.getAmount() <= 0) return false;
        AtomicBoolean result = new AtomicBoolean(false);
        Direction facing = getFacing();
        BlockPos currentPos = worldPosition.relative(facing);
        int maxDistance = 16;
        
        for (int distance = 0; distance < maxDistance; distance++) {

            assert level != null;
            BlockState blockState = level.getBlockState(currentPos);
            if (blockState.is(ACCELERATOR_BLOCKS.get("particle_beam").get())) {
                currentPos = currentPos.relative(facing);
                continue;
            }
            
            if (level.getBlockEntity(currentPos) instanceof AcceleratorBeamPortBE targetPort) {
                if (targetPort.getFacing() == facing.getOpposite()) {
                    if (targetPort.controller() != null) {
                        particleStack.addFocus(-Equations.focusLoss(distance, particleStack));
                        targetPort.controller().getCapability(PARTICLE_HANDLER_CAPABILITY, facing.getOpposite())
                                .ifPresent(handler -> {
                                    handler.reciveParticle(facing.getOpposite(), particleStack);
                                    result.set(true);
                                });
                    }
                }
                break;
            } else if (level.getBlockEntity(currentPos) instanceof TargetChamberBeamPortBE targetPort) {
                if (targetPort.getFacing() == facing.getOpposite()) {
                    if (targetPort.controller() != null) {
                        particleStack.addFocus(-Equations.focusLoss(distance, particleStack));
                        targetPort.getCapability(PARTICLE_HANDLER_CAPABILITY, facing.getOpposite())
                                .ifPresent(handler -> {
                                    handler.reciveParticle(facing.getOpposite(), particleStack);
                                    result.set(true);
                                });
                    }
                }
                break;
            } else {
                break;
            }
        }
        controller().particleStorage.clearServer();
        clientParticle = controller().particleStorage.getClientParticleStack();
        markDirty();
        return result.get();
    }
}
