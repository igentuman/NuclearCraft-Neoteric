package igentuman.nc.handler.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import igentuman.nc.recipes.type.OreVeinRecipe;
import igentuman.nc.util.insitu_leaching.WorldVeinsManager;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;

import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;

import java.util.UUID;

public class CommandNcVeinCheck {

    private CommandNcVeinCheck() {}

    public static LiteralArgumentBuilder<CommandSource> register() {
        MinecraftForge.EVENT_BUS.register(CommandNcVeinCheck.class);
        return Commands.literal("nc_vein_check")
                .executes(ctx -> {
            return execute(ctx.getSource());
        });
    }

    public static int execute(CommandSource ctx) throws CommandSyntaxException {
        ServerPlayerEntity pl = ctx.getPlayerOrException();
        World level = pl.getLevel();
        int qty = 0;
        OreVeinRecipe vein = WorldVeinsManager.get(level).getWorldVeinData(level).getVeinForChunk(pl.xChunk, pl.zChunk);
        String name = "none";
        if(vein != null) {
            name = vein.getId().getPath().replace("nc_ore_veins/", "");
            qty = WorldVeinsManager.get(level).getWorldVeinData(level).getBlocksLeft(pl.xChunk, pl.zChunk);
        }
        pl.sendMessage(new TranslationTextComponent("nc.ore_vein."+name), pl.getUUID());
        pl.sendMessage(new TranslationTextComponent("amount", qty), pl.getUUID());
        return 0;
    }
}
