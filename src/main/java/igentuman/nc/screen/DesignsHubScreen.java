package igentuman.nc.screen;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import igentuman.nc.client.gui.fission.designer.DesignGrid;
import igentuman.nc.client.gui.fission.designer.DesignSimulator;
import igentuman.nc.hub.HubApiClient;
import igentuman.nc.hub.HubConfig;
import igentuman.nc.hub.HubResult;
import igentuman.nc.hub.dto.DeleteDesignResponseDto;
import igentuman.nc.hub.dto.DesignDto;
import igentuman.nc.hub.dto.DesignListItemDto;
import igentuman.nc.hub.dto.DesignListResponseDto;
import igentuman.nc.hub.dto.VoteResultDto;
import igentuman.nc.item.FissionReactorPlanItem;
import igentuman.nc.screen.element.FuelDropdown;
import igentuman.nc.util.TextUtils;
import igentuman.nc.util.builder.DesignPreviewRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DesignsHubScreen extends Screen {

    private static final int WIN_W = 460;
    private static final int WIN_H = 340;
    private static final int PAGE_SIZE = 20;
    private static final int ROW_H = 76;
    private static final int PREVIEW_SIZE = 64;
    private static final int MAX_HYDRATE_IN_FLIGHT = 3;

    private final FissionDesignerScreen parent;

    private int winX;
    private int winY;

    private int page = 1;
    private int total = 0;
    private String sortBy = "new";
    private boolean loading = false;
    private Component errorMessage;
    private int scroll = 0;
    private boolean fuelSelected = false;

    private final List<DesignListItemDto> items = new ArrayList<>();
    private final Map<String, DesignGrid> grids = new HashMap<>();
    private final Map<String, DesignSimulator> simulators = new HashMap<>();
    private final Set<String> hydrating = new HashSet<>();
    private final Set<String> voting = new HashSet<>();
    private final Set<String> deleting = new HashSet<>();
    private final Map<String, Component> rowMessages = new HashMap<>();

    private FuelDropdown fuelDropdown;

    private int listY;
    private int listH;
    private int sortBtnX, pagerPrevX, pagerNextX, controlsY;
    private int fuelY;

    public DesignsHubScreen(FissionDesignerScreen parent) {
        super(Component.translatable("screen.nuclearcraft.hub.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        winX = (width - WIN_W) / 2;
        winY = (height - WIN_H) / 2;

        controlsY = winY + 22;
        fuelY = winY + 40;
        listY = winY + 58;
        listH = WIN_H - 58 - 12;

        sortBtnX = winX + 8;
        pagerPrevX = winX + WIN_W - 90;
        pagerNextX = winX + WIN_W - 20;

        fuelDropdown = new FuelDropdown(winX + 8, fuelY, WIN_W - 16, 14);
        fuelDropdown.setOnSelect(idx -> {
            fuelSelected = true;
            recomputeAllStats();
        });

        addRenderableWidget(Button.builder(Component.literal("X"), b -> onClose())
                .bounds(winX + WIN_W - 16, winY + 4, 12, 12).build());

        fetchPage();
    }

    private void fetchPage() {
        loading = true;
        errorMessage = null;
        HubApiClient.listDesigns(page, PAGE_SIZE, sortBy, HubConfig.CHANNEL, null, null)
                .thenAccept(result -> Minecraft.getInstance().execute(() -> onListResult(result)));
    }

    private void onListResult(HubResult<DesignListResponseDto> result) {
        loading = false;
        if (result instanceof HubResult.Success<DesignListResponseDto> s) {
            total = s.value().total;
            int maxPage = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
            if (page > maxPage) {
                page = maxPage;
                fetchPage();
                return;
            }
            items.clear();
            items.addAll(s.value().designs);
            grids.clear();
            simulators.clear();
            hydrating.clear();
            scroll = 0;
            hydrateVisible();
        } else if (result instanceof HubResult.NetworkError<DesignListResponseDto> e) {
            errorMessage = Component.translatable("screen.nuclearcraft.hub.list_network_error", e.message());
        } else {
            errorMessage = Component.translatable("screen.nuclearcraft.hub.list_failed");
        }
    }

    private void hydrateVisible() {
        for (DesignListItemDto item : items) {
            if (hydrating.size() >= MAX_HYDRATE_IN_FLIGHT) {
                return;
            }
            if (grids.containsKey(item.id) || hydrating.contains(item.id)) {
                continue;
            }
            hydrating.add(item.id);
            HubApiClient.getDesign(item.id)
                    .thenAccept(result -> Minecraft.getInstance().execute(() -> onDesignHydrated(item.id, result)));
        }
    }

    private void onDesignHydrated(String id, HubResult<DesignDto> result) {
        hydrating.remove(id);
        if (result instanceof HubResult.Success<DesignDto> s) {
            try {
                CompoundTag tag = TagParser.parseTag(s.value().design);
                DesignGrid grid = DesignGrid.fromTag(tag);
                grids.put(id, grid);
                DesignSimulator sim = new DesignSimulator(grid);
                if (fuelSelected) {
                    sim.setFuel(fuelDropdown.getSelectedFuelKey(), fuelDropdown.getSelectedVariant());
                }
                sim.simulateIfDirty();
                simulators.put(id, sim);
            } catch (CommandSyntaxException | RuntimeException ignored) {
            }
        }
        hydrateVisible();
    }

    private void recomputeAllStats() {
        String key = fuelDropdown.getSelectedFuelKey();
        String variant = fuelDropdown.getSelectedVariant();
        for (DesignSimulator sim : simulators.values()) {
            sim.setFuel(key, variant);
            sim.simulateIfDirty();
        }
    }

    private void setSort(String newSort) {
        if (sortBy.equals(newSort)) {
            return;
        }
        sortBy = newSort;
        page = 1;
        fetchPage();
    }

    private void changePage(int delta) {
        int maxPage = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
        int next = Math.max(1, Math.min(maxPage, page + delta));
        if (next != page) {
            page = next;
            fetchPage();
        }
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
        g.fill(0, 0, width, height, 0x80000000);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
        renderBackground(g, mouseX, mouseY, partialTicks);
        g.fill(winX, winY, winX + WIN_W, winY + WIN_H, 0xF0202020);
        drawOutline(g, winX, winY, WIN_W, WIN_H, 0xFF5A5A5A);
        g.drawCenteredString(font, title, winX + WIN_W / 2, winY + 6, 0xFFFFFF);

        drawButton(g, sortBtnX, controlsY, 90, Component.translatable("screen.nuclearcraft.hub.sort", sortLabel()), mouseX, mouseY);
        int maxPage = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
        drawButton(g, pagerPrevX, controlsY, 16, Component.literal("<"), mouseX, mouseY);
        g.drawCenteredString(font, Component.translatable("screen.nuclearcraft.hub.page", page, maxPage), pagerPrevX - 55, controlsY + 4, 0xC0C0C0);
        drawButton(g, pagerNextX, controlsY, 16, Component.literal(">"), mouseX, mouseY);

        fuelDropdown.draw(g, mouseX, mouseY, partialTicks);

        int viewportBottom = listY + listH;
        g.enableScissor(winX, listY, winX + WIN_W, viewportBottom);
        int rowY = listY - scroll;
        for (DesignListItemDto item : items) {
            if (rowY + ROW_H >= listY && rowY <= viewportBottom) {
                drawRow(g, item, winX + 8, rowY, mouseX, mouseY);
            }
            rowY += ROW_H;
        }
        g.disableScissor();

        if (loading) {
            g.drawCenteredString(font, Component.translatable("screen.nuclearcraft.hub.loading"), winX + WIN_W / 2, listY + listH / 2, 0xFFFFFF);
        } else if (errorMessage != null) {
            g.drawCenteredString(font, errorMessage, winX + WIN_W / 2, listY + listH / 2, 0xFF6060);
        } else if (items.isEmpty()) {
            g.drawCenteredString(font, Component.translatable("screen.nuclearcraft.hub.no_designs"), winX + WIN_W / 2, listY + listH / 2, 0xC0C0C0);
        }

        fuelDropdown.drawOverlay(g, mouseX, mouseY, partialTicks);
        for (net.minecraft.client.gui.components.Renderable renderable : renderables) {
            renderable.render(g, mouseX, mouseY, partialTicks);
        }
    }

    private Component sortLabel() {
        return switch (sortBy) {
            case "votes" -> Component.translatable("screen.nuclearcraft.hub.sort.votes");
            case "size" -> Component.translatable("screen.nuclearcraft.hub.sort.size");
            default -> Component.translatable("screen.nuclearcraft.hub.sort.new");
        };
    }

    private void drawRow(GuiGraphics g, DesignListItemDto item, int x, int y, int mouseX, int mouseY) {
        g.fill(x, y, x + WIN_W - 16, y + ROW_H - 2, 0xFF2A2A2A);
        DesignGrid grid = grids.get(item.id);
        if (grid != null) {
            DesignPreviewRenderer.render(g, grid.cells, x + 4, y + 4, PREVIEW_SIZE, PREVIEW_SIZE);
        } else {
            g.drawCenteredString(font, "...", x + 4 + PREVIEW_SIZE / 2, y + PREVIEW_SIZE / 2, 0x808080);
        }

        int textX = x + PREVIEW_SIZE + 14;
        g.drawString(font, item.name, textX, y + 6, 0xFFFFFF, false);
        g.drawString(font, Component.translatable("screen.nuclearcraft.hub.author", item.author), textX, y + 20, 0x909090, false);

        DesignSimulator sim = simulators.get(item.id);
        Component statsLine;
        if (!fuelSelected) {
            statsLine = Component.translatable("screen.nuclearcraft.hub.select_fuel");
        } else if (sim == null) {
            statsLine = Component.translatable("screen.nuclearcraft.hub.loading_stats");
        } else {
            statsLine = Component.translatable("screen.nuclearcraft.hub.stats",
                    TextUtils.scaledFormat(sim.energyPerTick), sim.steamPerTick, TextUtils.numberFormat(sim.netHeat));
        }
        g.drawString(font, statsLine, textX, y + 36, 0xC0C0C0, false);

        Component msg = rowMessages.get(item.id);
        if (msg != null) {
            g.drawString(font, msg, textX, y + 50, 0xFF6060, false);
        } else {
            g.drawString(font, Component.translatable("screen.nuclearcraft.hub.score",
                            String.format("%+d", item.upvotes), String.format("%+d", -item.downvotes), String.format("%.2f", item.score)),
                    textX, y + 50, 0x909090, false);
        }

        int btnY = y + ROW_H - 20;
        if (!HubConfig.adminSecret().isBlank()) {
            drawSmallButton(g, x + WIN_W - 16 - 166, btnY, 54, Component.translatable("screen.nuclearcraft.hub.delete"), mouseX, mouseY);
        }
        drawSmallButton(g, x + WIN_W - 16 - 110, btnY, 26, Component.translatable("screen.nuclearcraft.hub.upvote"), mouseX, mouseY);
        drawSmallButton(g, x + WIN_W - 16 - 80, btnY, 26, Component.translatable("screen.nuclearcraft.hub.downvote"), mouseX, mouseY);
        drawSmallButton(g, x + WIN_W - 16 - 54, btnY, 54, Component.translatable("screen.nuclearcraft.designer.load"), mouseX, mouseY);
    }

    private void drawButton(GuiGraphics g, int x, int y, int w, Component text, int mouseX, int mouseY) {
        boolean hover = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + 14;
        g.fill(x, y, x + w, y + 14, hover ? 0xFF505050 : 0xFF303030);
        drawOutline(g, x, y, w, 14, 0xFF5A5A5A);
        drawFittedButtonText(g, x, y, w, text);
    }

    private void drawSmallButton(GuiGraphics g, int x, int y, int w, Component text, int mouseX, int mouseY) {
        boolean hover = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + 14;
        g.fill(x, y, x + w, y + 14, hover ? 0xFF505050 : 0xFF303030);
        drawOutline(g, x, y, w, 14, 0xFF5A5A5A);
        drawFittedButtonText(g, x, y, w, text);
    }

    private void drawFittedButtonText(GuiGraphics g, int x, int y, int w, Component text) {
        float scale = Math.min(1f, (w - 4f) / Math.max(1, font.width(text)));
        g.pose().pushPose();
        g.pose().translate(x + w / 2f, y + 7, 0);
        g.pose().scale(scale, scale, 1);
        g.drawCenteredString(font, text, 0, -4, 0xFFFFFF);
        g.pose().popPose();
    }

    private void drawOutline(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y, x + 1, y + h, color);
        g.fill(x + w - 1, y, x + w, y + h, color);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        boolean wasOpen = fuelDropdown.isOpen();
        if (fuelDropdown.mouseClicked(mx, my, button)) {
            return true;
        }
        if (wasOpen) {
            return true;
        }
        if (hit(sortBtnX, controlsY, 90, 14, mx, my)) {
            setSort(nextSort());
            return true;
        }
        if (hit(pagerPrevX, controlsY, 16, 14, mx, my)) {
            changePage(-1);
            return true;
        }
        if (hit(pagerNextX, controlsY, 16, 14, mx, my)) {
            changePage(1);
            return true;
        }
        if (my >= listY && my < listY + listH) {
            int rowIndex = (int) ((my - listY + scroll) / ROW_H);
            if (rowIndex >= 0 && rowIndex < items.size()) {
                DesignListItemDto item = items.get(rowIndex);
                int rowY = listY - scroll + rowIndex * ROW_H;
                int btnY = rowY + ROW_H - 20;
                int rowX = winX + 8;
                if (!HubConfig.adminSecret().isBlank()
                        && hit(rowX + WIN_W - 16 - 166, btnY, 54, 14, mx, my)) {
                    deleteDesign(item);
                    return true;
                }
                if (hit(rowX + WIN_W - 16 - 110, btnY, 26, 14, mx, my)) {
                    castVote(item, 1);
                    return true;
                }
                if (hit(rowX + WIN_W - 16 - 80, btnY, 26, 14, mx, my)) {
                    castVote(item, -1);
                    return true;
                }
                if (hit(rowX + WIN_W - 16 - 54, btnY, 54, 14, mx, my)) {
                    loadDesign(item);
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    private String nextSort() {
        return switch (sortBy) {
            case "new" -> "votes";
            case "votes" -> "size";
            default -> "new";
        };
    }

    private void castVote(DesignListItemDto item, int voteValue) {
        if (voting.contains(item.id) || Minecraft.getInstance().player == null) {
            return;
        }
        voting.add(item.id);
        rowMessages.remove(item.id);
        String playerUuid = Minecraft.getInstance().player.getGameProfile().getId().toString();
        HubApiClient.vote(item.id, voteValue, playerUuid)
                .thenAccept(result -> Minecraft.getInstance().execute(() -> onVoteResult(item, result)));
    }

    private void onVoteResult(DesignListItemDto item, HubResult<VoteResultDto> result) {
        voting.remove(item.id);
        if (result instanceof HubResult.Success<VoteResultDto> s) {
            item.upvotes = s.value().upvotes;
            item.downvotes = s.value().downvotes;
            item.score = s.value().score;
        } else if (result instanceof HubResult.Forbidden<VoteResultDto>) {
            rowMessages.put(item.id, Component.translatable("screen.nuclearcraft.hub.own_vote"));
        } else if (result instanceof HubResult.RateLimited<VoteResultDto> r) {
            rowMessages.put(item.id, Component.translatable("screen.nuclearcraft.hub.vote_rate_limited", r.retryAfterSeconds()));
        } else {
            rowMessages.put(item.id, Component.translatable("screen.nuclearcraft.hub.vote_failed"));
        }
    }

    private void deleteDesign(DesignListItemDto item) {
        String secret = HubConfig.adminSecret();
        if (secret.isBlank() || !deleting.add(item.id)) {
            return;
        }
        rowMessages.remove(item.id);
        HubApiClient.deleteDesign(item.id, secret)
                .thenAccept(result -> Minecraft.getInstance().execute(() -> onDeleteResult(item.id, result)));
    }

    private void onDeleteResult(String id, HubResult<DeleteDesignResponseDto> result) {
        deleting.remove(id);
        if (result instanceof HubResult.Success<DeleteDesignResponseDto> s && s.value().ok) {
            rowMessages.remove(id);
            fetchPage();
        } else if (result instanceof HubResult.Forbidden<DeleteDesignResponseDto>) {
            rowMessages.put(id, Component.translatable("screen.nuclearcraft.hub.delete_forbidden"));
        } else if (result instanceof HubResult.NotFound<DeleteDesignResponseDto>) {
            rowMessages.put(id, Component.translatable("screen.nuclearcraft.hub.delete_not_found"));
        } else {
            rowMessages.put(id, Component.translatable("screen.nuclearcraft.hub.delete_failed"));
        }
    }

    private void loadDesign(DesignListItemDto item) {
        DesignGrid grid = grids.get(item.id);
        if (grid == null) {
            return;
        }
        onClose();
        int sep = item.fuel == null ? -1 : item.fuel.indexOf('|');
        String fuelKey = sep >= 0 ? item.fuel.substring(0, sep) : item.fuel;
        String variant = sep >= 0 ? item.fuel.substring(sep + 1) : "";
        parent.applyLoadedDesign(grid, fuelKey, variant);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double scrollX, double scrollY) {
        if (fuelDropdown.mouseScrolled(mx, my, scrollX, scrollY)) {
            return true;
        }
        if (my >= listY && my < listY + listH) {
            int totalHeight = items.size() * ROW_H;
            int maxScroll = Math.max(0, totalHeight - listH);
            scroll = Math.max(0, Math.min(maxScroll, scroll - (int) (scrollY * ROW_H / 2)));
            return true;
        }
        return super.mouseScrolled(mx, my, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (fuelDropdown.isFieldFocused() && fuelDropdown.keyPressed(key, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char c, int modifiers) {
        if (fuelDropdown.isFieldFocused() && fuelDropdown.charTyped(c, modifiers)) {
            return true;
        }
        return super.charTyped(c, modifiers);
    }

    private boolean hit(int x, int y, int w, int h, double mx, double my) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
