package igentuman.nc.compat.gregtech;

import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.PlatformEnergyCompat;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import igentuman.nc.block.entity.NuclearCraftBE;
import igentuman.nc.handler.config.CommonConfig.GTCEUCompatibilityConfig.GTCEUTier;
import igentuman.nc.util.capability.CustomEnergyStorage;
import igentuman.nc.util.TextUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;

import static igentuman.nc.handler.config.CommonConfig.GTCEUCompatibilityConfig.GTCEUCompatibility.ONLY_GTCEU;
import static igentuman.nc.handler.config.CommonConfig.GTCEU_CONFIG;

public class GTUtils {

    public static boolean isOnlyGTCEUCapEnabled() {
        return GTCEU_CONFIG.COMPATIBILITY.get() == ONLY_GTCEU;
    }

    public static String formatEUTier(int capacity) {
        long tier = tierByFe(capacity);
        return GTCEUTier.values()[(int) tier].name();
    }

    public static String formatEUEnergy(int energy)
    {
        energy = energy / FE2EURatio();
        if(energy >= 1000000) {
            return TextUtils.numberFormat(energy/1000000d)+" MEU";
        }
        if(energy >= 1000) {
            return TextUtils.numberFormat(energy/1000d)+" kEU";
        }
        return TextUtils.numberFormat(energy)+" EU";
    }

    //todo usually tier is defined for each container separately, keep it return 3 just in case if no tier is defined
    public static long tierByFe(int fe) {
        long tier = 3L;
        /*while (fe % 4 == 0) {
            fe = fe / 4;
            tier += 1;
        }*/
        return tier;
    }

    public static LazyOptional<IEnergyContainer> getGTEnergy(NuclearCraftBE energyHolder, @Nullable Direction side) {
        return GTEnergyContainer.wrapped(energyHolder.energyStorage(), side, energyHolder).cast();
    }

    public static int convert2FE(long eu) {
        long converted = eu * FE2EURatio();
        if(converted > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if(converted < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        return (int) converted;
    }
    public static int convert2EU(int fe) {
        return (int) (fe / FE2EURatio());
    }

    public static int FE2EURatio() {
        return PlatformEnergyCompat.ratio(true);
    }

    public static int EU2FERatio() {
        return PlatformEnergyCompat.ratio(false);
    }

    public static void transferEU(NuclearCraftBE nuclearCraftBE, BlockEntity be, CustomEnergyStorage energyStorage, Direction direction) {
        int amps = (int) (energyStorage.getEnergyStored() / (energyStorage.getGTOuputVoltage() * EU2FERatio()));
        amps = (int) Math.min(amps, energyStorage.getGTOutputAmperage());
        if(amps < 1) {
            return;
        }
        if (be.getCapability(GTCapability.CAPABILITY_ENERGY_CONTAINER, direction.getOpposite()).isPresent()) {
            IEnergyContainer gtEnergyContainer = be.getCapability(GTCapability.CAPABILITY_ENERGY_CONTAINER, direction.getOpposite()).orElse(null);
            if (gtEnergyContainer != null) {
                long outAmps = gtEnergyContainer.acceptEnergyFromNetwork(direction.getOpposite(), energyStorage.getGTOuputVoltage(), amps);
                long received = outAmps * energyStorage.getGTOuputVoltage();
                energyStorage.consumeEnergy((int) received * EU2FERatio());
                nuclearCraftBE.setChanged();
            }
        }
    }

    public static int getMaxOutputFE(GTCEUTier gtceuTier) {
        long voltage = CustomEnergyStorage.V[gtceuTier.ordinal()];
        long amperage = 16;
        return convert2FE(voltage * amperage);
    }
}
