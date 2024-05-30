package igentuman.nc.setup;

import com.mojang.serialization.Codec;
import igentuman.nc.effect.RadiationResistance;
import igentuman.nc.multiblock.fusion.FusionReactor;
import igentuman.nc.multiblock.turbine.TurbineRegistration;
import igentuman.nc.multiblock.fission.FissionReactor;
import igentuman.nc.recipes.NcRecipeSerializers;
import igentuman.nc.recipes.NcRecipeType;
import igentuman.nc.setup.registration.*;
import igentuman.nc.world.structure.LaboratoryStructure;
import igentuman.nc.world.structure.PortalStructure;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.setup.registration.Registries.*;

public class Registration {

    public static final RegistryObject<MobEffect> RADIATION_RESISTANCE = EFFECTS.register("radiation_resistance", () -> new RadiationResistance(MobEffectCategory.BENEFICIAL, 0xd4ffFF));

    public static void init() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        Registries.init();
        NCBlocks.init();
        NCStorageBlocks.init();
        NCItems.init();
        FissionFuel.init();
        NCFluids.init();
        NCEnergyBlocks.init();
        NCProcessors.init();
        FissionReactor.init();
        FusionReactor.init();
        TurbineRegistration.init();
        CreativeTabs.init();
        NcRecipeSerializers.init();
        NcRecipeType.init();
        NcParticleTypes.init();
        NCSounds.init();
        //initOreGeneration();
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if(event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
           // event.accept(ModItems.SAPPHIRE);
        }
    }

    private static void initOreGeneration() {
       // ORE_GENERATION = registerOreGenerators();
    }

    public static final BlockBehaviour.Properties BLOCK_PROPERTIES = BlockBehaviour.Properties.of().sound(SoundType.METAL).strength(2f).requiresCorrectToolForDrops();

    //public static final TagKey<StructureSet> WASTELAND_DIMENSION_STRUCTURE_SET = TagKey.create(Registries.STRUCTURE_SET, NuclearcraftChunkGenerator.);

    public static final RegistryObject<StructureType<?>> PORTAL = STRUCTURES.register("portal", () -> typeConvert(PortalStructure.CODEC));

    public static final RegistryObject<StructureType<?>> LABORATORY = STRUCTURES.register("nc_laboratory", () -> typeConvert(LaboratoryStructure.CODEC));

    // Some common properties for our blocks and items
    public static final Item.Properties ITEM_PROPERTIES = new Item.Properties();

   // public static List<RegistryObject<PlacedFeature>> ORE_GENERATION = registerOreGenerators();
   // public static final TagKey<Biome> HAS_PORTAL = TagKey.create(Registries.BIOME, new ResourceLocation(MODID, "has_structure/portal"));
  //  public static final TagKey<Biome> HAS_THIEFDEN = TagKey.create(Registries.BIOME, new ResourceLocation(MODID, "has_structure/nc_laboratory"));

    private static List<RegistryObject<PlacedFeature>> registerOreGenerators()
    {
        List<RegistryObject<PlacedFeature>> temp = new ArrayList<>();
        for(String ore: NCBlocks.ORE_BLOCKS.keySet()) {
            //temp.add(PLACED_FEATURES.register("nc_ores_"+ore, () -> Generator.createOregen(ore)));
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
