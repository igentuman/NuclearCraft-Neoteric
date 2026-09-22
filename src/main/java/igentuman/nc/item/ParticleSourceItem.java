package igentuman.nc.item;

import igentuman.nc.api.particle.ParticleDefinition;
import igentuman.nc.api.particle.ParticleStack;
import igentuman.nc.particle.ParticleSourceData;
import igentuman.nc.setup.Registers;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static igentuman.nc.util.TextUtils.__;

public class ParticleSourceItem extends Item {

    private static final int MAX_BAR_WIDTH = 13;

    public ParticleSourceItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return 1;
    }

    @Nullable
    public static ParticleSourceData data(ItemStack stack) {
        return stack.get(Registers.PARTICLE_SOURCE.get());
    }

    public static void setData(ItemStack stack, ParticleSourceData data) {
        stack.set(Registers.PARTICLE_SOURCE.get(), data);
    }

    public static ParticleStack emit(ItemStack stack, long batchSize) {
        ParticleSourceData current = data(stack);
        if (current == null || current.isDepleted()) {
            return ParticleStack.EMPTY;
        }
        ParticleSourceData.EmitResult result = current.emit(batchSize);
        setData(stack, result.remaining());
        return result.stack();
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return data(stack) != null;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        ParticleSourceData sourceData = data(stack);
        if (sourceData == null) {
            return 0;
        }
        return (int) Math.min(MAX_BAR_WIDTH, Math.round(MAX_BAR_WIDTH * (double) sourceData.remainingAmount() / sourceData.capacity()));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x33BBFF;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag) {
        ParticleSourceData sourceData = data(stack);
        if (sourceData == null) {
            return;
        }
        ParticleDefinition definition = Registers.PARTICLE_DEFINITION_REGISTRY.get(sourceData.particleId());
        Component species = definition == null
                ? Component.literal(sourceData.particleId().toString())
                : __(definition.translationKey());
        list.add(__("tooltip.nuclearcraft.particle_source.species", species).withStyle(ChatFormatting.AQUA));
        list.add(__("tooltip.nuclearcraft.particle_source.remaining", sourceData.remainingAmount(), sourceData.capacity()).withStyle(ChatFormatting.BLUE));
    }
}
