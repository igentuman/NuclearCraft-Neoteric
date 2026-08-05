package igentuman.nc.screen.element;

import igentuman.nc.content.fuel.FuelDef;
import igentuman.nc.item.FissionReactorPlanItem;
import igentuman.nc.registration.FissionFuelEntry;
import igentuman.nc.setup.ModEntries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/** Searchable dropdown that lists every fission fuel variant; selection drives the design simulation. */
public class FuelDropdown extends AbstractWidget {

    private record Entry(int index, String key, String variant, String label, String searchKey) {}

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
        search = new EditBox(Minecraft.getInstance().font, x + 2, y + 1, width - 4, height - 2, Component.empty());
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
        List<Entry> tmp = new ArrayList<>();
        for (FissionFuelEntry fuel : ModEntries.FISSION_FUEL.values()) {
            if (!fuel.isEnabled()) continue;
            String[] variants = fuel.base().isSpecial() ? new String[]{""} : FuelDef.ITEM_VARIANTS;
            for (String v : variants) {
                String lbl = FissionReactorPlanItem.fuelLabel(fuel.key, v);
                tmp.add(new Entry(0, fuel.key, v, lbl, lbl.toLowerCase(Locale.ROOT)));
            }
        }
        tmp.sort((a, b) -> a.label().compareToIgnoreCase(b.label()));
        int i = 0;
        for (Entry e : tmp) {
            all.add(new Entry(i++, e.key(), e.variant(), e.label(), e.searchKey()));
        }
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

    public String getSelectedFuelKey() {
        if (selectedIndex < 0 || selectedIndex >= all.size()) {
            return null;
        }
        return all.get(selectedIndex).key();
    }

    public String getSelectedVariant() {
        if (selectedIndex < 0 || selectedIndex >= all.size()) {
            return "";
        }
        return all.get(selectedIndex).variant();
    }

    public void setSelectedFuel(String key, String variant) {
        if (key == null) {
            return;
        }
        String v = variant == null ? "" : variant;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).key().equals(key) && all.get(i).variant().equals(v)) {
                selectedIndex = i;
                setSearchValue(selectedLabel());
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

    public boolean isOpen() {
        return open;
    }

    public void close() {
        if (open) {
            open = false;
            search.setFocused(false);
            setSearchValue(selectedLabel());
        }
    }

    public void draw(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        graphics.fill(getX(), getY(), getX() + width, getY() + height, 0xFF101010);
        graphics.fill(getX(), getY(), getX() + width, getY() + 1, 0xFF5A5A5A);
        graphics.fill(getX(), getY() + height - 1, getX() + width, getY() + height, 0xFF5A5A5A);
        graphics.fill(getX(), getY(), getX() + 1, getY() + height, 0xFF5A5A5A);
        graphics.fill(getX() + width - 1, getY(), getX() + width, getY() + height, 0xFF5A5A5A);
        search.setX(getX() + 2);
        search.setY(getY() + 1);
        search.render(graphics, mouseX, mouseY, partialTicks);
        graphics.drawString(Minecraft.getInstance().font, Component.literal("v"),
                getX() + width - 7, getY() + (height - 8) / 2, 0xFFB0B0B0);
    }

    protected int visibleRows() {
        return Math.min(Math.max(1, filtered.size()), MAX_VISIBLE_ROWS);
    }

    protected int maxScroll() {
        return Math.max(0, filtered.size() - MAX_VISIBLE_ROWS);
    }

    public void drawOverlay(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (!open) {
            return;
        }
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 300);
        Font font = Minecraft.getInstance().font;
        int rows = visibleRows();
        int top = getY() + height;
        int bottom = top + rows * ROW_HEIGHT;
        graphics.fill(getX() - 1, top - 1, getX() + width + 1, bottom + 1, 0xFF5A5A5A);
        graphics.fill(getX(), top, getX() + width, bottom, 0xF0101010);
        for (int i = 0; i < MAX_VISIBLE_ROWS; i++) {
            int idx = scroll + i;
            if (idx >= filtered.size()) {
                break;
            }
            Entry e = filtered.get(idx);
            int rowY = top + i * ROW_HEIGHT;
            boolean hovered = mouseX >= getX() && mouseX < getX() + width && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT;
            if (hovered) {
                graphics.fill(getX(), rowY, getX() + width, rowY + ROW_HEIGHT, 0x60FFFFFF);
            }
            String text = font.plainSubstrByWidth(e.label(), width - 8);
            graphics.drawString(font, text, getX() + 4, rowY + (ROW_HEIGHT - 8) / 2, 0xFFFFFF);
        }
        if (filtered.size() > MAX_VISIBLE_ROWS) {
            int trackHeight = rows * ROW_HEIGHT;
            int thumbHeight = Math.max(8, trackHeight * MAX_VISIBLE_ROWS / filtered.size());
            int travel = trackHeight - thumbHeight;
            int thumbY = top + (maxScroll() == 0 ? 0 : travel * scroll / maxScroll());
            graphics.fill(getX() + width - 2, thumbY, getX() + width, thumbY + thumbHeight, 0xFFB0B0B0);
        }
        graphics.pose().popPose();
    }

    protected boolean inField(double mouseX, double mouseY) {
        return mouseX >= getX() && mouseX < getX() + width && mouseY >= getY() && mouseY < getY() + height;
    }

    protected boolean inList(double mouseX, double mouseY) {
        int top = getY() + height;
        return open && mouseX >= getX() && mouseX < getX() + width && mouseY >= top && mouseY < top + visibleRows() * ROW_HEIGHT;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (inList(mouseX, mouseY)) {
            scroll = Math.max(0, Math.min(maxScroll(), scroll - (int) Math.signum(scrollY)));
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
            search.setX(getX() + 2);
            search.setY(getY() + 1);
            search.mouseClicked(mouseX, mouseY, button);
            return true;
        }
        if (inList(mouseX, mouseY)) {
            int idx = scroll + (int) ((mouseY - (getY() + height)) / ROW_HEIGHT);
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

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        draw(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
    }
}
