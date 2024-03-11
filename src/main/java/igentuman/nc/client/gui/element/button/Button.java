package igentuman.nc.client.gui.element.button;

import com.mojang.blaze3d.matrix.MatrixStack;
import igentuman.nc.NuclearCraft;
import igentuman.nc.client.gui.processor.side.SideConfigSlotSelectionScreen;
import igentuman.nc.container.NCProcessorContainer;
import igentuman.nc.client.gui.element.NCGuiElement;
import igentuman.nc.network.toServer.PacketGuiButtonPress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Button<T extends ContainerScreen<?>> extends NCGuiElement {
    protected ContainerScreen<?> container;
    protected ContainerScreen<?> screen;
    protected int bId;

    protected ImageButton btn;
    protected ITextComponent tooltipKey  = new TranslationTextComponent("");

    public Button(int xPos, int yPos, T screen, int id)  {
        super(xPos, yPos, 18, 18, new TranslationTextComponent(""));
        x = xPos;
        y = yPos;
        //this.container = screen.getMenu();
        this.screen = screen;
        bId = id;
    }

    public List<ITextComponent> getTooltips() {
        return Arrays.asList(tooltipKey);
    }

    @Override
    public void draw(MatrixStack transform, int mX, int mY, float pTicks) {
        super.draw(transform, mX, mY, pTicks);
        btn.render(transform, mX, mY, pTicks);
    }

    @Override
    public boolean onPress() {
        btn.onPress();
        return true;
    }

    public static class SideConfig extends Button {
        public SideConfig(int xPos, int yPos, ContainerScreen<?> screen) {
            super(xPos, yPos, screen, 69);//nice
            height = 18;
            width = 18;
            btn = new ImageButton(X(), Y(), width, height, 220, 220, 18, TEXTURE, pButton -> {
                Minecraft.getInstance().forceSetScreen(new SideConfigSlotSelectionScreen<>(screen));
            });
            tooltipKey = new TranslationTextComponent("gui.nc.side_config.tooltip");
        }
    }

    public static class RedstoneConfig extends Button {
        private final BlockPos pos;
        public static final int BTN_ID = 70;

        public int mode = 0;

        public RedstoneConfig(int xPos, int yPos, ContainerScreen<?> screen, BlockPos pos) {
            super(xPos, yPos, screen, 70);
            this.pos = pos;
            height = 18;
            width = 18;
            btn = new ImageButton(X(), Y(), width, height, 238, 220, 18, TEXTURE, pButton -> {
                NuclearCraft.packetHandler().sendToServer(new PacketGuiButtonPress(pos, BTN_ID));
            });
        }

        public List<ITextComponent> getTooltips() {
            return Arrays.asList(new TranslationTextComponent("gui.nc.redstone_config.tooltip_"+mode));
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

    public static class ShowRecipes extends Button {
        private final BlockPos pos;
        public static int BTN_ID = 70;

        public int mode = 0;

        public ShowRecipes(int xPos, int yPos, ContainerScreen<?> screen, BlockPos pos) {
            super(xPos, yPos, screen, 70);
            this.pos = pos;
            height = 18;
            width = 18;
            btn = new ImageButton(X(), Y(), width, height, 238, 4, 18, TEXTURE, pButton -> {

            });
        }

        public List<ITextComponent> getTooltips() {
            return Arrays.asList();
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
        public <T extends NCProcessorContainer> CloseConfig(int xPos, int yPos, ContainerScreen<T> screen) {
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

        public ReactorMode(int xPos, int yPos, ContainerScreen<?> screen, BlockPos pos) {
            super(xPos, yPos, screen, BTN_ID);
            this.pos = pos;
            height = 18;
            width = 18;
            btn = new ImageButton(X(), Y(), width, height, 220, 184, 18, TEXTURE, pButton -> {
                NuclearCraft.packetHandler().sendToServer(new PacketGuiButtonPress(pos, BTN_ID));
            });
        }

        public List<ITextComponent> getTooltips() {
            String code = "energy";
            if(mode) code = "steam";
            List<ITextComponent> list = new ArrayList<>(Arrays.asList(
                    new TranslationTextComponent("gui.nc.reactor_mode.tooltip_" + code)
            ));
            if(timer < 2000) {
                list.add(new TranslationTextComponent("gui.nc.reactor_mode.timer", timer/20));
            }
            return list;
        }

        public void setMode(boolean reactorMode) {
            mode = reactorMode;
            int y = reactorMode ? 1 : 0;
            try {
                Field f = btn.getClass().getDeclaredField("yTexStart");
                f.setAccessible(true);
                f.set(btn, 184 - (y+1) * 36);
            } catch (NoSuchFieldException | IllegalAccessException ignore) {
            }
        }

        public void setTimer(int modeTimer) {
            timer = modeTimer;
        }
    }

    public static class ReactorComparatorModeButton extends Button {
        private final BlockPos pos;
        public static final int BTN_ID = 71;
        public byte mode = 2;
        public byte strength = 0;

        public ReactorComparatorModeButton(int xPos, int yPos, ContainerScreen<?> screen, BlockPos pos) {
            super(xPos, yPos, screen, BTN_ID);
            this.pos = pos;
            height = 18;
            width = 18;
            btn = new ImageButton(X(), Y(), width, height, 238, 220, 18, TEXTURE, pButton -> {
                NuclearCraft.packetHandler().sendToServer(new PacketGuiButtonPress(pos, BTN_ID));
            });
        }

        public List<ITextComponent> getTooltips() {
            return Arrays.asList(
                    new TranslationTextComponent("gui.nc.reactor_comparator_config.tooltip_"+mode),
                    new TranslationTextComponent("gui.nc.reactor_comparator_strength.tooltip", strength)
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
