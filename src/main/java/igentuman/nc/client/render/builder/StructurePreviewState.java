package igentuman.nc.client.render.builder;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * Picture-in-picture state for a rotating 3D structure preview inside a GUI rectangle.
 *
 * @param blocks      stable snapshot keyed relative to the minimum corner; replace the map when the layout changes
 * @param blockEntities detached states extracted for this GUI frame, keyed relative to the minimum corner
 * @param sw          structure width (x)
 * @param sh          structure height (y)
 * @param sd          structure depth (z)
 * @param yaw         rotation around the vertical axis, degrees
 * @param pitch       tilt towards the viewer, degrees
 * @param sliceLevel  layers at or above this y are hidden; {@code -1} shows all
 * @param boundingBox draw the green structure bounds
 * @param scale       GUI pixels per block
 */
public record StructurePreviewState(
        Map<BlockPos, BlockState> blocks,
        Map<BlockPos, BlockEntityRenderState> blockEntities,
        int sw, int sh, int sd,
        float yaw, float pitch,
        int sliceLevel,
        boolean boundingBox,
        int x0, int y0, int x1, int y1,
        float scale,
        @Nullable ScreenRectangle scissorArea,
        @Nullable ScreenRectangle bounds
) implements PictureInPictureRenderState {
}
