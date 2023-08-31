package igentuman.nc.radiation.client;

import com.mojang.blaze3d.systems.RenderSystem;
import igentuman.nc.client.NcClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.util.Random;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.setup.registration.NCItems.ALL_NC_ITEMS;

public class WhiteNoiseOverlay {

    private static final ResourceLocation NOISE = new ResourceLocation(MODID,
            "textures/gui/overlay/white_noise.png");

    public static final IGuiOverlay WHITE_NOISE = (gui, poseStack, partialTicks, width, height) -> {
        Player pl = NcClient.tryGetClientPlayer();
        if (pl == null) return;
        int radiation = ClientRadiationData.getCurrentWorldRadiation();
        int level = radiation/100000;
        if(level < 5) return;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, NOISE);
        assert Minecraft.getInstance().level != null;
        RandomSource rand = Minecraft.getInstance().level.random;
        for(int i = 0; i < rand.nextInt(level); i++) {
            int x1 = rand.nextInt(width);
            int y1 = rand.nextInt(height);
            int w = rand.nextInt(10);
            int h = rand.nextInt(10);
            GuiComponent.blit(poseStack, x1, y1, w, h,1,1,12,12);
        }
    };
}