package igentuman.nc.compat.kubejs;

import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.RecipeValidationContext;
import dev.latvian.mods.kubejs.util.ErrorStack;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ModKubeJSPluginTest {

    private static final RecipeValidationContext VALIDATION =
            new RecipeValidationContext.Impl(null, ErrorStack.NONE);

    @BeforeAll
    static void bootstrap() {
        LoadingModList.of(List.of(), List.of(), List.of(), List.of(), Map.of());
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void unusedRecipeCategoriesAcceptExplicitEmptyLists() throws ReflectiveOperationException {
        for (String name : List.of("ITEM_INPUTS", "FLUID_INPUTS", "ITEM_OUTPUTS",
                "FLUID_OUTPUTS", "PARTICLE_OUTPUTS")) {
            RecipeKey<List<?>> key = key(name);
            assertDoesNotThrow(() -> key.component.validate(VALIDATION, List.of()), name);
        }
    }

    @Test
    void emptyListSupportStillRejectsEmptyItemIngredients() throws ReflectiveOperationException {
        RecipeKey<List<?>> key = key("ITEM_INPUTS");
        assertThrows(RuntimeException.class, () -> key.component.validate(VALIDATION,
                List.of(new SizedIngredient(Ingredient.EMPTY, 1))));
    }

    @Test
    void collisionParticleInputsStillRequireAnEntry() throws ReflectiveOperationException {
        RecipeKey<List<?>> key = key("PARTICLE_INPUTS");
        assertThrows(RuntimeException.class, () -> key.component.validate(VALIDATION, List.of()));
    }

    @SuppressWarnings("unchecked")
    private static RecipeKey<List<?>> key(String name) throws ReflectiveOperationException {
        var field = ModKubeJSPlugin.class.getDeclaredField(name);
        field.setAccessible(true);
        return (RecipeKey<List<?>>) field.get(null);
    }
}
