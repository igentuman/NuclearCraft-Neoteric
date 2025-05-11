package igentuman.nc.container;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.kugelblitz.EXPLBE;
import igentuman.nc.network.toServer.PacketGuiButtonPress;
import igentuman.nc.network.toServer.PacketSliderChanged;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration.*;
import static igentuman.nc.util.TextUtils.__;

public class EXPLContainer extends AbstractContainerMenu {

    protected final EXPLBE blockEntity;
    protected final Player playerEntity;
    protected final String name = "expl";

    protected IItemHandler playerInventory;

    public EXPLContainer(int pContainerId, BlockPos pos, Inventory playerInventory) {
        super(EXPL_CONTAINER.get(), pContainerId);
        this.playerEntity = playerInventory.player;
        blockEntity = (EXPLBE) playerEntity.getCommandSenderWorld().getExistingBlockEntity(pos);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player pPlayer, int pIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player playerIn) {
        return stillValid(
                ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()),
                playerEntity,
                EXPL_BLOCK.get()
        ) || stillValid(
                ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()),
                playerEntity,
                EXPL_PROXY_BLOCK.get()
        );
    }

    public Component getTitle() {
        return __("block."+MODID+"."+name);
    }

    public BlockPos getBlockPos() {
        return blockEntity.getBlockPos();
    }

    public boolean isReady() {
        return hasEnoughEnergy() && !blockEntity.activated;
    }

    private boolean hasEnoughEnergy() {
        return blockEntity.hasEnoughEnergy();
    }

    public void burst() {
        NuclearCraft.packetHandler().sendToServer(new PacketGuiButtonPress(blockEntity.getBlockPos(), 77));
        //avoid spamming
        blockEntity.aggregatedEnergy = 0;
    }

    public long getCharge() {
        return blockEntity.aggregatedEnergy;
    }
}
