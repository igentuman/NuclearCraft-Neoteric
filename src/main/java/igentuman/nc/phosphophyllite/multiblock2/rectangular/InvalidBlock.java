package igentuman.nc.phosphophyllite.multiblock2.rectangular;

import igentuman.nc.util.annotation.NonnullDefault;
import igentuman.nc.util.joml.Vector3ic;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import igentuman.nc.phosphophyllite.multiblock2.ValidationException;

@NonnullDefault
@SuppressWarnings("unused")
public class InvalidBlock extends ValidationException {
    
    public InvalidBlock(String s) {
        super(s);
    }
    
    public InvalidBlock(Block block, Vector3ic worldPosition, String multiblockPosition) {
        super(Component.translatable(
                "multiblock.error.phosphophyllite.invalid_block." + multiblockPosition,
                Component.translatable(block.getDescriptionId()),
                "(x: " + worldPosition.x() + "; y: " + worldPosition.y() + "; z: " + worldPosition.z() + ")")
        );
    }
}
