package igentuman.nc.client.render.builder;

import igentuman.nc.block.fusion.FusionReactorCoreBlock;
import igentuman.nc.block.kugelblitz.EXPLBlock;
import igentuman.nc.block.turbine.TurbineRotorBlock;
import igentuman.nc.client.render.fusion.FusionCoreRenderer;
import igentuman.nc.client.render.kugelblitz.EXPLRenderer;
import igentuman.nc.client.render.turbine.TurbineRotorRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.blockentity.state.SignRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class PreviewBlockEntities {
    // Exact classes deliberately exclude third-party overrides and scene-effect renderers.
    private static final Set<Class<?>> VANILLA_RENDERERS = Set.of(
            StandingSignRenderer.class, HangingSignRenderer.class, BannerRenderer.class,
            BedRenderer.class, ShulkerBoxRenderer.class, SkullBlockRenderer.class,
            EnchantTableRenderer.class);
    private static final Set<Class<?>> MACHINE_RENDERERS = Set.of(
            EXPLRenderer.class, FusionCoreRenderer.class, TurbineRotorRenderer.class);

    private PreviewBlockEntities() {}

    private static boolean supportsVanilla(Block block) {
        return (block instanceof SignBlock || block instanceof AbstractBannerBlock
                || block instanceof BedBlock || block instanceof ShulkerBoxBlock
                || block instanceof AbstractSkullBlock || block instanceof EnchantingTableBlock)
                && BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals("minecraft");
    }

    private static boolean supportsMachine(Block block) {
        Class<?> type = block.getClass();
        return type == EXPLBlock.class || type == FusionReactorCoreBlock.class || type == TurbineRotorBlock.class;
    }

    public static Map<BlockPos, BlockEntityRenderState> extract(Map<BlockPos, BlockState> blocks,
                                                               Map<BlockPos, CompoundTag> payloads, int slice) {
        Minecraft mc = Minecraft.getInstance();
        Map<BlockPos, BlockEntityRenderState> result = new HashMap<>();
        blocks.forEach((pos, blockState) -> {
            if ((slice >= 0 && pos.getY() >= slice) || !(blockState.getBlock() instanceof EntityBlock factory)) return;
            boolean vanilla = supportsVanilla(blockState.getBlock());
            if (!vanilla && !supportsMachine(blockState.getBlock())) return;
            BlockEntity entity = factory.newBlockEntity(pos, blockState);
            if (entity == null) return;
            BlockEntityRenderer<BlockEntity, BlockEntityRenderState> renderer = mc.getBlockEntityRenderDispatcher().getRenderer(entity);
            Set<Class<?>> allowedRenderers = vanilla ? VANILLA_RENDERERS : MACHINE_RENDERERS;
            if (renderer == null || !allowedRenderers.contains(renderer.getClass())) return;
            // Machine previews show the idle assembly from block states alone.
            CompoundTag payload = vanilla ? payloads.get(pos) : null;
            if (payload != null && mc.level != null) {
                // The block factory, rather than an untrusted payload id, chooses the type.
                entity.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, mc.level.registryAccess(), payload.copy()));
            }
            // Never attach a live level, tick the entity, or insert it into a world.
            BlockEntityRenderState state = renderer.createRenderState();
            renderer.extractRenderState(entity, state, 0f, Vec3.ZERO, null);
            state.lightCoords = LightCoordsUtil.FULL_BRIGHT;
            if (state instanceof SignRenderState sign) sign.drawOutline = true;
            result.put(pos.immutable(), state);
        });
        return Map.copyOf(result);
    }
}
