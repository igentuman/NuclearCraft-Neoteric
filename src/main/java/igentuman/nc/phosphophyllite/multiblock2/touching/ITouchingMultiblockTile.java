package igentuman.nc.phosphophyllite.multiblock2.touching;

import igentuman.nc.phosphophyllite.util.VectorUtil;
import igentuman.nc.util.annotation.NonnullDefault;
import igentuman.nc.util.annotation.OnModLoad;
import igentuman.nc.util.joml.Vector3i;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import igentuman.nc.phosphophyllite.modular.api.IModularTile;
import igentuman.nc.phosphophyllite.modular.api.ModuleRegistry;
import igentuman.nc.phosphophyllite.modular.api.TileModule;
import igentuman.nc.phosphophyllite.multiblock2.IMultiblockTile;
import igentuman.nc.phosphophyllite.multiblock2.MultiblockController;
import igentuman.nc.phosphophyllite.multiblock2.common.IPersistentMultiblock;
import igentuman.nc.phosphophyllite.multiblock2.common.IPersistentMultiblockTile;
import igentuman.nc.phosphophyllite.multiblock2.modular.ICoreMultiblockTileModule;
import igentuman.nc.phosphophyllite.multiblock2.rectangular.IRectangularMultiblock;
import igentuman.nc.phosphophyllite.multiblock2.rectangular.IRectangularMultiblockBlock;
import igentuman.nc.phosphophyllite.multiblock2.rectangular.IRectangularMultiblockTile;

@NonnullDefault
public interface ITouchingMultiblockTile<
        TileType extends BlockEntity & ITouchingMultiblockTile<TileType, BlockType, ControllerType> & IRectangularMultiblockTile<TileType, BlockType, ControllerType> & IPersistentMultiblockTile<TileType, BlockType, ControllerType>,
        BlockType extends Block & IRectangularMultiblockBlock,
        ControllerType extends MultiblockController<TileType, BlockType, ControllerType> & ITouchingMultiblock<TileType, BlockType, ControllerType> & IRectangularMultiblock<TileType, BlockType, ControllerType> & IPersistentMultiblock<TileType, BlockType, ControllerType>
        > extends IMultiblockTile<TileType, BlockType, ControllerType> {
    
    final class Module<
            TileType extends BlockEntity & ITouchingMultiblockTile<TileType, BlockType, ControllerType> & IRectangularMultiblockTile<TileType, BlockType, ControllerType> & IPersistentMultiblockTile<TileType, BlockType, ControllerType>,
            BlockType extends Block & IRectangularMultiblockBlock,
            ControllerType extends MultiblockController<TileType, BlockType, ControllerType> & ITouchingMultiblock<TileType, BlockType, ControllerType> & IRectangularMultiblock<TileType, BlockType, ControllerType> & IPersistentMultiblock<TileType, BlockType, ControllerType>
            > extends TileModule<TileType> implements ICoreMultiblockTileModule<TileType, BlockType, ControllerType> {
        
        boolean assembled = false;
        final Vector3i min = new Vector3i();
        final Vector3i max = new Vector3i();
        
        @OnModLoad
        public static void register() {
            ModuleRegistry.registerTileModule(ITouchingMultiblockTile.class, Module::new);
        }
        
        public Module(IModularTile iface) {
            super(iface);
        }
        
        @Override
        public boolean shouldConnectTo(TileType tile, Direction direction) {
            if (!assembled) {
                return true;
            }
            return VectorUtil.lequal(min, tile.getBlockPos()) && VectorUtil.grequal(max, tile.getBlockPos());
        }
    
        @Override
        public String saveKey() {
            return "touching_multiblock";
        }
    
        @Override
        public void readNBT(CompoundTag nbt) {
            assembled = nbt.getBoolean("assembled");
            min.set(nbt.getInt("minx"), nbt.getInt("miny"), nbt.getInt("minz"));
            max.set(nbt.getInt("maxx"), nbt.getInt("maxy"), nbt.getInt("maxz"));
        }
        
        @Override
        public CompoundTag writeNBT() {
            final var tag = new CompoundTag();
            tag.putBoolean("assembled", assembled);
            tag.putInt("minx", min.x());
            tag.putInt("miny", min.y());
            tag.putInt("minz", min.z());
            tag.putInt("maxx", max.x());
            tag.putInt("maxy", max.y());
            tag.putInt("maxz", max.z());
            return tag;
        }
    }
}
