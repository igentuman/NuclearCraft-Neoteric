package igentuman.nc.client.gui.element.button;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.NuclearCraft;
import igentuman.nc.client.gui.element.NCGuiElement;
import igentuman.nc.client.gui.processor.side.SideConfigScreen;
import igentuman.nc.network.toServer.PacketSideConfigToggle;
import igentuman.nc.util.sided.SidedContentHandler;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

import static igentuman.nc.util.TextUtils.applyFormat;
import static net.minecraft.ChatFormatting.*;

public class SideConfig extends NCGuiElement {
    protected SideConfigScreen screen;
    private int color;
    protected SideBtn btn;
    private int slotId;
    private int direction;

    public SideConfig(int xPos, int yPos, int slotId, SideConfigScreen screen, int direction, ResourceLocation btnTexture)  {
        x = xPos;
        y = yPos;
        width = 16;
        height = 16;
        this.screen = screen;
        this.slotId = slotId;
        btn = new SideBtn(X(), Y(), btnTexture, pButton -> {
            NuclearCraft.packetHandler().sendToServer(new PacketSideConfigToggle(screen.getPosition(), slotId, direction));
        });

        this.direction = direction;
    }

    public boolean onPress() {
        btn.onPress();
        return true;
    }

    @Override
    public void draw(PoseStack transform, int mX, int mY, float pTicks) {
        super.draw(transform, mX, mY, pTicks);
        renderButton(transform, mX, mY, pTicks);
    }

    public String getDirectionName() {
        return SidedContentHandler.RelativeDirection.getDirectionName(direction).toLowerCase();
    }

    @Override
    public List<Component> getTooltips() {
        tooltips.clear();
        tooltips.add(applyFormat(Component.translatable("side_config."+getDirectionName()), AQUA)
                .append(applyFormat(Component.translatable("side_config."+screen.getSlotMode(direction, slotId).name().toLowerCase()),GOLD)));
        return tooltips;
    }

    public void getColorOverlay() {
        color = screen.getSlotMode(direction, slotId).getColor();
    }

    @Override
    public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        RenderSystem.enableDepthTest();
        getColorOverlay();
        btn.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        fill(pPoseStack, X(), Y(), X() + this.width, Y() + this.height, color);
        if (this.isHovered) {
            this.renderToolTip(pPoseStack, pMouseX, pMouseY);
        }
    }

    public static class SideBtn extends ImageButton {
        public SideBtn(int x, int y, ResourceLocation btnTexture, OnPress onPress) {
            super(x, y, 16, 16, 0, 0, 0, btnTexture, 16, 16, onPress);
        }
    }
}
