package igentuman.nc.item;

import igentuman.nc.entity.Q36EnergyFlash;
import igentuman.nc.entity.Q36PulseProjectile;
import igentuman.nc.entity.anomaly.AnomalyEntity;
import igentuman.nc.setup.NCSounds;
import igentuman.nc.network.PacketQ36BeamFx;
import igentuman.nc.util.NCDamageSources;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.setup.entries.Parts.Q36_ENERGY_FLASH;
import static igentuman.nc.util.TextUtils.__;

public class Q36Item extends Item {

    public static final String TAG_MODE = "q36_mode";
    public static final String TAG_CHARGE = "q36_charge";
    public static final String TAG_COOLDOWN_END = "q36_cooldown_end";
    public static final String TAG_FX_END = "q36_fx_end";

    public static final int MAX_CHARGE = 100_000;

    public enum FireMode {
        PULSE, BEAM;

        public String key() {
            return name().toLowerCase();
        }

        public int cooldownTicks() {
            return (this == PULSE ? igentuman.nc.config.Q36.PULSE_COOLDOWN_TICKS : igentuman.nc.config.Q36.BEAM_COOLDOWN_TICKS).get();
        }

        public int qcPerShot() {
            return (this == PULSE ? igentuman.nc.config.Q36.PULSE_QC_COST : igentuman.nc.config.Q36.BEAM_QC_COST).get();
        }

        public float damage() {
            return (this == PULSE ? igentuman.nc.config.Q36.PULSE_DAMAGE : igentuman.nc.config.Q36.BEAM_DAMAGE).get().floatValue();
        }

        public int fxTicks() {
            return (this == PULSE ? igentuman.nc.config.Q36.PULSE_FX_TICKS : igentuman.nc.config.Q36.BEAM_FX_TICKS).get();
        }
    }

    public Q36Item(Properties properties) {
        super(properties);
    }

    private static void writeCustomData(ItemStack stack, java.util.function.Consumer<CompoundTag> mutator) {
        CompoundTag tag = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        mutator.accept(tag);
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private static CompoundTag readCustomData(ItemStack stack) {
        CustomData data = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        return data == null ? new CompoundTag() : data.copyTag();
    }

    public static FireMode getMode(ItemStack stack) {
        int idx = readCustomData(stack).getInt(TAG_MODE);
        FireMode[] values = FireMode.values();
        if (idx < 0 || idx >= values.length) idx = 0;
        return values[idx];
    }

    public static void cycleMode(ItemStack stack) {
        FireMode next = FireMode.values()[(getMode(stack).ordinal() + 1) % FireMode.values().length];
        writeCustomData(stack, tag -> tag.putInt(TAG_MODE, next.ordinal()));
    }

    public static int getCharge(ItemStack stack) {
        CompoundTag tag = readCustomData(stack);
        if (!tag.contains(TAG_CHARGE)) return MAX_CHARGE;
        return tag.getInt(TAG_CHARGE);
    }

    public static void setCharge(ItemStack stack, int qc) {
        writeCustomData(stack, tag -> tag.putInt(TAG_CHARGE, Mth.clamp(qc, 0, MAX_CHARGE)));
    }

    public static boolean tryConsume(ItemStack stack, int qc, Player player) {
        if (player.getAbilities().instabuild) return true;
        int current = getCharge(stack);
        if (current < qc) return false;
        setCharge(stack, current - qc);
        return true;
    }

    public static boolean isReady(ItemStack stack, Level level) {
        long end = readCustomData(stack).getLong(TAG_COOLDOWN_END);
        return level.getGameTime() >= end;
    }

    public static void setCooldown(ItemStack stack, Level level, int ticks) {
        writeCustomData(stack, tag -> tag.putLong(TAG_COOLDOWN_END, level.getGameTime() + ticks));
    }

    public static void setFxFlash(ItemStack stack, Level level, int ticks) {
        writeCustomData(stack, tag -> tag.putLong(TAG_FX_END, level.getGameTime() + ticks));
    }

    public static boolean isFxActive(ItemStack stack, Level level) {
        return level.getGameTime() < readCustomData(stack).getLong(TAG_FX_END);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(stack);
        }
        if (!level.isClientSide) {
            cycleMode(stack);
            FireMode mode = getMode(stack);
            player.displayClientMessage(__("tooltip.nc.q36_mode", __("tooltip.nc.q36_mode." + mode.key())).withStyle(ChatFormatting.AQUA), true);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.LEVER_CLICK, SoundSource.PLAYERS, 0.5F, 1.6F);
        }
        return InteractionResultHolder.success(stack);
    }

    public static void serverFire(ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof Q36Item)) return;
        Level level = player.level();
        if (!isReady(stack, level)) return;
        FireMode mode = getMode(stack);
        if (!tryConsume(stack, mode.qcPerShot(), player)) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.6F, 1.2F);
            return;
        }
        setCooldown(stack, level, mode.cooldownTicks());
        setFxFlash(stack, level, mode.fxTicks() - 2);
        switch (mode) {
            case PULSE -> firePulse(player, level);
            case BEAM -> fireBeam(player, (ServerLevel) level, mode);
        }
    }

    private static void firePulse(ServerPlayer player, Level level) {
        Q36PulseProjectile proj = new Q36PulseProjectile(level, player);
        proj.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 3.0F, 0.0F);
        level.addFreshEntity(proj);
        if (level instanceof ServerLevel sl) {
            Vec3 look = player.getViewVector(1.0F);
            Vec3 right = new Vec3(look.z, 0.0D, -look.x).normalize();
            double side = (player.getMainArm() == HumanoidArm.RIGHT ? -1.0D : 1.0D) * 0.3D;
            Vec3 muzzle = player.getEyePosition(1.0F).add(0, -0.3, 0).add(right.scale(side)).add(look.scale(1.05D));
            sl.sendParticles(net.minecraft.core.particles.ParticleTypes.FLASH, muzzle.x, muzzle.y, muzzle.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            sl.sendParticles(net.minecraft.core.particles.ParticleTypes.DRAGON_BREATH, muzzle.x, muzzle.y, muzzle.z, 6, 0.08D, 0.08D, 0.08D, 0.01D);
            sl.sendParticles(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK, muzzle.x, muzzle.y, muzzle.z, 8, 0.05D, 0.05D, 0.05D, 0.15D);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                NCSounds.Q36_PULSE_SHOT.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private static void fireBeam(ServerPlayer player, ServerLevel level, FireMode mode) {
        double range = igentuman.nc.config.Q36.BEAM_RANGE.get();
        Vec3 look = player.getViewVector(1.0F);
        Vec3 right = new Vec3(look.z, 0.0D, -look.x).normalize();
        double side = (player.getMainArm() == HumanoidArm.RIGHT ? -1.0D : 1.0D) * 0.3D;
        Vec3 start = player.getEyePosition(1.0F).add(0, -0.3, 0).add(right.scale(side)).add(look.scale(1.05D));
        Vec3 maxEnd = start.add(look.scale(range));

        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 rayEnd = eye.add(look.scale(range));
        HitResult blockHit = level.clip(new ClipContext(eye, rayEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 blockEnd = blockHit.getType() == HitResult.Type.BLOCK ? blockHit.getLocation() : maxEnd;

        AABB box = player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0D);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(level, player, start, blockEnd, box,
                e -> !e.isSpectator() && e.isPickable() && e != player);

        Vec3 end = blockEnd;
        if (entityHit != null) {
            end = entityHit.getLocation();
            Entity target = entityHit.getEntity();
            if (target instanceof AnomalyEntity anomaly) {
                anomaly.onQ36Hit(player);
            } else {
                target.hurt(NCDamageSources.of(level, NCDamageSources.Q36), mode.damage());
                if (target instanceof LivingEntity living) {
                    Vec3 push = look.scale(0.9D);
                    living.setDeltaMovement(living.getDeltaMovement().add(push.x, 0.2D, push.z));
                }
            }
        }

        if (entityHit == null) {
            int breakRadius = igentuman.nc.config.Q36.BEAM_BLOCK_BREAK_RADIUS.get();
            if (breakRadius > 0) breakBlocksAt(level, player, end, breakRadius);
            damageEntitiesAt(level, end, 3, mode, player, null);
        } else {
            damageEntitiesAt(level, end, 3, mode, player, entityHit.getEntity());
        }

        spawnBeamParticles(level, start, end);
        spawnImpactParticles(level, end, 3.0D);
        spawnMuzzleFlash(level, start);

        Q36EnergyFlash flash = new Q36EnergyFlash(Q36_ENERGY_FLASH.get(), level);
        flash.setPos(end.x, end.y, end.z);
        level.addFreshEntity(flash);

        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new PacketQ36BeamFx(start, end));
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                NCSounds.Q36_BEAM_SHOT.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private static void spawnBeamParticles(ServerLevel level, Vec3 start, Vec3 end) {
        Vec3 diff = end.subtract(start);
        double len = diff.length();
        if (len < 1.0E-4D) return;
        Vec3 dir = diff.scale(1.0D / len);
        double step = 0.5D;
        int steps = (int) Math.ceil(len / step);
        for (int i = 0; i <= steps; i++) {
            double t = Math.min(i * step, len);
            Vec3 p = start.add(dir.scale(t));
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.DRAGON_BREATH, p.x, p.y, p.z, 1, 0.05D, 0.05D, 0.05D, 0.0D);
        }
    }

    private static void spawnImpactParticles(ServerLevel level, Vec3 center, double radius) {
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.DRAGON_BREATH,
                center.x, center.y, center.z, 60, radius * 0.5D, radius * 0.5D, radius * 0.5D, 0.1D);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.FLASH, center.x, center.y, center.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
    }

    private static void spawnMuzzleFlash(ServerLevel level, Vec3 pos) {
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.FLASH, pos.x, pos.y, pos.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.DRAGON_BREATH, pos.x, pos.y, pos.z, 8, 0.1D, 0.1D, 0.1D, 0.02D);
    }

    private static void damageEntitiesAt(ServerLevel level, Vec3 center, double radius, FireMode mode, ServerPlayer shooter, Entity skip) {
        AABB box = new AABB(center.x - radius, center.y - radius, center.z - radius,
                center.x + radius, center.y + radius, center.z + radius);
        double r2 = radius * radius;
        float modeDamage = mode.damage();
        for (Entity e : level.getEntities(shooter, box, en -> en != shooter && en != skip && en.isAlive() && !en.isSpectator())) {
            double dist = e.position().distanceTo(center);
            if (dist * dist > r2) continue;
            float dmg = (float) (modeDamage / (4.0D * Math.max(1.0D, dist)));
            e.hurt(NCDamageSources.of(level, NCDamageSources.Q36), dmg);
        }
    }

    private static void breakBlocksAt(ServerLevel level, ServerPlayer shooter, Vec3 center, int radius) {
        int ceilR = radius + 1;
        double rCore = radius - 0.5D;
        double rOuter = radius + 0.4D;
        for (int dx = -ceilR; dx <= ceilR; dx++) {
            for (int dy = -ceilR; dy <= ceilR; dy++) {
                for (int dz = -ceilR; dz <= ceilR; dz++) {
                    double bx = Math.floor(center.x) + dx + 0.5D;
                    double by = Math.floor(center.y) + dy + 0.5D;
                    double bz = Math.floor(center.z) + dz + 0.5D;
                    double dxC = bx - center.x, dyC = by - center.y, dzC = bz - center.z;
                    double d = Math.sqrt(dxC * dxC + dyC * dyC + dzC * dzC);
                    if (d > rOuter) continue;
                    if (d > rCore && level.random.nextFloat() < (d - rCore) / (rOuter - rCore)) continue;
                    BlockPos pos = new BlockPos((int) Math.floor(bx), (int) Math.floor(by), (int) Math.floor(bz));
                    BlockState state = level.getBlockState(pos);
                    if (state.isAir()) continue;
                    if (state.getDestroySpeed(level, pos) < 0) continue;
                    BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(level, pos, state, shooter);
                    if (NeoForge.EVENT_BUS.post(breakEvent).isCanceled()) continue;
                    level.destroyBlock(pos, true);
                }
            }
        }
    }

    @Override
    public boolean canAttackBlock(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player) {
        return false;
    }

    @Override
    public @NotNull net.minecraft.world.item.UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return net.minecraft.world.item.UseAnim.NONE;
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {
        float ratio = (float) getCharge(stack) / (float) MAX_CHARGE;
        return Mth.clamp((int) (ratio * 13.0F), 0, 13);
    }

    @Override
    public int getBarColor(@NotNull ItemStack stack) {
        float ratio = (float) getCharge(stack) / (float) MAX_CHARGE;
        return Mth.hsvToRgb(Math.max(0.0F, ratio) / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public boolean isRepairable(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public boolean isEnchantable(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        FireMode mode = getMode(stack);
        tooltip.add(__("tooltip.nc.q36_mode", __("tooltip.nc.q36_mode." + mode.key())).withStyle(ChatFormatting.AQUA));
        tooltip.add(__("tooltip.nc.q36_charge", getCharge(stack), MAX_CHARGE).withStyle(ChatFormatting.GREEN));
        Level level = context.level();
        if (level != null) {
            long remaining = readCustomData(stack).getLong(TAG_COOLDOWN_END) - level.getGameTime();
            if (remaining > 0) {
                tooltip.add(__("tooltip.nc.q36_cooldown", remaining).withStyle(ChatFormatting.GRAY));
            }
        }
        tooltip.add(__("tooltip.nc.q36_hint").withStyle(ChatFormatting.DARK_GRAY));
    }
}
