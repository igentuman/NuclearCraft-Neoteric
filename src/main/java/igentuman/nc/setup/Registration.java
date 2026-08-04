package igentuman.nc.setup;

import igentuman.nc.container.MultiblockControllerContainer;
import igentuman.nc.content.NCRadiationDamageSource;
import igentuman.nc.content.particles.ParticleSources;
import igentuman.nc.effect.MaxHealthBoost;
import igentuman.nc.effect.QuickdrawBoost;
import igentuman.nc.effect.RadiationDecay;
import igentuman.nc.effect.RadiationResistance;
import igentuman.nc.multiblock.accelerator.AcceleratorRegistration;
import igentuman.nc.multiblock.particle_chamber.ParticleChamberRegistration;
import igentuman.nc.multiblock.fission.FissionReactorRegistration;
import igentuman.nc.multiblock.fusion.FusionReactorRegistration;
import igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration;
import igentuman.nc.multiblock.heat_exchanger.HeatExchangerRegistration;
import igentuman.nc.multiblock.turbine.TurbineRegistration;
import igentuman.nc.recipes.NcRecipeSerializers;
import igentuman.nc.recipes.NcRecipeType;
import igentuman.nc.setup.registration.*;
import igentuman.nc.world.placement.NCPlacementModifierTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegistryObject;

import static igentuman.nc.setup.registration.Registries.CONTAINERS;
import static igentuman.nc.setup.registration.Registries.EFFECTS;

public class Registration {

    public static final RegistryObject<MobEffect> RADIATION_RESISTANCE = EFFECTS.register("radiation_resistance", () -> new RadiationResistance(MobEffectCategory.BENEFICIAL, 0xd4ffFF));
    public static final RegistryObject<MobEffect> RADIATION_DECAY = EFFECTS.register("radiation_decay", () -> new RadiationDecay(MobEffectCategory.BENEFICIAL, 0xd4ffFF));
    public static final RegistryObject<MobEffect> QUICKDRAW_BOOST = EFFECTS.register("quickdraw_boost", () -> new QuickdrawBoost(MobEffectCategory.BENEFICIAL, 0xE0C068));
    public static final RegistryObject<MobEffect> MAX_HEALTH_BOOST = EFFECTS.register("max_health_boost", () -> new MaxHealthBoost(MobEffectCategory.BENEFICIAL, 0xF05050));
    public static final RegistryObject<MenuType<MultiblockControllerContainer>> MULTIBLOCK_REPORT_CONTAINER = CONTAINERS.register("multilblock_report_container",
            () -> IForgeMenuType.create((windowId, inv, data) -> new MultiblockControllerContainer(windowId, data.readBlockPos(), inv))
    );

    @SubscribeEvent
    public static void onConstruction(FMLConstructModEvent event) {
        event.enqueueWork(() -> {
            //ParticleSources.registerRuntimeSources();
            FissionFuel.registerRuntimeFuels();
            NCFluids.init();
        });
    }

    public static void init(FMLJavaModLoadingContext context) {
        Registries.init(context);
        NCStorageBlocks.init();
        NCBlocks.init();
        NCCrafter.init();
        FissionFuel.init();
        ParticleSources.init();
        NCFluids.init();
        NCRadiationDamageSource.init();
        FissionDesignerRegistration.init();
        FissionReactorRegistration.init();
        FusionReactorRegistration.init();
        KugelblitzRegistration.init();
        TurbineRegistration.init();
        HeatExchangerRegistration.init();
        AcceleratorRegistration.init();
        ParticleChamberRegistration.init();
        CreativeTabs.init();
        NCEnergyBlocks.init();
        NCItems.init();
        NCProcessors.init();
        Villager.init(context);
        NCSounds.init();
        GameEvents.init(context);
        NcRecipeType.init();
        NcParticleTypes.init();
        WorldGeneration.init();
        NcRecipeSerializers.init();
        NCPlacementModifierTypes.init();
    }
}
