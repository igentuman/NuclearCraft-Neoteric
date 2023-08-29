package igentuman.nc.handler.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import igentuman.nc.recipes.type.OreVeinRecipe;
import igentuman.nc.util.insitu_leaching.WorldVeinsManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;

public class CommandNcVeinCheck {

    private CommandNcVeinCheck() {}

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        MinecraftForge.EVENT_BUS.register(CommandNcVeinCheck.class);
        return Commands.literal("nc_vein_check")
                .executes(ctx -> {
            return execute(ctx.getSource());
        });
    }

    public static int execute(CommandSourceStack ctx) {
        ServerPlayer pl = ctx.getPlayer();
        ServerLevel level = pl.getLevel();
        int qty = WorldVeinsManager.get(level).getWorldVeinData(level).getBlocksLeft(pl.chunkPosition().x, pl.chunkPosition().z);
        OreVeinRecipe vein = WorldVeinsManager.get(level).getWorldVeinData(level).getVeinForChunk(pl.chunkPosition().x, pl.chunkPosition().z);
        String name = "none";
        if(vein != null) {
            name = vein.getId().getPath().replace("nc_ore_veins/", "");
        }
        pl.sendSystemMessage(Component.translatable("nc.ore_vein."+name));
        pl.sendSystemMessage(Component.translatable("amount", qty));
        return 0;
    }
}
