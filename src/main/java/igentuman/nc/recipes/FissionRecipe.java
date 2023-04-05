package igentuman.nc.recipes;

import com.google.gson.JsonObject;
import igentuman.nc.item.ItemFuel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import static igentuman.nc.NuclearCraft.MODID;

public class FissionRecipe implements Recipe<SimpleContainer>, INCRecipe {

    private final ResourceLocation id;
    private final Ingredient input;
    private final ItemStack output;


    public FissionRecipe(ResourceLocation pRecipeId, Ingredient input, ItemStack output) {
        id = pRecipeId;
        this.input = input;
        this.output = output.copy();
    }

    public FissionRecipe(ItemStack input, ItemStack output) {
        id = new ResourceLocation(MODID, "fission_reactor");
        this.input = Ingredient.of(input);
        this.output = output.copy();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    @Override
    public boolean matches(SimpleContainer pContainer, Level pLevel) {
        if(pLevel.isClientSide()) {
            return false;
        }

        return input.test(pContainer.getItem(0));
    }


    @Override
    public ItemStack assemble(SimpleContainer pContainer) {
        return output;
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    public Ingredient getInput() {
        return input;
    }
    

    public ItemStack getOutput() {
        return output.copy();
    }

    @Override
    public CompoundTag serialize() {
        CompoundTag data = new CompoundTag();
        data.putString("type", "fission_reactor");
        data.put("input", getInputStack().serializeNBT());
        data.put("output", getOutput().serializeNBT());
        return data;
    }

    @Override
    public INCRecipe deserialize(CompoundTag tag) {
        return null;
    }

    @NotNull
    @Override
    public ItemStack getResultItem() {
        return output.copy();
    }

    public int getDepletionTime() {
        return ((ItemFuel)getInputStack().getItem()).depletion*20;
    }

    public int getEnergy() {
        return ((ItemFuel)getInputStack().getItem()).forge_energy;
    }

    public double getHeat() {
        return ((ItemFuel)getInputStack().getItem()).heat;
    }

    public double getRadiation() {
        return 0;
    }

    public static class Type implements RecipeType<FissionRecipe> {
        private Type() { }
        public static final Type INSTANCE = new Type();
        public static final String ID = "fission_reactor";
    }

    public static class Serializer implements RecipeSerializer<FissionRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID =
                new ResourceLocation(MODID, "fission_reactor");

        @Override
        public FissionRecipe fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {
            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pSerializedRecipe, "output"));

            Ingredient input = Ingredient.of(ShapedRecipe.itemFromJson(GsonHelper.getAsJsonObject(pSerializedRecipe, "input")));

            return new FissionRecipe(pRecipeId, input, output);
        }

        @Override
        public @Nullable FissionRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            Ingredient input = Ingredient.fromNetwork(buf);
            ItemStack output = buf.readItem();
            return new FissionRecipe(id, input, output);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, FissionRecipe recipe) {
            buf.writeInt(recipe.getIngredients().size());

            for (Ingredient ing : recipe.getIngredients()) {
                ing.toNetwork(buf);
            }
            buf.writeItemStack(recipe.getResultItem(), false);
        }

    }

    public ItemStack getInputStack() {
        return getInput().getItems()[0];
    }
}