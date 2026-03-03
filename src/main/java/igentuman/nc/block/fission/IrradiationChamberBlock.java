package igentuman.nc.block.fission;

import igentuman.nc.block.MultiblockBlock;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.SoundType;

import java.util.List;

import static igentuman.nc.util.TextUtils.__;

public class IrradiationChamberBlock extends MultiblockBlock {

    public IrradiationChamberBlock() {
        this(Properties.of()
                .sound(SoundType.METAL)
                .strength(2.0f)
                .requiresCorrectToolForDrops());
    }

    public IrradiationChamberBlock(Properties pProperties) {
        super(pProperties.sound(SoundType.METAL));
    }

    @Override
    public void appendHoverText(ItemStack pStack, Item.TooltipContext pContext, List<Component> list, TooltipFlag pFlag) {
        list.add(TextUtils.applyFormat(__("irradiation_chamber.descr"), ChatFormatting.AQUA));
    }
}
