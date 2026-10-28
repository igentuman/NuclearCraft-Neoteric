package igentuman.nc.screen;

import igentuman.nc.screen.element.NcPanel;
import igentuman.nc.handler.crafter.CraftShortage;
import igentuman.nc.util.GuiFluidRenderer;
import igentuman.nc.util.TextUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public class CraftDeniedScreen extends Screen {

    private static final int PANEL_W = 176;
    private static final int MAX_ROWS = 8;
    private static final int ROW_H = 18;

    private final Screen parent;
    private final List<CraftShortage<ItemStack>> items;
    private final List<CraftShortage<FluidStack>> fluids;
    private final boolean tooComplex;
    private int rows;
    private int panelH;
    private int left;
    private int top;

    public CraftDeniedScreen(Screen parent, List<CraftShortage<ItemStack>> items,
                             List<CraftShortage<FluidStack>> fluids, boolean tooComplex) {
        super(Component.translatable("screen.nuclearcraft.crafter.denied_title"));
        this.parent = parent;
        this.items = items;
        this.fluids = fluids;
        this.tooComplex = tooComplex;
    }

    private int total() {
        return items.size() + fluids.size();
    }

    @Override
    protected void init() {
        rows = tooComplex ? 1 : Math.min(total(), MAX_ROWS);
        panelH = 36 + rows * ROW_H + 30;
        left = (width - PANEL_W) / 2;
        top = (height - panelH) / 2;

        addRenderableWidget(Button.builder(Component.translatable("screen.nuclearcraft.crafter.back"), b -> onClose())
                .bounds(left + PANEL_W / 2 - 40, top + panelH - 24, 80, 20).build());
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
        NcPanel.drawCentered(g, font, title, left + PANEL_W / 2, top + 8, 0x404040);

        if (tooComplex) {
            NcPanel.drawCentered(g, font, Component.translatable("screen.nuclearcraft.crafter.denied.too_complex"),
                    left + PANEL_W / 2, top + 32, 0x802020);
            return;
        }
        int rowY = top + 28;
        int shown = Math.min(total(), MAX_ROWS);
        for (int i = 0; i < shown; i++) {
            if (i < items.size()) {
                ItemStack stack = items.get(i).stack();
                g.renderItem(stack, left + 12, rowY);
                g.drawString(font, Component.translatable("screen.nuclearcraft.crafter.denied.entry",
                        items.get(i).amount(), stack.getHoverName()), left + 34, rowY + 4, 0x404040, false);
            } else {
                int f = i - items.size();
                FluidStack fluid = fluids.get(f).stack();
                GuiFluidRenderer.renderFluidTank(g, left + 12, rowY, 16, 16, fluid, Math.max(1, fluid.getAmount()));
                g.drawString(font, Component.translatable("screen.nuclearcraft.crafter.denied.fluid_entry",
                        TextUtils.formatLiquid(fluids.get(f).amount()), fluid.getHoverName()), left + 34, rowY + 4, 0x404040, false);
            }
            rowY += ROW_H;
        }
        if (total() > MAX_ROWS) {
            NcPanel.drawCentered(g, font, Component.translatable("screen.nuclearcraft.crafter.denied.more",
                    total() - MAX_ROWS), left + PANEL_W / 2, rowY + 2, 0x606060);
        }
    }
}
