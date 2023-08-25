package igentuman.nc.client.gui.element.button;

import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.NuclearCraft;
import igentuman.nc.client.gui.processor.side.SideConfigSlotSelectionScreen;
import igentuman.nc.container.NCProcessorContainer;
import igentuman.nc.client.gui.element.NCGuiElement;
import igentuman.nc.network.toServer.PacketGuiButtonPress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.lang.reflect.Field;
import java.util.List;

public class Button<T extends AbstractContainerScreen> extends NCGuiElement {
    protected AbstractContainerMenu container;
    protected AbstractContainerScreen screen;
    protected int bId;

    protected ImageButton btn;
    protected Component tooltipKey = Component.empty();

    public Button(int xPos, int yPos, T screen, int id)  {
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
    public void draw(PoseStack transform, int mX, int mY, float pTicks) {
        super.draw(transform, mX, mY, pTicks);
        btn.render(transform, mX, mY, pTicks);
    }

    @Override
    public boolean onPress() {
        btn.onPress();
        return true;
    }

    public static class SideConfig extends Button {
        public SideConfig(int xPos, int yPos, AbstractContainerScreen screen) {
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
        public static int BTN_ID = 70;

        public int mode = 0;

        public RedstoneConfig(int xPos, int yPos, AbstractContainerScreen screen, BlockPos pos) {
            super(xPos, yPos, screen, 70);
            this.pos = pos;
            height = 18;
            width = 18;
            btn = new ImageButton(X(), Y(), width, height, 238, 220, 18, TEXTURE, pButton -> {
                NuclearCraft.packetHandler().sendToServer(new PacketGuiButtonPress(pos, BTN_ID));
            });
        }

        public List<Component> getTooltips() {
            return List.of(Component.translatable("gui.nc.redstone_config.tooltip_"+mode));
        }

        public void setMode(int redstoneMode) {
            mode = redstoneMode;
            try {
                Field f = btn.getClass().getDeclaredField("yTexStart");
                f.setAccessible(true);
                f.set(btn, 220 - redstoneMode * 36);
            } catch (NoSuchFieldException | IllegalAccessException ignore) {
            }
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

    public static class ReactorComparatorModeButton extends Button {
        private final BlockPos pos;
        public static int BTN_ID = 71;
        public byte mode = 2;
        public byte strength = 0;

        public ReactorComparatorModeButton(int xPos, int yPos, AbstractContainerScreen screen, BlockPos pos) {
            super(xPos, yPos, screen, BTN_ID);
            this.pos = pos;
            height = 18;
            width = 18;
            btn = new ImageButton(X(), Y(), width, height, 238, 220, 18, TEXTURE, pButton -> {
                NuclearCraft.packetHandler().sendToServer(new PacketGuiButtonPress(pos, BTN_ID));
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
            try {
                Field f = btn.getClass().getDeclaredField("yTexStart");
                f.setAccessible(true);
                f.set(btn, 220 - (redstoneMode+1) * 36);
            } catch (NoSuchFieldException | IllegalAccessException ignore) {
            }
        }
    }
}
