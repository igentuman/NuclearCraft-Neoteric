package igentuman.nc.screen;

import net.minecraft.client.renderer.RenderPipelines;
import igentuman.nc.container.UniversalProcessorContainer;
import igentuman.nc.handler.SlotModePair;
import igentuman.nc.screen.element.SideConfigButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.util.TextUtils.__;

/** Popup screen laying out six directional buttons to configure a single slot's I/O mode per face. */
public class SideConfigScreen extends Screen {

    private static final Identifier TEXTURE = rl("textures/gui/small_window.png");

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
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, winX, winY, 0, 0, WIN_W, WIN_H, 256, 256);
        FormattedCharSequence formattedcharsequence = this.title.getVisualOrderText();
        guiGraphics.text(font, formattedcharsequence, winX + WIN_W / 2 - font.width(formattedcharsequence) / 2 - 6, winY + 6, 0xFF404040, false);
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
