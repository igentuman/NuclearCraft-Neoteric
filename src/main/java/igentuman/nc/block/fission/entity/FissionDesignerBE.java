package igentuman.nc.block.fission.entity;

import igentuman.nc.block.entity.NuclearCraftBE;
import igentuman.nc.setup.registration.FissionDesignerRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class FissionDesignerBE extends NuclearCraftBE {

    public static final String NAME = "fission_reactor_designer";

    public FissionDesignerBE(BlockPos pPos, BlockState pBlockState) {
        super(FissionDesignerRegistration.FISSION_REACTOR_DESIGNER_BE.get(), pPos, pBlockState);
    }

    public void saveDesignToPaper(Player player) {
    }

    public ItemStack loadDesignFromPaper(ItemStack stack) {
        return ItemStack.EMPTY;
    }
}
