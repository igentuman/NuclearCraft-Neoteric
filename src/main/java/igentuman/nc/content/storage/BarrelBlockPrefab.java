package igentuman.nc.content.storage;

import igentuman.nc.block.entity.BarrelBE;
import igentuman.nc.handler.config.CommonConfig;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

import java.util.Arrays;

import static igentuman.nc.handler.config.CommonConfig.STORAGE_BLOCKS;

public class BarrelBlockPrefab {
    private boolean registered = true;
    private boolean initialized = false;
    private String name;
    protected int capacity = 0;

    public BarrelBlockPrefab(String name, int capacity) {
        this.capacity = capacity;
        //blockEntity = BarrelBE::new;
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
            int id = Arrays.asList(BarrelBlocks.all().keySet().stream().toArray()).indexOf(name);
            registered = STORAGE_BLOCKS.REGISTER_BARREL.get().get(id);
            capacity = STORAGE_BLOCKS.BARREL_CAPACITY.get().get(id);
            initialized = true;
        }
        return this;
    }
    public boolean isRegistered() {
        return  registered;
    }

    public TileEntityType<? extends TileEntity> getBlockEntity() {
        return blockEntity;
    }

    public BarrelBlockPrefab setBlockEntity(TileEntityType<? extends TileEntity>  blockEntity) {
        this.blockEntity = blockEntity;
        return this;
    }
    private TileEntityType<? extends TileEntity>  blockEntity;
}
