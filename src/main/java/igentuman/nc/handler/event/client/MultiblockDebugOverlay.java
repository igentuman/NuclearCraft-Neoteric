package igentuman.nc.handler.event.client;

import igentuman.nc.block.entity.MultiblockControllerBE;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class MultiblockDebugOverlay {

    public static final IGuiOverlay MULTIBLOCK_DEBUG = (gui, graphics, partialTick, width, height) -> {
        if (!BlockOverlayHandler.isDebugOverlayActive()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        HitResult hit = mc.hitResult;
        if (!(hit instanceof BlockHitResult bhr) || hit.getType() != HitResult.Type.BLOCK) return;
        Level world = mc.level;
        BlockEntity be = world.getBlockEntity(bhr.getBlockPos());
        if (!(be instanceof MultiblockControllerBE controller)) return;

        Font font = mc.font;
        String[] lines = new String[]{
                "topCasing: " + controller.topCasing,
                "bottomCasing: " + controller.bottomCasing,
                "leftCasing: " + controller.leftCasing,
                "rightCasing: " + controller.rightCasing
        };
        int x = 8;
        int y = height / 2 - (lines.length * 10) / 2;
        for (String line : lines) {
            graphics.drawString(font, line, x, y, 0xFFFFFF55, true);
            y += 10;
        }
    };
}
