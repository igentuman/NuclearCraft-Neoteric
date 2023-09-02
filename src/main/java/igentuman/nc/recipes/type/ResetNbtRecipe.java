package igentuman.nc.recipes.type;

import igentuman.nc.item.BatteryBlockItem;
import igentuman.nc.item.BatteryItem;
import igentuman.nc.item.ProcessorBlockItem;
import igentuman.nc.recipes.NcRecipeSerializers;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;


@NothingNullByDefault
public class ResetNbtRecipe extends CustomRecipe {

    public ResetNbtRecipe(ResourceLocation id) {
        super(id);
    }


    @Override
    public boolean matches(CraftingContainer inv, Level world) {
        ItemStack targetStack = ItemStack.EMPTY;
        for (int i = 0; i < inv.getContainerSize(); ++i) {
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
        return true;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv) {
        ItemStack targetStack = ItemStack.EMPTY;
        for (int i = 0; i < inv.getContainerSize(); ++i) {
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
        result.setTag(new CompoundTag());
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