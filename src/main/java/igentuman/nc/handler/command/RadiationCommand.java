package igentuman.nc.handler.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import igentuman.nc.radiation.data.RadiationManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import static igentuman.nc.handler.config.RadiationConfig.RADIATION_CONFIG;
import static igentuman.nc.radiation.data.PlayerRadiationProvider.PLAYER_RADIATION;
import static igentuman.nc.util.TextUtils.__;

public class RadiationCommand {

    public static void register(CommandDispatcher<CommandSourceStack> command) {
        command.register(Commands.literal("nc_radiation")
                .then(Commands.argument("action", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            builder.suggest("disable");
                            builder.suggest("enable");
                            builder.suggest("clear_all");
                            builder.suggest("clear_player");
                            return builder.buildFuture();
                        })
                        .requires(cs -> cs.hasPermission(3))
                        .executes(RadiationCommand::executeCommand)
                        .then(Commands.argument("player", EntityArgument.player())
                                .requires(cs -> cs.hasPermission(3))
                                .executes(RadiationCommand::executeCommandWithPlayer)
                        )
                )
        );
    }

    private static int executeCommandWithPlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer executor = context.getSource().getPlayerOrException();
        if (!executor.hasPermissions(3)) {
            executor.sendSystemMessage(__("commands.nuclearcraft.no_permission"));
            return 1;
        }
        
        String action = StringArgumentType.getString(context, "action");
        
        if ("clear_player".equals(action)) {
            ServerPlayer targetPlayer = EntityArgument.getPlayer(context, "player");
            targetPlayer.getCapability(PLAYER_RADIATION).ifPresent(playerRadiation -> {
                playerRadiation.setRadiation(0);
            });
            executor.sendSystemMessage(Component.literal("Cleared player radiation " + targetPlayer.getName().getString()));
            return 1;
        } else {
            return executeCommand(context);
        }
    }

    private static int executeCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        if (!player.hasPermissions(3)) {
            player.sendSystemMessage(__("commands.nuclearcraft.no_permission"));
            return 1;
        }
        String action = StringArgumentType.getString(context, "action");

        switch (action) {
            case "disable":
                RADIATION_CONFIG.ENABLED.set(false);
                player.sendSystemMessage(Component.literal("Radiation disabled!"));
                break;
            case "enable":
                RADIATION_CONFIG.ENABLED.set(true);
                player.sendSystemMessage(Component.literal("Radiation enabled!"));
                break;
            case "clear_all":
                RadiationManager.get(player.level()).clear(player.level());
                player.sendSystemMessage(Component.literal("Radiation cleared!"));
                break;
            default:
                context.getSource().sendFailure(Component.literal("Invalid action: " + action));
                return 0;
        }

        return 1;
    }
}
