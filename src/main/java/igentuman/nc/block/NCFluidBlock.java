package igentuman.nc.block;

import igentuman.nc.setup.registration.NCFluids;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.state.StateHolder;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Direction;
import net.minecraft.world.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


public class NCFluidBlock implements IFluidBlock
{
    private static NCFluids.FluidEntry entryStatic;
	//private final NCFluids.FluidEntry entry;
	@Nullable
	private Effect effect;
	private int duration;
	private int level;
/*
	public NCFluidBlock(NCFluids.FluidEntry entry, Properties props)
	{
		super(entry.getStillGetter(), Util.make(props, $ -> entryStatic = entry).noOcclusion().noCollission());
		this.entry = entry;
		entryStatic = null;
	}
	@Override
	public boolean isFireSource(BlockState state, IWorldReader level, BlockPos pos, Direction direction)
	{
		return getFluid().getAttributes().getTemperature() > 600;
	}


	@Override
	public int getLightValue(BlockState state, IBlockReader level, BlockPos pos)
	{
		if(getFluid().getAttributes().getTemperature() > 600) {
			return 5;
		}
		return 0;
	}*/

/*	@Override
	protected void createBlockStateDefinition(@Nonnull StateContainer.Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		//for(Property<?> p : (entry==null?entryStatic: entry))
		//	builder.add(p);
	}*/

/*	@Nonnull
	@Override
	public FluidState getFluidState(@Nonnull BlockState state)
	{
		FluidState baseState = super.getFluidState(state);
		for(Property<?> prop : baseState.getProperties())
			if(state.hasProperty(prop))
				baseState = withCopiedValue(prop, baseState, state);
		return baseState;
	}*/

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

	//@Override
	public void entityInside(@Nonnull BlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Entity entityIn)
	{
	//	super.entityInside(state, worldIn, pos, entityIn);
		if(effect!=null&&entityIn instanceof LivingEntity)
			((LivingEntity)entityIn).addEffect(new EffectInstance(effect, duration, level));
/*		if(getFluid().getFluidType().getTemperature() > 600) {
			entityIn.setSecondsOnFire(1);
		}
		if(getFluid().getFluidType().toString().contains("acid")) {
			entityIn.hurt(NCDamageSources.ACID, 1.0F);
		}*/
	}

	public void onPlace(BlockState pState, World pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
		//super.onPlace(pState, pLevel, pPos, pOldState, pIsMoving);
/*
		if(pLevel.getFluidState(pPos).getFluidType().getDensity() == -1000) {
			pLevel.setBlock(pPos, AIR.defaultBlockState(), 3);
		}
*/

	}

	@Override
	public Fluid getFluid() {
		return null;
	}

	@Override
	public int place(World world, BlockPos blockPos, @Nonnull FluidStack fluidStack, IFluidHandler.FluidAction fluidAction) {
		return 0;
	}

	@Nonnull
	@Override
	public FluidStack drain(World world, BlockPos blockPos, IFluidHandler.FluidAction fluidAction) {
		return null;
	}

	@Override
	public boolean canDrain(World world, BlockPos blockPos) {
		return false;
	}

	@Override
	public float getFilledPercentage(World world, BlockPos blockPos) {
		return 0;
	}
}
