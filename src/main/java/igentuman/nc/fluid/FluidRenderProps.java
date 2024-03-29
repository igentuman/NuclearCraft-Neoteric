package igentuman.nc.fluid;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;

import static igentuman.nc.NuclearCraft.rl;

public class FluidRenderProps implements IClientFluidTypeExtensions {
    private final ResourceLocation still;
    private final ResourceLocation flowing;
    private final int colorTint;

    public FluidRenderProps(String still, String flowing) {
        this(still, flowing, 0xCCFFFFFF);
    }

    public FluidRenderProps(String still, String flowing, int colorTint) {
        this.still = still.indexOf(':') > 0 ? new ResourceLocation(still) : rl("block/material/fluid/" + still);
        this.flowing = flowing.indexOf(':') > 0 ? new ResourceLocation(flowing) : rl("block/material/fluid/" + flowing);
        this.colorTint = colorTint;
    }

    public static FluidRenderProps gas(int colorTint) {
        return new FluidRenderProps("gas", "gas", colorTint);
    }

    @Override
    public ResourceLocation getStillTexture() {
        return still;
    }

    @Override
    public ResourceLocation getFlowingTexture() {
        return flowing;
    }

    @Override
    public int getTintColor() {
        return colorTint;
    }
}