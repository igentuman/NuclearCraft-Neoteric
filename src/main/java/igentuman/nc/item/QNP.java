package igentuman.nc.item;

import igentuman.nc.handler.ItemEnergyHandler;
import igentuman.nc.setup.registration.NcParticleTypes;
import igentuman.nc.util.CapabilityUtils;
import igentuman.nc.util.CustomEnergyStorage;
import igentuman.nc.util.RayTraceUtils;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import org.apache.commons.lang3.tuple.Pair;

import static igentuman.nc.handler.event.client.BlockOverlayHandler.getArea;
import static igentuman.nc.setup.registration.NCSounds.ITEM_CHARGED;

public class QNP extends PickaxeItem
{
	public QNP(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties) {
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

	public boolean canEquip(ItemStack stack, EquipmentSlot armorType, Entity entity)
	{
		return false;
	}

	@Override
	public int getBarColor(ItemStack pStack)
	{
		return Mth.hsvToRgb(Math.max(0.0F, getBarWidth(pStack)/(float)MAX_BAR_WIDTH)/3.0F, 1.0F, 1.0F);
	}

	protected int getEnergyMaxStorage() {
		return 1500000;
	}

	@Override
	public int getBarWidth(ItemStack stack) {
		CustomEnergyStorage energyStorage = getEnergy(stack);
		float chargeRatio = (float) energyStorage.getEnergyStored() / (float) getEnergyMaxStorage();
		return (int) Math.min(13, 13*chargeRatio);
	}

	@Override
	public boolean isDamaged(ItemStack stack) {
		return false;
	}

	@Override
	public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
		return true;
	}
	@Override
	public void setDamage(ItemStack stack, int damage)
	{
	}

	@Override
	public boolean isBarVisible(ItemStack pStack) {
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
		worldIn.getEntitiesOfClass(ExperienceOrb.class, new AABB(area.getLeft(), area.getRight()).inflate(1)).forEach(entityXPOrb -> entityXPOrb.teleportTo(entityLiving.blockPosition().getX(), entityLiving.blockPosition().getY(), entityLiving.blockPosition().getZ()));
		return totalDrops;
	}

	public static Mode getMode(ItemStack stack) {
		CompoundTag tag = stack.getOrCreateTag();
		if(!tag.contains("mode")) {
			tag.putInt("mode", Mode.ONE_BLOCK.ordinal());
			stack.save(tag);
		}
		return Mode.values()[tag.getInt("mode")];
	}

	public boolean mineBlock(@NotNull ItemStack stack, @NotNull Level worldIn, @NotNull BlockState state, @NotNull BlockPos pos, @NotNull LivingEntity entityLiving) {
		if (entityLiving instanceof Player) {
			HitResult rayTraceResult = RayTraceUtils.rayTraceSimple(worldIn, entityLiving, 16, 0);
			if (rayTraceResult.getType() == HitResult.Type.BLOCK) {
				BlockHitResult blockResult = (BlockHitResult) rayTraceResult;
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
	private List<ItemStack> harvestBlock(BlockPos pos, Level worldIn, LivingEntity entityLiving, ItemStack tool, boolean veinMode, List<ItemStack> totalDrops) {
		BlockState tempState = worldIn.getBlockState(pos);
		Block block = tempState.getBlock();
		if(!enoughEnergy(tool)) return totalDrops;
		int xp = ForgeHooks.onBlockBreakEvent(worldIn, ((ServerPlayer) entityLiving).gameMode.getGameModeForPlayer(), (ServerPlayer) entityLiving, pos);
		if (xp >= 0 && block.onDestroyedByPlayer(tempState, worldIn, pos, (Player) entityLiving, true, tempState.getFluidState())) {
			block.destroy(worldIn, pos, tempState);
			Block.getDrops(tempState, (ServerLevel) worldIn, pos, null, (Player) entityLiving, tool).forEach(itemStack -> {
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
			((ServerLevel) worldIn).sendParticles(NcParticleTypes.RADIATION.get(), pos.getX() + (random.nextFloat() - 0.5), pos.getY() + (random.nextFloat() - 0.5),
					pos.getZ() + (random.nextFloat() - 0.5), 3, 0, 0, 0, 0);
			getEnergy(tool).extractEnergy(100, false);
			if(veinMode && veinMinedBlocksCounter < 20) {
				veinMinedBlocksCounter++;
				for (Direction facing : Direction.values()) {
					BlockPos newPos = pos.relative(facing);
					if (worldIn.getBlockState(newPos).equals(tempState)) {
						harvestBlock(newPos, worldIn, entityLiving, tool, true, totalDrops);
					}
				}
			}
			block.popExperience((ServerLevel) worldIn, pos, xp);
			getEnergy(tool).extractEnergy(100, false);
		}
		return totalDrops;
	}

	private void mineVein(BlockPos pos, Level worldIn, LivingEntity entityLiving, BlockHitResult blockResult, ItemStack stack, List<ItemStack> totalDrops) {
		BlockState initialBlockState = worldIn.getBlockState(pos);
		if (!initialBlockState.is(BlockTags.MINEABLE_WITH_PICKAXE) && !initialBlockState.is(BlockTags.MINEABLE_WITH_SHOVEL)) return;
		if (initialBlockState.getDestroySpeed(worldIn, pos) < 0) return;
		//mine initialblock always
		harvestBlock(pos, worldIn, entityLiving, stack, false, totalDrops);

		getEnergy(stack).extractEnergy(100, false);
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
		if(getEnergy(stack).getEnergyStored() > 100) return getTier().getSpeed();
		return 0.1F;
	}

	public boolean chargeFromEnergyBlock(BlockEntity be, ItemStack tool)
	{
		if(be == null) return false;
		if(getEnergy(tool).getEnergyStored() == getEnergy(tool).getMaxEnergyStored()) return false;
		for(Direction side: Direction.values()) {
			IEnergyStorage storage = be.getCapability(ForgeCapabilities.ENERGY, side).orElse(null);
			if (storage == null) return false;
			if (storage.canExtract()) {
				int energy = storage.extractEnergy(getEnergy(tool).receiveEnergy(storage.extractEnergy(getEnergy(tool).getMaxEnergyStored() - getEnergy(tool).getEnergyStored(), true), false), false);
				getEnergy(tool).receiveEnergy(energy, false);
				return true;
			}
		}

		IEnergyStorage storage = be.getCapability(ForgeCapabilities.ENERGY).orElse(null);
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
			tool.getOrCreateTag().putInt("mode", miningMode.ordinal());
			pPlayer.sendSystemMessage(Component.translatable("tooltip.nc.qnp_mode", Component.translatable("tooltip.mode." + miningMode.getName())).withStyle(ChatFormatting.GREEN));
			return InteractionResultHolder.success(pPlayer.getItemInHand(pUsedHand));
		}
		return super.use(pLevel, pPlayer, pUsedHand);
	}


	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
		return new ItemEnergyHandler(stack, getEnergyMaxStorage(), 50000, 50000);
	}

	public CustomEnergyStorage getEnergy(ItemStack stack)
	{
		return (CustomEnergyStorage) CapabilityUtils.getPresentCapability(stack, ForgeCapabilities.ENERGY);
	}

	@Override
	public void appendHoverText(ItemStack stack, @javax.annotation.Nullable Level world, List<Component> list, TooltipFlag flag)
	{
		list.add(Component.translatable("tooltip.nc.qnp_mode", Component.translatable("tooltip.mode." + getMode(stack).getName())).withStyle(ChatFormatting.BLUE));
		list.add(Component.translatable("tooltip.nc.shift_rbm_to_change").withStyle(ChatFormatting.GRAY));
		list.add(Component.translatable("tooltip.nc.energy_stored", formatEnergy(getEnergy(stack).getEnergyStored()), formatEnergy(getEnergyMaxStorage())).withStyle(ChatFormatting.BLUE));

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
