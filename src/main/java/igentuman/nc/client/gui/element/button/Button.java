package igentuman.nc.client.gui.element.button;

import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.client.gui.processor.side.SideConfigSlotSelectionScreen;
import igentuman.nc.container.NCProcessorContainer;
import igentuman.nc.client.gui.element.NCGuiElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class Button<T extends AbstractContainerScreen> extends NCGuiElement {
    protected NCProcessorContainer container;
    protected AbstractContainerScreen screen;
    protected int bId;

    protected ImageButton btn;
    protected Component tooltipKey = Component.empty();

    public Button(int xPos, int yPos, T screen, int id)  {
        x = xPos;
        y = yPos;
        this.container = (NCProcessorContainer) screen.getMenu();
        this.screen = screen;
        bId = id;
    }

    public List<Component> getTooltips() {
        return List.of(tooltipKey);
    }

    @Override
    public void draw(PoseStack transform, int mX, int mY, float pTicks) {
        super.draw(transform, mX, mY, pTicks);
        btn.render(transform, mX, mY, pTicks);
    }

    @Override
    public boolean onPress() {
        btn.onPress();
        return true;
    }

    public static class SideConfig extends Button {
        public SideConfig(int xPos, int yPos, AbstractContainerScreen screen) {
            super(xPos, yPos, screen, 69);//nice
            height = 18;
            width = 18;
            btn = new ImageButton(X(), Y(), width, height, 220, 220, 18, TEXTURE, pButton -> {
                //NCMessages.sendToServer(new PacketGuiButtonPress(container.getPosition(), bId));
                Minecraft.getInstance().forceSetScreen(new SideConfigSlotSelectionScreen<>(screen));
            });
            tooltipKey = Component.translatable("gui.nc.side_config.tooltip");
        }
    }

    public static class RedstoneConfig extends Button {
        public RedstoneConfig(int xPos, int yPos, AbstractContainerScreen screen) {
            super(xPos, yPos, screen, 70);
            height = 18;
            width = 18;
            btn = new ImageButton(X(), Y(), width, height, 238, 220, 18, TEXTURE, (net.minecraft.client.gui.components.Button.OnPress)null);
        }
    }

    public static class CloseConfig extends Button {
        public <T extends NCProcessorContainer> CloseConfig(int xPos, int yPos, AbstractContainerScreen<T> screen) {
            super(xPos, yPos, screen, 71);
            height = 18;
            width = 18;
            btn = new ImageButton(X(), Y(), width, height, 202, 220, 18, TEXTURE, pButton -> {
                this.screen.onClose();
            });
        }
    }
}
