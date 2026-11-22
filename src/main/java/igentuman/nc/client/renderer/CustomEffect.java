package igentuman.nc.client.renderer;

import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public class CustomEffect {

    private final int gridSize;
    private final Identifier texture;

    private Vec3 pos = Vec3.ZERO;
    private int[] color = {255, 255, 255, 255};
    private float scale = 1F;

    protected int ticker;

    public CustomEffect(Identifier texture) {
        this(texture, 4);
    }

    public CustomEffect(Identifier texture, int gridSize) {
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

    public Identifier getTexture() {
        return texture;
    }

    public int getTextureGridSize() {
        return gridSize;
    }
}
