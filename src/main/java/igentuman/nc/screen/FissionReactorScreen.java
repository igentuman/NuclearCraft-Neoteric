package igentuman.nc.screen;

import igentuman.nc.container.MultiblockControllerContainer;
import igentuman.nc.network.PacketFissionToggleMode;
import igentuman.nc.screen.element.BoilingBar;
import igentuman.nc.screen.element.HeatBar;
import igentuman.nc.screen.element.ProcessorImageButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.util.TextUtils.__;

/** Fission reactor controller screen adding heat/boiling bars, reactivity readout and an energy/steam mode toggle. */
public class FissionReactorScreen extends MultiblockControllerScreen {

    private static final ResourceLocation TEXTURE = rl("textures/gui/fission_controller.png");

    private ProcessorImageButton modeButton;
    private BoilingBar boilingBar;

    public FissionReactorScreen(MultiblockControllerContainer menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new HeatBar(leftPos + 18, topPos + 10, 8, 70, () -> menu.getBlockEntity().heatBuffer()));
        modeButton = new ProcessorImageButton(
                leftPos + 152, topPos + 50,
                () -> 220,
                () -> isSteamMode() ? 112 : 148,
                () -> PacketDistributor.sendToServer(new PacketFissionToggleMode(menu.getPosition())),
                List::of
        );
        boilingBar = new BoilingBar(leftPos + 8, topPos + 10, 8, 70, () -> menu.getBlockEntity().boilingBuffer());
        addRenderableWidget(boilingBar);
        addRenderableWidget(modeButton);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        drawCenteredString(guiGraphics, this.font, __("screen.nuclearcraft.fission_reactor"), imageWidth/2, this.titleLabelY, 4210752);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);

        modeButton.visible = menu.isFormed();
        if (!menu.isFormed()) return;

        boolean steam = isSteamMode();
        int x = 38;
        int y = 38;
        line(guiGraphics, x, y + 20, "screen.nuclearcraft.fission.reactivity", synced("reactivity") + "%");
        if (synced("toggleTimer") < 2000) {
            modeButton.setTooltip(Tooltip.create(__("tooltip.nuclearcraft.wait", synced("toggleTimer")/20)));
        } else {
            if(steam) {
                modeButton.setTooltip(Tooltip.create(__("tooltip.nuclearcraft.switch_to_energy")));
            } else {
                modeButton.setTooltip(Tooltip.create(__("tooltip.nuclearcraft.switch_to_boiling")));
            }
        }
    }

    private boolean isSteamMode() {
        return synced("steamMode") != 0;
    }

    private void line(GuiGraphics g, int x, int y, String key, String value) {
        g.drawString(font, Component.translatable(key).append(": " + value).withStyle(ChatFormatting.DARK_GRAY),
                x, y, 0x404040, false);
    }

    private int synced(String field) {
        int idx = menu.getBlockEntity().getSyncFieldIndex(field);
        return idx >= 0 ? menu.getSyncedValue(idx) : 0;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
        progressBar.setProgress(menu.getProgress());
        energyBar.visible = !isSteamMode();
        boilingBar.visible = isSteamMode();
    }
}
