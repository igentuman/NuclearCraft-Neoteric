package igentuman.nc.compat;

import igentuman.nc.api.particle.ParticleStack;
import igentuman.nc.particle.ParticleSourceCatalog;
import igentuman.nc.setup.Registers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.NuclearCraft.rl;

public record ParticleSourceRecipe(Identifier id, ItemStack item, FluidStack fluid, ParticleStack output) {

    public static List<ParticleSourceRecipe> all() {
        List<ParticleSourceRecipe> recipes = new ArrayList<>();
        ParticleSourceCatalog.itemSources().forEach((id, data) -> {
            if (!Registers.PARTICLE_DEFINITION_REGISTRY.containsKey(data.particleId())) return;
            BuiltInRegistries.ITEM.getOptional(id).ifPresent(item -> {
                ItemStack stack = new ItemStack(item);
                if (stack.isEmpty()) return;
                var source = ParticleSourceCatalog.sourceData(stack);
                ParticleStack output = source.emit(source.remainingAmount()).stack();
                if (!output.isEmpty()) {
                    recipes.add(new ParticleSourceRecipe(recipeId("item", id), stack, FluidStack.EMPTY, output));
                }
            });
        });
        ParticleSourceCatalog.fluidSources().forEach((id, data) -> {
            if (!Registers.PARTICLE_DEFINITION_REGISTRY.containsKey(data.particleId())) return;
            BuiltInRegistries.FLUID.getOptional(id).ifPresent(fluid -> {
                FluidStack stack = new FluidStack(fluid, 1);
                ParticleStack output = data.emit(data.capacity()).stack();
                if (!stack.isEmpty() && !output.isEmpty()) {
                    recipes.add(new ParticleSourceRecipe(recipeId("fluid", id), ItemStack.EMPTY, stack, output));
                }
            });
        });
        return recipes;
    }

    private static Identifier recipeId(String kind, Identifier source) {
        return rl("particle_source/" + kind + "/" + source.getNamespace() + "/" + source.getPath());
    }
}
