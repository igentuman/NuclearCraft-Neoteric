package igentuman.nc.handler.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;

import static igentuman.nc.util.TextUtils.__;

public class PatronsCommand {

    private PatronsCommand() {}

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        MinecraftForge.EVENT_BUS.register(PatronsCommand.class);
        return Commands.literal("nc_patrons")
                .executes(ctx -> {
            return execute(ctx.getSource());
        });
    }

    public static int execute(CommandSourceStack ctx) {
        ServerPlayer pl = ctx.getPlayer();
        pl.sendSystemMessage(__("nc.message.patrons"));
        return 0;
    }
}
