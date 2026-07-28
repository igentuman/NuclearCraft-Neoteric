package igentuman.nc.block.accelerator.entity;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.MultiblockPortBE;
import igentuman.nc.block.target_chamber.entity.TargetChamberBeamPortBE;
import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.multiblock.AbstractMultiblock;
import igentuman.nc.multiblock.accelerator.AbstractAcceleratorMultiblock;
import igentuman.nc.util.Equations;
import igentuman.nc.util.PortMode;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
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
import static igentuman.nc.util.PortMode.PORT_MODE;

public class AcceleratorBeamPortBE extends MultiblockPortBE {

    public static String NAME = "accelerator_beam_port";

    protected AbstractAcceleratorMultiblock multiblock;
    @NBTField
    public ParticleStack clientParticle;
    public AbstractAcceleratorControllerBE controller;
    private boolean alreadySentParticle = false;

    public AcceleratorBeamPortBE(BlockPos pPos, BlockState pBlockState) {
        super(ACCELERATOR_BE.get(NAME).get(), pPos, pBlockState);
    }

    @Override
    public void setMultiblock(AbstractMultiblock multiblock) {
        if(multiblock == this.multiblock) return;
        this.multiblock = (AbstractAcceleratorMultiblock) multiblock;
        if (this.multiblock != null) {
            controllerPos = this.multiblock.controller().controllerBE().getBlockPos();
            controller = (AbstractAcceleratorControllerBE) this.multiblock.controller().controllerBE();
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
        alreadySentParticle = false;
        boolean updated = updateController() || currentTick % 40 == 0;
        if(!isConnectedToController()) return;
        updateIfNeeded(updated);
    }

    protected void saveClientData(CompoundTag tag) {
        super.saveClientData(tag);
        clientParticle = ParticleStack.EMPTY;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(!isConnectedToController()) return super.getCapability(cap, side);
        if (cap == PARTICLE_HANDLER_CAPABILITY) {
            return controller().particleHandler().cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public AbstractAcceleratorControllerBE controller() {
        return (AbstractAcceleratorControllerBE) super.controller();
    }

    public boolean extractParticle(ParticleStack particleStack) {
        if(alreadySentParticle || !isConnectedToController() || particleStack == null || particleStack.getAmount() <= 0) return false;
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
            BlockEntity be = level.getExistingBlockEntity(currentPos);
            if (be instanceof AcceleratorBeamPortBE targetPort) {
                if (targetPort.getFacing() == facing.getOpposite() && targetPort.isInput() && targetPort.controller() != null) {
                    int finalDistance = distance;
                    targetPort.controller().getCapability(PARTICLE_HANDLER_CAPABILITY, facing.getOpposite())
                            .ifPresent(handler -> {
                                particleStack.addFocus(-Equations.focusLoss(finalDistance, particleStack));
                                handler.reciveParticle(facing.getOpposite(), particleStack);
                                result.set(true);
                            });
                }
                break;
            } else if (be instanceof TargetChamberBeamPortBE targetPort && targetPort.isInput()) {
                if (targetPort.getFacing() == facing.getOpposite()) {
                    int finalDistance1 = distance;
                    targetPort.getCapability(PARTICLE_HANDLER_CAPABILITY, facing.getOpposite())
                            .ifPresent(handler -> {
                                particleStack.addFocus(-Equations.focusLoss(finalDistance1, particleStack));
                                handler.reciveParticle(facing.getOpposite(), particleStack);
                                result.set(true);
                            });
                }
                break;
            } else if (be != null) {
                int finalDistance2 = distance;
                be.getCapability(PARTICLE_HANDLER_CAPABILITY, facing.getOpposite())
                        .ifPresent(handler -> {
                            particleStack.addFocus(-Equations.focusLoss(finalDistance2, particleStack));
                            handler.reciveParticle(facing.getOpposite(), particleStack);
                            result.set(true);
                        });
                break;
            }
            else {
                break;
            }
        }
        alreadySentParticle = result.get();
        clientParticle = particleStack.copy();
        markDirty();
        return result.get();
    }

    public boolean isOutput() {
        return blockState.getValue(PORT_MODE) == PortMode.Mode.OUTPUT;
    }

    public boolean isInput() {
        return blockState.getValue(PORT_MODE) == PortMode.Mode.INPUT;
    }
}
