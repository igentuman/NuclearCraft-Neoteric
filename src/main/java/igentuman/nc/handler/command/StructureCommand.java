package igentuman.nc.handler.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
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

import static igentuman.nc.util.TextUtils.__;

public class StructureCommand  {

    public static void register(CommandDispatcher<CommandSourceStack> command) {
        command.register(Commands.literal("nc_build")
                .then(Commands.argument("structure", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            builder.suggest("fission_reactor");
                            builder.suggest("fusion_reactor");
                            builder.suggest("kugelblitz_chamber");
                            builder.suggest("turbine");
                            return builder.buildFuture();
                        })
                        .requires(cs -> cs.hasPermission(3))
                        .executes(StructureCommand::executeCommand)
                )
        );
    }

    private static int executeCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        if (!player.hasPermissions(3)) {
            player.sendSystemMessage(__("commands.nuclearcraft.no_permission"));
            return 1;
        }
        String structure = StringArgumentType.getString(context, "structure");

        switch (structure) {
            case "fission_reactor" -> placeFissionReactor(player);
            case "fusion_reactor" -> placeFusionReactor(player);
            case "kugelblitz_chamber" -> placeKugelblitzChamber(player);
            case "turbine" -> placeTurbine(player);
            default -> context.getSource().sendFailure(Component.literal("Invalid structure: " + structure));
        }

        return 1; // Command succeeded
    }

    private static void placeKugelblitzChamber(ServerPlayer player) {
        double rayTraceRange = 30.0D;
        HitResult hitResult = player.pick(rayTraceRange, 0.0F, false);
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult) hitResult;
            BlockPos blockPos = blockHitResult.getBlockPos().offset(-3, 6, -3);
            WorldGeneration.StructurePlacer.placeStructure((ServerLevel) player.level(), blockPos, "kugelblitz_chamber");
            player.sendSystemMessage(Component.literal("Placing chamber!"));
        } else {
            player.sendSystemMessage(Component.literal("No block targeted!"));
        }
    }

    private static void placeTurbine(ServerPlayer player) {
        double rayTraceRange = 30.0D;
        HitResult hitResult = player.pick(rayTraceRange, 0.0F, false);
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult) hitResult;
            BlockPos blockPos = blockHitResult.getBlockPos().offset(-3, 1, -3);
            WorldGeneration.StructurePlacer.placeStructure((ServerLevel) player.level(), blockPos, "turbine");
            player.sendSystemMessage(Component.literal("Placing turbine!"));
        } else {
            player.sendSystemMessage(Component.literal("No block targeted!"));
        }
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
