package igentuman.nc.client.block;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.IItemDecorator;

public class BatteryBlockItemDecorator implements IItemDecorator {
    public static final BatteryBlockItemDecorator INSTANCE = new BatteryBlockItemDecorator();

    private BatteryBlockItemDecorator() {
    }

    @Override
    public boolean render(GuiGraphics guiGraphics, Font font, ItemStack stack, int x, int y) {
        if (stack.isEmpty()) {
            return false;
        }
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(0, 0, 0);
        pose.scale(1.0F, 1.0F, 1.0F);
        guiGraphics.renderItem(stack, x, y);
        pose.popPose();
        return true;
    }
}
