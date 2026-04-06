# NuclearCraft KubeJS Plugin Port — NeoForge 1.21.1 / KubeJS 2101.7.2

Plan only. Do not execute without review.

> **Amendment 2026-04-06:** Sections §6.5 (round-trip audit) and §7.1 (per-recipe-type schema matrix) added after discovering during execution that the original plan's assumption of a single shared schema does not hold — several NC recipe types use different top-level JSON field names for the same semantic slot (e.g. `fission_boiling` writes `heatRequired`, `fusion_coolant` writes `coolingRate`). The port must build **one schema per recipe-type group**, not one shared schema. Earlier sections of this document that assume a single base schema should be read as the "happy path" for the generic group; the real work is in §7.1.

## Goal

Re-enable NC's KubeJS integration so that pack-level scripts can:

1. Create, remove, replace, and replace-output/replace-input NC recipes using KubeJS syntax (e.g. `event.recipes.nuclearcraft.manufactory({...})`, `event.remove({type:'nuclearcraft:manufactory'})`, `event.replaceOutput({type:'nuclearcraft:manufactory'}, 'minecraft:iron_ingot', '#c:ingots/steel')`).
2. Drive cross-mod recipe unification from JS — feeding other mods' outputs through NC processors and vice versa.
3. Register custom fission fuels from scripts (existing feature, currently dead).
4. React to the black hole player-enter event from scripts (existing feature, currently dead).

Non-goals for this plan (can be follow-ups):

- New recipe types beyond what already exists in `NcRecipeType.ALL_RECIPES`.
- Exposing NC internals (BE state, multiblock data) as KubeJS bindings.
- A custom `particle` ingredient wrapper with full parity — the particle component can ship as a thin passthrough that only round-trips JSON (see §5.2).

## Current state

`build.gradle` excludes three compilation units:

```groovy
exclude 'igentuman/nc/compat/kubejs/**'
exclude 'igentuman/nc/mixin/KubeJSRecipesEventJSMixin.java'
// ParticleStackKubeJSMixin.java is compiled but NOT registered in nuclearcraft.mixins.json
```

Files currently present but excluded / unregistered:

| File | Status |
|------|--------|
| `compat/kubejs/NuclearCraftKubeJSPlugin.java` | excluded |
| `compat/kubejs/NCKubeJsEvents.java` | excluded |
| `compat/kubejs/InputParticle.java` | excluded |
| `compat/kubejs/OutputParticle.java` | excluded |
| `compat/kubejs/ParticleComponents.java` | excluded |
| `compat/kubejs/ParticleMatch.java` | excluded |
| `mixin/KubeJSRecipesEventJSMixin.java` | excluded |
| `mixin/ParticleStackKubeJSMixin.java` | compiled but not registered in `nuclearcraft.mixins.json` |

NeoForge events already fired by NC (no change needed):

- `BlackHoleBE.PlayerEnterBlackholeEvent` — fired at `BlackHoleBE.java:229`.
- `FissionFuel.RegisterFissionFuelEvent` — fired at `FissionFuel.java:56`.

Both have TODO markers referencing the excluded `NCKubeJsEvents` bridge.

## API impact summary — old → new

NC's current (excluded) KubeJS code was written against the old 20x.x.x series API. KubeJS 2101.7.2 is a near-complete rewrite of the recipe/component/event layer. Below is every symbol NC currently references that has changed, moved, or been removed. Sources cited are from the local checkout at `/Users/caliane/Sources/kubejs` branch `2101`.

| Old symbol | Status | Replacement |
|---|---|---|
| `dev.latvian.mods.kubejs.KubeJSPlugin` | moved | `dev.latvian.mods.kubejs.plugin.KubeJSPlugin` |
| Plugin discovery via `@KubeJSPlugin` / reflection | unchanged mechanism | `META-INF`/`resources/kubejs.plugins.txt`, one FQCN per line, optional `client` / mod-id flags. Parsed by `plugin/KubeJSPlugins.java` |
| `RecipeJS` | renamed | `dev.latvian.mods.kubejs.recipe.KubeRecipe` |
| `RecipeJS::new` (passed to schema ctor) | removed | Schemas take no class; factory bound via `RecipeSchema.factory(KubeRecipeFactory)` fluent call |
| `RecipesEventJS` | removed | Hook `KubeJSPlugin.beforeRecipeLoading(RecipesKubeEvent, RecipeManagerKJS, Map<ResourceLocation, JsonElement>)` |
| `RegisterRecipeSchemasEvent` | removed | Override `KubeJSPlugin.registerRecipeSchemas(RecipeSchemaRegistry)`; no event class |
| `event.namespace(MODID).register(name, schema)` | API moved | `RecipeSchemaRegistry.register(...)` (signature to confirm at impl time — new registry API lives in `recipe/schema/RecipeSchemaRegistry.java`) |
| `RecipeKey<T>` | unchanged | `dev.latvian.mods.kubejs.recipe.RecipeKey` |
| `ItemComponents.INPUT_ARRAY` / `OUTPUT_ARRAY` | removed | `SizedIngredientComponent.SIZED_INGREDIENT` (+ `OPTIONAL_`, `FLAT_`, `OPTIONAL_FLAT_` variants). Value type: NeoForge `SizedIngredient` |
| `FluidComponents.INPUT_ARRAY` / `OUTPUT_ARRAY` | removed | `SizedFluidIngredientComponent.FLAT` / `NESTED` / `OPTIONAL_*`. Value type: NeoForge `SizedFluidIngredient` |
| `InputItem` / `OutputItem` (kubejs.item) | removed | Use `SizedIngredient` directly; there is no KubeJS wrapper layer |
| `NumberComponent.DoubleRange.ANY_DOUBLE.min(x).max(y).key(n).defaultOptional()` | API shape changed | `NumberComponent` is now a sealed interface; ranges are records constructed via `NumberComponent.doubleRange(min, max)` / `NumberComponent.longRange(min, max)`. `.key()` / `.defaultOptional()` fluent ops on `RecipeKey` are still expected but need verification in `NumberComponent.java` at impl time |
| `RecipeComponent<T>.componentType(): String` | signature changed | `type(): RecipeComponentType<?>` — now returns a registered type handle, not a string |
| `RecipeComponent<T>.read(recipe, from)` | renamed | `wrap(RecipeScriptContext, Object)` |
| `RecipeComponent<T>.isInput/isOutput(recipe, value, ReplacementMatch)` | consolidated | `matches(RecipeMatchContext, T, ReplacementMatchInfo)` |
| `RecipeComponentWithParent` | unchanged package | Still at `recipe/component/RecipeComponentWithParent.java`; override semantics preserved, but methods take `KubeRecipe` not `RecipeJS` |
| `ReplacementMatch` | package moved | `recipe/match/ReplacementMatch.java`; old `contains()`-style contract is gone — replacement logic now lives on `RecipeComponent.matches()` + `replace()` |
| `InputReplacement` / `OutputReplacement` | removed | Implement via `RecipeComponent.replace(RecipeScriptContext, T, ReplacementMatchInfo, Object)` |
| `IngredientSupplierKJS.kjs$asIngredient()` | still exists | `core/IngredientSupplierKJS.java` — same contract |
| `IngredientJS.of(Object)` / `.ofJson(JsonElement)` | removed | Parse via `IngredientWrapper` in `plugin/builtin/wrapper/IngredientWrapper.java` (need to verify exact static entry point at impl time — may be a Rhino-context-scoped wrap) |
| `ItemStackJS.of(Object)` | removed | Use vanilla `ItemStack`; KubeJS exposes item wrapping through `ItemWrapper` context bindings |
| `IngredientPlatformHelper.get().tag(String)` | removed | Use `Ingredient.of(TagKey<Item>)` directly with `ItemTags.create(ResourceLocation)` |
| `RecipePlatformHelper.get().getCustomIngredient(JsonObject)` | removed | Use NeoForge `ICustomIngredient` codec path (via `CraftingHelper`) or the new `IngredientWrapper` JSON parser |
| `EventJS` / `StartupEventJS` | removed | `KubeEvent` / `KubeStartupEvent` — both interfaces in `event/` |
| `EventGroup.of`, `.server(name, supplier)`, `.startup(name, supplier)` | signature tweak | Still at `event/EventGroup.java`, but supplier is now `Supplier<Class<? extends KubeEvent>>` |
| `EventHandler.post(ScriptType, EventJS)` | removed as a public surface | Firing from Java goes through the handler's own post method, now typed to `KubeEvent`. Signature to confirm in `EventHandler.java` at impl time |
| `ScriptType.SERVER/STARTUP/CLIENT` | unchanged | `script/ScriptType.java` |
| `EventResult.interruptDefault/False/True` | unchanged | `event/EventResult.java` |
| `ConsoleJS.getCurrent(ConsoleJS.SERVER)` | signature changed | `script/ConsoleJS.java` — `getCurrent(@Nullable Context)` + static fields `ConsoleJS.SERVER / STARTUP / CLIENT` |
| `TinyMap` | unchanged | `util/TinyMap.java` |
| `JsonSerializable` (was `rhino.mod.util`) | moved | `dev.latvian.mods.kubejs.util.JsonSerializable`; now `toJson(Context cx): JsonElement` |
| `RecipeExceptionJS` | renamed | `error/RecipeComponentException` |

Anything marked "to confirm at impl time" is something the research pass could not pin down without reading implementation, and should be resolved by reading the source file on the spot rather than guessing.

## Design

### Architecture

The plugin does NOT need to subclass `KubeRecipe`. NC's existing `AbstractRecipe` / `NcRecipe` hierarchy already deserializes recipes from JSON via NeoForge codecs (see `NcRecipeSerializer.codec()` / `.streamCodec()`). The KubeJS side only needs to produce JSON that matches that shape.

This means the port can be built on the **default `KubeRecipe` factory** — we just register a `RecipeSchema` per NC recipe type, with the right `RecipeKey`s that match NC's JSON fields (`input`, `output`, `inputFluids`, `outputFluids`, `timeModifier`, `powerModifier`, `radiation`, and for target chamber also `inputParticles`, `outputParticles`, `crossSection`, `maxEnergy`). When KubeJS writes the JSON back, NC's existing codecs parse it.

One caveat: KubeJS's built-in item/fluid components serialize to NeoForge's `SizedIngredient` / `SizedFluidIngredient` JSON shapes. These may not match NC's current `ItemStackIngredient` / `FluidStackIngredient` JSON shape byte-for-byte. This is the single biggest open question in the plan — see §6.

### Recipe type enumeration

NC has ~15 active recipe types (`NcRecipeType.initializeRecipes()` lines 39–54). For each, we register one `RecipeSchema`. There are two distinct schema shapes:

**Base schema** (14 types): manufactory, fission_controller, msr_controller, nc_ore_veins, fusion_core, fusion_coolant, accelerator_coolant, fission_boiling, kugelblitz_chamber, turbine_controller, plus every entry in `Processors.all().keySet()` with `hasRecipes() == true`.

Keys: `input` (item[]), `output` (item[]), `inputFluids` (fluid[]), `outputFluids` (fluid[]), `timeModifier` (double), `powerModifier` (double), `radiation` (double). All optional.

**Target chamber schema** (1 type): `target_chamber`.

Keys: all of the base keys, plus `inputParticles`, `outputParticles`, `crossSection` (double), `maxEnergy` (long).

The existing `NuclearCraftKubeJSPlugin.registerRecipeSchemas` already iterates `ALL_RECIPES.keySet()` and branches on `"target_chamber"`. That structure is kept; only the inner API calls change.

## Files to port or create

### 1. `NuclearCraftKubeJSPlugin.java` — rewrite

New base: `dev.latvian.mods.kubejs.plugin.KubeJSPlugin`. Signatures to override:

- `void registerRecipeSchemas(RecipeSchemaRegistry registry)` — iterate `NcRecipeType.ALL_RECIPES.keySet()`, register base schema or target_chamber schema per entry under namespace `"nuclearcraft"`.
- `void registerRecipeComponents(RecipeComponentTypeRegistry registry)` — register the particle component type (see §5).
- `void registerEvents(EventGroupRegistry registry)` — register `NCKubeJsEvents.GROUP`.
- `void beforeRecipeLoading(RecipesKubeEvent event, RecipeManagerKJS manager, Map<ResourceLocation, JsonElement> recipeJsons)` — injection point for `generateCustomFuelRecipes`. Same logic as the old `injectRuntimeRecipes`, but add synthesized recipes into `recipeJsons` (the JSON map) instead of `recipesByName` (the parsed map).
- `void afterInit()` (optional) — call `NcRecipeType.invalidateCache()` if still needed.

`RecipeKey` declarations: rebuild using the new component APIs. Rough structure (signatures to verify at impl time):

```java
private static final RecipeKey<SizedIngredient[]> INPUT_ITEMS =
    SizedIngredientComponent.SIZED_INGREDIENT.asArray().key("input", ComponentRole.INPUT).optional();
private static final RecipeKey<SizedIngredient[]> OUTPUT_ITEMS =
    SizedIngredientComponent.SIZED_INGREDIENT.asArray().key("output", ComponentRole.OUTPUT).optional();
private static final RecipeKey<SizedFluidIngredient[]> INPUT_FLUIDS =
    SizedFluidIngredientComponent.FLAT.asArray().key("inputFluids", ComponentRole.INPUT).optional();
private static final RecipeKey<SizedFluidIngredient[]> OUTPUT_FLUIDS =
    SizedFluidIngredientComponent.FLAT.asArray().key("outputFluids", ComponentRole.OUTPUT).optional();
private static final RecipeKey<Double> POWER_MODIFIER =
    NumberComponent.doubleRange(-1000, 1000).key("powerModifier", ComponentRole.OTHER).optional();
// etc.
```

The exact method names (`asArray`, `.key(name, role)`, `.optional()`) need verification against `RecipeComponent.java` and `RecipeKey.java` at implementation time. The old code's `.key(name).defaultOptional()` is likely still similar but the role argument is now required in some overloads.

### 2. `NCKubeJsEvents.java` — rewrite

- Convert event classes (`PlayerEnterBlackholeEventJS`, `RegisterFissionFuelEventJS`) from extending `EventJS` / `StartupEventJS` to implementing `KubeEvent` / `KubeStartupEvent`. Both are interfaces with default methods, so implementations are short — no abstract methods must be implemented unless we want custom cancel/exit semantics.
- `EventGroup.server(name, () -> PlayerEnterBlackholeEventJS.class)` and `EventGroup.startup(name, () -> RegisterFissionFuelEventJS.class)` — same shape, supplier-of-Class.
- Replace `EventHandler.post(ScriptType.SERVER, evenjs)` with whatever the 2101.x firing API is. Look at how `ServerEvents.RECIPES` or similar is posted in `server/KubeJSServerEventHandler.java` for a current example.
- `generateCustomFuelRecipes` — now writes into the `Map<ResourceLocation, JsonElement> recipeJsons` passed to `beforeRecipeLoading`. The existing logic already builds `JsonObject`s, so the body barely changes — only the return path differs (no `event.custom(...).id(...)`).
- Delete the stale `@SubscribeEvent` NeoForge import — it was unused.

### 3. `InputParticle.java` — rewrite

Drop interfaces:

- `IngredientSupplierKJS` — keep if the class is still in `core/`, but note: `kjs$asIngredient()` returns vanilla `Ingredient`. Particles have no ItemStack equivalent so this returns `Ingredient.of(Items.BARRIER)` exactly as today (placeholder). This is fine for the component-level matching path because KubeJS never actually looks at the item — only our `ParticleMatch` does.
- `InputReplacement` — **REMOVED**. Replacement logic moves into `ParticleComponents.INPUT.replace(cx, original, match, with)`.
- `JsonSerializable` — relocate import to `dev.latvian.mods.kubejs.util.JsonSerializable`. Method signature becomes `JsonElement toJson(Context cx)`. The existing `toJsonJS(boolean alwaysNest)` can stay as a helper but the interface method becomes the simpler `toJson(cx)` variant.

Drop imports and their uses:

- `dev.latvian.mods.kubejs.item.ItemStackJS` → replaced by vanilla `ItemStack` access.
- `dev.latvian.mods.kubejs.item.ingredient.IngredientJS` → replaced with `IngredientWrapper.parseString` / `parseJson` (or `Ingredient.of(ItemTags.create(...))` for tag shortcuts, `Ingredient.fromJson(...)` via vanilla codec for raw JSON).
- `dev.latvian.mods.kubejs.platform.IngredientPlatformHelper` → removed; use vanilla tag construction.
- `dev.latvian.mods.kubejs.platform.RecipePlatformHelper` → for custom ingredient JSON: use NeoForge's `Ingredient.CODEC.parse(...)` or whatever the 2101.x wrapper exposes.
- `dev.latvian.mods.kubejs.recipe.RecipeExceptionJS` → `dev.latvian.mods.kubejs.error.RecipeComponentException`.
- `dev.latvian.mods.rhino.mod.util.JsonSerializable` → `dev.latvian.mods.kubejs.util.JsonSerializable`.
- `dev.latvian.mods.rhino.util.RemapForJS` → still exists in rhino; verify import path. Probably unchanged.

### 4. `OutputParticle.java` — rewrite

- Drop `OutputReplacement` implementation; move replacement logic to `ParticleComponents.OUTPUT.replace(...)`.
- `dev.latvian.mods.kubejs.item.InputItem` → unused, delete the import.
- `dev.latvian.mods.kubejs.util.ConsoleJS` → `dev.latvian.mods.kubejs.script.ConsoleJS`; call site becomes `ConsoleJS.getCurrent(null)` (or pass a captured Rhino `Context` when available) — `ConsoleJS.SERVER` also works as a direct field access.
- Same ingredient/platform helper removals as §3.

### 5. `ParticleComponents.java` — rewrite

Two sub-tasks:

#### 5.1 Component shape

Rewrite `INPUT` and `OUTPUT` as `RecipeComponent<InputParticle>` / `RecipeComponent<OutputParticle>` against the new interface:

- `RecipeComponentType<?> type()` — see §5.2; returns the type handle registered in `registerRecipeComponents`.
- `Codec<InputParticle> codec()` — new. Must produce a codec that round-trips NC's JSON shape `{ "particle": "proton", "amount": 100, "meanEnergy": 1000, "focus": 0.5, ... }`. Can be implemented hand-rolled or via `RecordCodecBuilder`.
- `TypeInfo typeInfo()` — Rhino type info, return `TypeInfo.of(InputParticle.class)`.
- `wrap(RecipeScriptContext cx, Object from)` — old `read(recipe, from)`, same body calling `InputParticle.of(from)`.
- `writeToJson(KubeRecipe, RecipeComponentValue<InputParticle>, JsonObject)` — unchanged body modulo parameter type renames.
- `readFromJson(KubeRecipe, RecipeComponentValue<InputParticle>, JsonObject)` — unchanged body.
- `matches(RecipeMatchContext, InputParticle, ReplacementMatchInfo)` — replaces old `isInput(recipe, value, ReplacementMatch)`. The `ReplacementMatchInfo` wraps the actual `ReplacementMatch` — need to call `info.match()` (or whatever the accessor is) and check for `ParticleMatch` instance.
- `replace(RecipeScriptContext, InputParticle, ReplacementMatchInfo, Object)` — moved from `InputReplacement.replaceInput`.
- `isEmpty(InputParticle)` — unchanged.
- `asPatternKey()` — remove; not part of the new interface.

Same pattern for `OUTPUT`. `INPUT_ARRAY` / `OUTPUT_ARRAY` become `INPUT.asArray()` / `OUTPUT.asArray()` if `RecipeComponent` still exposes that helper; otherwise use `ListRecipeComponent` from `recipe/component/ListRecipeComponent.java`.

#### 5.2 Component type registration

`RecipeComponentType<InputParticle>` and `RecipeComponentType<OutputParticle>` must be declared as static constants and registered via `KubeJSPlugin.registerRecipeComponents`:

```java
public static final RecipeComponentType<InputParticle> INPUT_TYPE =
    RecipeComponentType.unit(rl("input_particle"), INPUT_INSTANCE);
public static final RecipeComponentType<OutputParticle> OUTPUT_TYPE =
    RecipeComponentType.unit(rl("output_particle"), OUTPUT_INSTANCE);
```

Exact constructor signature of `RecipeComponentType.unit(ResourceLocation, RecipeComponent<T>)` to verify on the spot — there may be a lambda variant that takes a type-to-component function.

### 6. JSON shape compatibility — OPEN QUESTION

KubeJS writes item arrays using NeoForge `SizedIngredient`'s codec. NC reads item arrays using `ItemStackIngredient.fromJson(...)`. If these codecs agree on the wire format, the port is transparent. If they don't, one of two things must happen:

1. **Wrap NC ingredients in a custom `RecipeComponent<ItemStackIngredient>`** — more code but keeps JSON shape identical to what NC already expects. This means losing KubeJS's automatic `replaceInput`/`replaceOutput` support for item-matching, unless we also implement `matches()` + `replace()` on the custom component.
2. **Make NC's `NcRecipeBuilder` / `NcIngredient` accept `SizedIngredient`-shaped JSON as an input alias**, leaving the canonical NC recipe files unchanged but allowing KubeJS-produced JSON through the same deserializer.

**Resolution step before implementation:** write a one-shot test — use KubeJS to create a dummy manufactory recipe in-game, dump its raw JSON, and compare to the JSON produced by NC's datagen for the same recipe. If the shapes match (or differ only in optional-field naming), we take path (1) with built-in components. If they diverge structurally, we take path (2) with custom components or adapters.

This decision blocks the concrete wiring in §1 but does not block any other file. I recommend porting everything else first and stubbing the item/fluid keys, then resolving this last when it is the only thing left.

### 7. `ParticleMatch.java` — minor edit

Only change is `dev.latvian.mods.kubejs.recipe.ReplacementMatch` → `dev.latvian.mods.kubejs.recipe.match.ReplacementMatch`. The `contains()` default-method pattern on the old interface may be gone — `ReplacementMatch` in 2101.x has no `contains()` — so `ParticleMatch` becomes a standalone interface extending `ReplacementMatch` with its own `contains(ParticleStack)` method, and the match is driven by `ParticleComponents.INPUT.matches()` checking `info.match() instanceof ParticleMatch m`.

### 8. `KubeJSRecipesEventJSMixin.java` — delete

The target class `RecipesEventJS` no longer exists. The mixin's purpose (diagnostic log) is obsolete. Delete the file and remove the `build.gradle` exclusion line. Do not replace it with a 2101.x equivalent — no equivalent is needed.

### 9. `ParticleStackKubeJSMixin.java` — register and verify

- Remove the TODO comment.
- Add `"ParticleStackKubeJSMixin"` to the `mixins` array in `src/main/resources/nuclearcraft.mixins.json`.
- Verify it compiles against the new `ParticleStack` class (NC's own class; hasn't changed).
- The three `kjs$` methods (`withCount`, `getId`, `getCount`) are purely additive on `ParticleStack` and require no KubeJS-side contract other than the `kjs$` prefix convention, which is still honored in 2101.x.

### 10. `build.gradle` — re-enable compilation

Delete these three lines:

```groovy
exclude 'igentuman/nc/compat/kubejs/**'        // KubeJS
exclude 'igentuman/nc/mixin/KubeJSRecipesEventJSMixin.java' // KubeJS mixin
```

Change `compileOnly` on the kubejs dependency to remain `compileOnly` (still correct — NC should not ship KubeJS at runtime, it should only soft-depend on it).

### 11. `META-INF/neoforge.mods.toml` — add optional dependency

Declare an optional `kubejs` dependency so that NC's KubeJS plugin only loads when KubeJS is present, and does not crash when it isn't. Check `igentuman/nc/compat/kubejs/NuclearCraftKubeJSPlugin.java` use sites — they must be guarded by `ModList.get().isLoaded("kubejs")` only at the *call-site* boundaries that NC itself invokes (the `BlackHoleBE` and `FissionFuel` bridges). KubeJS's own plugin loader already gates plugin class loading on its own presence via `kubejs.plugins.txt`, so the plugin class itself is safe.

### 12. `kubejs.plugins.txt` — create

Create `src/main/resources/kubejs.plugins.txt` with:

```
igentuman.nc.compat.kubejs.NuclearCraftKubeJSPlugin kubejs
```

The trailing `kubejs` gates the plugin on the `kubejs` mod-id being loaded, so the class is never touched in environments without KubeJS.

### 13. Wire `BlackHoleBE` and `FissionFuel` bridges

In `BlackHoleBE.java:229`:

```java
if (ModList.get().isLoaded("kubejs")) {
    igentuman.nc.compat.kubejs.NCKubeJsEvents.onPlayerEnterBlackhole(event);
}
if (event.isCanceled()) return;
```

In `FissionFuel.java:56`:

```java
if (ModList.get().isLoaded("kubejs")) {
    igentuman.nc.compat.kubejs.NCKubeJsEvents.onFissionFuelRegister(event);
}
```

Delete the TODO comments at both sites.

## §6.5 Round-trip audit findings (added during execution)

Before the original plan is viable, the schema definition must preserve every JSON field NC writes and every field NC reads. KubeJS silently drops any field that isn't mapped to a `RecipeKey` on the schema, so this is not optional. The following was verified by reading NC's recipe serializers and sampling generated recipe JSON files.

### A. Alternate timing-field names per recipe type

Several NC recipe types write a domain-specific field in place of `timeModifier`. These are **different top-level JSON keys**, not aliases — they coexist with other timing fields and a single shared schema cannot use `RecipeKey.alt()` because the primary name used on write-back must match what NC reads.

| Recipe type | Primary "timing" field name | JSON also has `timeModifier`? |
|---|---|---|
| `fission_boiling` | `heatRequired` | No (verified: `src/generated/resources/data/nuclearcraft/recipe/fission_boiling/water-steam.json`) |
| `fusion_coolant` | `coolingRate` | No (verified: `fusion_coolant/liquid_helium.json`) |
| `accelerator_coolant` | `coolingRate` | No (verified: `accelerator_coolant/liquid_helium.json`) |
| `turbine_controller` | `heatRequired` | Not verified — audit claim; confirm from generated JSON during implementation |
| `fusion_core` | `timeModifier` + extra `temperature` field | **Yes** — uses standard `timeModifier` AND adds a separate `temperature` field. Both present in `fusion_core/deuterium-helium_3.json` |
| All other recipe types | `timeModifier` | Yes |

Java mapping quirk: NC's `NcRecipeSerializer` **reuses the `timeModifier` parameter slot** as storage for `heatRequired` / `coolingRate` / `conversionRate` at the Java object level, but at the JSON layer these are genuinely different top-level keys. The KubeJS schema layer only cares about the JSON layer.

### B. Per-recipe-type extra fields

| Field | JSON key | Type | Recipe types | Source |
|---|---|---|---|---|
| Temperature | `temperature` | double | `fusion_core` | `NcRecipeSerializer.java:232` + verified JSON |
| Rarity modifier | `rarityModifier` | double | `nc_ore_veins` (optional — only appears on non-default recipes, e.g. `platinum.json`) | `NcRecipeSerializer.java:231` |
| Particle input | `inputParticles` | particle[] | `target_chamber` only | `TargetChamberRecipe.java:23` |
| Particle output | `outputParticles` | particle[] | `target_chamber` only | `TargetChamberRecipe.java:24` |
| Max particle energy | `maxEnergy` | long | `target_chamber` only | `TargetChamberRecipe.java:21` |
| Cross-section | `crossSection` | double | `target_chamber` only | `TargetChamberRecipe.java:22` |

Special case: in `NcRecipeSerializer.java:233-234`, if `temperature > 1` the serializer overwrites `rarityModifier` with `temperature`. This means `temperature` and `rarityModifier` share the same Java slot but live under different JSON keys at serialization time. For the schema, keep them as two **independent optional** keys on the relevant recipe types.

### C. Particle JSON shape

`ParticleStack.fromJSON()` at `ParticleStack.java:68-79` requires all four fields with **no defaults**:

```json
{
  "particle": "proton",
  "amount": 100,
  "meanEnergy": 1000000,
  "focus": 0.5
}
```

Missing any of the four is a parse failure. This means:

1. **`InputParticle` must store `meanEnergy` and `focus`** in addition to `particleName` and `count`. The codec must round-trip all four. My in-progress `InputParticle` rewrite currently stores only `particleName` and `count` — **needs extension** before the port can ship.
2. `OutputParticle` already stores the full `ParticleStack` (which carries particle/amount/meanEnergy/focus). Its codec just needs to expose those fields plus the optional `chance` / `minRolls` / `maxRolls` modifiers.
3. No generated target chamber recipes exist in the current repo output, so there are no real examples to diff against. The schema must still round-trip the shape that `TargetChamberRecipe.read(JsonObject)` expects.

### D. NC's `nbt` field on item outputs — confirmed dead

11 generated recipes (10 ore veins + 1 analyzer) include a legacy `"nbt": "{...}"` string field on item outputs. Grepping `ItemStackIngredientCreator.java` for `nbt` returns **nothing** — NC has no code that reads this field in 1.21.1. The vanilla `Ingredient.CODEC` used by `NCIngredients.fromJson` silently ignores unknown fields on a map.

**Conclusion: this is a stale datagen artifact with zero runtime effect.** The KubeJS port does not need to preserve it. If a script writes an ore_veins recipe without the `nbt` field, NC behaves identically to if the field were present. No custom item component required.

Flag for later cleanup: NC's datagen still writes these fields. They should be removed from datagen output in a separate issue — they're dead weight.

### E. NC's fluid tag shape `{"tag": "...", "amount": N}`

NC's `FluidStackIngredientCreator.deserialize` reads both `{"fluid": id, "amount": N}` and `{"tag": id, "amount": N}` forms (verified from generated JSON — e.g. `deuterium-helium_3.json` uses `"tag": "c:deuterium"`). NeoForge 1.21.1's `SizedFluidIngredient.FLAT_CODEC` wraps NeoForge's sealed `FluidIngredient` hierarchy which includes `FluidTagIngredient`, so tag support is expected to work. **Unverified at the codec level** — needs a runtime round-trip test during step 9 (dev smoke).

If the round-trip fails, fallback is a custom `NcFluidIngredient` component that parses NC's flat tag shape directly. Deferred until verified.

### F. Conditional recipes

`target_chamber_controller_tier_1.json` (source, not generated) uses `"type": "neoforge:conditional"` with a `conditions` array wrapping an inner recipe. KubeJS handles this automatically at the recipe-event level via its conditional codec — no schema-level change needed. Verify that KubeJS doesn't strip the `conditions` wrapper on round-trip during step 9.

### G. Particles — InputParticle field extension required

**Blocking amendment to step 4b:** the in-progress `InputParticle` rewrite adds a `particleName` field but omits `meanEnergy` and `focus`. Extend it to mirror the four particle fields:

```java
public class InputParticle implements IngredientSupplierKJS, JsonSerializable {
    public final Ingredient ingredient;   // placeholder (Items.BARRIER)
    public final int count;               // → "amount" in JSON
    public final String particleName;     // → "particle" in JSON
    public final long meanEnergy;         // → "meanEnergy" in JSON
    public final double focus;            // → "focus" in JSON

    public static final Codec<InputParticle> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("particle").forGetter(ip -> ip.particleName),
        Codec.INT.fieldOf("amount").forGetter(ip -> ip.count),
        Codec.LONG.fieldOf("meanEnergy").forGetter(ip -> ip.meanEnergy),
        Codec.DOUBLE.fieldOf("focus").forGetter(ip -> ip.focus)
    ).apply(instance, InputParticle::new));
}
```

All four fields are required (matching `ParticleStack.fromJSON` semantics). The `Ingredient` placeholder is no longer exposed in the codec — it exists only to satisfy `IngredientSupplierKJS.kjs$asIngredient`.

Same extension applies to `OutputParticle` but it already carries the full `ParticleStack` internally, so its codec just needs to expose the fields. Add `chance` as optional (defaults to `Double.NaN` — encoded only if `hasChance()`).

## §7.1 Per-recipe-type schema matrix (amended)

The original plan used two schemas (base + target_chamber). The audit findings in §6.5 show this is insufficient. The port must build **five schemas** mapped to recipe types by name. Every recipe type in `NcRecipeType.ALL_RECIPES` maps to exactly one of these five schemas.

### Schema-A: generic `timeModifier` schema

Keys:
- `input` (SizedIngredient list, optional)
- `output` (SizedIngredient list, optional)
- `inputFluids` (SizedFluidIngredient list, optional)
- `outputFluids` (SizedFluidIngredient list, optional)
- `timeModifier` (double, optional, default 1.0)
- `powerModifier` (double, optional, default 1.0)
- `radiation` (double, optional, default 1.0)
- `rarityModifier` (double, optional, default 1.0)

Applies to recipe types: **all processors in `Processors.all()` with `hasRecipes() == true`**, plus `fission_reactor_controller`, `msr_controller`, `nc_ore_veins`, `kugelblitz_chamber`, **and `target_chamber` base fields** (target_chamber extends this schema with extras — see Schema-E).

### Schema-B: `heatRequired` schema

Same as Schema-A except:
- Remove `timeModifier`
- Add `heatRequired` (double, optional, default 1.0)

Applies to: `fission_boiling`, `turbine_controller`.

### Schema-C: `coolingRate` schema

Same as Schema-A except:
- Remove `timeModifier`
- Add `coolingRate` (double, optional, default 1.0)

Applies to: `fusion_coolant`, `accelerator_coolant`.

### Schema-D: fusion core schema

Same as Schema-A plus:
- Add `temperature` (double, optional, default 1.0)

Applies to: `fusion_core`. (Keeps `timeModifier` as well.)

### Schema-E: target chamber schema

Same as Schema-A plus:
- Add `inputParticles` (InputParticle list, optional)
- Add `outputParticles` (OutputParticle list, optional)
- Add `crossSection` (double, optional, default 1.0)
- Add `maxEnergy` (long, optional, default 0)

Applies to: `target_chamber`.

### Implementation sketch

Build each schema via a shared helper that takes the timing-field override:

```java
private static RecipeSchema buildSchema(
        String timingKeyName,
        List<RecipeKey<?>> extraKeys
) {
    var keys = new ArrayList<RecipeKey<?>>();
    keys.add(SizedIngredientComponent.FLAT.instance().asList()
        .key("input", ComponentRole.INPUT).defaultOptional());
    keys.add(SizedIngredientComponent.FLAT.instance().asList()
        .key("output", ComponentRole.OUTPUT).defaultOptional());
    keys.add(SizedFluidIngredientComponent.FLAT.instance().asList()
        .key("inputFluids", ComponentRole.INPUT).defaultOptional());
    keys.add(SizedFluidIngredientComponent.FLAT.instance().asList()
        .key("outputFluids", ComponentRole.OUTPUT).defaultOptional());
    keys.add(NumberComponent.doubleRange(-1000, 1000)
        .key(timingKeyName, ComponentRole.OTHER).defaultOptional());
    keys.add(NumberComponent.doubleRange(-1000, 1000)
        .key("powerModifier", ComponentRole.OTHER).defaultOptional());
    keys.add(NumberComponent.doubleRange(-1000, 1000)
        .key("radiation", ComponentRole.OTHER).defaultOptional());
    keys.add(NumberComponent.doubleRange(0, 1000)
        .key("rarityModifier", ComponentRole.OTHER).defaultOptional());
    keys.addAll(extraKeys);
    return new RecipeSchema(keys.toArray(new RecipeKey<?>[0]));
}

var schemaA = buildSchema("timeModifier", List.of());
var schemaB = buildSchema("heatRequired", List.of());
var schemaC = buildSchema("coolingRate", List.of());
var schemaD = buildSchema("timeModifier", List.of(
    NumberComponent.doubleRange(0, 100000)
        .key("temperature", ComponentRole.OTHER).defaultOptional()
));
var schemaE = buildSchema("timeModifier", List.of(
    particleInputComponent.asList()
        .key("inputParticles", ComponentRole.INPUT).defaultOptional(),
    particleOutputComponent.asList()
        .key("outputParticles", ComponentRole.OUTPUT).defaultOptional(),
    NumberComponent.doubleRange(0, 1000)
        .key("crossSection", ComponentRole.OTHER).defaultOptional(),
    NumberComponent.longRange(0, Long.MAX_VALUE)
        .key("maxEnergy", ComponentRole.OTHER).defaultOptional()
));
```

Then in `registerRecipeSchemas(RecipeSchemaRegistry registry)`:

```java
for (String recipeType : NcRecipeType.ALL_RECIPES.keySet()) {
    RecipeSchema schema = switch (recipeType) {
        case "fission_boiling", "turbine_controller" -> schemaB;
        case "fusion_coolant", "accelerator_coolant" -> schemaC;
        case "fusion_core" -> schemaD;
        case "target_chamber" -> schemaE;
        default -> schemaA;
    };
    registry.register(ResourceLocation.fromNamespaceAndPath(MODID, recipeType), schema);
}
```

### Verification checklist for §7.1

Before merging the port, every one of these must pass at runtime (added to step 9 dev smoke tests):

1. Read an existing fission_boiling JSON recipe via KubeJS, do no-op replaceOutput, write it back, diff against original — `heatRequired` must survive.
2. Same for fusion_coolant / accelerator_coolant (`coolingRate`).
3. Same for fusion_core (`temperature` + `timeModifier` both present).
4. Same for nc_ore_veins with a `rarityModifier` recipe (only `platinum.json` in current datagen).
5. Same for target_chamber (if any recipes exist — if not, create one from JS and verify NC's recipe loader accepts it on reload).
6. Create a brand-new recipe of each type from JS and verify NC loads it without error.
7. Fluid tag ingredient round-trip: read a fusion_core recipe that uses `"tag": "c:deuterium"`, round-trip via KubeJS, verify tag is preserved (resolves the §6.5-E open question).
8. Conditional recipe wrapper: read `target_chamber_controller_tier_1.json` via KubeJS, verify the `neoforge:conditional` wrapper survives (don't write anything back — just read).

If any of (1)–(6) fails, the schema for that recipe type is wrong — fix before shipping. If (7) fails, a custom fluid component is required. If (8) fails, enable `.asConditionalList()` on the item/fluid list components.

## Step-by-step execution order

Intended to minimize rework by resolving uncertainties early and keeping the build broken only as long as necessary:

1. **Resolve API uncertainties in a scratch file.** Read `RecipeComponent.java`, `RecipeKey.java`, `RecipeSchema.java`, `RecipeSchemaRegistry.java`, `NumberComponent.java`, `SizedIngredientComponent.java`, `SizedFluidIngredientComponent.java`, `EventGroup.java`, `EventHandler.java`, `KubeJSPlugin.java`, `IngredientWrapper.java` in the checkout. For each, copy the current method signatures into a local notes file. This is a one-hour read-only pass and removes all remaining guesses.
2. **JSON shape decision (§6).** Build a minimal 3-recipe KubeJS script, run in dev with just NC loaded, compare the generated JSON to NC's datagen output. Commit the finding to the plan.
3. **Remove `build.gradle` exclusions** to force the compiler to tell us exactly what breaks. Expect ~40 errors across 6 files. Work top-down, leaving `NuclearCraftKubeJSPlugin.java` for last.
4. **Fix in order, one file at a time, committing between files** (WIP commits are fine — squash at the end if desired):
   1. `ParticleMatch.java` — trivial import update.
   2. `InputParticle.java` — drop `InputReplacement`, move imports, switch exception class.
   3. `OutputParticle.java` — same shape as 4.2.
   4. `ParticleComponents.java` — the biggest rewrite. Register component type, rebuild `INPUT`/`OUTPUT` against the new `RecipeComponent` interface.
   5. Delete `KubeJSRecipesEventJSMixin.java`.
   6. Register `ParticleStackKubeJSMixin` in `nuclearcraft.mixins.json`.
   7. `NCKubeJsEvents.java` — rewrite events, switch base interfaces, move `generateCustomFuelRecipes` return path to inject into the JSON map.
   8. `NuclearCraftKubeJSPlugin.java` — rebuild against new plugin API.
5. **Add `kubejs.plugins.txt`.**
6. **Wire the two bridge sites** in `BlackHoleBE` and `FissionFuel`.
7. **`./gradlew compileJava`** — should be clean.
8. **`./gradlew runData`** — datagen must still run (plugin should not interfere with datagen environment).
9. **Dev runtime smoke tests** (dev client with KubeJS in `runs/client/mods/`):
   - Write a script that creates a manufactory recipe from JS. Craft it in-game. Verify the output is what the script declared.
   - Write a `event.remove({type:'nuclearcraft:manufactory'})` script. Verify the target recipe is gone.
   - Write a `replaceOutput` script that rewires a manufactory recipe's output. Verify.
   - Write a script that calls `NCKJSEvents.RegisterFissionFuel` (if the old scripting surface can be preserved — otherwise just exercise the Java path by verifying the bridge is reached with a log line).
   - Drop a player into a black hole. Verify the `PlayerEnterBlackhole` event fires in KubeJS.
   - Create a target chamber recipe from JS with a particle input and particle output. Verify it deserializes and runs.
10. **Pack-level smoke test on ramdisk server** with the actual FTB Evolution scripts loaded. Confirm the existing `kubejs/server_scripts/` unification work still runs (it does not depend on NC's plugin, but this catches any regression caused by the plugin registering badly).
11. **Update PR #277 body** to remove the "KubeJS excluded — no 1.21.1 port" language and accurately describe the now-live integration. This becomes a separate small commit, independent of the porting commits.

## Risk register

| Risk | Severity | Mitigation |
|---|---|---|
| JSON shape mismatch between KubeJS `SizedIngredient` and NC `ItemStackIngredient` | **High** — blocks recipe creation from JS | §6 decision step before committing to an approach |
| `RecipeComponentType` registration order / lifecycle collision with NC's `RecipeType` registration | Medium | Register components in `registerRecipeComponents`, not in static initializer of `NuclearCraftKubeJSPlugin`. Keep `NcRecipeType.ALL_RECIPES` initialization strictly before KubeJS plugin loads (it's a mod-bus `RegisterEvent` so ordering is already enforced) |
| `ParticleComponents` codec round-trip loses data (`meanEnergy`, `focus`) because `RecordCodecBuilder` field order differs from current hand-rolled JSON | Medium | Port the existing `writeToJson` / `readFromJson` bodies verbatim rather than introducing a `RecordCodecBuilder` — `RecipeComponent.codec()` can delegate to `Codec.of(encoder, decoder)` built from them |
| `ConsoleJS.getCurrent(null)` throwing at startup because no Rhino Context is active | Low | Use direct `ConsoleJS.SERVER` field access for warnings at non-script-time call sites |
| Loading NC in an environment without KubeJS triggers `NoClassDefFoundError` on `NCKubeJsEvents` | Low | `kubejs.plugins.txt` gates plugin load; bridge sites in BlackHoleBE / FissionFuel are guarded by `ModList.get().isLoaded("kubejs")` |
| Runtime recipe injection via `beforeRecipeLoading` happens before NC's own serializers run, so synthesized fission fuel JSON must be the pre-parse form | Low | The existing `generateFuelRecipe` already builds `JsonObject`, which is the pre-parse form. Wire it directly into `recipeJsons` |
| `kjs$` prefix convention dropped in 2101.x | Low | Verified present in `core/CustomIngredientKJS.kjs$asIngredient` — convention is still honored. No mitigation needed |
| Particle matching logic (`ParticleMatch`) never exercised because KubeJS's `replaceInput` / `replaceOutput` doesn't route through custom components for unknown ingredient-match types | Medium | Acceptance: particle replacement from JS is a bonus, not a requirement. The primary use-case is creating and removing target_chamber recipes wholesale, which works without `ParticleMatch` ever firing |

## Out of scope — do not attempt in this port

- Exposing NC's `FluidStackIngredient` JSON shape as a full custom KubeJS component. Use SizedFluidIngredient or an adapter; do not rebuild the ingredient layer.
- Supporting `event.recipes.nuclearcraft.X.modify(...)` or other bespoke script ergonomics beyond what default `KubeRecipe` + `RecipeSchema` already provides.
- Rewriting pack-level scripts in FTB Evolution to use NC's plugin bindings. Those scripts are pack content, not mod content; they stay as-is.
- Replacing the `Items.BARRIER` placeholder ingredient in `InputParticle.createIngredientFromParticle` with a real item. That's a pre-existing kludge and should stay kludged — it's never actually used for matching at runtime.

## What this plan does NOT cover

- The PR #277 body update. That is a separate task, blocked on this plan's completion so the new language can accurately describe what shipped.
- Verification that NC's existing KubeJS pack scripts (in `ftb-evolution-server/server/kubejs/`) need any changes. They should not — they don't use NC's plugin bindings today.
