package igentuman.nc.radiation.data;

import igentuman.nc.content.NCRadiationDamageSource;
import igentuman.nc.radiation.ItemRadiation;
import igentuman.nc.radiation.ItemShielding;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.world.World;

import java.util.LinkedList;
import java.util.List;

import static igentuman.nc.handler.config.RadiationConfig.RADIATION_CONFIG;

public class PlayerRadiation implements IPlayerRadiationCapability {

    private final double decaySpeed = RADIATION_CONFIG.DECAY_SPEED_FOR_PLAYER.get();

    public World level;
    private int radiation = 0;
    private int timestamp = 0;

    private int contaminationStage = 0;

    public static int maxPlayerRadiation = 500000000;

    public PlayerRadiation() {
    }

    public static PlayerRadiation deserialize(CompoundNBT radiation) {
        PlayerRadiation playerRadiation = new PlayerRadiation();
        playerRadiation.deserializeNBT(radiation);
        return playerRadiation;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT tag = new CompoundNBT();
        tag.putInt("radiation", radiation);
        tag.putInt("timestamp", timestamp);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        CompoundNBT radiationTag = nbt.getCompound("radiation");
        radiation = radiationTag.getInt("radiation");
        timestamp = radiationTag.getInt("timestamp");
    }

    public void copyFrom(PlayerRadiation source) {
        radiation = source.radiation;
        timestamp = source.timestamp;
    }

    public int getInventoryRadiation(PlayerEntity player) {
        int rad = 0;
        for(ItemStack itemStack: player.inventory.items) {
            rad += (int) (ItemRadiation.byItem(itemStack.getItem())*1000000);
        }
        return rad/5;//player is not getting radiation instantly
    }

    public static int getRadiationShielding(LivingEntity player)
    {
        int shielding = 0;
        for(ItemStack stack: player.getArmorSlots()) {
            if(stack.isEmpty()) continue;
            shielding += ItemShielding.byItem(stack.getItem());
            if(stack.hasTag() && stack.getTag().contains("rad_shielding")) {
                shielding += stack.getTag().getInt("rad_shielding");
            }
        }
/*        if(player.hasEffect(RADIATION_RESISTANCE.get())) {
            int resistance = player.getEffect(RADIATION_RESISTANCE.get()).getAmplifier()+1;
            shielding += resistance*2;
        }*/
        return shielding;
    }

    public void updateRadiation(World level, LivingEntity player) {
        this.level = level;
        WorldRadiation worldRadiation = RadiationManager.get(level).getWorldRadiation();
        int chunkRadiation = worldRadiation.getChunkRadiation(player.xChunk, player.zChunk);
        double shieldingRate = Math.max(0.001, 0.7 - getRadiationShielding(player)/100.0);
        if(chunkRadiation > radiation) {
            radiation = (int) (((chunkRadiation + radiation)/10D * RADIATION_CONFIG.GAIN_SPEED_FOR_PLAYER.get()) * shieldingRate + radiation);
        } else {
            radiation = (int) ((chunkRadiation * (RADIATION_CONFIG.GAIN_SPEED_FOR_PLAYER.get()/1000D)) * shieldingRate + radiation);
        }
        if(player instanceof PlayerEntity) {
            radiation += (int) (getInventoryRadiation((PlayerEntity) player) * shieldingRate);
        }
        radiation -= (int) decaySpeed;
        radiation = Math.min(maxPlayerRadiation, Math.max(0, radiation));
        assert player instanceof PlayerEntity;
        updateContaminationStage((PlayerEntity) player);
    }

    public void updateContaminationStage(PlayerEntity player)
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

    public List<EffectInstance> contaminationEffects = new LinkedList<>();
    private void setContaminationStage(int i, PlayerEntity player) {
        if(player.isCreative()) return;
        contaminationStage = i;
        for(EffectInstance effect: contaminationEffects) {
            player.removeEffect(effect.getEffect());
        }
        contaminationEffects.clear();
        switch (contaminationStage) {
            case 3:
                contaminationEffects.add(new EffectInstance(Effects.WEAKNESS, 900000, 3));
                contaminationEffects.add(new EffectInstance(Effects.CONFUSION, 900000,2));
                contaminationEffects.add(new EffectInstance(Effects.GLOWING, 900000));
                contaminationEffects.add(new EffectInstance(Effects.UNLUCK, 900000));
                contaminationEffects.add(new EffectInstance(Effects.POISON, 900000));
                contaminationEffects.add(new EffectInstance(Effects.BLINDNESS, 900000));
            break;
            case 2:
                contaminationEffects.add(new EffectInstance(Effects.WEAKNESS, 900000, 2));
                contaminationEffects.add(new EffectInstance(Effects.CONFUSION, 900000));
                contaminationEffects.add(new EffectInstance(Effects.GLOWING, 900000));
                contaminationEffects.add(new EffectInstance(Effects.UNLUCK, 900000));
                contaminationEffects.add(new EffectInstance(Effects.POISON, 900000));
            break;
            case 1:
                contaminationEffects.add(new EffectInstance(Effects.WEAKNESS, 90000));
                contaminationEffects.add(new EffectInstance(Effects.UNLUCK, 90000));
            }
        
        for(EffectInstance effect: contaminationEffects) {
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
