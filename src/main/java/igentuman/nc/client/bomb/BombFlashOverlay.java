package igentuman.nc.client.bomb;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import org.jetbrains.annotations.NotNull;

public class BombFlashOverlay implements LayeredDraw.Layer {

    public static final BombFlashOverlay BOMB_FLASH = new BombFlashOverlay();

    @Override
    public void render(@NotNull GuiGraphics graphics, @NotNull DeltaTracker deltaTracker) {
        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);
        float alpha = BombFxManager.flashAlpha(partialTick);
        if (alpha <= 0.01f) return;
        int a = (int) (Math.min(1f, alpha) * 255f);
        int color = (a << 24) | 0xFFFFFF;
        graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), color);
    }
}
