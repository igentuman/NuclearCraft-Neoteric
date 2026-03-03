package igentuman.nc.block.fusion;

import igentuman.nc.block.MultiblockBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;

import static igentuman.nc.util.TextUtils.__;

public class FusionCasingBlock extends MultiblockBlock {

    public FusionCasingBlock() {
        this(Properties.of()
                .sound(SoundType.METAL)
                .strength(3.0f)
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
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag flag)
    {
        list.add(__("tooltip.nc.fusion_casing.descr").withStyle(ChatFormatting.YELLOW));
        list.add(__("multiblock.build_in_chunk.advise").withStyle(ChatFormatting.GREEN));
    }
}
