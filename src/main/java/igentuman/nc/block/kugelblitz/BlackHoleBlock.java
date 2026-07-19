package igentuman.nc.block.kugelblitz;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class BlackHoleBlock extends KugelblitzEntityBlock {

    private MapCodec<BlackHoleBlock> codec;

    public BlackHoleBlock(BlockBehaviour.Properties props, String name) {
        super(props, name, false);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        if (codec == null) {
            codec = simpleCodec(props -> new BlackHoleBlock(props, name));
        }
        return codec;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }
}
