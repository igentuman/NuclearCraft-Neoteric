package igentuman.nc.client.gui.crafter;

import igentuman.nc.NuclearCraft;
import igentuman.nc.network.toServer.PacketTerminalCraft;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import static igentuman.nc.util.TextUtils.__;

public class CraftConfirmScreen extends Screen {

    private static final int PANEL_W = 176;
    private static final int PANEL_H = 116;
    private static final int SHIFT_STEP = 10;

    private final EngineersCrafterScreen parent;
    private final BlockPos pos;
    private final ItemStack target;
    private int qty = 1;
    private int left;
    private int top;

    public CraftConfirmScreen(EngineersCrafterScreen parent, BlockPos pos, ItemStack target) {
        super(__("gui.nc.crafter.confirm.title"));
        this.parent = parent;
        this.pos = pos;
        this.target = target;
    }

    @Override
    protected void init() {
        super.init();
        left = (width - PANEL_W) / 2;
        top = (height - PANEL_H) / 2;

        int stepY = top + 56;
        addRenderableWidget(new Button(left + PANEL_W / 2 - 82, stepY, 18, 18, Component.literal("-"), b -> setQty(qty - step())));
        addRenderableWidget(new Button(left + PANEL_W / 2 + 64, stepY, 18, 18, Component.literal("+"), b -> setQty(qty + step())));

        int btnW = 80;
        int gap = 6;
        int btnY = top + PANEL_H - 24;
        addRenderableWidget(new Button(left + PANEL_W / 2 - btnW - gap / 2, btnY, btnW, 20, __("gui.nc.crafter.confirm"), b -> confirm()));
        addRenderableWidget(new Button(left + PANEL_W / 2 + gap / 2, btnY, btnW, 20, __("gui.nc.crafter.cancel"), b -> onClose()));
    }

    private int step() {
        return hasShiftDown() ? SHIFT_STEP : 1;
    }

    private void setQty(int value) {
        qty = Mth.clamp(value, 1, PacketTerminalCraft.MAX_QTY);
    }

    private void confirm() {
        NuclearCraft.packetHandler().sendToServer(new PacketTerminalCraft(pos, target, qty));
        onClose();
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public void render(@NotNull PoseStack graphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(graphics);
        EngineersEncoderScreen.drawPanel(graphics, left, top, PANEL_W, PANEL_H);
        centered(graphics, title, top + 8, 0x404040);
        net.minecraft.client.Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(target, left + PANEL_W / 2 - 8, top + 22);
        centered(graphics, target.getHoverName(), top + 42, 0x404040);
        centered(graphics, __("gui.nc.crafter.qty", qty), top + 61, 0x202020);
        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    private void centered(@NotNull PoseStack graphics, Component text, int y, int color) {
        drawString(graphics, font, text, left + PANEL_W / 2 - font.width(text) / 2, y, color);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
