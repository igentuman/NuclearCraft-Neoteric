package igentuman.nc.handler.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import igentuman.nc.recipes.type.OreVeinRecipe;
import igentuman.nc.util.insitu_leaching.WorldVeinsManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;

import static igentuman.nc.handler.config.CommonConfig.MISC_CONFIG;
import static igentuman.nc.util.TextUtils.__;

public class CommandNCDebug {

    private CommandNCDebug() {}

    public static void register(CommandDispatcher<CommandSourceStack> command) {
        command.register(Commands.literal("nc_debug")
                .then(Commands.argument("action", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            builder.suggest("disable");
                            builder.suggest("enable");
                            return builder.buildFuture();
                        })
                        .requires(cs -> cs.hasPermission(3))
                        .executes(CommandNCDebug::execute)
                )
        );
    }

    public static int execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        if (!player.hasPermissions(3)) {
            player.sendSystemMessage(__("commands.nuclearcraft.no_permission"));
            return 0;
        }
        String action = StringArgumentType.getString(ctx, "action");
        if (!action.equals("enable") && !action.equals("disable")) {
            player.sendSystemMessage(__("commands.nuclearcraft.invalid_action"));
            return 0;
        }
        MISC_CONFIG.DEBUG_LOG.set(action.equals("enable"));
        player.sendSystemMessage(__("message.nc.debug_logging."+action));
        return 1;
    }
}
