package igentuman.nc.handler.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import igentuman.nc.radiation.data.RadiationManager;
import igentuman.nc.setup.registration.WorldGeneration;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import static igentuman.nc.handler.config.RadiationConfig.RADIATION_CONFIG;
import static igentuman.nc.util.TextUtils.__;

public class NCRadiationCommand {

    public static void register(CommandDispatcher<CommandSourceStack> command) {
        command.register(Commands.literal("nc_radiation")
                .then(Commands.argument("action", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            builder.suggest("disable");
                            builder.suggest("enable");
                            builder.suggest("clear_all");
                            builder.suggest("clear_chunk");
                            return builder.buildFuture();
                        })
                        .requires(cs -> cs.hasPermission(3))
                        .executes(NCRadiationCommand::executeCommand)
                )
        );
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
            case "clear_chunk":
                RadiationManager.get(player.level()).clearChunk(player.blockPosition().getX() >> 4, player.blockPosition().getZ() >> 4);
                player.sendSystemMessage(Component.literal("Radiation chunk cleared!"));
                break;
            default:
                context.getSource().sendFailure(Component.literal("Invalid action: " + action));
                return 0; // Command failed
        }

        return 1; // Command succeeded
    }
}
