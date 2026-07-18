package igentuman.nc.item;

import igentuman.nc.setup.registration.CreativeTabs;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static igentuman.nc.compat.mekanism.MekInteractions.handleMultitoolInteractionWithMek;
import static igentuman.nc.util.ModUtil.isMekanismLoaded;
import static igentuman.nc.util.TextUtils.__;

public class MultitoolItem extends Item
{
	private int burnTime = -1;
	private boolean isHidden = false;

	public MultitoolItem()
	{
		this(new Properties());
	}

	public MultitoolItem(Properties props)
	{
		this(props, CreativeTabs.NC_ITEMS_TAB);
	}

	public MultitoolItem(Properties props, CreativeModeTab group)
	{
		super(props.tab(group));
	}

	public MultitoolItem setBurnTime(int burnTime)
	{
		this.burnTime = burnTime;
		return this;
	}

	@Override
	public boolean doesSneakBypassUse(ItemStack stack, net.minecraft.world.level.LevelReader world, BlockPos pos, Player player)
	{
		return true;
	}

	@Override
	public int getBurnTime(ItemStack itemStack, RecipeType<?> type)
	{
		return burnTime;
	}

	public boolean isHidden()
	{
		return isHidden;
	}

	@Override
	public boolean isRepairable(@Nonnull ItemStack stack)
	{
		return false;
	}

	public boolean isIERepairable(@Nonnull ItemStack stack)
	{
		return super.isRepairable(stack);
	}

	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book)
	{
		return false;
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

	private static class BombTask {
		ServerLevel level;
		BlockPos pos;
		int ticksLeft;
		int stage;
		boolean isTnt;

		BombTask(ServerLevel level, BlockPos pos, int ticksLeft, int stage, boolean isTnt) {
			this.level = level;
			this.pos = pos;
			this.ticksLeft = ticksLeft;
			this.stage = stage;
			this.isTnt = isTnt;
		}
	}

	private static final List<BombTask> BOMB_TASKS = new java.util.ArrayList<>();

	public static void scheduleBombTask(ServerLevel level, BlockPos pos, boolean isTnt) {
		synchronized (BOMB_TASKS) {
			BOMB_TASKS.add(new BombTask(level, pos, 0, 0, isTnt));
		}
	}

	public static void tickTasks() {
		List<BombTask> ready = new java.util.ArrayList<>();
		synchronized (BOMB_TASKS) {
			java.util.Iterator<BombTask> iterator = BOMB_TASKS.iterator();
			while (iterator.hasNext()) {
				BombTask task = iterator.next();
				if (task.ticksLeft <= 0) {
					ready.add(task);
					iterator.remove();
				} else {
					task.ticksLeft--;
				}
			}
		}
		for (BombTask task : ready) {
			if (task.stage == 0) {
				net.minecraft.world.level.ChunkPos cp = new net.minecraft.world.level.ChunkPos(task.pos);
				net.minecraftforge.common.world.ForgeChunkManager.forceChunk(task.level, igentuman.nc.NuclearCraft.MODID, task.pos, cp.x, cp.z, true, true);
				synchronized (BOMB_TASKS) {
					BOMB_TASKS.add(new BombTask(task.level, task.pos, 1, 1, task.isTnt));
				}
			} else if (task.stage == 1) {
				if (task.isTnt) {
					if (task.level.getBlockState(task.pos).is(net.minecraft.world.level.block.Blocks.TNT)) {
						net.minecraft.world.level.block.TntBlock.explode(task.level, task.pos);
						task.level.setBlock(task.pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 11);
					}
				} else {
					net.minecraft.world.level.block.entity.BlockEntity be = task.level.getBlockEntity(task.pos);
					if (be instanceof igentuman.nc.block.bomb.entity.Pu239BombBE bomb) {
						bomb.arm();
					}
				}
			}
		}
	}

	@NotNull
	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		Level world = context.getLevel();
		if(player == null) {
			return super.useOn(context);
		}
		BlockPos pos = context.getClickedPos();
		if (world.getBlockState(pos).is(net.minecraft.world.level.block.Blocks.TNT)) {
			if (world.isClientSide()) {
				return InteractionResult.SUCCESS;
			}
			ItemStack stack = context.getItemInHand();
			CompoundTag tag = stack.getOrCreateTag();
			tag.putInt("bomb_x", pos.getX());
			tag.putInt("bomb_y", pos.getY());
			tag.putInt("bomb_z", pos.getZ());
			tag.putBoolean("has_bomb", false);
			tag.putBoolean("has_tnt", true);
			player.sendSystemMessage(__("message.nc.multitool.connected_to_tnt", pos.getX(), pos.getY(), pos.getZ()));
			return InteractionResult.SUCCESS;
		}
		BlockEntity be = world.getExistingBlockEntity(pos);
		if (be instanceof igentuman.nc.block.bomb.entity.Pu239BombBE bomb) {
			if (world.isClientSide()) {
				return InteractionResult.SUCCESS;
			}
			ItemStack stack = context.getItemInHand();
			if (player.getUUID().toString().equals(bomb.placerUuid)) {
				CompoundTag tag = stack.getOrCreateTag();
				tag.putInt("bomb_x", pos.getX());
				tag.putInt("bomb_y", pos.getY());
				tag.putInt("bomb_z", pos.getZ());
				tag.putBoolean("has_bomb", true);
				tag.putBoolean("has_tnt", false);
				player.sendSystemMessage(__("message.nc.multitool.connected_to_bomb", pos.getX(), pos.getY(), pos.getZ()));
			} else {
				player.sendSystemMessage(__("message.nc.multitool.not_your_project"));
			}
			return InteractionResult.SUCCESS;
		}
		if (world.isClientSide()) {
			return super.useOn(context);
		}
		Direction side = context.getClickedFace();
		if(isMekanismLoaded() && handleMultitoolInteractionWithMek(be, player, side)) {
			return InteractionResult.SUCCESS;
		}
		return super.useOn(context);
	}

	@NotNull
	@Override
	public InteractionResultHolder<ItemStack> use(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		CompoundTag tag = stack.getTag();
		if (tag != null && (tag.getBoolean("has_bomb") || tag.getBoolean("has_tnt"))) {
			if (world.isClientSide()) {
				return InteractionResultHolder.success(stack);
			}
			int bombX = tag.getInt("bomb_x");
			int bombY = tag.getInt("bomb_y");
			int bombZ = tag.getInt("bomb_z");
			boolean isTnt = tag.getBoolean("has_tnt");

			long gameTime = world.getGameTime();
			long lastArmedTime = tag.getLong("last_armed_time");
			if (gameTime - lastArmedTime <= 60 && gameTime >= lastArmedTime) {
				if (isTnt) {
					player.sendSystemMessage(__("message.nc.multitool.tnt_detonated", bombX, bombY, bombZ));
				} else {
					player.sendSystemMessage(__("message.nc.multitool.bomb_detonated", bombX, bombY, bombZ));
				}
				if (world instanceof ServerLevel serverLevel) {
					scheduleBombTask(serverLevel, new BlockPos(bombX, bombY, bombZ), isTnt);
				}
				tag.remove("last_armed_time");
			} else {
				player.sendSystemMessage(__("message.nc.multitool.armed_confirm"));
				tag.putLong("last_armed_time", gameTime);
			}
			return InteractionResultHolder.success(stack);
		}
		return super.use(world, player, hand);
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag)
	{
		list.add(__("tooltip.nc.multitool.desc").withStyle(ChatFormatting.YELLOW));
		list.add(__("tooltip.nc.multitool.shift.desc").withStyle(ChatFormatting.YELLOW));
		CompoundTag tag = stack.getTag();
		if (tag != null && tag.getBoolean("has_bomb")) {
			int bombX = tag.getInt("bomb_x");
			int bombY = tag.getInt("bomb_y");
			int bombZ = tag.getInt("bomb_z");
			list.add(__("tooltip.nc.multitool.connected_to_bomb", bombX, bombY, bombZ).withStyle(ChatFormatting.RED));
		}
		if (tag != null && tag.getBoolean("has_tnt")) {
			int bombX = tag.getInt("bomb_x");
			int bombY = tag.getInt("bomb_y");
			int bombZ = tag.getInt("bomb_z");
			list.add(__("tooltip.nc.multitool.connected_to_tnt", bombX, bombY, bombZ).withStyle(ChatFormatting.RED));
		}
	}
}
