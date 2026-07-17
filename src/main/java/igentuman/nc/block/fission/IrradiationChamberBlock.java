package igentuman.nc.block.fission;

import igentuman.nc.block.MultiblockBlock;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SoundType;

import java.util.List;

import static igentuman.nc.multiblock.fission.FissionReactorRegistration.FISSION_BLOCKS;
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
    public void appendHoverText(ItemStack pStack, @javax.annotation.Nullable BlockGetter pLevel, List<Component> list, TooltipFlag pFlag) {
        list.add(TextUtils.applyFormat(__("irradiation_chamber.descr"), ChatFormatting.AQUA));
        if (pStack.is(FISSION_BLOCKS.get("fission_reactor_pile-driver_irradiation_chamber").get().asItem())) {
            list.add(TextUtils.applyFormat(__("pile-driver_irradiation_chamber.descr"), ChatFormatting.LIGHT_PURPLE));
        }
    }
}
