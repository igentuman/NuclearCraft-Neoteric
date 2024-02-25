package igentuman.nc.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import igentuman.nc.NuclearCraft;
import igentuman.nc.util.math.FloatingLong;
import it.unimi.dsi.fastutil.longs.Long2DoubleArrayMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.UsernameCache;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static igentuman.nc.multiblock.fission.FissionReactor.FISSION_BLOCKS;
import static igentuman.nc.multiblock.fission.FissionReactor.FISSION_BLOCK_ITEMS;
import static igentuman.nc.multiblock.fusion.FusionReactor.FUSION_BLOCKS;
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
        Item item = stack.getItem();
        String modid = item.getCreatorModId(stack);
        if (modid == null) {
            ResourceLocation registryName = getName(item);
            if (registryName == null) {
                NuclearCraft.LOGGER.error("Unexpected null registry name for item of class type: {}", item.getClass().getSimpleName());
                return "";
            }
            return registryName.getNamespace();
        }
        return modid;
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