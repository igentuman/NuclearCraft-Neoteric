package igentuman.nc.multiblock.kugelblitz;

import igentuman.nc.block.entity.fission.FissionControllerBE;
import igentuman.nc.block.entity.kugelblitz.BlackHoleBE;
import igentuman.nc.block.entity.kugelblitz.ChamberTerminalBE;
import igentuman.nc.block.entity.kugelblitz.PhotonConcentratorBE;
import igentuman.nc.multiblock.AbstractNCMultiblock;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.ValidationResult;
import igentuman.nc.util.NCBlockPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.HashMap;
import java.util.List;
import static igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration.CASING_BLOCKS;
import static igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration.KUGELBLITZ_BLOCKS;
import static igentuman.nc.util.TagUtil.getBlocksByTagKey;
import static net.minecraft.world.level.block.Blocks.AIR;

public class KugelblitzMultiblock extends AbstractNCMultiblock {

    private ChamberTerminalBE controllerBe;
    private final HashMap<Direction, Integer> pulseEnergy = new HashMap<>();
    private boolean hasBlackHole = false;
    private boolean collectingEnergy = true;

    public int maxHeight() {
        return 8;
    }
    public int minHeight() {
        return 7;
    }
    public int maxWidth() {
        return 8;
    }
    public int minWidth() {
        return 7;
    }
    public int maxDepth() {
        return 8;
    }
    public int minDepth() {
        return 7;
    }

    private ChamberTerminalBE controllerBE() {
        if (controllerBe == null) {
            controllerBe = (ChamberTerminalBE) controller().controllerBE();
        }
        return controllerBe;
    }

    @Override
    protected Direction getFacing() {
        return controllerBE().getFacing();
    }

    public KugelblitzMultiblock(ChamberTerminalBE be) {
        super(getBlocksByTagKey(CASING_BLOCKS.location().toString()), List.of(KUGELBLITZ_BLOCKS.get("black_hole").get(), AIR), new KugelblitzController(be));
        id = "chamber_"+be.getBlockPos().toShortString();
        MultiblockHandler.addMultiblock(this);
    }

    @Override
    public void validateOuter()
    {
        super.validateOuter();
        validatePhotonConcentrators();
    }

    private void validatePhotonConcentrators() {
        BlockPos center = getCenter();
        BlockPos left = center.offset(-3, 0, 0);
        BlockPos right = center.offset(3, 0, 0);
        BlockPos top = center.offset(0, 0, -3);
        BlockPos bottom = center.offset(0, 0, 3);
        if (!(getLevel().getExistingBlockEntity(left) instanceof PhotonConcentratorBE) ||
                !(getLevel().getExistingBlockEntity(right) instanceof PhotonConcentratorBE) ||
                        !(getLevel().getExistingBlockEntity(top) instanceof PhotonConcentratorBE) ||
                                !(getLevel().getExistingBlockEntity(bottom) instanceof PhotonConcentratorBE)) {
            validationResult = ValidationResult.PHOTON_CONCENTRATOR;
        }
    }

    @Override
    public void tick() {
        super.tick();
        handleBlackHole();
        if(!pulseEnergy.isEmpty() && collectingEnergy) {
            collectingEnergy = false;
            return;
        }
        collectingEnergy = true;
        pulseEnergy.clear();
    }

    private void handleBlackHole() {
        BlockEntity be = getLevel().getExistingBlockEntity(getCenter());
        hasBlackHole = be instanceof BlackHoleBE;
        if(!isFormed && hasBlackHole) {
            getLevel().setBlockAndUpdate(getCenter(), AIR.defaultBlockState());
            hasBlackHole = false;
        }
        if (!hasBlackHole) {
            boolean energyTransfered = true;
            //validate if all pulse energy is transferred
            for (Direction direction : Direction.values()) {
                if(!pulseEnergy.containsKey(direction)) {
                    energyTransfered = false;
                    break;
                }
            }
            if (energyTransfered) {
                getLevel().setBlockAndUpdate(getCenter(), KUGELBLITZ_BLOCKS.get("black_hole").get().defaultBlockState());
            }
        }
    }

    @Override
    public void invalidateStats() {
        pulseEnergy.clear();
    }

    public BlockPos getCenter() {
        BlockPos pos = new BlockPos(getTopRightBlock());
        return switch (getFacing()) {
            case NORTH -> pos.offset(+3, -3, +3);
            case SOUTH -> pos.offset(-3, -3, -3);
            case WEST -> pos.offset(+3, -3, -3);
            case EAST -> pos.offset(-3, -3, +3);
            default -> pos;
        };
    }

    public void addPulseEnergy(int pulseEnergy, Direction facing) {
        this.pulseEnergy.put(facing, pulseEnergy);
    }
}
