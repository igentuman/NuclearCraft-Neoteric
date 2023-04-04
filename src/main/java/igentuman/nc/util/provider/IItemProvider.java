package igentuman.nc.util.provider;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;

@MethodsReturnNonnullByDefault
public interface IItemProvider extends IBaseProvider, ItemLike {

    default ItemStack getItemStack() {
        return getItemStack(1);
    }

    default ItemStack getItemStack(int size) {
        return new ItemStack(asItem(), size);
    }

    @Override
    default ResourceLocation getRegistryName() {
        return ForgeRegistries.ITEMS.getKey(asItem());
    }

    @Override
    default String getTranslationKey() {
        return asItem().getDescriptionId();
    }
}