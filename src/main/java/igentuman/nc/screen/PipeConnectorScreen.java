package igentuman.nc.screen;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.minecraft.client.renderer.RenderPipelines;
import igentuman.nc.container.PipeConnectorContainer;
import igentuman.nc.network.PacketPipeConnectorButton;
import igentuman.nc.pipe.PipeCapabilityType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.util.TextUtils.__;

public class PipeConnectorScreen extends AbstractContainerScreen<PipeConnectorContainer> {

    private static final Identifier GUI = rl("textures/gui/processor.png");

    private Button modeButton;
    private Button redstoneButton;

    public PipeConnectorScreen(PipeConnectorContainer container, Inventory inv, Component name) {
        super(container, inv, name, 176, 176);
    }

    @Override
    protected void init() {
        super.init();
        for (PipeCapabilityType type : PipeCapabilityType.values()) {
            int row = type.index;
            addRenderableWidget(new CapabilityCheckbox(
                    leftPos + 10, topPos + 18 + row * 11, 52, 12,
                    capabilityLabel(type), buttonIdFor(type),
                    () -> menu.isCapabilityEnabled(type)));
        }
        modeButton = addRenderableWidget(Button.builder(modeLabel(), b -> send(PipeConnectorContainer.BTN_MODE))
                .bounds(leftPos + 92, topPos + 58, 78, 20)
                .build());
        redstoneButton = addRenderableWidget(Button.builder(redstoneLabel(), b -> send(PipeConnectorContainer.BTN_REDSTONE))
                .bounds(leftPos + 92, topPos + 34, 78, 20)
                .build());
    }

    private void send(int buttonId) {
        ClientPacketDistributor.sendToServer(new PacketPipeConnectorButton(menu.getBlockPos(), buttonId));
    }

    private static int buttonIdFor(PipeCapabilityType type) {
        return switch (type) {
            case ITEM -> PipeConnectorContainer.BTN_CAP_ITEM;
            case FLUID -> PipeConnectorContainer.BTN_CAP_FLUID;
            case ENERGY -> PipeConnectorContainer.BTN_CAP_ENERGY;
        };
    }

    private Component capabilityLabel(PipeCapabilityType type) {
        return __("gui." + MODID + ".pipe.capability." + type.name().toLowerCase());
    }

    private Component modeLabel() {
        return __("gui." + MODID + ".pipe.mode." + menu.getConnectorMode().name().toLowerCase());
    }

    private Component redstoneLabel() {
        return __("gui." + MODID + ".pipe.redstone." + menu.getConnectorRedstoneMode().name().toLowerCase());
    }

    @Override
    public void extractRenderState(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        modeButton.setMessage(modeLabel());
        redstoneButton.setMessage(redstoneLabel());
        super.extractRenderState(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void extractBackground(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(graphics, mouseX, mouseY, partialTicks);
        graphics.blit(RenderPipelines.GUI_TEXTURED, GUI, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override
    protected void extractLabels(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, title, titleLabelX, titleLabelY, 0xFF404040, false);
        graphics.text(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xFF404040, false);
    }

    private class CapabilityCheckbox extends AbstractButton {

        private final int buttonId;
        private final BooleanSupplier state;

        CapabilityCheckbox(int x, int y, int width, int height, Component label, int buttonId, BooleanSupplier state) {
            super(x, y, width, height, label);
            this.buttonId = buttonId;
            this.state = state;
        }

        @Override
        public void onPress(InputWithModifiers input) {
            send(buttonId);
        }

        @Override
        protected void extractContents(@NotNull GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTicks) {
            int boxSize = 11;
            int bx = getX();
            int by = getY() + (height - boxSize) / 2;
            g.fill(bx, by, bx + boxSize, by + boxSize, 0xFFAAAAAA);
            g.fill(bx + 1, by + 1, bx + boxSize - 1, by + boxSize - 1, 0xFF2B2B2B);
            if (state.getAsBoolean()) {
                g.fill(bx + 3, by + 3, bx + boxSize - 3, by + boxSize - 3, 0xFF55D355);
            }
            int textColor = isHoveredOrFocused() ? 0xFFFFFFA0 : 0xFF000000;
            g.text(font, getMessage(), bx + boxSize + 4, getY() + (height - 8) / 2, textColor, false);
        }

        @Override
        protected void updateWidgetNarration(@NotNull NarrationElementOutput narration) {
            defaultButtonNarrationText(narration);
        }
    }
}
