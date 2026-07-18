package igentuman.nc.client.gui.kugelblitz;

import com.mojang.blaze3d.systems.RenderSystem;
import igentuman.nc.client.gui.IProgressScreen;
import igentuman.nc.client.gui.IVerticalBarScreen;
import igentuman.nc.client.gui.element.NCGuiElement;
import igentuman.nc.client.gui.element.bar.ProgressBar;
import igentuman.nc.client.gui.element.bar.VerticalBar;
import igentuman.nc.client.gui.element.button.Button;
import igentuman.nc.container.ChamberPortContainer;
import igentuman.nc.container.EXPLContainer;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.TextUtils.formatEnergy;

public class EXPLScreen extends AbstractContainerScreen<EXPLContainer> {
    protected final ResourceLocation GUI = rl("textures/gui/small_window.png");
    protected int relX;
    protected int relY;
    private int xCenter;

    public EXPLContainer container()
    {
        return (EXPLContainer)menu;
    }

    public List<NCGuiElement> widgets = new ArrayList<>();


    private Button burstButton;

    public EXPLScreen(EXPLContainer container, Inventory inv, Component name) {
        super(container, inv, name);
        imageWidth = 112;
        imageHeight = 126;
    }

    protected void updateRelativeCords()
    {
        relX = (this.width - this.imageWidth) / 2;
        relY = (this.height - this.imageHeight) / 2;
        NCGuiElement.RELATIVE_X = relX;
        NCGuiElement.RELATIVE_Y = relY;
    }

    protected void init() {
        super.init();
        Minecraft mc = Minecraft.getInstance();
        updateRelativeCords();
        widgets.clear();
        burstButton = new Button(13, 46, this, 77, __("gui.nuclearcraft:button.burst"), (button) -> {
            if(container().isReady()) {
                container().burst();
            }
        });
        burstButton.clearTooltips();
    }

    @Override
    public void render(PoseStack graphics, int mouseX, int mouseY, float partialTicks) {
        xCenter = getGuiLeft()-imageWidth/2;
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    private void renderWidgets(PoseStack graphics, float partialTicks, int mouseX, int mouseY) {
        for(NCGuiElement widget: widgets) {
            widget.draw(graphics, mouseX, mouseY, partialTicks);
        }
        if(container().isReady()) {
            burstButton.draw(graphics, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    protected void renderLabels(PoseStack graphics, int mouseX, int mouseY) {
        drawCenteredString(graphics, font,  menu.getTitle(), imageWidth/2, titleLabelY, 0xffffff);
        drawCenteredString(graphics, font,  __("label.kugelblitz.charge", formatEnergy(container().getCharge())), imageWidth/2, titleLabelY+20, 0xffffff);
        renderTooltips(graphics, mouseX-relX, mouseY-relY);
    }

    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        for(NCGuiElement widget : widgets) {
            if(widget.mouseClicked(pMouseX, pMouseY, pButton)) {
                return true;
            }
        }
        if(container().isReady()) {
            burstButton.mouseClicked(pMouseX, pMouseY, pButton);
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    protected void renderBg(PoseStack graphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI);
        updateRelativeCords();
        blit(graphics, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
        renderWidgets(graphics, partialTicks, mouseX, mouseY);
    }

    private void renderTooltips(PoseStack graphics, int pMouseX, int pMouseY) {

        for(NCGuiElement widget: widgets) {
           if(widget.isMouseOver(pMouseX, pMouseY)) {
               if(widget.getTooltips().size() > 0 && !widget.getTooltips().get(0).getString().isBlank()) {
                   renderTooltip(graphics, widget.getTooltips(),
                           Optional.empty(), pMouseX, pMouseY);
               }
           }
        }
    }

}
