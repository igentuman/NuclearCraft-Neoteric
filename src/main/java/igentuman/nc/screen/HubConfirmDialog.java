package igentuman.nc.screen;

import igentuman.nc.screen.element.NcPanel;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class HubConfirmDialog extends Screen {

    private static final int PANEL_W = 220;
    private static final int LINE_H = 10;

    private final Screen parent;
    private final Component message;
    private final boolean cancellable;
    private final boolean withNameInput;
    private final String defaultName;
    private final Runnable onConfirm;
    private final Consumer<String> onConfirmWithName;
    private EditBox nameInput;
    private int left;
    private int top;
    private int panelH;
    private int messageBlockH;

    private HubConfirmDialog(Screen parent, Component title, Component message, boolean cancellable,
                              boolean withNameInput, String defaultName,
                              Runnable onConfirm, Consumer<String> onConfirmWithName) {
        super(title);
        this.parent = parent;
        this.message = message;
        this.cancellable = cancellable;
        this.withNameInput = withNameInput;
        this.defaultName = defaultName;
        this.onConfirm = onConfirm;
        this.onConfirmWithName = onConfirmWithName;
    }

    public static HubConfirmDialog confirm(Screen parent, Component title, Component message, Runnable onYes) {
        return new HubConfirmDialog(parent, title, message, true, false, null, onYes, null);
    }

    public static HubConfirmDialog confirmWithName(Screen parent, Component title, Component message,
                                                     String defaultName, Consumer<String> onYes) {
        return new HubConfirmDialog(parent, title, message, true, true, defaultName, null, onYes);
    }

    public static HubConfirmDialog result(Screen parent, Component title, Component message) {
        return new HubConfirmDialog(parent, title, message, false, false, null, null, null);
    }

    @Override
    protected void init() {
        int lineCount = Math.max(1, font.split(message, PANEL_W - 20).size());
        messageBlockH = lineCount * LINE_H;
        int contentH = 28 + messageBlockH + 6;
        if (withNameInput) {
            contentH += 16 + 6;
        }
        panelH = contentH + 20 + 8;

        left = (width - PANEL_W) / 2;
        top = (height - panelH) / 2;

        if (withNameInput) {
            int inputY = top + 28 + messageBlockH + 6;
            nameInput = new EditBox(font, left + 10, inputY, PANEL_W - 20, 16, Component.empty());
            nameInput.setMaxLength(100);
            nameInput.setValue(defaultName == null ? "" : defaultName);
            addRenderableWidget(nameInput);
        }

        if (cancellable) {
            addRenderableWidget(Button.builder(Component.literal("Yes"), b -> {
                        String name = withNameInput ? nameInput.getValue().trim() : null;
                        if (withNameInput && name.isEmpty()) {
                            return;
                        }
                        onClose();
                        if (onConfirmWithName != null) {
                            onConfirmWithName.accept(name);
                        } else if (onConfirm != null) {
                            onConfirm.run();
                        }
                    })
                    .bounds(left + 20, top + panelH - 26, 80, 20).build());
            addRenderableWidget(Button.builder(Component.literal("No"), b -> onClose())
                    .bounds(left + PANEL_W - 100, top + panelH - 26, 80, 20).build());
        } else {
            addRenderableWidget(Button.builder(Component.literal("OK"), b -> onClose())
                    .bounds(left + PANEL_W / 2 - 40, top + panelH - 26, 80, 20).build());
        }
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
        super.renderBackground(g, mouseX, mouseY, partialTicks);
        NcPanel.drawPanel(g, left, top, PANEL_W, panelH);
        NcPanel.drawCentered(g, font, title, left + PANEL_W / 2, top + 10, 0x404040);
        int lineY = top + 28;
        for (net.minecraft.util.FormattedCharSequence line : font.split(message, PANEL_W - 20)) {
            g.drawString(font, line, left + 10, lineY, 0x202020, false);
            lineY += LINE_H;
        }
    }
}
