package igentuman.nc.block.entity.fission;

import igentuman.nc.handler.NCItemStackHandler;
import igentuman.nc.recipes.FissionRecipe;
import igentuman.nc.recipes.RecipeInfo;
import igentuman.nc.setup.multiblocks.FissionBlocks;
import igentuman.nc.setup.registration.NCFluids;
import igentuman.nc.util.CustomEnergyStorage;
import igentuman.nc.util.NBTField;
import igentuman.nc.util.ValidationResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static igentuman.nc.handler.config.CommonConfig.FissionConfig.*;


public class FissionControllerBE extends FissionBE {
    public static String NAME = "fission_reactor_controller";

    @NBTField
    public int heat = 0;
    @NBTField
    public int fuelCellsCount = 0;
    @NBTField
    public int activeModeratorsCount = 0;

    @NBTField
    public int activeHeatSinksCount = 0;
    @NBTField
    public int activeModeratorsAttachmentsCount = 0;
    @NBTField
    public boolean isCasingValid = false;
    @NBTField
    public boolean isInternalValid = false;
    @NBTField
    public int height = 1;
    @NBTField
    public int width = 1;
    @NBTField
    public int depth = 1;
    @NBTField
    public double heatSinkCooling = 0;
    @NBTField
    public double heatPerTick = 0;
    @NBTField
    public int energyPerTick = 0;
    @NBTField
    public double heatMultiplier = 0;


    private Direction facing;
    private static List<Block> moderatorBlocks = new ArrayList<>();
    private List<BlockPos> moderators = new ArrayList<>();
    private List<BlockPos> heatSinks = new ArrayList<>();
    public List<BlockPos> fuelCells = new ArrayList<>();

    public HashMap<BlockPos, FissionHeatSinkBE> activeHeatSinks = new HashMap<>();

    public ValidationResult validationResult = new ValidationResult(false, "", BlockPos.ZERO);
    public int topCasing = 0;
    public int bottomCasing = 0;
    public int leftCasing = 0;
    public int rightCasing = 0;
    public final NCItemStackHandler itemHandler = createHandler();
    protected final LazyOptional<IItemHandler> handler = LazyOptional.of(() -> itemHandler);
    public final CustomEnergyStorage energyStorage = createEnergy();
    public RecipeInfo recipeInfo = new RecipeInfo();



    public FissionControllerBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, NAME);
    }

    public LazyOptional<IEnergyStorage> getEnergy() {
        return energy;
    }

    protected final LazyOptional<IEnergyStorage> energy = LazyOptional.of(() -> energyStorage);

    private NCItemStackHandler createHandler() {
        return new NCItemStackHandler(3) {

            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                return true;//todo recipe validator
            }

            @Override
            @NotNull
            public ItemStack extractItem(int slot, int amount, boolean simulate)
            {
                if(slot != 1) return ItemStack.EMPTY;
                return super.extractItem(slot, amount, simulate);
            }


            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                if(slot != 0) return ItemStack.EMPTY;
                return super.insertItem(slot, stack, simulate);
            }
        };
    }

    private CustomEnergyStorage createEnergy() {
        return new CustomEnergyStorage(1000000, 0, 1000000) {
            @Override
            protected void onEnergyChanged() {
                setChanged();
            }
        };
    }

    private void updateEnergyStorage() {
        energyStorage.setMaxCapacity(Math.max(fuelCellsCount,1)*1000000);
        energyStorage.setMaxExtract(Math.max(fuelCellsCount,1)*1000000);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return handler.cast();
        }
        if (cap == ForgeCapabilities.ENERGY) {
            return energy.cast();
        }
        return super.getCapability(cap, side);
    }

    public void tickClient() {
    }

    public void tickServer() {
        validationResult = validateStructure();
        processReaction();
        coolDown();
        sendOutPower();
        handleMeltdown();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    private void handleMeltdown() {
        if(heat >= getMaxHeat()) {
            if(EXPLOSION_RADIUS.get() == 0) {
                getLevel().explode(null, getForwardPos(2).getX(), getForwardPos(2).getY(), getForwardPos(2).getZ(), 2F, true, Explosion.BlockInteraction.NONE);
            } else {
                getLevel().explode(null, getForwardPos(2).getX(), getForwardPos(2).getY(), getForwardPos(2).getZ(), EXPLOSION_RADIUS.get().floatValue(), true, Explosion.BlockInteraction.DESTROY);
            }
            for(BlockPos pos: fuelCells) {
                getLevel().setBlock(pos, NCFluids.NC_MATERIALS.get("corium").getBlock().defaultBlockState(), 1);
            }
            getLevel().setBlock(getBlockPos(), NCFluids.NC_MATERIALS.get("corium").getBlock().defaultBlockState(), 1);
            setRemoved();
            //at any case if reactor still works we punish player
            //heat = getMaxHeat();
            //energyStorage.setEnergy((int) (energyStorage.getEnergyStored() - calculateEnergy()));
        }

    }

    private void coolDown() {
        heat -= coolingPerTick();
        heat = Math.max(0, heat);
    }

    private boolean processReaction() {
        if(!isCasingValid || !isInternalValid) {
            return false;
        }
        heatMultiplier = heatMultiplier()+collectedHeatMultiplier()-1;
        if(!hasRecipe() && !itemHandler.getStackInSlot(0).equals(ItemStack.EMPTY)) {
            updateRecipe();
        }
        if(hasRecipe()) {
            return process();
        }
        return false;
    }

    private boolean process() {
        recipeInfo.process(fuelCellsCount*(heatMultiplier()+collectedHeatMultiplier()-1));
        if(!recipeInfo.isCompleted()) {
            energyStorage.addEnergy((int) calculateEnergy());
            heat += calculateHeat();
        }

        if(recipeInfo.isCompleted()) {
            handleRecipeOutput();
        }
        return true;
    }

    protected void sendOutPower() {
        AtomicInteger capacity = new AtomicInteger(energyStorage.getEnergyStored());
        if (capacity.get() > 0) {
            for (Direction direction : Direction.values()) {
                BlockEntity be = level.getBlockEntity(worldPosition.relative(direction));
                if (be != null) {
                    boolean doContinue = be.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).map(handler -> {
                                if (handler.canReceive()) {
                                    int received = handler.receiveEnergy(Math.min(capacity.get(), energyStorage.getMaxEnergyStored()), false);
                                    capacity.addAndGet(-received);
                                    energyStorage.consumeEnergy(received);
                                    setChanged();
                                    return capacity.get() > 0;
                                } else {
                                    return true;
                                }
                            }
                    ).orElse(true);
                    if (!doContinue) {
                        return;
                    }
                }
            }
        }
    }

    private void handleRecipeOutput() {
        if(recipeInfo.recipe!=null) {
            if(itemHandler.insertItemInternal(1, recipeInfo.recipe.getResultItem(), true).isEmpty()) {
                itemHandler.insertItemInternal(1, recipeInfo.recipe.getResultItem(), false);
                itemHandler.extractItemInternal(2, 1, false);
                recipeInfo.reset();
            }
        } else {
            itemHandler.extractItemInternal(2, 1, false);
            recipeInfo.reset();
        }

    }

    public double heatMultiplier() {
        double h = heatPerTick();
        double c = Math.max(1, coolingPerTick());
        return Math.log10(h/c)/(1+Math.exp(h/c*HEAT_MULTIPLIER.get()))+1;
    }

    //hotter reactor gives some advantage in FE generation
    public double collectedHeatMultiplier()
    {
        return Math.min(HEAT_MULTIPLIER_CAP.get(), Math.pow((heat+getMaxHeat()/8)/getMaxHeat(),5)+0.9999694824);
    }

    public double coolingPerTick() {
        return heatSinksCooling()+environmentCooling();
    }

    public double environmentCooling() {
        return getLevel().getBiome(getBlockPos()).get().getBaseTemperature()*10;
    }


    public double heatSinksCooling() {
        if(heatSinkCooling == 0) {
            for (FissionHeatSinkBE hs : activeHeatSinks().values()) {
                heatSinkCooling+=hs.getHeat();
            }
        }
        return heatSinkCooling;
    }

    public Map<BlockPos, FissionHeatSinkBE> activeHeatSinks() {
        if(activeHeatSinks.isEmpty()) {
            for(BlockPos hpos: heatSinks) {
                BlockEntity be = getLevel().getBlockEntity(hpos);
                if(be instanceof FissionHeatSinkBE) {
                    FissionHeatSinkBE hs = (FissionHeatSinkBE) be;
                    if(hs.isValid(true)) {
                        activeHeatSinks.put(hpos, hs);
                    }
                }
            }
        }
        activeHeatSinksCount = activeHeatSinks.size();
        return activeHeatSinks;
    }

    public double heatPerTick() {
        heatPerTick = recipeInfo.heat*fuelCellsCount+moderatorsHeat();
        return heatPerTick;
    }

    private double calculateHeat() {
        return heatPerTick();
    }

    private int calculateEnergy() {
        energyPerTick = (int) ((recipeInfo.energy*fuelCellsCount+moderatorsFE())*(heatMultiplier()+collectedHeatMultiplier()-1));
        return energyPerTick;
    }

    public double moderatorsHeat() {
        return recipeInfo.heat*activeModeratorsAttachmentsCount*(MODERATOR_HEAT_MULTIPLIER.get()/100);
    }

    public double moderatorsFE() {
        return recipeInfo.energy*activeModeratorsAttachmentsCount*(MODERATOR_FE_MULTIPLIER.get()/100);
    }


    private void updateRecipe() {
        if(recipeIsStuck()) return;
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());

        inventory.setItem(0, itemHandler.getStackInSlot(0));

        Optional<FissionRecipe> recipe = level.getRecipeManager()
                .getRecipeFor(FissionRecipe.Type.INSTANCE, inventory, level);

        if(recipe.isPresent() && itemHandler.getStackInSlot(2).isEmpty()) {
            ItemStack input = itemHandler.getStackInSlot(0).copy();
            input.setCount(1);
            itemHandler.extractItemInternal(0, 1, false);
            itemHandler.insertItemInternal(2, input, false);
            recipeInfo.setRecipe(recipe.get());
            recipeInfo.ticks = ((FissionRecipe)recipeInfo.recipe).getDepletionTime();
            recipeInfo.energy = ((FissionRecipe)recipeInfo.recipe).getEnergy();
            recipeInfo.heat = ((FissionRecipe)recipeInfo.recipe).getHeat();
            recipeInfo.radiation = ((FissionRecipe)recipeInfo.recipe).getRadiation();
        }
    }

    public boolean recipeIsStuck() {
        if(recipeInfo.isCompleted() || recipeInfo.recipe == null) {
            handleRecipeOutput();
            return !itemHandler.getStackInSlot(2).isEmpty();
        }
        return false;
    }

    public boolean hasRecipe() {
        return !recipeIsStuck() && recipeInfo.recipe != null;
    }


    @Override
    public String getName() {
        return NAME;
    }

    public ValidationResult validateStructure()
    {
        ValidationResult casingValidation = validateCasing(getBlockPos(), getLevel());
        isCasingValid = casingValidation.isValid;
        if(!isCasingValid) {
            isInternalValid = false;
            return casingValidation;
        }
        ValidationResult internalValidation = validateInterior(getBlockPos(), getLevel());
        isInternalValid = internalValidation.isValid;
        if(!internalValidation.isValid) {
            return internalValidation;
        }
        return new ValidationResult(true);
    }

    public ValidationResult validateInterior(BlockPos blockPos, Level level) {
        fuelCells.clear();
        heatSinks.clear();
        moderators.clear();
        activeHeatSinks.clear();
        activeModeratorsAttachmentsCount = 0;
        activeModeratorsCount = 0;
        heatSinkCooling = 0;
        for(int y = 1; y < getHeight()-1; y++) {
            for(int x = 1; x < getWidth()-1; x++) {
                for (int z = 1; z < getDepth()-1; z++) {
                    BlockPos toCheck = getSidePos(x - leftCasing).above(y - bottomCasing).relative(getFacing(), -z);
                    if (isFissionCasing(toCheck)) {
                        return new ValidationResult(
                                false,
                                "fission.interior.casing_inside_reactor",
                                getSidePos(x - leftCasing).above(y - bottomCasing).relative(getFacing(), -z)
                        );
                    }
                    if(isFuelCell(toCheck)) {
                        BlockEntity be = getLevel().getBlockEntity(toCheck);
                        if(be instanceof FissionFuelCellBE) {
                            fuelCells.add(toCheck);
                            activeModeratorsAttachmentsCount += ((FissionFuelCellBE) be).getAttachedModeratorsCount();
                        }
                    }
                    if(isModerator(toCheck, getLevel())) {
                        moderators.add(toCheck);
                    }
                    if(isHeatSink(toCheck)) {
                        heatSinks.add(toCheck);
                    }
                }
            }
        }
        fuelCellsCount = fuelCells.size();
        updateEnergyStorage();
        energyStorage.addEnergy(0);
        activeModeratorsCount = moderators.size();
        if(fuelCellsCount == 0) {
            return new ValidationResult(false, "fission.interior.no_fuel_cells", BlockPos.ZERO);
        }
        return new ValidationResult(true);
    }

    public static boolean isModerator(BlockPos pos, Level world) {
        if(moderatorBlocks.isEmpty()) {
            moderatorBlocks = getBlocksByTagKey(FissionBlocks.MODERATORS_BLOCKS.location().toString());
        }
        return  moderatorBlocks.contains(Objects.requireNonNull(world).getBlockState(pos).getBlock());
    }

    private boolean isHeatSink(BlockPos pos) {
        return getLevel().getBlockState(pos).toString().contains("heat_sink");
    }

    private boolean isFuelCell(BlockPos pos) {
        return getLevel().getBlockState(pos).toString().contains("fission_reactor_solid_fuel_cell");
    }

    private void notifyPlayers(String messageKey, BlockPos errorBlock) {
        //getLevel().getNearestPlayer()
    }

    public ValidationResult validateCasing(BlockPos blockPos, Level level) {
        for(int y = 0; y < getHeight(); y++) {
            for(int x = 0; x < getWidth(); x++) {
                for (int z = 0; z < getDepth(); z++) {
                    if(width < 3 || height < 3 || depth < 3)
                    {
                        return new ValidationResult(
                                false,
                                "fission.casing.reactor_incomplete",
                                getBlockPos()
                        );
                    }
                    if(y == 0 || x == 0 || z == 0 || y == getHeight()-1 || x == getWidth()-1 || z == getDepth()-1) {
                        if (!isFissionCasing(getSidePos(x - leftCasing).above(y - bottomCasing).relative(getFacing(), -z))) {
                            return new ValidationResult(
                                    false,
                                    "fission.casing.wrong.block",
                                    getSidePos(x - leftCasing).above(y - bottomCasing).relative(getFacing(), -z)
                            );
                        }
                        setControllerToBlock(getSidePos(x - leftCasing).above(y - bottomCasing).relative(getFacing(), -z));
                    }
                }
            }
        }
        return new ValidationResult(true);
    }

    private void setControllerToBlock(BlockPos pos) {
        BlockEntity be = getLevel().getBlockEntity(pos);
        if(be instanceof FissionBE) {
            ((FissionBE) be).controller = this;
        }
    }

    public boolean isFissionCasing(BlockPos pos)
    {
        if(getLevel() == null) return false;
        if(casingBlocks.isEmpty()) {
            casingBlocks = getBlocksByTagKey(FissionBlocks.CASING_BLOCKS.location().toString());
        }
        try {
            return  casingBlocks.contains(getLevel().getBlockState(pos).getBlock());
        } catch (NullPointerException ignored) { }
        return false;
    }

    protected List<Block> casingBlocks = new ArrayList<>();

    private static List<Block> getBlocksByTagKey(String key)
    {
        List<Block> tmp = new ArrayList<>();
        TagKey<Block> tag = TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(key));
        for(Holder<Block> holder : Registry.BLOCK.getTagOrEmpty(tag)) {
            tmp.add(holder.get());
        }
        return tmp;
    }

    public int getHeight()
    {
        if(height == 1 || !isCasingValid) {
            for(int i = 1; i<24-1; i++) {
                if(!isFissionCasing(getBlockPos().above(i))) {
                    topCasing = i-1;
                    height = i;
                    break;
                }
            }
            for(int i = 1; i<24-height-1; i++) {
                if(!isFissionCasing(getBlockPos().below(i))) {
                    bottomCasing = i-1;
                    height += i-1;
                    break;
                }
            }
        }
        return height;
    }

    public int getWidth()
    {
        if(width == 1 || !isCasingValid) {
            for(int i = 1; i<24-1; i++) {
                if(!isFissionCasing(getLeftPos(i))) {
                    leftCasing = i-1;
                    width = i;
                    break;

                }
            }
            for(int i = 1; i<24-width-1; i++) {
                if(!isFissionCasing(getRightPos(i))) {
                    rightCasing = i-1;
                    width += i-1;
                    break;
                }
            }
        }
        return width;
    }

    public int getDepth()
    {
        if(depth == 1 || !isCasingValid) {
            for(int i = 1; i<24-1; i++) {
                if(!isFissionCasing(getForwardPos(i).above(topCasing))) {
                    depth = i;
                    break;
                }
            }
        }
        return depth;
    }

    public BlockPos getForwardPos(int i) {
        return getBlockPos().relative(getFacing(), -i);
    }

    public BlockPos getLeftPos(int i)
    {
        return getSidePos(-i);
    }

    public BlockPos getRightPos(int i)
    {
        return getSidePos(i);
    }

    public BlockPos getSidePos(int i) {
        return switch (getFacing().ordinal()) {
            case 3 -> getBlockPos().east(i);
            case 5 -> getBlockPos().north(i);
            case 2 -> getBlockPos().west(i);
            case 4 -> getBlockPos().south(i);
            default -> null;
        };
    }


    @Override
    public void load(CompoundTag tag) {
        if (tag.contains("Inventory")) {
            itemHandler.deserializeNBT(tag.getCompound("Inventory"));
        }

        if (tag.contains("Energy")) {
            energyStorage.deserializeNBT(tag.get("Energy"));
        }
        if (tag.contains("Info")) {
            CompoundTag infoTag = tag.getCompound("Info");
            readTagData(infoTag);
            if (infoTag.contains("recipeInfo")) {
                recipeInfo.deserializeNBT(infoTag.getCompound("recipeInfo"));
            }
            if(!isCasingValid || !isInternalValid) {
                validationResult = new ValidationResult(
                        false,
                        infoTag.getString("validationKey"),
                        BlockPos.of(infoTag.getLong("erroredBlock"))
                );
            } else {
                validationResult = new ValidationResult(true);
            }
        }
        super.load(tag);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        CompoundTag infoTag = new CompoundTag();
        tag.put("Inventory", itemHandler.serializeNBT());
        tag.put("Energy", energyStorage.serializeNBT());
        infoTag.put("recipeInfo", recipeInfo.serializeNBT());
        infoTag.putString("validationKey", validationResult.messageKey);
        infoTag.putLong("erroredBlock", validationResult.errorBlock.asLong());
        saveTagData(infoTag);
        tag.put("Info", infoTag);
    }

    private Direction getFacing() {
        if(facing == null) {
            facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        }
        return facing;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        if (tag != null) {
            loadClientData(tag);
        }
    }

    private void loadClientData(CompoundTag tag) {
        if (tag.contains("Info")) {
            CompoundTag infoTag = tag.getCompound("Info");
            if (infoTag.contains("recipeInfo")) {
                recipeInfo.deserializeNBT(infoTag.getCompound("recipeInfo"));
            }
            energyStorage.setEnergy(infoTag.getInt("energy"));
            readTagData(infoTag);
            if(!isCasingValid || !isInternalValid) {
                validationResult = new ValidationResult(
                        false,
                        infoTag.getString("validationKey"),
                        BlockPos.of(infoTag.getLong("erroredBlock"))
                );
            } else {
                validationResult = new ValidationResult(true);
            }
        }

    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveClientData(tag);
        return tag;
    }

    private void saveClientData(CompoundTag tag) {
        CompoundTag infoTag = new CompoundTag();
        tag.put("Info", infoTag);
        infoTag.putInt("energy", energyStorage.getEnergyStored());
        saveTagData(infoTag);
        infoTag.put("recipeInfo", recipeInfo.serializeNBT());

        infoTag.putString("validationKey", validationResult.messageKey);
        infoTag.putLong("erroredBlock", validationResult.errorBlock.asLong());
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        int oldEnergy = energyStorage.getEnergyStored();

        CompoundTag tag = pkt.getTag();
        handleUpdateTag(tag);

        if (oldEnergy != energyStorage.getEnergyStored()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    public double getDepletionProgress() {
        return recipeInfo.getProgress();
    }

    public double getMaxHeat() {
        return 1000000;
    }

    public double getEfficiency() {
        return (double)calculateEnergy()/((double)recipeInfo.energy/100);
    }

    public double getNetHeat() {
        return heatPerTick - heatSinkCooling;
    }
}
