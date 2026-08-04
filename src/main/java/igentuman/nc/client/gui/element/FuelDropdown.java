package igentuman.nc.client.gui.element;

import igentuman.nc.setup.registration.FissionFuel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class FuelDropdown extends NCGuiElement implements IDropdown {

    private record Entry(int index, List<String> key, String label, String searchKey) {}

    protected static final int ROW_HEIGHT = 14;
    protected static final int MAX_VISIBLE_ROWS = 10;

    protected final EditBox search;
    protected final List<Entry> all = new ArrayList<>();
    protected final List<Entry> filtered = new ArrayList<>();

    protected int selectedIndex = 0;
    protected int scroll = 0;
    protected boolean open = false;
    protected boolean suppressResponder = false;
    protected Consumer<Integer> onSelect;

    public FuelDropdown(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        search = new EditBox(Minecraft.getInstance().font, X() + 2, Y() + 1, width - 4, height - 2, Component.empty());
        search.setMaxLength(64);
        search.setBordered(true);
        search.setTextColor(0xFFFFFF);
        search.setResponder(value -> {
            if (!suppressResponder) {
                recompute();
            }
        });
        buildEntries();
        recompute();
        setSearchValue(selectedLabel());
    }

    protected void buildEntries() {
        all.clear();
        List<List<String>> keys = new ArrayList<>(FissionFuel.NC_FUEL.keySet());
        keys.sort((a, b) -> label(a).compareToIgnoreCase(label(b)));
        int i = 0;
        for (List<String> key : keys) {
            String lbl = label(key);
            all.add(new Entry(i++, key, lbl, lbl.toLowerCase(Locale.ROOT)));
        }
    }

    protected static String label(List<String> key) {
        // key = ["fuel", group, name, subtype]
        StringBuilder sb = new StringBuilder();
        if (key.size() > 2) {
            sb.append(key.get(2));
        }
        if (key.size() > 3 && !key.get(3).isEmpty()) {
            sb.append(" (").append(key.get(3)).append(")");
        }
        if (key.size() > 1) {
            sb.append(" [").append(key.get(1)).append("]");
        }
        return sb.toString();
    }

    protected void recompute() {
        String query = search.getValue().toLowerCase(Locale.ROOT).trim();
        filtered.clear();
        for (Entry e : all) {
            if (query.isEmpty() || e.searchKey().contains(query)) {
                filtered.add(e);
            }
        }
        scroll = 0;
    }

    protected void setSearchValue(String value) {
        suppressResponder = true;
        search.setValue(value);
        suppressResponder = false;
    }

    public FuelDropdown setOnSelect(Consumer<Integer> consumer) {
        this.onSelect = consumer;
        return this;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int index) {
        selectedIndex = Math.max(0, Math.min(Math.max(0, all.size() - 1), index));
        setSearchValue(selectedLabel());
    }

    public List<String> getSelectedFuelKey() {
        if (selectedIndex < 0 || selectedIndex >= all.size()) {
            return null;
        }
        return all.get(selectedIndex).key();
    }

    public void setSelectedFuelKey(List<String> key) {
        if (key == null) {
            return;
        }
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).key().equals(key)) {
                setSelectedIndex(i);
                return;
            }
        }
    }

    protected String selectedLabel() {
        if (selectedIndex >= 0 && selectedIndex < all.size()) {
            return all.get(selectedIndex).label();
        }
        return all.isEmpty() ? "N/A" : all.get(0).label();
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public void close() {
        if (open) {
            open = false;
            search.setFocused(false);
            setSearchValue(selectedLabel());
        }
    }

    protected void selectEntry(Entry entry) {
        selectedIndex = entry.index();
        setSearchValue(entry.label());
        open = false;
        search.setFocused(false);
        if (onSelect != null) {
            onSelect.accept(selectedIndex);
        }
    }

    @Override
    public void draw(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        graphics.fill(X(), Y(), X() + width, Y() + height, 0xFF101010);
        graphics.fill(X(), Y(), X() + width, Y() + 1, 0xFF5A5A5A);
        graphics.fill(X(), Y() + height - 1, X() + width, Y() + height, 0xFF5A5A5A);
        graphics.fill(X(), Y(), X() + 1, Y() + height, 0xFF5A5A5A);
        graphics.fill(X() + width - 1, Y(), X() + width, Y() + height, 0xFF5A5A5A);
        search.setX(X() + 2);
        search.setY(Y() + 1);
        search.render(graphics, mouseX, mouseY, partialTicks);
        graphics.drawString(Minecraft.getInstance().font, Component.literal("v"),
                X() + width - 7, Y() + (height - 8) / 2, 0xFFB0B0B0);
    }

    protected int visibleRows() {
        return Math.min(Math.max(1, filtered.size()), MAX_VISIBLE_ROWS);
    }

    protected int maxScroll() {
        return Math.max(0, filtered.size() - MAX_VISIBLE_ROWS);
    }

    @Override
    public void drawOverlay(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (!open) {
            return;
        }
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 300);
        Font font = Minecraft.getInstance().font;
        int rows = visibleRows();
        int top = Y() + height;
        int bottom = top + rows * ROW_HEIGHT;
        graphics.fill(X() - 1, top - 1, X() + width + 1, bottom + 1, 0xFF5A5A5A);
        graphics.fill(X(), top, X() + width, bottom, 0xF0101010);
        for (int i = 0; i < MAX_VISIBLE_ROWS; i++) {
            int idx = scroll + i;
            if (idx >= filtered.size()) {
                break;
            }
            Entry e = filtered.get(idx);
            int rowY = top + i * ROW_HEIGHT;
            boolean hovered = mouseX >= X() && mouseX < X() + width && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT;
            if (hovered) {
                graphics.fill(X(), rowY, X() + width, rowY + ROW_HEIGHT, 0x60FFFFFF);
            }
            String text = font.plainSubstrByWidth(e.label(), width - 8);
            graphics.drawString(font, text, X() + 4, rowY + (ROW_HEIGHT - 8) / 2, 0xFFFFFF);
        }

        // scrollbar
        if (filtered.size() > MAX_VISIBLE_ROWS) {
            int trackHeight = rows * ROW_HEIGHT;
            int thumbHeight = Math.max(8, trackHeight * MAX_VISIBLE_ROWS / filtered.size());
            int travel = trackHeight - thumbHeight;
            int thumbY = top + (maxScroll() == 0 ? 0 : travel * scroll / maxScroll());
            graphics.fill(X() + width - 2, thumbY, X() + width, thumbY + thumbHeight, 0xFFB0B0B0);
        }
        graphics.pose().popPose();
    }

    protected boolean inField(double mouseX, double mouseY) {
        return mouseX >= X() && mouseX < X() + width && mouseY >= Y() && mouseY < Y() + height;
    }

    protected boolean inList(double mouseX, double mouseY) {
        int top = Y() + height;
        return open && mouseX >= X() && mouseX < X() + width && mouseY >= top && mouseY < top + visibleRows() * ROW_HEIGHT;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (inList(mouseX, mouseY)) {
            scroll = Math.max(0, Math.min(maxScroll(), scroll - (int) Math.signum(delta)));
            return true;
        }
        return false;
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
            search.setX(X() + 2);
            search.setY(Y() + 1);
            search.mouseClicked(mouseX, mouseY, button);
            return true;
        }
        if (inList(mouseX, mouseY)) {
            int idx = scroll + (int) ((mouseY - (Y() + height)) / ROW_HEIGHT);
            if (idx >= 0 && idx < filtered.size()) {
                selectEntry(filtered.get(idx));
            }
            return true;
        }
        if (open) {
            close();
        }
        return false;
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (!search.isFocused()) {
            return false;
        }
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            return false;
        }
        search.keyPressed(key, scanCode, modifiers);
        return true;
    }

    @Override
    public boolean charTyped(char c, int modifiers) {
        if (search.isFocused()) {
            return search.charTyped(c, modifiers);
        }
        return false;
    }

    public boolean isFieldFocused() {
        return search.isFocused();
    }

    public void clearFieldFocus() {
        search.setFocused(false);
    }
}
