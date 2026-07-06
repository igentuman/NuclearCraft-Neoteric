package igentuman.nc.screen;

import igentuman.nc.block_entity.GlobalBlockEntity;
import igentuman.nc.container.UniversalProcessorContainer;
import igentuman.nc.network.PacketProcessorButtonPress;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.screen.element.EnergyBar;
import igentuman.nc.screen.element.ProcessorImageButton;
import igentuman.nc.screen.element.ProgressBar;
import igentuman.nc.screen.element.SlotWidget;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.GuiFluidRenderer;
import igentuman.nc.util.SlotDef;
import igentuman.nc.util.SlotsLayout;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import igentuman.nc.handler.sided.FluidCapabilityHandler;
import igentuman.nc.compat.emi.EmiHelper;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.util.TextUtils.__;

public class UniversalProcessorScreen extends AbstractContainerScreen<UniversalProcessorContainer> {

    private static final ResourceLocation TEXTURE = rl("textures/gui/processor.png");
    private static final int U_SIDE_CONFIG    = 220;
    private static final int V_SIDE_CONFIG    = 220;
    private static final int U_REDSTONE       = 184;
    private static final int V_REDSTONE_BASE  = 220;
    private static final int U_RECIPES        = 220;
    private static final int V_RECIPES        = 76;

    private final List<SlotWidget> slotWidgets = new ArrayList<>();
    private ProgressBar progressBar;

    public UniversalProcessorScreen(UniversalProcessorContainer menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = 180;
        imageHeight = 180;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;

        slotWidgets.clear();
        SlotsLayout layout = menu.getLayout();
        int progressBarOffset = 72;
        if (layout != null) {
            SlotWidget.RELATIVE_X = leftPos;
            SlotWidget.RELATIVE_Y = topPos;

            ModEntry entry = ModEntries.get(menu.getBlockEntity().name);
            int inputItemCount = entry.itemCap() != null ? entry.itemCap().inputSlots : 0;
            int inputFluidCount = entry.fluidCap() != null ? entry.fluidCap().inputTanks.size() : 0;
            int outputItemCount = entry.itemCap() != null ? entry.itemCap().outputSlots : 0;
            int outputFluidsCount = entry.fluidCap() != null ? entry.fluidCap().outputTanks.size() : 0;

            for (int i = 0; i < layout.slots.size(); i++) {
                SlotDef slotDef = layout.slots.get(i);
                SlotWidget widget = new SlotWidget(slotDef.x, slotDef.y, 18, 18, Component.empty());

                boolean isInputFluid = i >= inputItemCount && i < inputItemCount + inputFluidCount;
                boolean isOutputFluid = i >= inputItemCount + inputFluidCount + outputItemCount;

                if (isInputFluid || isOutputFluid) {
                    widget.fluid();
                }
                if (slotDef.output) {
                    widget.output();
                }

                slotWidgets.add(widget);
                this.addRenderableWidget(widget);
            }
            if (inputFluidCount + inputItemCount > 4) {
                progressBarOffset += 20;
            }
            if (outputItemCount + outputFluidsCount > 4) {
                progressBarOffset -= 20;
            }
        }

        ModEntry progressEntry = ModEntries.get(menu.getBlockEntity().name);
        int barIndex = progressEntry != null ? progressEntry.progressBar() : 0;
        progressBar = new ProgressBar((this.width - this.imageWidth) / 2 + progressBarOffset, (this.height - this.imageHeight) / 2 + 30, barIndex);
        addRenderableWidget(progressBar);

        GlobalBlockEntity be = menu.getBlockEntity();
        if (be.hasEnergyStorage()) {
            int barX = leftPos + 8;
            int barY = topPos + 10;
            addRenderableWidget(new EnergyBar(barX, barY, () -> menu.getBlockEntity().energyStorage));
        }

        addRenderableWidget(new ProcessorImageButton(
                leftPos + imageWidth - 48, topPos + 74,
                U_SIDE_CONFIG, V_SIDE_CONFIG,
                () -> Minecraft.getInstance().setScreen(new SideConfigSlotSelectionScreen(this)),
                List.of(__("screen.nuclearcraft.side_config"))
        ));

        addRenderableWidget(new ProcessorImageButton(
                leftPos + imageWidth - 28, topPos + 74,
                () -> U_REDSTONE,
                () -> V_REDSTONE_BASE - menu.getRedstoneMode() * 36,
                () -> PacketDistributor.sendToServer(new PacketProcessorButtonPress(
                        menu.getPosition(), PacketProcessorButtonPress.REDSTONE_BTN_ID)),
                () -> List.of(__("screen.nuclearcraft.redstone_config_" + menu.getRedstoneMode()))
        ));

        if (progressEntry != null && progressEntry.hasRecipes() && progressEntry.hasItem()) {
            Runnable callback = () -> {return;};
            if (ModList.get().isLoaded("emi")) {
                ItemStack workstation = new ItemStack(progressEntry.item().get());
                callback = () -> EmiHelper.displayRecipes(workstation);
            }

            addRenderableWidget(new ProcessorImageButton(
                    leftPos + imageWidth - 68, topPos + 74,
                    U_RECIPES, V_RECIPES,
                    callback,
                    List.of(__("screen.nuclearcraft.show_recipes"))
            ));
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
        progressBar.setProgress(menu.getProgress());
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        GlobalBlockEntity be = menu.getBlockEntity();
        if (be.hasFluidTanks()) {
            FluidCapabilityHandler tanks = be.contentHandler.getFluidHandler();
            renderFluidTanks(guiGraphics, 0, 0, tanks, false, 0, 0);
            renderFluidTanks(guiGraphics, 0, 0, tanks, true, mouseX - leftPos, mouseY - topPos);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private void renderFluidTanks(GuiGraphics guiGraphics, int x, int y,
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

    public String getRecipeTypeName() {
        return menu.getBlockEntity().name;
    }
}
