package igentuman.nc.setup;

import igentuman.nc.container.MultiblockControllerContainer;
import igentuman.nc.content.NCRadiationDamageSource;
import igentuman.nc.content.particles.ParticleSources;
import igentuman.nc.effect.RadiationDecay;
import igentuman.nc.effect.RadiationResistance;
import igentuman.nc.multiblock.accelerator.AcceleratorRegistration;
import igentuman.nc.multiblock.particle_chamber.TargetChamberRegistration;
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
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

import static igentuman.nc.setup.registration.Registries.CONTAINERS;
import static igentuman.nc.setup.registration.Registries.EFFECTS;

public class Registration {

    public static final DeferredHolder<MobEffect, MobEffect> RADIATION_RESISTANCE = EFFECTS.register("radiation_resistance", () -> new RadiationResistance(MobEffectCategory.BENEFICIAL, 0xd4ffFF));
    public static final DeferredHolder<MobEffect, MobEffect> RADIATION_DECAY = EFFECTS.register("radiation_decay", () -> new RadiationDecay(MobEffectCategory.BENEFICIAL, 0xd4ffFF));
    public static final DeferredHolder<MenuType<?>, MenuType<MultiblockControllerContainer>> MULTIBLOCK_REPORT_CONTAINER = CONTAINERS.register("multilblock_report_container",
            () -> IMenuTypeExtension.create((windowId, inv, data) -> new MultiblockControllerContainer(windowId, data.readBlockPos(), inv))
    );

    @SubscribeEvent
    public static void onConstruction(FMLConstructModEvent event) {
        event.enqueueWork(() -> {
            FissionFuel.registerRuntimeFuels();
            NCFluids.init();
        });
    }

    public static void init(IEventBus modbus) {
        Registries.init(modbus);
        NCStorageBlocks.init();
        NCBlocks.init();
        ParticleSources.init();
        FissionFuel.init();
        NCFluids.init();
        NCRadiationDamageSource.init();
        FissionReactorRegistration.init();
        FusionReactorRegistration.init();
        KugelblitzRegistration.init();
        TurbineRegistration.init();
        AcceleratorRegistration.init();
        TargetChamberRegistration.init();
        CreativeTabs.init();
        NCEnergyBlocks.init();
        NCItems.init();
        NCProcessors.init();
        Villager.init(modbus);
        NCSounds.init();
        GameEvents.init(modbus);
        NcRecipeType.init();
        NcParticleTypes.init();
        WorldGeneration.init();
        NcRecipeSerializers.init();
        NCPlacementModifierTypes.init();
    }
}
