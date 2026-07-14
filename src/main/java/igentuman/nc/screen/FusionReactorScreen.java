package igentuman.nc.screen;

import igentuman.nc.block_entity.fusion.FusionReactorControllerBE;
import igentuman.nc.container.FusionReactorContainer;
import igentuman.nc.container.MultiblockControllerContainer;
import igentuman.nc.screen.element.CoolantBar;
import igentuman.nc.screen.element.FluidTankBar;
import igentuman.nc.screen.element.FusionAmplifierSlider;
import igentuman.nc.screen.element.TemperatureBar;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.util.TextUtils.__;

/** Fusion reactor controller screen with coolant/temperature/fuel/product bars, an amplifier slider and status readouts. */
public class FusionReactorScreen extends MultiblockControllerScreen {

    private static final ResourceLocation TEXTURE = rl("textures/gui/fusion_reactor.png");
    private static final int PANEL_TEXT = 0xFFFFFF;

    private static final int BAR_WIDTH = 8;
    private static final int BAR_HEIGHT = 70;
    private static final int BAR_HALF_HEIGHT = 35;
    private static final int BAR_TOP = 10;
    private static final int OUTPUT_LEFT = 191;
    private static final int OUTPUT_TOP = 55;

    private FusionAmplifierSlider amplifierSlider;

    public FusionReactorScreen(MultiblockControllerContainer menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = 214;
        imageHeight = 186;
    }

    @Override
    protected void init() {
        super.init();
        amplifierSlider = new FusionAmplifierSlider(leftPos + 77, topPos + 42, 100, 10,
                menu.getPosition(), () -> be() != null ? be().amplificationAdjustment : 50);
        addRenderableWidget(amplifierSlider);
        progressBar.visible = false;
        infoCheckbox.setX(leftPos+6);
        infoCheckbox.setY(topPos + 112);

        addFluidBars();
    }

    /** Coolant + temperature next to the energy bar, fuel inputs beside them, products on the right. */
    private void addFluidBars() {
        FusionReactorContainer c = fusionMenu();
        int w = BAR_WIDTH;
        int top = topPos + BAR_TOP;

        // Full-height bars next to the energy bar (energy sits at leftPos + 8).
        addRenderableWidget(new CoolantBar(leftPos + 18, top, w, BAR_HEIGHT,
                () -> c.fluid(FusionReactorContainer.TANK_COOLANT),
                () -> c.fluid(FusionReactorContainer.TANK_HOT_COOLANT),
                () -> c.capacity(FusionReactorContainer.TANK_COOLANT)));
        addRenderableWidget(new TemperatureBar(leftPos + 28, top, w, BAR_HEIGHT,
                c::reactorHeat, c::maxHeat));

        // Input fuel bars: same width, half height.
        addRenderableWidget(new FluidTankBar(leftPos + 38, top, w, BAR_HEIGHT,
                () -> c.fluid(FusionReactorContainer.TANK_FUEL_A),
                () -> c.capacity(FusionReactorContainer.TANK_FUEL_A)));
        addRenderableWidget(new FluidTankBar(leftPos + 48, top, w, BAR_HEIGHT,
                () -> c.fluid(FusionReactorContainer.TANK_FUEL_B),
                () -> c.capacity(FusionReactorContainer.TANK_FUEL_B)));

        // Output product bars on the right: same width, half height.
        int outY = topPos + BAR_TOP;
        for (int i = 0; i < FusionReactorContainer.PRODUCT_COUNT; i++) {
            int tank = FusionReactorContainer.TANK_PRODUCT_FIRST + i;
            int left = i * 10;
            if(i > 1) {
                left -= 20;
                outY =  topPos + BAR_HALF_HEIGHT + 12;
            }
            addRenderableWidget(new FluidTankBar(leftPos + OUTPUT_LEFT + left, outY, w, BAR_HALF_HEIGHT,
                    () -> c.fluid(tank),
                    () -> c.capacity(tank)));
        }
    }

    private FusionReactorContainer fusionMenu() {
        return (FusionReactorContainer) menu;
    }

    private FusionReactorControllerBE be() {
        return menu.getBlockEntity() instanceof FusionReactorControllerBE be ? be : null;
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        drawCenteredString(guiGraphics, this.font, __("screen.nuclearcraft.fusion_reactor"), imageWidth / 2 + 20, this.titleLabelY, PANEL_TEXT);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY+8, 4210752, false);

        FusionReactorControllerBE be = be();
        if (be == null) return;

        int cx = imageWidth / 2 + 20;
        drawCenteredString(guiGraphics, this.font, __("screen.nuclearcraft.fusion.rf_amplifiers", be.rfAmplificationRatio), cx, 20, PANEL_TEXT);
        drawCenteredString(guiGraphics, this.font, __("screen.nuclearcraft.fusion.rf_adjustment", be.amplificationAdjustment), cx, 30, PANEL_TEXT);
        if (be.functionalBlocksCharge < 100) {
            drawCenteredString(guiGraphics, this.font, __("screen.nuclearcraft.fusion.charging", be.functionalBlocksCharge), cx, 56, PANEL_TEXT);
        }
        if (be.running) {
            drawCenteredString(guiGraphics, this.font, __("screen.nuclearcraft.fusion.efficiency", (int) (be.efficiency * 100)), cx, 66, PANEL_TEXT);
            drawCenteredString(guiGraphics, this.font, __("screen.nuclearcraft.fusion.output", be.energyPerTick), cx, 76, PANEL_TEXT);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }
}
