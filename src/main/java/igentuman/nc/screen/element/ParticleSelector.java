package igentuman.nc.screen.element;

import igentuman.nc.api.particle.ParticleDefinition;
import igentuman.nc.setup.Registers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/** Searchable particle dropdown used by the creative particle source. */
public class ParticleSelector extends AbstractWidget {

    private record Entry(ResourceLocation id, ParticleDefinition definition, Component label, String searchKey) {
    }

    private static final int ROW_HEIGHT = 16;
    private static final int VISIBLE_ROWS = 5;
    private static final int ICON_WIDTH = 18;

    private final EditBox search;
    private final List<Entry> all = new ArrayList<>();
    private final List<Entry> filtered = new ArrayList<>();
    private Consumer<ResourceLocation> onSelect;
    private ResourceLocation selected;
    private int scrollOffset;
    private boolean open;
    private boolean suppressResponder;

    public ParticleSelector(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
        search = new EditBox(Minecraft.getInstance().font, x + ICON_WIDTH, y + 1,
                width - ICON_WIDTH - 2, height - 2, Component.empty());
        search.setMaxLength(64);
        search.setBordered(true);
        search.setTextColor(0xFFFFFF);
        search.setResponder(value -> {
            if (!suppressResponder) recompute();
        });
        buildEntries();
        recompute();
    }

    private void buildEntries() {
        all.clear();
        Registers.PARTICLE_DEFINITION_REGISTRY.entrySet().stream()
                .map(entry -> {
                    ResourceLocation id = entry.getKey().location();
                    ParticleDefinition definition = entry.getValue();
                    Component label = Component.translatable(definition.translationKey());
                    String searchKey = (label.getString() + " " + id).toLowerCase(Locale.ROOT);
                    return new Entry(id, definition, label, searchKey);
                })
                .sorted(Comparator.comparing(entry -> entry.label().getString(), String.CASE_INSENSITIVE_ORDER))
                .forEach(all::add);
    }

    private void recompute() {
        String query = search.getValue().strip().toLowerCase(Locale.ROOT);
        filtered.clear();
        all.stream().filter(entry -> query.isEmpty() || entry.searchKey().contains(query)).forEach(filtered::add);
        scrollOffset = 0;
    }

    public ParticleSelector setOnSelect(Consumer<ResourceLocation> consumer) {
        onSelect = consumer;
        return this;
    }

    public ParticleSelector setSelected(ResourceLocation particleId) {
        selected = particleId;
        setSearchValue(selectedLabel());
        return this;
    }

    public ResourceLocation getSelected() {
        return selected;
    }

    public boolean isOpen() {
        return open;
    }

    public boolean isFieldFocused() {
        return search.isFocused();
    }

    public void clearFieldFocus() {
        search.setFocused(false);
    }

    public void close() {
        open = false;
        search.setFocused(false);
        setSearchValue(selectedLabel());
    }

    private void setSearchValue(String value) {
        suppressResponder = true;
        search.setValue(value);
        suppressResponder = false;
    }

    private String selectedLabel() {
        if (selected == null) return "";
        ParticleDefinition definition = Registers.PARTICLE_DEFINITION_REGISTRY.get(selected);
        return definition == null ? selected.toString() : Component.translatable(definition.translationKey()).getString();
    }

    private int visibleRows() {
        return Math.min(VISIBLE_ROWS, Math.max(1, filtered.size()));
    }

    private int maxScroll() {
        return Math.max(0, filtered.size() - VISIBLE_ROWS);
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(getX(), getY(), getX() + width, getY() + height, 0xFF101010);
        graphics.fill(getX(), getY(), getX() + width, getY() + 1, 0xFF5A5A5A);
        graphics.fill(getX(), getY() + height - 1, getX() + width, getY() + height, 0xFF5A5A5A);

        ParticleDefinition definition = selected == null ? null : Registers.PARTICLE_DEFINITION_REGISTRY.get(selected);
        if (definition != null) {
            graphics.blit(definition.textureId(), getX() + 1, getY() + (height - 16) / 2,
                    0, 0, 16, 16, 16, 16);
        }
        search.setX(getX() + ICON_WIDTH);
        search.setY(getY() + 1);
        search.render(graphics, mouseX, mouseY, partialTick);
    }

    public void renderOverlay(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!open) return;
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 300);
        int top = getY() + height;
        int rows = visibleRows();
        int bottom = top + rows * ROW_HEIGHT;
        graphics.fill(getX() - 1, top - 1, getX() + width + 1, bottom + 1, 0xFF5A5A5A);
        graphics.fill(getX(), top, getX() + width, bottom, 0xF0101010);

        Font font = Minecraft.getInstance().font;
        for (int row = 0; row < VISIBLE_ROWS; row++) {
            int index = scrollOffset + row;
            if (index >= filtered.size()) break;
            Entry entry = filtered.get(index);
            int rowY = top + row * ROW_HEIGHT;
            if (mouseX >= getX() && mouseX < getX() + width && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT) {
                graphics.fill(getX(), rowY, getX() + width, rowY + ROW_HEIGHT, 0x60FFFFFF);
            }
            graphics.blit(entry.definition().textureId(), getX() + 1, rowY, 0, 0, 16, 16, 16, 16);
            String label = font.plainSubstrByWidth(entry.label().getString(), width - ICON_WIDTH - 4);
            graphics.drawString(font, label, getX() + ICON_WIDTH, rowY + 4, 0xFFFFFF, false);
        }

        if (filtered.size() > VISIBLE_ROWS) {
            int trackHeight = rows * ROW_HEIGHT;
            int thumbHeight = Math.max(8, trackHeight * VISIBLE_ROWS / filtered.size());
            int travel = trackHeight - thumbHeight;
            int thumbY = top + (maxScroll() == 0 ? 0 : travel * scrollOffset / maxScroll());
            graphics.fill(getX() + width - 2, thumbY, getX() + width, thumbY + thumbHeight, 0xFFB0B0B0);
        }
        graphics.pose().popPose();
    }

    private boolean inField(double mouseX, double mouseY) {
        return mouseX >= getX() && mouseX < getX() + width && mouseY >= getY() && mouseY < getY() + height;
    }

    private boolean inList(double mouseX, double mouseY) {
        int top = getY() + height;
        return open && mouseX >= getX() && mouseX < getX() + width
                && mouseY >= top && mouseY < top + visibleRows() * ROW_HEIGHT;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (inField(mouseX, mouseY)) {
            if (!open) {
                open = true;
                setSearchValue("");
                recompute();
            }
            search.setFocused(true);
            search.mouseClicked(mouseX, mouseY, button);
            return true;
        }
        if (inList(mouseX, mouseY)) {
            int index = scrollOffset + (int) ((mouseY - getY() - height) / ROW_HEIGHT);
            if (index >= 0 && index < filtered.size()) {
                Entry entry = filtered.get(index);
                selected = entry.id();
                close();
                if (onSelect != null) onSelect.accept(selected);
            }
            return true;
        }
        if (open) close();
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!inList(mouseX, mouseY)) return false;
        scrollOffset = Math.max(0, Math.min(maxScroll(), scrollOffset - (int) Math.signum(scrollY)));
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!search.isFocused() || keyCode == GLFW.GLFW_KEY_ESCAPE) return false;
        return search.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return search.isFocused() && search.charTyped(codePoint, modifiers);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
    }
}
