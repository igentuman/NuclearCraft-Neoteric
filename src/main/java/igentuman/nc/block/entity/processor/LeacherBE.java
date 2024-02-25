package igentuman.nc.block.entity.processor;

import igentuman.nc.NuclearCraft;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.handler.event.client.BlockOverlayHandler;
import igentuman.nc.handler.sided.SlotModePair;
import igentuman.nc.recipes.NcRecipeType;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.util.NCBlockPos;
import igentuman.nc.util.annotation.NBTField;
import igentuman.nc.util.annotation.NothingNullByDefault;
import igentuman.nc.util.insitu_leaching.WorldVeinsManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static igentuman.nc.block.ProcessorBlock.ACTIVE;
import static igentuman.nc.compat.GlobalVars.CATALYSTS;
import static igentuman.nc.compat.GlobalVars.RECIPE_CLASSES;
import static igentuman.nc.radiation.ItemRadiation.getItemByName;
import static igentuman.nc.setup.registration.NCItems.NC_PARTS;
import static igentuman.nc.util.ModUtil.isCcLoaded;
import static net.minecraft.world.item.Items.*;
import static igentuman.nc.util.ModUtil.isIeLoaded;;

public class LeacherBE extends NCProcessorBE<LeacherBE.Recipe> {
    public LeacherBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, Processors.LEACHER);
    }

    public static final byte PUMPS_ERROR = 4;
    public static final byte NO_SOURCE = 3;
    public static final byte WRONG_POSITION = 2;
    public static final byte POSITION_IS_CORRECT = 1;
    public static final byte NO_ACID = 0;

    protected List<ItemStack> minableOres;

    @NBTField
    public BlockPos currentMiningPos;
    @NBTField
    public boolean pumpsAreValid = false;

    @NBTField
    public byte leacherState = 2;

    @NBTField
    public ItemStack catalyst = ItemStack.EMPTY;

    protected PumpBE[] pumps = new PumpBE[4];

    protected byte pumpValidationTimeout = 40;

    @Override
    public String getName() {
        return Processors.LEACHER;
    }

    @Override
    public void tickServer() {
        if(NuclearCraft.instance.isNcBeStopped) return;
        handleState();
        byte lastState = leacherState;
        leacherState = POSITION_IS_CORRECT;

        if(!hasCatalyst()) {
            leacherState = NO_SOURCE;
        }

        if(!pumpsAreValid) {
            leacherState = PUMPS_ERROR;
        }

        if(lastState != leacherState) {
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(ACTIVE, isActive));
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState().setValue(ACTIVE, isActive), Block.UPDATE_ALL);
        }
        if(leacherState == POSITION_IS_CORRECT) {
            super.tickServer();
        }

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
                      ItemStackIngredient[] input, ItemStackIngredient[] output,
                      FluidStackIngredient[] inputFluids, FluidStackIngredient[] outputFluids,
                      double timeModifier, double powerModifier, double heatModifier, double rarity) {
            super(id, input, output, inputFluids, outputFluids, timeModifier, powerModifier, heatModifier, 1);
        }

        @Override
        public String getCodeId() {
            return Processors.LEACHER;
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
        if(contentHandler.fluidCapability.getFluidInSlot(0).isEmpty()) {
            leacherState = NO_ACID;
            return;
        }
        if(catalyst.getItem().equals(FILLED_MAP)) {
            ore = useMapCatalyst();
        }
        if(catalyst.getItem().equals(NC_PARTS.get("research_paper").get())) {
            ore = useResearchPaper();
        }
        
        if(isIeLoaded()){
            if(catalyst.getItem().equals(getItemByName("immersiveengineering:coresample"))) {
                ore = useIECoreSample();
            }
        }
        contentHandler.itemHandler.insertItemInternal(0, ore, false);
    }

    //todo implement
    protected ItemStack useIECoreSample() {
        return ItemStack.EMPTY;
    }


    protected ItemStack useResearchPaper() {
        CompoundTag tagData = catalyst.getOrCreateTag();
        if(!tagData.contains("pos") || !tagData.contains("vein")) {
            leacherState = NO_SOURCE;
            return ItemStack.EMPTY;
        }
        BlockPos mapPos = BlockPos.of(tagData.getLong("pos"));
        ChunkPos chunkPos = new ChunkPos(mapPos);
        if(!chunkPos.equals(new ChunkPos(getBlockPos()))) {
            leacherState = WRONG_POSITION;
            return ItemStack.EMPTY;
        }
        if(getLevel() == null) return ItemStack.EMPTY;
        return WorldVeinsManager.get(getLevel())
                .getWorldVeinData((ServerLevel) getLevel()).gatherRandomOre(chunkPos.x, chunkPos.z);
    }

    int currentMiningTimeout = 0;
    protected ItemStack useMapCatalyst() {

        MapItemSavedData mapData = ((MapItem)catalyst.getItem()).getSavedData(catalyst, getLevel());
        if(mapData == null) return ItemStack.EMPTY;
        if(worldPosition.getX() < mapData.x-64 || worldPosition.getX() > mapData.x+64 &&
                worldPosition.getZ() < mapData.z-64 || worldPosition.getZ() > mapData.z+64) {
            leacherState = WRONG_POSITION;
            return ItemStack.EMPTY;
        }

        if(currentMiningPos == null) {
            if(currentMiningTimeout > 200) {
                currentMiningTimeout = 0;

                currentMiningPos = new NCBlockPos(getBlockPos().getX(), getBlockPos().getY()-1, getBlockPos().getZ());
            } else {
                currentMiningTimeout++;
                return ItemStack.EMPTY;
            }
        }

        if(!mineFirstMinableBlock()) {
            currentMiningPos = null;
            currentMiningTimeout = 0;
        };
        return ItemStack.EMPTY;//this method handles inventory updates already
    }

    public List<ItemStack> allMinableOres()
    {
        if(minableOres == null) {
            minableOres = new ArrayList<>();
            for(LeacherBE.Recipe recipe: getRecipes()) {
                for(Ingredient ingredient: recipe.getItemIngredients()) {
                    minableOres.addAll(Arrays.asList(ingredient.getItems()));
                }
            }
        }
        return minableOres;
    }

    private List<Recipe> getRecipes() {
        return (List<Recipe>) NcRecipeType.ALL_RECIPES.get(getName()).getRecipeType().getRecipes(getLevel());
    }

    protected boolean mineFirstMinableBlock()
    {
        int startY = currentMiningPos.getY();
        int startX = new ChunkPos(currentMiningPos).getMinBlockX();
        int startZ = new ChunkPos(currentMiningPos).getMinBlockZ();
        NCBlockPos tempMiningPos = new NCBlockPos(currentMiningPos);
        for(int y = startY; y > getLevel().getMinBuildHeight(); y--) {
           for(int x = 0; x < 16; x++) {
               for(int z = 0; z < 16; z++) {
                   BlockState toCheck = getLevel().getBlockState(tempMiningPos.y(y).x(x+startX).z(z+startZ));
                   if(toCheck.is(Tags.Blocks.ORES)) {
                        ItemStack toMine = new ItemStack(toCheck.getBlock());
                        if(isMinable(toMine)) {
                            currentMiningPos = new BlockPos(tempMiningPos);
                            if(contentHandler.itemHandler.insertItemInternal(0, toMine, true).isEmpty()) {
                                //todo add permissions check support
                                getLevel().destroyBlock(tempMiningPos.y(y).x(x+startX).z(z+startZ), false);
                                contentHandler.itemHandler.insertItemInternal(0, toMine, false);
                                return true;
                            }
                        }
                   }
               }
           }
        }
        return false;
    }

    private boolean isMinable(ItemStack toMine) {
        for(ItemStack stack: allMinableOres()) {
            if(stack.equals(toMine, false)) return true;
        }
        return false;
    }

    @Nonnull
    @Override
    public final <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) { //not letting to access item handler from outside
            return LazyOptional.empty();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public List<Item> getAllowedCatalysts() {
        List<Item> items = new ArrayList<>(List.of(
                NC_PARTS.get("research_paper").get(),
                FILLED_MAP
        ));
        if(isIeLoaded()){
            Item ieCoreSample = getItemByName("immersiveengineering:coresample");
            items.add(ieCoreSample);
        }
        return items;
    }

    public int toggleSideConfig(int slotId, int direction) {
        if(slotId == 1) return SlotModePair.SlotMode.DISABLED.ordinal();
        setChanged();
        saveSideMapFlag = true;
        return contentHandler.toggleSideConfig(slotId, direction);
    }
}
