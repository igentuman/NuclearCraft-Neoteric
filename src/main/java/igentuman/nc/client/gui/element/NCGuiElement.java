package igentuman.nc.client.gui.element;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.matrix.MatrixStack;
import igentuman.nc.client.gui.processor.NCProcessorScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.fonts.Font;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;

public class NCGuiElement extends Widget {
    protected static ResourceLocation TEXTURE = new ResourceLocation(MODID, "textures/gui/widgets.png");
    public static int RELATIVE_X = 0;
    public static int RELATIVE_Y = 0;
    protected int width;
    protected int height;
    public int x;
    public int y;
    public boolean configFlag = false;
    protected List<ITextComponent> tooltips = new ArrayList<>();
    protected ContainerScreen screen;
    protected int slotId;

    public NCGuiElement(int p_i232254_1_, int p_i232254_2_, int p_i232254_3_, int p_i232254_4_, ITextComponent p_i232254_5_) {
        super(p_i232254_1_, p_i232254_2_, p_i232254_3_, p_i232254_4_, p_i232254_5_);
    }

    public int X()
    {
        return RELATIVE_X+x;
    }

    public int Y()
    {
        return RELATIVE_Y+y;
    }

    protected ITextComponent message;
    protected boolean isHovered;
    public boolean active = true;
    public boolean visible = true;
    protected float alpha = 1.0F;
    protected boolean focused;

    public boolean onPress() {
        return false;
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if(X() <= pMouseX && pMouseX < X() + width && Y() <= pMouseY && pMouseY < Y() + height) {
            return onPress();
        }
        return false;
    }

    protected BlockPos getPosition() {
        if(screen instanceof NCProcessorScreen<?>) {
            NCProcessorScreen<?> processorScreen = (NCProcessorScreen<?>) screen;
            return processorScreen.getMenu().getPosition();
        }
        return BlockPos.ZERO;
    }

    @Override
    public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTick) {
        if (this.visible) {
            this.isHovered = pMouseX >= this.x && pMouseY >= this.y && pMouseX < this.x + this.width && pMouseY < this.y + this.height;
            this.renderButton(pMatrixStack, pMouseX, pMouseY, pPartialTick);
        }
    }

    protected void onFocusedChanged(boolean pFocused) {
    }

    public boolean isMouseOver(double pMouseX, double pMouseY) {
        return this.active && this.visible && pMouseX >= (double)this.x && pMouseY >= (double)this.y && pMouseX < (double)(this.x + this.width) && pMouseY < (double)(this.y + this.height);
    }

    public void renderToolTip(MatrixStack pMatrixStack, int pMouseX, int pMouseY) {

    }

    protected boolean clicked(double pMouseX, double pMouseY) {
        return this.active && this.visible && pMouseX >= (double)this.x && pMouseY >= (double)this.y && pMouseX < (double)(this.x + this.width) && pMouseY < (double)(this.y + this.height);
    }

    public boolean isHoveredOrFocused() {
        return this.isHovered || this.focused;
    }

    public boolean changeFocus(boolean pFocus) {
        if (this.active && this.visible) {
            this.focused = !this.focused;
            this.onFocusedChanged(this.focused);
            return this.focused;
        } else {
            return false;
        }
    }

    public void renderButton(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        FontRenderer fontrenderer = minecraft.font;
        minecraft.getTextureManager().bind(WIDGETS_LOCATION);
        int i = this.getYImage(this.isHoveredOrFocused());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        this.blit(pMatrixStack, this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
        this.blit(pMatrixStack, this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
        this.renderBg(pMatrixStack, minecraft, pMouseX, pMouseY);
        int j = getFGColor();
        drawCenteredString(pMatrixStack, fontrenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, j | MathHelper.ceil(this.alpha * 255.0F) << 24);
    }

    public static final int UNSET_FG_COLOR = -1;
    protected int packedFGColor = UNSET_FG_COLOR;
    public int getFGColor() {
        if (packedFGColor != UNSET_FG_COLOR) return packedFGColor;
        return this.active ? 16777215 : 10526880; // White : Light Grey
    }
    public void setFGColor(int color) {
        this.packedFGColor = color;
    }
    public void clearFGColor() {
        this.packedFGColor = UNSET_FG_COLOR;
    }

    protected void renderBg(MatrixStack pMatrixStack, Minecraft pMinecraft, int pMouseX, int pMouseY) {
    }

    public int getWidth() {
        return this.width;
    }

    public void setWidth(int pWidth) {
        this.width = pWidth;
    }

    public void setHeight(int value) {
        this.height = value;
    }

    public void setAlpha(float pAlpha) {
        this.alpha = pAlpha;
    }

    public void setMessage(ITextComponent pMessage) {
        this.message = pMessage;
    }

    public ITextComponent getMessage() {
        return this.message;
    }

    protected int getYImage(boolean pIsHovered) {
        int i = 1;
        if (!this.active) {
            i = 0;
        } else if (pIsHovered) {
            i = 2;
        }

        return i;
    }

    public void draw(MatrixStack transform, int mX, int mY, float pTicks) {
        Minecraft.getInstance().getTextureManager().bind(TEXTURE);
    }

    public List<ITextComponent> getTooltips() {
        return tooltips;
    }

    public void addTooltip(ITextComponent tooltip)
    {
        tooltips.add(tooltip);
    }

    public void clearTooltips() {
        tooltips.clear();
    }

    public NCGuiElement forConfig(ContainerScreen<?> screen, int slotId) {
        this.configFlag = true;
        this.screen = screen;
        this.slotId = slotId;
        return this;
    }

    public NCGuiElement setScreen(ContainerScreen<?> screen) {
        this.screen = screen;
        return this;
    }

    public NCGuiElement setSlotId(int id) {
        this.slotId = id;
        return this;
    }
}
