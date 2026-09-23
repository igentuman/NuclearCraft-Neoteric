package igentuman.nc.block_entity.accelerator;

import igentuman.nc.api.particle.IParticleHandler;
import igentuman.nc.block.accelerator.BeamPortMode;
import igentuman.nc.item.ParticleSourceItem;
import igentuman.nc.multiblock.accelerator.LinearAcceleratorCache;
import igentuman.nc.multiblock.accelerator.LinearAcceleratorLogic;
import igentuman.nc.multiblock.StructureRole;
import igentuman.nc.particle.ParticlePortView;
import igentuman.nc.particle.ParticleSourceCatalog;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LinearAcceleratorControllerBE extends AbstractAcceleratorControllerBE {

    public static final int TANK_SOURCE = 0;
    public static final int TANK_COOLANT_IN = 1;
    public static final int TANK_COOLANT_OUT = 2;

    private boolean sourceValidatorsInstalled;

    public LinearAcceleratorControllerBE(BlockPos pos, BlockState state, String name) {
        super(pos, state, name);
        installSourceValidators();
    }

    @Override
    public List<BlockPos> rolePositions(StructureRole role) {
        LinearAcceleratorCache cache = cache();
        if (cache == null || !formed) return List.of();
        return switch (role) {
            case BEAM_INPUT -> cache.beamInputs;
            case BEAM_OUTPUT -> cache.beamOutputs;
            case ION_SOURCE -> cache.ionSources;
            case SERVICE_PORT -> cache.servicePorts;
            default -> List.of();
        };
    }

    public boolean ownsIonSourcePort(BlockPos portPos) {
        return rolePositions(StructureRole.ION_SOURCE).contains(portPos);
    }

    @Nullable
    @Override
    public IParticleHandler getParticleHandler(BlockPos portPos, BeamPortMode mode, int channel) {
        LinearAcceleratorLogic logic = logic();
        if (logic == null || !formed || channel < 0) return null;
        StructureRole role = mode == BeamPortMode.INPUT ? StructureRole.BEAM_INPUT
                : mode == BeamPortMode.OUTPUT ? StructureRole.BEAM_OUTPUT : null;
        if (role == null) return null;
        List<BlockPos> positions = rolePositions(role);
        if (channel >= positions.size() || !positions.get(channel).equals(portPos)) return null;
        return mode == BeamPortMode.INPUT
                ? new ParticlePortView(logic.input(), 0, ParticlePortView.Access.INPUT)
                : new ParticlePortView(logic.pendingOutput(), 0, ParticlePortView.Access.OUTPUT);
    }

    public void installSourceValidators() {
        if (sourceValidatorsInstalled) return;
        ParticleSourceCatalog catalog = ParticleSourceCatalog.builtin();
        if (contentHandler.getItemHandler() != null) {
            contentHandler.getItemHandler().setSlotValidator(0, stack -> stack.getItem() instanceof ParticleSourceItem
                    && catalog.itemSources().containsKey(BuiltInRegistries.ITEM.getKey(stack.getItem()))
                    && ParticleSourceItem.data(stack) != null);
        }
        if (contentHandler.getFluidHandler() != null) {
            contentHandler.getFluidHandler().getInternalHandler().setTankValidator(TANK_SOURCE,
                    stack -> catalog.fluidSources().containsKey(BuiltInRegistries.FLUID.getKey(stack.getFluid())));
        }
        sourceValidatorsInstalled = true;
    }

    @Nullable
    private LinearAcceleratorCache cache() {
        var instance = instance();
        return instance != null && instance.cache instanceof LinearAcceleratorCache cache ? cache : null;
    }

    @Nullable
    private LinearAcceleratorLogic logic() {
        var instance = instance();
        return instance != null && instance.logic instanceof LinearAcceleratorLogic logic ? logic : null;
    }
}
