package igentuman.nc.block.entity.processor;

import igentuman.nc.content.processors.Processors;
import igentuman.nc.handler.OreVeinProvider;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.recipes.type.OreVeinRecipe;
import igentuman.nc.util.annotation.NBTField;
import igentuman.nc.util.annotation.NothingNullByDefault;
import igentuman.nc.util.insitu_leaching.WorldVeinsManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

import static igentuman.nc.compat.GlobalVars.CATALYSTS;
import static igentuman.nc.compat.GlobalVars.RECIPE_CLASSES;
import static igentuman.nc.radiation.ItemRadiation.getItemByName;
import static igentuman.nc.setup.registration.NCItems.NC_PARTS;
import static net.minecraft.world.item.Items.*;

public class LeacherBE extends NCProcessorBE<LeacherBE.Recipe> {
    public LeacherBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, Processors.LEACHER);
    }

    public static final byte WRONG_POSITION = 2;
    public static final byte POSITION_IS_CORRECT = 1;
    public static final byte POSITION_UNKNOWN = 0;
    @NBTField
    public int veinDepletion = 0;

    @NBTField
    public byte positionState = 2;

    @NBTField
    public ItemStack catalyst = ItemStack.EMPTY;

    protected OreVeinRecipe veinRecipe;
    @Override
    public String getName() {
        return Processors.LEACHER;
    }

    @NothingNullByDefault
    public static class Recipe extends NcRecipe {
        public Recipe(ResourceLocation id,
                      ItemStackIngredient[] input, ItemStack[] output,
                      FluidStackIngredient[] inputFluids, FluidStack[] outputFluids,
                      double timeModifier, double powerModifier, double heatModifier, double rarity) {
            super(id, input, output, inputFluids, outputFluids, timeModifier, powerModifier, heatModifier, 1);
            ID = Processors.LEACHER;
            RECIPE_CLASSES.put(ID, this.getClass());
            CATALYSTS.put(ID, List.of(getToastSymbol()));
        }
    }

    public boolean hasCatalyst() {
        return !catalystHandler.getStackInSlot(0).isEmpty();
    }

    @Override
    public void processRecipe() {
        if(!hasCatalyst()) return;
        super.processRecipe();
    }

    protected void updateRecipe() {
        gatherOre();
        super.updateRecipe();
    }

    public void gatherOre()
    {
        if(!hasCatalyst()) {
            catalyst = ItemStack.EMPTY;
            return;
        }

        catalyst = catalystHandler.getStackInSlot(0);
        ItemStack ore = ItemStack.EMPTY;

        if(catalyst.getItem().equals(FILLED_MAP)) {
            ore = useMapCatalyst();
        }
        if(catalyst.getItem().equals(NC_PARTS.get("research_paper").get())) {
            ore = useResearchPaper();
        }
        
        if(catalyst.getItem().equals(getItemByName("immersiveengineering:coresample"))) {
            ore = useIECoreSample();
        }
        contentHandler.itemHandler.insertItem(0, ore, false);
    }

    //todo implement
    protected ItemStack useIECoreSample() {
        return ItemStack.EMPTY;
    }


    protected ItemStack useResearchPaper() {
        BlockPos mapPos = BlockPos.of(catalyst.getOrCreateTag().getLong("pos"));
        ChunkPos chunkPos = new ChunkPos(mapPos);
        if(!chunkPos.equals(new ChunkPos(getBlockPos()))) {
            positionState = WRONG_POSITION;
            return ItemStack.EMPTY;
        }
        if(getLevel() == null) return ItemStack.EMPTY;
        return WorldVeinsManager.get(getLevel())
                .getWorldVeinData((ServerLevel) getLevel()).gatherRandomOre(chunkPos.x, chunkPos.z);
    }

    //todo implement
    protected ItemStack useMapCatalyst() {
        return ItemStack.EMPTY;
    }

    @Override
    public List<Item> getAllowedCatalysts() {
        List<Item> items = List.of(
                NC_PARTS.get("research_paper").get(),
                MAP
        );
        Item ieCoreSample = getItemByName("immersiveengineering:coresample");
        if(ieCoreSample != null && !ieCoreSample.equals(AIR)) items.add(ieCoreSample);
        return items;
    }
}
