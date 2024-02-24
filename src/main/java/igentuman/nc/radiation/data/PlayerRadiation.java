package igentuman.nc.radiation.data;

import igentuman.nc.content.NCRadiationDamageSource;
import igentuman.nc.radiation.ItemRadiation;
import igentuman.nc.radiation.ItemShielding;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static igentuman.nc.handler.config.RadiationConfig.RADIATION_CONFIG;
import static igentuman.nc.setup.Registration.RADIATION_RESISTANCE;

public class PlayerRadiation implements IPlayerRadiationCapability {

    private final double decaySpeed = RADIATION_CONFIG.DECAY_SPEED_FOR_PLAYER.get();

    public Level level;
    private int radiation = 0;
    private int timestamp = 0;

    private int contaminationStage = 0;

    public static int maxPlayerRadiation = 500000000;

    public PlayerRadiation() {
    }

    public static PlayerRadiation deserialize(CompoundTag radiation) {
        PlayerRadiation playerRadiation = new PlayerRadiation();
        playerRadiation.deserializeNBT(radiation);
        return playerRadiation;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("radiation", radiation);
        tag.putInt("timestamp", timestamp);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        CompoundTag radiationTag = nbt.getCompound("radiation");
        radiation = radiationTag.getInt("radiation");
        timestamp = radiationTag.getInt("timestamp");
    }

    public void copyFrom(PlayerRadiation source) {
        radiation = source.radiation;
        timestamp = source.timestamp;
    }

    public int getInventoryRadiation(Player player) {
        int rad = 0;
        for(ItemStack itemStack: player.getInventory().items) {
            rad += (int) (ItemRadiation.byItem(itemStack.getItem())*1000000);
        }
        return rad/5;//player is not getting radiation instantly
    }

    public static int getRadiationShielding(LivingEntity player, String...modFilter)
    {
        int shielding = 0;
        for(ItemStack stack: player.getArmorSlots()) {
            if(stack.isEmpty()) continue;
            if(modFilter.length > 0) {
                String stackMod = stack.getItem().getCreatorModId(stack);
                boolean hasMod = false;
                for(String mod: modFilter) {
                    assert stackMod != null;
                    if(stackMod.equals(mod)) {
                        hasMod = true;
                        break;
                    }
                }
                if(!hasMod) continue;
            }
            shielding += ItemShielding.byItem(stack.getItem());
            if(stack.hasTag() && stack.getTag().contains("rad_shielding")) {
                shielding += stack.getTag().getInt("rad_shielding");
            }
        }
        if(player.hasEffect(RADIATION_RESISTANCE.get())) {
            int resistance = player.getEffect(RADIATION_RESISTANCE.get()).getAmplifier()+1;
            shielding += resistance*2;
        }
        return shielding;
    }

    public void updateRadiation(Level level, LivingEntity player) {
        this.level = level;
        WorldRadiation worldRadiation = RadiationManager.get(level).getWorldRadiation();
        int chunkRadiation = worldRadiation.getChunkRadiation(player.chunkPosition().x, player.chunkPosition().z);
        double shieldingRate = Math.max(0.001, 0.7 - getRadiationShielding(player)/100.0);
        if(chunkRadiation > radiation) {
            radiation = (int) (((chunkRadiation + radiation)/10D * RADIATION_CONFIG.GAIN_SPEED_FOR_PLAYER.get()) * shieldingRate + radiation);
        } else {
            radiation = (int) ((chunkRadiation * (RADIATION_CONFIG.GAIN_SPEED_FOR_PLAYER.get()/1000D)) * shieldingRate + radiation);
        }
        if(player instanceof Player) {
            radiation += (int) (getInventoryRadiation((Player) player) * shieldingRate);
        }
        radiation -= (int) decaySpeed;
        radiation = Math.min(maxPlayerRadiation, Math.max(0, radiation));
        assert player instanceof Player;
        updateContaminationStage((Player) player);
    }

    public void updateContaminationStage(Player player)
    {
        if(radiation >= maxPlayerRadiation) {
            radiation = radiation/3;
            player.hurt(NCRadiationDamageSource.RADIATION, 1000000);
            return;
        }
        if(radiation >= maxPlayerRadiation*0.66) {
            setContaminationStage(3, player);
            return;
        }
        if(radiation >= maxPlayerRadiation*0.44) {
            setContaminationStage(2, player);
            return;
        }

        if(radiation >= maxPlayerRadiation*0.22) {
            setContaminationStage(1, player);
            return;
        }
        setContaminationStage(0, player);
    }

    public List<MobEffectInstance> contaminationEffects = new LinkedList<>();
    private void setContaminationStage(int i, Player player) {
        if(player.isCreative()) return;
        contaminationStage = i;
        for(MobEffectInstance effect: contaminationEffects) {
            player.removeEffect(effect.getEffect());
        }
        contaminationEffects.clear();
        switch (contaminationStage) {
            case 3 -> {
                contaminationEffects.add(new MobEffectInstance(MobEffects.WEAKNESS, 900000, 3));
                contaminationEffects.add(new MobEffectInstance(MobEffects.CONFUSION, 900000,2));
                contaminationEffects.add(new MobEffectInstance(MobEffects.GLOWING, 900000));
                contaminationEffects.add(new MobEffectInstance(MobEffects.UNLUCK, 900000));
                contaminationEffects.add(new MobEffectInstance(MobEffects.POISON, 900000));
                contaminationEffects.add(new MobEffectInstance(MobEffects.BLINDNESS, 900000));
            }
            case 2 -> {
                contaminationEffects.add(new MobEffectInstance(MobEffects.WEAKNESS, 900000, 2));
                contaminationEffects.add(new MobEffectInstance(MobEffects.CONFUSION, 900000));
                contaminationEffects.add(new MobEffectInstance(MobEffects.GLOWING, 900000));
                contaminationEffects.add(new MobEffectInstance(MobEffects.UNLUCK, 900000));
                contaminationEffects.add(new MobEffectInstance(MobEffects.POISON, 900000));
            }
            case 1 -> {
                contaminationEffects.add(new MobEffectInstance(MobEffects.WEAKNESS, 90000));
                contaminationEffects.add(new MobEffectInstance(MobEffects.UNLUCK, 90000));
            }
        }
        for(MobEffectInstance effect: contaminationEffects) {
            player.addEffect(effect);
        }
    }

    @Override
    public int getRadiation() {
        return radiation;
    }

    @Override
    public void setRadiation(int radiation) {
        this.radiation = radiation;
    }

    @Override
    public int getTimestamp() {
        return timestamp;
    }

    @Override
    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }
}
