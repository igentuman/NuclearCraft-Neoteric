package igentuman.nc.registration;

import igentuman.nc.block_entity.catalyst.CatalystType;
import igentuman.nc.config.Entries;
import igentuman.nc.multiblock.MultiblockEntry;
import igentuman.nc.util.SlotsLayout;
import igentuman.nc.util.caps.EnergyCapDefinition;
import igentuman.nc.util.caps.FluidCapDefinition;
import igentuman.nc.util.caps.ItemCapDefinition;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.Set;

public record ModEntry (
        String name,
        DeferredBlock<Block> block,
        DeferredItem<Item> item,
        DeferredHolder<MenuType<?>, MenuType<?>> menu,
        DeferredHolder<BlockEntityType<?>, BlockEntityType<?>> blockEntity,
        boolean hasRecipes,
        DeferredHolder<RecipeType<?>, RecipeType<?>> recipeType,
        DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> recipeSerializer,
        MaterialEntry materialEntry,
        ItemCapDefinition itemCap,
        FluidCapDefinition fluidCap,
        EnergyCapDefinition energyCap,
        SlotsLayout slotsLayout,
        ToolSetEntry toolSetEntry,
        ArmorSetEntry armorSetEntry,
        Set<MultiblockEntry> linkedMultiblocks,
        Set<CatalystType> supportedCatalysts
) {

    public boolean hasBlockEntity() {
        return blockEntity != null;
    }

    /**
     * Universal F8 gate. Non-material entries route through the {@link Entries} config toggle;
     * material entries are gated per product type elsewhere, so they report enabled here.
     * Read live (per call) so a config reload takes effect without a restart.
     */
    public boolean isEnabled() {
        if (materialEntry != null) return true;
        return Entries.isEnabled(name);
    }

    public boolean hasMenu() {
        return menu != null;
    }

    public boolean hasBlock() {
        return block != null;
    }

    public boolean hasRecipes() {
        return hasRecipes;
    }

    public boolean hasItem() {
        return item != null;
    }

    public boolean hasToolSet() {
        return toolSetEntry != null;
    }

    public boolean hasArmorSet() {
        return armorSetEntry != null;
    }

    public boolean hasCatalysts() {
        return supportedCatalysts != null && !supportedCatalysts.isEmpty();
    }

    public DeferredBlock<Block> block() {
        return block;
    }

    public DeferredItem<Item> item() {
        return item;
    }

    public void linkMultiblock(MultiblockEntry entry) {
        this.linkedMultiblocks.add(entry);
    }

    public Set<MultiblockEntry> linkedMultiblocks() {
        return this.linkedMultiblocks;
    }

    public void unlinkMultiblock(MultiblockEntry entry) {
        this.linkedMultiblocks.remove(entry);
    }

}
