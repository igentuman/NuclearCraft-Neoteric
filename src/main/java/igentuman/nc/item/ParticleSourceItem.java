package igentuman.nc.item;

import igentuman.nc.content.particles.IItemParticleAmount;
import igentuman.nc.content.particles.ParticleSources;
import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.util.TextUtils;
import igentuman.nc.util.math.MathUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

import static igentuman.nc.util.TextUtils.*;

public class ParticleSourceItem extends Item implements IItemParticleAmount {
    public ParticleSourceItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @NotNull ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        setAmountStored(stack, getItemCapacity(stack));
        return stack;
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack pStack) {
        return true;
    }

    @Override
    public int getItemCapacity(ItemStack stack) {
        return ParticleSources.getCapacity(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack)
    {
        return (int) (1 - MathUtils.clamp((long) ((double) getAmountStored(stack) / getItemCapacity(stack)), 0L, 1L));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag)
    {
        ParticleSourceItem particleItem = (ParticleSourceItem) stack.getItem();
        list.add(applyFormat(__("tooltip.ion_source.particle", particleItem.getParticleName(stack)), ChatFormatting.GOLD));
        list.add(applyFormat(__("tooltip.ion_source.amount", scaledFormat(particleItem.getAmountStored(stack)), scaledFormat(particleItem.getItemCapacity(stack))), ChatFormatting.GOLD));
    }

    private Component getParticleName(ItemStack stack) {
        return getParticle(stack).getLocalizedName();
    }
}
