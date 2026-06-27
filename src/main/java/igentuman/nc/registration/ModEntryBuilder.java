package igentuman.nc.registration;

import igentuman.nc.util.SlotsLayout;
import igentuman.nc.util.caps.EnergyCapDefinition;
import igentuman.nc.util.caps.FluidCapDefinition;
import igentuman.nc.util.caps.ItemCapDefinition;
import igentuman.nc.block.UniversalProcessorBlock;
import igentuman.nc.block_entity.UniversalProcessorBE;
import igentuman.nc.block_entity.catalyst.CatalystDef;
import igentuman.nc.block_entity.catalyst.CatalystFactory;
import igentuman.nc.block_entity.catalyst.CatalystRegistry;
import igentuman.nc.block_entity.catalyst.CatalystType;
import igentuman.nc.container.MultiblockControllerContainer;
import igentuman.nc.container.MultiblockPortContainer;
import igentuman.nc.container.UniversalProcessorContainer;
import igentuman.nc.block.MultiblockControllerBlock;
import igentuman.nc.block.MultiblockPartBlock;
import igentuman.nc.block_entity.MultiblockControllerBE;
import igentuman.nc.block_entity.MultiblockPartBE;
import igentuman.nc.recipe.UniversalProcessorRecipe;
import igentuman.nc.recipe.UniversalProcessorRecipeSerializer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import org.apache.commons.lang3.function.TriFunction;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.function.Function;

import static igentuman.nc.Main.rl;
import static igentuman.nc.setup.ModEntries.ENTRIES;
import static igentuman.nc.setup.Registers.*;

public class ModEntryBuilder {

    private final String name;
    private Supplier<? extends Block> blockSupplier;
    private Supplier<? extends Item> itemSupplier;
    private Function<Block, Supplier<? extends BlockEntityType<?>>> entitySupplierFactory;
    private Supplier<MenuType<?>> menuType;
    private EnergyCapDefinition energy;
    private ItemCapDefinition itemCapDefinition;
    private FluidCapDefinition fluidCapDefinition;
    private Supplier<RecipeType<?>> recipeTypeSupplier;
    private Supplier<RecipeSerializer<?>> recipeSerializerSupplier;
    public MaterialEntry material;
    private SlotsLayout slotsLayout;
    private Tier toolTier;
    private String toolItemPrefix;
    private Holder<ArmorMaterial> armorMaterialHolder;
    private ArmorMaterialEntry armorMaterialEntry;
    private int armorDurabilityMultiplier = 15;
    private String armorItemPrefix;
    private BiFunction<Tier, Item.Properties, ? extends Item> swordFactory;
    private BiFunction<Tier, Item.Properties, ? extends Item> pickaxeFactory;
    private BiFunction<Tier, Item.Properties, ? extends Item> axeFactory;
    private BiFunction<Tier, Item.Properties, ? extends Item> shovelFactory;
    private BiFunction<Tier, Item.Properties, ? extends Item> hoeFactory;
    private TriFunction<Holder<ArmorMaterial>, ArmorItem.Type, Item.Properties, ? extends Item> helmetFactory;
    private TriFunction<Holder<ArmorMaterial>, ArmorItem.Type, Item.Properties, ? extends Item> chestplateFactory;
    private TriFunction<Holder<ArmorMaterial>, ArmorItem.Type, Item.Properties, ? extends Item> leggingsFactory;
    private TriFunction<Holder<ArmorMaterial>, ArmorItem.Type, Item.Properties, ? extends Item> bootsFactory;
    private DeferredHolder<BlockEntityType<?>, BlockEntityType<?>> registeredBe;
    private Set<CatalystType> supportedCatalysts = Set.of();

    private ModEntryBuilder(String name) {
        this.name = name;
    }

    public static ModEntryBuilder add(String name) {
        return new ModEntryBuilder(name);
    }

    public static ModEntryBuilder addItem(String name) {
        return add(name).item(() -> new Item(new Item.Properties()));
    }

    public static ModEntryBuilder addItem(String name, Supplier<? extends Item> itemSupplier) {
        return add(name).item(itemSupplier);
    }

    /**
     * Registers an isotope (item-form variants + decay metadata) into {@link igentuman.nc.setup.ModEntries#ISOTOPES}.
     * Isotopes live in their own registry, not {@code ENTRIES}; see {@link IsotopeEntry}.
     */
    public static IsotopeEntry addIsotope(String name, double radiation) {
        return IsotopeEntry.register(name, radiation);
    }

    public static ModEntryBuilder addMetalOreMaterial(String name, int color) {
        ModEntryBuilder materialEntry = add(name)
                .material(color);
        materialEntry.material.metalOre();
        return materialEntry;
    }

    public static ModEntryBuilder addToolSet(String name, Tier tier) {
        ModEntryBuilder b = add(name + "_tools");
        b.toolTier = tier;
        b.toolItemPrefix = name;
        return b;
    }

    public static ModEntryBuilder addArmorSet(String name, Holder<ArmorMaterial> material) {
        ModEntryBuilder b = add(name + "_armor");
        b.armorMaterialHolder = material;
        b.armorItemPrefix = name;
        return b;
    }

    public static ModEntryBuilder addArmorSet(String name, Holder<ArmorMaterial> material, int durabilityMultiplier) {
        ModEntryBuilder b = addArmorSet(name, material);
        b.armorDurabilityMultiplier = durabilityMultiplier;
        return b;
    }

    public static ModEntryBuilder addArmorSet(String name, ArmorMaterialEntry material) {
        ModEntryBuilder b = add(name + "_armor");
        b.armorMaterialEntry = material;
        b.armorItemPrefix = name;
        return b;
    }

    public ModEntryBuilder sword(BiFunction<Tier, Item.Properties, ? extends Item> factory)   { this.swordFactory = factory;   return this; }
    public ModEntryBuilder pickaxe(BiFunction<Tier, Item.Properties, ? extends Item> factory) { this.pickaxeFactory = factory; return this; }
    public ModEntryBuilder axe(BiFunction<Tier, Item.Properties, ? extends Item> factory)     { this.axeFactory = factory;     return this; }
    public ModEntryBuilder shovel(BiFunction<Tier, Item.Properties, ? extends Item> factory)  { this.shovelFactory = factory;  return this; }
    public ModEntryBuilder hoe(BiFunction<Tier, Item.Properties, ? extends Item> factory)     { this.hoeFactory = factory;     return this; }

    public ModEntryBuilder toolFactory(BiFunction<Tier, Item.Properties, ? extends Item> factory) {
        this.swordFactory = factory;
        this.pickaxeFactory = factory;
        this.axeFactory = factory;
        this.shovelFactory = factory;
        this.hoeFactory = factory;
        return this;
    }

    public ModEntryBuilder helmet(TriFunction<Holder<ArmorMaterial>, ArmorItem.Type, Item.Properties, ? extends Item> factory)     { this.helmetFactory = factory;     return this; }
    public ModEntryBuilder chestplate(TriFunction<Holder<ArmorMaterial>, ArmorItem.Type, Item.Properties, ? extends Item> factory) { this.chestplateFactory = factory; return this; }
    public ModEntryBuilder leggings(TriFunction<Holder<ArmorMaterial>, ArmorItem.Type, Item.Properties, ? extends Item> factory)   { this.leggingsFactory = factory;   return this; }
    public ModEntryBuilder boots(TriFunction<Holder<ArmorMaterial>, ArmorItem.Type, Item.Properties, ? extends Item> factory)      { this.bootsFactory = factory;      return this; }

    public ModEntryBuilder armorFactory(TriFunction<Holder<ArmorMaterial>, ArmorItem.Type, Item.Properties, ? extends Item> factory) {
        this.helmetFactory = factory;
        this.chestplateFactory = factory;
        this.leggingsFactory = factory;
        this.bootsFactory = factory;
        return this;
    }

    public ModEntryBuilder material(int color) {
        material = MaterialEntry.of(color, name);
        return this;
    }

    public ModEntryBuilder ingot() {
        if(material == null) {
            material(0);
        }
        material.setIngotSupplier(() -> new Item(new Item.Properties()));
        return this;
    }

    public ModEntryBuilder gem() {
        if(material == null) {
            material(0);
        }
        material.setGemSupplier(() -> new Item(new Item.Properties()));
        return this;
    }

    public ModEntryBuilder dust() {
        if(material == null) {
            material(0);
        }
        material.setDustSupplier(() -> new Item(new Item.Properties()));
        return this;
    }

    public ModEntryBuilder nugget() {
        if(material == null) {
            material(0);
        }
        material.setNuggetSupplier(() -> new Item(new Item.Properties()));
        return this;
    }

    public ModEntryBuilder rawOre() {
        if(material == null) {
            material(0);
        }
        material.setRawOreSupplier(() -> new Item(new Item.Properties()));
        return this;
    }

    public ModEntryBuilder plate() {
        if(material == null) {
            material(0);
        }
        material.setPlateSupplier(() -> new Item(new Item.Properties()));
        return this;
    }

    public ModEntryBuilder ore() {
        if(material == null) {
            material(0);
        }
        BlockBehaviour.Properties oreProps = BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(3.0f, 3.0f)
                .requiresCorrectToolForDrops()
                .sound(SoundType.STONE);
        material.setOre(() -> new Block(oreProps), () -> new BlockItem(new Block(oreProps), new Item.Properties()));
        return this;
    }

    public ModEntryBuilder storageBlock() {
        if(material == null) {
            material(0);
        }
        BlockBehaviour.Properties blockProps = BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0f, 6.0f)
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL);
        material.setBlock(() -> new Block(blockProps), () -> new BlockItem(new Block(blockProps), new Item.Properties()));
        return this;
    }

    public ModEntryBuilder fluid() {
        if(material == null) {
            material(0);
        }
        material.setFluidDefinition(FluidDefinition.metal());
        return this;
    }

    public ModEntryBuilder fluid(FluidDefinition fluidDefinition) {
        if(material == null) {
            material(0);
        }
        material.setFluidDefinition(fluidDefinition);
        return this;
    }

    public ModEntryBuilder worldgenConfig(int minHeight, int maxHeight, int qty) {
        if(material == null) {
            material(0);
        }
        material.worldgenConfig(minHeight, maxHeight, qty);
        return this;
    }

    public ModEntryBuilder withLayout(SlotsLayout layout) {
        this.slotsLayout = layout;
        return this;
    }

    /**
     * Declares which catalyst types this processor accepts. One catalyst slot is allocated
     * per type (in {@link CatalystType} ordinal order) during {@link #build()}.
     */
    public ModEntryBuilder catalysts(CatalystType... types) {
        this.supportedCatalysts = types.length == 0 ? Set.of() : EnumSet.copyOf(Arrays.asList(types));
        return this;
    }

    /**
     * Registers a catalyst: creates the catalyst item and records a {@link CatalystDef} so any
     * processor supporting {@code type} auto-attaches it. The item participates in F8 gating.
     */
    public static ModEntry addCatalyst(String name, CatalystType type, CatalystFactory factory) {
        ModEntry entry = add(name).item(() -> new Item(new Item.Properties())).build();
        CatalystRegistry.register(new CatalystDef(entry.item(), type, factory));
        return entry;
    }

    @SuppressWarnings("unchecked")
    public static ModEntryBuilder addMultiblockController(String name) {
        ModEntryBuilder b = add(name);
        b.blockSupplier = () -> new MultiblockControllerBlock(defaultMultiblockProps(), name,
                () -> (BlockEntityType<? extends MultiblockControllerBE>) b.registeredBe.get());
        b.entitySupplierFactory = block -> () -> BlockEntityType.Builder.of(
                (pos, state) -> new MultiblockControllerBE(b.registeredBe.get(), pos, state, name),
                block
        ).build(null);
        b.menuType = () -> IMenuTypeExtension.create(
                (IContainerFactory<MultiblockControllerContainer>) MultiblockControllerContainer::new);
        return b.withRecipes();
    }

    public static ModEntry addMultiblockPart(String name) {
        return addMultiblockPart(name, defaultMultiblockProps());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static ModEntry addMultiblockPart(String name, BlockBehaviour.Properties props) {
        final DeferredHolder<BlockEntityType<?>, BlockEntityType<?>>[] beHolder = new DeferredHolder[1];

        DeferredBlock<Block> block = BLOCKS.register(name, () ->
                new MultiblockPartBlock(props, name,
                        () -> (BlockEntityType<? extends MultiblockPartBE>) beHolder[0].get()));

        DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<?>> beReg =
                BLOCK_ENTITIES.register(name, () -> BlockEntityType.Builder.of(
                        (pos, state) -> new MultiblockPartBE(beHolder[0].get(), pos, state, name),
                        block.get()
                ).build(null));
        beHolder[0] = (DeferredHolder<BlockEntityType<?>, BlockEntityType<?>>) (DeferredHolder) beReg;

        DeferredItem<Item> item = ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));

        DeferredHolder<MenuType<?>, MenuType<?>> menu =
                (DeferredHolder<MenuType<?>, MenuType<?>>) (DeferredHolder<?, ?>)
                        CONTAINERS.register(name, () -> IMenuTypeExtension.create(
                                (IContainerFactory<MultiblockPortContainer>) MultiblockPortContainer::new));

        ModEntry entry = new ModEntry(name, block, item, menu, beHolder[0], false, null, null, null, null, null, null, null, null, null, Set.of(), Set.of());
        ENTRIES.put(name, entry);
        return entry;
    }

    private static BlockBehaviour.Properties defaultMultiblockProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3.5f, 6.0f)
                .requiresCorrectToolForDrops();
    }

    public static ModEntryBuilder addProcessor(String name) {
        return add(name)
                .block(UniversalProcessorBlock::new)
                .blockEntity(UniversalProcessorBE::new)
                .menu(UniversalProcessorContainer::new)
                .withEnergyInput(100000)
                .withLayout(SlotsLayout.ONE_TO_ONE)
                .withRecipes();
    }

    // ---- Leaf registry factories ----
    // These vanilla registries have no slot in the ModEntry record, so the factory
    // returns its own DeferredHolder directly. Client-side wiring (particle providers,
    // entity renderers/attributes) is the caller's responsibility in their own setup listener.

    public static DeferredHolder<ParticleType<?>, SimpleParticleType> addParticleType(String name, boolean overrideLimiter) {
        return PARTICLE_TYPES.register(name, () -> new SimpleParticleType(overrideLimiter) {});
    }

    public static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> addEntityType(String name, EntityType.Builder<T> builder) {
        return ENTITY_TYPES.register(name, () -> builder.build(name));
    }

    public static DeferredHolder<SoundEvent, SoundEvent> addSoundEvent(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(rl(name)));
    }

    public static DeferredHolder<MobEffect, MobEffect> addMobEffect(String name, Supplier<MobEffect> factory) {
        return MOB_EFFECTS.register(name, factory);
    }

    // ---- Persistent data builders (backed by AttachmentType) ----
    // NeoForge 1.21.1 has no "level capability" type; per-world and per-entity persistent data
    // both use AttachmentType. The holder is attached at use-site: level.getData(holder) for world
    // data, entity.getData(holder) for entity data. Supply a fully-built AttachmentType via the
    // factory, e.g. () -> AttachmentType.builder(MyData::new).serialize(MyData.CODEC).build().

    public static <T> DeferredHolder<AttachmentType<?>, AttachmentType<T>> addWorldCapability(String name, Supplier<AttachmentType<T>> attachmentFactory) {
        return ATTACHMENT_TYPES.register(name, attachmentFactory);
    }

    public static <T> DeferredHolder<AttachmentType<?>, AttachmentType<T>> addEntityCapability(String name, Supplier<AttachmentType<T>> attachmentFactory) {
        return ATTACHMENT_TYPES.register(name, attachmentFactory);
    }

    public <B extends Block> ModEntryBuilder block(Supplier<B> blockSupplier) {
        this.blockSupplier = blockSupplier;
        return this;
    }

    public <B extends Block> ModEntryBuilder block(Function<String, B> blockFactory) {
        this.blockSupplier = () -> blockFactory.apply(name);
        return this;
    }

    public ModEntryBuilder item(Supplier<? extends Item> itemSupplier) {
        this.itemSupplier = itemSupplier;
        return this;
    }

    public <E extends BlockEntity> ModEntryBuilder blockEntity(TriFunction<BlockPos, BlockState, String, E> entityConstructor) {
        this.entitySupplierFactory = block -> () -> BlockEntityType.Builder.of(
                (pos, state) -> entityConstructor.apply(pos, state, name),
                block
        ).build(null);
        return this;
    }

    public <T extends AbstractContainerMenu> ModEntryBuilder menu(IContainerFactory<T> factory) {
        this.menuType = () -> IMenuTypeExtension.create(factory);
        return this;
    }


    public ModEntryBuilder withEnergyInput(int capacity) {
        this.energy = EnergyCapDefinition.processor(capacity);
        return this;
    }

    public ModEntryBuilder withEnergyOutput(int capacity) {
        this.energy = EnergyCapDefinition.generator(capacity);
        return this;
    }

    public ModEntryBuilder fluidCap(int inputTanks, int outputTanks, int defaultTanks) {
        FluidCapDefinition def = FluidCapDefinition.create();
        for (int i = 0; i < inputTanks; i++) def.addInput(100000);
        for (int i = 0; i < outputTanks; i++) def.addOutput(100000);
        for (int i = 0; i < defaultTanks; i++) def.addGlobal(100000);
        this.fluidCapDefinition = def;
        return this;
    }

    public ModEntryBuilder itemCap(int inputSlots, int outputSlots) {
        this.itemCapDefinition = ItemCapDefinition.create().inputs(inputSlots).outputs(outputSlots);
        return this;
    }

    public ModEntryBuilder withRecipes() {
        this.recipeTypeSupplier = () -> RecipeType.<UniversalProcessorRecipe>simple(rl(name));
        this.recipeSerializerSupplier = () -> new UniversalProcessorRecipeSerializer(name);
        return this;
    }

    public ModEntryBuilder withRecipes(
            Supplier<RecipeType<?>> recipeTypeSupplier,
            Supplier<RecipeSerializer<?>> recipeSerializerSupplier
    ) {
        this.recipeTypeSupplier = recipeTypeSupplier;
        this.recipeSerializerSupplier = recipeSerializerSupplier;
        return this;
    }

    public ModEntry build() {
        DeferredBlock<Block> block = null;
        DeferredItem<Item> item = null;
        DeferredHolder<MenuType<?>, MenuType<?>> menu = null;
        DeferredHolder<BlockEntityType<?>, BlockEntityType<?>> blockEntity = null;
        DeferredHolder<RecipeType<?>, RecipeType<?>> recipeType = null;
        if (blockSupplier != null) {
            block = BLOCKS.register(name, blockSupplier);
        }

        final DeferredBlock<Block> finalBlock = block;

        // Register item - use provided supplier or create BlockItem
        if (itemSupplier != null) {
            item = ITEMS.register(name, itemSupplier);
        } else if(finalBlock != null) {
            item = ITEMS.register(name, () -> new BlockItem(finalBlock.get(), new Item.Properties()));
        }

        if (entitySupplierFactory != null && block != null) {
            @SuppressWarnings("unchecked")
            DeferredHolder<BlockEntityType<?>, BlockEntityType<?>> entityCast =
                    (DeferredHolder<BlockEntityType<?>, BlockEntityType<?>>)
                            BLOCK_ENTITIES.register(name, () -> entitySupplierFactory.apply(finalBlock.get()).get());
            blockEntity = entityCast;
            this.registeredBe = entityCast;
        }
        if (menuType != null) {
            @SuppressWarnings("unchecked")
            DeferredHolder<MenuType<?>, MenuType<?>> menuCast =
                    (DeferredHolder<MenuType<?>, MenuType<?>>)
                            (DeferredHolder<?, ?>) CONTAINERS.register(name, menuType);
            menu = menuCast;
        }
        DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> recipeSerializer = null;
        if (recipeTypeSupplier != null) {
            @SuppressWarnings("unchecked")
            DeferredHolder<RecipeType<?>, RecipeType<?>> recipeCast =
                    (DeferredHolder<RecipeType<?>, RecipeType<?>>)
                            (DeferredHolder<?, ?>) RECIPE_TYPES.register(name, recipeTypeSupplier);
            recipeType = recipeCast;
        }
        if (recipeSerializerSupplier != null) {
            @SuppressWarnings("unchecked")
            DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> serializerCast =
                    (DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>>)
                            (DeferredHolder<?, ?>) RECIPE_SERIALIZERS.register(name, recipeSerializerSupplier);
            recipeSerializer = serializerCast;
        }

        if (material != null) {
            material.build();
        }

        ToolSetEntry toolSetEntry = null;
        if (toolTier != null) {
            toolSetEntry = ToolSetEntry.build(toolItemPrefix != null ? toolItemPrefix : name, toolTier,
                    swordFactory, pickaxeFactory, axeFactory, shovelFactory, hoeFactory);
        }

        ArmorSetEntry armorSetEntry = null;
        String armorName = armorItemPrefix != null ? armorItemPrefix : name;
        if (armorMaterialEntry != null) {
            armorSetEntry = ArmorSetEntry.build(armorName, armorMaterialEntry,
                    helmetFactory, chestplateFactory, leggingsFactory, bootsFactory);
        } else if (armorMaterialHolder != null) {
            armorSetEntry = ArmorSetEntry.build(armorName, armorMaterialHolder, armorDurabilityMultiplier,
                    helmetFactory, chestplateFactory, leggingsFactory, bootsFactory);
        }

        if (!supportedCatalysts.isEmpty()) {
            if (itemCapDefinition == null) itemCapDefinition = ItemCapDefinition.create();
            itemCapDefinition.catalyst(supportedCatalysts.size());
        }

        ModEntry entry = new ModEntry(name, block, item, menu, blockEntity, recipeTypeSupplier != null, recipeType, recipeSerializer, material, itemCapDefinition, fluidCapDefinition, energy, slotsLayout, toolSetEntry, armorSetEntry, Set.of(), supportedCatalysts);
        ENTRIES.put(name, entry);
        return entry;

    }
}
