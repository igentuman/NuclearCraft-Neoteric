package igentuman.nc.handler.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import igentuman.nc.client.RuntimeFuelModelGenerator;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Command to regenerate fuel models at runtime
 * Usage: /nc_fuel_models
 */
public class FuelModelsCommand {

    private FuelModelsCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> command) {
        command.register(Commands.literal("nc_fuel_models")
                .requires(cs -> cs.hasPermission(2))
                .executes(FuelModelsCommand::execute)
        );
    }

    public static int execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        
        if (!player.hasPermissions(2)) {
            player.sendSystemMessage(Component.literal("You don't have permission to use this command")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
        
        player.sendSystemMessage(Component.literal("Regenerating fuel models...")
                .withStyle(ChatFormatting.GOLD));
        
        try {
            // Run on client thread
            player.getServer().execute(() -> {
                RuntimeFuelModelGenerator.generateResources();
                
                player.sendSystemMessage(Component.literal("✓ Fuel models regenerated successfully!")
                        .withStyle(ChatFormatting.GREEN));
                player.sendSystemMessage(Component.literal("Press F3+T to reload resources")
                        .withStyle(ChatFormatting.YELLOW));
                player.sendSystemMessage(Component.literal(RuntimeFuelModelGenerator.getGenerationStats())
                        .withStyle(ChatFormatting.GRAY));
            });
            
            return 1;
        } catch (Exception e) {
            player.sendSystemMessage(Component.literal("✗ Failed to regenerate models: " + e.getMessage())
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
    }
}