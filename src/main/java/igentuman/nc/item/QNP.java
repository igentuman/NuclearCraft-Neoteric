package igentuman.nc.item;

import igentuman.api.platform.NCItemStacks;
import igentuman.api.platform.NCLevels;
import igentuman.nc.setup.registration.NcParticleTypes;
import igentuman.nc.util.capability.CustomEnergyStorage;
import igentuman.nc.util.RayTraceUtils;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;

import static igentuman.nc.block.entity.NuclearCraftBE.isGTEUCapEnabled;
import static igentuman.nc.compat.gregtech.GTUtils.formatEUEnergy;
import static igentuman.nc.compat.gregtech.GTUtils.isOnlyGTCEUCapEnabled;
import static igentuman.nc.handler.config.CommonConfig.ENERGY_STORAGE;
import static igentuman.nc.setup.registration.NCSounds.ITEM_CHARGED;
import static igentuman.nc.util.AreaUtil.getArea;
import static igentuman.nc.util.ModUtil.isGtLoaded;
import static igentuman.nc.util.TextUtils.__;

public class QNP extends PickaxeItem
{
	public QNP(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties) {
		super(pTier, pProperties);
	}

	/** Helper to get enchantment level from a ResourceKey (1.21.1 compat). */
	private static int getEnchantmentLevel(ItemStack stack, ResourceKey<Enchantment> key) {
		for (var entry : stack.getEnchantments().entrySet()) {
			if (entry.getKey().is(key)) {
				return entry.getIntValue();
			}
		}
		return 0;
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

	public boolean canEquip(ItemStack stack, EquipmentSlot armorType, Entity entity)
	{
		return false;
	}

	@Override
	public int getBarColor(@NotNull ItemStack pStack)
	{
		return Mth.hsvToRgb(Math.max(0.0F, getBarWidth(pStack)/(float)MAX_BAR_WIDTH)/3.0F, 1.0F, 1.0F);
	}

	public int getEnergyMaxStorage() {
		return ENERGY_STORAGE.QNP_ENERGY_STORAGE.get();
	}

    @Override
    public boolean canPerformAction(ItemStack stack, net.neoforged.neoforge.common.ItemAbility toolAction) {
        return super.canPerformAction(stack, toolAction) && enoughEnergy(stack);
    }

	@Override
	public int getBarWidth(@NotNull ItemStack stack) {
		CustomEnergyStorage energyStorage = getEnergy(stack);
		float chargeRatio = (float) energyStorage.getEnergyStored() / (float) getEnergyMaxStorage();
		return (int) Math.min(13, 13*chargeRatio);
	}

	@Override
	public boolean isDamaged(ItemStack stack) {
		return false;
	}

	@Override
	public boolean isCorrectToolForDrops(@NotNull ItemStack stack, @NotNull BlockState state) {
		return true;
	}
	@Override
	public void setDamage(ItemStack stack, int damage)
	{
	}

	@Override
	public boolean isBarVisible(@NotNull ItemStack pStack) {
		return true;
	}

	public List<ItemStack> mineArea(BlockPos pos, Level worldIn, LivingEntity entityLiving, BlockHitResult blockResult, ItemStack stack, List<ItemStack> totalDrops) {
		Direction facing = blockResult.getDirection();
		Mode miningMode = getMode(stack);
		Pair<BlockPos, BlockPos> area = getArea(pos, facing, miningMode.radius, miningMode.depth);

		BlockPos.betweenClosed(area.getLeft(), area.getRight()).forEach(blockPos -> {
			if (enoughEnergy(stack) && worldIn.getBlockEntity(blockPos) == null && worldIn instanceof ServerLevel && entityLiving instanceof ServerPlayer && !worldIn.isEmptyBlock(blockPos)) {
				BlockState tempState = worldIn.getBlockState(blockPos);
				if (!tempState.is(BlockTags.MINEABLE_WITH_PICKAXE) && !tempState.is(BlockTags.MINEABLE_WITH_SHOVEL)) return;
				if (tempState.getDestroySpeed(worldIn, blockPos) < 0) return;
				harvestBlock(blockPos, worldIn, entityLiving, stack, false, totalDrops);
			}
		});
		worldIn.getEntitiesOfClass(ExperienceOrb.class, AABB.encapsulatingFullBlocks(area.getLeft(), area.getRight()).inflate(1)).forEach(entityXPOrb -> entityXPOrb.teleportTo(entityLiving.blockPosition().getX(), entityLiving.blockPosition().getY(), entityLiving.blockPosition().getZ()));
		return totalDrops;
	}

	public static Mode getMode(@NotNull ItemStack stack) {
		if(!NCItemStacks.contains(stack, "mode")) {
			NCItemStacks.putInt(stack, "mode", Mode.ONE_BLOCK.ordinal());
		}
		return Mode.values()[NCItemStacks.getInt(stack, "mode")];
	}

	public int energyPerBlock(ItemStack stack)
	{
		int durability = getEnchantmentLevel(stack, Enchantments.UNBREAKING);
		if(durability > 0) {
			return (int) (ENERGY_STORAGE.QNP_ENERGY_PER_BLOCK.get() / Math.sqrt(durability + 1));
		}
		return ENERGY_STORAGE.QNP_ENERGY_PER_BLOCK.get();
	}

	public void consumeEnergy(ItemStack stack) {
		int energyPerBlock = energyPerBlock(stack);
		if (getEnergy(stack).getEnergyStored() > energyPerBlock) {
			getEnergy(stack).setEnergy(getEnergy(stack).getEnergyStored() - energyPerBlock);
			NCItemStacks.putInt(stack, "energy", getEnergy(stack).getEnergyStored());
		}
	}

	public static void dropResource(Level pLevel, BlockPos pPos, ItemStack pStack) {
		double d0 = (double) EntityType.ITEM.getHeight() / (double)2.0F;
		double d1 = (double)pPos.getX() + (double)0.5F + Mth.nextDouble(pLevel.random, (double)-0.25F, (double)0.25F);
		double d2 = (double)pPos.getY() + (double)0.5F + Mth.nextDouble(pLevel.random, (double)-0.25F, (double)0.25F) - d0;
		double d3 = (double)pPos.getZ() + (double)0.5F + Mth.nextDouble(pLevel.random, (double)-0.25F, (double)0.25F);
		popResource(pLevel, (Supplier)(() -> new ItemEntity(pLevel, d1, d2, d3, pStack)), pStack);
	}

	private static void popResource(Level pLevel, Supplier<ItemEntity> pItemEntitySupplier, ItemStack pStack) {
		if (!pLevel.isClientSide && !pStack.isEmpty() && pLevel.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS) && !pLevel.restoringBlockSnapshots) {
			ItemEntity itementity = (ItemEntity)pItemEntitySupplier.get();
			itementity.setNoPickUpDelay();
			itementity.age /= 2;
			pLevel.addFreshEntity(itementity);
		}
	}

	public boolean mineBlock(@NotNull ItemStack stack, @NotNull Level worldIn, @NotNull BlockState state, @NotNull BlockPos pos, @NotNull LivingEntity entityLiving) {
		if (entityLiving instanceof Player) {
			HitResult rayTraceResult = RayTraceUtils.rayTraceSimple(worldIn, entityLiving, 16, 0);
			if (rayTraceResult.getType() == HitResult.Type.BLOCK) {
				BlockEntity be = NCLevels.getExistingBlockEntity(worldIn, pos);
				if(be != null) {
					return super.mineBlock(stack, worldIn, state, pos, entityLiving);
				}
				BlockHitResult blockResult = (BlockHitResult) rayTraceResult;
				List<ItemStack> totalDrops = new ArrayList<>();
				if(getMode(stack) == Mode.VEIN_MINER) {
					mineVein(pos, worldIn, entityLiving, blockResult, stack, totalDrops);
				} else {
					mineArea(pos, worldIn, entityLiving, blockResult, stack, totalDrops);
				}
				totalDrops.forEach(itemStack -> {
					dropResource(worldIn, entityLiving.blockPosition().relative(entityLiving.getDirection(), 2), itemStack);
				});
			}
		}
		return super.mineBlock(stack, worldIn, state, pos, entityLiving);
	}

	public int veinMinedBlocksCounter = 0;
	private List<ItemStack> harvestBlock(BlockPos pos, @NotNull Level worldIn, LivingEntity entityLiving, ItemStack tool, boolean veinMode, List<ItemStack> totalDrops) {
		BlockState tempState = worldIn.getBlockState(pos);
		Block block = tempState.getBlock();
		if(!enoughEnergy(tool)) return totalDrops;
		// 1.21.1: onBlockBreakEvent removed, use fireBlockBreak instead
		var breakEvent = CommonHooks.fireBlockBreak(worldIn, ((ServerPlayer) entityLiving).gameMode.getGameModeForPlayer(), (ServerPlayer) entityLiving, pos, tempState);
		if (!breakEvent.isCanceled() && block.onDestroyedByPlayer(tempState, worldIn, pos, (ServerPlayer) entityLiving, true, tempState.getFluidState())) {
			block.destroy(worldIn, pos, tempState);
			//block.playerDestroy(worldIn, (Player) entityLiving, pos, tempState, NCLevels.getExistingBlockEntity(worldIn, pos), tool);
			Block.getDrops(tempState, (ServerLevel) worldIn, pos, NCLevels.getExistingBlockEntity(worldIn, pos), entityLiving, tool).forEach(itemStack -> {
				boolean combined = false;
				for (ItemStack drop : totalDrops) {
					if (NCItemStacks.canStack(drop, itemStack)) {
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
			((ServerLevel) worldIn).sendParticles(NcParticleTypes.RADIATION.get(), pos.getX() + (random.nextFloat() - 0.5), pos.getY() + (random.nextFloat() - 0.5),
					pos.getZ() + (random.nextFloat() - 0.5), 3, 0, 0, 0, 0);
			consumeEnergy(tool);
			if(veinMode && veinMinedBlocksCounter < 20) {
				veinMinedBlocksCounter++;
				for (Direction facing : Direction.values()) {
					BlockPos newPos = pos.relative(facing);
					if (worldIn.getBlockState(newPos).equals(tempState)) {
						harvestBlock(newPos, worldIn, entityLiving, tool, true, totalDrops);
					}
				}
			}
			int xp = tempState.getExpDrop(worldIn, pos, NCLevels.getExistingBlockEntity(worldIn, pos), entityLiving, tool);
			block.popExperience((ServerLevel) worldIn, pos, xp);
			consumeEnergy(tool);
		}
		return totalDrops;
	}

	private void mineVein(BlockPos pos, Level worldIn, LivingEntity entityLiving, BlockHitResult blockResult, ItemStack stack, List<ItemStack> totalDrops) {
		BlockState initialBlockState = worldIn.getBlockState(pos);
		if (!initialBlockState.is(BlockTags.MINEABLE_WITH_PICKAXE) && !initialBlockState.is(BlockTags.MINEABLE_WITH_SHOVEL)) return;
		if (initialBlockState.getDestroySpeed(worldIn, pos) < 0) return;
		//mine initialblock always
		harvestBlock(pos, worldIn, entityLiving, stack, false, totalDrops);

		consumeEnergy(stack);
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

		int energyPerBlock = energyPerBlock(itemStack);
		if(getMode(itemStack).radius == 0) return getEnergy(itemStack).getEnergyStored() > energyPerBlock;
		int fe = energyPerBlock*(getMode(itemStack).radius*2+1)*(getMode(itemStack).radius*2+1);
		if(getMode(itemStack).depth) fe *= getMode(itemStack).radius*2+1;
		return getEnergy(itemStack).getEnergyStored() > fe;
	}

	@Override
	public float getDestroySpeed(@NotNull ItemStack stack, @NotNull BlockState state) {
		int efficiency = getEnchantmentLevel(stack, Enchantments.EFFICIENCY);
		if(enoughEnergy(stack)) return getTier().getSpeed() + efficiency*0.5F;
		return 0.1F;
	}

	public boolean chargeFromEnergyBlock(BlockEntity be, ItemStack tool)
	{
		if(be == null) return false;
		if(be.getLevel() == null) return false;
		if(getEnergy(tool).getEnergyStored() == getEnergy(tool).getMaxEnergyStored()) return false;
		for(Direction side: Direction.values()) {
			IEnergyStorage storage = be.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK, be.getBlockPos(), side);
			if (storage == null) continue;
			if (storage.canExtract()) {
				int energy = storage.extractEnergy(getEnergy(tool).receiveEnergy(storage.extractEnergy(getEnergy(tool).getMaxEnergyStored() - getEnergy(tool).getEnergyStored(), true), false), false);
				getEnergy(tool).receiveEnergy(energy, false);
				return true;
			}
		}

		IEnergyStorage storage = be.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK, be.getBlockPos(), null);
		if (storage == null) return false;
		if (storage.canExtract()) {
			int energy = storage.extractEnergy(getEnergy(tool).receiveEnergy(storage.extractEnergy(getEnergy(tool).getMaxEnergyStored() - getEnergy(tool).getEnergyStored(), true), false), false);
			getEnergy(tool).receiveEnergy(energy, false);
			return true;
		}
		return false;
	}

	@Override
	public @NotNull InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
		if(context.getLevel().isClientSide) return super.onItemUseFirst(stack, context);
		BlockEntity be = context.getLevel().getBlockEntity(context.getClickedPos());
		if(chargeFromEnergyBlock(be, context.getItemInHand())) {
			Objects.requireNonNull(context.getPlayer()).playSound(ITEM_CHARGED.get(), 0.5f, 1f);
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
		if(pLevel.isClientSide) return super.use(pLevel, pPlayer, pUsedHand);
		if(pPlayer.isSteppingCarefully()) {
			ItemStack tool = pPlayer.getItemInHand(pUsedHand);
			Mode miningMode = Mode.values()[(getMode(tool).ordinal()+1)%Mode.values().length];
			NCItemStacks.putInt(tool, "mode", miningMode.ordinal());
			pPlayer.sendSystemMessage(__("tooltip.nc.qnp_mode", miningMode.getName()).withStyle(ChatFormatting.GREEN));
			return InteractionResultHolder.success(pPlayer.getItemInHand(pUsedHand));
		}
		return super.use(pLevel, pPlayer, pUsedHand);
	}


	public CustomEnergyStorage getEnergy(ItemStack stack)
	{
		IEnergyStorage storage = stack.getCapability(Capabilities.EnergyStorage.ITEM);
		return (CustomEnergyStorage) storage;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext pContext, List<Component> list, TooltipFlag flag)
	{
		list.add(__("tooltip.nc.qnp_mode", __("tooltip.mode." + getMode(stack).getName())).withStyle(ChatFormatting.BLUE));
		list.add(__("tooltip.nc.shift_rbm_to_change").withStyle(ChatFormatting.GRAY));
		if(isGtLoaded() && isGTEUCapEnabled()) {
			list.add(__("tooltip.nc.eu_energy_stored", formatEUEnergy(getEnergy(stack).getEnergyStored()), formatEUEnergy(getEnergyMaxStorage())).withStyle(ChatFormatting.GOLD));
		}
		if(!isGtLoaded() || !isOnlyGTCEUCapEnabled()) {
			list.add(__("tooltip.nc.energy_stored", formatEnergy(getEnergy(stack).getEnergyStored()), formatEnergy(getEnergyMaxStorage())).withStyle(ChatFormatting.BLUE));
		}
	}

	public String formatEnergy(int energy)
	{
		return TextUtils.numberFormat(energy/1000)+" KFE";
	}

	public enum Mode {
		ONE_BLOCK("1", 0, false),
		THREE_BY_THREE("3x3", 1, false),
		THREE_BY_THREE_BY_THREE("3x3x3", 1, true),
		FIVE_BY_FIVE("5x5", 2, false),
		FIVE_BY_FIVE_BY_FIVE("5x5x5", 2, true),
		SEVEN_BY_SEVEN("7x7", 3, false),
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
