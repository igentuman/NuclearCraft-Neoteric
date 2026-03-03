package igentuman.api.nc;

import igentuman.nc.handler.CatalystHandler;
import igentuman.nc.handler.UpgradesHandler;
import igentuman.nc.handler.sided.capability.ItemCapabilityHandler;
import igentuman.nc.recipes.type.NcRecipe;
import net.minecraft.world.item.Item;

import java.util.List;

public interface Processor {

    boolean hasRedstoneSignal();

    double getProgress();

    void processRecipe();

    boolean hasRecipe();

    NcRecipe getCachedRecipe();

    void updateRecipe();

    void handleRecipeOutput();

    ItemCapabilityHandler getItemInventory();

    String getName();

    int getEnergyCapacity();

    List<Item> getAllowedCatalysts();

    int getBaseProcessTime();

    int getBasePower();

    default CatalystHandler createCatalystHandler() {
        return null;
    }

    default UpgradesHandler createUpgradesHandler() {
        return null;
    }

}
