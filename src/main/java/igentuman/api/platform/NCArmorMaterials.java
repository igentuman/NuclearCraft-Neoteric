package igentuman.api.platform;

import igentuman.nc.NuclearCraft;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.List;

import static igentuman.nc.setup.registration.NCItems.NC_PARTS;
import static igentuman.nc.setup.registration.NCItems.NC_SHIELDING;
import static igentuman.nc.setup.registration.Tags.forgeIngot;

/**
 * Platform translation layer for armor materials.
 * <p>
 * MC 1.21 changed {@code ArmorMaterial} from an interface to a registry record.
 * {@code ArmorItem} now takes {@code Holder<ArmorMaterial>} instead of the raw
 * material. This class registers NC's three armor materials via DeferredRegister.
 */
public final class NCArmorMaterials {

    private NCArmorMaterials() {}

    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
            DeferredRegister.create(net.minecraft.core.registries.Registries.ARMOR_MATERIAL, NuclearCraft.MODID);

    // Defense values from old enum constructor (slotProtections array):
    // index order was [BOOTS, LEGGINGS, CHESTPLATE, HELMET]

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> HAZMAT = ARMOR_MATERIALS.register(
            "hazmat",
            () -> new ArmorMaterial(
                    Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                        map.put(ArmorItem.Type.BOOTS, 1);
                        map.put(ArmorItem.Type.LEGGINGS, 2);
                        map.put(ArmorItem.Type.CHESTPLATE, 3);
                        map.put(ArmorItem.Type.HELMET, 1);
                        map.put(ArmorItem.Type.BODY, 2);
                    }),
                    15,
                    SoundEvents.ARMOR_EQUIP_LEATHER,
                    () -> Ingredient.of(NC_PARTS.get("bioplastic").get()),
                    List.of(new ArmorMaterial.Layer(
                            ResourceLocation.fromNamespaceAndPath(NuclearCraft.MODID, "hazmat")
                    )),
                    0.0F,
                    0.0F
            )
    );

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> TOUGH = ARMOR_MATERIALS.register(
            "tough",
            () -> new ArmorMaterial(
                    Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                        map.put(ArmorItem.Type.BOOTS, 3);
                        map.put(ArmorItem.Type.LEGGINGS, 6);
                        map.put(ArmorItem.Type.CHESTPLATE, 8);
                        map.put(ArmorItem.Type.HELMET, 3);
                        map.put(ArmorItem.Type.BODY, 5);
                    }),
                    15,
                    SoundEvents.ARMOR_EQUIP_DIAMOND,
                    () -> Ingredient.of(forgeIngot("tough_alloy")),
                    List.of(new ArmorMaterial.Layer(
                            ResourceLocation.fromNamespaceAndPath(NuclearCraft.MODID, "tough")
                    )),
                    3.5F,
                    0.2F
            )
    );

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> HEV = ARMOR_MATERIALS.register(
            "hev",
            () -> new ArmorMaterial(
                    Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                        map.put(ArmorItem.Type.BOOTS, 3);
                        map.put(ArmorItem.Type.LEGGINGS, 5);
                        map.put(ArmorItem.Type.CHESTPLATE, 7);
                        map.put(ArmorItem.Type.HELMET, 3);
                        map.put(ArmorItem.Type.BODY, 4);
                    }),
                    25,
                    SoundEvents.ARMOR_EQUIP_NETHERITE,
                    () -> Ingredient.of(NC_SHIELDING.get("dps").get()),
                    List.of(new ArmorMaterial.Layer(
                            ResourceLocation.fromNamespaceAndPath(NuclearCraft.MODID, "hev")
                    )),
                    4.0F,
                    0.3F
            )
    );

    public static void init(IEventBus bus) {
        ARMOR_MATERIALS.register(bus);
    }
}
