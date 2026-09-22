package igentuman.nc.recipe.particle;

public abstract class ParticleRecipeSerializer<T> {

    private final Class<T> recipeClass;

    protected ParticleRecipeSerializer(Class<T> recipeClass) {
        this.recipeClass = recipeClass;
    }

    public Class<T> recipeClass() {
        return recipeClass;
    }
}
