package igentuman.nc.screen;

import igentuman.nc.container.MultiblockControllerContainer;
import igentuman.nc.network.PacketHeatExchangerToggleRadiators;
import igentuman.nc.screen.element.HeatBar;
import igentuman.nc.screen.element.RadiatorToggleButton;
import igentuman.nc.setup.ModEntries;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.util.TextUtils.__;

public class HeatExchangerControllerScreen extends MultiblockControllerScreen {

    private HeatBar heatBar;
    private RadiatorToggleButton radiatorToggle;

    public HeatExchangerControllerScreen(MultiblockControllerContainer menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        heatBar = new HeatBar(leftPos + 18, topPos + 10, 8, 70, () -> menu.getBlockEntity().heatBuffer());
        addRenderableWidget(heatBar);
        radiatorToggle = new RadiatorToggleButton(
                leftPos + 30, topPos + 66,
                ModEntries.get("heat_exchanger_radiator").block().toStack(),
                this::radiatorsEnabled,
                () -> PacketDistributor.sendToServer(new PacketHeatExchangerToggleRadiators(menu.getPosition())),
                this::radiatorToggleTooltip
        );
        addRenderableWidget(radiatorToggle);
        progressBar.visible = false;
    }

    private boolean radiatorsEnabled() {
        return synced("radiatorsEnabled") != 0;
    }

    private List<Component> radiatorToggleTooltip() {
        return List.of(__("screen.nuclearcraft.heat_exchanger.radiator_toggle." + (radiatorsEnabled() ? "disable" : "enable")));
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        radiatorToggle.visible = menu.isFormed();
        if (!menu.isFormed()) return;

        int x = 70;
        int y = 20;
        line(guiGraphics, x, y, __("screen.nuclearcraft.heat_exchanger.blocks", synced("heatExchangers")));
        line(guiGraphics, x, y += 11, __("screen.nuclearcraft.heat_exchanger.hot_cycle", synced("hotCycleOps")));
        line(guiGraphics, x, y += 11, __("screen.nuclearcraft.heat_exchanger.cold_cycle", synced("coldCycleOps")));
    }

    @Override
    public List<Component> infoCheckboxTooltip() {
        List<Component> tooltip = new ArrayList<>();
        if (!menu.isFormed()) {
            tooltip.add(__("screen.nuclearcraft.multiblock.not_assembled").withStyle(ChatFormatting.RED));
            return tooltip;
        }
        tooltip.add(__("screen.nuclearcraft.multiblock.assembled").withStyle(ChatFormatting.GREEN));
        tooltip.add(__("screen.nuclearcraft.heat_exchanger.blocks", synced("heatExchangers")).withStyle(ChatFormatting.GOLD));
        tooltip.add(__("screen.nuclearcraft.heat_exchanger.radiators", synced("radiators")).withStyle(ChatFormatting.GOLD));
        tooltip.add(__("screen.nuclearcraft.heat_exchanger.hot_cycle", synced("hotCycleOps")).withStyle(ChatFormatting.GOLD));
        tooltip.add(__("screen.nuclearcraft.heat_exchanger.cold_cycle", synced("coldCycleOps")).withStyle(ChatFormatting.GOLD));
        return tooltip;
    }

    private void line(GuiGraphics g, int x, int y, Component text) {
        float scale = 0.75f;
        g.pose().pushPose();
        g.pose().scale(scale, scale, 1f);
        g.drawString(font, text, (int) (x / scale), (int) (y / scale), 0x404040, false);
        g.pose().popPose();
    }

    private int synced(String field) {
        int idx = menu.getBlockEntity().getSyncFieldIndex(field);
        return idx >= 0 ? menu.getSyncedValue(idx) : 0;
    }
}
