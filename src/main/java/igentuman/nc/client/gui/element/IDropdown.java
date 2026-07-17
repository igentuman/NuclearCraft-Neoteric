package igentuman.nc.client.gui.element;

import net.minecraft.client.gui.GuiGraphics;

public interface IDropdown {
    boolean isOpen();
    void close();
    void drawOverlay(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks);
}
