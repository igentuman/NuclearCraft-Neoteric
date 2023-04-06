package igentuman.nc.phosphophyllite.multiblock2.validated;

import igentuman.nc.util.annotation.NonnullDefault;
import igentuman.nc.util.annotation.OnModLoad;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import igentuman.nc.phosphophyllite.modular.api.BlockModule;
import igentuman.nc.phosphophyllite.modular.api.IModularBlock;
import igentuman.nc.phosphophyllite.modular.api.ModuleRegistry;
import igentuman.nc.phosphophyllite.multiblock2.IAssemblyStateBlock;
import igentuman.nc.phosphophyllite.multiblock2.IMultiblockBlock;
import igentuman.nc.phosphophyllite.multiblock2.MultiblockController;

@NonnullDefault
public interface IValidatedMultiblockBlock extends IMultiblockBlock {
    class Module extends BlockModule<IMultiblockBlock> {
        
        public Module(IModularBlock iface) {
            super(iface);
        }
        
        @Override
        public InteractionResult onUse(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
            if (player.getMainHandItem().isEmpty() && hand == InteractionHand.MAIN_HAND && (!state.hasProperty(IAssemblyStateBlock.ASSEMBLED) || !state.getValue(IAssemblyStateBlock.ASSEMBLED))) {
                if (!level.isClientSide && level.getBlockEntity(pos) instanceof IValidatedMultiblockTile tile) {
                    MultiblockController<?, ?, ?> controller = tile.nullableController();
                    if (controller != null) {
                        final IValidatedMultiblock.Module<?, ?, ?> module = controller.module(IValidatedMultiblock.class, IValidatedMultiblock.Module.class);
                        if (module != null) {
                            if (module.lastValidationError != null) {
                                player.sendSystemMessage(module.lastValidationError.getTextComponent());
                            } else {
                                player.sendSystemMessage(Component.translatable("multiblock.error.phosphophyllite.unknown"));
                            }
                        } else {
                            player.sendSystemMessage(Component.translatable("multiblock.error.phosphophyllite.null_validated_module"));
                        }
                    } else {
                        player.sendSystemMessage(Component.translatable("multiblock.error.phosphophyllite.null_controller"));
                    }
                }
                return InteractionResult.SUCCESS;
            }
            return super.onUse(state, level, pos, player, hand, hitResult);
        }
        
        @OnModLoad
        private static void onModLoad() {
            ModuleRegistry.registerBlockModule(IValidatedMultiblockBlock.class, Module::new);
        }
    }
}
