package igentuman.nc.block.fusion;

import com.mojang.serialization.MapCodec;
import igentuman.nc.block.MultiblockControllerBlock;
import igentuman.nc.block_entity.MultiblockControllerBE;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

public class FusionReactorCoreBlock extends MultiblockControllerBlock {

    private MapCodec<FusionReactorCoreBlock> codec;
    private final Supplier<BlockEntityType<? extends MultiblockControllerBE>> beTypeSupplier;

    public FusionReactorCoreBlock(BlockBehaviour.Properties props, String name,
                                  Supplier<BlockEntityType<? extends MultiblockControllerBE>> beTypeSupplier) {
        super(props.noOcclusion(), name, beTypeSupplier);
        this.beTypeSupplier = beTypeSupplier;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        if (codec == null) {
            codec = simpleCodec(props -> new FusionReactorCoreBlock(props, getMultiblockName(), beTypeSupplier));
        }
        return codec;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }
}
