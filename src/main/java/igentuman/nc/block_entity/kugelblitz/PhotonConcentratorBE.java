package igentuman.nc.block_entity.kugelblitz;

import igentuman.nc.block_entity.GlobalBlockEntity;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class PhotonConcentratorBE extends GlobalBlockEntity {

    @Nullable
    @NBTField
    public BlockPos controllerPos;

    public PhotonConcentratorBE(BlockPos pos, BlockState state, String name) {
        super(ModEntries.get(name).blockEntity().get(), pos, state, name);
    }

    public void setControllerPos(BlockPos pos) {
        if (pos.equals(controllerPos)) return;
        controllerPos = pos;
        setChanged();
    }

    @Nullable
    public ChamberTerminalBE controller() {
        if (controllerPos == null || level == null) return null;
        BlockEntity be = level.getBlockEntity(controllerPos);
        return be instanceof ChamberTerminalBE c ? c : null;
    }

    public void gotEnergy(Direction facing) {
        if (level == null || level.isClientSide()) return;
        ChamberTerminalBE controller = controller();
        if (controller != null) {
            controller.gotEnergy(facing);
        }
    }
}
