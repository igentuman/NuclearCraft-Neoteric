package igentuman.nc.handler.event.client;

import igentuman.nc.recipes.NcRecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;


import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.recipes.NcRecipeType.ALL_RECIPES;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ServerLoad {
    public static void register(FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.addListener(ServerLoad::onLevelLoad);
    }
    public static boolean initialized = false;
    public static void onLevelLoad(LevelEvent.Load event) {
        if(initialized) return;
        if(event.getLevel().getServer() == null) return;
        Level level = event.getLevel().getServer().getLevel(Level.OVERWORLD);
        for (String name: ALL_RECIPES.keySet()) {
            NcRecipeType<?> recipeType = ALL_RECIPES.get(name).getRecipeType();
            recipeType.loadRecipes(level);
        }
        initialized = true;
    }
}
