package igentuman.nc.screen;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import igentuman.nc.block_entity.MultiblockBuilderBE;
import igentuman.nc.client.gui.fission.designer.DesignGrid;
import igentuman.nc.container.MultiblockBuilderContainer;
import igentuman.nc.network.PacketBuildMultiblock;
import igentuman.nc.network.PacketLoadFissionDesign;
import igentuman.nc.util.builder.DesignPreviewRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;

import static igentuman.nc.util.TextUtils.__;

/** Builder GUI: shows the loaded design preview and offers Load Plan / Build actions. */
public class MultiblockBuilderScreen extends AbstractContainerScreen<MultiblockBuilderContainer> {

    public HashMap<BlockPos, Block> blockMap = new HashMap<>();
    protected int relX;
    protected int relY;

    public MultiblockBuilderScreen(MultiblockBuilderContainer container, Inventory inv, Component name) {
        super(container, inv, name, 178, 200);
    }

    @Override
    protected void init() {
        super.init();
        relX = (this.width - this.imageWidth) / 2;
        relY = (this.height - this.imageHeight) / 2;
        blockMap = getMenu().getBlocksMap();

        addRenderableWidget(Button.builder(__("nc.multiblock_builder.load_plan"), b -> onLoad())
                .bounds(relX + 8, relY + imageHeight - 48, 162, 20).build());
        addRenderableWidget(Button.builder(__("nc.multiblock_builder.build"), b -> onBuild())
                .bounds(relX + 8, relY + imageHeight - 24, 162, 20).build());
    }

    protected void onLoad() {
        int slot = getMinecraft().player.getInventory().getSelectedSlot();
        ClientPacketDistributor.sendToServer(new PacketLoadFissionDesign(slot));
    }

    protected void onBuild() {
        if (blockMap.isEmpty()) return;
        ClientPacketDistributor.sendToServer(new PacketBuildMultiblock(
                getMenu().getPosition(), PacketBuildMultiblock.writeBlockMap(blockMap)));
    }

    public void applyLoadedDesign(DesignGrid grid) {
        blockMap = new HashMap<>(grid.cells);
        getMenu().setBlocksMap(blockMap);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(graphics, mouseX, mouseY, partialTicks);
        graphics.fill(relX, relY, relX + imageWidth, relY + imageHeight, 0xFF202020);
        graphics.fill(relX + 8, relY + 20, relX + imageWidth - 8, relY + imageHeight - 52, 0xFF101820);
        if (!blockMap.isEmpty()) {
            DesignPreviewRenderer.render(graphics, blockMap, relX + 8, relY + 20, imageWidth - 16, imageHeight - 72);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (!blockMap.isEmpty()) {
            Vec3i size = MultiblockBuilderBE.getSize(blockMap);
            graphics.text(font, (size.getX() + 1) + "x" + (size.getY() + 1) + "x" + (size.getZ() + 1),
                    8, 6, 0xFFE0E0E0, false);
        } else {
            graphics.text(font, this.title, 8, 6, 0xFFE0E0E0, false);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTicks);
    }
}
