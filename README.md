# Mod Template for NeoForge 1.21.1

A universal template for building Minecraft mods on **NeoForge 1.21.1**. The core idea is a **single-point registration system** powered by `ModEntryBuilder` - define your items, blocks, machines, and processors in one place, and the mod automatically registers everything needed: blocks, block items, block entities, menus, screens, recipe types, recipe serializers, and creative tab entries.

## Features

- **One-liner registration** - add a full-featured processing machine with a single builder chain
- **Material registration system** - define a complete material (ore, ingot, dust, plate, nugget, raw ore, storage block, molten fluid, bucket) with one call via `addMetalOreMaterial`
- **Unified `ModEntry` record** - holds all deferred references (block, item, block entity, menu, recipe type, recipe serializer, material) in one object
- **Universal processor system** - reusable block, block entity, container, screen, and recipe classes that work for any machine
- **Side configuration system** - per-slot, per-face push/pull/disable mode for both item and fluid handlers, with NBT persistence and auto-transfer via `SidedContentHandler`
- **Energy bar GUI widget** - `EnergyBar` renders a live FE fill bar with hover tooltip showing stored/max FE
- **Auto creative tab placement** - machines go to Functional Blocks, plain blocks to Building Blocks, standalone items to Ingredients
- **Auto screen registration** - all entries with menus get their GUI screens registered on the client automatically
- **Datagen-ready** - includes providers for block states, item models, loot tables, recipes, tags (block, item, fluid), and language files
- **Recipe datagen builder** - `UniversalProcessorRecipeBuilder` with fluent API for item/fluid inputs/outputs, process time, and energy cost
- **Dynamic JEI integration** - automatically registers JEI recipe categories, catalysts, and recipes for every processor entry
- **EMI integration** - automatically registers EMI recipe categories, workstations, and recipes for every processor entry
- **AE2 pattern encoder support** - transfer processor recipes from JEI directly into AE2 Pattern Encoding Terminal via `JEI2PatternEncoderTransfer`
- **KubeJS support** - processor recipe schemas auto-registered for all entries with recipes
- **ComputerCraft support** - every machine auto-exposed as a CC peripheral with Lua functions for energy, inventory, fluid tanks, progress, and slot/tank management
- **Automatic ore world generation** - `addMetalOreMaterial` / `addCrystalOreMaterial` auto-registers configured features, placed features, and biome modifiers; height range and vein counts driven by a live TOML config via `WorldGen`
- **Multiblock system** - declare controller + ports + casing/interior or an authored NBT structure via `MultiblockEntryBuilder`; auto-wires validator, off-thread tick executor, block-change listener, persistence, and client sync
- **Tool & armor sets** - register a full tool set (`addToolSet`) or armor set (`addArmorSet`) in one call, with per-slot factory overrides for custom item classes
- **Per-entry config gating** - every `ModEntryBuilder` registration gets an individual TOML toggle in `entries.toml`; disabling makes the entry inert (skips tick, won't open its GUI) and hides it from creative tabs + JEI/EMI, with runtime gating that responds to config reload
- **Extra registry builders** - one-call registration for `ParticleType`, `EntityType`, `SoundEvent`, `MobEffect`, plus per-world and per-entity persistent data backed by `AttachmentType` (`addParticleType`, `addEntityType`, `addSoundEvent`, `addMobEffect`, `addWorldCapability`, `addEntityCapability`)
- **Processor catalysts** - declare supported catalyst types per processor (`energy` / `speed` / `efficiency`); each type gets its own catalyst slot, auto-attaches matching catalyst items, and runs `preTick`/`postTick` hooks scaled by stack count (`addCatalyst`, `.catalysts(...)`)
- **Tag-based recipe outputs** - recipe item/fluid outputs may be tags, resolved to one concrete stack at production time via a `mod_tag_priority` config; empty item tags drop the recipe at load via NeoForge conditions, empty fluid tags via the runtime completeness gate

## Auto-Registered Components

When you use `ModEntryBuilder`, the following registries are populated automatically based on what you configure:

| Component | Registry | Registered when |
|---|---|---|
| **Block** | `BLOCKS` | `.block(...)` is called |
| **BlockItem** | `ITEMS` | A block is present but no custom item supplier is set |
| **Item** | `ITEMS` | `.item(...)` is called (standalone item) |
| **BlockEntityType** | `BLOCK_ENTITIES` | `.blockEntity(...)` is called |
| **MenuType** | `CONTAINERS` | `.menu(...)` is called |
| **RecipeType** | `RECIPE_TYPES` | `.withRecipes()` is called |
| **RecipeSerializer** | `RECIPE_SERIALIZERS` | `.withRecipes()` is called |
| **Creative Tab Entry** | - | Automatic based on entry type (machine / block / item) |
| **GUI Screen** | Client event | Automatic for every entry that has a menu |
| **Material (ore, ingot, etc.)** | `BLOCKS` / `ITEMS` | `addMetalOreMaterial(...)` is called |
| **Molten Fluid** | `FLUID_TYPES` / `FLUIDS` | Material has a `FluidDefinition` |
| **Bucket Item** | `ITEMS` | Material has a fluid |
| **JEI Category** | JEI plugin | Automatic for every entry with recipes |
| **EMI Category** | EMI plugin | Automatic for every entry with recipes |
| **AE2 Pattern Transfer** | JEI transfer handler | Automatic for every entry with recipes (requires AE2) |
| **CC Peripheral** | `PeripheralCapability` | Automatic for every entry with a block entity (requires CC:Tweaked) |
| **Multiblock Controller** | `BLOCKS` / `BLOCK_ENTITIES` / `CONTAINERS` | `addMultiblockController(...)` is called |
| **Multiblock Port** | `BLOCKS` / `BLOCK_ENTITIES` | `addMultiblockPart(...)` is called |
| **Multiblock Entry** | `MultiblockRegistry` | `MultiblockEntryBuilder.build()` is called |
| **Tool Set** (sword, pickaxe, axe, shovel, hoe) | `ITEMS` | `addToolSet(name, tier)` is called |
| **Armor Set** (helmet, chestplate, leggings, boots) | `ITEMS` | `addArmorSet(name, material)` is called |
| **Armor Material** | `ARMOR_MATERIALS` | `ArmorMaterialEntry.builder(...).build()` |
| **Configured Feature** | Datapack / worldgen | Material has `worldgenQty > 0` (default for `metalOre` / `crystalOre`) |
| **Placed Feature** | Datapack / worldgen | Same |
| **Biome Modifier** | Datapack / worldgen | Same; targets `IS_OVERWORLD` biomes |
| **ParticleType** | `PARTICLE_TYPES` | `addParticleType(...)` is called |
| **EntityType** | `ENTITY_TYPES` | `addEntityType(...)` is called |
| **SoundEvent** | `SOUND_EVENTS` | `addSoundEvent(...)` is called |
| **MobEffect** | `MOB_EFFECTS` | `addMobEffect(...)` is called |
| **AttachmentType** (world / entity data) | `ATTACHMENT_TYPES` | `addWorldCapability(...)` / `addEntityCapability(...)` |
| **Catalyst** (item + behavior) | `ITEMS` / `CatalystRegistry` | `addCatalyst(...)` is called |
| **Per-entry config toggle** | `entries.toml` | Automatic for every non-material entry |


## For Developers: Building Your Own Mod

### 1. Fork This Repository

Click **Fork** on GitHub to create your own copy of this template.

### 2. Rename the Mod ID

Replace all occurrences of `nc` and `nc` with your chosen mod ID:

- **`gradle.properties`** - update `mod_id`, `mod_name`, `mod_description`, `mod_authors`, and `maven_group`
- **`src/main/resources/META-INF/neoforge.mods.toml`** - verify property expansion picks up your new values
- **`src/main/resources/nc.mixins.json`** - rename the file and update the `"package"` and `"refmap"` fields inside
- **Base package** - rename `src/main/java/igentuman/nc/` to match your `maven_group` and `mod_id`
- **`Main.java`** - update the `@Mod("nc")` annotation value
- **`build.gradle`** - confirm `mixin { add ... }` references your new mixin JSON name

> **Tip:** Use your IDE's global search-and-replace to catch every reference in one pass.

### 3. Add Your Content

With the mod ID in place, define your content inside [`setup/ModEntries.java`](./src/main/java/igentuman/nc/setup/ModEntries.java) using the `ModEntryBuilder` fluent API. The following documentation pages cover the most common scenarios:

- [Processors & Items Registration](./docs/processors-registration.md) - machines, simple items, plain blocks
- [Materials Registration](./docs/materials-registration.md) - full metal material sets (ore, ingot, dust, fluid, …)
- [Tools & Armor Registration](./docs/tools-armor-registration.md) - full tool / armor sets with per-slot custom item classes
- [Custom Block Entities](./docs/custom-block-entities.md) - custom block/BE/container/screen classes
- [Side Configuration System](./docs/side-configuration.md) - per-face push/pull config for machines
- [Multiblocks](./docs/multiblocks.md) - controller + ports + cubic/NBT validator, off-thread tick logic
- [Entry Config Gating](./docs/entry-config-gating.md) - per-entry enable/disable toggles in `entries.toml`
- [Registry Builders](./docs/registry-builders.md) - particle, entity, sound, mob effect, world/entity data
- [Processor Catalysts](./docs/catalysts.md) - energy/speed/efficiency catalysts with per-tick hooks
- [Tag-Based Recipe Outputs](./docs/tag-outputs.md) - tag outputs resolved via `mod_tag_priority`
- [JEI Integration](./docs/jei-integration.md) - automatic and custom recipe categories
- [KubeJS Support](./docs/kubejs-support.md) - script processor recipes with KubeJS
- [ComputerCraft Support](./docs/cc-support.md) - Lua peripheral API for all machines
- [Materials Registration](./docs/materials-registration.md#world-generation) - ore world generation via builder

After adding content, run `./gradlew runData` to regenerate assets and data, then `./gradlew runClient` to test in-game.

---

## Project Structure

```
src/main/java/igentuman/nc/
  Main.java                          - Mod entry point (@Mod)
  registration/
    ModEntryBuilder.java             - Fluent builder for mod entries
    ModEntry.java                    - Record holding all deferred registrations
    MaterialEntry.java               - Material definition (ore, ingot, dust, plate, etc.)
    FluidDefinition.java             - Fluid properties for materials
    MaterialFluid.java               - Registered fluid references (source, flowing, block, bucket)
    MaterialFluidType.java           - Custom FluidType with rendering properties
  setup/
    Registers.java                   - All DeferredRegister instances
    ModEntries.java                  - Where you define your content
    Client.java                      - Client-side setup (screen + fluid rendering)
    Common.java                      - Common setup events
  block/
    UniversalProcessorBlock.java     - Reusable processor block
    MultiblockControllerBlock.java   - Controller block for a multiblock
    MultiblockPartBlock.java         - Port/part block for a multiblock
  block_entity/
    GlobalBlockEntity.java           - Base block entity (energy, inventory, fluid, side config, tick)
    UniversalProcessorBE.java        - Thin subclass wiring capabilities + catalysts from ModEntry config
    MultiblockControllerBE.java      - Controller BE, bridges block lifecycle to MultiblockHandler
    MultiblockPartBE.java            - Port BE with controller back-reference
    catalyst/
      CatalystType.java              - energy / speed / efficiency
      Catalyst.java                  - Abstract behavior (host, power, preTick/postTick)
      CatalystFactory.java           - host -> Catalyst factory interface
      CatalystDef.java               - Registered catalyst (item + type + factory)
      CatalystRegistry.java          - type -> defs map
      EnergyCatalyst.java / SpeedCatalyst.java / EfficiencyCatalyst.java - reference catalysts
  multiblock/
    MultiblockEntry.java             - Registered multiblock (validator/logic/cache suppliers)
    MultiblockEntryBuilder.java      - Fluent builder
    MultiblockRegistry.java          - name → entry map
    MultiblockHandler.java           - Per-level executor, instances, block-change events
  api/multiblock/
    IMultiblockValidator.java        - Validator interface
    IMultiblockLogic.java            - Logic/tick interface (tickServer runs off-thread)
    IMultiblockCache.java            - Cached state + structure-position set
    BlockPredicate.java              - (BlockState, BlockEntity) predicate
  api/impl/
    CubicMultiblockValidator.java        - Auto-detected hollow-box validator
    DeterminedMultiblockValidator.java   - NBT structure validator
    MultiblockCacheImpl.java             - Default cache
    MultiblockLogicImpl.java             - Default logic (wires port BE controller refs)
    MultiblockPattern.java               - Optional 3D BlockPredicate grid
  container/
    UniversalProcessorContainer.java - Reusable menu/container
  screen/
    UniversalProcessorScreen.java    - Reusable GUI screen
  recipe/
    UniversalProcessorRecipe.java    - Default recipe implementation
    UniversalProcessorRecipeSerializer.java
    ItemOutput.java                  - Item output union (concrete item OR item tag) + codec
    FluidOutput.java                 - Fluid output union (concrete fluid OR fluid tag) + codec
    TagOutputResolver.java           - Resolves tag outputs via mod_tag_priority (cached)
  datagen/
    ModBlockStateProvider.java       - Block state & model datagen
    ModItemModelProvider.java        - Item model datagen
    ModLanguageProvider.java         - Language file datagen
    ModDataGenerators.java           - Datagen entry point
    recipe/
      ModRecipeProvider.java         - Recipe datagen entry point
      UniversalProcessorRecipeBuilder.java - Fluent recipe builder
    tag/
      ModBlockTagProvider.java       - Block tag datagen
      ModItemTagProvider.java        - Item tag datagen
      ModFluidTagProvider.java       - Fluid tag datagen
  setup/level/
    ModConfiguredFeatures.java       - Ore configured features (auto-generated per material)
    ModPlacedFeatures.java           - Ore placed features with height/count placement
    ModBiomeModifiers.java           - Attaches placed features to overworld biomes
    ConfigurableOrePlacement.java    - Custom PlacementModifier reading from WorldGen config
    ModBiomes.java                   - Stub for custom biome registration
    ModDimensions.java               - Stub for custom dimension registration
  config/
    WorldGen.java                    - TOML config (min/max height, vein size, veins per chunk)
    Entries.java                     - Per-entry enable toggles (entries.toml)
    Materials.java                   - Per-material per-product-type toggles (materials.toml)
    Processors.java                  - Thin alias delegating to Entries (fork compat)
    Common.java                      - mod_tag_priority list for tag-output resolution (common.toml)
  compat/
    jei/ModJeiPlugin.java            - Dynamic JEI integration
    jei/ProcessorRecipeCategory.java - JEI recipe category renderer
    emi/ModEmiPlugin.java            - Dynamic EMI integration
    emi/ProcessorEmiRecipe.java      - EMI recipe wrapper
    ae2/JEI2PatternEncoderTransfer.java - AE2 pattern encoder transfer handler
    kubejs/ModKubeJSPlugin.java      - KubeJS recipe schema registration
    cc/CCCompatHandler.java          - Registers PeripheralCapability for every block entity
    cc/ProcessorPeripheral.java      - IPeripheral wrapping GlobalBlockEntity with Lua API
```

## Documentation

- [Processors & Items Registration](./docs/processors-registration.md) - Register machines, simple items, and plain blocks using `ModEntryBuilder`
- [Materials Registration](./docs/materials-registration.md) - Register full metal materials (ore, ingot, dust, fluid, etc.)
- [Tools & Armor Registration](./docs/tools-armor-registration.md) - Register full tool / armor sets, optionally with per-slot custom item classes
- [Custom Block Entities](./docs/custom-block-entities.md) - Add custom Block, BlockEntity, Container, and Screen classes
- [Side Configuration System](./docs/side-configuration.md) - Per-slot, per-face push/pull configuration for machines
- [Multiblocks](./docs/multiblocks.md) - Declare controllers, ports, cubic/NBT validators, and off-thread tick logic via `MultiblockEntryBuilder`
- [Entry Config Gating](./docs/entry-config-gating.md) - Per-entry enable/disable toggles, runtime gating, and how disabling behaves
- [Registry Builders](./docs/registry-builders.md) - Register `ParticleType`, `EntityType`, `SoundEvent`, `MobEffect`, and world/entity data
- [Processor Catalysts](./docs/catalysts.md) - Energy/speed/efficiency catalysts with per-tick hooks and per-type slots
- [Tag-Based Recipe Outputs](./docs/tag-outputs.md) - Tag item/fluid outputs resolved via `mod_tag_priority`, with datagen conditions
- [JEI Integration](./docs/jei-integration.md) - Automatic and custom JEI recipe category setup
- [KubeJS Support](./docs/kubejs-support.md) - Script processor recipes with KubeJS
- [ComputerCraft Support](./docs/cc-support.md) - Lua peripheral API, all functions, and example scripts
- [Materials Registration](./docs/materials-registration.md) - Register full metal materials including automatic ore world generation

---

## Build & Run

Requires **Java 21**.

```bash
./gradlew build          # Build the mod JAR
./gradlew runClient      # Run Minecraft client
./gradlew runServer      # Run dedicated server
./gradlew runData        # Run data generators
./gradlew clean          # Clean build artifacts
```

## License

MIT
