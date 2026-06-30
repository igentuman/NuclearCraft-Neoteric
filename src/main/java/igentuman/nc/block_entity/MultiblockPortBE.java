package igentuman.nc.block_entity;

import igentuman.nc.container.MultiblockPortContainer;
import igentuman.nc.util.NBTField;
import igentuman.nc.util.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

/** Block entity for non-controller multiblock parts (ports). Stores its controller's position and
 *  proxies capabilities to it. Subclasses add multiblock-specific behavior. */
public class MultiblockPortBE extends GlobalBlockEntity implements MenuProvider {

    /** Constructs a port BE; lets registration pick a subclass while sharing the type/pos/state/name wiring. */
    @FunctionalInterface
    public interface Factory {
        MultiblockPortBE create(BlockEntityType<?> type, BlockPos pos, BlockState state, String name);
    }

    @Nullable
    @NBTField(syncToClient = true)
    public BlockPos controllerPos;

    public MultiblockPortBE(BlockEntityType<?> type, BlockPos pos, BlockState state, String name) {
        super(type, pos, state, name);
    }

    @Override
    public void serverTick() {
        // Ports proxy to the controller; skip GlobalBlockEntity recipe/content processing.
        if (wasChanged) {
            wasChanged = false;
            setChanged();
        }
    }

    public void setControllerPos(@Nullable BlockPos pos) {
        this.controllerPos = pos;
        markDirty();
    }

    @Nullable
    public BlockPos getControllerPos() {
        return controllerPos;
    }

    @Nullable
    public MultiblockControllerBE controller() {
        if (controllerPos == null || level == null) return null;
        BlockEntity be = level.getBlockEntity(controllerPos);
        return be instanceof MultiblockControllerBE c ? c : null;
    }

    private static final String[] NO_REDSTONE_MODES = new String[0];

    /** Mode-label keys this port exposes for the unified redstone switch button; empty = no button.
     *  Each key resolves to lang {@code message.nuclearcraft.redstone_mode.<key>}. Override per port. */
    public String[] redstoneModes() {
        return NO_REDSTONE_MODES;
    }

    /** Index of the currently selected redstone mode. */
    public int getRedstoneMode() {
        return 0;
    }

    /** Advances to the next redstone mode (server side); returns the new mode index. */
    public int cycleRedstoneMode() {
        return 0;
    }

    @Nullable
    @Override
    public IItemHandler getItemHandler(@Nullable Direction side) {
        MultiblockControllerBE c = controller();
        return c != null ? c.getItemHandler(side) : super.getItemHandler(side);
    }

    @Nullable
    @Override
    public IFluidHandler getFluidHandler(@Nullable Direction side) {
        MultiblockControllerBE c = controller();
        return c != null ? c.getFluidHandler(side) : super.getFluidHandler(side);
    }

    @Nullable
    @Override
    public IEnergyStorage getEnergyHandler(@Nullable Direction side) {
        MultiblockControllerBE c = controller();
        return c != null ? c.getEnergyHandler(side) : super.getEnergyHandler(side);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (controllerPos != null) tag.putLong("controllerPos", controllerPos.asLong());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("controllerPos")) controllerPos = BlockPos.of(tag.getLong("controllerPos"));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.nuclearcraft." + name);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new MultiblockPortContainer(containerId, playerInventory, this, containerData);
    }
}
