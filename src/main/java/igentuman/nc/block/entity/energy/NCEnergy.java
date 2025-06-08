package igentuman.nc.block.entity.energy;

import igentuman.nc.block.entity.NuclearCraftBE;
import igentuman.nc.setup.registration.NCEnergyBlocks;
import igentuman.nc.util.CustomEnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicInteger;

import static igentuman.nc.compat.gregtech.GTUtils.getGTEnergy;
import static igentuman.nc.compat.gregtech.GTUtils.isOnlyGTCEUCapEnabled;
import static igentuman.nc.handler.config.CommonConfig.GTCEUCompatibilityConfig.GTCEUCompatibility.ONLY_GTCEU;
import static igentuman.nc.handler.config.CommonConfig.GTCEU_CONFIG;
import static igentuman.nc.util.ModUtil.isGtLoaded;
import static net.minecraftforge.common.capabilities.ForgeCapabilities.ENERGY;

public class NCEnergy extends NuclearCraftBE {

    protected String name;
    public static String NAME;
    public final CustomEnergyStorage energyStorage;
    protected final LazyOptional<IEnergyStorage> energy;

    public NCEnergy(BlockPos pPos, BlockState pBlockState, String name) {
        super(NCEnergyBlocks.ENERGY_BE.get(name).get(), pPos, pBlockState);
        this.name = name;
        energyStorage = createEnergy();
        energyStorage.setOutputEnergyTier(getOutputEnergyTier())
                .setInputEnergyTier(getInputEnergyTier());
        energy = LazyOptional.of(() -> energyStorage);
    }

    @Override
    public CustomEnergyStorage energyStorage() {
        return energyStorage;
    }

    @Override
    public LazyOptional<IEnergyStorage> getEnergy() {
        return energy;
    }

    protected int getEnergyMaxStorage() {
        return 100;
    }

    protected int getEnergyTransferPerTick() {
        return Math.min(getEnergyMaxStorage(), energyStorage().getEnergyStored());
    }

    private CustomEnergyStorage createEnergy() {
        return new CustomEnergyStorage(getEnergyMaxStorage(), getMaxTransfer(), getEnergyMaxStorage()) {
            @Override
            protected void onEnergyChanged() {
                setChanged();
            }
        };
    }

    public int getMaxTransfer() {
        return 0;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(isGtLoaded()) {
            if (cap == com.gregtechceu.gtceu.api.capability.forge.GTCapability.CAPABILITY_ENERGY_CONTAINER) {
                if (isGTEUCapEnabled()) {
                    return getGTEnergy(this, side).cast();
                }
            }
        }
        if (cap == ENERGY) {
            if(!isOnlyGTCEUCapEnabled()) {
                return getEnergy().cast();
            } else {
                return LazyOptional.empty();
            }
        }
        return super.getCapability(cap, side);
    }

    public String getName() {
        return name;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        getEnergy().invalidate();
    }
}
