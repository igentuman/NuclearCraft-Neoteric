package igentuman.nc.block;

import igentuman.nc.fluid.NCFluid;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.state.StateHolder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public class NCFluidBlock  extends FlowingFluidBlock {
    private final NCFluid ieFluid;
    @Nullable
    private Effect effect;
    private int duration;
    private int level;
    private static NCFluid tempFluid;

    private static Supplier<NCFluid> supply(NCFluid fluid)
    {
        tempFluid = fluid;
        return () -> fluid;
    }

    public NCFluidBlock(NCFluid fluid)
    {
        super(supply(fluid), AbstractBlock.Properties.of(Material.WATER));
        this.ieFluid = fluid;
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder)
    {

        super.createBlockStateDefinition(builder);
        NCFluid f;
        if(ieFluid!=null)
            f = ieFluid;
        else
            f = tempFluid;
        builder.add(f.getStateDefinition().getProperties().toArray(new Property[0]));
    }

    @Nonnull
    @Override
    public FluidState getFluidState(@Nonnull BlockState state)
    {
        FluidState baseState = super.getFluidState(state);
        for(Property<?> prop : ieFluid.getStateDefinition().getProperties())
            if(prop!=FlowingFluidBlock.LEVEL)
                baseState = withCopiedValue(prop, baseState, state);
        return baseState;
    }

    public static <T extends StateHolder<?, T>, S extends Comparable<S>>
    T withCopiedValue(Property<S> prop, T oldState, StateHolder<?, ?> copyFrom)
    {
        return oldState.setValue(prop, copyFrom.getValue(prop));
    }

    public void setEffect(@Nonnull Effect effect, int duration, int level)
    {
        this.effect = effect;
        this.duration = duration;
        this.level = level;
    }

    @Override
    public void entityInside(@Nonnull BlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Entity entityIn)
    {
        super.entityInside(state, worldIn, pos, entityIn);
        if(effect!=null&&entityIn instanceof LivingEntity)
            ((LivingEntity)entityIn).addEffect(new EffectInstance(effect, duration, level));
    }
}
