package igentuman.nc.setup;

import igentuman.nc.api.impl.DeterminedMultiblockValidator;
import igentuman.nc.api.multiblock.BlockPredicate;
import igentuman.nc.multiblock.MultiblockEntry;
import igentuman.nc.multiblock.MultiblockEntryBuilder;
import igentuman.nc.registration.ArmorMaterialEntry;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.util.SlotsLayout;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.awt.*;
import java.util.HashMap;

import static igentuman.nc.registration.ModEntryBuilder.*;

public class ModEntries {
    public static final HashMap<String, ModEntry> ENTRIES = new HashMap<>();
    public static BlockBehaviour.Properties COMMON_BLOCK_PROPS = BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5f).requiresCorrectToolForDrops();

    public static void init() {}

    public static ModEntry get(String name) {
        return ENTRIES.getOrDefault(name, null);
    }

    public static boolean isEnabled(String name) {
        ModEntry entry = get(name);
        return entry == null || entry.isEnabled();
    }
}
