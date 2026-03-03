package igentuman.nc.recipes.type;

import igentuman.api.platform.NCItemStacks;
import igentuman.nc.item.BatteryBlockItem;
import igentuman.nc.item.BatteryItem;
import igentuman.nc.item.ProcessorBlockItem;
import igentuman.nc.recipes.NcRecipeSerializers;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;


@NothingNullByDefault
public class ResetNbtRecipe extends CustomRecipe {

    public ResetNbtRecipe(CraftingBookCategory cat) {
        super(CraftingBookCategory.EQUIPMENT);
    }


    @Override
    public boolean matches(CraftingInput inv, Level world) {
        ItemStack targetStack = ItemStack.EMPTY;
        for (int i = 0; i < inv.size(); ++i) {
            if(targetStack != ItemStack.EMPTY && !inv.getItem(i).isEmpty()) {
                return false; //only allow 1 item
            }
            if(inv.getItem(i).getItem() instanceof BatteryBlockItem) {
                targetStack = inv.getItem(i);
                continue;
            }

            if(inv.getItem(i).getItem() instanceof BatteryItem) {
                targetStack = inv.getItem(i);
                continue;
            }

            if(inv.getItem(i).getItem() instanceof ProcessorBlockItem) {
                targetStack = inv.getItem(i);
                continue;
            }
        }
        return targetStack != ItemStack.EMPTY;
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider access) {
        ItemStack targetStack = ItemStack.EMPTY;
        for (int i = 0; i < inv.size(); ++i) {
            if(targetStack != ItemStack.EMPTY && !inv.getItem(i).isEmpty()) {
                return ItemStack.EMPTY; //only allow 1 item
            }
            if(inv.getItem(i).getItem() instanceof BatteryBlockItem) {
                targetStack = inv.getItem(i);
                continue;
            }

            if(inv.getItem(i).getItem() instanceof BatteryItem) {
                targetStack = inv.getItem(i);
                continue;
            }

            if(inv.getItem(i).getItem() instanceof ProcessorBlockItem) {
                targetStack = inv.getItem(i);

                continue;
            }
        }
        ItemStack result = targetStack.copy();
        result.setCount(1);
        NCItemStacks.setTag(result, new CompoundTag());
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return NcRecipeSerializers.RESET_NBT.get();
    }

    @SubscribeEvent
    public static void onCrafting(PlayerEvent.ItemCraftedEvent event) {
        ItemStack result = event.getCrafting();

    }
}
