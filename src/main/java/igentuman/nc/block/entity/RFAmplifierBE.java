package igentuman.nc.block.entity;

import igentuman.nc.content.RFAmplifier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import static igentuman.nc.setup.registration.NCBlocks.NC_BE;

public class RFAmplifierBE extends BlockEntity {

    protected String name;
    public boolean hasToUpdate = true;

    public RFAmplifierBE(BlockPos blockPos, BlockState blockState) {
        this(NC_BE.get(getName(blockState)).get(), blockPos, blockState);
    }
    public static String getName(BlockState pBlockState) {
        return pBlockState.getBlock().asItem().toString();
    }
    public RFAmplifierBE(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
        name = getName(pBlockState);
    }

    public RFAmplifier.RFAmplifierPrefab prefab() {
        return RFAmplifier.all().get(name);
    }

    public int getAmplification() {
        return prefab().getVoltage();
    }

    public int getPower() {
        return prefab().getPower();
    }

    public int getMaxTemperature() {
        return prefab().getMaxTemp();
    }

    public void tickClient() {
    }

    public void tickServer() {
        if(hasToUpdate) {
            hasToUpdate = false;
            setChanged();
        }
    }

    public double getEfficiency() {
        return prefab().getEfficiency();
    }
}
