package igentuman.nc.screen;

import igentuman.nc.block_entity.particle.CreativeParticleSourceBE;
import igentuman.nc.container.UniversalProcessorContainer;
import igentuman.nc.network.particle.CreativeParticleSourceSettingsPayload;
import igentuman.nc.screen.element.ParticleSelector;
import igentuman.nc.screen.element.ScaleDropdown;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import static igentuman.nc.NuclearCraft.rl;

public class CreativeParticleSourceScreen extends AbstractContainerScreen<UniversalProcessorContainer> {

    private static final ResourceLocation TEXTURE = rl("textures/gui/processor.png");

    private ParticleSelector particleSelector;
    private ScaleDropdown scaleDropdown;
    private EditBox focusField;
    private EditBox energyField;
    private ResourceLocation selectedParticle;
    private double focusValue = 1D;
    private double energyValue = 1D;
    private int scaleIndex = 1;

    public CreativeParticleSourceScreen(UniversalProcessorContainer menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 180;
        imageHeight = 180;
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
        field.setTextColor(0xFFFFFF);
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
        PacketDistributor.sendToServer(new CreativeParticleSourceSettingsPayload(
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
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawCenteredString(font, title, imageWidth / 2, titleLabelY, 0xFFFFFF);
        graphics.drawString(font, Component.translatable("gui.nuclearcraft.creative_particle_source.focus"),
                10, 45, 0x404040, false);
        graphics.drawString(font, Component.translatable("gui.nuclearcraft.creative_particle_source.energy"),
                10, 65, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        particleSelector.renderOverlay(graphics, mouseX, mouseY);
        scaleDropdown.renderOverlay(graphics, mouseX, mouseY);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            boolean closed = particleSelector.isOpen() || scaleDropdown.isOpen();
            particleSelector.close();
            scaleDropdown.close();
            if (closed) return true;
        }
        if (particleSelector.isFieldFocused() && particleSelector.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (particleSelector.isFieldFocused() && particleSelector.charTyped(codePoint, modifiers)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        focusField.setFocused(false);
        energyField.setFocused(false);
        particleSelector.clearFieldFocus();
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
