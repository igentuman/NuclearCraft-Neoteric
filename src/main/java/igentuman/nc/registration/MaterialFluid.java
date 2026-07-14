package igentuman.nc.registration;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

/** Holds the registered fluid objects for a material fluid (source, flowing, block, bucket, fluid type). */
public class MaterialFluid {
    private final DeferredHolder<FluidType, FluidType> fluidType;
    private final DeferredHolder<Fluid, FlowingFluid> source;
    private final DeferredHolder<Fluid, FlowingFluid> flowing;
    private final DeferredBlock<LiquidBlock> fluidBlock;
    private final DeferredItem<Item> bucket;

    public MaterialFluid(
            DeferredHolder<FluidType, FluidType> fluidType,
            DeferredHolder<Fluid, FlowingFluid> source,
            DeferredHolder<Fluid, FlowingFluid> flowing,
            DeferredBlock<LiquidBlock> fluidBlock,
            DeferredItem<Item> bucket
    ) {
        this.fluidType = fluidType;
        this.source = source;
        this.flowing = flowing;
        this.fluidBlock = fluidBlock;
        this.bucket = bucket;
    }

    public DeferredHolder<FluidType, FluidType> fluidType() { return fluidType; }
    public DeferredHolder<Fluid, FlowingFluid> source() { return source; }
    public DeferredHolder<Fluid, FlowingFluid> flowing() { return flowing; }
    public DeferredBlock<LiquidBlock> fluidBlock() { return fluidBlock; }
    public DeferredItem<Item> bucket() { return bucket; }
}
