package igentuman.nc.client.render.builder;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
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
    public float getShade(Direction direction, boolean shade) {
        if (!shade) return 1.0f;
        return switch (direction) {
            case DOWN -> 0.5f;
            case UP -> 1.0f;
            case NORTH, SOUTH -> 0.8f;
            case EAST, WEST -> 0.6f;
        };
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
        Minecraft mc = Minecraft.getInstance();
        return mc.level != null ? mc.level.getLightEngine() : null;
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
    public int getMinBuildHeight() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 256;
    }
}
