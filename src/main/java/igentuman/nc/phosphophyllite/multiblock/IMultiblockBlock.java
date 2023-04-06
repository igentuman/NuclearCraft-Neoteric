package igentuman.nc.phosphophyllite.multiblock;

import igentuman.nc.phosphophyllite.modular.api.BlockModule;
import igentuman.nc.phosphophyllite.modular.api.IModularBlock;
import igentuman.nc.phosphophyllite.modular.api.ModuleRegistry;
import igentuman.nc.util.annotation.OnModLoad;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public interface IMultiblockBlock extends IModularBlock, EntityBlock {
    class Module extends BlockModule<IMultiblockBlock> {
        
        public Module(IModularBlock iface) {
            super(iface);
        }
        
        @Override
        public InteractionResult onUse(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
            if (player.getMainHandItem().isEmpty() && hand == InteractionHand.MAIN_HAND && (!state.hasProperty(IAssemblyStateBlock.ASSEMBLED) || !state.getValue(IAssemblyStateBlock.ASSEMBLED))) {
                if (!level.isClientSide && level.getBlockEntity(pos) instanceof IMultiblockTile tile) {
                    var controller = tile.nullableController();
                    if (controller != null && controller.lastValidationError != null) {
                        player.sendSystemMessage(controller.lastValidationError.getTextComponent());
                    } else {
                        if (controller == null) {
                            player.sendSystemMessage(Component.translatable("multiblock.error.phosphophyllite.null_controller"));
                        } else {
                            player.sendSystemMessage(Component.translatable("multiblock.error.phosphophyllite.unknown"));
                        }
                    }
                }
                return InteractionResult.SUCCESS;
            }
            return super.onUse(state, level, pos, player, hand, hitResult);
        }
        
        @OnModLoad
        static void onModLoad() {
            ModuleRegistry.registerBlockModule(IMultiblockBlock.class, Module::new);
        }
    }
}
