package igentuman.nc.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ResearchPaperItem extends Item {
    public ResearchPaperItem(Properties pProperties) {
        super(pProperties);
    }

    public ResearchPaperItem(Properties props, ItemGroup group)
    {
        super(props.tab(group));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flag)
    {
        CompoundNBT tag = stack.getOrCreateTag();
        if(tag.contains("vein")) {
            list.add(new TranslationTextComponent(tag.getString("vein")).withStyle(TextFormatting.AQUA));
        }
        if(tag.contains("pos")) {
            BlockPos pos = BlockPos.of(tag.getLong("pos"));
            list.add(new TranslationTextComponent("tooltip.nc.chunk_position", pos.toShortString()).withStyle(TextFormatting.BLUE));
            list.add(new TranslationTextComponent("tooltip.nc.use_in_leacher", pos.toShortString()).withStyle(TextFormatting.GREEN));
        }
    }

}
