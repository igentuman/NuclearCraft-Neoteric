package igentuman.nc.block.collision_chamber.entity;

import igentuman.nc.block.entity.ParticleChamberPortBE;
import igentuman.nc.multiblock.particle_chamber.CollisionChamberMultiblock;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import static igentuman.nc.multiblock.particle_chamber.ParticleChamberRegistration.TARGET_CHAMBER_BE;

public class CollisionChamberPortBE extends ParticleChamberPortBE<CollisionChamberControllerBE, CollisionChamberMultiblock> {

    public static final String NAME = "collision_chamber_port";

    /**
     * INPUT_A=0 / INPUT_B=1 → fed into one of the two collision streams.
     * OUTPUT_1..4 = 2..5 → output beam ports.
     */
    @NBTField
    public byte portRole = ROLE_OUTPUT_1;

    public static final byte ROLE_INPUT_A = 0;
    public static final byte ROLE_INPUT_B = 1;
    public static final byte ROLE_OUTPUT_1 = 2;
    public static final byte ROLE_OUTPUT_2 = 3;
    public static final byte ROLE_OUTPUT_3 = 4;
    public static final byte ROLE_OUTPUT_4 = 5;

    public CollisionChamberPortBE(BlockPos pPos, BlockState pBlockState) {
        super(TARGET_CHAMBER_BE.get(NAME).get(), pPos, pBlockState);
    }

    @Override
    protected Class<CollisionChamberControllerBE> controllerClass() {
        return CollisionChamberControllerBE.class;
    }

    public boolean isInput() {
        return portRole == ROLE_INPUT_A || portRole == ROLE_INPUT_B;
    }

    public boolean isOutput() {
        return !isInput();
    }

    public byte getPortRole() {
        return portRole;
    }

    public void setPortRole(byte role) {
        this.portRole = role;
        setChanged();
    }
}
