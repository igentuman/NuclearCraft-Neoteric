package igentuman.nc.setup.registration;

import igentuman.nc.block.*;
import igentuman.nc.block.storage.BarrelBlock;
import igentuman.nc.block.storage.BatteryBlock;
import igentuman.nc.block.storage.ContainerBlock;
import igentuman.nc.content.materials.Materials;
import igentuman.nc.content.materials.Ores;
import igentuman.nc.content.processors.Processors;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;

import java.util.*;

import static igentuman.nc.NuclearCraft.debugLog;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_BLOCKS;
import static igentuman.nc.multiblock.particle_chamber.TargetChamberRegistration.TARGET_CHAMBER_BLOCKS;
import static igentuman.nc.multiblock.fission.FissionReactorRegistration.FISSION_BLOCKS;
import static igentuman.nc.multiblock.fusion.FusionReactorRegistration.FUSION_BLOCKS;
import static igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration.EXPL_BLOCK;
import static igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration.KUGELBLITZ_BLOCKS;
import static igentuman.nc.multiblock.turbine.TurbineRegistration.TURBINE_BLOCKS;
import static igentuman.nc.setup.registration.FissionFuel.*;
import static igentuman.nc.setup.registration.NCBlocks.*;
import static igentuman.nc.setup.registration.NCEnergyBlocks.ENERGY_BLOCKS;
import static igentuman.nc.setup.registration.NCFluids.FluidEntry.ALL_BUCKETS;
import static igentuman.nc.setup.registration.NCItems.*;
import static igentuman.nc.setup.registration.NCProcessors.PROCESSORS;
import static igentuman.nc.setup.registration.NCStorageBlocks.STORAGE_BLOCKS;
import static igentuman.nc.setup.registration.Registries.CREATIVE_TABS;
import static igentuman.nc.util.TagUtil.*;
import static igentuman.nc.util.TextUtils.__;

public class CreativeTabs {

    public static final RegistryObject<CreativeModeTab> FUSION_REACTOR_TAB = CREATIVE_TABS.register("fusion_reactor",
            () ->  CreativeModeTab.builder()
            .displayItems((displayParams, output) -> FUSION_BLOCKS.values().forEach(itemlike -> output.accept(new ItemStack(itemlike.get()))))
            .icon(() -> new ItemStack(FUSION_BLOCKS.get("fusion_core").get()))
            .title(__("itemGroup.nuclearcraft_fusion_reactor"))
            .build());

    public static final RegistryObject<CreativeModeTab> KUGELBLITZ_TAB = CREATIVE_TABS.register("kugelblitz",
            () ->  CreativeModeTab.builder()
                    .displayItems((displayParams, output) -> KUGELBLITZ_BLOCKS.values().forEach(itemlike -> output.accept(new ItemStack(itemlike.get()))))
                    .icon(() -> new ItemStack(KUGELBLITZ_BLOCKS.get("chamber_terminal").get()))
                    .title(__("itemGroup.nuclearcraft_kugelblitz"))
                    .build());

    public static final RegistryObject<CreativeModeTab> ACCELERATOR_TAB = CREATIVE_TABS.register("accelerator",
            () ->  CreativeModeTab.builder()
                    .displayItems((displayParams, output) -> acceleratorStuff().forEach(output::accept))
                    .icon(() -> new ItemStack(ACCELERATOR_BLOCKS.get("linear_accelerator_controller").get()))
                    .title(__("itemGroup.nuclearcraft_accelerator"))
                    .build());


    public static final RegistryObject<CreativeModeTab> NC_BLOCKS_TAB = CREATIVE_TABS.register("nc_blocks",
            () ->  CreativeModeTab.builder()
            .icon(() -> new ItemStack(getSingleBlockByTagKey("forge:storage_blocks/uranium")))
            .displayItems((displayParams, output) -> getBlocks().forEach(output::accept))
            .title(__("itemGroup.nuclearcraft_blocks"))
            .build());

    public static final RegistryObject<CreativeModeTab> NC_ITEMS_TAB = CREATIVE_TABS.register("nc_items",
            () ->  CreativeModeTab.builder()
                    .icon(() -> new ItemStack(getItemsByTagKey("forge:ingots/uranium").get(0)))
                    .displayItems((displayParams, output) -> getItems().forEach(output::accept))
                    .title(__("itemGroup.nuclearcraft_items"))
                    .build()
    );

    public static final RegistryObject<CreativeModeTab> NC_PARTS_TAB = CREATIVE_TABS.register("nc_parts",
            () ->  CreativeModeTab.builder()
                    .icon(() -> new ItemStack(NC_PARTS.get("actuator").get()))
                    .displayItems((displayParams, output) -> NC_PARTS.values().forEach(itemlike -> output.accept(new ItemStack(itemlike.get()))))
                    .title(__("itemGroup.nuclearcraft_items"))
                    .build());

    public static final RegistryObject<CreativeModeTab> FISSION_REACTOR_TAB = CREATIVE_TABS.register("fission_reactor",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(FISSION_BLOCKS.get("fission_reactor_controller").get()))
                    .displayItems((displayParams, output) -> FISSION_BLOCKS.values().forEach(itemlike -> output.accept(new ItemStack(itemlike.get()))))
                    .title(__("itemGroup.nuclearcraft_fission_reactor"))
                    .build());

    public static final RegistryObject<CreativeModeTab> TURBINE_TAB = CREATIVE_TABS.register("turbine",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(TURBINE_BLOCKS.get("turbine_controller").get()))
                    .displayItems((displayParams, output) -> TURBINE_BLOCKS.values().forEach(itemlike -> output.accept(new ItemStack(itemlike.get()))))
                    .title(__("itemGroup.nuclearcraft_turbine"))
                    .build());

    public static final RegistryObject<CreativeModeTab> NC_FLUIDS = CREATIVE_TABS.register("nc_fluids",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ALL_BUCKETS.get(0).get()))
                    .displayItems((displayParams, output) -> ALL_BUCKETS.forEach(itemlike -> output.accept(new ItemStack(itemlike.get()))))
                    .title(__("itemGroup.nuclearcraft_fluids"))
                    .build());

    private static List<ItemStack> itemStacks(Collection<RegistryObject<Item>> map) {
        List<ItemStack> stacks = new ArrayList<>();
        for(RegistryObject<Item> item: map) {
            stacks.add(new ItemStack(item.get()));
        }
        return stacks;
    }

    private static List<ItemStack> blockStacks(Collection<RegistryObject<Block>> map) {
        List<ItemStack> stacks = new ArrayList<>();
        for(RegistryObject<Block> item: map) {
            stacks.add(new ItemStack(item.get()));
        }
        return stacks;
    }

    private static List<ItemStack> onlyEnabledItems(String type, HashMap<String, RegistryObject<Item>> items)
    {
        List<ItemStack> itemsList = new ArrayList<>();
        Set<String> enabled = Materials.registeredOf(type);
        for(String name: items.keySet()) {
            if(enabled.contains(name)) {
                itemsList.add(new ItemStack(items.get(name).get()));
            }
        }
        return itemsList;
    }

    private static List<ItemStack> onlyEnabledBlocks(HashMap<String, RegistryObject<Block>> block)
    {
        List<ItemStack> itemsList = new ArrayList<>();
        Set<String> enabled = Materials.registeredOf("block");
        for(String name: block.keySet()) {
            if(enabled.contains(name)) {
                itemsList.add(new ItemStack(block.get(name).get()));
            }
        }
        return itemsList;
    }

    private static List<ItemStack> getItems()
    {
        List<ItemStack> items = itemStacks(NC_PARTS.values());
        items.addAll(itemStacks(NC_ITEMS.values()));
        items.addAll(itemStacks(NC_RECORDS.values()));
        items.addAll(itemStacks(NC_FOOD.values()));
        items.addAll(itemStacks(NC_SHIELDING.values()));
        items.addAll(itemStacks(NC_WASTE.values()));
        items.addAll(onlyEnabledItems("ingot", NC_INGOTS));
        items.addAll(onlyEnabledItems("chunk",NC_CHUNKS));
        items.addAll(onlyEnabledItems("dust",NC_DUSTS));
        items.addAll(onlyEnabledItems("gem",NC_GEMS));
        items.addAll(onlyEnabledItems("nugget",NC_NUGGETS));
        items.addAll(onlyEnabledItems("plate",NC_PLATES));
        items.addAll(itemStacks(NC_ISOTOPES.values()));
        items.addAll(itemStacks(NC_FUEL.values()));
        items.addAll(itemStacks(NC_DEPLETED_FUEL.values()));
        items.add(new ItemStack(HEV_HELMET.get()));
        items.add(new ItemStack(FERAL_GHOUL_SPAWN_EGG.get()));
        items.add(new ItemStack(PORTAL_BLOCK.get()));
        items.add(new ItemStack(HEV_CHEST.get()));
        items.add(new ItemStack(HEV_PANTS.get()));
        items.add(new ItemStack(HEV_BOOTS.get()));
        items.add(new ItemStack(HAZMAT_MASK.get()));
        items.add(new ItemStack(HAZMAT_CHEST.get()));
        items.add(new ItemStack(HAZMAT_PANTS.get()));
        items.add(new ItemStack(HAZMAT_BOOTS.get()));
        items.add(new ItemStack(TOUGH_HELMET.get()));
        items.add(new ItemStack(TOUGH_CHEST.get()));
        items.add(new ItemStack(TOUGH_PANTS.get()));
        items.add(new ItemStack(TOUGH_BOOTS.get()));
        items.add(new ItemStack(SPAXELHOE_THORIUM.get()));
        items.add(new ItemStack(SPAXELHOE_TOUGH.get()));
        items.add(new ItemStack(QNP.get()));
        items.add(new ItemStack(Q36.get()));
        items.add(new ItemStack(MULTITOOL.get()));
        items.add(new ItemStack(GEIGER_COUNTER.get()));
        items.add(new ItemStack(LITHIUM_ION_CELL.get()));
        items.add(new ItemStack(MUSHROOM_BLOCK.get()));
        items.add(new ItemStack(WASTELAND_EARTH.get()));
        return items;
    }

    private static List<ItemStack> acceleratorStuff() {
        List<ItemStack> items = new ArrayList<>();
        for(RegistryObject<Block> block: ACCELERATOR_BLOCKS.values()) {
            items.add(new ItemStack(block.get()));
        }
        for(RegistryObject<Block> block: TARGET_CHAMBER_BLOCKS.values()) {
            items.add(new ItemStack(block.get()));
        }
        for(RegistryObject<Item> item: ION_SOURCES.values()) {
            items.add(new ItemStack(item.get()));
        }
        return items;
    }

    private static List<ItemStack> getBlocks()
    {
        List<ItemStack> items = new ArrayList<>();
        for(String name: PROCESSORS.keySet()) {
            if(Processors.registered().containsKey(name)) {
                items.add(new ItemStack(PROCESSORS.get(name).get()));
            } else {
                debugLog("Processor not registered: " + name);
            }
        }
        items.addAll(blockStacks(NC_BLOCKS.values()));
        items.add(new ItemStack(REDSTONE_DIMMER_ITEM_BLOCK.get()));
        items.add(new ItemStack(CHARGING_STATION_ITEM_BLOCK.get()));
        items.add(new ItemStack(MULTIBLOCK_BUILDER_BLOCK.get()));
        items.add(new ItemStack(EXPL_BLOCK.get()));
        items.addAll(blockStacks(NC_ELECTROMAGNETS.values()));
        items.addAll(blockStacks(NC_RF_AMPLIFIERS.values()));
        for(RegistryObject<Block> block: ENERGY_BLOCKS.values()) {
            if(block.get() instanceof SolarPanelBlock solarPanel) {
                if(solarPanel.registered()) {
                    items.add(new ItemStack(solarPanel));
                }
                continue;
            }
            if(block.get() instanceof RTGBlock rtgBlock) {
                if(rtgBlock.registered()) {
                    items.add(new ItemStack(rtgBlock));
                }
                continue;
            }
            if(block.get() instanceof BatteryBlock batteryBlock) {
                if(batteryBlock.registered()) {
                    items.add(new ItemStack(batteryBlock));
                }
                continue;
            }
            items.add(new ItemStack(block.get()));
        }
        for(ItemStack ore: blockStacks(ORE_BLOCKS.values())) {
            String type = ore.getItem().toString().replaceAll("_ore|_deepslate_ore", "");
            if(Ores.registered().containsKey(type)) {
                items.add(ore);
            }
        }
        items.addAll(onlyEnabledBlocks(NC_MATERIAL_BLOCKS));
        for(RegistryObject<Block> block: STORAGE_BLOCKS.values()) {
            if(block.get() instanceof ContainerBlock containerBlock) {
                if(containerBlock.registered()) {
                    items.add(new ItemStack(containerBlock));
                }
                continue;
            }
            if(block.get() instanceof BarrelBlock barrelBlock) {
                if(barrelBlock.registered()) {
                    items.add(new ItemStack(barrelBlock));
                }
                continue;
            }
            items.add(new ItemStack(block.get()));
        }
        return items;
    }


    public static void init() {

    }
}
