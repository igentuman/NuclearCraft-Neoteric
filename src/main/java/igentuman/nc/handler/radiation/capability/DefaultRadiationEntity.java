package igentuman.nc.handler.radiation.capability;

import igentuman.nc.NuclearCraft;
import igentuman.nc.handler.radiation.RadiationManager;
import igentuman.nc.capability.BasicCapabilityResolver;
import igentuman.nc.capability.Capabilities;
import igentuman.nc.capability.CapabilityCache;
import igentuman.nc.setup.registration.NcDamageSource;
import igentuman.nc.util.NBTConstants;
import igentuman.nc.util.NcUtils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import org.jetbrains.annotations.NotNull;

public class DefaultRadiationEntity implements IRadiationEntity {

    private double radiation = RadiationManager.BASELINE;

    @Override
    public double getRadiation() {
        return radiation;
    }

    @Override
    public void radiate(double magnitude) {
        radiation += magnitude;
    }

    @Override
    public void update(@NotNull LivingEntity entity) {
        if (entity instanceof Player player && !NcUtils.isPlayingMode(player)) {
            return;
        }

        RandomSource rand = entity.level.getRandom();
        double minSeverity = 0.1d;
        double severityScale = RadiationManager.RadiationScale.getScaledDoseSeverity(radiation);
        double chance = minSeverity + rand.nextDouble() * (1 - minSeverity);

        if (severityScale > chance) {
            //Calculate effect strength based on radiation severity
            float strength = Math.max(1, (float) Math.log1p(radiation));
            //Hurt randomly
            if (rand.nextBoolean()) {
                if (entity instanceof ServerPlayer player) {
                    MinecraftServer server = entity.getServer();
                    int totemTimesUsed = -1;
                    if (server != null && server.isHardcore()) {//Only allow totems to count on hardcore
                        totemTimesUsed = player.getStats().getValue(Stats.ITEM_USED.get(Items.TOTEM_OF_UNDYING));
                    }
                    if (entity.hurt(NcDamageSource.RADIATION, strength)) {
                        boolean hardcoreTotem = totemTimesUsed != -1 && totemTimesUsed < player.getStats().getValue(Stats.ITEM_USED.get(Items.TOTEM_OF_UNDYING));
                       // NcCriteriaTriggers.DAMAGE.trigger(player, NcDamageSource.RADIATION, hardcoreTotem);
                    }
                } else {
                    entity.hurt(NcDamageSource.RADIATION, strength);
                }
            }
            if (entity instanceof ServerPlayer player && strength > 0) {
                player.getFoodData().addExhaustion(strength);
            }
        }
    }

    @Override
    public void set(double magnitude) {
        radiation = magnitude;
    }

    @Override
    public void decay() {
        radiation = Math.max(RadiationManager.BASELINE, radiation * 0.95d);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag ret = new CompoundTag();
        ret.putDouble(NBTConstants.RADIATION, radiation);
        return ret;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        radiation = nbt.getDouble(NBTConstants.RADIATION);
    }

    public static class Provider implements ICapabilitySerializable<CompoundTag> {

        public static final ResourceLocation NAME = NuclearCraft.rl(NBTConstants.RADIATION);
        private final IRadiationEntity defaultImpl = new DefaultRadiationEntity();
        private final CapabilityCache capabilityCache = new CapabilityCache();

        public Provider() {
            capabilityCache.addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.RADIATION_ENTITY, defaultImpl));
        }

        @NotNull
        @Override
        public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, Direction side) {
            return capabilityCache.getCapability(capability, side);
        }

        public void invalidate() {
            capabilityCache.invalidate(Capabilities.RADIATION_ENTITY, null);
        }

        @Override
        public CompoundTag serializeNBT() {
            return defaultImpl.serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            defaultImpl.deserializeNBT(nbt);
        }
    }
}
