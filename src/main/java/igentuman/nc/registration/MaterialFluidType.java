package igentuman.nc.registration;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.FluidType;
import org.joml.Vector3f;

/**
 * A FluidType for material fluids that uses vanilla water textures with a color tint.
 */
public class MaterialFluidType extends FluidType {

    private final ResourceLocation stillTexture;
    private final ResourceLocation flowingTexture;
    private final ResourceLocation overlayTexture;
    private final int tintColor;

    public MaterialFluidType(Properties properties, int tintColor) {
        super(properties);
        this.tintColor = tintColor;
        this.stillTexture = ResourceLocation.withDefaultNamespace("block/water_still");
        this.flowingTexture = ResourceLocation.withDefaultNamespace("block/water_flow");
        this.overlayTexture = ResourceLocation.withDefaultNamespace("block/water_overlay");
    }

    public ResourceLocation getStillTexture() {
        return stillTexture;
    }

    public ResourceLocation getFlowingTexture() {
        return flowingTexture;
    }

    public ResourceLocation getOverlayTexture() {
        return overlayTexture;
    }

    public int getTintColor() {
        return tintColor;
    }

    /**
     * Returns the tint color as a Vector3f (RGB 0-1 range) for rendering.
     */
    public Vector3f getTintColorVec() {
        float r = ((tintColor >> 16) & 0xFF) / 255.0f;
        float g = ((tintColor >> 8) & 0xFF) / 255.0f;
        float b = (tintColor & 0xFF) / 255.0f;
        return new Vector3f(r, g, b);
    }
}
