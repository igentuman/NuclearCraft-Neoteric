package igentuman.nc.compat.jei;

import igentuman.nc.multiblock.MultiblockEntry;
import igentuman.nc.util.MultiblockStructure;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static igentuman.nc.Main.rl;

public class MultiblockStructureRecipe {
    private final ResourceLocation id;
    private final CompoundTag structureNbt;
    private final String name;
    private final MultiblockStructure structure;
    public int currentLayer = 0;
    public List<ItemStack> outputs = new ArrayList<>();
    private IIngredientManager ingredientManager;

    public MultiblockStructureRecipe(MultiblockEntry entry, MultiblockStructure structure) {
        this.id = rl(entry.name());
        this.structureNbt = structure.getStructureNbt();
        this.name =  entry.name();
        this.structure = new MultiblockStructure(structureNbt);
        this.currentLayer = structure.getMaxY();
        List<Block> blocks = new ArrayList<>();
        for(BlockPos pos : structure.getBlocks().keySet()) {
            Block block = structure.getBlocks().get(pos).getBlock();
            if (!blocks.contains(block)) {
                blocks.add(block);
                outputs.add(new ItemStack(block));
            }
        }
        for(ItemStack stackItem :outputs) {
            for(Map.Entry<BlockPos, BlockState> block : structure.getBlocks().entrySet()) {
                if(stackItem.is(block.getValue().getBlock().asItem())) {
                    stackItem.setCount(stackItem.getCount() + 1);
                }
            }
            stackItem.setCount(stackItem.getCount() - 1);
        }
        this.ingredientManager = ingredientManager;
    }
    
    public ResourceLocation getId() {
        return id;
    }
    
    public CompoundTag getStructureNbt() {
        return structureNbt;
    }
    
    public String getName() {
        return name;
    }

    public MultiblockStructure getStructure() {
        return structure;
    }

    public void slice() {
        if(structure.getMaxY() < currentLayer) {
            currentLayer = structure.getMaxY();
        }
        currentLayer--;
        if (currentLayer < structure.getMinY()) {
            currentLayer = structure.getMaxY();
        }
    }

    public Ingredient getIngredients() {
        Ingredient ingredient = Ingredient.of(outputs.toArray(new ItemStack[0]));
        return ingredient;
    }

    public IIngredientManager getIngredientManager() {
        return ingredientManager;
    }
}