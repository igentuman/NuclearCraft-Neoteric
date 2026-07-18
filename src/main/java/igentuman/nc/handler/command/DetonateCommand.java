package igentuman.nc.handler.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import igentuman.nc.item.MultitoolItem;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import static igentuman.nc.util.TextUtils.__;

public class DetonateCommand {

    private DetonateCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> command) {
        command.register(Commands.literal("nc_detonate")
                .requires(cs -> cs.hasPermission(3))
                .executes(DetonateCommand::execute)
        );
    }

    public static int execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        if (!player.hasPermissions(3)) {
            player.sendSystemMessage(__("commands.nuclearcraft.no_permission"));
            return 0;
        }
        if (!(player.level instanceof ServerLevel serverLevel)) {
            return 0;
        }

        int detonated = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!(stack.getItem() instanceof MultitoolItem)) {
                continue;
            }
            CompoundTag tag = stack.getTag();
            if (tag == null) {
                continue;
            }
            boolean hasBomb = tag.getBoolean("has_bomb");
            boolean hasTnt = tag.getBoolean("has_tnt");
            if (!hasBomb && !hasTnt) {
                continue;
            }
            BlockPos pos = new BlockPos(tag.getInt("bomb_x"), tag.getInt("bomb_y"), tag.getInt("bomb_z"));
            MultitoolItem.scheduleBombTask(serverLevel, pos, hasTnt);
            tag.remove("last_armed_time");
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
