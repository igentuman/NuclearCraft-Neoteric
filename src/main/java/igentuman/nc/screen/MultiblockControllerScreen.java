package igentuman.nc.screen;

import net.minecraft.client.renderer.RenderPipelines;
import igentuman.nc.api.particle.ParticleDefinition;
import igentuman.nc.block_entity.GlobalBlockEntity;
import igentuman.nc.container.MultiblockControllerContainer;
import igentuman.nc.handler.sided.FluidCapabilityHandler;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.screen.element.Checkbox;
import igentuman.nc.screen.element.EnergyBar;
import igentuman.nc.screen.element.ProgressBar;
import igentuman.nc.screen.element.SlotWidget;
import igentuman.nc.screen.element.FluidSlotWidget;
import igentuman.nc.screen.element.FluidTankInteraction;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.setup.Registers;
import igentuman.nc.util.GuiFluidRenderer;
import igentuman.nc.util.GuiText;
import igentuman.nc.util.GuiTooltips;
import igentuman.nc.util.SlotDef;
import igentuman.nc.util.SlotsLayout;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.util.TextUtils.__;

/** Base GUI screen for a multiblock controller: renders slots, fluid tanks, energy/progress bars and a formed indicator. */
public class MultiblockControllerScreen extends AbstractContainerScreen<MultiblockControllerContainer> {

    private static final Identifier TEXTURE = rl("textures/gui/processor.png");
    protected final List<SlotWidget> slotWidgets = new ArrayList<>();
    private final List<FluidTankInteraction> fluidTankInteractions = new ArrayList<>();
    protected Identifier backgroundTexture = TEXTURE;
    protected ProgressBar progressBar;
    protected Checkbox infoCheckbox;
    protected EnergyBar  energyBar;

    public MultiblockControllerScreen(MultiblockControllerContainer menu, Inventory playerInventory, Component title) {
        this(menu, playerInventory, title, 180, 180);
    }

    protected MultiblockControllerScreen(MultiblockControllerContainer menu, Inventory playerInventory, Component title,
                                         int imageWidth, int imageHeight) {
        super(menu, playerInventory, title, imageWidth, imageHeight);
    }

    public void drawCenteredString(GuiGraphicsExtractor guiGraphics, Font font, Component text, int x, int y, int color) {
        FormattedCharSequence formattedcharsequence = text.getVisualOrderText();
        guiGraphics.text(font, formattedcharsequence, x - font.width(formattedcharsequence) / 2, y, GuiText.legacy(color), false);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;

        slotWidgets.clear();
        fluidTankInteractions.clear();
        SlotsLayout layout = menu.getLayout();
        if (layout != null) {
            SlotWidget.RELATIVE_X = leftPos;
            SlotWidget.RELATIVE_Y = topPos;
            for (int i = 0; i < layout.slots.size(); i++) {
                SlotDef slotDef = layout.slots.get(i);
                SlotWidget widget = new SlotWidget(slotDef.x, slotDef.y, 18, 18, Component.empty());

                ModEntry entry = ModEntries.get(menu.getBlockEntity().name);
                int inputItemCount = entry.itemCap() != null ? entry.itemCap().inputSlots : 0;
                int inputFluidCount = entry.fluidCap() != null ? entry.fluidCap().inputTanks.size() : 0;
                int outputItemCount = entry.itemCap() != null ? entry.itemCap().outputSlots : 0;
                int outputFluidCount = entry.fluidCap() != null ? entry.fluidCap().outputTanks.size() : 0;

                boolean isInputFluid = i >= inputItemCount && i < inputItemCount + inputFluidCount;
                int outputFluidOffset = inputItemCount + inputFluidCount + outputItemCount;
                boolean isOutputFluid = i >= outputFluidOffset && i < outputFluidOffset + outputFluidCount;

                if (isInputFluid || isOutputFluid) {
                    int tank = isInputFluid ? i - inputItemCount : i - inputItemCount - outputItemCount;
                    widget = new FluidSlotWidget(slotDef.x, slotDef.y, menu, tank);
                }
                if (slotDef.output) {
                    widget.output();
                }

                slotWidgets.add(widget);
                this.addRenderableWidget(widget);
            }
        }
        infoCheckbox = new Checkbox(leftPos + imageWidth - 20, topPos + 70, 10, menu.isFormed(), this::infoCheckboxTooltip);
        progressBar = new ProgressBar((this.width - this.imageWidth) / 2 + 73, (this.height - this.imageHeight) / 2 + 34);
        addRenderableWidget(progressBar);
        addRenderableWidget(infoCheckbox);

        GlobalBlockEntity be = menu.getBlockEntity();
        if (be.hasEnergyStorage()) {
            energyBar = new EnergyBar(leftPos + 8, topPos + 10, 8, 70, () -> menu.getBlockEntity().energyStorage);
            addRenderableWidget(energyBar);
        }
    }

    protected void addFluidTankInteraction(int x, int y, int width, int height, int tank) {
        fluidTankInteractions.add(new FluidTankInteraction(leftPos + x, topPos + y, width, height, menu, tank));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        for (FluidTankInteraction interaction : fluidTankInteractions) {
            if (interaction.mouseClicked(event, doubleClick)) return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    protected int synced(String field) {
        int idx = menu.getBlockEntity().getSyncFieldIndex(field);
        return idx >= 0 ? menu.getSyncedValue(idx) : 0;
    }

    protected int syncedArray(String field, int channel) {
        int idx = menu.getBlockEntity().getSyncFieldIndex(field, channel);
        return idx >= 0 ? menu.getSyncedValue(idx) : 0;
    }

    protected void renderParticleChannel(GuiGraphicsExtractor guiGraphics, int localMouseX, int localMouseY,
                                          int x, int y, String prefix, int channel) {
        int regId = syncedArray(prefix + "ParticleRegId", channel);
        if (regId < 0) return;
        ParticleDefinition definition = Registers.PARTICLE_DEFINITION_REGISTRY.byId(regId);
        if (definition == null) return;

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, definition.textureId(), x, y, 0, 0, 16, 16, 16, 16);

        if (localMouseX >= x && localMouseX < x + 16 && localMouseY >= y && localMouseY < y + 16) {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.translatable(definition.translationKey()));
            long amount = syncedArray(prefix + "ParticleAmount", channel);
            long energy = syncedArray(prefix + "ParticleEnergy", channel);
            double focus = syncedArray(prefix + "ParticleFocus", channel) / 1000D;
            if (amount > 0) {
                tooltip.add(__("tooltip.nuclearcraft.particlestack.amount", TextUtils.scaledFormat(amount))
                        .withStyle(ChatFormatting.GRAY));
            }
            if (energy > 0) {
                tooltip.add(__("tooltip.nuclearcraft.particlestack.energy", TextUtils.formatParticleEnergy(energy))
                        .withStyle(ChatFormatting.GRAY));
            }
            if (focus > 0) {
                tooltip.add(__("tooltip.nuclearcraft.particlestack.focus", TextUtils.numberFormat(focus))
                        .withStyle(ChatFormatting.GRAY));
            }
            GuiTooltips.components(guiGraphics, tooltip, localMouseX, localMouseY);
        }
    }

    public List<Component> infoCheckboxTooltip() {
        List<Component> tooltip = new ArrayList<>();
        if (menu.isFormed()) {
            tooltip.add(__("screen.nuclearcraft.multiblock.assembled").withStyle(ChatFormatting.GREEN));
        } else {
            __("screen.nuclearcraft.multiblock.not_assembled").withStyle(ChatFormatting.RED);
        }
        return tooltip;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float a) {
        super.extractBackground(guiGraphics, mouseX, mouseY, a);
        extractPanel(guiGraphics, mouseX, mouseY, a);
    }

    /**
     * Draws the screen panel after the vanilla dimmed background. This is the former {@code renderBg}
     * hook; subclasses override it to replace or extend the panel.
     */
    protected void extractPanel(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float a) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, backgroundTexture, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
        progressBar.setProgress(menu.getProgress());
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        super.extractLabels(guiGraphics, mouseX, mouseY);
        GlobalBlockEntity be = menu.getBlockEntity();
        if (be.hasFluidTanks()) {
            FluidCapabilityHandler tanks = be.contentHandler.getFluidHandler();
            renderFluidTanks(guiGraphics, 0, 0, tanks, false, 0, 0);
            renderFluidTanks(guiGraphics, 0, 0, tanks, true, mouseX - leftPos, mouseY - topPos);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, a);
        infoCheckbox.setChecked(menu.isFormed());
    }

    private void renderFluidTanks(GuiGraphicsExtractor guiGraphics, int x, int y,
                                   FluidCapabilityHandler tanks, boolean tooltip, int mouseX, int mouseY) {
        SlotsLayout layout = menu.getLayout();
        if (layout == null) return;

        ModEntry entry = ModEntries.get(menu.getBlockEntity().name);
        int inputItemCount  = entry.itemCap()  != null ? entry.itemCap().inputSlots        : 0;
        int inputFluidCount = entry.fluidCap() != null ? entry.fluidCap().inputTanks.size() : 0;
        int outputItemCount = entry.itemCap()  != null ? entry.itemCap().outputSlots        : 0;

        int outputFluidOffset = inputItemCount + inputFluidCount + outputItemCount;

        for (int i = 0; i < inputFluidCount && (inputItemCount + i) < layout.slots.size(); i++) {
            SlotDef def = layout.slots.get(inputItemCount + i);
            if (tooltip) {
                GuiFluidRenderer.renderFluidTooltip(guiGraphics, mouseX, mouseY,
                        x + def.x, y + def.y, 16, 16,
                        tanks.getFluidInTank(i), tanks.getTankCapacity(i));
            } else {
                GuiFluidRenderer.renderFluidTank(guiGraphics, x + def.x, y + def.y, 16, 16,
                        tanks.getFluidInTank(i), tanks.getTankCapacity(i));
            }
        }

        int outputFluidCount = entry.fluidCap() != null ? entry.fluidCap().outputTanks.size() : 0;
        for (int i = 0; i < outputFluidCount && (outputFluidOffset + i) < layout.slots.size(); i++) {
            SlotDef def = layout.slots.get(outputFluidOffset + i);
            int tankIndex = inputFluidCount + i;
            if (tooltip) {
                GuiFluidRenderer.renderFluidTooltip(guiGraphics, mouseX, mouseY,
                        x + def.x, y + def.y, 16, 16,
                        tanks.getFluidInTank(tankIndex), tanks.getTankCapacity(tankIndex));
            } else {
                GuiFluidRenderer.renderFluidTank(guiGraphics, x + def.x, y + def.y, 16, 16,
                        tanks.getFluidInTank(tankIndex), tanks.getTankCapacity(tankIndex));
            }
        }
    }
}
