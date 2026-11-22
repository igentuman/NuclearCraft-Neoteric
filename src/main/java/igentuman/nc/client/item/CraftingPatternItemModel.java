package igentuman.nc.client.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import igentuman.nc.handler.crafter.CraftingPattern;
import igentuman.nc.item.CraftingPatternItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

import static igentuman.nc.NuclearCraft.rl;

public class CraftingPatternItemModel implements ItemModel {

    public static final Identifier ID = rl("crafting_pattern");

    private final ItemModel base;

    public CraftingPatternItemModel(ItemModel base) {
        this.base = base;
    }

    @Override
    public void update(ItemStackRenderState output, ItemStack stack, ItemModelResolver resolver,
                       ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        if (!CraftingPattern.isEncoded(stack)) {
            base.update(output, stack, resolver, displayContext, level, owner, seed);
            return;
        }
        output.appendModelIdentityElement(this);
        CraftingPattern pattern = CraftingPattern.from(stack);
        if (pattern == null) return;
        ItemStack out = pattern.output();
        if (out.isEmpty() && !pattern.fluidOutputs().isEmpty()) {
            out = new ItemStack(pattern.fluidOutputs().getFirst().getFluid().getBucket());
        }
        // An encoded pattern that cannot show an output renders nothing, as the old item renderer did.
        if (out.isEmpty() || out.getItem() instanceof CraftingPatternItem) return;

        resolver.appendItemLayers(output, out, displayContext, level, owner, seed);

        if (displayContext == ItemDisplayContext.GUI) {
            ItemStack badge = new ItemStack(pattern.isProcessing() ? Items.FURNACE : Items.CRAFTING_TABLE);
            ItemStackRenderState badgeState = new ItemStackRenderState();
            resolver.updateForTopItem(badgeState, badge, displayContext, level, owner, seed);
            output.appendModelIdentityElement(badge.getItem());
            output.newLayer().setupSpecialModel(BadgeRenderer.INSTANCE, badgeState);
        }
    }

    private static final class BadgeRenderer implements SpecialModelRenderer<ItemStackRenderState> {
        static final BadgeRenderer INSTANCE = new BadgeRenderer();

        @Override
        public void submit(@Nullable ItemStackRenderState badge, PoseStack pose, SubmitNodeCollector collector,
                           int light, int overlay, boolean hasFoil, int outlineColor) {
            if (badge == null) return;
            pose.pushPose();
            pose.translate(0.78F, 0.22F, 0.8F);
            pose.scale(0.45F, 0.45F, 0.45F);
            badge.submit(pose, collector, light, overlay, outlineColor);
            pose.popPose();
        }

        @Override
        public void getExtents(Consumer<Vector3fc> output) {
        }

        @Override
        public @Nullable ItemStackRenderState extractArgument(ItemStack stack) {
            return null;
        }
    }

    public record Unbaked(ItemModel.Unbaked base) implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                ItemModels.CODEC.fieldOf("base").forGetter(Unbaked::base)
        ).apply(i, Unbaked::new));

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            base.resolveDependencies(resolver);
        }

        @Override
        public ItemModel bake(BakingContext context, Matrix4fc transformation) {
            return new CraftingPatternItemModel(base.bake(context, transformation));
        }
    }
}
