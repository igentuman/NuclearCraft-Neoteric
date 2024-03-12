package igentuman.nc.setup;

import com.mojang.serialization.Codec;
import igentuman.nc.effect.RadiationResistance;
import igentuman.nc.multiblock.fusion.FusionReactor;
import igentuman.nc.multiblock.turbine.TurbineRegistration;
import igentuman.nc.recipes.NcRecipeType;
import igentuman.nc.multiblock.fission.FissionReactor;
import igentuman.nc.recipes.NcRecipeSerializers;
import igentuman.nc.setup.registration.*;
import igentuman.nc.world.ore.Generator;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.block.Block;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.potion.Effect;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.recipes.NcRecipeSerializers.RECIPE_SERIALIZERS;
import static igentuman.nc.setup.registration.NCSounds.SOUND_EVENTS;
import static igentuman.nc.setup.registration.NcParticleTypes.PARTICLE_TYPES;

public class Registration {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<TileEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, MODID);
    private static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, MODID);
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, MODID);
    private static final DeferredRegister<Feature<?>> FEATURE_REGISTER = DeferredRegister.create(ForgeRegistries.FEATURES, MODID);
    public static final DeferredRegister<IRecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MODID);
//    public static final DeferredRegister<Effect> EFFECTS = DeferredRegister.create(Registry.EFF., MODID);

  //  public static final RegistryObject<Effect> RADIATION_RESISTANCE = EFFECTS.register("radiation_resistance", () -> new RadiationResistance(MobEffectCategory.BENEFICIAL, 0xd4ffFF));

    public static void init() {

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        PARTICLE_TYPES.register(bus);
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        CONTAINERS.register(bus);
        ENTITIES.register(bus);
        FEATURE_REGISTER.register(bus);
        CONTAINERS.register(bus);
        SERIALIZERS.register(bus);
    //    EFFECTS.register(bus);


        NCBlocks.init();
        NCStorageBlocks.init();
        NCItems.init();
        Fuel.init();
        NCFluids.init();
        NCEnergyBlocks.init();
        NCProcessors.init();
        FissionReactor.init();
        FusionReactor.init();
        TurbineRegistration.init();
        RECIPE_SERIALIZERS.register(bus);
        SOUND_EVENTS.register(bus);
    }


    static void initOreGeneration() {
        ORE_GENERATION = new ArrayList<>();
        for(String ore: NCBlocks.ORE_BLOCKS.keySet()) {
            ORE_GENERATION.add(Generator.createOregen(ore));
        }
    }

    public static final Block.Properties BLOCK_PROPERTIES = Block.Properties.of(Material.METAL).strength(2f).requiresCorrectToolForDrops();

    // Some common properties for our blocks and items
    public static final Item.Properties ITEM_PROPERTIES = new Item.Properties().tab(CreativeTabs.NC_ITEMS);

    public static List<ConfiguredFeature<?, ?>> ORE_GENERATION;

    public static <B extends Block> RegistryObject<Item> fromBlock(RegistryObject<B> block) {
        return ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(), ITEM_PROPERTIES));
    }

}
