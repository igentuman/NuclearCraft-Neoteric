package igentuman.nc.client.gui.element.button;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.matrix.MatrixStack;
import igentuman.nc.NuclearCraft;
import igentuman.nc.client.gui.element.NCGuiElement;
import igentuman.nc.client.gui.processor.side.SideConfigScreen;
import igentuman.nc.network.toServer.PacketSideConfigToggle;
import igentuman.nc.handler.sided.SidedContentHandler;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.ResourceLocation;

import java.util.List;

import static igentuman.nc.util.TextUtils.applyFormat;
import static net.minecraft.util.text.TextFormatting.AQUA;
import static net.minecraft.util.text.TextFormatting.GOLD;

public class SideConfig extends NCGuiElement {
    protected SideConfigScreen screen;
    private int color;
    protected SideBtn btn;
    private int slotId;
    private int direction;

    public SideConfig(int xPos, int yPos, int slotId, SideConfigScreen screen, int direction, ResourceLocation btnTexture)  {
        super(xPos, yPos, 16, 16, new TranslationTextComponent(""));
        x = xPos;
        y = yPos;
        width = 16;
        height = 16;
        this.screen = screen;
        this.slotId = slotId;
        btn = new SideBtn(X(), Y(), btnTexture, (Button.IPressable) pButton -> {
            NuclearCraft.packetHandler().sendToServer(new PacketSideConfigToggle(screen.getPosition(), slotId, direction));
        });

        this.direction = direction;
    }

    public boolean onPress() {
        btn.onPress();
        return true;
    }

    @Override
    public void draw(MatrixStack transform, int mX, int mY, float pTicks) {
        super.draw(transform, mX, mY, pTicks);
        renderButton(transform, mX, mY, pTicks);
    }

    public String getDirectionName() {
        return SidedContentHandler.RelativeDirection.getDirectionName(direction).toLowerCase();
    }

    @Override
    public List<ITextComponent> getTooltips() {
        tooltips.clear();
        tooltips.add(applyFormat(new TranslationTextComponent("side_config."+getDirectionName()), AQUA)
                .append(applyFormat(new TranslationTextComponent("side_config."+screen.getSlotMode(direction, slotId).name().toLowerCase()),GOLD)));
        return tooltips;
    }

    public void getColorOverlay() {
        color = screen.getSlotMode(direction, slotId).getColor();
    }

    @Override
    public void renderButton(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTick) {
        RenderSystem.enableDepthTest();
        getColorOverlay();
        btn.render(pMatrixStack, pMouseX, pMouseY, pPartialTick);
        fill(pMatrixStack, X(), Y(), X() + this.width, Y() + this.height, color);
        if (this.isHovered) {
            this.renderToolTip(pMatrixStack, pMouseX, pMouseY);
        }
    }

    public static class SideBtn extends ImageButton {
        public SideBtn(int x, int y, ResourceLocation btnTexture) {
            super(x, y, 16, 16, 0, 0, 0, btnTexture, 16, 16, null);
        }

        public SideBtn(int x, int y, ResourceLocation btnTexture, Object o) {
            super(x, y, 16, 16, 0, 0, 0, btnTexture, 16, 16, (IPressable) o);
        }
    }
}
