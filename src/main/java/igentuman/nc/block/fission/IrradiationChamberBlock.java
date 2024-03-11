package igentuman.nc.block.fission;

import igentuman.nc.block.entity.fission.FissionIrradiationChamberBE;
import igentuman.nc.multiblock.fission.FissionReactor;
import igentuman.nc.util.TextUtils;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import javax.annotation.Nullable;

import java.util.List;

public class IrradiationChamberBlock extends Block {

    public IrradiationChamberBlock() {
        this(Properties.of(Material.METAL)
                .sound(SoundType.METAL)
                .strength(2.0f)
                .requiresCorrectToolForDrops());
    }

    public IrradiationChamberBlock(Properties pProperties) {
        super(pProperties.sound(SoundType.METAL));
    }
    /*
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return FissionReactor.FISSION_BE.get("fission_reactor_irradiation_chamber").get().create(pPos, pState);
    }*/


    @Override
    public void appendHoverText(ItemStack pStack, @javax.annotation.Nullable IBlockReader pLevel, List<ITextComponent> list, ITooltipFlag pFlag) {
        list.add(TextUtils.applyFormat(new TranslationTextComponent("irradiation_chamber.descr"), TextFormatting.AQUA));
    }

}
