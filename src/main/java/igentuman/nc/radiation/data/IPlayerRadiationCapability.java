package igentuman.nc.radiation.data;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;


public interface IPlayerRadiationCapability extends INBTSerializable<CompoundNBT> {
    int getRadiation();
    void setRadiation(int radiation);

    int getTimestamp();
    void setTimestamp(int timestamp);

    void updateRadiation(World level, LivingEntity player);
}
