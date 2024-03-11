package igentuman.nc.block.entity;

import igentuman.nc.content.Electromagnets;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.BlockState;

import static igentuman.nc.setup.registration.NCBlocks.NC_BE;

public class ElectromagnetBE extends TileEntity {
    protected String name;
    public boolean hasToUpdate = true;
    public ElectromagnetBE(BlockPos blockPos, BlockState blockState) {
        this(NC_BE.get(getName(blockState)).get(), blockPos, blockState);
    }

    public ElectromagnetBE() {
        super(NC_BE.get("basic_electromagnet").get());
    }

    public static String getName(BlockState pBlockState) {
        return pBlockState.getBlock().asItem().toString().replace("_slope", "");
    }
    public ElectromagnetBE(TileEntityType<?> pType) {
        super(pType);
       /// name = getName(pBlockState);
    }

    public ElectromagnetBE(TileEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType);
        name = getName(pBlockState);
    }

    public Electromagnets.MagnetPrefab prefab() {
        return Electromagnets.all().get(name);
    }

    public double getStrength() {
        return prefab().getMagneticField();
    }
    public double getEfficiency() {
        return prefab().getEfficiency();
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
}
