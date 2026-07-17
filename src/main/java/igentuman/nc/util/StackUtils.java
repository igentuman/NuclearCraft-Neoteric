package igentuman.nc.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AirItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static igentuman.nc.handler.config.MaterialsConfig.MATERIAL_PRODUCTS;
import static igentuman.nc.setup.registration.NCItems.MULTITOOL;
import static igentuman.nc.setup.registration.Registries.ITEM_REGISTRY;
import static igentuman.nc.util.NcUtils.getModId;
import static igentuman.nc.util.NcUtils.rlFromString;

public final class StackUtils {

    private StackUtils() {
    }

    public static ItemStack size(ItemStack stack, int size) {
        if (size <= 0 || stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return ItemHandlerHelper.copyStackWithSize(stack, size);
    }

    public static ItemStack resolveStackByModPriority(ItemStack[] items) {
        for(String mod: MATERIAL_PRODUCTS.MODS_PRIORITY.get()) {
            for(ItemStack item: items) {
                if(getModId(item).equals(mod)) {
                    return item;
                }
            }
        }
        return items[0];
    }

    @Nullable
    public static BlockState getStateForPlacement(ItemStack stack, BlockPos pos, Player player) {
        return Block.byItem(stack.getItem()).getStateForPlacement(new BlockPlaceContext(new UseOnContext(player, InteractionHand.MAIN_HAND,
              new BlockHitResult(Vec3.ZERO, Direction.UP, pos, false))));
    }

    public static List<String> getItemsByTagKey(String key)
    {
        List<String> tmp = new ArrayList<>();
        TagKey<Item> tag = TagKey.create(ITEM_REGISTRY, rlFromString(key));
        Ingredient ing = Ingredient.fromValues(Stream.of(new Ingredient.TagValue(tag)));
        for (ItemStack item: ing.getItems()) {
            tmp.add(item.getItem().toString());
        }
        return tmp;
    }


    public static Item getItemByRegistryName(String id) {
        return ForgeRegistries.ITEMS.getValue(rlFromString(id));
    }

    private static final List<Item> allowedTools = new ArrayList<>();

    public static boolean isMultiTool(ItemStack stack) {
        if(allowedTools.isEmpty()) {
            allowedTools.add(MULTITOOL.get());
            Item wrench = getItemByRegistryName("rftoolsbase:smartwrench");
            Item configurator = getItemByRegistryName("mekanism:configurator");
            Item thermal = getItemByRegistryName("thermalfoundation:wrench");
            Item hammer = getItemByRegistryName("immersiveengineering:hammer");
            Item enderIoWrench = getItemByRegistryName("enderio:item_yeta_wrench");
            if(!(wrench instanceof AirItem)) {
                allowedTools.add(wrench);
            }
            if (!(configurator instanceof AirItem)) {
                allowedTools.add(configurator);
            }
            if (!(thermal instanceof AirItem)) {
                allowedTools.add(thermal);
            }
            if (!(hammer instanceof AirItem)) {
                allowedTools.add(hammer);
            }
            if (!(enderIoWrench instanceof AirItem)) {
                allowedTools.add(enderIoWrench);
            }
        }
        return allowedTools.contains(stack.getItem());
    }
}