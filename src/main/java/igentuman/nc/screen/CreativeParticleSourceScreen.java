package igentuman.nc.screen;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.minecraft.client.renderer.RenderPipelines;
import igentuman.nc.block_entity.particle.CreativeParticleSourceBE;
import igentuman.nc.container.UniversalProcessorContainer;
import igentuman.nc.network.particle.CreativeParticleSourceSettingsPayload;
import igentuman.nc.screen.element.ParticleSelector;
import igentuman.nc.screen.element.ScaleDropdown;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import static igentuman.nc.NuclearCraft.rl;

public class CreativeParticleSourceScreen extends AbstractContainerScreen<UniversalProcessorContainer> {

    private static final Identifier TEXTURE = rl("textures/gui/processor.png");

    private ParticleSelector particleSelector;
    private ScaleDropdown scaleDropdown;
    private EditBox focusField;
    private EditBox energyField;
    private Identifier selectedParticle;
    private double focusValue = 1D;
    private double energyValue = 1D;
    private int scaleIndex = 1;
    private int dropdownMouseButton = -1;

    public CreativeParticleSourceScreen(UniversalProcessorContainer menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 180, 180);
    }

    @Override
    protected void init() {
        super.init();
        if (menu.getBlockEntity() instanceof CreativeParticleSourceBE source) {
            selectedParticle = source.particleId();
            focusValue = source.focus();
            energyValue = source.energyValue();
            scaleIndex = source.energyScale();
        }

        particleSelector = addRenderableWidget(new ParticleSelector(leftPos + 8, topPos + 20, 162, 16)
                .setSelected(selectedParticle)
                .setOnSelect(particleId -> {
                    selectedParticle = particleId;
                    sendUpdate();
                }));

        focusField = numericField(leftPos + 50, topPos + 42, 118, 14);
        focusField.setValue(format(focusValue));
        focusField.setResponder(value -> {
            Double parsed = parseNonNegative(value);
            if (parsed != null) {
                focusValue = parsed;
                sendUpdate();
            }
        });
        addRenderableWidget(focusField);

        energyField = numericField(leftPos + 50, topPos + 62, 80, 14);
        energyField.setValue(format(energyValue));
        energyField.setResponder(value -> {
            Double parsed = parseNonNegative(value);
            if (parsed != null && validEnergy(parsed, scaleIndex)) {
                energyValue = parsed;
                sendUpdate();
            }
        });
        addRenderableWidget(energyField);

        scaleDropdown = addRenderableWidget(new ScaleDropdown(leftPos + 134, topPos + 62, 34, 16)
                .setSelectedIndex(scaleIndex)
                .setOnSelect(index -> {
                    if (validEnergy(energyValue, index)) {
                        scaleIndex = index;
                        sendUpdate();
                    } else {
                        scaleDropdown.setSelectedIndex(scaleIndex);
                    }
                }));
    }

    private EditBox numericField(int x, int y, int width, int height) {
        EditBox field = new EditBox(font, x, y, width, height, Component.empty());
        field.setMaxLength(32);
        field.setTextColor(0xFFFFFFFF);
        field.setFilter(value -> value.isEmpty() || value.matches("\\d*(\\.\\d*)?"));
        return field;
    }

    private static Double parseNonNegative(String value) {
        if (value.isEmpty()) return null;
        try {
            double parsed = Double.parseDouble(value);
            return Double.isFinite(parsed) && parsed >= 0D ? parsed : null;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static boolean validEnergy(double value, int scale) {
        try {
            CreativeParticleSourceBE.energyKeV(value, scale);
            return true;
        } catch (ArithmeticException | IllegalArgumentException ignored) {
            return false;
        }
    }

    private void sendUpdate() {
        if (selectedParticle == null || !validEnergy(energyValue, scaleIndex)) return;
        ClientPacketDistributor.sendToServer(new CreativeParticleSourceSettingsPayload(
                menu.getPosition(), selectedParticle, focusValue, energyValue, scaleIndex));
    }

    private static String format(double value) {
        if (Double.isFinite(value) && value == Math.rint(value)
                && value >= Long.MIN_VALUE && value <= Long.MAX_VALUE) {
            return Long.toString((long) value);
        }
        return Double.toString(value);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.centeredText(font, title, imageWidth / 2, titleLabelY, 0xFFFFFFFF);
        graphics.text(font, Component.translatable("gui.nuclearcraft.creative_particle_source.focus"),
                10, 45, 0xFF404040, false);
        graphics.text(font, Component.translatable("gui.nuclearcraft.creative_particle_source.energy"),
                10, 65, 0xFF404040, false);
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractContents(graphics, mouseX, mouseY, partialTick);
        particleSelector.renderOverlay(graphics, mouseX, mouseY);
        scaleDropdown.renderOverlay(graphics, mouseX, mouseY);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (!isOverDropdown(mouseX, mouseY)) {
            super.extractTooltip(graphics, mouseX, mouseY);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.isEscape()) {
            boolean closed = particleSelector.isOpen() || scaleDropdown.isOpen();
            particleSelector.close();
            scaleDropdown.close();
            if (closed) return true;
        }
        if (particleSelector.isFieldFocused() && particleSelector.keyPressed(event)) {
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (particleSelector.isFieldFocused() && particleSelector.charTyped(event)) {
            return true;
        }
        return super.charTyped(event);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        if (isOverDropdown(x, y)) {
            particleSelector.mouseScrolled(x, y, scrollX, scrollY);
            return true;
        }
        return super.mouseScrolled(x, y, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double x = event.x();
        double y = event.y();
        if (dropdownMouseButton != -1) {
            return true;
        }
        if (!particleSelector.isMouseOver(x, y)) {
            particleSelector.close();
        }
        if (!scaleDropdown.isMouseOver(x, y)) {
            scaleDropdown.close();
        }
        if (isOverDropdown(x, y)) {
            dropdownMouseButton = event.button();
        } else if (getChildAt(x, y).isEmpty()) {
            setFocused(null);
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        return dropdownMouseButton != -1 || super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (dropdownMouseButton == -1) {
            return super.mouseReleased(event);
        }
        if (dropdownMouseButton == event.button()) {
            dropdownMouseButton = -1;
            setDragging(false);
        }
        // Dropdown rows can cover inventory slots, including when the click closes the list.
        return true;
    }

    private boolean isOverDropdown(double x, double y) {
        return particleSelector.isMouseOver(x, y) || scaleDropdown.isMouseOver(x, y);
    }
}
