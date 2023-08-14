package igentuman.nc.radiation.data;

import igentuman.nc.content.NCRadiationDamageSource;
import igentuman.nc.radiation.ItemRadiation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static igentuman.nc.handler.config.CommonConfig.RADIATION_CONFIG;

public class PlayerRadiation implements IPlayerRadiationCapability {

    private final double decaySpeed = RADIATION_CONFIG.DECAY_SPEED_FOR_PLAYER.get();

    public Level level;
    private int radiation = 0;
    private int timestamp = 0;

    private int contaminationStage = 0;

    private int maxPlayerRadiation = 1000000;

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
        return rad/10;//player is not getting radiation instantly
    }

    public void updateRadiation(Level level, Entity player) {
        this.level = level;
        WorldRadiation worldRadiation = RadiationManager.get(level).getWorldRadiation();
        int chunkRadiation = worldRadiation.getChunkRadiation(player.chunkPosition().x, player.chunkPosition().z);
        if(chunkRadiation > radiation) {
            radiation = (int) (chunkRadiation * 0.05f + radiation);
        }
        if(player instanceof Player) {
            radiation += getInventoryRadiation((Player) player);
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
