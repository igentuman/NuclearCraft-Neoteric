package igentuman.nc.handler.event.client;

import igentuman.nc.recipes.NcRecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;


import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.recipes.NcRecipeType.ALL_RECIPES;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ServerLoad {
    public static void register(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.addListener(ServerLoad::onLevelLoad);
    }
    public static boolean initialized = false;
    public static void onLevelLoad(WorldEvent.Load event) {
        if(initialized) return;
        if(event.getWorld().getServer() == null) return;
        Level level = event.getWorld().getServer().getLevel(Level.OVERWORLD);
        for (String name: ALL_RECIPES.keySet()) {
            level.getRecipeManager().getAllRecipesFor(ALL_RECIPES.get(name));
         //   NcRecipeType<?> recipeType = (NcRecipeType<?>) ALL_RECIPES.get(name);
          //  recipeType.loadRecipes(level);
        }
        initialized = true;
    }
}
