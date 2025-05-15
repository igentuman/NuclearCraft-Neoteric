package igentuman.nc.compat.jei.ingredient;

import com.google.common.base.MoreObjects;
import igentuman.nc.content.particles.Particle;
import igentuman.nc.content.particles.ParticleStack;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

import static igentuman.nc.NuclearCraft.rl;

/**
 * source https://github.com/Lach01298/QMD
 */
public class ParticleStackHelper implements IIngredientHelper<ParticleStack>
{

	@Override
	public IIngredientType<ParticleStack> getIngredientType() {
		return ParticleType.Particle;
	}

	@Override
	public String getDisplayName(ParticleStack ingredient)
	{
		return ingredient.getParticle().getUnlocalizedName();
	}


	@Override
	public String getUniqueId(ParticleStack ingredient, UidContext uidContext)
	{

		return "particle:" + ingredient.getParticle().getName();
	}

	@Override
	public String getWildcardId(ParticleStack ingredient)
	{
		return getUniqueId(ingredient, null);
	}


	@Override
	public ResourceLocation getResourceLocation(ParticleStack particleStack) {
		return rl(particleStack.getParticle().getName());
	}

	@Override
	public ItemStack getCheatItemStack(ParticleStack ingredient)
	{
		return ItemStack.EMPTY;
	}

	@Override
	public ParticleStack copyIngredient(ParticleStack ingredient)
	{
		return ingredient.copy();
	}

	@Override
	public String getErrorInfo(@Nullable ParticleStack ingredient)
	{
		if (ingredient == null)
		{
			return "null";
		}
		MoreObjects.ToStringHelper toStringHelper = MoreObjects.toStringHelper(ParticleStack.class);

		Particle particle = ingredient.getParticle();
		if (particle != null)
		{
			toStringHelper.add("Particle", particle.getName());
		}
		else
		{
			toStringHelper.add("Particle", "null");
		}

		toStringHelper.add("amount", ingredient.getAmount());
		toStringHelper.add("Energy", ingredient.getMeanEnergy());
		toStringHelper.add("Focus", ingredient.getFocus());


		return toStringHelper.toString();
	}
}
