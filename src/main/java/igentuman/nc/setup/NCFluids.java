package igentuman.nc.setup;

import com.google.common.collect.ImmutableList;
import igentuman.nc.item.NCBucketItem;
import igentuman.nc.setup.fluid.NCFluid;
import igentuman.nc.setup.fluid.NCFluidBlock;
import igentuman.nc.setup.fuel.FuelManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.setup.ModSetup.ITEM_GROUP;

public class NCFluids {
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, MODID);
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(
            ForgeRegistries.Keys.FLUID_TYPES, MODID
    );
    public static final List<FluidEntry> ALL_ENTRIES = new ArrayList<>();
    public static final Set<NCBlocks.BlockEntry<? extends LiquidBlock>> ALL_FLUID_BLOCKS = new HashSet<>();
    public static HashMap<String, FluidEntry> NC_MATERIALS = new HashMap<>();

    public static void register(IEventBus eventBus) {
        FLUIDS.register(eventBus);
    }

    public static void init() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        FLUIDS.register(bus);
        FLUID_TYPES.register(bus);
        materialFluids();
        gases();
        fuel();
        isotopes();
    }

    private static void materialFluids() {
        for (String name: Materials.fluids().keySet()) {
            NC_MATERIALS.put(name, FluidEntry.makeMoltenLiquid(name, Materials.fluids().get(name).color));
        }
    }

    private static void fuel() {
        for (String name: FuelManager.all().keySet()) {
            for(String subType: FuelManager.all().get(name).keySet()) {
               /* NC_FUEL.put(List.of("fuel", name, subType, ""),
                        ITEMS.register("fuel_"+name+"_"+subType.replace("-","_"),
                                () -> new ItemFuel(ITEM_PROPERTIES, FuelManager.all().get(name).get(subType).getDefault())));
                NC_FUEL.put(List.of("fuel", name, subType, "ox"),
                        ITEMS.register("fuel_"+name+"_"+subType.replace("-","_")+"_ox",
                                () -> new ItemFuel(ITEM_PROPERTIES, FuelManager.all().get(name).get(subType).getOxide())));
                NC_FUEL.put(List.of("fuel", name, subType, "ni"),
                        ITEMS.register("fuel_"+name+"_"+subType.replace("-","_")+"_ni",
                                () -> new ItemFuel(ITEM_PROPERTIES, FuelManager.all().get(name).get(subType).getNitride())));
                NC_FUEL.put(List.of("fuel", name, subType, "za"),
                        ITEMS.register("fuel_"+name+"_"+subType.replace("-","_")+"_za",
                                () -> new ItemFuel(ITEM_PROPERTIES, FuelManager.all().get(name).get(subType).getZirconiumAlloy())));
                NC_FUEL.put(List.of("fuel", name, subType, "tr"),
                        ITEMS.register("fuel_"+name+"_"+subType.replace("-","_")+"_tr",
                                () -> new ItemFuel(ITEM_PROPERTIES, FuelManager.all().get(name).get(subType).getTriso())));

                NC_DEPLETED_FUEL.put(List.of("depleted", name, subType, ""),
                        ITEMS.register("depleted_fuel_"+name+"_"+subType.replace("-","_"),
                                () -> new Item(ITEM_PROPERTIES)));
                NC_DEPLETED_FUEL.put(List.of("depleted", name, subType, "ox"),
                        ITEMS.register("depleted_fuel_"+name+"_"+subType.replace("-","_")+"_ox",
                                () -> new Item(ITEM_PROPERTIES)));
                NC_DEPLETED_FUEL.put(List.of("depleted", name, subType, "ni"),
                        ITEMS.register("depleted_fuel_"+name+"_"+subType.replace("-","_")+"_ni",
                                () -> new Item(ITEM_PROPERTIES)));
                NC_DEPLETED_FUEL.put(List.of("depleted", name, subType, "za"),
                        ITEMS.register("depleted_fuel_"+name+"_"+subType.replace("-","_")+"_za",
                                () -> new Item(ITEM_PROPERTIES)));
                NC_DEPLETED_FUEL.put(List.of("depleted", name, subType, "tr"),
                        ITEMS.register("depleted_fuel_"+name+"_"+subType.replace("-","_")+"_tr",
                                () -> new Item(ITEM_PROPERTIES)));*/
            }
        }
    }


    private static void gases() {
        List<String> items = Arrays.asList(
                "end_of_the_world",
                "hyperspace",
                "money_for_nothing",
                "wanderer"
        );
        for(String name: items) {
           // NC_RECORDS.put(name, ITEMS.register(name, () -> new Item(ITEM_PROPERTIES)));
        }
    }

    private static void isotopes()
    {
        for(String name: Materials.isotopes()) {
            for(String type: new String[]{"", "_za", "_ox","_ni"}) {
                if(NC_MATERIALS.containsKey(name+type)) continue;
                NC_MATERIALS.put(name+type, FluidEntry.makeMoltenLiquid(name.replace("/", "_")+type,0xFF42784C));
            }
        }
    }

    public record FluidEntry(
            RegistryObject<NCFluid> flowing,
            RegistryObject<NCFluid> still,
            NCBlocks.BlockEntry<NCFluidBlock> block,
            RegistryObject<BucketItem> bucket,
            RegistryObject<FluidType> type,
            List<Property<?>> properties
    )
    {
        private static FluidEntry makeMoltenLiquid(String name, int color)
        {
            return make(name, rl("block/material/fluid/molten_still"), rl("block/material/fluid/molten_flow"), color);
        }

        private static FluidEntry make(String name, ResourceLocation stillTex, ResourceLocation flowingTex, int color)
        {
            return make(name, 0, stillTex, flowingTex, color);
        }

        private static FluidEntry make(String name, ResourceLocation stillTex, ResourceLocation flowingTex)
        {
            return make(name, 0, stillTex, flowingTex, 0xFFFFFFFF);
        }

        private static FluidEntry make(
                String name, ResourceLocation stillTex, ResourceLocation flowingTex, Consumer<FluidType.Properties> buildAttributes
        )
        {
            return make(name, 0, stillTex, flowingTex, buildAttributes, 0xFFFFFFFF);
        }

        private static FluidEntry make(String name, int burnTime, ResourceLocation stillTex, ResourceLocation flowingTex, int color)
        {
            return make(name, burnTime, stillTex, flowingTex, null, color);
        }

        private static FluidEntry make(
                String name, int burnTime,
                ResourceLocation stillTex, ResourceLocation flowingTex,
                @Nullable Consumer<FluidType.Properties> buildAttributes,
                int color
        )
        {
            return make(
                    name, burnTime, stillTex, flowingTex, NCFluid::new, NCFluid.Flowing::new, buildAttributes,
                    ImmutableList.of(), color
            );
        }

        private static FluidEntry make(
                String name, ResourceLocation stillTex, ResourceLocation flowingTex,
                Function<FluidEntry, ? extends NCFluid> makeStill, Function<FluidEntry, ? extends NCFluid> makeFlowing,
                @Nullable Consumer<FluidType.Properties> buildAttributes, ImmutableList<Property<?>> properties,
                int color
        )
        {
            return make(name, 0, stillTex, flowingTex, makeStill, makeFlowing, buildAttributes, properties, color);
        }

        private static FluidEntry make(
                String name, int burnTime,
                ResourceLocation stillTex, ResourceLocation flowingTex,
                Function<FluidEntry, ? extends NCFluid> makeStill, Function<FluidEntry, ? extends NCFluid> makeFlowing,
                @Nullable Consumer<FluidType.Properties> buildAttributes, List<Property<?>> properties, int color
        )
        {
            FluidType.Properties builder = FluidType.Properties.create();
            if(buildAttributes!=null)
                buildAttributes.accept(builder);
            RegistryObject<FluidType> type;
            if(color == 0xFFFFFFFF) {
                type = FLUID_TYPES.register(
                        name, () -> makeTypeWithTextures(builder, stillTex, flowingTex)
                );
            } else {
                type = FLUID_TYPES.register(
                        name, () -> makeColoredTypeWithTextures(builder, stillTex, flowingTex, color)
                );
            }
            Mutable<FluidEntry> thisMutable = new MutableObject<>();
            RegistryObject<NCFluid> still = FLUIDS.register(name, () -> NCFluid.makeFluid(
                    makeStill, thisMutable.getValue()
            ));
            RegistryObject<NCFluid> flowing = FLUIDS.register(name+"_flowing", () -> NCFluid.makeFluid(
                    makeFlowing, thisMutable.getValue()
            ));
            NCBlocks.BlockEntry<NCFluidBlock> block = new NCBlocks.BlockEntry<>(
                    name+"_fluid_block",
                    () -> BlockBehaviour.Properties.copy(Blocks.WATER),
                    p -> new NCFluidBlock(thisMutable.getValue(), p)
            );
            RegistryObject<BucketItem> bucket = NCItems.ITEMS.register(name+"_bucket", () -> makeBucket(still, burnTime));
            FluidEntry entry = new FluidEntry(flowing, still, block, bucket, type, properties);
            thisMutable.setValue(entry);
            ALL_FLUID_BLOCKS.add(block);
            ALL_ENTRIES.add(entry);
            return entry;
        }

        private static FluidType makeColoredTypeWithTextures(
                FluidType.Properties builder, ResourceLocation stillTex, ResourceLocation flowingTex, int color
        )
        {
            return new FluidType(builder)
            {
                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer)
                {
                    consumer.accept(new IClientFluidTypeExtensions()
                    {
                        @Override
                        public ResourceLocation getStillTexture()
                        {
                            return stillTex;
                        }

                        @Override
                        public ResourceLocation getFlowingTexture()
                        {
                            return flowingTex;
                        }
                        @Override
                        public int getTintColor()
                        {
                            return color;
                        }
                    });
                }
            };
        }

        private static FluidType makeTypeWithTextures(
                FluidType.Properties builder, ResourceLocation stillTex, ResourceLocation flowingTex
        )
        {
            return new FluidType(builder)
            {
                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer)
                {
                    consumer.accept(new IClientFluidTypeExtensions()
                    {
                        @Override
                        public ResourceLocation getStillTexture()
                        {
                            return stillTex;
                        }

                        @Override
                        public ResourceLocation getFlowingTexture()
                        {
                            return flowingTex;
                        }
                    });
                }
            };
        }

        public NCFluid getFlowing()
        {
            return flowing.get();
        }

        public NCFluid getStill()
        {
            return still.get();
        }

        public NCFluidBlock getBlock()
        {
            return block.get();
        }

        public BucketItem getBucket()
        {
            return bucket.get();
        }

        private static BucketItem makeBucket(RegistryObject<NCFluid> still, int burnTime)
        {
            return new NCBucketItem(
                    still, new Item.Properties()
                    .stacksTo(1)
                    .tab(ITEM_GROUP)
                    .craftRemainder(Items.BUCKET))
            {
                @Override
                public int getBurnTime(ItemStack itemStack, RecipeType<?> type)
                {
                    return burnTime;
                }
            };
        }

        public RegistryObject<NCFluid> getStillGetter()
        {
            return still;
        }
    }
}
