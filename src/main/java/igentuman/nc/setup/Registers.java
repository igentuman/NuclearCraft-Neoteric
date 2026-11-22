package igentuman.nc.setup;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.Codec;
import igentuman.nc.api.crafting.CraftingPatternData;
import igentuman.nc.api.particle.ParticleDefinition;
import igentuman.nc.api.particle.ParticleSourceData;
import igentuman.nc.recipe.ResetNbtRecipe;
import igentuman.nc.setup.level.ConfigurableOrePlacement;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import static igentuman.nc.NuclearCraft.MODID;

/** Owns every DeferredRegister for the mod and registers them all to the mod event bus. */
public class Registers {
    public static final DeferredRegister<ParticleDefinition> PARTICLE_DEFINITIONS = DeferredRegister.create(ParticleDefinition.REGISTRY_KEY, MODID);
    public static final Registry<ParticleDefinition> PARTICLE_DEFINITION_REGISTRY = PARTICLE_DEFINITIONS.makeRegistry(builder -> builder.sync(true));
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(Registries.MENU, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, MODID);
    public static final DeferredRegister<RecipeBookCategory> RECIPE_BOOK_CATEGORIES = DeferredRegister.create(Registries.RECIPE_BOOK_CATEGORY, MODID);
    public static final DeferredHolder<RecipeBookCategory, RecipeBookCategory> PROCESSOR_RECIPE_CATEGORY =
            RECIPE_BOOK_CATEGORIES.register("processor", RecipeBookCategory::new);
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, MODID);
    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIER_TYPES = DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, MODID);
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, MODID);
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, MODID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, MODID);
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, MODID);
    public static final DeferredRegister<GameEvent> GAME_EVENTS = DeferredRegister.create(Registries.GAME_EVENT, MODID);
    public static final DeferredHolder<GameEvent, GameEvent> BLACKHOLE_VIBRATION =
            GAME_EVENTS.register("blackhole_vibration", () -> new GameEvent(32));
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, MODID);
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MODID);
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MODID);
    public static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, MODID);
    public static final DeferredRegister<VillagerProfession> VILLAGER_PROFESSIONS = DeferredRegister.create(Registries.VILLAGER_PROFESSION, MODID);
    public static final DeferredRegister<MapCodec<? extends LootItemCondition>> LOOT_CONDITION_TYPES = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> CONTAINER_MAGNET =
            DATA_COMPONENTS.register("container_magnet", () -> DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> QNP_ENERGY =
            DATA_COMPONENTS.register("qnp_energy", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.INT)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> QNP_MODE =
            DATA_COMPONENTS.register("qnp_mode", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.INT)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> HEV_ENERGY =
            DATA_COMPONENTS.register("hev_energy", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.INT)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> HEV_QE_CHARGE =
            DATA_COMPONENTS.register("hev_qe_charge", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.INT)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CraftingPatternData>> CRAFTING_PATTERN =
            DATA_COMPONENTS.register("crafting_pattern", () -> DataComponentType.<CraftingPatternData>builder()
                    .persistent(CraftingPatternData.CODEC)
                    .networkSynchronized(CraftingPatternData.STREAM_CODEC)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ParticleSourceData>> PARTICLE_SOURCE =
            DATA_COMPONENTS.register("particle_source", () -> DataComponentType.<ParticleSourceData>builder()
                    .persistent(ParticleSourceData.CODEC)
                    .networkSynchronized(ParticleSourceData.STREAM_CODEC)
                    .build());

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ResetNbtRecipe>> RESET_NBT_SERIALIZER =
            RECIPE_SERIALIZERS.register("reset_nbt", ResetNbtRecipe::createSerializer);

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static final DeferredHolder<PlacementModifierType<?>, PlacementModifierType<?>> CONFIGURABLE_ORE_PLACEMENT =
        (DeferredHolder) PLACEMENT_MODIFIER_TYPES.register("configurable_ore",
            () -> (PlacementModifierType) () -> ConfigurableOrePlacement.CODEC);

    public static void init(IEventBus modEventBus) {
        GAME_EVENTS.register(modEventBus);
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        CONTAINERS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        RECIPE_TYPES.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
        RECIPE_BOOK_CATEGORIES.register(modEventBus);
        FLUID_TYPES.register(modEventBus);
        FLUIDS.register(modEventBus);
        PLACEMENT_MODIFIER_TYPES.register(modEventBus);
        FEATURES.register(modEventBus);
        PARTICLE_TYPES.register(modEventBus);
        PARTICLE_DEFINITIONS.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);
        SOUND_EVENTS.register(modEventBus);
        MOB_EFFECTS.register(modEventBus);
        ATTACHMENT_TYPES.register(modEventBus);
        DATA_COMPONENTS.register(modEventBus);
        POI_TYPES.register(modEventBus);
        VILLAGER_PROFESSIONS.register(modEventBus);
        LOOT_CONDITION_TYPES.register(modEventBus);
    }
}
