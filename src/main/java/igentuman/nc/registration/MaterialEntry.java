package igentuman.nc.registration;

import igentuman.nc.block.NCFluidBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.function.Supplier;
import java.util.function.Function;

import static igentuman.nc.setup.Registers.*;

/** Fluent builder registering a material's product forms (ore, block, ingot, gem, dust, plate, nugget, raw ore, fluid). */
public class MaterialEntry {

    public final int color;
    public final String name;

    private Function<BlockBehaviour.Properties, ? extends Block> oreBlockFactory;
    private Supplier<BlockBehaviour.Properties> oreBlockProperties;
    private Function<BlockBehaviour.Properties, ? extends Block> deepslateOreBlockFactory;
    private Supplier<BlockBehaviour.Properties> deepslateOreBlockProperties;
    private Function<BlockBehaviour.Properties, ? extends Block> storageBlockFactory;
    private Supplier<BlockBehaviour.Properties> storageBlockProperties;
    private Function<Item.Properties, ? extends Item> ingotSupplier;
    private Function<Item.Properties, ? extends Item> gemSupplier;
    private Function<Item.Properties, ? extends Item> rawOreSupplier;
    private Function<Item.Properties, ? extends Item> dustSupplier;
    private Function<Item.Properties, ? extends Item> plateSupplier;
    private Function<Item.Properties, ? extends Item> nuggetSupplier;

    public FluidDefinition fluidDefinition = null;

    public int worldgenMinHeight = 0;
    public int worldgenMaxHeight = 0;
    public int worldgenQty = 0;
    public int worldgenVeinsPerChunk = 4;

    private RegisteredEntry entry;

    public MaterialEntry(String name, int color) {
        this.name = name;
        this.color = color;
    }

    private MaterialEntry(int color, String name) {
        this.color = color;
        this.name = name;
    }

    public static MaterialEntry of(int color, String name) {
        return new MaterialEntry(color, name);
    }

    public MaterialEntry setIngotSupplier(Function<Item.Properties, ? extends Item> ingotSupplier) {
        this.ingotSupplier = ingotSupplier;
        return this;
    }

    public MaterialEntry setGemSupplier(Function<Item.Properties, ? extends Item> gemSupplier) {
        this.gemSupplier = gemSupplier;
        return this;
    }

    public MaterialEntry setBlock(Function<BlockBehaviour.Properties, ? extends Block> factory,
                                     Supplier<BlockBehaviour.Properties> properties) {
        this.storageBlockFactory = factory;
        this.storageBlockProperties = properties;
        return this;
    }

    public MaterialEntry setOre(Function<BlockBehaviour.Properties, ? extends Block> factory,
                                     Supplier<BlockBehaviour.Properties> properties) {
        this.oreBlockFactory = factory;
        this.oreBlockProperties = properties;
        return this;
    }

    public MaterialEntry setDeepslateOre(Function<BlockBehaviour.Properties, ? extends Block> factory,
                                     Supplier<BlockBehaviour.Properties> properties) {
        this.deepslateOreBlockFactory = factory;
        this.deepslateOreBlockProperties = properties;
        return this;
    }

    public MaterialEntry setDustSupplier(Function<Item.Properties, ? extends Item> dustSupplier) {
        this.dustSupplier = dustSupplier;
        return this;
    }

    public MaterialEntry setNuggetSupplier(Function<Item.Properties, ? extends Item> nuggetSupplier) {
        this.nuggetSupplier = nuggetSupplier;
        return this;
    }

    public MaterialEntry setRawOreSupplier(Function<Item.Properties, ? extends Item> rawOreSupplier) {
        this.rawOreSupplier = rawOreSupplier;
        return this;
    }

    public MaterialEntry setPlateSupplier(Function<Item.Properties, ? extends Item> plateSupplier) {
        this.plateSupplier = plateSupplier;
        return this;
    }

    public MaterialEntry setFluidDefinition(FluidDefinition fluidDefinition) {
        this.fluidDefinition = fluidDefinition;
        return this;
    }

    public MaterialEntry noIngot() { this.ingotSupplier = null; return this; }
    public MaterialEntry noGem() { this.gemSupplier = null; return this; }
    public MaterialEntry noBlock() { this.storageBlockFactory = null; return this; }
    public MaterialEntry noOre() { this.oreBlockFactory = null; return this; }
    public MaterialEntry noDeepslateOre() { this.deepslateOreBlockFactory = null; return this; }
    public MaterialEntry noDust() { this.dustSupplier = null; return this; }
    public MaterialEntry noNugget() { this.nuggetSupplier = null; return this; }
    public MaterialEntry noRawOre() { this.rawOreSupplier = null; return this; }
    public MaterialEntry noPlate() { this.plateSupplier = null; return this; }
    public MaterialEntry noFluid() { this.fluidDefinition = null; return this; }

    public boolean hasIngot() { return ingotSupplier != null; }
    public boolean hasGem() { return gemSupplier != null; }
    public boolean hasBlock() { return storageBlockFactory != null; }
    public boolean hasOre() { return oreBlockFactory != null; }
    public boolean hasDeepslateOre() { return deepslateOreBlockFactory != null; }
    public boolean hasDust() { return dustSupplier != null; }
    public boolean hasNugget() { return nuggetSupplier != null; }
    public boolean hasRawOre() { return rawOreSupplier != null; }
    public boolean hasPlate() { return plateSupplier != null; }
    public boolean hasFluid() { return fluidDefinition != null; }
    public boolean hasWorldgenConfig() { return worldgenQty > 0; }

    public MaterialEntry worldgenConfig(int minHeight, int maxHeight, int qty) {
        this.worldgenMinHeight = minHeight;
        this.worldgenMaxHeight = maxHeight;
        this.worldgenQty = qty;
        return this;
    }

    public MaterialEntry worldgenConfig(int veinsPerChunk, int veinSize) {
        this.worldgenVeinsPerChunk = veinsPerChunk;
        this.worldgenQty = veinSize;
        return this;
    }

    public DeferredBlock<Block> oreBlock() { return entry.oreBlock(); }
    public DeferredBlock<Block> deepslateOreBlock() { return entry.deepslateOreBlock(); }
    public DeferredBlock<Block> storageBlock() { return entry.storageBlock(); }
    public DeferredItem<Item> ingot() { return entry.ingot(); }
    public DeferredItem<Item> gem() { return entry.gem(); }
    public DeferredItem<Item> rawOre() { return entry.rawOre(); }
    public DeferredItem<BlockItem> oreItem() { return entry.oreItem(); }
    public DeferredItem<BlockItem> deepslateOreItem() { return entry.deepslateOreItem(); }
    public DeferredItem<BlockItem> storageItem() { return entry.storageItem(); }
    public DeferredItem<Item> dust() { return entry.dust(); }
    public DeferredItem<Item> plate() { return entry.plate(); }
    public DeferredItem<Item> nugget() { return entry.nugget(); }
    public DeferredItem<Item> bucket() { return entry.bucket(); }
    public MaterialFluid materialFluid() { return entry.materialFluid(); }

    public MaterialEntry metalOre() {
        BlockBehaviour.Properties oreProps = BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(3.0f, 3.0f)
                .requiresCorrectToolForDrops()
                .sound(SoundType.STONE);
        BlockBehaviour.Properties blockProps = BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0f, 6.0f)
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL);

        this.setOre(Block::new, () -> oreProps);

        this.setIngotSupplier(Item::new);
        this.setDustSupplier(Item::new);
        this.setPlateSupplier(Item::new);
        this.setNuggetSupplier(Item::new);
        this.setRawOreSupplier(Item::new);

        this.setBlock(Block::new, () -> blockProps);

        // Molten metal fluid by default
        this.setFluidDefinition(FluidDefinition.metal());
        this.worldgenConfig(-60, 60, 9);

        return this;
    }

    public MaterialEntry metalOreWithDeepslate() {
        metalOre();
        BlockBehaviour.Properties deepslateOreProps = BlockBehaviour.Properties.of()
                .mapColor(MapColor.DEEPSLATE)
                .strength(4.5f, 3.0f)
                .requiresCorrectToolForDrops()
                .sound(SoundType.DEEPSLATE);
        this.setDeepslateOre(Block::new, () -> deepslateOreProps);
        return this;
    }

    public MaterialEntry deepslateOnlyMetalOre() {
        metalOre();
        this.noOre();
        BlockBehaviour.Properties deepslateOreProps = BlockBehaviour.Properties.of()
                .mapColor(MapColor.DEEPSLATE)
                .strength(4.5f, 3.0f)
                .requiresCorrectToolForDrops()
                .sound(SoundType.DEEPSLATE);
        this.setDeepslateOre(Block::new, () -> deepslateOreProps);
        this.worldgenConfig(-60, 0, 9);
        return this;
    }

    public MaterialEntry crystalOre() {
        BlockBehaviour.Properties oreProps = BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(3.0f, 3.0f)
                .requiresCorrectToolForDrops()
                .sound(SoundType.STONE);
        BlockBehaviour.Properties blockProps = BlockBehaviour.Properties.of()
                .mapColor(MapColor.DIAMOND)
                .strength(5.0f, 6.0f)
                .requiresCorrectToolForDrops()
                .sound(SoundType.AMETHYST_CLUSTER);

        this.setOre(Block::new, () -> oreProps);

        this.setGemSupplier(Item::new);
        this.setDustSupplier(Item::new);
        this.setPlateSupplier(Item::new);
        this.setNuggetSupplier(Item::new);
        this.setRawOreSupplier(Item::new);

        this.setBlock(Block::new, () -> blockProps);

        return this;
    }

    @SuppressWarnings("unchecked")
    public MaterialEntry build() {
        DeferredBlock<Block> regOreBlock = null;
        DeferredItem<BlockItem> regOreItem = null;
        DeferredBlock<Block> regDeepslateOreBlock = null;
        DeferredItem<BlockItem> regDeepslateOreItem = null;
        DeferredBlock<Block> regStorageBlock = null;
        DeferredItem<BlockItem> regStorageItem = null;
        DeferredItem<Item> regIngot = null;
        DeferredItem<Item> regGem = null;
        DeferredItem<Item> regRawOre = null;
        DeferredItem<Item> regDust = null;
        DeferredItem<Item> regPlate = null;
        DeferredItem<Item> regNugget = null;
        DeferredItem<Item> regBucket = null;
        MaterialFluid regMaterialFluid = null;

        if (oreBlockFactory != null) {
            regOreBlock = BLOCKS.registerBlock(name + "_ore", oreBlockFactory, oreBlockProperties);
            final DeferredBlock<Block> finalOreBlock = regOreBlock;
            regOreItem = ITEMS.registerSimpleBlockItem(name + "_ore", finalOreBlock);
        }
        if (deepslateOreBlockFactory != null) {
            regDeepslateOreBlock = BLOCKS.registerBlock(name + "_deepslate_ore", deepslateOreBlockFactory, deepslateOreBlockProperties);
            final DeferredBlock<Block> finalDeepslateOreBlock = regDeepslateOreBlock;
            regDeepslateOreItem = ITEMS.registerSimpleBlockItem(name + "_deepslate_ore", finalDeepslateOreBlock);
        }
        if (storageBlockFactory != null) {
            regStorageBlock = BLOCKS.registerBlock(name + "_block", storageBlockFactory, storageBlockProperties);
            final DeferredBlock<Block> finalStorageBlock = regStorageBlock;
            regStorageItem = ITEMS.registerSimpleBlockItem(name + "_block", finalStorageBlock);
        }
        if (ingotSupplier != null) {
            regIngot = ITEMS.registerItem(name + "_ingot", ingotSupplier);
        }
        if (gemSupplier != null) {
            regGem = ITEMS.registerItem(name + "_gem", gemSupplier);
        }
        if (rawOreSupplier != null) {
            regRawOre = ITEMS.registerItem("raw_" + name, rawOreSupplier);
        }
        if (dustSupplier != null) {
            regDust = ITEMS.registerItem(name + "_dust", dustSupplier);
        }
        if (plateSupplier != null) {
            regPlate = ITEMS.registerItem(name + "_plate", plateSupplier);
        }
        if (nuggetSupplier != null) {
            regNugget = ITEMS.registerItem(name + "_nugget", nuggetSupplier);
        }

        // Register fluid (FluidType + source + flowing + block + bucket)
        if (fluidDefinition != null) {
            regMaterialFluid = registerFluid(fluidDefinition);
            regBucket = regMaterialFluid.bucket();
        }

        entry = new RegisteredEntry(
                name, color, fluidDefinition,
                regOreBlock, regOreItem,
                regDeepslateOreBlock, regDeepslateOreItem,
                regStorageBlock, regStorageItem,
                regIngot, regGem, regRawOre,
                regDust, regPlate, regNugget,
                regBucket, regMaterialFluid
        );
        return this;
    }

    @SuppressWarnings("unchecked")
    private MaterialFluid registerFluid(FluidDefinition def) {
        String fluidName = def.resolveName(name);

        // Register the FluidType
        DeferredHolder<FluidType, FluidType> fluidType = (DeferredHolder<FluidType, FluidType>)
                (DeferredHolder<?, ?>) FLUID_TYPES.register(fluidName, () -> new MaterialFluidType(
                        FluidType.Properties.create()
                                .temperature(def.temperature)
                                .density(def.density)
                                .viscosity(def.viscosity)
                                .lightLevel(def.luminosity),
                        color,
                        def
                ));

        // Array holders to break circular reference between source <-> flowing
        final DeferredHolder<Fluid, FlowingFluid>[] sourceHolder = new DeferredHolder[1];
        final DeferredHolder<Fluid, FlowingFluid>[] flowingHolder = new DeferredHolder[1];
        final DeferredBlock<LiquidBlock>[] blockHolder = new DeferredBlock[1];
        final DeferredItem<Item>[] bucketHolder = new DeferredItem[1];

        // Supplier that creates fresh Properties each time (all references resolved lazily)
        Supplier<BaseFlowingFluid.Properties> propsSupplier = () -> {
            BaseFlowingFluid.Properties props = new BaseFlowingFluid.Properties(
                    fluidType::get,
                    () -> sourceHolder[0].get(),
                    () -> flowingHolder[0].get()
            ).block(() -> blockHolder[0].get()).bucket(() -> bucketHolder[0].get());
            return props;
        };

        // Register source and flowing fluids
        sourceHolder[0] = (DeferredHolder<Fluid, FlowingFluid>)
                (DeferredHolder<?, ?>) FLUIDS.register(fluidName,
                        () -> new BaseFlowingFluid.Source(propsSupplier.get()));

        flowingHolder[0] = (DeferredHolder<Fluid, FlowingFluid>)
                (DeferredHolder<?, ?>) FLUIDS.register("flowing_" + fluidName,
                        () -> new BaseFlowingFluid.Flowing(propsSupplier.get()));

        // Register fluid block
        blockHolder[0] = BLOCKS.registerBlock(fluidName + "_block",
                props -> new NCFluidBlock(sourceHolder[0].get(), props),
                () -> BlockBehaviour.Properties.of()
                        .noCollision()
                        .strength(100.0F)
                        .noLootTable()
                        .liquid()
                        .replaceable());

        // Register bucket item
        bucketHolder[0] = ITEMS.registerItem(fluidName + "_bucket", props -> new BucketItem(
                sourceHolder[0].get(), props.craftRemainder(Items.BUCKET).stacksTo(1)));

        MaterialFluid materialFluid = new MaterialFluid(
                fluidType, sourceHolder[0], flowingHolder[0], blockHolder[0], bucketHolder[0]
        );
        def.setRegisteredFluid(materialFluid);
        return materialFluid;
    }

    public record RegisteredEntry(
            String name,
            int color,
            FluidDefinition fluidDefinition,
            DeferredBlock<Block> oreBlock,
            DeferredItem<BlockItem> oreItem,
            DeferredBlock<Block> deepslateOreBlock,
            DeferredItem<BlockItem> deepslateOreItem,
            DeferredBlock<Block> storageBlock,
            DeferredItem<BlockItem> storageItem,
            DeferredItem<Item> ingot,
            DeferredItem<Item> gem,
            DeferredItem<Item> rawOre,
            DeferredItem<Item> dust,
            DeferredItem<Item> plate,
            DeferredItem<Item> nugget,
            DeferredItem<Item> bucket,
            MaterialFluid materialFluid
    ) {
        public boolean hasOre() { return oreBlock != null; }
        public boolean hasDeepslateOre() { return deepslateOreBlock != null; }
        public boolean hasBlock() { return storageBlock != null; }
        public boolean hasIngot() { return ingot != null; }
        public boolean hasGem() { return gem != null; }
        public boolean hasRawOre() { return rawOre != null; }
        public boolean hasDust() { return dust != null; }
        public boolean hasPlate() { return plate != null; }
        public boolean hasNugget() { return nugget != null; }
        public boolean hasBucket() { return bucket != null; }
        public boolean hasFluid() { return materialFluid != null; }
    }
}
