package igentuman.nc.block_entity;

import igentuman.nc.compat.ie.IEMineralVein;
import igentuman.nc.config.Common;
import igentuman.nc.item.ResearchPaperItem;
import igentuman.nc.recipe.UniversalProcessorRecipe;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.NBTField;
import igentuman.nc.util.insitu_leaching.WorldVeinsManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** Leacher processor BE; extracts ore from virtual veins or physical blocks using aqua regia acid and a catalyst. */
public class LeacherBE extends UniversalProcessorBE {

    public static final byte NO_ACID = 0;
    public static final byte POSITION_IS_CORRECT = 1;
    public static final byte WRONG_POSITION = 2;
    public static final byte NO_SOURCE = 3;
    public static final byte PUMPS_ERROR = 4;

    private static final TagKey<Block> ORES_TAG = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", "ores"));

    @NBTField
    public BlockPos currentMiningPos = null;

    @NBTField
    public boolean pumpsAreValid = false;

    @NBTField(syncToClient = true)
    public byte leacherState = WRONG_POSITION;

    @NBTField
    public ItemStack catalyst = ItemStack.EMPTY;

    protected PumpBE[] pumps = new PumpBE[4];
    protected byte pumpValidationTimeout = 80;
    protected int currentMiningTimeout = 0;

    public LeacherBE(BlockPos pos, BlockState state, String name) {
        super(pos, state, name);
    }

    @Override
    public void serverTick() {
        handleState();
        byte lastState = leacherState;
        leacherState = POSITION_IS_CORRECT;

        if (!hasCatalyst()) {
            leacherState = NO_SOURCE;
        }
        if (!pumpsAreValid) {
            leacherState = PUMPS_ERROR;
        }

        if (lastState != leacherState) {
            setChanged();
        }

        catalyst = getCatalystStack();

        if (leacherState == POSITION_IS_CORRECT) {
            gatherOre();
            super.serverTick();
        }
    }

    protected void handleState() {
        pumpValidationTimeout--;
        if (pumpValidationTimeout <= 0) {
            pumpValidationTimeout = 80;
            validatePumps();
        }
    }

    /** Checks the four chunk-corner positions for valid PumpBE instances. */
    public void validatePumps() {
        if (level == null) return;
        ChunkPos chunkPos = new ChunkPos(getBlockPos());
        pumpsAreValid = isPumpValid(chunkPos.getMinBlockX(), worldPosition.getY() + 1, chunkPos.getMinBlockZ(), 0);
        if (!isPumpValid(chunkPos.getMinBlockX(), worldPosition.getY() + 1, chunkPos.getMaxBlockZ(), 1)) pumpsAreValid = false;
        if (!isPumpValid(chunkPos.getMaxBlockX(), worldPosition.getY() + 1, chunkPos.getMaxBlockZ(), 2)) pumpsAreValid = false;
        if (!isPumpValid(chunkPos.getMaxBlockX(), worldPosition.getY() + 1, chunkPos.getMinBlockZ(), 3)) pumpsAreValid = false;
    }

    protected boolean isPumpValid(int x, int y, int z, int id) {
        BlockPos pos = new BlockPos(x, y+1, z);
        for(int i = 1; i < 4; i++) {
            if (level.getBlockEntity(pos.relative(Direction.DOWN, i)) instanceof PumpBE pump) {
                pumps[id] = pump;
                return pump.isInSituValid();
            }
        }

        pumps[id] = null;
        return false;
    }

    public PumpBE[] getPumpsForClient() {
        if (pumpsAreValid && pumps[0] != null) return pumps;
        validatePumps();
        return pumps;
    }

    public boolean hasCatalyst() {
        return !getCatalystStack().isEmpty();
    }

    protected ItemStack getCatalystStack() {
        if (!contentHandler.hasItemCapability()) return ItemStack.EMPTY;
        ModEntry entry = ModEntries.get(name);
        if (entry == null || entry.itemCap() == null) return ItemStack.EMPTY;
        int base = entry.itemCap().inputSlots + entry.itemCap().outputSlots + entry.itemCap().globalSlots;
        return contentHandler.getItemHandler().getStackInSlot(base);
    }

    protected boolean hasAcid() {
        if (!contentHandler.hasFluidCapability()) return false;
        return !contentHandler.getFluidHandler().getFluidInTank(0).isEmpty();
    }

    /** Dispatches ore gathering to the appropriate catalyst mode (research paper, map, or IE core sample). */
    protected void gatherOre() {
        if (!contentHandler.hasItemCapability()) return;
        if (!contentHandler.getItemHandler().getStackInSlot(0).isEmpty()) return;
        ItemStack cat = getCatalystStack();
        if (cat.isEmpty()) return;
        if (!hasAcid()) {
            leacherState = NO_ACID;
            return;
        }
        ItemStack ore = ItemStack.EMPTY;
        if (cat.getItem() instanceof ResearchPaperItem) {
            ore = useResearchPaper();
        } else if (cat.is(Items.FILLED_MAP)) {
            ore = useMapCatalyst();
        } else if (isIECoreSample(cat)) {
            ore = useIECoreSample();
        }
        if (!ore.isEmpty()) {
            contentHandler.getItemHandler().insertItem(0, ore, false);
        }
    }

    /** Reads vein NBT from a research paper and pulls ore from the virtual vein via WorldVeinsManager. */
    protected ItemStack useResearchPaper() {
        CompoundTag tag = catalyst.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.contains("pos") || !tag.contains("vein")) {
            leacherState = NO_SOURCE;
            return ItemStack.EMPTY;
        }
        BlockPos mapPos = BlockPos.of(tag.getLong("pos"));
        ChunkPos chunkPos = new ChunkPos(mapPos);
        if (!chunkPos.equals(new ChunkPos(getBlockPos()))) {
            leacherState = WRONG_POSITION;
            return ItemStack.EMPTY;
        }
        if (!(level instanceof ServerLevel serverLevel)) return ItemStack.EMPTY;
        return WorldVeinsManager.get(serverLevel)
                .getWorldVeinData(serverLevel)
                .gatherRandomOre(chunkPos.x, chunkPos.z);
    }

    /** Scans the chunk below for ore blocks, destroys the first minable one, and inserts it into the input slot. */
    protected ItemStack useMapCatalyst() {
        if (currentMiningTimeout > 0) {
            currentMiningTimeout--;
            return ItemStack.EMPTY;
        }
        if (level == null) return ItemStack.EMPTY;
        ChunkPos chunkPos = new ChunkPos(getBlockPos());
        int minX = chunkPos.getMinBlockX();
        int minZ = chunkPos.getMinBlockZ();
        int startY = getBlockPos().getY() - 1;
        for (int y = startY; y >= level.getMinBuildHeight(); y--) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    BlockPos pos = new BlockPos(minX + x, y, minZ + z);
                    BlockState state = level.getBlockState(pos);
                    if (!state.is(ORES_TAG)) continue;
                    ItemStack toMine = new ItemStack(state.getBlock());
                    if (!isMinable(toMine)) continue;
                    if (contentHandler.getItemHandler().insertItem(0, toMine, true).isEmpty()) {
                        level.destroyBlock(pos, false);
                        contentHandler.getItemHandler().insertItem(0, toMine, false);
                        currentMiningTimeout = 20;
                        currentMiningPos = pos;
                        setChanged();
                        return ItemStack.EMPTY;
                    }
                }
            }
        }
        currentMiningTimeout = 20;
        return ItemStack.EMPTY;
    }

    private boolean isMinable(ItemStack stack) {
        for (Recipe<?> recipe : recipeInfo.getRecipes().values()) {
            if (recipe instanceof UniversalProcessorRecipe upr) {
                for (SizedIngredient input : upr.getItemInputs()) {
                    if (input.test(stack)) return true;
                }
            }
        }
        return false;
    }

    /** Validates chunk + dimension match against an IE core sample and gets ore from IE's mineral vein system. */
    protected ItemStack useIECoreSample() {
        if (!ModList.get().isLoaded("immersiveengineering")) return ItemStack.EMPTY;
        if (!(level instanceof ServerLevel serverLevel)) return ItemStack.EMPTY;
        ChunkPos sampleChunk = IEMineralVein.getChunkPos(catalyst);
        if (sampleChunk == null) return ItemStack.EMPTY;
        if (!sampleChunk.equals(new ChunkPos(getBlockPos()))) {
            leacherState = WRONG_POSITION;
            return ItemStack.EMPTY;
        }
        var sampleDim = IEMineralVein.getDimension(catalyst);
        if (sampleDim != null && !level.dimension().equals(sampleDim)) {
            leacherState = WRONG_POSITION;
            return ItemStack.EMPTY;
        }
        return IEMineralVein.getNextVeinItem(serverLevel, catalyst, allowedInputItems());
    }

    private boolean isIECoreSample(ItemStack stack) {
        if (!Common.IN_SITU_ALLOW_IE_VEINS.get()) return false;
        if (!ModList.get().isLoaded("immersiveengineering")) return false;
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().equals("immersiveengineering:coresample");
    }

    /** Returns the list of item stacks that the leacher can accept as catalysts. */
    protected List<ItemStack> allowedInputItems() {
        List<ItemStack> items = new ArrayList<>();
        ModEntry rpEntry = ModEntries.get("research_paper");
        if (rpEntry != null && rpEntry.hasItem()) {
            items.add(new ItemStack(rpEntry.item().get()));
        }
        items.add(new ItemStack(Items.FILLED_MAP));
        if (ModList.get().isLoaded("immersiveengineering")) {
            Item ieCoreSample = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("immersiveengineering", "coresample"));
            if (ieCoreSample != Items.AIR) {
                items.add(new ItemStack(ieCoreSample));
            }
        }
        return items;
    }

    @Override
    @Nullable
    public IItemHandler getItemHandler(@Nullable Direction side) {
        if (side != null) return null;
        return super.getItemHandler(null);
    }
}
