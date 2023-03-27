package igentuman.nc.gui.element.button;

import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.container.NCProcessorContainer;
import igentuman.nc.gui.element.NCGuiElement;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.network.chat.Component;

import java.util.List;

public class Button extends NCGuiElement {
    protected NCProcessorContainer container;
    protected int bId;

    protected ImageButton btn;
    public Button(int xPos, int yPos, NCProcessorContainer container, int id)  {
        x = xPos;
        y = yPos;
        this.container = container;
        bId = id;
    }

    public List<Component> getTooltips() {
        return List.of(Component.literal(" FE"));
    }

    @Override
    public void draw(PoseStack transform, int mX, int mY, float pTicks) {
        super.draw(transform, mX, mY, pTicks);
        btn.render(transform, mX, mY, pTicks);
    }

    public static class SideConfig extends Button {
        public SideConfig(int xPos, int yPos, NCProcessorContainer container) {
            super(xPos, yPos, container, 69);//nice
            btn = new ImageButton(X(), Y(), 18, 18, 220, 220, 18, TEXTURE, (net.minecraft.client.gui.components.Button.OnPress)null);
        }
    }

    public static class RedstoneConfig extends Button {
        public RedstoneConfig(int xPos, int yPos, NCProcessorContainer container) {
            super(xPos, yPos, container, 70);
            btn = new ImageButton(X(), Y(), 18, 18, 238, 220, 18, TEXTURE, (net.minecraft.client.gui.components.Button.OnPress)null);
        }
    }
}
