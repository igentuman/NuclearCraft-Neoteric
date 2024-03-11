package igentuman.nc.handler.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.MinecraftForge;

import java.util.UUID;

public class CommandNcPlayerRadiation {

    private CommandNcPlayerRadiation() {}

    public static LiteralArgumentBuilder<CommandSource> register() {
        MinecraftForge.EVENT_BUS.register(CommandNcPlayerRadiation.class);
        return Commands.literal("nc_player_radiation")
                .executes(ctx -> {
            return execute(ctx.getSource());
        });
    }

    public static int execute(CommandSource ctx) throws CommandSyntaxException {
        ServerPlayerEntity pl = ctx.getPlayerOrException();
        pl.sendMessage(new TranslationTextComponent("nc.message.player_radiation"), UUID.randomUUID());
        return 0;
    }
}
