package igentuman.nc.screen;

import net.minecraft.client.renderer.RenderPipelines;
import igentuman.nc.container.MultiblockControllerContainer;
import igentuman.nc.block_entity.accelerator.AbstractAcceleratorControllerBE;
import igentuman.nc.handler.sided.FluidCapabilityHandler;
import igentuman.nc.screen.element.AcceleratorThermalBar;
import igentuman.nc.screen.element.Checkbox;
import igentuman.nc.screen.element.EnergyBar;
import igentuman.nc.screen.element.FluidTankBar;
import igentuman.nc.setup.Registers;
import igentuman.nc.api.particle.ParticleDefinition;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import igentuman.nc.util.GuiTooltips;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.TextUtils.formatParticleEnergy;
import static igentuman.nc.util.TextUtils.numberFormat;
import static igentuman.nc.util.TextUtils.scaledFormat;

public abstract class AbstractAcceleratorControllerScreen extends MultiblockControllerScreen {

    private static final Identifier TEXTURE = rl("textures/gui/accelerators/linear_controller.png");

    private final boolean compactLayout;
    private AcceleratorThermalBar thermalBar;
    private FluidTankBar coolantBar;
    private Checkbox statusCheckbox;

    protected AbstractAcceleratorControllerScreen(MultiblockControllerContainer menu, Inventory inventory,
                                                   Component title, boolean compactLayout) {
        super(menu, inventory, title, compactLayout ? 196 : 180, compactLayout ? 109 : 180);
        this.compactLayout = compactLayout;
        if (compactLayout) {
            titleLabelY = 6;
            inventoryLabelY = imageHeight + 1;
            backgroundTexture = TEXTURE;
        }
    }

    protected abstract int coolantInputTank();

    @Override
    protected void init() {
        super.init();
        if (!compactLayout) {
            thermalBar = new AcceleratorThermalBar(leftPos + 18, topPos + 10, 8, 70,
                    () -> synced("temperatureK"), () -> synced("maximumTemperatureK"),
                    () -> synced("overheated") != 0, () -> synced("overheatCooldownTicks"));
            addRenderableWidget(thermalBar);
            coolantBar = new FluidTankBar(leftPos + 28, topPos + 10, 8, 70, this::coolant, this::coolantCapacity).interactive(menu, coolantInputTank());
            addRenderableWidget(coolantBar);
            progressBar.visible = false;
            return;
        }
        clearWidgets();

        addRenderableWidget(new EnergyBar(leftPos + 7, topPos + 20, 8, 82,
                () -> menu.getBlockEntity().energyStorage));
        addRenderableWidget(new AcceleratorThermalBar(leftPos + 17, topPos + 20, 8, 82,
                () -> synced("temperatureK"), () -> synced("maximumTemperatureK"),
                () -> synced("overheated") != 0, () -> synced("overheatCooldownTicks")));
        addRenderableWidget(new FluidTankBar(leftPos + 27, topPos + 20, 8, 82,
                this::coolant, this::coolantCapacity).interactive(menu, coolantInputTank()));

        statusCheckbox = new Checkbox(leftPos + imageWidth - 19, topPos + 80, 11,
                menu.isFormed(), this::infoCheckboxTooltip);
        addRenderableWidget(statusCheckbox);
    }

    private FluidStack coolant() {
        FluidCapabilityHandler tanks = menu.getBlockEntity().contentHandler.getFluidHandler();
        int tank = coolantInputTank();
        return tanks != null && tank < tanks.getTanks() ? tanks.getFluidInTank(tank) : FluidStack.EMPTY;
    }

    private int coolantCapacity() {
        FluidCapabilityHandler tanks = menu.getBlockEntity().contentHandler.getFluidHandler();
        int tank = coolantInputTank();
        return tanks != null && tank < tanks.getTanks() ? tanks.getTankCapacity(tank) : 0;
    }

    @Override
    protected void extractPanel(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float a) {
        if (!compactLayout) {
            super.extractPanel(guiGraphics, mouseX, mouseY, a);
            return;
        }
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, backgroundTexture, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        if (!compactLayout) {
            super.extractLabels(guiGraphics, mouseX, mouseY);
            if (!menu.isFormed()) return;
            int y = 20;
            line(guiGraphics, 70, y, __("screen.nuclearcraft.accelerator.control_signal", synced("controlSignal")));
            for (Component stat : statLines()) {
                y += 9;
                line(guiGraphics, 70, y, stat);
            }
            return;
        }
        drawCenteredString(guiGraphics, font, title, imageWidth / 2, titleLabelY, 0xFFFFFF);
        renderParticle(guiGraphics, mouseX, mouseY);
        if (!menu.isFormed()) return;
        int y = 40;
        for (Component stat : machineLines()) {
            guiGraphics.text(font, stat, 37, y, 0xFFFFFFFF, false);
            y += 10;
        }
    }

    protected List<Component> machineLines() {
        List<Component> lines = new ArrayList<>();
        lines.add(__("screen.nuclearcraft.accelerator.maximum_temperature",
                scaledFormat(synced("maximumTemperatureK"))));
        lines.add(__("screen.nuclearcraft.accelerator.current_temperature",
                scaledFormat(synced("temperatureK"))));
        lines.add(__("screen.nuclearcraft.accelerator.voltage", scaledFormat(synced("voltage"))));
        lines.add(__("screen.nuclearcraft.accelerator.magnet_strength",
                numberFormat(synced("quadrupoleField") + synced("dipoleField"))));
        if (synced("overheated") != 0) {
            lines.add(__("screen.nuclearcraft.accelerator.overheated", synced("overheatCooldownTicks"))
                    .withStyle(ChatFormatting.RED));
        }
        return lines;
    }

    private void renderParticle(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        AbstractAcceleratorControllerBE controller = (AbstractAcceleratorControllerBE) menu.getBlockEntity();
        if (controller.particleAmount <= 0) return;
        ParticleDefinition definition = Registers.PARTICLE_DEFINITION_REGISTRY.byId(controller.particleTypeId);
        if (definition == null) return;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, definition.textureId(), 40, 21, 0, 0, 16, 16, 16, 16);
        int localMouseX = mouseX - leftPos;
        int localMouseY = mouseY - topPos;
        if (localMouseX >= 40 && localMouseX < 56 && localMouseY >= 21 && localMouseY < 37) {
            GuiTooltips.components(guiGraphics, List.of(
                    Component.translatable(definition.translationKey()),
                    __("tooltip.nuclearcraft.particlestack.amount", controller.particleAmount)
                            .withStyle(ChatFormatting.GRAY),
                    __("tooltip.nuclearcraft.particlestack.energy", formatParticleEnergy(controller.particleEnergyKeV))
                            .withStyle(ChatFormatting.GRAY),
                    __("tooltip.nuclearcraft.particlestack.focus",
                            numberFormat(controller.particleFocusScaled / 10_000D))
                            .withStyle(ChatFormatting.GRAY)
            ), localMouseX, localMouseY);
        }
    }

    protected List<Component> statLines() {
        List<Component> lines = new ArrayList<>();
        lines.add(__("screen.nuclearcraft.accelerator.voltage", synced("voltage")));
        lines.add(__("screen.nuclearcraft.accelerator.beam_length", synced("beamLength")));
        lines.add(__("screen.nuclearcraft.accelerator.quadrupole_field", synced("quadrupoleField")));
        AbstractAcceleratorControllerBE controller = (AbstractAcceleratorControllerBE) menu.getBlockEntity();
        if (controller.particleAmount > 0) {
            lines.add(__("screen.nuclearcraft.accelerator.particle_beam",
                    controller.particleAmount, formatParticleEnergy(controller.particleEnergyKeV)));
        }
        if (synced("overheated") != 0) {
            lines.add(__("screen.nuclearcraft.accelerator.overheated", synced("overheatCooldownTicks"))
                    .copy().withStyle(ChatFormatting.RED));
        }
        return lines;
    }

    @Override
    public List<Component> infoCheckboxTooltip() {
        List<Component> tooltip = new ArrayList<>();
        if (!menu.isFormed()) {
            tooltip.add(__("screen.nuclearcraft.multiblock.not_assembled").withStyle(ChatFormatting.RED));
            return tooltip;
        }
        tooltip.add(__("screen.nuclearcraft.multiblock.assembled").withStyle(ChatFormatting.GREEN));
        tooltip.add(__("screen.nuclearcraft.accelerator.control_signal", synced("controlSignal")).withStyle(ChatFormatting.GOLD));
        for (Component stat : statLines()) {
            tooltip.add(stat.copy().withStyle(ChatFormatting.GOLD));
        }
        return tooltip;
    }

    private void line(GuiGraphicsExtractor guiGraphics, int x, int y, Component text) {
        float scale = 0.75f;
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().scale(scale, scale);
        guiGraphics.text(font, text, (int) (x / scale), (int) (y / scale), 0xFF404040, false);
        guiGraphics.pose().popMatrix();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float a) {
        if (compactLayout) {
            statusCheckbox.setChecked(menu.isFormed());
        }
        super.extractRenderState(guiGraphics, mouseX, mouseY, a);
    }
}
