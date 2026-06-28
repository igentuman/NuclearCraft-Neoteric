package igentuman.nc.screen;

import igentuman.nc.container.UniversalProcessorContainer;
import igentuman.nc.handler.SlotModePair;
import igentuman.nc.screen.element.SideConfigButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;

import static igentuman.nc.Main.rl;
import static igentuman.nc.util.TextUtils.__;

public class SideConfigScreen extends Screen {

    private static final ResourceLocation TEXTURE = rl("textures/gui/small_window.png");

    private static final int WIN_W = 120;
    private static final int WIN_H = 100;
    private static final int BTN  = 16;
    private static final int GAP  = 18;

    private final AbstractContainerScreen<UniversalProcessorContainer> parentScreen;
    private final int slotId;

    private int winX;
    private int winY;

    public SideConfigScreen(AbstractContainerScreen<UniversalProcessorContainer> parentScreen, int slotId) {
        super(__("screen.nuclearcraft.side_config"));
        this.parentScreen = parentScreen;
        this.slotId = slotId;
    }

    @Override
    protected void init() {
        winX = (this.width - WIN_W) / 2;
        winY = (this.height - WIN_H) / 2;

        int cx = winX + WIN_W / 2 - BTN / 2 - 8;
        int cy = winY + WIN_H / 2 - BTN / 2 - 6;

        // RelativeDirection ordinals: FRONT=0, BACK=1, LEFT=2, RIGHT=3, UP=4, DOWN=5
        addRenderableWidget(new SideConfigButton(cx,        cy - GAP,  slotId, 4, this));   // UP
        addRenderableWidget(new SideConfigButton(cx - GAP,  cy,        slotId, 2, this));   // LEFT
        addRenderableWidget(new SideConfigButton(cx,        cy,        slotId, 0, this));  // FRONT
        addRenderableWidget(new SideConfigButton(cx + GAP,  cy,        slotId, 3, this));   // RIGHT
        addRenderableWidget(new SideConfigButton(cx,        cy + GAP,  slotId, 5, this));   // DOWN
        addRenderableWidget(new SideConfigButton(cx,        cy + GAP * 2, slotId, 1, this)); // BACK
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.blit(TEXTURE, winX, winY, 0, 0, WIN_W, WIN_H, 256, 256);
        guiGraphics.drawCenteredString(font, this.title, winX + WIN_W / 2, winY + 6, 0x404040);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parentScreen);
    }

    public SlotModePair.SlotMode getSlotMode(int direction, int slotId) {
        return parentScreen.getMenu().getSlotMode(direction, slotId);
    }

    public UniversalProcessorContainer getMenu() {
        return parentScreen.getMenu();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
