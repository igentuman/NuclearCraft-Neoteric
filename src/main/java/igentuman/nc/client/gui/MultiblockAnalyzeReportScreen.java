package igentuman.nc.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.client.NcClient;
import igentuman.nc.client.gui.element.NCGuiElement;
import igentuman.nc.client.gui.element.button.Button;
import igentuman.nc.container.MultiblockControllerContainer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.client.event.ContainerScreenEvent.Render.Background;

import java.util.*;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.util.TextUtils.__;

public class MultiblockAnalyzeReportScreen<T extends MultiblockControllerContainer> extends AbstractContainerScreen<T> {
    protected final ResourceLocation GUI = rl("textures/gui/window_no_inventory.png");
    protected int relX;
    protected int relY;

    protected AbstractContainerScreen parentScreen;
    public List<NCGuiElement> widgets = new ArrayList<>();

    public MultiblockAnalyzeReportScreen(T container, Inventory inv, Component name) {
        super(container, inv, name);
        imageWidth = 180;
        imageHeight = 180;
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
        updateRelativeCords();
        widgets.clear();
        widgets.add(new Button.CloseConfig(159, 79, this));
    }

    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        for(NCGuiElement widget : widgets) {
            if(widget.mouseClicked(pMouseX, pMouseY, pButton)) {
                return true;
            }
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().forceSetScreen(parentScreen);
    }

    public MultiblockAnalyzeReportScreen(AbstractContainerScreen parentScreen, T container) {
        this(container, NcClient.tryGetClientPlayer().getInventory(), Component.empty());
        this.parentScreen = parentScreen;
    }

    @Override
    public void render(PoseStack graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);
        int i = this.leftPos;
        int j = this.topPos;
        this.renderBg(graphics, partialTicks, mouseX, mouseY);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new Background(this, graphics, mouseX, mouseY));
        RenderSystem.disableDepthTest();
        for(Widget widget : this.renderables) {
            widget.render(graphics, mouseX, mouseY, partialTicks);
        }
        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.translate((double)i, (double)j, 0.0D);
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        this.hoveredSlot = null;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        this.renderLabels(graphics, mouseX, mouseY);
        posestack.popPose();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.enableDepthTest();
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    private void renderWidgets(PoseStack graphics, float partialTicks, int mouseX, int mouseY) {
        for(NCGuiElement widget: widgets) {
            widget.draw(graphics, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    protected void renderLabels(PoseStack graphics, int mouseX, int mouseY) {
        drawCenteredString(graphics, font,  __("multiblock.analyze.report"), imageWidth/2, titleLabelY, 0xffffff);
        int y = 40;
        graphics.pushPose();
        graphics.scale(0.5f, 0.5f, 0.5f);
        Map<String, String> reportItems = container().getReportItems();
        List<String> tooltips = reportItems.keySet().stream()
                .sorted(Comparator.comparingInt(key -> {
                    String[] parts = key.split("\\.");
                    if (parts.length > 2) {
                        try {
                            return Integer.parseInt(parts[2]);
                        } catch (NumberFormatException e) {
                            return 0;
                        }
                    }
                    return 0;
                }))
                .toList();
        for(String record: tooltips) {
            drawString(graphics, font, __(record, reportItems.get(record)), 20, y, ChatFormatting.DARK_GRAY.getColor());
            y += 12;
        }
        drawString(graphics, font, __("report.nc.validation_duration", container().validationDuration()), 20, y, ChatFormatting.DARK_GRAY.getColor());
        y += 12;
        drawString(graphics, font, __("report.nc.validation_count", container().validationCount()), 20, y, ChatFormatting.DARK_GRAY.getColor());
        drawString(graphics, font, __("report.nc.multiblock_ticks_count", container().ticksCount()), 20, y+12, ChatFormatting.DARK_GRAY.getColor());
        graphics.scale(1f, 1f, 1f);
        graphics.popPose();
    }

    private MultiblockControllerContainer container() {
        return this.getMenu();
    }

    @Override
    protected void renderBg(PoseStack graphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI);
        updateRelativeCords();
        blit(graphics, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
        renderWidgets(graphics, partialTicks, mouseX, mouseY);
    }
}
