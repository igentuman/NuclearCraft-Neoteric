package igentuman.nc.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.state.BlockState;

/** Combined pickaxe/axe/shovel tool that mines any block at its tier speed and always drops results. */
public class PaxelItem extends Item {

    private final ToolMaterial material;

    public PaxelItem(ToolMaterial material, Item.Properties properties) {
        super(properties.pickaxe(material, 3.0F, -2.4F));
        this.material = material;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return material.speed();
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return true;
    }
}
