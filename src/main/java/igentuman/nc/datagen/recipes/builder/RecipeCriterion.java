package igentuman.nc.datagen.recipes.builder;

import net.minecraft.advancements.CriterionTriggerInstance;
import org.antlr.v4.runtime.misc.NotNull;;

import java.util.Objects;


public record RecipeCriterion(@NotNull String name, @NotNull CriterionTriggerInstance criterion) {

    public RecipeCriterion {
        Objects.requireNonNull(name, "Criterion must have a name.");
        Objects.requireNonNull(criterion, "Recipe criterion's must have a criterion to match.");
    }
}