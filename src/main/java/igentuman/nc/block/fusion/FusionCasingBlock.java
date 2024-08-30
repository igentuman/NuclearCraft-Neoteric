package igentuman.nc.block.fusion;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class FusionCasingBlock extends Block {

    public FusionCasingBlock() {
        this(Properties.of()
                .sound(SoundType.METAL)
                .strength(2.0f)
                .noOcclusion()
                .requiresCorrectToolForDrops());
    }
    public FusionCasingBlock(Properties pProperties) {
        super(pProperties.sound(SoundType.METAL));
        this.registerDefaultState(
                this.stateDefinition.any()
        );
        if(getCode().contains("glass")) {
            properties.noOcclusion();
        }
    }
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState();
    }

    public String getCode()
    {
        return ForgeRegistries.BLOCKS.getKey(this).getPath();
    }

    @Override
    public void appendHoverText(ItemStack stack, @javax.annotation.Nullable BlockGetter world, List<Component> list, TooltipFlag flag)
    {
        if (getCode().equals("fusion_reactor_connector")) {
            list.add(Component.translatable("tooltip.nc.fusion_connector.descr").withStyle(ChatFormatting.YELLOW));
        } else {
            list.add(Component.translatable("tooltip.nc.fusion_casing.descr").withStyle(ChatFormatting.YELLOW));
        }
    }
}
