package igentuman.nc.client.gui.fission.designer;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public final class DesignGridRenderer {

    private DesignGridRenderer() {
    }

    public static void drawLayer(GuiGraphicsExtractor graphics, DesignGrid grid, int layer, int x, int y, int cell) {
        graphics.fill(x - 1, y - 1, x + cell * grid.sizeX + 1, y + cell * grid.sizeZ + 1, 0xCC3A3A3A);
        for (int cx = 0; cx < grid.sizeX; cx++) {
            for (int cz = 0; cz < grid.sizeZ; cz++) {
                int px = x + cx * cell;
                int py = y + cz * cell;
                graphics.fill(px, py, px + cell - 1, py + cell - 1, 0xCC13545d);
                Block block = grid.get(cx, layer, cz);
                if (block != null) {
                    float scale = Math.min(1f, cell / 16f);
                    graphics.pose().pushMatrix();
                    graphics.pose().translate(px + (cell - 16 * scale) / 2f, py + (cell - 16 * scale) / 2f);
                    graphics.pose().scale(scale, scale);
                    graphics.item(new ItemStack(block), 0, 0);
                    graphics.pose().popMatrix();
                }
                if (grid.invalidCells.contains(new BlockPos(cx, layer, cz))) {
                    graphics.outline(px, py, cell - 1, cell - 1, 0xCCFF3030);
                }
            }
        }
    }
}
