package igentuman.nc.setup;

import com.mojang.serialization.Codec;
import igentuman.nc.effect.RadiationResistance;
import igentuman.nc.recipes.NcRecipeType;
import igentuman.nc.setup.multiblocks.FissionReactor;
import igentuman.nc.setup.recipes.NcRecipeSerializers;
import igentuman.nc.setup.registration.*;
import igentuman.nc.world.dimension.WastelandChunkGenerator;
import igentuman.nc.world.ore.Generator;
import igentuman.nc.world.structure.LaboratoryStructure;
import igentuman.nc.world.structure.PortalStructure;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;

public class Registration {

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    private static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);
    private static final DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIERS = DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, MODID);
    private static final DeferredRegister<StructureType<?>> STRUCTURES = DeferredRegister.create(Registry.STRUCTURE_TYPE_REGISTRY, MODID);
    private static final DeferredRegister<PlacedFeature> PLACED_FEATURES = DeferredRegister.create(Registry.PLACED_FEATURE_REGISTRY, MODID);
    private static final DeferredRegister<Feature<?>> FEATURE_REGISTER = DeferredRegister.create(ForgeRegistries.FEATURES, MODID);
    private static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, MODID);
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MODID);
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, MODID);

    public static final RegistryObject<MobEffect> RADIATION_RESISTANCE = EFFECTS.register("radiation_resistance", () -> new RadiationResistance(MobEffectCategory.BENEFICIAL, 0xd4ffFF));

    public static void init() {

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        CONTAINERS.register(bus);
        ENTITIES.register(bus);
        STRUCTURES.register(bus);
        BIOME_MODIFIERS.register(bus);
        PLACED_FEATURES.register(bus);
        FEATURE_REGISTER.register(bus);
        CONTAINERS.register(bus);
        SERIALIZERS.register(bus);
        RECIPE_TYPES.register(bus);
        EFFECTS.register(bus);

        NcParticleTypes.PARTICLE_TYPES.register(bus);

        NCBlocks.init();
        NCItems.init();
        NCTools.init();
        NCArmor.init();
        Fuel.init();
        NCFluids.init();
        NCEnergyBlocks.init();
        NCProcessors.init();
        FissionReactor.init();
        initOreGeneration();

        NcRecipeSerializers.RECIPE_SERIALIZERS.register(bus);
        NcRecipeType.RECIPE_TYPES.register(bus);
        NCSounds.SOUND_EVENTS.register(bus);
    }

    private static void initOreGeneration() {
        ORE_GENERATION = registerOreGenerators();
    }

    public static final BlockBehaviour.Properties BLOCK_PROPERTIES = BlockBehaviour.Properties.of(Material.METAL).strength(2f).requiresCorrectToolForDrops();

    public static final TagKey<StructureSet> WASTELAND_DIMENSION_STRUCTURE_SET = TagKey.create(Registry.STRUCTURE_SET_REGISTRY, WastelandChunkGenerator.RL_WASTELAND_DIMENSION_SET);

    public static final RegistryObject<StructureType<?>> PORTAL = STRUCTURES.register("portal", () -> typeConvert(PortalStructure.CODEC));

    public static final RegistryObject<StructureType<?>> LABORATORY = STRUCTURES.register("nc_laboratory", () -> typeConvert(LaboratoryStructure.CODEC));

    // Some common properties for our blocks and items
    public static final Item.Properties ITEM_PROPERTIES = new Item.Properties().tab(CreativeTabs.NC_ITEMS);

    public static List<RegistryObject<PlacedFeature>> ORE_GENERATION = registerOreGenerators();
    public static final TagKey<Biome> HAS_PORTAL = TagKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(MODID, "has_structure/portal"));
    public static final TagKey<Biome> HAS_THIEFDEN = TagKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(MODID, "has_structure/nc_laboratory"));

    private static List<RegistryObject<PlacedFeature>> registerOreGenerators()
    {
        List<RegistryObject<PlacedFeature>> temp = new ArrayList<>();
        for(String ore: NCBlocks.ORE_BLOCKS.keySet()) {
            temp.add(PLACED_FEATURES.register("nc_ores_"+ore, () -> Generator.createOregen(ore)));
        }
        return temp;
    }

    public static <B extends Block> RegistryObject<Item> fromBlock(RegistryObject<B> block) {
        return ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(), ITEM_PROPERTIES));
    }

    private static <S extends Structure> StructureType<S> typeConvert(Codec<S> codec) {
        return () -> codec;
    }
}
