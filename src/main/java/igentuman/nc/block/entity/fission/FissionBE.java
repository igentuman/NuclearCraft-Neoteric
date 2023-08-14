package igentuman.nc.block.entity.fission;

import igentuman.nc.block.entity.NuclearCraftBE;
import igentuman.nc.multiblock.fission.FissionReactorMultiblock;
import igentuman.nc.setup.multiblocks.FissionReactor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static igentuman.nc.NuclearCraft.MODID;

public class FissionBE extends NuclearCraftBE {
    public FissionReactorMultiblock multiblock() {
        return multiblock;
    }

    public void setMultiblock(FissionReactorMultiblock multiblock) {
        this.multiblock = multiblock;
    }

    protected FissionReactorMultiblock multiblock;

    public static String NAME;
    public boolean refreshCacheFlag = true;
    public boolean attachedToFuelCell = false;

    public byte validationRuns = 0;

    public FissionControllerBE controller;

    public FissionBE(BlockPos pPos, BlockState pBlockState, String name) {
        super(FissionReactor.MULTIBLOCK_BE.get(name).get(), pPos, pBlockState);
    }

    public void invalidateCache()
    {
        refreshCacheFlag = true;
        validationRuns = 0;
    }

    public void tickClient() {
    }

    public void tickServer() {
    }

    public boolean isAttachedToFuelCell() {
        if(refreshCacheFlag || validationRuns < 2) {
            validationRuns++;
            if(refreshCacheFlag) {
                attachedToFuelCell = false;
                validationRuns = 0;
            }
            for (Direction dir : Direction.values()) {
                BlockEntity be = getLevel().getBlockEntity(getBlockPos().relative(dir));
                if (be instanceof FissionFuelCellBE) {
                    attachedToFuelCell = true;
                    break;
                }
                if(be instanceof FissionBE) {
                    boolean attached = (refreshCacheFlag ? ((FissionBE) be).isAttachedToFuelCell() : ((FissionBE) be).attachedToFuelCell)
                            || attachedToFuelCell;
                    if(attached) {
                        attachedToFuelCell = true;
                        break;
                    }
                }
            }
        }
        return attachedToFuelCell;
    }

    public boolean isModeratorValid(BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockEntity be = getLevel().getBlockEntity(getBlockPos().relative(dir));
            if (be instanceof FissionFuelCellBE) {
                return true;
            }
        }
        return false;
    }

    public void onNeighborChange(BlockState state, BlockPos pos, BlockPos neighbor) {
        if(multiblock() != null) {
            multiblock().onNeighborChange(state, pos, neighbor);
        }
    }
}
