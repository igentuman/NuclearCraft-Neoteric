package igentuman.nc.block.entity.processor;

import igentuman.nc.content.processors.Processors;
import igentuman.nc.handler.OreVeinProvider;
import igentuman.nc.handler.event.client.BlockOverlayHandler;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.recipes.type.OreVeinRecipe;
import igentuman.nc.util.NCBlockPos;
import igentuman.nc.util.annotation.NBTField;
import igentuman.nc.util.annotation.NothingNullByDefault;
import igentuman.nc.util.insitu_leaching.WorldVeinsManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;
import java.util.Objects;

import static igentuman.nc.block.ProcessorBlock.ACTIVE;
import static igentuman.nc.compat.GlobalVars.CATALYSTS;
import static igentuman.nc.compat.GlobalVars.RECIPE_CLASSES;
import static igentuman.nc.radiation.ItemRadiation.getItemByName;
import static igentuman.nc.setup.registration.NCItems.NC_PARTS;
import static net.minecraft.world.item.Items.*;

public class LeacherBE extends NCProcessorBE<LeacherBE.Recipe> {
    public LeacherBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, Processors.LEACHER);
    }

    public static final byte PUMPS_ERROR = 4;
    public static final byte NO_SOURCE = 3;
    public static final byte WRONG_POSITION = 2;
    public static final byte POSITION_IS_CORRECT = 1;
    public static final byte POSITION_UNKNOWN = 0;

    @NBTField
    public int veinDepletion = 0;
    @NBTField
    public boolean pumpsAreValid = false;

    @NBTField
    public byte positionState = 2;

    @NBTField
    public ItemStack catalyst = ItemStack.EMPTY;

    protected PumpBE[] pumps = new PumpBE[4];

    protected byte pumpValidationTimeout = 40;

    protected OreVeinRecipe veinRecipe;
    @Override
    public String getName() {
        return Processors.LEACHER;
    }

    @Override
    public void tickServer() {
        handleState();
        byte lastState = positionState;
        positionState = POSITION_IS_CORRECT;

        if(!hasCatalyst()) {
            positionState = NO_SOURCE;
        }

        if(!pumpsAreValid) {
            positionState = PUMPS_ERROR;
        }

        if(lastState != positionState) {
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(ACTIVE, isActive));
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState().setValue(ACTIVE, isActive), Block.UPDATE_ALL);
        }
        super.tickServer();

    }

    private void handleState() {
        pumpValidationTimeout--;
        if(pumpValidationTimeout <= 0) {
            pumpValidationTimeout = 40;
            clearHighligts();
            validatePumps();
        }
    }

    @Override
    public void setRemoved()
    {
        super.setRemoved();
        clearHighligts();
    }

    private void clearHighligts() {
        ChunkPos chunkPos = new ChunkPos(getBlockPos());
        boolean isClientSide = Objects.requireNonNull(getLevel()).isClientSide;
        if(isClientSide) {
            BlockOverlayHandler.removeFromOutline(new NCBlockPos(chunkPos.getMinBlockX(), worldPosition.getY(), chunkPos.getMinBlockZ()));
            BlockOverlayHandler.removeFromOutline(new NCBlockPos(chunkPos.getMinBlockX(), worldPosition.getY(), chunkPos.getMaxBlockZ()));
            BlockOverlayHandler.removeFromOutline(new NCBlockPos(chunkPos.getMaxBlockX(), worldPosition.getY(), chunkPos.getMaxBlockZ()));
            BlockOverlayHandler.removeFromOutline(new NCBlockPos(chunkPos.getMaxBlockX(), worldPosition.getY(), chunkPos.getMinBlockZ()));
        }
    }

    public PumpBE[] getPumpsForClient() {
        validatePumps();
        return pumps;
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

    public void tickClient() {
        handleState();
    }

    public boolean isPumpValid(NCBlockPos pos, int id) {
        for (int y = 0; y < 20; y++) {
            BlockEntity be = getLevel().getBlockEntity( pos.below());
            if (be instanceof PumpBE) {
                pumps[id] = (PumpBE) be;
                return pumps[id].isInSituValid();
            }
        }
        return false;
    }

    public void validatePumps() {
        ChunkPos chunkPos = new ChunkPos(getBlockPos());
        boolean isClientSide = Objects.requireNonNull(getLevel()).isClientSide;
        pumpsAreValid = isPumpValid(
                new NCBlockPos(chunkPos.getMinBlockX(), worldPosition.getY()+5, chunkPos.getMinBlockZ()),
                0
        );
        if(!pumpsAreValid) {
            if(isClientSide) {
                BlockOverlayHandler.addToOutline(new NCBlockPos(chunkPos.getMinBlockX(), worldPosition.getY(), chunkPos.getMinBlockZ()));
            }
        }
        if(!isPumpValid(
                new NCBlockPos(chunkPos.getMinBlockX(), worldPosition.getY()+5, chunkPos.getMaxBlockZ()),
                1
        )) {
            pumpsAreValid = false;
            if(isClientSide) {
                BlockOverlayHandler.addToOutline(new NCBlockPos(chunkPos.getMinBlockX(), worldPosition.getY(), chunkPos.getMaxBlockZ()));
            }
        }

        if(!isPumpValid(
                new NCBlockPos(chunkPos.getMaxBlockX(), worldPosition.getY()+5, chunkPos.getMaxBlockZ()),
                2
        )) {
            if(isClientSide) {
                BlockOverlayHandler.addToOutline(new NCBlockPos(chunkPos.getMaxBlockX(), worldPosition.getY(), chunkPos.getMaxBlockZ()));
            }
            pumpsAreValid = false;
        }

        if(!isPumpValid(
                new NCBlockPos(chunkPos.getMaxBlockX(), worldPosition.getY()+5, chunkPos.getMinBlockZ()),
                3
        )) {
            if(isClientSide) {
                BlockOverlayHandler.addToOutline(new NCBlockPos(chunkPos.getMaxBlockX(), worldPosition.getY(), chunkPos.getMinBlockZ()));
            }
            pumpsAreValid = false;
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
