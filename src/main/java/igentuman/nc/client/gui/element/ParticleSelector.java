package igentuman.nc.client.gui.element;

import com.mojang.blaze3d.systems.RenderSystem;
import igentuman.nc.content.particles.Particle;
import igentuman.nc.content.particles.Particles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class ParticleSelector extends NCGuiElement implements IDropdown {

    private record Entry(String name, Component label, String searchKey, Particle particle) {}

    protected static final int ROW_HEIGHT = 16;
    protected static final int VISIBLE_ROWS = 5;
    protected static final int ICON_BOX = 18;

    protected final EditBox search;
    protected final List<Entry> all = new ArrayList<>();
    protected final List<Entry> filtered = new ArrayList<>();
    protected Consumer<String> onSelect;
    protected String selectedName = "";
    protected boolean open = false;
    protected int scrollOffset = 0;
    protected boolean suppressResponder = false;

    public ParticleSelector(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        search = new EditBox(Minecraft.getInstance().font, X() + ICON_BOX, Y() + 1, width - ICON_BOX - 2, height - 2, Component.empty());
        search.setMaxLength(64);
        search.setBordered(true);
        search.setTextColor(0xFFFFFF);
        search.setResponder(value -> {
            if (!suppressResponder) {
                recompute();
            }
        });
        buildList();
        recompute();
    }

    public ParticleSelector setOnSelect(Consumer<String> consumer) {
        this.onSelect = consumer;
        return this;
    }

    public ParticleSelector setSelected(String name) {
        selectedName = name == null ? "" : name;
        setSearchValue(displayName(selectedName));
        return this;
    }

    public String getSelected() {
        return selectedName;
    }

    protected void buildList() {
        all.clear();
        for (Particle particle : Particles.particles.values()) {
            Component label = particle.getLocalizedName();
            String key = (label.getString() + " " + particle.getName()).toLowerCase(Locale.ROOT);
            all.add(new Entry(particle.getName(), label, key, particle));
        }
        all.sort(Comparator.comparing(e -> e.label().getString().toLowerCase(Locale.ROOT)));
    }

    protected void recompute() {
        String query = search.getValue().toLowerCase(Locale.ROOT).trim();
        filtered.clear();
        for (Entry e : all) {
            if (query.isEmpty() || e.searchKey().contains(query)) {
                filtered.add(e);
            }
        }
        scrollOffset = 0;
    }

    protected void setSearchValue(String value) {
        suppressResponder = true;
        search.setValue(value);
        suppressResponder = false;
    }

    protected String displayName(String name) {
        Particle particle = Particles.getParticleFromName(name);
        return particle == null ? "" : particle.getLocalizedName().getString();
    }

    protected int listTop() {
        return Y() + height;
    }

    protected int maxScroll() {
        return Math.max(0, filtered.size() - VISIBLE_ROWS);
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
            setSearchValue(displayName(selectedName));
        }
    }

    protected void selectEntry(Entry entry) {
        selectedName = entry.name();
        setSearchValue(entry.label().getString());
        open = false;
        search.setFocused(false);
        if (onSelect != null) {
            onSelect.accept(selectedName);
        }
    }

    @Override
    public void draw(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        // field background
        graphics.fill(X(), Y(), X() + width, Y() + height, 0xFF101010);
        graphics.fill(X(), Y(), X() + width, Y() + 1, 0xFF5A5A5A);
        graphics.fill(X(), Y() + height - 1, X() + width, Y() + height, 0xFF5A5A5A);
        // selected particle icon
        Particle selected = Particles.getParticleFromName(selectedName);
        if (selected != null) {
            RenderSystem.setShaderTexture(0, selected.getTexture());
            graphics.blit(selected.getTexture(), X() + 1, Y() + (height - 16) / 2, 0, 0, 16, 16, 16, 16);
        }
        search.setX(X() + ICON_BOX);
        search.setY(Y() + 1);
        search.render(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void drawOverlay(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (!open) {
            return;
        }
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 300);
        int top = listTop();
        int rows = Math.min(VISIBLE_ROWS, Math.max(1, filtered.size()));
        int bottom = top + rows * ROW_HEIGHT;
        graphics.fill(X(), top, X() + width, bottom, 0xF0101010);
        graphics.fill(X() - 1, top - 1, X() + width + 1, top, 0xFF5A5A5A);
        graphics.fill(X() - 1, bottom, X() + width + 1, bottom + 1, 0xFF5A5A5A);
        graphics.fill(X() - 1, top, X(), bottom, 0xFF5A5A5A);
        graphics.fill(X() + width, top, X() + width + 1, bottom, 0xFF5A5A5A);

        Font font = Minecraft.getInstance().font;
        for (int i = 0; i < VISIBLE_ROWS; i++) {
            int idx = i + scrollOffset;
            if (idx >= filtered.size()) {
                break;
            }
            Entry e = filtered.get(idx);
            int rowY = top + i * ROW_HEIGHT;
            boolean hovered = mouseX >= X() && mouseX < X() + width && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT;
            if (hovered) {
                graphics.fill(X(), rowY, X() + width, rowY + ROW_HEIGHT, 0x60FFFFFF);
            }
            RenderSystem.setShaderTexture(0, e.particle().getTexture());
            graphics.blit(e.particle().getTexture(), X() + 1, rowY, 0, 0, 16, 16, 16, 16);
            graphics.drawString(font, e.label(), X() + ICON_BOX, rowY + (ROW_HEIGHT - 8) / 2, 0xFFFFFF);
        }

        // scrollbar
        if (filtered.size() > VISIBLE_ROWS) {
            int trackHeight = rows * ROW_HEIGHT;
            int thumbHeight = Math.max(8, trackHeight * VISIBLE_ROWS / filtered.size());
            int travel = trackHeight - thumbHeight;
            int thumbY = top + (maxScroll() == 0 ? 0 : travel * scrollOffset / maxScroll());
            graphics.fill(X() + width - 2, thumbY, X() + width, thumbY + thumbHeight, 0xFFB0B0B0);
        }
        graphics.pose().popPose();
    }

    protected boolean inField(double mouseX, double mouseY) {
        return mouseX >= X() && mouseX < X() + width && mouseY >= Y() && mouseY < Y() + height;
    }

    protected boolean inList(double mouseX, double mouseY) {
        int rows = Math.min(VISIBLE_ROWS, Math.max(1, filtered.size()));
        return open && mouseX >= X() && mouseX < X() + width && mouseY >= listTop() && mouseY < listTop() + rows * ROW_HEIGHT;
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
            search.setX(X() + ICON_BOX);
            search.setY(Y() + 1);
            search.mouseClicked(mouseX, mouseY, button);
            return true;
        }
        if (inList(mouseX, mouseY)) {
            int idx = (int) ((mouseY - listTop()) / ROW_HEIGHT) + scrollOffset;
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
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (inList(mouseX, mouseY)) {
            scrollOffset -= (int) Math.signum(delta);
            scrollOffset = Math.max(0, Math.min(maxScroll(), scrollOffset));
            return true;
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
