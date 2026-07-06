package igentuman.nc.registration;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.FluidType;
import org.joml.Vector3f;

import static igentuman.nc.NuclearCraft.rl;

/**
 * FluidType for material fluids. Picks the still/flow textures by fluid kind
 * (molten / gas / liquid) and carries the ARGB tint color used for rendering.
 */
public class MaterialFluidType extends FluidType {

    private final ResourceLocation stillTexture;
    private final ResourceLocation flowingTexture;
    private final ResourceLocation overlayTexture;
    private final int tintColor;
    private final boolean molten;
    private final boolean gas;
    private final boolean toxic;

    public MaterialFluidType(Properties properties, int tintColor, FluidDefinition def) {
        super(properties);
        this.tintColor = tintColor;
        this.molten = def.isMolten;
        this.gas = def.isGas;
        this.toxic = def.isToxic;

        if (def.isMolten) {
            this.stillTexture = rl("block/fluid/molten_still");
            this.flowingTexture = rl("block/fluid/molten_flow");
        } else if (def.isGas) {
            this.stillTexture = rl("block/fluid/gas");
            this.flowingTexture = rl("block/fluid/gas");
        } else {
            this.stillTexture = rl("block/fluid/liquid_still");
            this.flowingTexture = rl("block/fluid/liquid_flow");
        }
        this.overlayTexture = null;
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

    public boolean isMolten() {
        return molten;
    }

    public boolean isGas() {
        return gas;
    }

    public boolean isToxic() {
        return toxic;
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
