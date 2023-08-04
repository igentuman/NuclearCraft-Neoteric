package igentuman.nc.recipes.serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import igentuman.nc.NuclearCraft;
import igentuman.nc.handler.config.CommonConfig;
import igentuman.nc.recipes.type.ItemStackToItemStackRecipe;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.ingredient.creator.IngredientCreatorAccess;
import igentuman.nc.util.JsonConstants;
import igentuman.nc.util.SerializerHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ItemStackToItemStackRecipeSerializer<RECIPE extends ItemStackToItemStackRecipe> implements RecipeSerializer<RECIPE> {

    private final IFactory<RECIPE> factory;

    public ItemStackToItemStackRecipeSerializer(IFactory<RECIPE> factory) {
        this.factory = factory;
    }

    public ItemStack getFirstMatchingItemStackFromIngredient(ItemStackIngredient ingredient) {
        List<ItemStack> stacks = ingredient.getRepresentations();
        if(stacks.size() == 1) return stacks.get(0);
        HashMap<String, ItemStack> mappedStacks = new HashMap<>();
        for(ItemStack stack: stacks) {
            mappedStacks.put(Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(stack.getItem())).getNamespace(), stack);
        }
        for(String modid: CommonConfig.MaterialProductsConfig.MODS_PRIORITY.get()) {
            if(mappedStacks.containsKey(modid)) return mappedStacks.get(modid);
        }
        return stacks.get(0);
    }

    @Override
    public RECIPE fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
        JsonElement input = GsonHelper.isArrayNode(json, JsonConstants.INPUT) ? GsonHelper.getAsJsonArray(json, JsonConstants.INPUT) :
                            GsonHelper.getAsJsonObject(json, JsonConstants.INPUT);
        ItemStackIngredient inputIngredient = IngredientCreatorAccess.item().deserialize(input);
        ItemStack[] outputItems;
        if(GsonHelper.isArrayNode(json, JsonConstants.OUTPUT)) {
            JsonElement output = GsonHelper.getAsJsonArray(json, JsonConstants.OUTPUT);
            outputItems = new ItemStack[output.getAsJsonArray().size()];
            int i = 0;
            for (JsonElement out : output.getAsJsonArray()) {
                ItemStack stack = getFirstMatchingItemStackFromIngredient(IngredientCreatorAccess.item().deserialize(out));
                if (stack.isEmpty()) {
                    return null;
                }
                outputItems[i] = stack;
                i++;
            }
        } else {
           outputItems = new ItemStack[]{SerializerHelper.getItemStack(json, JsonConstants.OUTPUT)};
        }
        double timeModifier = GsonHelper.getAsDouble(json, "timeModifier", 1.0);
        double powerModifier = GsonHelper.getAsDouble(json, "powerModifier", 1.0);
        double radiation = GsonHelper.getAsDouble(json, "radiation", 1.0);
        if (outputItems.length == 0) {
            throw new JsonSyntaxException("Recipe output must not be empty.");
        }
        return this.factory.create(recipeId, inputIngredient, outputItems, timeModifier, powerModifier, radiation);
    }

    @Override
    public RECIPE fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
        try {
            ItemStackIngredient inputIngredient = IngredientCreatorAccess.item().read(buffer);
            int outputSize = buffer.readInt();
            ItemStack[] outputList = new ItemStack[outputSize];
            for(int i = 0; i < outputSize; i++) {
                outputList[i] = buffer.readItem();
            }
            double timeModifier = buffer.readDouble();
            double powerModifier = buffer.readDouble();
            double radiation = buffer.readDouble();
            return this.factory.create(recipeId, inputIngredient, outputList, timeModifier, powerModifier, radiation);
        } catch (Exception e) {
            NuclearCraft.LOGGER.error("Error reading itemstack to itemstack recipe from packet.", e);
            throw e;
        }
    }

    @Override
    public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull RECIPE recipe) {
        try {
            recipe.write(buffer);
        } catch (Exception e) {
            NuclearCraft.LOGGER.error("Error writing itemstack to itemstack recipe to packet.", e);
            throw e;
        }
    }

    @FunctionalInterface
    public interface IFactory<RECIPE extends ItemStackToItemStackRecipe> {
        RECIPE create(ResourceLocation id, ItemStackIngredient input, ItemStack[] output, double timeMultiplier, double powerMultiplier, double radiationMultiplier);
    }
}