package igentuman.nc.block.target_chamber.entity;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.MultiblockPortBE;
import igentuman.nc.block.accelerator.entity.AcceleratorBeamPortBE;
import igentuman.nc.block.entity.ParticleChamberControllerBE;
import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.multiblock.AbstractMultiblock;
import igentuman.nc.multiblock.particle_chamber.ParticleChamberMultiblock;
import igentuman.nc.util.Equations;
import igentuman.nc.util.PortMode;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicBoolean;

import static igentuman.nc.NuclearCraft.currentTick;
import static igentuman.nc.content.particles.CapabilityParticleStackHandler.PARTICLE_HANDLER_CAPABILITY;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_BLOCKS;
import static igentuman.nc.multiblock.particle_chamber.ParticleChamberRegistration.TARGET_CHAMBER_BE;
import static igentuman.nc.util.PortMode.PORT_MODE;

public class TargetChamberBeamPortBE extends MultiblockPortBE {

    public static String NAME = "target_chamber_beam_port";

    @NBTField
    public ParticleStack clientParticle;
    protected ParticleChamberMultiblock multiblock;
    public ParticleChamberControllerBE controller;
    private boolean alreadySentParticle = false;

    public TargetChamberBeamPortBE(BlockPos pPos, BlockState pBlockState) {
        super(TARGET_CHAMBER_BE.get(NAME).get(), pPos, pBlockState);
    }

    @Override
    public void setMultiblock(AbstractMultiblock multiblock) {
        this.multiblock = (ParticleChamberMultiblock) multiblock;
        markDirty();
    }

    @Override
    public ParticleChamberMultiblock getMultiblock() {
        return multiblock;
    }

    @Override
    public void tickServer() {
        if(lastTickTime == currentTick || NuclearCraft.instance.isNcBeStopped || isRemoved()) return;
        alreadySentParticle = false;
        lastTickTime = currentTick;
        boolean updated = updateController();
        if(!isConnectedToController()) return;
        updateIfNeeded(updated);
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
    public ParticleChamberControllerBE controller() {
        return (ParticleChamberControllerBE) super.controller();
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
                if (targetPort.getFacing() == facing.getOpposite() && targetPort.isInput()) {
                    int finalDistance = distance;
                    targetPort.getCapability(PARTICLE_HANDLER_CAPABILITY, facing.getOpposite())
                            .ifPresent(handler -> {
                                particleStack.addFocus(-Equations.focusLoss(finalDistance, particleStack));
                                handler.reciveParticle(facing.getOpposite(), particleStack);
                                result.set(true);
                            });
                }
                break;
            } else if (be instanceof TargetChamberBeamPortBE targetPort) {
                if (targetPort.getFacing() == facing.getOpposite() && targetPort.isInput()) {
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
