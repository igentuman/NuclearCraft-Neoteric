package igentuman.nc.recipes.type;

import igentuman.nc.item.RadShieldingItem;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;


@NothingNullByDefault
public class RadShieldingRecipe extends SpecialRecipe {

    public RadShieldingRecipe(ResourceLocation id) {
        super(id);
    }


    @Override
    public boolean matches(CraftingInventory inv, World world) {
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
    public ItemStack assemble(CraftingInventory inv) {
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
    public IRecipeSerializer<?> getSerializer() {
        return null;
        //return NcRecipeSerializers.SHIELDING;
    }

    @SubscribeEvent
    public static void onCrafting(PlayerEvent.ItemCraftedEvent event) {
        ItemStack result = event.getCrafting();
    }
}