package igentuman.nc.compat.kubejs;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.RecipeValidationContext;
import dev.latvian.mods.kubejs.util.ErrorStack;
import igentuman.nc.testing.MinecraftTestBootstrap;
import net.neoforged.fml.loading.FMLLoader;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ModKubeJSPluginTest {

    private static final RecipeValidationContext VALIDATION =
            new RecipeValidationContext.Impl(null, ErrorStack.NONE);
    private static FMLLoader loader;

    @BeforeAll
    static void bootstrap() throws ReflectiveOperationException {
        loader = MinecraftTestBootstrap.open();
    }

    @AfterAll
    static void closeLoader() {
        if (loader != null) loader.close();
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
    void flatItemInputRejectsAnEntryWithoutAnIngredient() {
        var codec = NcRecipeComponents.FLAT_SIZED_INGREDIENT.codec();
        assertTrue(codec.parse(JsonOps.INSTANCE, JsonParser.parseString("""
                {"count":1}
                """)).isError());
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
