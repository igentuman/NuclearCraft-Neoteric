package igentuman.nc.block_entity.accelerator;

import igentuman.nc.api.particle.IParticleHandler;
import igentuman.nc.block.accelerator.BeamPortMode;
import igentuman.nc.container.RingAcceleratorContainer;
import igentuman.nc.multiblock.accelerator.RingAcceleratorCache;
import igentuman.nc.multiblock.accelerator.RingAcceleratorLogic;
import igentuman.nc.multiblock.StructureRole;
import igentuman.nc.particle.ParticlePortView;
import igentuman.nc.util.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RingAcceleratorControllerBE extends AbstractAcceleratorControllerBE {

    public static final int TANK_COOLANT_IN = 0;
    public static final int TANK_COOLANT_OUT = 1;

    @NBTField(syncToClient = true)
    public long maximumEnergyKeV;
    @NBTField(syncToClient = true)
    public boolean inputEnergyTooLow;
    @NBTField(syncToClient = true)
    public boolean inputEnergyTooHigh;
    @NBTField(syncToClient = true)
    public boolean incompatibleParticle;

    public RingAcceleratorControllerBE(BlockPos pos, BlockState state, String name) {
        super(pos, state, name);
    }

    @Override
    public List<BlockPos> rolePositions(StructureRole role) {
        RingAcceleratorCache cache = cache();
        if (cache == null || !formed) return List.of();
        return switch (role) {
            case BEAM_INPUT -> cache.beamInputs;
            case BEAM_OUTPUT -> cache.beamOutputs;
            case SERVICE_PORT -> cache.servicePorts;
            default -> List.of();
        };
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new RingAcceleratorContainer(containerId, playerInventory, this, containerData);
    }

    @Nullable
    @Override
    public IParticleHandler getParticleHandler(BlockPos portPos, BeamPortMode mode, int channel) {
        RingAcceleratorLogic logic = logic();
        if (logic == null || !formed || channel < 0) return null;
        StructureRole role = mode == BeamPortMode.INPUT ? StructureRole.BEAM_INPUT
                : mode == BeamPortMode.OUTPUT ? StructureRole.BEAM_OUTPUT : null;
        if (role == null) return null;
        List<BlockPos> positions = rolePositions(role);
        if (channel >= positions.size() || !positions.get(channel).equals(portPos)) return null;
        if (mode == BeamPortMode.OUTPUT && channel != 0) return null;
        return mode == BeamPortMode.INPUT
                ? new ParticlePortView(logic.input(), 0, ParticlePortView.Access.INPUT)
                : new ParticlePortView(logic.pendingOutput(), 0, ParticlePortView.Access.OUTPUT);
    }

    public void updateDiagnostics(long maximum, boolean tooLow, boolean tooHigh, boolean incompatible) {
        if (maximumEnergyKeV == maximum && inputEnergyTooLow == tooLow && inputEnergyTooHigh == tooHigh
                && incompatibleParticle == incompatible) return;
        maximumEnergyKeV = maximum;
        inputEnergyTooLow = tooLow;
        inputEnergyTooHigh = tooHigh;
        incompatibleParticle = incompatible;
        markDirty();
    }

    @Nullable
    private RingAcceleratorCache cache() {
        var instance = instance();
        return instance != null && instance.cache instanceof RingAcceleratorCache cache ? cache : null;
    }

    @Nullable
    private RingAcceleratorLogic logic() {
        var instance = instance();
        return instance != null && instance.logic instanceof RingAcceleratorLogic logic ? logic : null;
    }
}
