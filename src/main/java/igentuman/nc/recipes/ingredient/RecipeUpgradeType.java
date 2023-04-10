package igentuman.nc.recipes.ingredient;

public enum RecipeUpgradeType {
    ENERGY,
    FLUID,
    GAS,
    ITEM,
    LOCK,//Note: Must be somewhere below item to ensure item gets ran first
    SORTING,
    UPGRADE;
}