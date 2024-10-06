package igentuman.nc.handler.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import igentuman.nc.setup.registration.WorldGeneration;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

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
                        .executes(NCRadiationCommand::executeCommand)
                )
        );
    }

    private static int executeCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();

        String structure = StringArgumentType.getString(context, "structure");

        switch (structure) {
            case "fission_reactor":
                placeFissionReactor(player);
                break;
            case "fusion_reactor":
                placeFusionReactor(player);
                break;
            default:
                context.getSource().sendFailure(Component.literal("Invalid structure: " + structure));
                return 0; // Command failed
        }

        return 1; // Command succeeded
    }

    // Handle placing the fission reactor
    private static void placeFissionReactor(ServerPlayer player) {
        double rayTraceRange = 30.0D;

        HitResult hitResult = player.pick(rayTraceRange, 0.0F, false);

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult) hitResult;
            BlockPos blockPos = blockHitResult.getBlockPos().offset(-3, 1, -3);
            WorldGeneration.StructurePlacer.placeStructure((ServerLevel) player.level(), blockPos, "fission_reactor");
            player.sendSystemMessage(Component.literal("Placing fission reactor!"));
        } else {
            player.sendSystemMessage(Component.literal("No block targeted!"));
        }

    }

    // Handle placing the fusion reactor
    private static void placeFusionReactor(ServerPlayer player) {
        double rayTraceRange = 30.0D;
        HitResult hitResult = player.pick(rayTraceRange, 0.0F, false);
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult) hitResult;
            BlockPos blockPos = blockHitResult.getBlockPos().offset(-5, 3, -5);;
            WorldGeneration.StructurePlacer.placeStructure((ServerLevel) player.level(), blockPos, "fusion_reactor");
            player.sendSystemMessage(Component.literal("Placing fusion reactor!"));
        } else {
            player.sendSystemMessage(Component.literal("No block targeted!"));
        }
    }
}
