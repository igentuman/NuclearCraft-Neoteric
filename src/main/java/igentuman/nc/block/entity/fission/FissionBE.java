package igentuman.nc.block.entity.fission;

import igentuman.nc.block.entity.NuclearCraftBE;
import igentuman.nc.setup.multiblocks.FissionReactor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class FissionBE extends NuclearCraftBE {

    protected String name;
    public static String NAME;
    public boolean attachedToFuelCell = false;
    public FissionControllerBE controller;

    public FissionBE(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public FissionBE(BlockPos pPos, BlockState pBlockState, String name) {
        super(FissionReactor.MULTIBLOCK_BE.get(name).get(), pPos, pBlockState);
    }

    public void tickClient() {
    }

    public void tickServer() {
        //level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    public boolean isAttachedToFuelCell() {
        if(!attachedToFuelCell) { //todo optimize
            for (Direction dir : Direction.values()) {
                BlockEntity be = getLevel().getBlockEntity(getBlockPos().relative(dir));
                if (be instanceof FissionFuelCellBE) {
                    attachedToFuelCell = true;
                }
            }
        }
        return attachedToFuelCell;
    }

    public String getName() {
        return name;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
    }


    @Override
    public void load(CompoundTag tag) {
        if (tag.contains("Info")) {

        }
        super.load(tag);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        CompoundTag infoTag = new CompoundTag();
        tag.put("Info", infoTag);
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
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        handleUpdateTag(tag);
    }
}
