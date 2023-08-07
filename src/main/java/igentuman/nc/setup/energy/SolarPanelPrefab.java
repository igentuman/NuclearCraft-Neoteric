package igentuman.nc.setup.energy;

import igentuman.nc.block.entity.energy.NCEnergy;
import igentuman.nc.handler.config.CommonConfig;
import igentuman.nc.content.processors.Processors;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class SolarPanelPrefab {
    private boolean registered = true;
    private boolean initialized = false;
    private String name;
    protected int generation = 0;

    public SolarPanelPrefab(String name, int generation) {
        this.generation = generation;
    }

    public int getGeneration() {
        return generation;
    }

    public SolarPanelPrefab setGeneration(int generation) {
        this.generation = generation;
        return this;
    }

    public SolarPanelPrefab config()
    {
        if(!initialized) {
            if(!CommonConfig.isLoaded()) {
                return this;
            }
            int id = Processors.all().keySet().stream().toList().indexOf(name);
            registered = CommonConfig.EnergyGenerationConfig.REGISTER_SOLAR_PANELS.get().get(id);
            generation = CommonConfig.EnergyGenerationConfig.SOLAR_PANELS_GENERATION.get().get(id);
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

    public SolarPanelPrefab setBlockEntity(BlockEntityType.BlockEntitySupplier<? extends NCEnergy>  blockEntity) {
        this.blockEntity = blockEntity;
        return this;
    }
    private BlockEntityType.BlockEntitySupplier<? extends NCEnergy>  blockEntity;
}
