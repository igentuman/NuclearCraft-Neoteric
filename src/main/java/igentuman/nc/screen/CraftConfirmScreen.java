package igentuman.nc.screen;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import igentuman.nc.network.PacketTerminalCraft;
import igentuman.nc.screen.element.NcPanel;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class CraftConfirmScreen extends Screen {

    private static final int PANEL_W = 176;
    private static final int PANEL_H = 116;

    private final Screen parent;
    private final BlockPos pos;
    private final ItemStack target;
    private int qty = 1;
    private int left;
    private int top;

    public CraftConfirmScreen(Screen parent, BlockPos pos, ItemStack target) {
        super(Component.translatable("screen.nuclearcraft.crafter.confirm_title"));
        this.parent = parent;
        this.pos = pos;
        this.target = target;
    }

    @Override
    protected void init() {
        left = (width - PANEL_W) / 2;
        top = (height - PANEL_H) / 2;

        addRenderableWidget(Button.builder(Component.literal("-"), b -> setQty(qty - step()))
                .bounds(left + 6, top + 56, 18, 18).build());
        addRenderableWidget(Button.builder(Component.literal("+"), b -> setQty(qty + step()))
                .bounds(left + PANEL_W - 24, top + 56, 18, 18).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.nuclearcraft.crafter.confirm"), b -> confirm())
                .bounds(left + 5, top + 92, 80, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.nuclearcraft.crafter.cancel"), b -> onClose())
                .bounds(left + 91, top + 92, 80, 20).build());
    }

    private int step() {
        return Minecraft.getInstance().hasShiftDown() ? 10 : 1;
    }

    private void setQty(int value) {
        qty = Mth.clamp(value, 1, PacketTerminalCraft.MAX_QTY);
    }

    private void confirm() {
        ClientPacketDistributor.sendToServer(new PacketTerminalCraft(pos, target, qty));
        onClose();
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
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(g, mouseX, mouseY, partialTicks);
        NcPanel.drawPanel(g, left, top, PANEL_W, PANEL_H);
        NcPanel.drawCentered(g, font, title, left + PANEL_W / 2, top + 8, 0xFF404040);
        g.item(target, left + PANEL_W / 2 - 8, top + 22);
        NcPanel.drawCentered(g, font, target.getHoverName(), left + PANEL_W / 2, top + 42, 0xFF404040);
        NcPanel.drawCentered(g, font, Component.translatable("screen.nuclearcraft.crafter.qty", qty),
                left + PANEL_W / 2, top + 61, 0xFF202020);
    }
}
