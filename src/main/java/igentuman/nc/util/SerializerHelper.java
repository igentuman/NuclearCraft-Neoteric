package igentuman.nc.util;

import com.google.gson.*;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import igentuman.nc.util.annotation.NothingNullByDefault;
import igentuman.nc.util.math.FloatingLong;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.antlr.v4.runtime.misc.NotNull;;


@NothingNullByDefault
public class SerializerHelper {

    private SerializerHelper() {
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /**
     * Deserializes a FloatingLong that is stored in a specific key in a Json Object.
     *
     * @param json Json Object.
     * @param key  Key the FloatingLong is stored in.
     *
     * @return FloatingLong.
     */
    public static FloatingLong getFloatingLong(@NotNull JsonObject json, @NotNull String key) {
        if (!json.has(key)) {
            throw new JsonSyntaxException("Missing '" + key + "', expected to find an object");
        }
        JsonElement jsonElement = json.get(key);
        if (!jsonElement.isJsonPrimitive()) {
            throw new JsonSyntaxException("Expected '" + key + "' to be a json primitive representing a FloatingLong");
        }
        try {
            return FloatingLong.parseFloatingLong(jsonElement.getAsNumber().toString(), true);
        } catch (NumberFormatException e) {
            throw new JsonSyntaxException("Expected '" + key + "' to be a valid FloatingLong (positive decimal number)");
        }
    }

    private static void validateKey(@NotNull JsonObject json, @NotNull String key) {
        if (!json.has(key)) {
            throw new JsonSyntaxException("Missing '" + key + "', expected to find an object");
        }
        if (!json.get(key).isJsonObject()) {
            throw new JsonSyntaxException("Expected '" + key + "' to be an object");
        }
    }
    
    /**
     * Helper to get and deserialize an Item Stack from a specific sub-element in a Json Object.
     *
     * @param json Parent Json Object
     * @param key  Key in the Json Object that contains an Item Stack.
     *
     * @return Item Stack.
     */
    public static ItemStack getItemStack(@NotNull JsonObject json, @NotNull String key) {
        validateKey(json, key);
        return ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, key));
    }

    public static ItemStack getItemStack(@NotNull JsonObject json) {
        return ShapedRecipe.itemStackFromJson(json);
    }

    /**
     * Helper to get and deserialize a Fluid Stack from a specific sub-element in a Json Object.
     *
     * @param json Parent Json Object
     * @param key  Key in the Json Object that contains a Fluid Stack.
     *
     * @return Fluid Stack.
     */
    public static FluidStack getFluidStack(@NotNull JsonObject json, @NotNull String key) {
        validateKey(json, key);
        return deserializeFluid(GsonHelper.getAsJsonObject(json, key));
    }

    public static FluidStack getFluidStack(@NotNull JsonObject json) {
        return deserializeFluid(json);
    }

    /**
     * Helper to deserialize a Json Object into a Fluid Stack.
     *
     * @param json Json object to deserialize.
     *
     * @return Fluid Stack.
     */
    public static FluidStack deserializeFluid(@NotNull JsonObject json) {
        if (!json.has("amount")) {
            throw new JsonSyntaxException("Expected to receive a amount that is greater than zero");
        }
        JsonElement count = json.get("amount");
        if (!GsonHelper.isNumberValue(count)) {
            throw new JsonSyntaxException("Expected amount to be a number greater than zero.");
        }
        int amount = count.getAsJsonPrimitive().getAsInt();
        if (amount < 1) {
            throw new JsonSyntaxException("Expected amount to be greater than zero.");
        }
        ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(json, "fluid"));
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(resourceLocation);
        if (fluid == null || fluid == Fluids.EMPTY) {
            throw new JsonSyntaxException("Invalid fluid type '" + resourceLocation + "'");
        }
        CompoundTag nbt = null;
        if (json.has("nbt")) {
            JsonElement jsonNBT = json.get("nbt");
            try {
                if (jsonNBT.isJsonObject()) {
                    nbt = TagParser.parseTag(GSON.toJson(jsonNBT));
                } else {
                    nbt = TagParser.parseTag(GsonHelper.convertToString(jsonNBT, "nbt"));
                }
            } catch (CommandSyntaxException e) {
                throw new JsonSyntaxException("Invalid NBT entry for fluid '" + resourceLocation + "'");
            }
        }
        return new FluidStack(fluid, amount, nbt);
    }


    /**
     * Helper to serialize an Item Stack into a Json Object.
     *
     * @param stack Stack to serialize.
     *
     * @return Json representation.
     */
    public static JsonElement serializeItemStack(@NotNull ItemStack stack) {
        JsonObject json = new JsonObject();
        json.addProperty("item", ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());
        if (stack.getCount() > 1) {
            json.addProperty("count", stack.getCount());
        }
        if (stack.hasTag()) {
            json.addProperty("nbt", stack.getTag().toString());
        }
        return json;
    }

    /**
     * Helper to serialize a Fluid Stack into a Json Object.
     *
     * @param stack Stack to serialize.
     *
     * @return Json representation.
     */
    public static JsonElement serializeFluidStack(@NotNull FluidStack stack) {
        JsonObject json = new JsonObject();
        json.addProperty("fluid", ForgeRegistries.FLUIDS.getKey(stack.getFluid()).toString());
        json.addProperty("amount", stack.getAmount());
        if (stack.hasTag()) {
            json.addProperty("nbt", stack.getTag().toString());
        }
        return json;
    }
}