package igentuman.nc.block;

import igentuman.nc.setup.registration.NCFluids;
import igentuman.nc.util.NCDamageSources;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.capability.wrappers.FluidBlockWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static net.minecraft.world.level.block.Blocks.AIR;

public class NCFluidBlock extends LiquidBlock
{
    private static NCFluids.FluidEntry entryStatic;
	private final NCFluids.FluidEntry entry;
	@Nullable
	private MobEffect effect;
	private int duration;
	private int level;

	public NCFluidBlock(NCFluids.FluidEntry entry, Properties props)
	{
		super(entry.getStillGetter(), Util.make(props, $ -> entryStatic = entry).noOcclusion().noCollission());
		this.entry = entry;
		entryStatic = null;
	}
	@Override
	public boolean isFireSource(BlockState state, LevelReader level, BlockPos pos, Direction direction)
	{
		return getFluid().getAttributes().getTemperature() > 600;
	}


	@Override
	public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos)
	{
		if(getFluid().getAttributes().getTemperature() > 600) {
			return 5;
		}
		return 0;
	}

	@Override
	protected void createBlockStateDefinition(@Nonnull Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		for(Property<?> p : (entry==null?entryStatic: entry).properties())
			builder.add(p);
	}

	@Nonnull
	@Override
	public FluidState getFluidState(@Nonnull BlockState state)
	{
		FluidState baseState = super.getFluidState(state);
		for(Property<?> prop : baseState.getProperties())
			if(state.hasProperty(prop))
				baseState = withCopiedValue(prop, baseState, state);
		return baseState;
	}

	public static <T extends StateHolder<?, T>, S extends Comparable<S>>
	T withCopiedValue(Property<S> prop, T oldState, StateHolder<?, ?> copyFrom)
	{
		return oldState.setValue(prop, copyFrom.getValue(prop));
	}

	public void setEffect(@Nonnull MobEffect effect, int duration, int level)
	{
		this.effect = effect;
		this.duration = duration;
		this.level = level;
	}

	@Override
	public void entityInside(@Nonnull BlockState state, @Nonnull Level worldIn, @Nonnull BlockPos pos, @Nonnull Entity entityIn)
	{
		super.entityInside(state, worldIn, pos, entityIn);
		if(effect!=null&&entityIn instanceof LivingEntity)
			((LivingEntity)entityIn).addEffect(new MobEffectInstance(effect, duration, level));
/*		if(getFluid().getFluidType().getTemperature() > 600) {
			entityIn.setSecondsOnFire(1);
		}
		if(getFluid().getFluidType().toString().contains("acid")) {
			entityIn.hurt(NCDamageSources.ACID, 1.0F);
		}*/
	}

	public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
		super.onPlace(pState, pLevel, pPos, pOldState, pIsMoving);
/*
		if(pLevel.getFluidState(pPos).getFluidType().getDensity() == -1000) {
			pLevel.setBlock(pPos, AIR.defaultBlockState(), 3);
		}
*/

	}
}
