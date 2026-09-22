package igentuman.nc.block.particle;

import igentuman.nc.block.accelerator.BeamPortMode;
import igentuman.nc.block_entity.MultiblockPortBE;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import java.util.function.Supplier;

public class ParticleChamberBeamPortBlock extends ParticleChamberPortBlock {

    public static final EnumProperty<BeamPortMode> PORT_MODE = EnumProperty.create("port_mode", BeamPortMode.class);

    public ParticleChamberBeamPortBlock(Properties properties, String name,
                                        Supplier<BlockEntityType<? extends MultiblockPortBE>> blockEntityType) {
        super(properties, name, blockEntityType);
        registerDefaultState(defaultBlockState().setValue(PORT_MODE, BeamPortMode.DISABLED));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(PORT_MODE);
    }
}
