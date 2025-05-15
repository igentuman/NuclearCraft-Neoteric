package igentuman.nc.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.util.math.FloatingLong;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static igentuman.nc.NuclearCraft.debugLog;
import static igentuman.nc.handler.config.MaterialsConfig.MATERIAL_PRODUCTS;
import static igentuman.nc.multiblock.fission.FissionReactorRegistration.FISSION_BLOCKS;
import static igentuman.nc.multiblock.fusion.FusionReactorRegistration.FUSION_BLOCKS;
import static igentuman.nc.multiblock.turbine.TurbineRegistration.TURBINE_BLOCKS;
import static igentuman.nc.setup.registration.NCBlocks.*;
import static igentuman.nc.setup.registration.NCEnergyBlocks.ENERGY_BLOCKS;
import static igentuman.nc.setup.registration.NCItems.*;
import static igentuman.nc.setup.registration.NCProcessors.PROCESSORS;

public final class NcUtils {

    public static final float ONE_OVER_ROOT_TWO = (float) (1 / Math.sqrt(2));

    private static final List<UUID> warnedFails = new ArrayList<>();

    public static ResourceLocation getName(ParticleType<?> element) {
        return getName(ForgeRegistries.PARTICLE_TYPES, element);
    }

    public static ResourceLocation rlFromString(String name) {
        return ResourceLocation.tryParse(name);
    }

    public static ResourceLocation getName(Item element) {
        return getName(ForgeRegistries.ITEMS, element);
    }

    public static ResourceLocation getName(Fluid element) {
        return getName(ForgeRegistries.FLUIDS, element);
    }

    private static <T> ResourceLocation getName(IForgeRegistry<T> registry, T element) {
        return registry.getKey(element);
    }
    public static String getPath(Item element) {
        return getName(element).getPath();
    }

    public static ResourceLocation getName(Block element) {
        return getName(ForgeRegistries.BLOCKS, element);
    }

    public static String getNamespace(Block element) {
        return getName(element).getNamespace();
    }

    public static ResourceLocation getName(MenuType<?> element) {
        return getName(ForgeRegistries.MENU_TYPES, element);
    }
    /**
     * Gets the creator's modid if it exists, or falls back to the registry name.
     *
     * @implNote While the default implementation of getCreatorModId falls back to the registry name, it is possible someone is overriding this and not falling back.
     */
    @NotNull
    public static String getModId(@NotNull ItemStack stack) {
        try {
            String mod = stack.getItemHolder().unwrap().left().get().location().getNamespace();
            if (mod != null && !mod.isEmpty()) {
                return mod;
            }
        } catch (Exception ignored) {}
        Item item = stack.getItem();
        String modid = item.getCreatorModId(stack);
        if (modid == null) {
            ResourceLocation registryName = getName(item);
            if (registryName == null) {
                debugLog("Unexpected null registry name for item of class type: {}" + item.getClass().getSimpleName());
                return "";
            }
            return registryName.getNamespace();
        }
        return modid;
    }

    public static ItemStack getItemStackByModPriority(ItemStackIngredient item) {
        if(item.getRepresentations().size() == 1) {
            return item.getRepresentations().get(0);
        }
        for(String mod: MATERIAL_PRODUCTS.MODS_PRIORITY.get()) {
            for(ItemStack i: item.getRepresentations()) {
                if(getModId(i).equals(mod)) {
                    return i;
                }
            }
        }
        return item.getRepresentations().get(0);
    }

    @NotNull
    public static String getModId(@NotNull FluidStack stack) {
        Fluid fluid = stack.getFluid();
        String modid = "";
        try {
            modid = ForgeRegistries.FLUIDS.getKey(fluid).getNamespace();
        } catch (Exception e) {
            //todo find workaround
            return "";
        }
        return modid;

    }

    public static ItemStack getItemInHand(LivingEntity entity, HumanoidArm side) {
        if (entity instanceof Player player) {
            return getItemInHand(player, side);
        } else if (side == HumanoidArm.RIGHT) {
            return entity.getMainHandItem();
        }
        return entity.getOffhandItem();
    }

    public static ItemStack getItemInHand(Player player, HumanoidArm side) {
        if (player.getMainArm() == side) {
            return player.getMainHandItem();
        }
        return player.getOffhandItem();
    }

    public static int redstoneLevelFromContents(long amount, long capacity) {
        double fractionFull = capacity == 0 ? 0 : amount / (double) capacity;
        return Mth.floor((float) (fractionFull * 14.0F)) + (fractionFull > 0 ? 1 : 0);
    }

    /**
     * Calculates the redstone level based on the percentage of amount stored.
     *
     * @param amount   Amount currently stored
     * @param capacity Total amount that can be stored.
     *
     * @return A redstone level based on the percentage of the amount stored.
     */
    public static int redstoneLevelFromContents(FloatingLong amount, FloatingLong capacity) {
        if (capacity.isZero() || amount.isZero()) {
            return 0;
        }
        return 1 + amount.divide(capacity).multiply(14).intValue();
    }


    /**
     * Checks whether the player is in creative or spectator mode.
     *
     * @param player the player to check.
     *
     * @return true if the player is neither in creative mode, nor in spectator mode.
     */
    public static boolean isPlayingMode(Player player) {
        return !player.isCreative() && !player.isSpectator();
    }

    /**
     * Helper to read the parameter names from the format saved by our annotation processor param name mapper.
     */
    public static List<String> getParameterNames(@Nullable JsonObject classMethods, String method, String signature) {
        if (classMethods != null) {
            JsonObject signatures = classMethods.getAsJsonObject(method);
            if (signatures != null) {
                JsonElement params = signatures.get(signature);
                if (params != null) {
                    if (params.isJsonArray()) {
                        JsonArray paramArray = params.getAsJsonArray();
                        List<String> paramNames = new ArrayList<>(paramArray.size());
                        for (JsonElement param : paramArray) {
                            paramNames.add(param.getAsString());
                        }
                        return Collections.unmodifiableList(paramNames);
                    }
                    return Collections.singletonList(params.getAsString());
                }
            }
        }
        return Collections.emptyList();
    }
    public static List<HashMap<String, RegistryObject<Item>>> ALL_ITEMS = List.of(
            NC_ITEMS,
            NC_PARTS,
            NC_GEMS,
            NC_INGOTS,
            NC_DUSTS,
            NC_NUGGETS,
            ALL_NC_ITEMS
    );
    public static List<HashMap<String, RegistryObject<Block>>> ALL_BLOCKS = List.of(
            NC_BLOCKS,
            FISSION_BLOCKS,
            FUSION_BLOCKS,
            PROCESSORS,
            ENERGY_BLOCKS,
            ORE_BLOCKS,
            TURBINE_BLOCKS
    );
    public static Block getNCBlock(String name)
    {
        for(HashMap<String, RegistryObject<Block>> map: ALL_BLOCKS) {
            if(map.containsKey(name)) {
                return map.get(name).get();
            }
        }

        return Blocks.AIR;
    }

    public static Item getNCItem(String name)
    {
        for(HashMap<String, RegistryObject<Item>> map: ALL_ITEMS) {
            if(map.containsKey(name)) {
                return map.get(name).get();
            }
        }

        return Items.AIR;
    }
}