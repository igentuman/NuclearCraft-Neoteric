package igentuman.nc.recipes;

import igentuman.nc.util.IgnoredIInventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class NcRecipe implements Recipe<IgnoredIInventory> {

    private final ResourceLocation id;

    /**
     * @param id Recipe name.
     */
    protected NcRecipe(ResourceLocation id) {
        this.id = Objects.requireNonNull(id, "Recipe name cannot be null.");
    }

    /**
     * Writes this recipe to a PacketBuffer.
     *
     * @param buffer The buffer to write to.
     */
    public abstract void write(FriendlyByteBuf buffer);

    @NotNull
    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public boolean matches(@NotNull IgnoredIInventory inv, @NotNull Level world) {
        return !isIncomplete();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public abstract boolean isIncomplete();

    @NotNull
    @Override
    public ItemStack assemble(@NotNull IgnoredIInventory inv) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @NotNull
    @Override
    public ItemStack getResultItem() {
        return ItemStack.EMPTY;
    }

    public static INCRecipe getRecipeFromTag(CompoundTag tag)
    {
        if(tag == null) return null;
        String recipeType = tag.getString("type");
        switch (recipeType) {
            case "fission_reactor":
                return new FissionRecipe(
                        ItemStack.of(tag.getCompound("input")),
                        ItemStack.of(tag.getCompound("output")));
        }
        return null;
    }
}