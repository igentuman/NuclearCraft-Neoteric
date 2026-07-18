package igentuman.nc.client.block;

import net.minecraft.client.gui.Font;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.IItemDecorator;

public class BatteryBlockItemDecorator implements IItemDecorator {
    public static final BatteryBlockItemDecorator INSTANCE = new BatteryBlockItemDecorator();

    private BatteryBlockItemDecorator() {
    }

    @Override
    public boolean render(Font font, ItemStack stack, int xOffset, int yOffset, float blitOffset) {
        if (stack.isEmpty()) {
            return false;
        }
        return false;
    }
}
