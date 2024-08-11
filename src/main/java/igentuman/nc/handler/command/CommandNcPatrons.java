package igentuman.nc.handler.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import igentuman.nc.radiation.data.PlayerRadiationProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;

public class CommandNcPatrons {

    private CommandNcPatrons() {}

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        MinecraftForge.EVENT_BUS.register(CommandNcPlayerRadiation.class);
        return Commands.literal("nc_patrons")
                .executes(ctx -> {
            return execute(ctx.getSource());
        });
    }

    public static int execute(CommandSourceStack ctx) {
        ServerPlayer pl = ctx.getPlayer();
        pl.sendSystemMessage(Component.translatable("nc.message.patrons"));
        return 0;
    }
}
