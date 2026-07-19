package igentuman.nc.screen;

import igentuman.nc.container.EXPLContainer;
import igentuman.nc.network.PacketExplBurst;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.TextUtils.formatEnergy;

/** GUI for the EXPL laser emitter: shows the accumulated charge and a burst trigger button. */
public class EXPLScreen extends AbstractContainerScreen<EXPLContainer> {

    private static final ResourceLocation GUI = rl("textures/gui/small_window.png");

    private Button burstButton;

    public EXPLScreen(EXPLContainer menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 112;
        imageHeight = 126;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = (imageWidth - font.width(title)) / 2;
        burstButton = Button.builder(__("gui.nuclearcraft.button.burst"), b -> {
            if (menu.isReady()) {
                PacketDistributor.sendToServer(new PacketExplBurst(menu.getPosition()));
            }
        }).bounds(leftPos + 13, topPos + 46, 86, 20).build();
        addRenderableWidget(burstButton);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(GUI, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        burstButton.active = menu.isReady();
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawCenteredString(font, title, imageWidth / 2, titleLabelY, 0xFFFFFF);
        graphics.drawCenteredString(font,
                __("screen.nuclearcraft.expl.charge", formatEnergy(menu.getCharge())),
                imageWidth / 2, titleLabelY + 16, 0xFFFFFF);
    }
}
