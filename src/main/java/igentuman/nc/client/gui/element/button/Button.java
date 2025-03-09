package igentuman.nc.client.gui.element.button;

import igentuman.nc.NuclearCraft;
import igentuman.nc.client.gui.processor.side.SideConfigSlotSelectionScreen;
import igentuman.nc.client.gui.turbine.TurbinePortScreen;
import igentuman.nc.container.NCProcessorContainer;
import igentuman.nc.client.gui.element.NCGuiElement;
import igentuman.nc.network.toServer.PacketGuiButtonPress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Button<T extends AbstractContainerScreen<?>> extends NCGuiElement {
    protected AbstractContainerMenu container;
    protected AbstractContainerScreen<?> screen;
    protected int bId;

    protected ImageButton btn;
    protected Component tooltipKey = Component.empty();

    public Button(int xPos, int yPos, T screen, int id)  {
        super(xPos, yPos, 18, 18, Component.empty());
        x = xPos;
        y = yPos;
        this.container = screen.getMenu();
        this.screen = screen;
        bId = id;
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

    public static class SideConfig extends Button {
        public SideConfig(int xPos, int yPos, AbstractContainerScreen<?> screen) {
            super(xPos, yPos, screen, 69);//nice
            height = 18;
            width = 18;
            btn = new ImageButton(X(), Y(), width, height, 220, 220, 18, TEXTURE, pButton -> {
                Minecraft.getInstance().forceSetScreen(new SideConfigSlotSelectionScreen<>(screen));
            });
            tooltipKey = Component.translatable("gui.nc.side_config.tooltip");
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
            return List.of(Component.translatable("gui.nc.redstone_config.tooltip_"+mode));
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
            btn = new ImageButton(X(), Y(), width, height, 238, 76, 18, TEXTURE, pButton -> {

            });
        }

        public List<Component> getTooltips() {
            return List.of();
        }
    }

    public static class CloseConfig extends Button {
        public <T extends NCProcessorContainer> CloseConfig(int xPos, int yPos, AbstractContainerScreen<T> screen) {
            super(xPos, yPos, screen, 71);
            height = 18;
            width = 18;
            btn = new ImageButton(X(), Y(), width, height, 202, 220, 18, TEXTURE, pButton -> {
                this.screen.onClose();
            });
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
                    Component.translatable("gui.nc.reactor_mode.tooltip_" + code)
            ));
            if(timer < 2000) {
                list.add(Component.translatable("gui.nc.reactor_mode.timer", timer/20));
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
                    Component.translatable("gui.nc.reactor_comparator_config.tooltip_"+mode),
                    Component.translatable("gui.nc.reactor_comparator_strength.tooltip", strength)
                    );
        }

        public void setMode(byte redstoneMode) {
            mode = redstoneMode;
            btn = new ImageButton(X(), Y(), width, height, 238, 256 - (redstoneMode+1) * 36, 18, TEXTURE, pButton -> {
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
                    Component.translatable("gui.nc.turbine_comparator_config.tooltip_"+mode),
                    Component.translatable("gui.nc.reactor_comparator_strength.tooltip", strength)
            );
        }

        public void setMode(byte redstoneMode) {
            mode = redstoneMode;
            btn = new ImageButton(X(), Y(), width, height, 238, 256 - (redstoneMode+1) * 36, 18, TEXTURE, pButton -> {
                NuclearCraft.packetHandler().sendToServer(new PacketGuiButtonPress(pos, BTN_ID));
            });
        }
    }
}
