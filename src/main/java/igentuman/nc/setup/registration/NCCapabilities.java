package igentuman.nc.setup.registration;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.NuclearCraftBE;
import igentuman.nc.block.entity.processor.LeacherBE;
import igentuman.nc.block.fusion.entity.FusionCoreBE;
import igentuman.nc.block.fusion.entity.FusionCoreProxyBE;
import igentuman.nc.handler.ItemEnergyHandler;
import igentuman.nc.item.*;
import igentuman.nc.util.capability.ItemCapabilityProvider;
import igentuman.nc.multiblock.accelerator.AcceleratorRegistration;
import igentuman.nc.multiblock.fission.FissionReactorRegistration;
import igentuman.nc.multiblock.fusion.FusionReactorRegistration;
import igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration;
import igentuman.nc.multiblock.particle_chamber.TargetChamberRegistration;
import igentuman.nc.multiblock.turbine.TurbineRegistration;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import igentuman.api.platform.NCFluidCapability;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = NuclearCraft.MODID, bus = EventBusSubscriber.Bus.MOD)
public class NCCapabilities {

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        registerBlockEntityCapabilities(event);
        registerItemCapabilities(event);
    }

    // ========================
    // Block Entity Capabilities
    // ========================

    private static void registerBlockEntityCapabilities(RegisterCapabilitiesEvent event) {
        List<BlockEntityType<? extends NuclearCraftBE>> allTypes = new ArrayList<>();

        // Processors (all have energy + content handler)
        for (var holder : NCProcessors.PROCESSORS_BE.values()) {
            @SuppressWarnings("unchecked")
            BlockEntityType<? extends NuclearCraftBE> beType = (BlockEntityType<? extends NuclearCraftBE>) holder.get();
            allTypes.add(beType);
        }

        // Energy blocks (solar, battery, RTG, decay generator)
        for (var holder : NCEnergyBlocks.ENERGY_BE.values()) {
            @SuppressWarnings("unchecked")
            BlockEntityType<? extends NuclearCraftBE> beType = (BlockEntityType<? extends NuclearCraftBE>) holder.get();
            allTypes.add(beType);
        }

        // Storage blocks (barrels, containers)
        for (var holder : NCStorageBlocks.STORAGE_BE.values()) {
            @SuppressWarnings("unchecked")
            BlockEntityType<? extends NuclearCraftBE> beType = (BlockEntityType<? extends NuclearCraftBE>) holder.get();
            allTypes.add(beType);
        }

        // Fission reactor BEs
        for (var holder : FissionReactorRegistration.FISSION_BE.values()) {
            @SuppressWarnings("unchecked")
            BlockEntityType<? extends NuclearCraftBE> beType = (BlockEntityType<? extends NuclearCraftBE>) holder.get();
            allTypes.add(beType);
        }

        // Fusion reactor BEs (excluding proxy — handled separately)
        for (var holder : FusionReactorRegistration.FUSION_BE.values()) {
            @SuppressWarnings("unchecked")
            BlockEntityType<? extends NuclearCraftBE> beType = (BlockEntityType<? extends NuclearCraftBE>) holder.get();
            allTypes.add(beType);
        }

        // Turbine BEs
        for (var holder : TurbineRegistration.TURBINE_BE.values()) {
            @SuppressWarnings("unchecked")
            BlockEntityType<? extends NuclearCraftBE> beType = (BlockEntityType<? extends NuclearCraftBE>) holder.get();
            allTypes.add(beType);
        }

        // Kugelblitz BEs
        for (var holder : KugelblitzRegistration.KUGELBLITZ_BE.values()) {
            @SuppressWarnings("unchecked")
            BlockEntityType<? extends NuclearCraftBE> beType = (BlockEntityType<? extends NuclearCraftBE>) holder.get();
            allTypes.add(beType);
        }
        addIfPresent(allTypes, KugelblitzRegistration.EXPL_BE);
        addIfPresent(allTypes, KugelblitzRegistration.EXPL_PROXY_BE);

        // Accelerator BEs
        for (var holder : AcceleratorRegistration.ACCELERATOR_BE.values()) {
            @SuppressWarnings("unchecked")
            BlockEntityType<? extends NuclearCraftBE> beType = (BlockEntityType<? extends NuclearCraftBE>) holder.get();
            allTypes.add(beType);
        }

        // Target Chamber BEs
        for (var holder : TargetChamberRegistration.TARGET_CHAMBER_BE.values()) {
            @SuppressWarnings("unchecked")
            BlockEntityType<? extends NuclearCraftBE> beType = (BlockEntityType<? extends NuclearCraftBE>) holder.get();
            allTypes.add(beType);
        }

        // Misc BEs
        addIfPresent(allTypes, NCBlocks.REDSTONE_DIMMER_BE);
        addIfPresent(allTypes, NCBlocks.MULTIBLOCK_BUILDER_BE);

        // Register standard capabilities for all types
        for (BlockEntityType<? extends NuclearCraftBE> beType : allTypes) {
            registerNuclearCraftBECapabilities(event, beType);
        }

        // Special case: FusionCoreProxyBE — delegates to controller
        registerFusionCoreProxyCapabilities(event);
    }

    @SuppressWarnings("unchecked")
    private static void addIfPresent(List<BlockEntityType<? extends NuclearCraftBE>> list, Object holder) {
        if (holder instanceof DeferredHolder<?, ?> dh) {
            try {
                list.add((BlockEntityType<? extends NuclearCraftBE>) dh.get());
            } catch (Exception ignored) {
            }
        }
    }

    private static <T extends NuclearCraftBE> void registerNuclearCraftBECapabilities(
            RegisterCapabilitiesEvent event,
            BlockEntityType<T> beType) {

        // Energy capability
        event.registerBlockEntity(
            Capabilities.EnergyStorage.BLOCK,
            beType,
            (be, side) -> be.energyStorage() != null ? be.energyStorage() : null
        );

        // Item handler capability
        // LeacherBE blocks external item handler access
        event.registerBlockEntity(
            Capabilities.ItemHandler.BLOCK,
            beType,
            (be, side) -> {
                if (be instanceof LeacherBE) {
                    return null; // Leacher blocks item handler from outside
                }
                if (be.contentHandler() != null) {
                    return be.contentHandler().getItemCapability(side);
                }
                return null;
            }
        );

        // Fluid handler capability
        event.registerBlockEntity(
            Capabilities.FluidHandler.BLOCK,
            beType,
            (be, side) -> {
                if (be.contentHandler() != null) {
                    return be.contentHandler().getFluidCapability(side);
                }
                return null;
            }
        );

        // Mekanism chemical handler capability — converts chemicals to NC fluids
        if (net.neoforged.fml.ModList.get().isLoaded("mekanism")) {
            event.registerBlockEntity(
                mekanism.common.capabilities.Capabilities.CHEMICAL.block(),
                beType,
                (be, side) -> {
                    if (be.contentHandler() != null && be.contentHandler().getFluidCapability(side) != null) {
                        return be.contentHandler().chemicalConverter(side);
                    }
                    return null;
                }
            );
        }
    }

    /**
     * FusionCoreProxyBE delegates capabilities to its controller.
     * Energy: only on vertical sides (UP/DOWN), not horizontal.
     * Fluid: delegates to controller on vertical sides.
     * Item: not exposed (proxy has no item slots).
     */
    @SuppressWarnings("unchecked")
    private static void registerFusionCoreProxyCapabilities(RegisterCapabilitiesEvent event) {
        BlockEntityType<FusionCoreProxyBE> proxyType;
        try {
            proxyType = (BlockEntityType<FusionCoreProxyBE>) FusionReactorRegistration.FUSION_CORE_PROXY_BE.get();
        } catch (Exception e) {
            return; // Registration not available
        }

        event.registerBlockEntity(
            Capabilities.EnergyStorage.BLOCK,
            proxyType,
            (proxy, side) -> {
                if (side != null && side.getAxis().isHorizontal()) {
                    return null; // Only vertical sides
                }
                FusionCoreBE controller = proxy.controller();
                if (controller != null && controller.energyStorage() != null) {
                    return controller.energyStorage();
                }
                return null;
            }
        );

        event.registerBlockEntity(
            Capabilities.FluidHandler.BLOCK,
            proxyType,
            (proxy, side) -> {
                if (side != null && side.getAxis().isHorizontal()) {
                    return null;
                }
                FusionCoreBE controller = proxy.controller();
                if (controller != null && controller.contentHandler() != null) {
                    return controller.contentHandler().getFluidCapability(side);
                }
                return null;
            }
        );

        // No item handler for proxy
    }

    // ========================
    // Item Capabilities
    // ========================

    private static void registerItemCapabilities(RegisterCapabilitiesEvent event) {
        // BatteryItem (lithium ion cell) — energy storage
        event.registerItem(
            Capabilities.EnergyStorage.ITEM,
            (stack, ctx) -> new ItemEnergyHandler(stack,
                    ((BatteryItem) stack.getItem()).getEnergyMaxStorage(),
                    ((BatteryItem) stack.getItem()).getEnergyMaxStorage(),
                    ((BatteryItem) stack.getItem()).getEnergyMaxStorage()
            ).getEnergy(),
            NCItems.LITHIUM_ION_CELL.get()
        );

        // HEV armor items — energy storage
        Item[] hevItems = {
            NCItems.HEV_HELMET.get(),
            NCItems.HEV_CHEST.get(),
            NCItems.HEV_BOOTS.get(),
            NCItems.HEV_PANTS.get()
        };
        event.registerItem(
            Capabilities.EnergyStorage.ITEM,
            (stack, ctx) -> {
                HEVItem hev = (HEVItem) stack.getItem();
                return new ItemEnergyHandler(stack, hev.getEnergyMaxStorage(), 5000, hev.getEnergyMaxStorage() / 4).getEnergy();
            },
            hevItems
        );

        // QNP — energy storage (receive only, no send)
        event.registerItem(
            Capabilities.EnergyStorage.ITEM,
            (stack, ctx) -> {
                QNP qnp = (QNP) stack.getItem();
                return new ItemEnergyHandler(stack, qnp.getEnergyMaxStorage(), 0, qnp.getEnergyMaxStorage() / 4).getEnergy();
            },
            NCItems.QNP.get()
        );

        // BatteryBlockItems — energy storage (registered dynamically for all battery types)
        for (var entry : NCEnergyBlocks.BLOCK_ITEMS.entrySet()) {
            Item item = entry.getValue().get();
            if (item instanceof BatteryBlockItem) {
                event.registerItem(
                    Capabilities.EnergyStorage.ITEM,
                    (stack, ctx) -> {
                        BatteryBlockItem bbi = (BatteryBlockItem) stack.getItem();
                        return new ItemEnergyHandler(stack, bbi.getEnergyMaxStorage(), bbi.getEnergyMaxStorage(), bbi.getEnergyMaxStorage()).getEnergy();
                    },
                    item
                );
            }
        }

        // BarrelBlockItems — fluid handler (registered dynamically for all barrel types)
        for (var entry : NCStorageBlocks.BLOCK_ITEMS.entrySet()) {
            Item item = entry.getValue().get();
            if (item instanceof BarrelBlockItem) {
                event.registerItem(
                    Capabilities.FluidHandler.ITEM,
                    (stack, ctx) -> NCFluidCapability.createItemFluidHandler(stack, ((BarrelBlockItem) stack.getItem()).getCapacity() * 1000),
                    item
                );
            }
        }

        // ContainerBlockItems — item handler (registered dynamically for all container types)
        for (var entry : NCStorageBlocks.BLOCK_ITEMS.entrySet()) {
            Item item = entry.getValue().get();
            if (item instanceof ContainerBlockItem) {
                event.registerItem(
                    Capabilities.ItemHandler.ITEM,
                    (stack, ctx) -> {
                        ContainerBlockItem cbi = (ContainerBlockItem) stack.getItem();
                        return new ItemCapabilityProvider(stack, cbi.getInventorySize(), 64).getItemHandler();
                    },
                    item
                );
            }
        }
    }
}
