package igentuman.nc.content.energy;

import igentuman.nc.block.entity.energy.NCEnergy;
import igentuman.nc.handler.config.CommonConfig;
import net.minecraft.world.level.block.entity.BlockEntityType;

import static igentuman.nc.handler.config.CommonConfig.ENERGY_GENERATION;

public class RTGPrefab {
    private boolean registered = true;
    private boolean initialized = false;
    private String name;
    protected int generation = 0;
    protected int radiation = 0;

    public RTGPrefab(String name, int generation, int radiation) {
        this.generation = generation;
        this.radiation = radiation;
    }

    public int getGeneration() {
        return generation;
    }

    public RTGPrefab setGeneration(int generation) {
        this.generation = generation;
        return this;
    }

    public RTGPrefab config()
    {
        if(!initialized) {
            if(!CommonConfig.isLoaded()) {
                return this;
            }
            int id = RTGs.all().keySet().stream().toList().indexOf(name);
            registered = ENERGY_GENERATION.REGISTER_RTG.get().get(id);
            generation = ENERGY_GENERATION.RTG_GENERATION.get().get(id);
            radiation = ENERGY_GENERATION.RTG_RADIATION.get().get(id);
            initialized = true;
        }
        return this;
    }
    public boolean isRegistered() {
        return  registered;
    }

    public BlockEntityType.BlockEntitySupplier<? extends NCEnergy>  getBlockEntity() {
        return blockEntity;
    }

    public RTGPrefab setBlockEntity(BlockEntityType.BlockEntitySupplier<? extends NCEnergy>  blockEntity) {
        this.blockEntity = blockEntity;
        return this;
    }
    private BlockEntityType.BlockEntitySupplier<? extends NCEnergy>  blockEntity;

    public int getRadiation() {
        return radiation;
    }
}
