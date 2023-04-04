package igentuman.nc.block.entity.fission;

import igentuman.nc.setup.multiblocks.FissionBlocks;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class FissionControllerBE extends FissionBE {
    public static String NAME = "fission_reactor_controller";

    @NBTField
    public int heat = 0;
    @NBTField
    public int fuelCellsCount = 0;
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


    private Direction facing;
    private List<Block> moderatorBlocks = new ArrayList<>();
    private List<BlockPos> moderators = new ArrayList<>();
    private List<BlockPos> heatSinks = new ArrayList<>();
    public List<BlockPos> fuelCells = new ArrayList<>();


    public ValidationResult validationResult;
    public int topCasing = 0;
    public int bottomCasing = 0;
    public int leftCasing = 0;
    public int rightCasing = 0;
    protected final ItemStackHandler itemHandler = createHandler();
    protected final LazyOptional<IItemHandler> handler = LazyOptional.of(() -> itemHandler);
    public final CustomEnergyStorage energyStorage = createEnergy();
    public FissionControllerBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, NAME);
    }

    public LazyOptional<IEnergyStorage> getEnergy() {
        return energy;
    }

    protected final LazyOptional<IEnergyStorage> energy = LazyOptional.of(() -> energyStorage);

    private ItemStackHandler createHandler() {
        return new ItemStackHandler(2) {

            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                return true;//todo recipe validator
            }

            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                return super.insertItem(slot, stack, simulate);
            }
        };
    }

    private CustomEnergyStorage createEnergy() {
        return new CustomEnergyStorage(10000000, 1000000, 0) {
            @Override
            protected void onEnergyChanged() {
                setChanged();
            }
        };
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
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
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
                        fuelCells.add(toCheck);
                    }
                    if(isModerator(toCheck)) {
                        moderators.add(toCheck);
                    }
                    if(isHeatSink(toCheck)) {
                        heatSinks.add(toCheck);
                    }
                }
            }
        }
        fuelCellsCount = fuelCells.size();
        if(fuelCellsCount == 0) {
            return new ValidationResult(false, "fission.interior.no_fuel_cells", BlockPos.ZERO);
        }
        return new ValidationResult(true);
    }

    private boolean isModerator(BlockPos pos) {
        if(moderatorBlocks.isEmpty()) {
            moderatorBlocks = getBlocksByTagKey(FissionBlocks.MODERATORS_BLOCKS.location().toString());
        }
        return  moderatorBlocks.contains(Objects.requireNonNull(getLevel()).getBlockState(pos).getBlock());
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
                    if(y == 0 || x == 0 || z == 0 || y == getHeight()-1 || x == getWidth()-1 || z == getDepth()-1) {
                        if (!isFissionCasing(getSidePos(x - leftCasing).above(y - bottomCasing).relative(getFacing(), -z))) {
                            return new ValidationResult(
                                    false,
                                    "fission.casing.wrong.block",
                                    getSidePos(x - leftCasing).above(y - bottomCasing).relative(getFacing(), -z)
                            );
                        }
                    }
                }
            }
        }
        return new ValidationResult(true);
    }

    public boolean isFissionCasing(BlockPos pos)
    {
        if(casingBlocks.isEmpty()) {
            casingBlocks = getBlocksByTagKey(FissionBlocks.CASING_BLOCKS.location().toString());
        }
       return  casingBlocks.contains(Objects.requireNonNull(getLevel()).getBlockState(pos).getBlock());
    }

    protected List<Block> casingBlocks = new ArrayList<>();

    private List<Block> getBlocksByTagKey(String key)
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
            case 0 -> getBlockPos().east(i);
            case 1 -> getBlockPos().north(i);
            case 2 -> getBlockPos().west(i);
            case 3 -> getBlockPos().south(i);
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

    public int getDepletionProgress() {
        return 0;
    }
}
