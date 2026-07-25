package igentuman.nc.item;

import igentuman.nc.config.Common;
import igentuman.nc.setup.NCSounds;
import igentuman.nc.setup.Registers;
import igentuman.nc.util.RayTraceUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.util.AreaUtil.getArea;
import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.TextUtils.formatEnergy;

public class QNPItem extends PickaxeItem {

    private static final int MAX_BAR_WIDTH = 13;
    private int veinMinedBlocksCounter = 0;

    public QNPItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    public IEnergyStorage getEnergy(ItemStack stack) {
        return stack.getCapability(Capabilities.EnergyStorage.ITEM);
    }

    public static Mode getMode(@NotNull ItemStack stack) {
        int idx = stack.getOrDefault(Registers.QNP_MODE.get(), Mode.ONE_BLOCK.ordinal());
        if (idx < 0 || idx >= Mode.values().length) idx = Mode.ONE_BLOCK.ordinal();
        return Mode.values()[idx];
    }

    private int baseEnergyPerBlock() {
        return Common.QNP_ENERGY_PER_BLOCK.get();
    }

    public int energyPerBlock(ItemStack stack, Level level) {
        int unbreaking = enchantLevel(level, stack, Enchantments.UNBREAKING);
        if (unbreaking > 0) {
            return (int) (baseEnergyPerBlock() / Math.sqrt(unbreaking + 1));
        }
        return baseEnergyPerBlock();
    }

    private static int enchantLevel(Level level, ItemStack stack, ResourceKey<Enchantment> key) {
        if (level == null) return 0;
        Holder<Enchantment> holder = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
        return EnchantmentHelper.getItemEnchantmentLevel(holder, stack);
    }

    private boolean enoughEnergy(ItemStack stack) {
        IEnergyStorage energy = getEnergy(stack);
        if (energy == null) return false;
        Mode mode = getMode(stack);
        int perBlock = baseEnergyPerBlock();
        if (mode.radius == 0) return energy.getEnergyStored() > perBlock;
        int fe = perBlock * (mode.radius * 2 + 1) * (mode.radius * 2 + 1);
        if (mode.depth) fe *= (mode.radius * 2 + 1);
        return energy.getEnergyStored() > fe;
    }

    private void consumeEnergy(ItemStack stack, Level level) {
        IEnergyStorage energy = getEnergy(stack);
        if (energy == null) return;
        energy.extractEnergy(energyPerBlock(stack, level), false);
    }

    @Override
    public boolean isRepairable(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public boolean isDamaged(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public void setDamage(ItemStack stack, int damage) {
    }

    @Override
    public boolean isCorrectToolForDrops(@NotNull ItemStack stack, @NotNull BlockState state) {
        return true;
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {
        IEnergyStorage energy = getEnergy(stack);
        float ratio = energy == null ? 0 : (float) energy.getEnergyStored() / (float) Common.QNP_ENERGY_STORAGE.get();
        return (int) Math.min(MAX_BAR_WIDTH, MAX_BAR_WIDTH * ratio);
    }

    @Override
    public int getBarColor(@NotNull ItemStack stack) {
        return Mth.hsvToRgb(Math.max(0.0F, getBarWidth(stack) / (float) MAX_BAR_WIDTH) / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public float getDestroySpeed(@NotNull ItemStack stack, @NotNull BlockState state) {
        return enoughEnergy(stack) ? getTier().getSpeed() : 0.1F;
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        return enoughEnergy(stack) && super.canPerformAction(stack, itemAbility);
    }

    @Override
    public boolean mineBlock(@NotNull ItemStack stack, @NotNull Level worldIn, @NotNull BlockState state, @NotNull BlockPos pos, @NotNull LivingEntity entityLiving) {
        if (entityLiving instanceof Player && worldIn instanceof ServerLevel && entityLiving instanceof ServerPlayer) {
            HitResult rayTraceResult = RayTraceUtils.rayTraceSimple(worldIn, entityLiving, 16, 0);
            if (rayTraceResult.getType() == HitResult.Type.BLOCK) {
                BlockEntity be = worldIn.getBlockEntity(pos);
                if (be != null) {
                    return super.mineBlock(stack, worldIn, state, pos, entityLiving);
                }
                BlockHitResult blockResult = (BlockHitResult) rayTraceResult;
                List<ItemStack> totalDrops = new ArrayList<>();
                if (getMode(stack) == Mode.VEIN_MINER) {
                    mineVein(pos, worldIn, entityLiving, state, stack, totalDrops);
                } else {
                    mineArea(pos, worldIn, entityLiving, blockResult, stack, totalDrops);
                }
                BlockPos dropPos = entityLiving.blockPosition().relative(entityLiving.getDirection(), 2);
                totalDrops.forEach(itemStack -> dropResource(worldIn, dropPos, itemStack));
            }
        }
        return super.mineBlock(stack, worldIn, state, pos, entityLiving);
    }

    public List<ItemStack> mineArea(BlockPos pos, Level worldIn, LivingEntity entityLiving, BlockHitResult blockResult, ItemStack stack, List<ItemStack> totalDrops) {
        Direction facing = blockResult.getDirection();
        Mode miningMode = getMode(stack);
        Tuple<BlockPos, BlockPos> area = getArea(pos, facing, miningMode.radius, miningMode.depth);
        BlockPos.betweenClosed(area.getA(), area.getB()).forEach(blockPos -> {
            if (enoughEnergy(stack) && worldIn.getBlockEntity(blockPos) == null && !worldIn.isEmptyBlock(blockPos)) {
                BlockState tempState = worldIn.getBlockState(blockPos);
                if (!tempState.is(BlockTags.MINEABLE_WITH_PICKAXE) && !tempState.is(BlockTags.MINEABLE_WITH_SHOVEL)) return;
                if (tempState.getDestroySpeed(worldIn, blockPos) < 0) return;
                harvestBlock(blockPos.immutable(), worldIn, entityLiving, stack, false, totalDrops);
            }
        });
        worldIn.getEntitiesOfClass(ExperienceOrb.class, AABB.encapsulatingFullBlocks(area.getA(), area.getB()).inflate(1))
                .forEach(orb -> orb.teleportTo(entityLiving.getX(), entityLiving.getY(), entityLiving.getZ()));
        return totalDrops;
    }

    private void mineVein(BlockPos pos, Level worldIn, LivingEntity entityLiving, BlockState referenceState, ItemStack stack, List<ItemStack> totalDrops) {
        if (!referenceState.is(BlockTags.MINEABLE_WITH_PICKAXE) && !referenceState.is(BlockTags.MINEABLE_WITH_SHOVEL)) return;
        if (!referenceState.is(Tags.Blocks.ORES)) return;
        veinMinedBlocksCounter++;
        for (Direction facing : Direction.values()) {
            BlockPos newPos = pos.relative(facing);
            if (worldIn.getBlockState(newPos).equals(referenceState)) {
                harvestBlock(newPos.immutable(), worldIn, entityLiving, stack, true, totalDrops);
            }
        }
        veinMinedBlocksCounter = 0;
    }

    private List<ItemStack> harvestBlock(BlockPos pos, @NotNull Level worldIn, LivingEntity entityLiving, ItemStack tool, boolean veinMode, List<ItemStack> totalDrops) {
        if (!enoughEnergy(tool)) return totalDrops;
        if (!(worldIn instanceof ServerLevel serverLevel) || !(entityLiving instanceof ServerPlayer serverPlayer)) return totalDrops;
        BlockState tempState = worldIn.getBlockState(pos);
        if (tempState.isAir()) return totalDrops;
        BlockEvent.BreakEvent event = CommonHooks.fireBlockBreak(worldIn, serverPlayer.gameMode.getGameModeForPlayer(), serverPlayer, pos, tempState);
        if (event.isCanceled()) return totalDrops;
        BlockEntity be = worldIn.getBlockEntity(pos);
        int xp = tempState.getExpDrop(serverLevel, pos, be, serverPlayer, tool);
        Block.getDrops(tempState, serverLevel, pos, be, entityLiving, tool).forEach(itemStack -> {
            for (ItemStack drop : totalDrops) {
                if (ItemStack.isSameItemSameComponents(drop, itemStack)) {
                    drop.setCount(drop.getCount() + itemStack.getCount());
                    return;
                }
            }
            totalDrops.add(itemStack);
        });
        worldIn.removeBlock(pos, false);
        consumeEnergy(tool, worldIn);
        if (veinMode && veinMinedBlocksCounter < 20) {
            veinMinedBlocksCounter++;
            for (Direction facing : Direction.values()) {
                BlockPos newPos = pos.relative(facing);
                if (worldIn.getBlockState(newPos).equals(tempState)) {
                    harvestBlock(newPos.immutable(), worldIn, entityLiving, tool, true, totalDrops);
                }
            }
        }
        if (xp > 0) {
            ExperienceOrb.award(serverLevel, net.minecraft.world.phys.Vec3.atCenterOf(pos), xp);
        }
        return totalDrops;
    }

    private static void dropResource(Level level, BlockPos pos, ItemStack stack) {
        Block.popResourceFromFace(level, pos, Direction.UP, stack);
    }

    private boolean chargeFromEnergyBlock(Level level, BlockPos pos, ItemStack tool) {
        IEnergyStorage toolEnergy = getEnergy(tool);
        if (toolEnergy == null) return false;
        if (toolEnergy.getEnergyStored() >= toolEnergy.getMaxEnergyStored()) return false;
        for (Direction side : Direction.values()) {
            if (pullEnergy(level, pos, side, toolEnergy)) return true;
        }
        return pullEnergy(level, pos, null, toolEnergy);
    }

    private boolean pullEnergy(Level level, BlockPos pos, Direction side, IEnergyStorage toolEnergy) {
        IEnergyStorage storage = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, side);
        if (storage == null || !storage.canExtract()) return false;
        int room = toolEnergy.getMaxEnergyStored() - toolEnergy.getEnergyStored();
        int simulated = storage.extractEnergy(room, true);
        int accepted = toolEnergy.receiveEnergy(simulated, false);
        storage.extractEnergy(accepted, false);
        return accepted > 0;
    }

    @Override
    public @NotNull InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) return InteractionResult.PASS;
        if (chargeFromEnergyBlock(level, context.getClickedPos(), stack)) {
            Player player = context.getPlayer();
            if (player != null) player.playSound(NCSounds.ITEM_CHARGED.get(), 0.5f, 1f);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        if (level.isClientSide) return super.use(level, player, hand);
        if (player.isSecondaryUseActive()) {
            ItemStack tool = player.getItemInHand(hand);
            Mode next = Mode.values()[(getMode(tool).ordinal() + 1) % Mode.values().length];
            tool.set(Registers.QNP_MODE.get(), next.ordinal());
            player.sendSystemMessage(__("tooltip.nc.qnp_mode", next.getName()).withStyle(ChatFormatting.GREEN));
            return InteractionResultHolder.success(tool);
        }
        return super.use(level, player, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag) {
        list.add(__("tooltip.nc.qnp_mode", __("tooltip.mode." + getMode(stack).getName())).withStyle(ChatFormatting.BLUE));
        list.add(__("tooltip.nc.shift_rbm_to_change").withStyle(ChatFormatting.GRAY));
        IEnergyStorage energy = getEnergy(stack);
        int stored = energy == null ? 0 : energy.getEnergyStored();
        list.add(__("tooltip.nuclearcraft.energy_stored",
                formatEnergy(stored), formatEnergy(Common.QNP_ENERGY_STORAGE.get())).withStyle(ChatFormatting.BLUE));
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

        Mode(String name, int radius, boolean depth) {
            this.name = name;
            this.radius = radius;
            this.depth = depth;
        }

        public String getName() {
            return name;
        }
    }
}
