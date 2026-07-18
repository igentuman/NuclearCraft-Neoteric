package igentuman.nc.client.gui.processor;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.processor.CreativeParticleSourceBE;
import igentuman.nc.client.gui.element.IDropdown;
import igentuman.nc.client.gui.element.NCGuiElement;
import igentuman.nc.client.gui.element.NCTextField;
import igentuman.nc.client.gui.element.ParticleSelector;
import igentuman.nc.client.gui.element.ScaleDropdown;
import igentuman.nc.container.NCProcessorContainer;
import igentuman.nc.network.toServer.PacketCreativeParticleSource;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.lwjgl.glfw.GLFW;

import static igentuman.nc.util.TextUtils.__;

public class CreativeParticleSourceScreen<T extends NCProcessorContainer<AbstractContainerMenu>> extends NCProcessorScreen<T> {

    protected ParticleSelector particleSelector;
    protected NCTextField focusField;
    protected NCTextField energyField;
    protected ScaleDropdown scaleDropdown;

    protected String selectedParticle = "";
    protected double focusValue = 1;
    protected double energyValue = 1;
    protected int scaleIndex = 1;

    public CreativeParticleSourceScreen(AbstractContainerMenu abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
    }

    @Override
    protected void init() {
        super.init();
        widgets.clear();

        if (menu.getBlockEntity() instanceof CreativeParticleSourceBE be) {
            selectedParticle = be.selectedParticle;
            focusValue = be.particleFocus;
            energyValue = be.particleEnergy;
            scaleIndex = be.energyScale;
        }

        particleSelector = new ParticleSelector(8, 20, 162, 16);
        particleSelector.setSelected(selectedParticle);
        particleSelector.setOnSelect(name -> {
            selectedParticle = name;
            sendUpdate();
        });
        addWidget(particleSelector);

        scaleDropdown = new ScaleDropdown(134, 62, 34, 16);
        scaleDropdown.setSelectedIndex(scaleIndex);
        scaleDropdown.setOnSelect(index -> {
            scaleIndex = index;
            sendUpdate();
        });
        addWidget(scaleDropdown);

        focusField = new NCTextField(50, 42, 118, 14, true);
        focusField.setValue(fmt(focusValue));
        focusField.setOnChange(value -> {
            if (value.isEmpty()) return;
            try {
                focusValue = Double.parseDouble(value);
                sendUpdate();
            } catch (NumberFormatException ignored) {
            }
        });
        addWidget(focusField);

        energyField = new NCTextField(50, 62, 80, 14, true);
        energyField.setValue(fmt(energyValue));
        energyField.setOnChange(value -> {
            if (value.isEmpty()) return;
            try {
                energyValue = Double.parseDouble(value);
                sendUpdate();
            } catch (NumberFormatException ignored) {
            }
        });
        addWidget(energyField);
    }

    protected void sendUpdate() {
        NuclearCraft.packetHandler().sendToServer(
                new PacketCreativeParticleSource(menu.getPosition(), selectedParticle, focusValue, energyValue, scaleIndex));
    }

    protected static String fmt(double value) {
        if (!Double.isInfinite(value) && value == Math.rint(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }

    @Override
    public void render(PoseStack graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        for (NCGuiElement widget : widgets) {
            if (widget instanceof IDropdown dropdown && dropdown.isOpen()) {
                dropdown.drawOverlay(graphics, mouseX, mouseY, partialTicks);
            }
        }
    }

    @Override
    protected void renderLabels(PoseStack graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        drawString(graphics, font, __("gui.nuclearcraft.creative_particle_source.focus"), 10, 45, 0x404040);
        drawString(graphics, font, __("gui.nuclearcraft.creative_particle_source.energy"), 10, 65, 0x404040);
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            boolean closedAny = false;
            for (NCGuiElement widget : widgets) {
                if (widget instanceof IDropdown dropdown && dropdown.isOpen()) {
                    dropdown.close();
                    closedAny = true;
                }
            }
            if (closedAny) {
                return true;
            }
        }
        for (NCGuiElement widget : widgets) {
            if (widget.keyPressed(key, scanCode, modifiers)) {
                return true;
            }
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (NCGuiElement widget : widgets) {
            if (widget instanceof NCTextField field) {
                field.setFieldFocused(false);
            }
            if (widget instanceof ParticleSelector selector) {
                selector.clearFieldFocus();
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char c, int modifiers) {
        for (NCGuiElement widget : widgets) {
            if (widget.charTyped(c, modifiers)) {
                return true;
            }
        }
        return super.charTyped(c, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        for (NCGuiElement widget : widgets) {
            if (widget.mouseScrolled(mouseX, mouseY, delta)) {
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    protected void addOtherSlots() {
    }
}
