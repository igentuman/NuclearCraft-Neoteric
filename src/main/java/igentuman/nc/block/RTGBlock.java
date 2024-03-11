package igentuman.nc.block;

import igentuman.nc.block.entity.energy.NCEnergy;
import igentuman.nc.block.entity.energy.RTGBE;
import igentuman.nc.content.energy.RTGs;
import igentuman.nc.setup.registration.NCEnergyBlocks;
import igentuman.nc.util.TextUtils;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.world.IBlockReader;

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

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new RTGBE(NCEnergyBlocks.ENERGY_BE.get(code()).get(), code(), state, world);
    }

    public String code()
    {
        return asItem().toString();
    }


    public void appendHoverText(ItemStack pStack, @javax.annotation.Nullable IBlockReader pLevel, List<ITextComponent> list, ITooltipFlag pFlag)
    {
        list.add(TextUtils.applyFormat(new TranslationTextComponent("rtg.fe_generation", TextUtils.numberFormat(RTGs.all().get(code()).config().getGeneration())), TextFormatting.GOLD));
    }


}
