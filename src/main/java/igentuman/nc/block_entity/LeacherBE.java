package igentuman.nc.block_entity;

import igentuman.nc.item.ResearchPaperItem;
import igentuman.nc.recipe.UniversalProcessorRecipe;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.NBTField;
import igentuman.nc.util.insitu_leaching.WorldVeinsManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;


public class LeacherBE extends UniversalProcessorBE {

    public static final byte NO_ACID = 0;
    public static final byte POSITION_IS_CORRECT = 1;
    public static final byte WRONG_POSITION = 2;
    public static final byte NO_SOURCE = 3;
    public static final byte PUMPS_ERROR = 4;

    private static final TagKey<Block> ORES_TAG = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", "ores"));

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

    public void validatePumps() {
        if (level == null) return;
        ChunkPos chunkPos = ChunkPos.containing(getBlockPos());
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

    protected void gatherOre() {
        if (!contentHandler.hasItemCapability()) return;
        if (!contentHandler.getItemHandler().getStackInSlot(0).isEmpty()) return;
        ItemStack cat = getCatalystStack();
        if (cat.isEmpty()) return;
        if (!hasAcid()) {
            leacherState = NO_ACID;
            return;
        }
        if (cat.getItem() instanceof ResearchPaperItem) {
            useResearchPaper();
        } else if (cat.is(Items.FILLED_MAP)) {
            useMapCatalyst();
        }
    }

    protected void useResearchPaper() {
        CompoundTag tag = catalyst.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.getLong("pos").isEmpty() || tag.getString("vein").isEmpty()) {
            leacherState = NO_SOURCE;
            return;
        }
        BlockPos mapPos = BlockPos.of(tag.getLong("pos").orElseThrow());
        ChunkPos chunkPos = ChunkPos.containing(mapPos);
        if (!chunkPos.equals(ChunkPos.containing(getBlockPos()))) {
            leacherState = WRONG_POSITION;
            return;
        }
        if (!(level instanceof ServerLevel serverLevel)) return;
        try (var transaction = Transaction.openRoot()) {
            ItemStack ore = WorldVeinsManager.get(serverLevel).getWorldVeinData(serverLevel)
                    .gatherRandomOre(chunkPos.x(), chunkPos.z(), transaction);
            if (ore.isEmpty()) return;
            if (contentHandler.getItemHandler().insert(0, ItemResource.of(ore), ore.getCount(), transaction)
                    != ore.getCount()) return;
            transaction.commit();
        }
    }

    protected void useMapCatalyst() {
        if (currentMiningTimeout > 0) {
            currentMiningTimeout--;
            return;
        }
        if (!(level instanceof ServerLevel)) return;
        ChunkPos chunkPos = ChunkPos.containing(getBlockPos());
        int minX = chunkPos.getMinBlockX();
        int minZ = chunkPos.getMinBlockZ();
        int startY = getBlockPos().getY() - 1;
        for (int y = startY; y >= level.getMinY(); y--) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    BlockPos pos = new BlockPos(minX + x, y, minZ + z);
                    BlockState state = level.getBlockState(pos);
                    if (!state.is(ORES_TAG)) continue;
                    ItemStack toMine = new ItemStack(state.getBlock());
                    if (!isMinable(toMine)) continue;
                    try (var transaction = Transaction.openRoot()) {
                        if (toMine.isEmpty() || contentHandler.getItemHandler().insert(0, ItemResource.of(toMine),
                                toMine.getCount(), transaction) != toMine.getCount()) continue;
                        // Reserve inventory space before the irreversible world edit; refusal aborts the insertion.
                        if (!level.destroyBlock(pos, false)) continue;
                        transaction.commit();
                    }
                    currentMiningTimeout = 20;
                    currentMiningPos = pos;
                    setChanged();
                    return;
                }
            }
        }
        currentMiningTimeout = 20;
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

    @Override
    @Nullable
    public ResourceHandler<ItemResource> getItemHandler(@Nullable Direction side) {
        if (side != null) return null;
        return super.getItemHandler(null);
    }
}
