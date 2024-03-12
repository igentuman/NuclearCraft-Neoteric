package igentuman.nc.content.storage;

import igentuman.nc.block.entity.ContainerBE;
import igentuman.nc.handler.config.CommonConfig;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

import java.util.Arrays;
import java.util.function.Supplier;

import static igentuman.nc.handler.config.CommonConfig.STORAGE_BLOCKS;

public class ContainerBlockPrefab {
    private boolean registered = true;
    private boolean initialized = false;

    private String name;
    protected int rows = 0;
    protected int colls = 0;

    public ContainerBlockPrefab(String name, int rows, int colls) {
        this.rows = rows;
        this.colls = colls;
      //  blockEntity = ContainerBE::new;
        this.name = name;
    }

    public int getCapacity() {
        return rows*colls;
    }

    public ContainerBlockPrefab config()
    {
        if(!initialized) {
            if(!CommonConfig.isLoaded()) {
                return this;
            }
            int id = Arrays.asList(BarrelBlocks.all().keySet().stream().toArray()).indexOf(name);
            registered = STORAGE_BLOCKS.REGISTER_BARREL.get().get(id);
            initialized = true;
        }
        return this;
    }
    public boolean isRegistered() {
        return  registered;
    }

    public Supplier<? extends TileEntity> getBlockEntity(String name) {
        return () -> new ContainerBE(name);
    }

    public ContainerBlockPrefab setBlockEntity(Supplier<? extends TileEntity>  blockEntity) {
        this.blockEntity = blockEntity;
        return this;
    }
    private Supplier<? extends TileEntity>  blockEntity;

    public int getRows() {
        return rows;
    }

    public int getColls() {
        return colls;
    }
}
