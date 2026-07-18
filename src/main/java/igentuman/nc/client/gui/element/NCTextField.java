package igentuman.nc.client.gui.element;

import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public class NCTextField extends NCGuiElement {

    protected final EditBox editBox;
    protected Consumer<String> onChange;
    protected boolean suppressResponder = false;

    public NCTextField(int x, int y, int width, int height, boolean numeric) {
        super(x, y, width, height, Component.empty());
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        editBox = new EditBox(Minecraft.getInstance().font, X(), Y(), width, height, Component.empty());
        editBox.setMaxLength(32);
        editBox.setBordered(true);
        editBox.setVisible(true);
        editBox.setTextColor(0xFFFFFF);
        editBox.setResponder(value -> {
            if (!suppressResponder && onChange != null) {
                onChange.accept(value);
            }
        });
        if (numeric) {
            editBox.setFilter(s -> s.isEmpty() || s.matches("\\d*(\\.\\d*)?"));
        }
    }

    public NCTextField setOnChange(Consumer<String> consumer) {
        this.onChange = consumer;
        return this;
    }

    public void setValue(String value) {
        suppressResponder = true;
        editBox.setValue(value);
        suppressResponder = false;
    }

    public void step(int delta) {
        int next = (int) Math.max(0, getDouble(0) + delta);
        editBox.setValue(String.valueOf(next));
    }

    public String getValue() {
        return editBox.getValue();
    }

    public double getDouble(double fallback) {
        try {
            return Double.parseDouble(editBox.getValue());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public boolean isFieldFocused() {
        return editBox.isFocused();
    }

    public void setFieldFocused(boolean value) {
        editBox.setFocus(value);
    }

    protected void syncPos() {
        editBox.x = X();
        editBox.y = Y();
    }

    @Override
    public void draw(PoseStack graphics, int mouseX, int mouseY, float partialTicks) {
        syncPos();
        editBox.render(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        syncPos();
        boolean inside = mouseX >= X() && mouseX < X() + width && mouseY >= Y() && mouseY < Y() + height;
        editBox.setFocus(inside);
        if (inside) {
            editBox.mouseClicked(mouseX, mouseY, button);
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (!editBox.isFocused()) {
            return false;
        }
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            return false;
        }
        editBox.keyPressed(key, scanCode, modifiers);
        return true;
    }

    @Override
    public boolean charTyped(char c, int modifiers) {
        if (editBox.isFocused()) {
            return editBox.charTyped(c, modifiers);
        }
        return false;
    }
}
