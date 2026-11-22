package igentuman.nc.client.item;

import com.mojang.serialization.MapCodec;
import igentuman.nc.item.ResoniteCrystalItem;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import static igentuman.nc.NuclearCraft.rl;

public record ResoniteCrystalTint() implements ItemTintSource {

    public static final Identifier ID = rl("resonite_crystal");
    public static final MapCodec<ResoniteCrystalTint> MAP_CODEC = MapCodec.unit(new ResoniteCrystalTint());

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
        Holder<MobEffect> effect = ResoniteCrystalItem.getEffect(stack);
        return effect == null ? 0xFFFFFFFF : 0xFF000000 | effect.value().getColor();
    }

    @Override
    public MapCodec<ResoniteCrystalTint> type() {
        return MAP_CODEC;
    }
}
