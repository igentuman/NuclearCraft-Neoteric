package igentuman.nc.handler.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import igentuman.nc.item.MultitoolItem;
import igentuman.nc.util.TextUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.item.ItemStack;

import static igentuman.nc.util.TextUtils.__;

public class DetonateCommand {

    private DetonateCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nc_detonate")
                .requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
                .executes(DetonateCommand::execute)
        );
    }

    public static int execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        if (!player.permissions().hasPermission(Permissions.COMMANDS_ADMIN)) {
            player.sendSystemMessage(TextUtils.applyFormat(
                    __("commands.nuclearcraft.no_permission"), net.minecraft.ChatFormatting.RED));
            return 0;
        }
        if (!(player.level() instanceof ServerLevel serverLevel)) return 0;

        int detonated = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!(stack.getItem() instanceof MultitoolItem)) continue;
            CompoundTag tag = MultitoolItem.readLinkTag(stack);
            if (tag == null) continue;
            boolean hasTnt = tag.getBooleanOr("has_tnt", false);
            boolean hasBomb = tag.getBooleanOr("has_bomb", false);
            if (!hasBomb && !hasTnt) continue;
            BlockPos pos = new BlockPos(tag.getIntOr("bomb_x", 0), tag.getIntOr("bomb_y", 0), tag.getIntOr("bomb_z", 0));
            MultitoolItem.scheduleBombTask(serverLevel, pos, hasTnt);
            MultitoolItem.clearLastArmedTime(stack);
            detonated++;
        }

        if (detonated == 0) {
            player.sendSystemMessage(__("commands.nuclearcraft.detonate.none"));
            return 0;
        }
        player.sendSystemMessage(__("commands.nuclearcraft.detonate.summary", detonated));
        return detonated;
    }
}
