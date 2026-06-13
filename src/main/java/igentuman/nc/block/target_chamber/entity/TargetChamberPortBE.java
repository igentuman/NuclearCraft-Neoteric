package igentuman.nc.block.target_chamber.entity;

import igentuman.nc.block.entity.ParticleChamberPortBE;
import igentuman.nc.multiblock.particle_chamber.TargetChamberMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static igentuman.nc.compat.oc2.TargetChamberDevice.DEVICE_CAPABILITY;
import static igentuman.nc.multiblock.particle_chamber.ParticleChamberRegistration.TARGET_CHAMBER_BE;
import static igentuman.nc.util.ModUtil.isCcLoaded;
import static igentuman.nc.util.ModUtil.isOC2Loaded;

public class TargetChamberPortBE extends ParticleChamberPortBE<TargetChamberControllerBE, TargetChamberMultiblock> {

    public static final String NAME = "target_chamber_port";
    public boolean isSteamMode = false;

    public TargetChamberPortBE(BlockPos pPos, BlockState pBlockState) {
        this(TARGET_CHAMBER_BE.get(NAME).get(), pPos, pBlockState);
    }

    public TargetChamberPortBE(net.minecraft.world.level.block.entity.BlockEntityType<?> type, BlockPos pPos, BlockState pBlockState) {
        super(type, pPos, pBlockState);
    }

    @Override
    protected Class<TargetChamberControllerBE> controllerClass() {
        return TargetChamberControllerBE.class;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (controller() != null) {
            if (isOC2Loaded() && cap == DEVICE_CAPABILITY) {
                return controller().getOCDevice(cap, side);
            }
            if (isCcLoaded() && cap == dan200.computercraft.shared.Capabilities.CAPABILITY_PERIPHERAL) {
                return controller().getPeripheral(cap, side);
            }
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void tickServer() {
        super.tickServer();
        if (controller() != null && getMultiblock() != null) {
            sendOutPower();
        }
    }

    public igentuman.nc.content.particles.ParticleStack getOutputParticle(int i) {
        if (controller() == null || controller().getRecipe() == null) return null;
        return controller().getRecipe().getOutputParticle(i);
    }
}
