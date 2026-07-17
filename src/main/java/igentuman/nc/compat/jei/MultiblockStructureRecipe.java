package igentuman.nc.compat.jei;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

public class MultiblockStructureRecipe {
    private final ResourceLocation id;
    private final CompoundTag structureNbt;
    private final String name;
    private final MultiblockStructure structure;
    public int currentLayer = 0;
    public List<ItemStack> outputs = new ArrayList<>();

    public MultiblockStructureRecipe(ResourceLocation id, CompoundTag structureNbt, String name) {
        this.id = id;
        this.structureNbt = structureNbt;
        this.name = "jei.recipe.nc." + name.replace(".nbt", "");
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
        currentLayer--;
        if (currentLayer < structure.getMinY()) {
            currentLayer = structure.getMaxY();
        }
    }

    public Ingredient getIngredients() {
        Ingredient ingredient = Ingredient.of(outputs.toArray(new ItemStack[0]));
        return ingredient;
    }
}