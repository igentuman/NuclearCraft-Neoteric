package igentuman.nc.item;

import igentuman.nc.handler.ItemEnergyHandler;
import igentuman.nc.setup.registration.NcParticleTypes;
import igentuman.nc.util.CapabilityUtils;
import igentuman.nc.util.CustomEnergyStorage;
import igentuman.nc.util.RayTraceUtils;
import igentuman.nc.util.TextUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.PickaxeItem;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Direction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.tags.BlockTags;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.ItemHandlerHelper;
import org.antlr.v4.runtime.misc.NotNull;;

import javax.annotation.Nonnull;
import java.util.*;

import org.apache.commons.lang3.tuple.Pair;

import static igentuman.nc.handler.config.CommonConfig.ENERGY_STORAGE;
import static igentuman.nc.handler.event.client.BlockOverlayHandler.getArea;
import static igentuman.nc.setup.registration.NCSounds.ITEM_CHARGED;
import static net.minecraft.util.math.MathHelper.hsvToRgb;
import static net.minecraftforge.energy.CapabilityEnergy.ENERGY;

public class QNP extends PickaxeItem
{
	public QNP(IItemTier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties) {
		super(pTier, pAttackDamageModifier, pAttackSpeedModifier, pProperties);
	}

	@Override
	public boolean isRepairable(@Nonnull ItemStack stack)
	{
		return false;
	}
	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book)
	{
		return true;
	}


	protected int getEnergyMaxStorage() {
		return ENERGY_STORAGE.QNP_ENERGY_STORAGE.get();
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		CustomEnergyStorage energyStorage = getEnergy(stack);
		float chargeRatio = (float) energyStorage.getEnergyStored() / (float) getEnergyMaxStorage();
		return (int) Math.min(13, 13*chargeRatio);
	}


	@Override
	public boolean isDamaged(ItemStack stack) {
		return false;
	}

	@Override
	public boolean isCorrectToolForDrops(BlockState state) {
		return true;
	}
	@Override
	public void setDamage(ItemStack stack, int damage)
	{
	}


	@Override
	public boolean showDurabilityBar(ItemStack pStack) {
		return true;
	}

	public List<ItemStack> mineArea(BlockPos pos, World worldIn, LivingEntity entityLiving, BlockRayTraceResult blockResult, ItemStack stack, List<ItemStack> totalDrops) {
		Direction facing = blockResult.getDirection();
		Mode miningMode = getMode(stack);
		Pair<BlockPos, BlockPos> area = getArea(pos, facing, miningMode.radius, miningMode.depth);

		BlockPos.betweenClosed(area.getLeft(), area.getRight()).forEach(blockPos -> {
			if (enoughEnergy(stack) && worldIn.getBlockEntity(blockPos) == null && worldIn instanceof ServerWorld && entityLiving instanceof ServerPlayerEntity && !worldIn.isEmptyBlock(blockPos)) {
				BlockState tempState = worldIn.getBlockState(blockPos);
				//if (!tempState.is(BlockTags.O) && !tempState.is(BlockTags.MINEABLE_WITH_SHOVEL)) return;
				if (tempState.getDestroySpeed(worldIn, blockPos) < 0) return;
				harvestBlock(blockPos, worldIn, entityLiving, stack, false, totalDrops);
			}
		});
		worldIn.getEntitiesOfClass(ExperienceOrbEntity.class, new AxisAlignedBB(area.getLeft(), area.getRight()).inflate(1)).forEach(entityXPOrb -> entityXPOrb.teleportTo(entityLiving.blockPosition().getX(), entityLiving.blockPosition().getY(), entityLiving.blockPosition().getZ()));
		return totalDrops;
	}

	public static Mode getMode(ItemStack stack) {
		CompoundNBT tag = stack.getOrCreateTag();
		if(!tag.contains("mode")) {
			tag.putInt("mode", Mode.ONE_BLOCK.ordinal());
			stack.save(tag);
		}
		return Mode.values()[tag.getInt("mode")];
	}

	public boolean mineBlock(@NotNull ItemStack stack, @NotNull World worldIn, @NotNull BlockState state, @NotNull BlockPos pos, @NotNull LivingEntity entityLiving) {
		if (entityLiving instanceof PlayerEntity) {
			RayTraceResult rayTraceResult = RayTraceUtils.rayTraceSimple(worldIn, entityLiving, 16, 0);
			if (rayTraceResult.getType() == RayTraceResult.Type.BLOCK) {
				BlockRayTraceResult blockResult = (BlockRayTraceResult) rayTraceResult;
				List<ItemStack> totalDrops = new ArrayList<>();
				if(getMode(stack) == Mode.VEIN_MINER) {
					mineVein(pos, worldIn, entityLiving, blockResult, stack, totalDrops);
				} else {
					mineArea(pos, worldIn, entityLiving, blockResult, stack, totalDrops);
				}
				totalDrops.forEach(itemStack -> {
					Block.popResource(worldIn, entityLiving.blockPosition().relative(entityLiving.getDirection(), 1), itemStack);
				});
			}
		}
		return super.mineBlock(stack, worldIn, state, pos, entityLiving);
	}

	public int veinMinedBlocksCounter = 0;
	private List<ItemStack> harvestBlock(BlockPos pos, World worldIn, LivingEntity entityLiving, ItemStack tool, boolean veinMode, List<ItemStack> totalDrops) {
		BlockState tempState = worldIn.getBlockState(pos);
		Block block = tempState.getBlock();
		if(!enoughEnergy(tool)) return totalDrops;
		int xp = ForgeHooks.onBlockBreakEvent(worldIn, ((ServerPlayerEntity) entityLiving).gameMode.getGameModeForPlayer(), (ServerPlayerEntity) entityLiving, pos);
		if (xp >= 0) {
			//block.playerDestroy(worldIn, (PlayerEntity) entityLiving, pos, tempState, worldIn.getBlockEntity(pos), tool);
			block.destroy(worldIn, pos, tempState);
			Block.getDrops(tempState, (ServerWorld) worldIn, pos, null, (PlayerEntity) entityLiving, tool).forEach(itemStack -> {
				boolean combined = false;
				for (ItemStack drop : totalDrops) {
					if (ItemHandlerHelper.canItemStacksStack(drop, itemStack)) {
						drop.setCount(drop.getCount() + itemStack.getCount());
						combined = true;
						break;
					}
				}
				if (!combined) {
					totalDrops.add(itemStack);
				}
			});
			Random random = new Random();
			((ServerWorld) worldIn).sendParticles(NcParticleTypes.RADIATION.get(), pos.getX() + (random.nextFloat() - 0.5), pos.getY() + (random.nextFloat() - 0.5),
					pos.getZ() + (random.nextFloat() - 0.5), 3, 0, 0, 0, 0);
			getEnergy(tool).extractEnergy(ENERGY_STORAGE.QNP_ENERGY_PER_BLOCK.get(), false);
			if(veinMode && veinMinedBlocksCounter < 20) {
				veinMinedBlocksCounter++;
				for (Direction facing : Direction.values()) {
					BlockPos newPos = pos.relative(facing);
					if (worldIn.getBlockState(newPos).equals(tempState)) {
						harvestBlock(newPos, worldIn, entityLiving, tool, true, totalDrops);
					}
				}
			}
			block.popExperience((ServerWorld) worldIn, pos, xp);
			getEnergy(tool).extractEnergy(ENERGY_STORAGE.QNP_ENERGY_PER_BLOCK.get(), false);
		}
		return totalDrops;
	}

	private void mineVein(BlockPos pos, World worldIn, LivingEntity entityLiving, BlockRayTraceResult blockResult, ItemStack stack, List<ItemStack> totalDrops) {
		BlockState initialBlockState = worldIn.getBlockState(pos);
		//if (!initialBlockState.is(BlockTags.MINEABLE_WITH_PICKAXE) && !initialBlockState.is(BlockTags.MINEABLE_WITH_SHOVEL)) return;
		if (initialBlockState.getDestroySpeed(worldIn, pos) < 0) return;
		//mine initialblock always
		harvestBlock(pos, worldIn, entityLiving, stack, false, totalDrops);

		getEnergy(stack).extractEnergy(ENERGY_STORAGE.QNP_ENERGY_PER_BLOCK.get(), false);
		veinMinedBlocksCounter++;
		if(initialBlockState.is(Tags.Blocks.ORES)) {
			//mine all blocks of the same type
			for (Direction facing : Direction.values()) {
				BlockPos newPos = pos.relative(facing);
				if (worldIn.getBlockState(newPos).equals(initialBlockState)) {
					harvestBlock(newPos, worldIn, entityLiving, stack, true, totalDrops);
				}
			}
		}
		veinMinedBlocksCounter = 0;
	}

	private boolean enoughEnergy(ItemStack itemStack) {

		if(getMode(itemStack).radius == 0) return getEnergy(itemStack).getEnergyStored() > 100;
		int fe = 100*(getMode(itemStack).radius*2+1)*(getMode(itemStack).radius*2+1);
		if(getMode(itemStack).depth) fe *= getMode(itemStack).radius*2+1;
		return getEnergy(itemStack).getEnergyStored() > fe;
	}

	@Override
	public float getDestroySpeed(@NotNull ItemStack stack, @NotNull BlockState state) {
		if(getEnergy(stack).getEnergyStored() > ENERGY_STORAGE.QNP_ENERGY_PER_BLOCK.get()) return getTier().getSpeed();
		return 0.1F;
	}

	public boolean chargeFromEnergyBlock(TileEntity be, ItemStack tool)
	{
		if(be == null) return false;
		if(getEnergy(tool).getEnergyStored() == getEnergy(tool).getMaxEnergyStored()) return false;
		for(Direction side: Direction.values()) {
			IEnergyStorage storage = be.getCapability(ENERGY, side).orElse(null);
			if (storage == null) return false;
			if (storage.canExtract()) {
				int energy = storage.extractEnergy(getEnergy(tool).receiveEnergy(storage.extractEnergy(getEnergy(tool).getMaxEnergyStored() - getEnergy(tool).getEnergyStored(), true), false), false);
				getEnergy(tool).receiveEnergy(energy, false);
				return true;
			}
		}

		IEnergyStorage storage = be.getCapability(ENERGY).orElse(null);
		if (storage == null) return false;
		if (storage.canExtract()) {
			int energy = storage.extractEnergy(getEnergy(tool).receiveEnergy(storage.extractEnergy(getEnergy(tool).getMaxEnergyStored() - getEnergy(tool).getEnergyStored(), true), false), false);
			getEnergy(tool).receiveEnergy(energy, false);
			return true;
		}
		return false;
	}


	@Override
	public @NotNull ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
		if(context.getLevel().isClientSide) return super.onItemUseFirst(stack, context);
		TileEntity be = context.getLevel().getBlockEntity(context.getClickedPos());
		if(chargeFromEnergyBlock(be, context.getItemInHand())) {
			//Objects.requireNonNull(context.getPlayer()).playSound(ITEM_CHARGED.get(), 0.5f, 1f);
			return ActionResultType.SUCCESS;
		}
		return ActionResultType.PASS;
	}


	@Override
	public ActionResult<ItemStack> use(World pLevel, PlayerEntity pPlayer, Hand pUsedHand) {
		if(pLevel.isClientSide) return super.use(pLevel, pPlayer, pUsedHand);
		if(pPlayer.isSteppingCarefully()) {
			ItemStack tool = pPlayer.getItemInHand(pUsedHand);
			Mode miningMode = Mode.values()[(getMode(tool).ordinal()+1)%Mode.values().length];
			tool.getOrCreateTag().putInt("mode", miningMode.ordinal());
			pPlayer.sendMessage(new TranslationTextComponent("tooltip.nc.qnp_mode", new TranslationTextComponent("tooltip.mode." + miningMode.getName())).withStyle(TextFormatting.GREEN), pPlayer.getUUID());
			return ActionResult.success(pPlayer.getItemInHand(pUsedHand));
		}
		return super.use(pLevel, pPlayer, pUsedHand);
	}


	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt) {
		return new ItemEnergyHandler(stack, getEnergyMaxStorage(), 0, getEnergyMaxStorage()/4);
	}

	public CustomEnergyStorage getEnergy(ItemStack stack)
	{
		return (CustomEnergyStorage) CapabilityUtils.getPresentCapability(stack, ENERGY);
	}

	@Override
	public void appendHoverText(ItemStack stack, @javax.annotation.Nullable World world, List<ITextComponent> list, ITooltipFlag flag)
	{
		list.add(new TranslationTextComponent("tooltip.nc.qnp_mode", new TranslationTextComponent("tooltip.mode." + getMode(stack).getName())).withStyle(TextFormatting.BLUE));
		list.add(new TranslationTextComponent("tooltip.nc.shift_rbm_to_change").withStyle(TextFormatting.GRAY));
		list.add(new TranslationTextComponent("tooltip.nc.energy_stored", formatEnergy(getEnergy(stack).getEnergyStored()), formatEnergy(getEnergyMaxStorage())).withStyle(TextFormatting.BLUE));

	}

	public String formatEnergy(int energy)
	{
		return TextUtils.numberFormat(energy/1000)+" KFE";
	}

	public enum Mode {
		ONE_BLOCK("one_block", 0, false),
		THREE_BY_THREE("3x3", 1, false),
		THREE_BY_THREE_BY_THREE("3x3x3", 1, true),
		FIVE_BY_FIVE("5x5", 2, false),
		VEIN_MINER("vein", 0, false);

		private final String name;
		public final int radius;
		public final boolean depth;

		public String getName()
		{
			return name;
		}

		Mode(String name, int radius, boolean depth) {
			this.name = name;
			this.radius = radius;
			this.depth = depth;
		}
	}
}
