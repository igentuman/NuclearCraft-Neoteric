package igentuman.nc.content.energy;

import igentuman.nc.block.entity.energy.BatteryBE;
import igentuman.nc.block.entity.energy.NCEnergy;
import igentuman.nc.handler.config.CommonConfig;
import net.minecraft.world.level.block.entity.BlockEntityType;

import static igentuman.nc.handler.config.CommonConfig.ENERGY_STORAGE;

public class BatteryBlockPrefab {
    private boolean registered = true;
    private boolean initialized = false;
    private String name;
    protected int storage = 0;

    public BatteryBlockPrefab(String name, int storage) {
        this.storage = storage;
        blockEntity = BatteryBE::new;
    }

    public int getStorage() {
        return storage;
    }

    public BatteryBlockPrefab setStorage(int capacity) {
        this.storage = storage;
        return this;
    }

    public BatteryBlockPrefab config()
    {
        if(!initialized) {
            if(!CommonConfig.isLoaded()) {
                return this;
            }
            int id = BatteryBlocks.all().keySet().stream().toList().indexOf(name);
            registered = ENERGY_STORAGE.REGISTER_ENERGY_BLOCK.get().get(id);
            storage = ENERGY_STORAGE.ENERGY_BLOCK_STORAGE.get().get(id);
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

    public BatteryBlockPrefab setBlockEntity(BlockEntityType.BlockEntitySupplier<? extends NCEnergy>  blockEntity) {
        this.blockEntity = blockEntity;
        return this;
    }
    private BlockEntityType.BlockEntitySupplier<? extends NCEnergy>  blockEntity;
}
