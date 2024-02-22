package igentuman.nc.fluid;

import igentuman.nc.block.NCFluidBlock;
import igentuman.nc.setup.registration.NCFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fluids.FluidAttributes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;


public class NCFluid extends FlowingFluid
{
	private static NCFluids.FluidEntry entryStatic;
	protected final NCFluids.FluidEntry entry;
	protected final ResourceLocation stillTex;
	protected final ResourceLocation flowingTex;
	@Nullable
	protected final Consumer<FluidAttributes.Builder> buildAttributes;
	public static NCFluid makeFluid(FluidConstructor make, NCFluids.FluidEntry entry, ResourceLocation stillTex, ResourceLocation flowingTex, @Nullable Consumer<FluidAttributes.Builder> buildAttributes)
	{
		entryStatic = entry;
		NCFluid result = make.create(entry, stillTex, flowingTex, buildAttributes);
		entryStatic = null;
		return result;
	}

	public interface FluidConstructor
	{
		NCFluid create(
				NCFluids.FluidEntry entry,
				ResourceLocation stillTex, ResourceLocation flowingTex,
				@Nullable Consumer<FluidAttributes.Builder> buildAttributes
		);
	}

	public NCFluid(NCFluids.FluidEntry entry, ResourceLocation stillTex, ResourceLocation flowingTex, @Nullable Consumer<FluidAttributes.Builder> buildAttributes)
	{
		this.entry = entry;
		this.stillTex = stillTex;
		this.flowingTex = flowingTex;
		this.buildAttributes = buildAttributes;
	}

	@Nonnull
	@Override
	public Item getBucket()
	{
		return entry.getBucket();
	}

	@Override
	protected boolean canBeReplacedWith(FluidState fluidState, BlockGetter blockReader, BlockPos pos, Fluid fluidIn, Direction direction)
	{
		return direction==Direction.DOWN&&!isSame(fluidIn);
	}

	@Override
	public boolean isSame(@Nonnull Fluid fluidIn)
	{
		return fluidIn==entry.getStill()||fluidIn==entry.getFlowing();
	}

	@Override
	public int getTickDelay(LevelReader p_205569_1_)
	{
		return 2;
	}

	@Override
	protected float getExplosionResistance()
	{
		return 100;
	}

	@Override
	protected void createFluidStateDefinition(Builder<Fluid, FluidState> builder)
	{
		super.createFluidStateDefinition(builder);
		for(Property<?> p : (entry==null?entryStatic: entry).properties())
			builder.add(p);
	}

	@Nonnull
	@Override
	protected FluidAttributes createAttributes()
	{
		FluidAttributes.Builder builder = FluidAttributes.builder(stillTex, flowingTex);
		if(buildAttributes!=null)
			buildAttributes.accept(builder);
		return builder.build(this);
	}

	@Override
	protected BlockState createLegacyBlock(FluidState state)
	{
		BlockState result = entry.getBlock().defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
		for(Property<?> prop : entry.properties())
			result = NCFluidBlock.withCopiedValue(prop, result, state);
		return result;
	}

	@Override
	public boolean isSource(FluidState state)
	{
		return state.getType()==entry.getStill();
	}

	@Override
	public int getAmount(FluidState state)
	{
		if(isSource(state))
			return 8;
		else
			return state.getValue(LEVEL);
	}

	@Nonnull
	@Override
	public Fluid getFlowing()
	{
		return entry.getFlowing();
	}

	@Nonnull
	@Override
	public Fluid getSource()
	{
		return entry.getStill();
	}

	@Override
	protected boolean canConvertToSource()
	{
		return false;
	}

	@Override
	protected void beforeDestroyingBlock(LevelAccessor iWorld, BlockPos blockPos, BlockState blockState)
	{

	}

	@Override
	protected int getSlopeFindDistance(LevelReader iWorldReader)
	{
		return 4;
	}

	@Override
	protected int getDropOff(LevelReader iWorldReader)
	{
		return 1;
	}

	public static Consumer<FluidAttributes.Builder> createBuilder(int density, int viscosity)
	{
		return builder -> builder.viscosity(viscosity).density(density);
	}


	public static class Flowing extends NCFluid
	{
		public String name;
		public Flowing(NCFluids.FluidEntry entry, ResourceLocation stillTex, ResourceLocation flowingTex,
					   @Nullable Consumer<FluidAttributes.Builder> buildAttributes)
		{
			super(entry, stillTex, flowingTex, buildAttributes);
			//name = entry.getFlowing().getFluidType().toString().split(":")[1];
		}

		@Override
		protected void createFluidStateDefinition(Builder<Fluid, FluidState> builder)
		{
			super.createFluidStateDefinition(builder);
			builder.add(LEVEL);
		}

		protected void spread(LevelAccessor pLevel, BlockPos pPos, FluidState pState) {

			BlockState blockstate = pLevel.getBlockState(pPos);
			BlockPos blockpos = pPos.above();
			BlockState blockstate1 = pLevel.getBlockState(blockpos);
			FluidState fluidstate = this.getNewLiquid(pLevel, blockpos.below(), blockstate1);
			if (this.canSpreadTo(pLevel, pPos, blockstate, Direction.UP, blockpos, blockstate1, pLevel.getFluidState(blockpos), fluidstate.getType())) {
				this.spreadTo(pLevel, blockpos, blockstate1, Direction.UP, fluidstate);
			}
		}
	}
}
