package igentuman.nc.block.fusion;

import igentuman.nc.block.MultiblockBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class MultiblockCasingBlock extends MultiblockBlock {

    public MultiblockCasingBlock() {
        this(Properties.of()
                .sound(SoundType.METAL)
                .strength(2.0f)
                .noOcclusion()
                .requiresCorrectToolForDrops());
    }
    public MultiblockCasingBlock(Properties pProperties) {
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
        list.add(Component.translatable("tooltip.nc.fusion_casing.descr").withStyle(ChatFormatting.YELLOW));
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return asItem().toString().contains("glass");
    }
}
