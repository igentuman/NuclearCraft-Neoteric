package igentuman.nc.effect;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;

import java.util.function.Consumer;

// Marker effect: bow/crossbow draw-speed buff. Mechanic lives in CrystalBuffEvents.onUseItemTick (trims draw ticks by amplifier+1 per tick).
public class QuickdrawBoost extends MobEffect {

    public QuickdrawBoost(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
        consumer.accept(new IClientMobEffectExtensions() {
            @Override
            public boolean renderInventoryIcon(MobEffectInstance instance, EffectRenderingInventoryScreen<?> screen, PoseStack poseStack, int x, int y, int blitOffset) {
                Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(new ItemStack(Items.BOW), x + 1, y + 1);
                return true;
            }

            @Override
            public boolean renderGuiIcon(MobEffectInstance instance, Gui gui, PoseStack poseStack, int x, int y, float z, float alpha) {
                Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(new ItemStack(Items.BOW), x + 1, y + 1);
                return true;
            }
        });
    }
}
