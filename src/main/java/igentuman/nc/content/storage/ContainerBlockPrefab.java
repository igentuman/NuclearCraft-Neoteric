package igentuman.nc.content.storage;

import igentuman.nc.block.storage.entity.ContainerBE;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

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
        blockEntity = ContainerBE::new;
        this.name = name;
    }

    public int getCapacity() {
        return rows*colls;
    }

    public ContainerBlockPrefab config()
    {
        if(!initialized) {
            int id = ContainerBlocks.all().keySet().stream().toList().indexOf(name);
            registered = STORAGE_BLOCKS.REGISTER_CONTAINER.get().get(id);
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

    public ContainerBlockPrefab setBlockEntity(BlockEntityType.BlockEntitySupplier<? extends BlockEntity>  blockEntity) {
        this.blockEntity = blockEntity;
        return this;
    }

    private BlockEntityType.BlockEntitySupplier<? extends BlockEntity>  blockEntity;

    public int getRows() {
        return rows;
    }

    public int getColls() {
        return colls;
    }
}
