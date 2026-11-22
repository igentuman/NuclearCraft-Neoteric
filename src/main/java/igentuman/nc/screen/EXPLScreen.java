package igentuman.nc.screen;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.minecraft.client.renderer.RenderPipelines;
import igentuman.nc.container.EXPLContainer;
import igentuman.nc.network.PacketExplBurst;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.TextUtils.formatEnergy;

/** GUI for the EXPL laser emitter: shows accumulated and target charge with a burst trigger button. */
public class EXPLScreen extends AbstractContainerScreen<EXPLContainer> {

    private static final Identifier GUI = rl("textures/gui/small_window.png");

    private Button burstButton;

    public EXPLScreen(EXPLContainer menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 112, 126);
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = (imageWidth - font.width(title)) / 2;
        burstButton = Button.builder(__("gui.nuclearcraft.button.burst"), b -> {
            if (menu.isReady()) {
                ClientPacketDistributor.sendToServer(new PacketExplBurst(menu.getPosition()));
            }
        }).bounds(leftPos + 13, topPos + 46, 86, 20).build();
        addRenderableWidget(burstButton);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, GUI, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        burstButton.active = menu.isReady();
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.centeredText(font, title, imageWidth / 2, titleLabelY, 0xFFFFFFFF);
        graphics.centeredText(font,
                __("screen.nuclearcraft.expl.charge", formatEnergy(menu.getCharge())),
                imageWidth / 2, titleLabelY + 16, 0xFFFFFFFF);
        graphics.centeredText(font,
                __("screen.nuclearcraft.expl.target_charge", formatEnergy(menu.getTargetCharge())),
                imageWidth / 2, titleLabelY + 26, 0xFFFFFFFF);
    }
}
