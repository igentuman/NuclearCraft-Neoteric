package igentuman.nc.compat.cc;

import igentuman.nc.block_entity.MultiblockControllerBE;

public abstract class ParticleControllerPeripheral<T extends MultiblockControllerBE> {

    protected final T controller;
    protected final ControllerKind kind;

    protected ParticleControllerPeripheral(T controller, ControllerKind kind) {
        this.controller = controller;
        this.kind = kind;
    }

    public enum ControllerKind {
        LINEAR_ACCELERATOR,
        RING_ACCELERATOR,
        BEAM_DIVERTER,
        TARGET_CHAMBER,
        DECAY_CHAMBER,
        COLLISION_CHAMBER
    }
}
