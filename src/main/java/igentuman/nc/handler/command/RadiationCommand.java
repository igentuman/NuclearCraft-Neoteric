package igentuman.nc.handler.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import igentuman.nc.radiation.data.RadiationManager;
import igentuman.nc.setup.registration.NCAttachments;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import static igentuman.nc.handler.config.RadiationConfig.RADIATION_CONFIG;
import static igentuman.nc.util.TextUtils.__;

public class RadiationCommand {

    public static void register(CommandDispatcher<CommandSourceStack> command) {
        command.register(Commands.literal("nc_radiation")
                .then(Commands.argument("action", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            builder.suggest("disable");
                            builder.suggest("set");
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
                        .then(Commands.argument("value", new net.minecraft.commands.arguments.RangeArgument.Ints())
                                .executes(RadiationCommand::executeSetRadiation)
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
            targetPlayer.getData(NCAttachments.PLAYER_RADIATION.get()).setRadiation(0);
            executor.sendSystemMessage(Component.literal("Cleared player radiation " + targetPlayer.getName().getString()));
            return 1;
        }
        if ("set".equals(action)) {
            executeSetRadiation(context);
            return 1;
        }

        return executeCommand(context);
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

    private static int executeSetRadiation(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        if (!player.hasPermissions(3)) {
            player.sendSystemMessage(__("commands.nuclearcraft.no_permission"));
            return 1;
        }
        String action = StringArgumentType.getString(context, "action");
        if (!"set".equals(action)) {
            context.getSource().sendFailure(Component.literal("Invalid action for value argument: " + action));
            return 0;
        }
        int value = 0;
        try {
            value = Math.max(0, Integer.parseInt(context.getInput().split(" ")[2]));
        } catch (Exception ex) {
            context.getSource().sendFailure(Component.literal("Invalid action for value argument: " + action));
            return 0;
        }
        RadiationManager.get(player.level()).setChunkRadiation(player.blockPosition(), value);
        player.sendSystemMessage(Component.literal("Set your radiation to " + value));
        return 1;
    }
}
