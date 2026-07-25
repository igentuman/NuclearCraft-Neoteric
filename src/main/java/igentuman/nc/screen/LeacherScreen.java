package igentuman.nc.screen;

import igentuman.nc.block_entity.LeacherBE;
import igentuman.nc.block_entity.PumpBE;
import igentuman.nc.container.UniversalProcessorContainer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import static igentuman.nc.block_entity.LeacherBE.*;
import static igentuman.nc.util.TextUtils.__;

public class LeacherScreen extends UniversalProcessorScreen {

    private static final int PUMP_SIZE = 10;
    private static final int PUMP_Y = 61;
    private static final int PUMP_X0 = 29;
    private static final int PUMP_STEP = 13;
    private static final int COLOR_VALID = 0xFF00AA00;
    private static final int COLOR_INVALID = 0xFFAA0000;
    private static final int COLOR_BORDER = 0xFF333333;

    public LeacherScreen(UniversalProcessorContainer menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    public PumpBE[] getPumps() {
        return ((LeacherBE) menu.getBlockEntity()).getPumpsForClient();
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        LeacherBE be = (LeacherBE) getMenu().getBlockEntity();
        switch (be.leacherState) {
            case WRONG_POSITION -> graphics.drawString(font, __("nc.label.leacher_wrong_position"), 30, 16, 0xFF0000);
            case NO_SOURCE      -> graphics.drawString(font, __("nc.label.leacher_no_source"), 30, 16, 0xFF0000);
            case NO_ACID        -> graphics.drawString(font, __("nc.label.leacher_no_acid"), 30, 16, 0xFF0000);
            case PUMPS_ERROR    -> graphics.drawString(font, __("nc.label.leacher_pumps_error"), 30, 16, 0xFF0000);
        }
        renderPumpIndicators(graphics, mouseX, mouseY);
    }

    private void renderPumpIndicators(GuiGraphics graphics, int mouseX, int mouseY) {
        PumpBE[] pumps = getPumps();
        graphics.drawString(font, Component.literal("Pumps:"), PUMP_X0, PUMP_Y - 10, 0x404040, false);
        int relX = mouseX - leftPos;
        int relY = mouseY - topPos;
        for (int i = 0; i < 4; i++) {
            int x = PUMP_X0 + i * PUMP_STEP;
            boolean valid = pumps != null && i < pumps.length && pumps[i] != null;
            graphics.fill(x - 1, PUMP_Y - 1, x + PUMP_SIZE + 1, PUMP_Y + PUMP_SIZE + 1, COLOR_BORDER);
            graphics.fill(x, PUMP_Y, x + PUMP_SIZE, PUMP_Y + PUMP_SIZE, valid ? COLOR_VALID : COLOR_INVALID);
            if (relX >= x && relX < x + PUMP_SIZE && relY >= PUMP_Y && relY < PUMP_Y + PUMP_SIZE) {
                String key = valid ? "leacher.tooltip.valid_pump" : "leacher.tooltip.invalid_pump";
                graphics.renderTooltip(font, __(key), mouseX, mouseY);
            }
        }
    }
}
