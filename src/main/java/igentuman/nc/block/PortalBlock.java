package igentuman.nc.block;

import igentuman.nc.world.dimension.Dimensions;
import igentuman.nc.world.dimension.ModTeleporter;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

import java.util.List;

import static igentuman.nc.compat.gregtech.GTUtils.formatEUEnergy;
import static igentuman.nc.handler.config.WorldConfig.DIMENSION_CONFIG;
import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.world.dimension.Dimensions.WASTELAND_KEY;

public class PortalBlock extends Block {

    private static final VoxelShape SHAPE = Shapes.box(0, 0, 0, 1, .5, 1);

    public PortalBlock() {
        super(Properties.of()
                .sound(SoundType.METAL)
                .strength(8.0F, 3600.0F)
                .requiresCorrectToolForDrops());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {

        if (entity instanceof ServerPlayer player) {
            teleportTo(player, pos.north());
        }
    }

    private void teleportTo(Entity player, BlockPos pPos) {
        if(!DIMENSION_CONFIG.registerWasteland.get()) {
            return;
        }
        if (player.level() instanceof ServerLevel serverlevel) {
            MinecraftServer minecraftserver = serverlevel.getServer();
            ResourceKey<Level> resourcekey = player.level().dimension() == Dimensions.WASTELAND ?
                    Level.OVERWORLD : Dimensions.WASTELAND;

            ServerLevel portalDimension = minecraftserver.getLevel(resourcekey);
            if (portalDimension != null && !player.isPassenger()) {
                if(resourcekey == Dimensions.WASTELAND) {
                    player.changeDimension(portalDimension, new ModTeleporter(pPos));
                } else {
                    player.changeDimension(portalDimension, new ModTeleporter(pPos));
                }
            }
        }
    }

    public void appendHoverText(ItemStack pStack, @javax.annotation.Nullable BlockGetter pLevel, List<Component> list, TooltipFlag pFlag)
    {
        if(!DIMENSION_CONFIG.registerWasteland.get()) {
            list.add(__("tooltip.nc.wasteland.disabled").withStyle(ChatFormatting.RED));
        } else {
            list.add(__("tooltip.nc.wasteland.portal.descr").withStyle(ChatFormatting.GOLD));
        }
    }
}