package igentuman.nc.handler.event.client;

import igentuman.nc.client.NcClient;
import igentuman.nc.recipes.NcRecipeType;
import net.minecraft.data.worldgen.DimensionTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.Objects;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.recipes.NcRecipeType.ALL_RECIPES;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ServerLoad {
    public static void register(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.addListener(ServerLoad::onLevelLoad);
    }
    public static boolean initialized = false;
    public static void onLevelLoad(LevelEvent.Load event) {
        if(initialized) return;
        if(event.getLevel().getServer() == null) return;
        Level level = event.getLevel().getServer().getLevel(Level.OVERWORLD);
        for (String name: ALL_RECIPES.keySet()) {
            NcRecipeType recipeType = ALL_RECIPES.get(name).getRecipeType();
            recipeType.loadRecipes(level);
        }
        initialized = true;
    }
}
