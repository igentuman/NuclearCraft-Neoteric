package igentuman.nc.client.gui.element;

import com.mojang.blaze3d.vertex.PoseStack;

public interface IDropdown {
    boolean isOpen();
    void close();
    void drawOverlay(PoseStack graphics, int mouseX, int mouseY, float partialTicks);
}
