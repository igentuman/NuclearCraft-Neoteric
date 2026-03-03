package igentuman.nc.radiation.client;

import com.mojang.blaze3d.systems.RenderSystem;
import igentuman.nc.client.NcClient;
import igentuman.nc.radiation.data.PlayerRadiation;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.setup.registration.NCItems.ALL_NC_ITEMS;

public class RadiationOverlay {

    private static final ResourceLocation RADIATION_BAR_TEXTURE = rl("textures/gui/overlay/radiation_bar.png");

    public static boolean hasDosimeter(Player player) {
        return player.getInventory().contains(new ItemStack(ALL_NC_ITEMS.get("dosimeter").get()));
    }

    public static final LayeredDraw.Layer RADIATION_BAR = (graphics, deltaTracker) -> {
        Player pl = NcClient.tryGetClientPlayer();
        if (pl == null) return;
        if(!hasDosimeter(pl)) return;
        ClientRadiationData.setCurrentChunk(pl.chunkPosition().x, pl.chunkPosition().z, pl.level());
        long radiation = ClientRadiationData.getPlayerRadiation();
        Minecraft mc = Minecraft.getInstance();
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        int y = height;

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.blit(RADIATION_BAR_TEXTURE,4, y - 15,0,0,94,11,256,256);
        long maxRadiationBar = PlayerRadiation.maxPlayerRadiation;
        int barWidth = (int) Math.min(128, radiation * 90 / maxRadiationBar);
        graphics.blit(RADIATION_BAR_TEXTURE,6,y - 13,0,11, barWidth,8,256,256);
    };
}
