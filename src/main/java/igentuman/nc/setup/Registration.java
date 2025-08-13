package igentuman.nc.setup;

import igentuman.nc.container.MultiblockControllerContailer;
import igentuman.nc.content.NCRadiationDamageSource;
import igentuman.nc.content.particles.ParticleSources;
import igentuman.nc.effect.RadiationDecay;
import igentuman.nc.effect.RadiationResistance;
import igentuman.nc.multiblock.accelerator.AcceleratorRegistration;
import igentuman.nc.multiblock.accelerator.TargetChamberRegistration;
import igentuman.nc.multiblock.fission.FissionReactorRegistration;
import igentuman.nc.multiblock.fusion.FusionReactorRegistration;
import igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration;
import igentuman.nc.multiblock.turbine.TurbineRegistration;
import igentuman.nc.recipes.NcRecipeSerializers;
import igentuman.nc.recipes.NcRecipeType;
import igentuman.nc.setup.registration.*;
import igentuman.nc.world.placement.NCPlacementModifierTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegistryObject;

import static igentuman.nc.setup.registration.Registries.CONTAINERS;
import static igentuman.nc.setup.registration.Registries.EFFECTS;

public class Registration {

    public static final RegistryObject<MobEffect> RADIATION_RESISTANCE = EFFECTS.register("radiation_resistance", () -> new RadiationResistance(MobEffectCategory.BENEFICIAL, 0xd4ffFF));
    public static final RegistryObject<MobEffect> RADIATION_DECAY = EFFECTS.register("radiation_decay", () -> new RadiationDecay(MobEffectCategory.BENEFICIAL, 0xd4ffFF));
    public static final RegistryObject<MenuType<MultiblockControllerContailer>> MULTIBLOCK_REPORT_CONTAINER = CONTAINERS.register("multilblock_report_container",
            () -> IForgeMenuType.create((windowId, inv, data) -> new MultiblockControllerContailer(windowId, data.readBlockPos(), inv))
    );
    public static void init(FMLJavaModLoadingContext context) {
        IEventBus bus = context.getModEventBus();
        Registries.init(context);
        ParticleSources.init();
        NCBlocks.init();
        NCStorageBlocks.init();
        NCItems.init();
        FissionFuel.init();
        NCFluids.init();
        NCEnergyBlocks.init();
        NCProcessors.init();
        NCRadiationDamageSource.init();
        FissionReactorRegistration.init();
        FusionReactorRegistration.init();
        KugelblitzRegistration.init();
        TurbineRegistration.init();
        AcceleratorRegistration.init();
        TargetChamberRegistration.init();
        CreativeTabs.init();
        NcRecipeSerializers.init();
        NcRecipeType.init();
        NcParticleTypes.init();
        NCSounds.init();
        Villager.init(context);
        GameEvents.init(context);
        WorldGeneration.init();
        NCPlacementModifierTypes.init();
    }
}
