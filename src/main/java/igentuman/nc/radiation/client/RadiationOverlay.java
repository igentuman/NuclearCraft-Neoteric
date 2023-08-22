package igentuman.nc.radiation.client;

import com.mojang.blaze3d.systems.RenderSystem;
import igentuman.nc.client.NcClient;
import igentuman.nc.radiation.data.PlayerRadiation;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.setup.registration.NCItems.ALL_NC_ITEMS;

public class RadiationOverlay {

    private static final ResourceLocation RADIATION_BAR_TEXTURE = new ResourceLocation(MODID,
            "textures/gui/overlay/radiation_bar.png");

    public static boolean hasDosimeter(Player player) {
        return player.getInventory().contains(new ItemStack(ALL_NC_ITEMS.get("dosimeter").get()));
    }

    public static final IGuiOverlay RADIATION_BAR = (gui, poseStack, partialTicks, width, height) -> {
        Player pl = NcClient.tryGetClientPlayer();
        if (pl == null) return;
        if(!hasDosimeter(pl)) return;
        int radiation = ClientRadiationData.getPlayerRadiation();
        String toDisplay = String.valueOf(radiation);
        int x = width / 2;
        int y = height;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, RADIATION_BAR_TEXTURE);
        GuiComponent.blit(poseStack,4, y - 15,0,0,94,11,256,256);
        int maxRadiationBar = PlayerRadiation.maxPlayerRadiation;
        int barWidth = (int) Math.min(128, radiation * 90 / maxRadiationBar);
        RenderSystem.setShaderTexture(0, RADIATION_BAR_TEXTURE);
        GuiComponent.blit(poseStack,6,y - 13,0,11, barWidth,8,256,256);
    };
}