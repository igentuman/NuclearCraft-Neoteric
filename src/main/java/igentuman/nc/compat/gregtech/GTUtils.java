package igentuman.nc.compat.gregtech;

import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.compat.FeCompat;
import com.gregtechceu.gtceu.api.capability.GTCapability;
import igentuman.nc.block.entity.NuclearCraftBE;
import igentuman.nc.handler.config.CommonConfig.GTCEUCompatibilityConfig.GTCEUTier;
import igentuman.nc.util.capability.CustomEnergyStorage;
import igentuman.nc.util.TextUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

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

    // TODO: LazyOptional removed in NeoForge 1.21.1. GTCapability API may also have changed.
    // Verify GTCapability usage with the NeoForge 1.21.1 version of GregTech.
    public static IEnergyContainer getGTEnergy(NuclearCraftBE energyHolder, @Nullable Direction side) {
        return GTEnergyContainer.wrapped(energyHolder.energyStorage(), side, energyHolder);
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
        return FeCompat.ratio(true);
    }

    public static int EU2FERatio() {
        return FeCompat.ratio(false);
    }

    // TODO: GTCapability.CAPABILITY_ENERGY_CONTAINER lookup needs updating for NeoForge 1.21.1.
    // In NeoForge 1.21.1, use level.getCapability(GTCapability.CAPABILITY_ENERGY_CONTAINER, pos, side)
    // or the GregTech NeoForge equivalent. Verify with GregTech NeoForge 1.21.1 API.
    public static void transferEU(NuclearCraftBE nuclearCraftBE, BlockEntity be, CustomEnergyStorage energyStorage, Direction direction) {
        int amps = (int) (energyStorage.getEnergyStored() / (energyStorage.getGTOuputVoltage() * EU2FERatio()));
        amps = (int) Math.min(amps, energyStorage.getGTOutputAmperage());
        if(amps < 1) {
            return;
        }
        if (be.getLevel() != null) {
            // TODO: Update GTCapability lookup for NeoForge 1.21.1 block capability API
            IEnergyContainer gtEnergyContainer = null;
            try {
                gtEnergyContainer = be.getLevel().getCapability(
                        GTCapability.CAPABILITY_ENERGY_CONTAINER,
                        be.getBlockPos(),
                        direction.getOpposite()
                );
            } catch (Exception ignored) {
                // GT capability lookup may not match this signature yet
            }
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
