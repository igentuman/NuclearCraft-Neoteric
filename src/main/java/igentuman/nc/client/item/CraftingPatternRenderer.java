package igentuman.nc.client.item;

import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.handler.crafter.CraftingPattern;
import igentuman.nc.item.CraftingPatternItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CraftingPatternRenderer extends BlockEntityWithoutLevelRenderer {

    private static final ItemStack CRAFTING_BADGE = new ItemStack(Items.CRAFTING_TABLE);
    private static final ItemStack PROCESSING_BADGE = new ItemStack(Items.FURNACE);

    private static CraftingPatternRenderer instance;

    public static CraftingPatternRenderer get() {
        if (instance == null) {
            Minecraft mc = Minecraft.getInstance();
            instance = new CraftingPatternRenderer(mc.getBlockEntityRenderDispatcher(), mc.getEntityModels());
        }
        return instance;
    }

    public CraftingPatternRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet models) {
        super(dispatcher, models);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext ctx, PoseStack pose, MultiBufferSource buffer, int light, int overlay) {
        CraftingPattern pattern = CraftingPattern.from(stack);
        if (pattern == null) return;
        Minecraft mc = Minecraft.getInstance();
        ItemStack out = pattern.output();
        if (out.isEmpty() && !pattern.fluidOutputs().isEmpty()) {
            out = new ItemStack(pattern.fluidOutputs().getFirst().getFluid().getBucket());
        }
        if (out.isEmpty() || out.getItem() instanceof CraftingPatternItem) return;

        pose.pushPose();
        pose.translate(0.5D, 0.5D, 0.5D);
        mc.getItemRenderer().renderStatic(out, ctx, light, overlay, pose, buffer, mc.level, 0);
        pose.popPose();

        // In inventories, badge the bottom-right corner with the pattern kind (grid vs machine).
        if (ctx == ItemDisplayContext.GUI) {
            pose.pushPose();
            pose.translate(0.78D, 0.22D, 0.8D);
            pose.scale(0.45F, 0.45F, 0.45F);
            ItemStack badge = pattern.isProcessing() ? PROCESSING_BADGE : CRAFTING_BADGE;
            mc.getItemRenderer().renderStatic(badge, ctx, light, overlay, pose, buffer, mc.level, 0);
            pose.popPose();
        }
    }
}
