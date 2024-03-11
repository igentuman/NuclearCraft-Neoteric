package igentuman.nc.item;

import net.minecraft.item.IItemTier;
import net.minecraft.item.ShovelItem;
import net.minecraft.tags.BlockTags;
import net.minecraft.item.ItemStack;
import net.minecraft.block.BlockState;

public class PaxelItem extends ShovelItem {
    public PaxelItem(float pAttackDamageModifier, float pAttackSpeedModifier, IItemTier pTier, Properties pProperties) {
        super(pTier, pAttackDamageModifier, pAttackSpeedModifier, pProperties);
    }

    public float getDestroySpeed(ItemStack pStack, BlockState pState) {
        return this.speed;
    }

    public boolean isCorrectToolForDrops(BlockState pBlock) {
       /* if (net.minecraftforge.common.TierSortingRegistry.isTierSorted(getTier())) {
            return net.minecraftforge.common.TierSortingRegistry.isCorrectTierForDrops(getTier(), pBlock);
        }*/
/*        int i = this.getTier().getLevel();
        if (i < 3 && pBlock.is(BlockTags.)) {
            return false;
        } else if (i < 2 && pBlock.is(BlockTags.NEEDS_IRON_TOOL)) {
            return false;
        } else {
            return i < 1 && pBlock.is(BlockTags.NEEDS_STONE_TOOL) ? false : true;
        }*/
        return true;
    }
}
