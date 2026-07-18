package igentuman.nc.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import igentuman.nc.NuclearCraft;
import igentuman.nc.container.PipeConnectorContainer;
import igentuman.nc.network.toServer.PacketGuiButtonPress;
import igentuman.nc.pipe.PipeCapabilityType;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.util.TextUtils.__;

public class PipeConnectorScreen extends AbstractContainerScreen<PipeConnectorContainer> {

    private static final ResourceLocation GUI = rl("textures/gui/processor.png");
    private static final ResourceLocation WIDGETS = rl("textures/gui/widgets.png");

    private Button modeButton;

    public PipeConnectorScreen(PipeConnectorContainer container, Inventory inv, Component name) {
        super(container, inv, name);
        imageWidth = 176;
        imageHeight = 176;
        inventoryLabelY = imageHeight - 94;
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
        modeButton = addRenderableWidget(new Button(leftPos + 92, topPos + 58, 78, 20, modeLabel(), b ->
                        NuclearCraft.packetHandler().sendToServer(
                                new PacketGuiButtonPress(menu.getBlockPos(), PipeConnectorContainer.BTN_MODE))));
        addRenderableWidget(new RedstoneModeButton(leftPos + 152, topPos + 37));
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

    @Override
    public void render(@NotNull PoseStack graphics, int mouseX, int mouseY, float partialTicks) {
        // DataSlots refresh each tick; keep button labels in step with the synced connector state.
        modeButton.setMessage(modeLabel());
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(@NotNull PoseStack graphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI);
        blit(graphics, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(@NotNull PoseStack graphics, int mouseX, int mouseY) {
        drawString(graphics, font, title, titleLabelX, titleLabelY, 0x404040);
        drawString(graphics, font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040);
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
        public void onPress() {
            NuclearCraft.packetHandler().sendToServer(new PacketGuiButtonPress(menu.getBlockPos(), buttonId));
        }

        @Override
        public void renderButton(@NotNull PoseStack g, int mouseX, int mouseY, float partialTicks) {
            int boxSize = 11;
            int bx = this.x;
            int by = this.y + (height - boxSize) / 2;
            fill(g, bx, by, bx + boxSize, by + boxSize, 0xFFAAAAAA);
            fill(g, bx + 1, by + 1, bx + boxSize - 1, by + boxSize - 1, 0xFF2B2B2B);
            if (state.getAsBoolean()) {
                fill(g, bx + 3, by + 3, bx + boxSize - 3, by + boxSize - 3, 0xFF55D355);
            }
            int textColor = isHoveredOrFocused() ? 0xFFFFA0 : 0x000000;
            drawString(g, PipeConnectorScreen.this.font, getMessage(), bx + boxSize + 4, this.y + (height - 8) / 2, textColor);
        }

        @Override
        public void updateNarration(@NotNull NarrationElementOutput narration) {
            defaultButtonNarrationText(narration);
        }
    }

    private class RedstoneModeButton extends AbstractButton {

        RedstoneModeButton(int x, int y) {
            super(x, y, 18, 18, Component.empty());
        }

        @Override
        public void onPress() {
            NuclearCraft.packetHandler().sendToServer(
                    new PacketGuiButtonPress(menu.getBlockPos(), PipeConnectorContainer.BTN_REDSTONE));
        }

        @Override
        public void renderButton(@NotNull PoseStack g, int mouseX, int mouseY, float partialTicks) {
            int mode = menu.getConnectorRedstoneMode().ordinal();
            int v = 220 - mode * 36;
            if (isHoveredOrFocused()) {
                v += 18;
            }
            RenderSystem.setShaderTexture(0, WIDGETS);
            blit(g, this.x, this.y, 184, v, 18, 18);
            // tooltip not supported in 1.19.2
        }

        @Override
        public void updateNarration(@NotNull NarrationElementOutput narration) {
            defaultButtonNarrationText(narration);
        }
    }
}
