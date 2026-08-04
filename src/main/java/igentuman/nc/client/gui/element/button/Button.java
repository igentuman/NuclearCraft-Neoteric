package igentuman.nc.client.gui.element.button;

import com.mojang.blaze3d.systems.RenderSystem;
import igentuman.nc.NuclearCraft;
import igentuman.nc.client.NcClient;
import igentuman.nc.client.gui.MultiblockAnalyzeReportScreen;
import igentuman.nc.client.gui.MultiblockBuilderScreen;
import igentuman.nc.client.gui.element.NCGuiElement;
import igentuman.nc.client.gui.element.NCTextField;
import igentuman.nc.client.gui.processor.side.SideConfigSlotSelectionScreen;
import igentuman.nc.compat.emi.EMIPlugin;
import igentuman.nc.container.MultiblockControllerContainer;
import igentuman.nc.network.toServer.PacketBuildMultiblock;
import igentuman.nc.network.toServer.PacketGuiButtonPress;
import igentuman.nc.network.toServer.PacketLoadFissionDesign;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button.Builder;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.NuclearCraft.debugLog;
import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.multiblock.heat_exchanger.HeatExchangerRegistration.HX_BLOCKS;
import static igentuman.nc.util.ModUtil.isEMILoaded;
import static igentuman.nc.util.TextUtils.__;

public class Button<T extends AbstractContainerScreen<?>> extends NCGuiElement {
    protected AbstractContainerMenu container;
    protected AbstractContainerScreen<?> screen;
    protected int bId;

    protected net.minecraft.client.gui.components.Button btn;
    protected Component tooltipKey = Component.empty();

    /**
     * Safely opens a URL using Minecraft's utility method
     * @param url The URL to open
     * @return true if successful, false otherwise
     */
    public static boolean openUrl(String url) {
        try {
            net.minecraft.Util.getPlatform().openUri(url);
            return true;
        } catch (Exception e) {
            debugLog("Minecraft platform method failed: " + e.getMessage());
        }

        try {
            // Method 2: Try Desktop API as fallback
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(new URI(url));
                    return true;
                }
            }
        } catch (IOException | URISyntaxException e) {
            debugLog("Desktop API failed: " + e.getMessage());
        }

        try {
            // Method 3: Try system-specific commands as last resort
            String os = System.getProperty("os.name").toLowerCase();
            Runtime runtime = Runtime.getRuntime();

            if (os.contains("win")) {
                runtime.exec("rundll32 url.dll,FileProtocolHandler " + url);
            } else if (os.contains("mac")) {
                runtime.exec("open " + url);
            } else if (os.contains("nix") || os.contains("nux")) {
                runtime.exec("xdg-open " + url);
            } else {
                debugLog("Unsupported operating system: " + os);
                return false;
            }
            return true;
        } catch (IOException e) {
            debugLog("System command failed: " + e.getMessage());
        }

        debugLog("All URL opening methods failed for: " + url);
        return false;
    }

    public Button(int xPos, int yPos, T screen, int id)  {
        super(xPos, yPos, 18, 18, Component.empty());
        x = xPos;
        y = yPos;
        this.container = screen.getMenu();
        this.screen = screen;
        bId = id;
    }

    public Button(int xPos, int yPos, AbstractContainerScreen<?> explScreen, int id, MutableComponent label, net.minecraft.client.gui.components.Button.OnPress o) {
        super(xPos, yPos, 18, 18, label);
        x = xPos;
        y = yPos;
        this.container = explScreen.getMenu();
        this.screen = explScreen;
        bId = id;
        height = 20;
        width = 80;
        btn = new Builder(label, o).pos(X(), Y()).size(80, 20).build();
    }

    public List<Component> getTooltips() {
        return List.of(tooltipKey);
    }

    @Override
    public void draw(GuiGraphics transform, int mX, int mY, float pTicks) {
        super.draw(transform, mX, mY, pTicks);
        btn.render(transform, mX, mY, pTicks);
    }

    @Override
    public boolean onPress() {
        btn.onPress();
        return true;
    }

    public void setEnabled(boolean b) {
        btn.active = b;
    }

    @Override
    public boolean isMouseOver(double pMouseX, double pMouseY) {
        return this.active && this.visible && pMouseX >= (double)x && pMouseY >= (double)y && pMouseX < (double)(x + this.width) && pMouseY < (double)(y + this.height);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if(X() <= pMouseX && pMouseX < X() + width && Y() <= pMouseY && pMouseY < Y() + height) {
            return onPress();
        }
        return false;
    }

    public static class SideConfig extends Button {
        public SideConfig(int xPos, int yPos, AbstractContainerScreen<?> screen) {
            super(xPos, yPos, screen, 69);//nice
            height = 18;
            width = 18;
            btn = new ImageButton(X(), Y(), width, height, 220, 220, 18, TEXTURE, pButton -> {
                Minecraft.getInstance().forceSetScreen(new SideConfigSlotSelectionScreen<>(screen));
            });
            tooltipKey = __("gui.nc.side_config.tooltip");
        }
    }

    public static class RedstoneConfig extends Button {
        private final BlockPos pos;
        public static final int BTN_ID = 70;

        public int mode = 0;

        public RedstoneConfig(int xPos, int yPos, AbstractContainerScreen<?> screen, BlockPos pos) {
            super(xPos, yPos, screen, 70);
            this.pos = pos;
            height = 18;
            width = 18;
        }

        public List<Component> getTooltips() {
            return List.of(__("gui.nc.redstone_config.tooltip_"+mode));
        }

        public void setMode(int redstoneMode) {
            mode = redstoneMode;
            btn = new ImageButton(X(), Y(), width, height, 184, 220 - redstoneMode * 36, 18, TEXTURE, pButton -> {
                NuclearCraft.packetHandler().sendToServer(new PacketGuiButtonPress(pos, BTN_ID));
            });
        }
    }

    public static class ShowRecipes extends Button {

        public int mode = 0;

        public ShowRecipes(int xPos, int yPos, AbstractContainerScreen<?> screen) {
            super(xPos, yPos, screen, 70);
            height = 18;
            width = 18;
            btn = new ImageButton(X(), Y(), width, height, 220, 76, 18, TEXTURE, pButton -> {
                if(isEMILoaded()) {
                    EMIPlugin.displayRecipes(screen);
                }
            });
        }

        public List<Component> getTooltips() {
            return List.of();
        }
    }

    public static class CloseConfig extends Button {
        public <T extends AbstractContainerMenu> CloseConfig(int xPos, int yPos, AbstractContainerScreen<T> screen) {
            super(xPos, yPos, screen, 71);
            height = 18;
            width = 18;
            btn = new ImageButton(X(), Y(), width, height, 202, 220, 18, TEXTURE, pButton -> {
                this.screen.onClose();
            });
        }
    }

    public static class Magnet extends Button {
        public static final int BTN_ID = 184;
        public boolean enabled = false;
        public byte strength = 0;

        public Magnet(int xPos, int yPos, AbstractContainerScreen<?> screen) {
            super(xPos, yPos, screen, BTN_ID);
            height = 18;
            width = 18;
            btn = new ImageButton(X(), Y(), width, height, 184, 112, 18, TEXTURE, pButton -> {
                NuclearCraft.packetHandler().sendToServer(new PacketGuiButtonPress(NcClient.tryGetClientPlayer(), BTN_ID));
            });
        }

        public List<Component> getTooltips() {
            String mode = "enable";
            if(enabled) mode = "disable";
            List<Component> list = new ArrayList<>(List.of(
                    __("tooltip.nc.magnet."+mode)
            ));
            return list;
        }

        public void setEnabled(boolean flag) {
            enabled = flag;
            int y = flag ? 1 : 0;
            btn = new ImageButton(X(), Y(), width, height, 184, 112 - (y+1) * 36, 18, TEXTURE, pButton -> {
                NuclearCraft.packetHandler().sendToServer(new PacketGuiButtonPress(NcClient.tryGetClientPlayer(), BTN_ID));
            });
        }

        public void refreshPosition() {
            setEnabled(enabled);
        }
    }

    public static class ReactorMode extends Button {
        private final BlockPos pos;
        public static final int BTN_ID = 72;
        public boolean mode = false;
        public byte strength = 0;
        public int timer = 2000;

        public ReactorMode(int xPos, int yPos, AbstractContainerScreen<?> screen, BlockPos pos) {
            super(xPos, yPos, screen, BTN_ID);
            this.pos = pos;
            height = 18;
            width = 18;
            btn = new ImageButton(X(), Y(), width, height, 220, 184, 18, TEXTURE, pButton -> {
                NuclearCraft.packetHandler().sendToServer(new PacketGuiButtonPress(pos, BTN_ID));
            });
        }

        public List<Component> getTooltips() {
            String code = "energy";
            if(mode) code = "steam";
            List<Component> list = new ArrayList<>(List.of(
                    __("gui.nc.reactor_mode.tooltip_" + code)
            ));
            if(timer < 2000) {
                list.add(__("gui.nc.reactor_mode.timer", timer/20));
            }
            return list;
        }

        public void setMode(boolean reactorMode) {
            mode = reactorMode;
            int y = reactorMode ? 1 : 0;
            btn = new ImageButton(X(), Y(), width, height, 220, 184 - (y+1) * 36, 18, TEXTURE, pButton -> {
                NuclearCraft.packetHandler().sendToServer(new PacketGuiButtonPress(pos, BTN_ID));
            });
        }

        public void setTimer(int modeTimer) {
            timer = modeTimer;
        }
    }

    public static class MultiblockAnalyze extends Button {
        private final BlockPos pos;
        public static final int BTN_ID = 777;
        public byte strength = 0;
        public int timer = 2000;

        public MultiblockAnalyze(int xPos, int yPos, AbstractContainerScreen<?> screen, BlockPos pos) {
            super(xPos, yPos, screen, BTN_ID);
            MultiblockControllerContainer container = new MultiblockControllerContainer(pos);
            this.pos = pos;
            height = 18;
            width = 18;
            btn = new ImageButton(X(), Y(), width, height, 184, 112, 18, TEXTURE, pButton -> {
                NuclearCraft.packetHandler().sendToServer(new PacketGuiButtonPress(pos, BTN_ID));
                Minecraft.getInstance().forceSetScreen(new MultiblockAnalyzeReportScreen<>(screen, container));
            });
        }

        public List<Component> getTooltips() {
            List<Component> list = new ArrayList<>(List.of(
                    __("tooltip.nc.analyze")
            ));
            if(timer < 2000) {
                list.add(__("gui.nc.reactor_mode.timer", timer/20));
            } else {
                list.add(__("tooltip.nc.analyze.descr"));
            }
            return list;
        }

        public void setTimer(int modeTimer) {
            timer = modeTimer;
        }
    }

    public static class ReportIssue extends Button {
        private final BlockPos pos;
        public static final int BTN_ID = 889;
        public byte strength = 0;
        public int timer = 2000;
        private final ResourceLocation BTN_TEXTURE = rl("textures/gui/buttons/bug.png");

        public ReportIssue(int xPos, int yPos, AbstractContainerScreen<?> screen, BlockPos pos) {
            super(xPos, yPos, screen, BTN_ID);
            this.pos = pos;
            height = 8;
            width = 8;
            String link = "https://github.com/igentuman/NuclearCraft-Neoteric/issues/new?template=bug_report.md";
            btn = new ImageButton(X(), Y(), width, height, 0, 0, 8, BTN_TEXTURE, 8, 16, pButton -> {
                if (!openUrl(link)) {
                    //debugLog("Failed to open link: " + link);
                }
            });
        }

        public List<Component> getTooltips() {
            List<Component> list = List.of(
                    __("tooltip.nc.report_issue")
            );
            return list;
        }

        public void setTimer(int modeTimer) {
            timer = modeTimer;
        }
    }

    public static class InsertJson extends Button {
        private final BlockPos pos;
        public static final int BTN_ID = 888;
        public byte strength = 0;
        public int timer = 2000;


        public InsertJson(int xPos, int yPos, MultiblockBuilderScreen screen, BlockPos pos) {
            super(xPos, yPos, screen, BTN_ID);
            this.pos = pos;
            height = 18;
            width = 18;
            btn = new ImageButton(X(), Y(), width, height, 144, 220, 18, TEXTURE, pButton -> {
                int slot = Minecraft.getInstance().player.getInventory().selected;
                NuclearCraft.packetHandler().sendToServer(new PacketLoadFissionDesign(slot));
            });
        }

        public List<Component> getTooltips() {
            List<Component> list = List.of(
                    __("tooltip.nc.load_plan"),
                    __("tooltip.nc.load_plan.descr")
            );
            return list;
        }

        public void setTimer(int modeTimer) {
            timer = modeTimer;
        }
    }

    public static class Build extends Button {
        private final BlockPos pos;
        public static final int BTN_ID = 889;
        public byte strength = 0;
        public int timer = 2000;
        public MultiblockBuilderScreen screen;

        public void draw(GuiGraphics transform, int mX, int mY, float pTicks) {
            if(screen.blockMap.isEmpty()) return;
            super.draw(transform, mX, mY, pTicks);
        }


        public Build(int xPos, int yPos, MultiblockBuilderScreen screen, BlockPos pos) {
            super(xPos, yPos, screen, BTN_ID);
            this.screen = screen;
            this.pos = pos;
            height = 18;
            width = 18;
            btn = new ImageButton(X(), Y(), width, height, 162, 220, 18, TEXTURE, pButton -> {
                NuclearCraft.packetHandler().sendToServer(new PacketBuildMultiblock(pos, screen.blockMap));
            });
        }

        public List<Component> getTooltips() {
            List<Component> list = List.of(
                    __("tooltip.nc.build")
            );
            if(visible && active) {
                return list;
            }
            return List.of();
        }

        public void setTimer(int modeTimer) {
            timer = modeTimer;
        }
    }

    public static class Link extends Button {
        private final BlockPos pos;
        public static final int BTN_ID = 890;
        public byte strength = 0;
        public AbstractContainerScreen<?> screen;
        public String link = "";
        public List<Component> tooltips = new ArrayList<>();

        public void draw(GuiGraphics transform, int mX, int mY, float pTicks) {
            super.draw(transform, mX, mY, pTicks);
        }

        public Link(int xPos, int yPos, AbstractContainerScreen<?> screen, BlockPos pos, String link, List<Component> tooltips) {
            super(xPos, yPos, screen, BTN_ID);
            this.screen = screen;
            this.link = link;
            this.pos = pos;
            this.tooltips = tooltips;
            height = 18;
            width = 18;
            btn = new ImageButton(X(), Y(), width, height, 126, 220, 18, TEXTURE, pButton -> {
                if (!openUrl(link)) {
                    debugLog("Failed to open link: " + link);
                }
            });
        }

        public List<Component> getTooltips() {
            return tooltips;
        }
    }

    public static class FusionReactorRedstoneModeButton extends ReactorPortRedstoneModeButton {
        public static final int BTN_ID = 73;
        public FusionReactorRedstoneModeButton(int xPos, int yPos, AbstractContainerScreen<?> screen, BlockPos pos) {
            super(BTN_ID, xPos, yPos, screen, pos);
        }

        @Override
        public void setMode(byte redstoneMode) {
            mode = redstoneMode;
            btn = new ImageButton(X(), Y(), width, height, 238, 256 - (redstoneMode-10) * 36, 18, TEXTURE, pButton -> {
                NuclearCraft.packetHandler().sendToServer(new PacketGuiButtonPress(pos, BTN_ID));
            });
        }
    }

    public static class ReactorPortRedstoneModeButton extends Button {
        public final BlockPos pos;
        public static final int BTN_ID = 71;
        public byte mode = 2;
        public byte strength = 0;

        public ReactorPortRedstoneModeButton(int btId, int xPos, int yPos, AbstractContainerScreen<?> screen, BlockPos pos) {
            super(xPos, yPos, screen, btId);
            this.pos = pos;
            height = 18;
            width = 18;
            btn = new ImageButton(X(), Y(), width, height, 238, 256, 18, TEXTURE, pButton -> {
                NuclearCraft.packetHandler().sendToServer(new PacketGuiButtonPress(pos, bId));
            });
        }

        public ReactorPortRedstoneModeButton(int xPos, int yPos, AbstractContainerScreen<?> screen, BlockPos pos) {
            super(xPos, yPos, screen, BTN_ID);
            this.pos = pos;
            height = 18;
            width = 18;
            btn = new ImageButton(X(), Y(), width, height, 238, 256, 18, TEXTURE, pButton -> {
                NuclearCraft.packetHandler().sendToServer(new PacketGuiButtonPress(pos, bId));
            });
        }

        public List<Component> getTooltips() {
            return List.of(
                    __("gui.nc.reactor_comparator_config.tooltip_"+mode),
                    __("gui.nc.reactor_comparator_strength.tooltip", strength)
                    );
        }

        public void setMode(byte redstoneMode) {
            mode = redstoneMode;
            btn = new ImageButton(X(), Y(), width, height, 238, 256 - (redstoneMode) * 36, 18, TEXTURE, pButton -> {
                NuclearCraft.packetHandler().sendToServer(new PacketGuiButtonPress(pos, BTN_ID));
            });
        }
    }

    public static class MSRPortRedstoneModeButton extends ReactorPortRedstoneModeButton {
        public static final int BTN_ID = 83;

        public MSRPortRedstoneModeButton(int xPos, int yPos, AbstractContainerScreen<?> screen, BlockPos pos) {
            super(BTN_ID, xPos, yPos, screen, pos);
            mode = 1;
        }

        @Override
        public List<Component> getTooltips() {
            return List.of(
                    __("gui.nc.msr_comparator_config.tooltip_" + mode),
                    __("gui.nc.reactor_comparator_strength.tooltip", strength)
            );
        }

        @Override
        public void setMode(byte redstoneMode) {
            mode = redstoneMode;
            btn = new ImageButton(X(), Y(), width, height, 238, 256 - (redstoneMode + 1) * 36, 18, TEXTURE, pButton -> {
                NuclearCraft.packetHandler().sendToServer(new PacketGuiButtonPress(pos, BTN_ID));
            });
        }
    }

    public static class TurbinePortRedstoneModeButton extends Button {
        public final BlockPos pos;
        public static final int BTN_ID = 74;
        public byte mode = 2;
        public byte strength = 0;
        public TurbinePortRedstoneModeButton(int xPos, int yPos, AbstractContainerScreen<?> screen, BlockPos pos) {
            super(xPos, yPos, screen, BTN_ID);
            this.pos = pos;
            height = 18;
            width = 18;
            btn = new ImageButton(X(), Y(), width, height, 238, 256, 18, TEXTURE, pButton -> {
                NuclearCraft.packetHandler().sendToServer(new PacketGuiButtonPress(pos, bId));
            });
        }

        public List<Component> getTooltips() {
            return List.of(
                    __("gui.nc.turbine_comparator_config.tooltip_"+mode),
                    __("gui.nc.reactor_comparator_strength.tooltip", strength)
            );
        }

        public void setMode(byte redstoneMode) {
            mode = redstoneMode;
            btn = new ImageButton(X(), Y(), width, height, 238, 256 - (redstoneMode+1) * 36, 18, TEXTURE, pButton -> {
                NuclearCraft.packetHandler().sendToServer(new PacketGuiButtonPress(pos, BTN_ID));
            });
        }
    }

    public static class Kugelblitz extends Button {
        public final BlockPos pos;
        public static final int BTN_ID = 75;
        public byte mode = 2;
        public byte strength = 0;

        public Kugelblitz(int xPos, int yPos, AbstractContainerScreen<?> screen, BlockPos pos) {
            super(xPos, yPos, screen, BTN_ID);
            this.pos = pos;
            height = 18;
            width = 18;
            btn = new ImageButton(X(), Y(), width, height, 202, 256, 18, TEXTURE, pButton -> {
                NuclearCraft.packetHandler().sendToServer(new PacketGuiButtonPress(pos, bId));
            });
        }

        public List<Component> getTooltips() {
            return List.of(
                    __("gui.nc.kugelblits_port.tooltip_"+mode),
                    __("gui.nc.kugelblits_port.tooltip_strength", strength)
            );
        }

        public void setMode(byte redstoneMode) {
            mode = redstoneMode;
            btn = new ImageButton(X(), Y(), width, height, 202, 256 - (redstoneMode+1) * 36, 18, TEXTURE, pButton -> {
                NuclearCraft.packetHandler().sendToServer(new PacketGuiButtonPress(pos, BTN_ID));
            });
        }
    }

    public static class AcceleratorPortRedstoneModeButton extends Button {
        public final BlockPos pos;
        public static final int BTN_ID = 76;
        public byte mode = 1;
        public byte strength = 0;

        public AcceleratorPortRedstoneModeButton(int xPos, int yPos, AbstractContainerScreen<?> screen, BlockPos pos) {
            super(xPos, yPos, screen, BTN_ID);
            this.pos = pos;
            height = 18;
            width = 18;
            btn = new ImageButton(X(), Y(), width, height, 162, 0, 18, TEXTURE, pButton -> {
                NuclearCraft.packetHandler().sendToServer(new PacketGuiButtonPress(pos, bId));
            });
        }

        public List<Component> getTooltips() {
            return List.of(
                    __("gui.nc.accelerator_comparator_config.tooltip_"+mode),
                    __("gui.nc.reactor_comparator_strength.tooltip", strength)
            );
        }

        public void setMode(byte redstoneMode) {
            mode = redstoneMode;
            btn = new ImageButton(X(), Y(), width, height, 162, (redstoneMode-1) * 36, 18, TEXTURE, pButton -> {
                NuclearCraft.packetHandler().sendToServer(new PacketGuiButtonPress(pos, BTN_ID));
            });
        }
    }

    public static class TargetChamberPortRedstoneModeButton extends Button {
        public final BlockPos pos;
        public static final int BTN_ID = 78;
        public byte mode = 1;
        public byte strength = 0;

        public TargetChamberPortRedstoneModeButton(int xPos, int yPos, AbstractContainerScreen<?> screen, BlockPos pos) {
            super(xPos, yPos, screen, BTN_ID);
            this.pos = pos;
            height = 18;
            width = 18;
            btn = new ImageButton(X(), Y(), width, height, 162, 0, 18, TEXTURE, pButton -> {
                NuclearCraft.packetHandler().sendToServer(new PacketGuiButtonPress(pos, bId));
            });
        }

        public List<Component> getTooltips() {
            return List.of(
                    __("gui.nc.accelerator_comparator_config.tooltip_"+mode),
                    __("gui.nc.reactor_comparator_strength.tooltip", strength)
            );
        }

        public void setMode(byte redstoneMode) {
            mode = redstoneMode;
            btn = new ImageButton(X(), Y(), width, height, 162, (redstoneMode-1) * 36, 18, TEXTURE, pButton -> {
                NuclearCraft.packetHandler().sendToServer(new PacketGuiButtonPress(pos, BTN_ID));
            });
        }
    }

    public static class VoidPebbles extends Button {
        private final BlockPos pos;
        public static final int BTN_ID = 81;

        public VoidPebbles(int xPos, int yPos, AbstractContainerScreen<?> screen, BlockPos pos) {
            super(xPos, yPos, screen, BTN_ID);
            this.pos = pos;
            height = 18;
            width = 18;
            btn = new ImageButton(X()*2, Y()*2, width, height, 72, 220, 18, TEXTURE, pButton -> {
                NuclearCraft.packetHandler().sendToServer(new PacketGuiButtonPress(pos, BTN_ID));
            });
        }

        public List<Component> getTooltips() {
            return List.of(__("gui.nc.msr.void_pebbles.tooltip"));
        }
    }

    public static class HeatExchangerPortRedstoneModeButton extends Button {
        public final BlockPos pos;
        public static final int BTN_ID = 80;
        public byte mode = 1;
        public byte strength = 0;

        public HeatExchangerPortRedstoneModeButton(int xPos, int yPos, AbstractContainerScreen<?> screen, BlockPos pos) {
            super(xPos, yPos, screen, BTN_ID);
            this.pos = pos;
            height = 18;
            width = 18;
            btn = new ImageButton(X(), Y(), width, height, 238, 256, 18, TEXTURE, pButton -> {
                NuclearCraft.packetHandler().sendToServer(new PacketGuiButtonPress(pos, bId));
            });
        }

        public List<Component> getTooltips() {
            return List.of(
                    __("gui.nc.hx_comparator_config.tooltip_" + mode),
                    __("gui.nc.reactor_comparator_strength.tooltip", strength)
            );
        }

        public void setMode(byte redstoneMode) {
            mode = redstoneMode;
            btn = new ImageButton(X(), Y(), width, height, 238, 256 - (redstoneMode) * 36, 18, TEXTURE, pButton -> {
                NuclearCraft.packetHandler().sendToServer(new PacketGuiButtonPress(pos, BTN_ID));
            });
        }
    }

    public static class RadiatorToggle extends Button {
        public final BlockPos pos;
        public static final int BTN_ID = 79;
        public boolean enabled = false;
        private final ItemStack icon = new ItemStack(HX_BLOCKS.get("heat_exchanger_radiator").get());

        public RadiatorToggle(int xPos, int yPos, AbstractContainerScreen<?> screen, BlockPos pos) {
            super(xPos, yPos, screen, BTN_ID);
            this.pos = pos;
            height = 18;
            width = 18;
        }

        @Override
        public void draw(GuiGraphics transform, int mX, int mY, float pTicks) {
            transform.blit(TEXTURE, X(), Y(), 0, 0, 18, 18);
            transform.renderItem(icon, X() + 1, Y() + 1);
            if (!enabled) {
                transform.fill(X() + 1, Y() + 1, X() + 17, Y() + 17, 0xAA101010);
            }
            if (mX >= X() && mX < X() + 18 && mY >= Y() && mY < Y() + 18) {
                transform.fill(X() + 1, Y() + 1, X() + 17, Y() + 17, 0x60FFFFFF);
            }
        }

        @Override
        public boolean onPress() {
            NuclearCraft.packetHandler().sendToServer(new PacketGuiButtonPress(pos, BTN_ID));
            return true;
        }

        @Override
        public void setEnabled(boolean b) {
            enabled = b;
        }

        @Override
        public List<Component> getTooltips() {
            return List.of(__("gui.nc.radiator_toggle.tooltip_" + (enabled ? "disable" : "enable")));
        }
    }

    public static class Stepper extends Button {
        public static final int BTN_ID = 82;
        private final NCTextField field;
        private final int sign;

        public Stepper(int xPos, int yPos, AbstractContainerScreen<?> screen, NCTextField field, int sign) {
            super(xPos, yPos, screen, BTN_ID);
            this.field = field;
            this.sign = sign >= 0 ? 1 : -1;
            width = 9;
            height = 10;
        }

        @Override
        public void draw(GuiGraphics transform, int mX, int mY, float pTicks) {
            transform.fill(X(), Y(), X() + width, Y() + height, 0xFF000000);
            transform.fill(X() + 1, Y() + 1, X() + width - 1, Y() + height - 1, 0xFF8B8B8B);
            int cx = X() + width / 2;
            int cy = Y() + height / 2;
            transform.fill(X() + 2, cy, X() + width - 2, cy + 1, 0xFF101010);
            if (sign > 0) {
                transform.fill(cx, Y() + 2, cx + 1, Y() + height - 2, 0xFF101010);
            }
            if (mX >= X() && mX < X() + width && mY >= Y() && mY < Y() + height) {
                transform.fill(X() + 1, Y() + 1, X() + width - 1, Y() + height - 1, 0x60FFFFFF);
            }
        }

        @Override
        public boolean onPress() {
            field.step(sign * (Screen.hasControlDown() ? 10 : 1));
            return true;
        }

        @Override
        public List<Component> getTooltips() {
            return List.of(__("gui.nc.stepper.tooltip"));
        }
    }
}
