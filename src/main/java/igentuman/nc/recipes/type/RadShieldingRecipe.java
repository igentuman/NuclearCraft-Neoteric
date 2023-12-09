package igentuman.nc.recipes.type;

import igentuman.nc.item.RadShieldingItem;
import igentuman.nc.recipes.NcRecipeSerializers;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;


@NothingNullByDefault
public class RadShieldingRecipe extends CustomRecipe {

    public RadShieldingRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, CraftingBookCategory.EQUIPMENT);
    }


    @Override
    public boolean matches(CraftingContainer inv, Level world) {
        ItemStack shielding = ItemStack.EMPTY;
        ItemStack armor = ItemStack.EMPTY;
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            if(inv.getItem(i).getItem() instanceof RadShieldingItem) {
                shielding = inv.getItem(i);
                continue;
            }
            if(inv.getItem(i).getItem() instanceof ArmorItem) {
                armor = inv.getItem(i);
                continue;
            }
            if(!shielding.isEmpty() && !armor.isEmpty() && !inv.getItem(i).isEmpty()) {
                return false;
            }
        }
        //only allow 2 items: shielding and armor
        if (shielding.isEmpty() || armor.isEmpty()) {
            return false;
        }
        return true;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess access) {
        ItemStack shielding = ItemStack.EMPTY;
        ItemStack armor = ItemStack.EMPTY;
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            if(inv.getItem(i).getItem() instanceof RadShieldingItem) {
                shielding = inv.getItem(i);
                continue;
            }
            if(inv.getItem(i).getItem() instanceof ArmorItem) {
                armor = inv.getItem(i);
                continue;
            }
            if(!shielding.isEmpty() && !armor.isEmpty() && !inv.getItem(i).isEmpty()) {
                return ItemStack.EMPTY;
            }
        }

        if (shielding.isEmpty() || armor.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack result = armor.copy();
        result.getOrCreateTag().putInt("rad_shielding", ((RadShieldingItem)shielding.getItem()).getRadiationShieldingLevel());
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return NcRecipeSerializers.SHIELDING.get();
    }

    @SubscribeEvent
    public static void onCrafting(PlayerEvent.ItemCraftedEvent event) {
        ItemStack result = event.getCrafting();
    }
}