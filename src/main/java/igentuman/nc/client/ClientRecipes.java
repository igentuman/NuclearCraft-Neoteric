package igentuman.nc.client;

import igentuman.nc.NuclearCraft;
import igentuman.nc.recipe.RecipeSources;
import net.minecraft.world.item.crafting.RecipeMap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;

/** Replaces the logical-client recipe snapshot on join/reload and releases it on disconnect. */
@EventBusSubscriber(modid = NuclearCraft.MODID, value = Dist.CLIENT)
public final class ClientRecipes {
    private ClientRecipes() {}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRecipesReceived(RecipesReceivedEvent event) {
        RecipeSources.replaceClientRecipes(event.getRecipeMap());
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        RecipeSources.replaceClientRecipes(RecipeMap.EMPTY);
    }
}
