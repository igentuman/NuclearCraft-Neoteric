package igentuman.nc.client.renderer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

/**
 * Generic descriptor for a camera-facing (billboarded) sprite effect rendered by
 * {@link BillboardingEffectRenderer}. Not tied to any specific block or entity.
 *
 * <p>The texture is treated as a square sprite-sheet of {@code gridSize x gridSize} frames; the
 * current frame advances with the render tick to animate. Subclass and override {@link #getPos}
 * (and call {@link #tick()}) to drive position/animation from your own logic.
 */
public class CustomEffect {

    private final int gridSize;
    private final ResourceLocation texture;

    private Vec3 pos = Vec3.ZERO;
    private int[] color = {255, 255, 255, 255};
    private float scale = 1F;

    protected int ticker;

    public CustomEffect(ResourceLocation texture) {
        this(texture, 4);
    }

    public CustomEffect(ResourceLocation texture, int gridSize) {
        this.texture = texture;
        this.gridSize = gridSize;
    }

    /** Advance internal animation state. Return {@code true} when the effect is finished. */
    public boolean tick() {
        ticker++;
        return false;
    }

    public void setPos(Vec3 pos) {
        this.pos = pos;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void setColor(int r, int g, int b, int a) {
        this.color = new int[]{r, g, b, a};
    }

    /** @return RGBA color components (0-255) applied to every vertex. */
    public int[] getColor() {
        return color;
    }

    public Vec3 getPos(float partialTick) {
        return pos;
    }

    public float getScale() {
        return scale;
    }

    public ResourceLocation getTexture() {
        return texture;
    }

    public int getTextureGridSize() {
        return gridSize;
    }
}
