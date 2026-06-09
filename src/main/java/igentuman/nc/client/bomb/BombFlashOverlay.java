package igentuman.nc.client.bomb;

import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class BombFlashOverlay {

    public static final IGuiOverlay BOMB_FLASH = (gui, graphics, partialTick, width, height) -> {
        float alpha = BombFxManager.flashAlpha(partialTick);
        if (alpha <= 0.01f) return;
        int a = (int) (Math.min(1f, alpha) * 255f);
        int color = (a << 24) | 0xFFFFFF;
        graphics.fill(0, 0, width, height, color);
    };
}
