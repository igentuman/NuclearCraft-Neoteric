package igentuman.nc.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;

public final class GuiFluidRenderer {
    private GuiFluidRenderer() {}

    public static void renderFluidTank(GuiGraphics graphics, int x, int y, int width, int height,
                                       FluidStack fluid, int capacity) {
        if (fluid.isEmpty() || capacity <= 0) return;
        var extensions = IClientFluidTypeExtensions.of(fluid.getFluid());
        var texture = extensions.getStillTexture(fluid);
        if (texture == null) return;
        var sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(texture);
        int color = extensions.getTintColor(fluid);
        int filled = Math.min(height, Math.max(1, (int) ((long) height * fluid.getAmount() / capacity)));
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(((color >> 16) & 255) / 255F, ((color >> 8) & 255) / 255F,
                (color & 255) / 255F, ((color >> 24) & 255) / 255F);
        for (int dx = 0; dx < width; dx += 16) {
            for (int dy = 0; dy < filled; dy += 16) {
                graphics.blit(x + dx, y + height - filled + dy, 0, Math.min(16, width - dx), Math.min(16, filled - dy), sprite);
            }
        }
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
    }
}
