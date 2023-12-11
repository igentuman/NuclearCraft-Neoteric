package igentuman.nc.setup.registration;

import igentuman.nc.content.materials.Materials;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.multiblock.fission.FissionReactor.FISSION_BLOCKS;
import static igentuman.nc.multiblock.fusion.FusionReactor.FUSION_BLOCKS;
import static igentuman.nc.setup.registration.Fuel.NC_FUEL;
import static igentuman.nc.setup.registration.Fuel.NC_ISOTOPES;
import static igentuman.nc.setup.registration.NCBlocks.*;
import static igentuman.nc.setup.registration.NCEnergyBlocks.ENERGY_BLOCKS;
import static igentuman.nc.setup.registration.NCFluids.FluidEntry.ALL_BUCKETS;
import static igentuman.nc.setup.registration.NCItems.*;
import static igentuman.nc.setup.registration.NCProcessors.PROCESSORS;
import static igentuman.nc.setup.registration.NCStorageBlocks.STORAGE_BLOCKS;

public class CreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final RegistryObject<CreativeModeTab> FUSION_REACTOR_TAB = CREATIVE_TABS.register("fusion_reactor",
            () ->  CreativeModeTab.builder()
            .displayItems((displayParams, output) -> FUSION_BLOCKS.values().forEach(itemlike -> output.accept(new ItemStack(itemlike.get()))))
            .icon(() -> new ItemStack(FUSION_BLOCKS.get("fusion_core").get()))
            .title(Component.translatable("itemGroup.nuclearcraft_fusion_reactor"))
            .build());


    public static final RegistryObject<CreativeModeTab> NC_BLOCKS_TAB = CREATIVE_TABS.register("nc_blocks",
            () ->  CreativeModeTab.builder()
            .icon(() -> new ItemStack(NC_BLOCKS.get(Materials.uranium).get()))
            .displayItems((displayParams, output) -> getBlocks().forEach(output::accept))
            .title(Component.translatable("itemGroup.nuclearcraft_blocks"))
            .build());

    public static final RegistryObject<CreativeModeTab> NC_ITEMS_TAB = CREATIVE_TABS.register("nc_items",
            () ->  CreativeModeTab.builder()
                    .icon(() -> new ItemStack(NC_INGOTS.get(Materials.uranium).get()))
                    .displayItems((displayParams, output) -> getItems().forEach(output::accept))
                    .title(Component.translatable("itemGroup.nuclearcraft_items"))
                    .build()
    );

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

    private static List<ItemStack> getItems()
    {
        List<ItemStack> items = itemStacks(NC_PARTS.values());
        items.addAll(itemStacks(NC_ITEMS.values()));
        items.addAll(itemStacks(NC_RECORDS.values()));
        items.addAll(itemStacks(NC_FOOD.values()));
        items.addAll(itemStacks(NC_SHIELDING.values()));
        items.addAll(itemStacks(NC_INGOTS.values()));
        items.addAll(itemStacks(NC_DUSTS.values()));
        items.addAll(itemStacks(NC_GEMS.values()));
        items.addAll(itemStacks(NC_NUGGETS.values()));
        items.addAll(itemStacks(NC_PLATES.values()));
        items.addAll(itemStacks(NC_ISOTOPES.values()));
        items.addAll(itemStacks(NC_FUEL.values()));
        items.add(new ItemStack(HEV_HELMET.get()));
        items.add(new ItemStack(HEV_CHEST.get()));
        items.add(new ItemStack(HEV_PANTS.get()));
        items.add(new ItemStack(HEV_BOOTS.get()));
        items.add(new ItemStack(TOUGH_HELMET.get()));
        items.add(new ItemStack(TOUGH_CHEST.get()));
        items.add(new ItemStack(TOUGH_PANTS.get()));
        items.add(new ItemStack(TOUGH_BOOTS.get()));
        items.add(new ItemStack(SPAXELHOE_THORIUM.get()));
        items.add(new ItemStack(SPAXELHOE_TOUGH.get()));
        items.add(new ItemStack(QNP.get()));
        items.add(new ItemStack(MULTITOOL.get()));
        items.add(new ItemStack(GEIGER_COUNTER.get()));
        items.add(new ItemStack(LITHIUM_ION_CELL.get()));
        return items;
    }

    private static List<ItemStack> getBlocks()
    {
        List<ItemStack> items = blockStacks(PROCESSORS.values());
        items.addAll(blockStacks(NC_BLOCKS.values()));
        items.addAll(blockStacks(NC_ELECTROMAGNETS.values()));
        items.addAll(blockStacks(NC_RF_AMPLIFIERS.values()));
        items.addAll(blockStacks(ENERGY_BLOCKS.values()));
        items.addAll(blockStacks(ORE_BLOCKS.values()));
        items.addAll(blockStacks(NC_MATERIAL_BLOCKS.values()));
        items.addAll(blockStacks(STORAGE_BLOCKS.values()));
        return items;
    }

    public static final RegistryObject<CreativeModeTab> NC_PARTS_TAB = CREATIVE_TABS.register("nc_parts",
            () ->  CreativeModeTab.builder()
            .icon(() -> new ItemStack(NC_PARTS.get("actuator").get()))
            .displayItems((displayParams, output) -> NC_PARTS.values().forEach(itemlike -> output.accept(new ItemStack(itemlike.get()))))
            .title(Component.translatable("itemGroup.nuclearcraft_items"))
            .build());

    public static final RegistryObject<CreativeModeTab> FISSION_REACTOR_TAB = CREATIVE_TABS.register("fission_reactor",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(FISSION_BLOCKS.get("fission_reactor_controller").get()))
                    .displayItems((displayParams, output) -> FISSION_BLOCKS.values().forEach(itemlike -> output.accept(new ItemStack(itemlike.get()))))
                    .title(Component.translatable("itemGroup.nuclearcraft_fission_reactor"))
                    .build());

    public static final RegistryObject<CreativeModeTab> NC_FLUIDS = CREATIVE_TABS.register("nc_fluids",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ALL_BUCKETS.get(0).get()))
                    .displayItems((displayParams, output) -> ALL_BUCKETS.forEach(itemlike -> output.accept(new ItemStack(itemlike.get()))))
                    .title(Component.translatable("itemGroup.nuclearcraft_fluids"))
                    .build());
}
