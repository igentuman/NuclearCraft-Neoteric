package igentuman.nc.setup.storage;

import igentuman.nc.block.entity.barrel.BarrelBE;
import igentuman.nc.handler.config.CommonConfig;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import static igentuman.nc.handler.config.CommonConfig.STORAGE_BLOCKS;

public class BarrelBlockPrefab {
    private boolean registered = true;
    private boolean initialized = false;
    private String name;
    protected int capacity = 0;

    public BarrelBlockPrefab(String name, int capacity) {
        this.capacity = capacity;
        blockEntity = BarrelBE::new;
    }

    public int getCapacity() {
        return capacity*1000;
    }

    public BarrelBlockPrefab setCapacity(int capacity) {
        this.capacity = capacity;
        return this;
    }

    public BarrelBlockPrefab config()
    {
        if(!initialized) {
            if(!CommonConfig.isLoaded()) {
                return this;
            }
            int id = BarrelBlocks.all().keySet().stream().toList().indexOf(name);
            registered = STORAGE_BLOCKS.REGISTER_BARREL.get().get(id);
            capacity = STORAGE_BLOCKS.BARREL_CAPACITY.get().get(id);
            initialized = true;
        }
        return this;
    }
    public boolean isRegistered() {
        return  registered;
    }

    public BlockEntityType.BlockEntitySupplier<? extends BlockEntity>  getBlockEntity() {
        return blockEntity;
    }

    public BarrelBlockPrefab setBlockEntity(BlockEntityType.BlockEntitySupplier<? extends BlockEntity>  blockEntity) {
        this.blockEntity = blockEntity;
        return this;
    }
    private BlockEntityType.BlockEntitySupplier<? extends BlockEntity>  blockEntity;
}
