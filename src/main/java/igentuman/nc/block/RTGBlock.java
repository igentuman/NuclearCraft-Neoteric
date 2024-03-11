package igentuman.nc.block;

import igentuman.nc.block.entity.energy.NCEnergy;
import igentuman.nc.content.energy.RTGs;
import igentuman.nc.setup.registration.NCEnergyBlocks;
import igentuman.nc.util.TextUtils;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import javax.annotation.Nullable;

import java.util.List;

public class RTGBlock extends Block {
    public RTGBlock() {
        this(Properties.of(Material.METAL)
                .sound(SoundType.METAL)
                .strength(4.0f)
                .noOcclusion()
                .requiresCorrectToolForDrops());
    }
    public RTGBlock(Properties pProperties) {
        super(pProperties.sound(SoundType.METAL));
    }

/*    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return NCEnergyBlocks.ENERGY_BE.get(code()).get().create(pPos, pState);
    }*/

    public String code()
    {
        return asItem().toString();
    }

/*
    public void appendHoverText(ItemStack pStack, @javax.annotation.Nullable BlockGetter pLevel, List<TextComponent> list, TooltipFlag pFlag)
    {
        list.add(TextUtils.applyFormat(new TranslationTextComponent("rtg.fe_generation", TextUtils.numberFormat(RTGs.all().get(code()).config().getGeneration())), TextFormatting.GOLD));
    }
*/

}
