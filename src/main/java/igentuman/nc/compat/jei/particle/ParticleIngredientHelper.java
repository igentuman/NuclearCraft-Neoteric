package igentuman.nc.compat.jei.particle;

import igentuman.nc.NuclearCraft;
import igentuman.nc.api.particle.ParticleDefinition;
import igentuman.nc.api.particle.ParticleStack;
import igentuman.nc.setup.Registers;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ParticleIngredientHelper implements IIngredientHelper<ParticleStack> {

    @Override
    public IIngredientType<ParticleStack> getIngredientType() {
        return ParticleType.PARTICLE;
    }

    @Override
    public String getDisplayName(ParticleStack ingredient) {
        if (ingredient.isEmpty()) return "";
        ParticleDefinition definition = Registers.PARTICLE_DEFINITION_REGISTRY.get(ingredient.particleId());
        return definition == null
                ? ingredient.particleId().getPath()
                : Component.translatable(definition.translationKey()).getString();
    }

    @Override
    @SuppressWarnings("removal")
    public String getUniqueId(ParticleStack ingredient, UidContext context) {
        return ingredient.isEmpty() ? "empty" : NuclearCraft.MODID + ":particle/" + ingredient.particleId();
    }

    @Override
    public ResourceLocation getResourceLocation(ParticleStack ingredient) {
        return ingredient.particleId();
    }

    @Override
    public long getAmount(ParticleStack ingredient) {
        return ingredient.amount();
    }

    @Override
    public ItemStack getCheatItemStack(ParticleStack ingredient) {
        return ItemStack.EMPTY;
    }

    @Override
    public ParticleStack copyIngredient(ParticleStack ingredient) {
        return ingredient;
    }

    @Override
    public String getErrorInfo(@Nullable ParticleStack ingredient) {
        return ingredient == null ? "null" : ingredient.toString();
    }
}
