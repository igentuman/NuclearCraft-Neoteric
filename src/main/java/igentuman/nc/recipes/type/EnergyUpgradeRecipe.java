package igentuman.nc.recipes.type;

import igentuman.nc.recipes.NcRecipeSerializers;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_ITEMS;
import static igentuman.nc.multiblock.accelerator.TargetChamberRegistration.TARGET_CHAMBER_ITEMS;
import static igentuman.nc.multiblock.fission.FissionReactorRegistration.FISSION_BLOCK_ITEMS;
import static igentuman.nc.multiblock.fusion.FusionReactorRegistration.FUSION_ITEMS;
import static igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration.KUGELBLITZ_ITEMS;
import static igentuman.nc.multiblock.turbine.TurbineRegistration.TURBINE_BLOCK_ITEMS;
import static igentuman.nc.setup.registration.NCItems.NC_ITEMS;


@NothingNullByDefault
public class EnergyUpgradeRecipe extends CustomRecipe {

    public EnergyUpgradeRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, CraftingBookCategory.MISC);
    }


    @Override
    public boolean matches(CraftingContainer inv, Level world) {
        int upgrades = 0;
        ItemStack block = ItemStack.EMPTY;
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            if(i == 4) {
                block = inv.getItem(i);
                continue;
            }
            if(inv.getItem(i).is(NC_ITEMS.get("upgrade_energy").get())) {
                upgrades++;
            }
        }

        if (upgrades != 8 || block.isEmpty() || !isValidForUpgrade(block)) {
            return false;
        }
        int curTier = block.getOrCreateTag().getInt("upgrade_tier");
        return curTier < 3;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess access) {
        int upgrades = 0;
        ItemStack block = ItemStack.EMPTY;
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            if(i == 4) {
                block = inv.getItem(i);
                continue;
            }
            if(inv.getItem(i).is(NC_ITEMS.get("upgrade_energy").get())) {
                upgrades++;
            }
        }

        if (upgrades != 8 || block.isEmpty() || !isValidForUpgrade(block)) {
            return ItemStack.EMPTY;
        }
        ItemStack result = block.copy();
        result.setCount(1);
        int curTier = block.getOrCreateTag().getInt("upgrade_tier");
        if(curTier < 3) {
            result.getOrCreateTag().putInt("upgrade_tier", curTier + 1);
        }
        return result;
    }

    private boolean isValidForUpgrade(ItemStack block) {
        return block.is(FISSION_BLOCK_ITEMS.get("fission_reactor_controller").get())
            || block.is(FUSION_ITEMS.get("fusion_core").get())
            || block.is(KUGELBLITZ_ITEMS.get("chamber_terminal").get())
            || block.is(TURBINE_BLOCK_ITEMS.get("turbine_controller").get())
            || block.is(ACCELERATOR_ITEMS.get("linear_accelerator_controller").get())
            || block.is(ACCELERATOR_ITEMS.get("thoroidal_accelerator_controller").get())
            || block.is(TARGET_CHAMBER_ITEMS.get("target_chamber_controller").get());
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height == 9;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return NcRecipeSerializers.ENERGY_UPGRADE.get();
    }

    @SubscribeEvent
    public static void onCrafting(PlayerEvent.ItemCraftedEvent event) {
        ItemStack result = event.getCrafting();
    }
}