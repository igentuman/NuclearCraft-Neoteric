package igentuman.nc.block.entity.fission;

import igentuman.nc.NuclearCraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import static igentuman.nc.multiblock.fission.FissionReactorMultiblock.isModerator;

public class FissionIrradiationChamberBE extends FissionBE {
    public static String NAME = "fission_reactor_irradiation_chamber";
    public int irradiationConnections = 0;
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
                BlockEntity be = getLevel().getBlockEntity(getBlockPos().relative(dir));
                if (be instanceof FissionFuelCellBE) {
                    irradiationConnections++;
                }
            }
        }
    }

}
