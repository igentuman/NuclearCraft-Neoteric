package igentuman.nc.screen;

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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.TextUtils.formatParticleEnergy;
import static igentuman.nc.util.TextUtils.numberFormat;
import static igentuman.nc.util.TextUtils.scaledFormat;

public abstract class AbstractAcceleratorControllerScreen extends MultiblockControllerScreen {

    private static final ResourceLocation TEXTURE = rl("textures/gui/accelerators/linear_controller.png");

    private final boolean compactLayout;
    private AcceleratorThermalBar thermalBar;
    private FluidTankBar coolantBar;
    private Checkbox statusCheckbox;

    protected AbstractAcceleratorControllerScreen(MultiblockControllerContainer menu, Inventory inventory,
                                                   Component title, boolean compactLayout) {
        super(menu, inventory, title);
        this.compactLayout = compactLayout;
        if (compactLayout) {
            imageWidth = 196;
            imageHeight = 109;
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
            coolantBar = new FluidTankBar(leftPos + 28, topPos + 10, 8, 70, this::coolant, this::coolantCapacity);
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
                this::coolant, this::coolantCapacity));

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
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        if (!compactLayout) {
            super.renderBg(guiGraphics, partialTick, mouseX, mouseY);
            return;
        }
        guiGraphics.blit(backgroundTexture, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!compactLayout) {
            super.renderLabels(guiGraphics, mouseX, mouseY);
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
            guiGraphics.drawString(font, stat, 37, y, 0xFFFFFF, false);
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

    private void renderParticle(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (synced("particleAmount") <= 0) return;
        ParticleDefinition definition = Registers.PARTICLE_DEFINITION_REGISTRY.byId(synced("particleTypeId"));
        if (definition == null) return;
        guiGraphics.blit(definition.textureId(), 40, 21, 0, 0, 16, 16, 16, 16);
        int localMouseX = mouseX - leftPos;
        int localMouseY = mouseY - topPos;
        if (localMouseX >= 40 && localMouseX < 56 && localMouseY >= 21 && localMouseY < 37) {
            guiGraphics.renderComponentTooltip(font, List.of(
                    Component.translatable(definition.translationKey()),
                    __("tooltip.nuclearcraft.particlestack.amount", synced("particleAmount"))
                            .withStyle(ChatFormatting.GRAY),
                    __("tooltip.nuclearcraft.particlestack.energy", formatParticleEnergy(particleEnergyKeV()))
                            .withStyle(ChatFormatting.GRAY),
                    __("tooltip.nuclearcraft.particlestack.focus",
                            numberFormat(synced("particleFocusScaled") / 10_000D))
                            .withStyle(ChatFormatting.GRAY)
            ), localMouseX, localMouseY);
        }
    }

    protected List<Component> statLines() {
        List<Component> lines = new ArrayList<>();
        lines.add(__("screen.nuclearcraft.accelerator.voltage", synced("voltage")));
        lines.add(__("screen.nuclearcraft.accelerator.beam_length", synced("beamLength")));
        lines.add(__("screen.nuclearcraft.accelerator.quadrupole_field", synced("quadrupoleField")));
        if (synced("particleAmount") != 0) {
            lines.add(__("screen.nuclearcraft.accelerator.particle_beam",
                    synced("particleAmount"), formatParticleEnergy(particleEnergyKeV())));
        }
        if (synced("overheated") != 0) {
            lines.add(__("screen.nuclearcraft.accelerator.overheated", synced("overheatCooldownTicks"))
                    .copy().withStyle(ChatFormatting.RED));
        }
        return lines;
    }

    private long particleEnergyKeV() {
        return ((AbstractAcceleratorControllerBE) menu.getBlockEntity()).particleEnergyKeV;
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

    private void line(GuiGraphics guiGraphics, int x, int y, Component text) {
        float scale = 0.75f;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, 1f);
        guiGraphics.drawString(font, text, (int) (x / scale), (int) (y / scale), 0x404040, false);
        guiGraphics.pose().popPose();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (compactLayout) {
            statusCheckbox.setChecked(menu.isFormed());
        }
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}
