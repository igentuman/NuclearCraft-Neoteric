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

public class CraftingPatternRenderer extends BlockEntityWithoutLevelRenderer {

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
        ItemStack out = CraftingPattern.output(stack);
        if (out.isEmpty() || out.getItem() instanceof CraftingPatternItem) return;
        Minecraft mc = Minecraft.getInstance();
        pose.pushPose();
        pose.translate(0.5D, 0.5D, 0.5D);
        mc.getItemRenderer().renderStatic(out, ctx, light, overlay, pose, buffer, mc.level, 0);
        pose.popPose();
    }
}
