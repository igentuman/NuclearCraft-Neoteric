package igentuman.nc.client.render.builder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;

import java.util.HashMap;
import java.util.Map;

public class FakeStructureLevel implements BlockAndTintGetter {

    private final Map<BlockPos, BlockState> blocks = new HashMap<>();

    public void clear() {
        blocks.clear();
    }

    public void put(BlockPos pos, BlockState state) {
        blocks.put(pos.immutable(), state);
    }

    public Map<BlockPos, BlockState> getBlocks() {
        return blocks;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return blocks.getOrDefault(pos, Blocks.AIR.defaultBlockState());
    }

    @Override
    public net.minecraft.world.level.block.entity.BlockEntity getBlockEntity(BlockPos pos) {
        return null;
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return getBlockState(pos).getFluidState();
    }

    @Override
    public CardinalLighting cardinalLighting() {
        return CardinalLighting.DEFAULT;
    }

    @Override
    public int getBlockTint(BlockPos pos, ColorResolver resolver) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            return resolver.getColor(mc.level.getBiome(pos).value(), pos.getX(), pos.getZ());
        }
        return 0xFFFFFF;
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return LevelLightEngine.EMPTY;
    }

    @Override
    public int getBrightness(LightLayer layer, BlockPos pos) {
        return 15;
    }

    @Override
    public int getRawBrightness(BlockPos pos, int minLight) {
        return 15;
    }

    @Override
    public int getMinY() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 256;
    }
}
