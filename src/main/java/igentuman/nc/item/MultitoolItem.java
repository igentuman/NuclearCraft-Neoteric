package igentuman.nc.item;

import igentuman.nc.block_entity.bomb.Pu239BombBE;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import static igentuman.nc.setup.entries.Bomb.TICKET_CONTROLLER;
import static igentuman.nc.util.TextUtils.__;

public class MultitoolItem extends Item {

    public MultitoolItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, net.minecraft.world.level.LevelReader world, BlockPos pos, Player player) {
        return true;
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (player == null) {
            return super.useOn(context);
        }
        BlockPos pos = context.getClickedPos();

        if (level.getBlockState(pos).is(Blocks.TNT)) {
            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            }
            writeLink(context.getItemInHand(), pos, false, true);
            player.sendSystemMessage(__("message.nc.multitool.connected_to_tnt", pos.getX(), pos.getY(), pos.getZ()));
            return InteractionResult.SUCCESS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof Pu239BombBE bomb) {
            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            }
            ItemStack stack = context.getItemInHand();
            if (player.getUUID().toString().equals(bomb.placerUuid)) {
                writeLink(stack, pos, true, false);
                player.sendSystemMessage(__("message.nc.multitool.connected_to_bomb", pos.getX(), pos.getY(), pos.getZ()));
            } else {
                player.sendSystemMessage(__("message.nc.multitool.not_your_project"));
            }
            return InteractionResult.SUCCESS;
        }

        if (level.isClientSide()) {
            return super.useOn(context);
        }
        return super.useOn(context);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        CompoundTag tag = readCustomData(stack);
        if (tag != null && (tag.getBooleanOr("has_bomb", false) || tag.getBooleanOr("has_tnt", false))) {
            if (world.isClientSide()) {
                return InteractionResult.SUCCESS;
            }
            int bombX = tag.getIntOr("bomb_x", 0);
            int bombY = tag.getIntOr("bomb_y", 0);
            int bombZ = tag.getIntOr("bomb_z", 0);
            boolean isTnt = tag.getBooleanOr("has_tnt", false);
            long gameTime = world.getGameTime();
            long lastArmedTime = tag.getLongOr("last_armed_time", 0L);

            if (gameTime - lastArmedTime <= 60 && gameTime >= lastArmedTime) {
                if (isTnt) {
                    player.sendSystemMessage(__("message.nc.multitool.tnt_detonated", bombX, bombY, bombZ));
                } else {
                    player.sendSystemMessage(__("message.nc.multitool.bomb_detonated", bombX, bombY, bombZ));
                }
                if (world instanceof ServerLevel serverLevel) {
                    scheduleBombTask(serverLevel, new BlockPos(bombX, bombY, bombZ), isTnt);
                }
                writeCustomData(stack, compoundTag -> compoundTag.remove("last_armed_time"));
            } else {
                player.sendSystemMessage(__("message.nc.multitool.armed_confirm"));
                writeCustomData(stack, compoundTag -> compoundTag.putLong("last_armed_time", gameTime));
            }
            return InteractionResult.SUCCESS;
        }
        return super.use(world, player, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, @NotNull TooltipContext context, TooltipDisplay display, Consumer<Component> list, TooltipFlag flag) {
        list.accept(__("tooltip.nc.multitool.desc").withStyle(ChatFormatting.YELLOW));
        list.accept(__("tooltip.nc.multitool.shift.desc").withStyle(ChatFormatting.YELLOW));
        CompoundTag tag = readCustomData(stack);
        if (tag != null && tag.getBooleanOr("has_bomb", false)) {
            list.accept(__("tooltip.nc.multitool.connected_to_bomb",
                    tag.getIntOr("bomb_x", 0), tag.getIntOr("bomb_y", 0), tag.getIntOr("bomb_z", 0))
                    .withStyle(ChatFormatting.RED));
        }
        if (tag != null && tag.getBooleanOr("has_tnt", false)) {
            list.accept(__("tooltip.nc.multitool.connected_to_tnt",
                    tag.getIntOr("bomb_x", 0), tag.getIntOr("bomb_y", 0), tag.getIntOr("bomb_z", 0))
                    .withStyle(ChatFormatting.RED));
        }
    }

    /**
     * Stamped onto the linked coords slot. A stack with this flag set runs the use() handler
     * instead of falling through to super.
     */
    public static boolean isLinked(ItemStack stack) {
        CompoundTag tag = readCustomData(stack);
        return tag != null && (tag.getBooleanOr("has_bomb", false) || tag.getBooleanOr("has_tnt", false));
    }

    /** Read-only view of the multitool's CUSTOM_DATA tag (or {@code null} if unset). */
    public static @Nullable CompoundTag readLinkTag(ItemStack stack) {
        return readCustomData(stack);
    }

    /** Strip just the two-press arm timestamp. Used by {@code /nc_detonate} after queueing. */
    public static void clearLastArmedTime(ItemStack stack) {
        writeCustomData(stack, tag -> tag.remove("last_armed_time"));
    }

    // -------- NBT helpers --------

    private static void writeLink(ItemStack stack, BlockPos pos, boolean hasBomb, boolean hasTnt) {
        writeCustomData(stack, tag -> {
            tag.putInt("bomb_x", pos.getX());
            tag.putInt("bomb_y", pos.getY());
            tag.putInt("bomb_z", pos.getZ());
            tag.putBoolean("has_bomb", hasBomb);
            tag.putBoolean("has_tnt", hasTnt);
        });
    }

    private static void writeCustomData(ItemStack stack, java.util.function.Consumer<CompoundTag> mutator) {
        CompoundTag tag = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        mutator.accept(tag);
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private static @Nullable CompoundTag readCustomData(ItemStack stack) {
        CustomData data = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        return data == null ? null : data.copyTag();
    }

    // -------- BombTask queue --------

    private static class BombTask {
        final ServerLevel level;
        final BlockPos pos;
        int ticksLeft;
        int stage;
        final boolean isTnt;

        BombTask(ServerLevel level, BlockPos pos, int ticksLeft, int stage, boolean isTnt) {
            this.level = level;
            this.pos = pos;
            this.ticksLeft = ticksLeft;
            this.stage = stage;
            this.isTnt = isTnt;
        }
    }

    private static final List<BombTask> BOMB_TASKS = new ArrayList<>();

    /** Schedule a remote-triggered TNT or Pu-239 bomb detonation. Server-side. */
    public static void scheduleBombTask(ServerLevel level, BlockPos pos, boolean isTnt) {
        synchronized (BOMB_TASKS) {
            BOMB_TASKS.add(new BombTask(level, pos, 0, 0, isTnt));
        }
    }

    public static void tickTasks() {
        List<BombTask> ready = new ArrayList<>();
        synchronized (BOMB_TASKS) {
            Iterator<BombTask> it = BOMB_TASKS.iterator();
            while (it.hasNext()) {
                BombTask task = it.next();
                if (task.ticksLeft <= 0) {
                    ready.add(task);
                    it.remove();
                } else {
                    task.ticksLeft--;
                }
            }
        }

        for (BombTask task : ready) {
            if (task.stage == 0) {
                net.minecraft.world.level.ChunkPos cp = net.minecraft.world.level.ChunkPos.containing(task.pos);
                try {
                    TICKET_CONTROLLER.forceChunk(task.level, task.pos, cp.x(), cp.z(), true, true);
                } catch (IllegalArgumentException e) {
                    // Controller not registered - proceed anyway; the bomb block itself enforces chunk loading.
                }
                synchronized (BOMB_TASKS) {
                    BOMB_TASKS.add(new BombTask(task.level, task.pos, 1, 1, task.isTnt));
                }
            } else if (task.stage == 1) {
                if (task.isTnt) {
                    BlockState tnt = task.level.getBlockState(task.pos);
                    if (tnt.is(Blocks.TNT)) {
                        if (tnt.onCaughtFire(task.level, task.pos, null, null)) {
                            task.level.setBlock(task.pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                        }
                    }
                } else {
                    BlockEntity be = task.level.getBlockEntity(task.pos);
                    if (be instanceof Pu239BombBE bomb) {
                        bomb.arm();
                    }
                }
            }
        }
    }
}
