package igentuman.nc.block.entity.fission;

import igentuman.nc.NuclearCraft;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Direction;
import net.minecraft.block.BlockState;

import static igentuman.nc.multiblock.fission.FissionReactorMultiblock.isModerator;

public class FissionIrradiationChamberBE extends FissionBE implements ITickableTileEntity {
    public static String NAME = "fission_reactor_irradiation_chamber";
    public int irradiationConnections = 0;
    public FissionIrradiationChamberBE() {
        this(BlockPos.ZERO, null);
    }
    public FissionIrradiationChamberBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, NAME);
    }
    @Override
    public void tickServer() {
        if(NuclearCraft.instance.isNcBeStopped) return;
        super.tickServer();
        if(multiblock() != null) {
            if (irradiationConnections == 0 || refreshCacheFlag) {
                countIrradiationConnections();
                refreshCacheFlag = false;
            }
        }
    }

    public void countIrradiationConnections() {
        irradiationConnections = 0;
        for (Direction dir : Direction.values()) {
            if(isModerator(getBlockPos().relative(dir), getLevel())) {
                TileEntity be = getLevel().getBlockEntity(getBlockPos().relative(dir, 2));
                if (be instanceof FissionFuelCellBE) {
                    irradiationConnections++;
                }
            }
        }
    }

    public boolean isValid(boolean forceCheck)
    {
        return irradiationConnections > 0;
    }

}
