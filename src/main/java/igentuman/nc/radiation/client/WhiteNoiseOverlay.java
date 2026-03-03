package igentuman.nc.radiation.client;

import com.mojang.blaze3d.systems.RenderSystem;
import igentuman.nc.client.NcClient;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

import static igentuman.nc.NuclearCraft.rl;

public class WhiteNoiseOverlay {

    private static final ResourceLocation NOISE = rl("textures/gui/overlay/white_noise.png");

    public static final LayeredDraw.Layer WHITE_NOISE = (graphics, deltaTracker) -> {
        if(true) return;
        Player pl = NcClient.tryGetClientPlayer();
        if (pl == null) return;
        int radiation = ClientRadiationData.getCurrentWorldRadiation();
        int level = radiation/100000;
        if(level < 5) return;
        Minecraft mc = Minecraft.getInstance();
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        assert mc.level != null;
        RandomSource rand = mc.level.random;
        for(int i = 0; i < rand.nextInt(level); i++) {
            int x1 = rand.nextInt(width);
            int y1 = rand.nextInt(height);
            int w = rand.nextInt(10);
            int h = rand.nextInt(10);
            graphics.blit(NOISE, x1, y1, w, h,1,1,12,12);
        }
    };
}
