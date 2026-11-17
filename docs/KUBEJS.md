# KubeJS support

Reference for NuclearCraft: Neoteric on NeoForge 1.21.1, based on the integration in this repository. Covers the NuclearCraft event API and recipe functions; general KubeJS APIs are outside its scope. Examples are illustrative and have not been run in game.

## Script locations

| Location | Purpose | Applying changes |
|---|---|---|
| `kubejs/startup_scripts/` | Register or change fuels, heat sinks, and particle sources | Restart the game/server |
| `kubejs/server_scripts/` | Add, remove, and replace recipes inside `ServerEvents.recipes` | Reload server recipes, for example with `/reload` |

The startup events are posted after startup scripts load, in this order: fuels, heat sinks, particle sources. New fuels and heat sinks collected by each event are registered after its callbacks finish. Keep the returned definition when configuring a newly registered entry; `override` looks up already registered entries.

Use lowercase registry-safe names. Fuel groups, fuel names, and heat sink names are local names, not namespaced IDs. Item/fluid arguments use full IDs such as `minecraft:iron_ingot`. Particle source methods also accept particle names without a namespace and assume `nuclearcraft`.

## All NuclearCraft events

These three events belong in startup scripts:

| Event | Purpose |
|---|---|
| `NuclearCraftEvents.registerFissionFuel(event => { ... })` | Add fuel families or change/disable existing families |
| `NuclearCraftEvents.registerHeatSink(event => { ... })` | Add heat sinks or change/disable existing heat sinks |
| `NuclearCraftEvents.registerParticleSource(event => { ... })` | Assign items and fluids as particle sources, replace assignments, or remove assignments |

Recipes use KubeJS's `ServerEvents.recipes(event => { ... })`. NuclearCraft does not add separate recipe events or custom KubeJS registry builder types.

## Fission fuels

### Event functions

| Function | Return / behavior |
|---|---|
| `event.register(group, name)` | Returns a new `FuelDef` to configure; queues its registration |
| `event.register(def)` | Queues an existing Java `FuelDef`; returns nothing |
| `event.override(group, name, def => { ... })` | Mutates an existing family's base definition and recalculates its variants |
| `event.remove(group, name)` | Marks an existing family disabled; does not unregister its items/fluids |
| `event.getCollected()` | Returns the Java list of new definitions queued by this event |

Unknown names passed to `remove` or `override` are ignored. Registering an existing group/name does not replace it; use `override`.

### FuelDef functions

All configuration functions return the same definition for chaining.

| Function | Value |
|---|---|
| `.forgeEnergy(int)` | Base Forge Energy parameter |
| `.heat(double)` | Base heat parameter |
| `.criticality(int)` | Base criticality parameter |
| `.depletion(int)` | Base depletion duration, in ticks |
| `.efficiency(int)` | Base efficiency parameter |
| `.isotopes(int a, int b)` | Two isotope mass numbers stored as metadata |
| `.recipeModifiers(double time, double power, double radiation)` | Recipe modifier metadata; defaults to `1.0, 1.0, 1.0` |
| `.isSpecial()` | Returns whether the group matches `xenorium.*` or `quantite.*` |
| `.variants()` | Returns a Java map from variant suffix to derived `FuelDef` |

The numeric fuel parameters default to zero, and isotopes default to `[0, 0]`. Set the operating parameters explicitly for new fuels. The fields `forgeEnergy`, `heat`, `criticality`, `depletion`, `efficiency`, `isotopes`, `timeModifier`, `powerModifier`, and `radiationModifier` are also public. `group` and `name` are final. Recipe modifiers are metadata, not a request to generate recipes.

A normal family creates fuel and depleted items for the base, `_ox` (oxide), `_ni` (nitride), `_za` (zirconium alloy), and `_tr` (TRISO) variants. All except TRISO also get fluid forms. Special families get only the base item/fluid forms. Variant statistics are derived automatically; `override` recalculates them from the base.

Item IDs follow `nuclearcraft:fuel_<group>_<name>`, replacing hyphens in the fuel name with underscores. Depleted item IDs add `depleted_` before `fuel_`; variant suffixes come last. Registration supplies content entries; provide the recipes, models, textures, and translations needed by your pack separately. Disabling a family preserves registry IDs and hides it from the creative listing; remove unwanted recipes separately.

```js
// kubejs/startup_scripts/nc_fuels.js
NuclearCraftEvents.registerFissionFuel(event => {
  event.register('custom', 'example-235')
    .forgeEnergy(300)
    .heat(55)
    .criticality(100)
    .depletion(240)
    .efficiency(100)
    .isotopes(235, 238)
    .recipeModifiers(1.0, 1.0, 1.0)

  // Creates, among others:
  // nuclearcraft:fuel_custom_example_235
  // nuclearcraft:fuel_custom_example_235_ox
  // nuclearcraft:depleted_fuel_custom_example_235

  event.override('uranium', 'leu-235', fuel => {
    fuel.forgeEnergy(280).heat(45)
  })

  event.remove('plutonium', 'hep-241')
})
```

For the `register(def)` overload, create a definition with `Java.loadClass('igentuman.nc.content.fuel.FuelDef')` and `new FuelDef(group, name)`. Its other public constructor takes `(group, name, forgeEnergy, heat, criticality, depletion, efficiency)`.

## Heat sinks

### Event and definition functions

| Function | Return / behavior |
|---|---|
| `event.register(name, heat, rules)` | Returns a new `HeatSinkDef`; `rules` is an array of strings |
| `event.override(name, def => { ... })` | Mutates an existing definition |
| `event.remove(name)` | Disables the heat sink; preserves its block/item registry entries |
| `event.getCollected()` | Returns the Java list of queued definitions |
| `def.heat(double)` | Sets cooling; returns the definition |
| `def.rules(String... rules)` | Replaces placement rules; returns the definition |
| `def.isActive()` | Returns whether the name starts with `active_` |
| `def.getPlacementRule()` | Returns a translated Minecraft text component describing the rules |
| `def.getValidator()` | Returns the lazily initialized Java validator |

`name`, `heat`, and `rules` are public fields. Keep the registered name stable. Validator helpers `.blockLines()` and `.blocks()` expose Java maps of parsed conditions and resolved blocks; these are inspection helpers, not additional registration functions. Configure rules during startup before querying the validator, which caches its parsed rules.

The block/item ID is `nuclearcraft:<name>_heat_sink`. Use the name without `_heat_sink` in event methods. Unknown names in `remove`/`override` are ignored; registering a name already present does not override it. Provide assets and crafting recipes for a new heat sink separately.

### Placement rule syntax

Rules check the six face-adjacent blocks. Separate array entries are AND conditions; `|` combines alternative block IDs or tags within one condition. Bare block names assume the `nuclearcraft` namespace. A passive `*_heat_sink` reference also includes its corresponding `active_*_heat_sink` block.

| Rule | Meaning |
|---|---|
| `fission_reactor_solid_fuel_cell` | At least one qualifying fuel-cell neighbor |
| `#nuclearcraft:moderators>2` | At least two qualifying moderators (`>` means **at least**) |
| `water_heat_sink=1` | Exactly one qualifying water heat sink |
| `water_heat_sink<2` | Fewer than two qualifying water heat sinks |
| `lapis_heat_sink-2` | Between qualifying lapis heat sinks on opposite faces |
| `#nuclearcraft:fission_reactor_casing^3` | Three qualifying casing neighbors in a corner arrangement |
| `water_heat_sink|redstone_heat_sink` | At least one qualifying water or redstone heat sink |

Neighbor heat sinks must themselves be valid, and moderators must be active to qualify. The parser reads only the final digit as the count and clamps it to at least one: use counts `1`-`6`, not zero or multi-digit counts. `-` checks an opposite pair. An empty rule array has no placement conditions.

```js
// kubejs/startup_scripts/nc_heat_sinks.js
NuclearCraftEvents.registerHeatSink(event => {
  event.register('example', 140, [
    'fission_reactor_solid_fuel_cell',
    '#nuclearcraft:moderators'
  ])

  event.override('water', sink => {
    sink.heat(75)
    sink.rules('fission_reactor_solid_fuel_cell')
  })

  event.remove('cryotheum')
})
```

## Particle sources

These functions assign existing items/fluids to the particle source catalog. They do not create items, fluids, or particle species. Registering the same item/fluid ID again replaces its catalog assignment.

| Function | Defaults / meaning |
|---|---|
| `event.item(itemId, particle, capacity)` | Energy `0` keV, focus `0.4` |
| `event.item(itemId, particle, capacity, energyKeV)` | Focus `0.4` |
| `event.item(itemId, particle, capacity, energyKeV, focus)` | Full item source definition |
| `event.fluid(fluidId, particle)` | `10000` particles/mB, energy `0` keV, focus `0.4` |
| `event.fluid(fluidId, particle, particlesPerMillibucket)` | Energy `0` keV, focus `0.4` |
| `event.fluid(fluidId, particle, particlesPerMillibucket, energyKeV, focus)` | Full fluid source definition |
| `event.removeItem(itemId)` | Removes an item catalog assignment |
| `event.removeFluid(fluidId)` | Removes a fluid catalog assignment |

There is no four-argument fluid overload. Capacity, particles/mB, and energy are Java `long` values; focus is a non-negative finite `double`. Use positive capacities and non-negative energies. Keep JavaScript integer literals within the exact integer range (`2^53 - 1`).

```js
// kubejs/startup_scripts/nc_particle_sources.js
NuclearCraftEvents.registerParticleSource(event => {
  event.item('minecraft:iron_ingot', 'electron', 1000000, 0, 0.5)
  event.fluid('nuclearcraft:hydrogen', 'proton', 15000, 0, 0.6)
  event.removeItem('nuclearcraft:source_iridium_192')
  event.removeFluid('nuclearcraft:diborane')
})
```

## Processor recipes

Call `event.recipes.nuclearcraft.<type>(...)` inside `ServerEvents.recipes`. All processor functions below use this argument order:

```js
event.recipes.nuclearcraft.manufactory(
  itemInputs, fluidInputs, itemOutputs, fluidOutputs, processTime, energyPerTick
)
```

| Key | Type | Default |
|---|---|---|
| `item_inputs` | List of sized item ingredients | `[]` |
| `fluid_inputs` | List of sized fluid ingredients | `[]` |
| `item_outputs` | List of NuclearCraft item outputs | `[]` |
| `fluid_outputs` | List of NuclearCraft fluid outputs | `[]` |
| `process_time` | Integer ticks | Required |
| `energy_per_tick` | Integer FE/t | Required |

Supply all six arguments in positional calls, using `[]` for unused input/output categories. At 20 TPS, 200 ticks is 10 seconds before machine modifiers. Fit the recipe within the machine's item slots and fluid tanks. Machines such as the pump, leacher, analyzer, nuclear furnace, and gas scrubber also have specialized behavior.

### All processor function names and slot counts

Each name is a function under `event.recipes.nuclearcraft` and a recipe type under `nuclearcraft:`. Counts describe the configured input/output slots or tanks, excluding catalyst slots.

| Function / type | Item in | Fluid in | Item out | Fluid out |
|---|---:|---:|---:|---:|
| `alloy_smelter` | 2 | 0 | 1 | 0 |
| `analyzer` | 1 | 0 | 1 | 0 |
| `assembler` | 6 | 0 | 1 | 0 |
| `centrifuge` | 0 | 1 | 0 | 6 |
| `chemical_reactor` | 0 | 2 | 0 | 2 |
| `crystallizer` | 0 | 1 | 1 | 0 |
| `decay_hastener` | 1 | 0 | 1 | 0 |
| `electrolyzer` | 0 | 1 | 0 | 4 |
| `extractor` | 1 | 0 | 1 | 1 |
| `fluid_enricher` | 1 | 1 | 0 | 1 |
| `fluid_infuser` | 1 | 1 | 1 | 0 |
| `fuel_reprocessor` | 1 | 0 | 8 | 0 |
| `gas_scrubber` | 0 | 1 | 0 | 1 |
| `ingot_former` | 0 | 1 | 1 | 0 |
| `irradiator` | 1 | 1 | 1 | 1 |
| `isotope_separator` | 1 | 0 | 2 | 0 |
| `leacher` | 1 (internal) | 1 | 0 | 1 |
| `manufactory` | 1 | 0 | 1 | 0 |
| `melter` | 1 | 0 | 0 | 1 |
| `nuclear_furnace` | 2 | 0 | 1 | 0 |
| `pressurizer` | 1 | 0 | 1 | 0 |
| `pump` | 1 | 0 | 0 | 1 |
| `rock_crusher` | 1 | 0 | 3 | 0 |
| `steam_turbine` | 0 | 1 | 0 | 1 |
| `subatomic_liquifier` | 1 | 1 | 0 | 1 |
| `supercooler` | 0 | 1 | 0 | 1 |

`steam_turbine` is the single-block generator; `turbine` is a separate multiblock recipe type.

```js
// kubejs/server_scripts/nc_processors.js
ServerEvents.recipes(event => {
  event.recipes.nuclearcraft.manufactory(
    ['minecraft:cobblestone'], [], ['minecraft:gravel'], [], 100, 20
  ).id('kubejs:nc/cobblestone_to_gravel')

  event.recipes.nuclearcraft.alloy_smelter(
    ['minecraft:iron_ingot', 'minecraft:gold_ingot'], [],
    ['2x minecraft:copper_ingot'], [], 200, 40
  ).id('kubejs:nc/example_alloy')

  event.recipes.nuclearcraft.melter(
    ['minecraft:ice'], [], [], [Fluid.of('minecraft:water', 1000)], 100, 20
  ).id('kubejs:nc/melt_ice')

  // Remove a recipe by its ID (replace this with the ID you want to remove):
  // event.remove({ id: 'kubejs:nc/example_alloy' })
  // To clear a machine's recipes before adding replacements:
  // event.remove({ type: 'nuclearcraft:manufactory' })
})
```

### Input and output formats

The direct functions accept KubeJS sized ingredient/stack values. The following object formats are the serialized forms used by `event.custom`:

| Value | Object example |
|---|---|
| Item input | `{ item: 'minecraft:iron_ingot', count: 2 }` |
| Tagged item input | `{ tag: 'c:ingots/iron', count: 2 }` |
| Fluid input | `{ fluid: 'minecraft:water', amount: 1000 }` |
| Tagged fluid input | `{ tag: 'c:water', amount: 1000 }` |
| Item output | `{ item: 'minecraft:iron_ingot', count: 2 }` |
| Tagged item output | `{ tag: 'c:ingots/iron', count: 2 }` |
| Fluid output | `{ fluid: 'minecraft:water', amount: 1000 }` |
| Tagged fluid output | `{ tag: 'c:water', amount: 1000 }` |

Amounts are in millibuckets; output `count` defaults to `1`, output fluid `amount` to `1000`. Use either a concrete ID or a tag in each output object. Tag output members are resolved using the configured mod priority; an empty tag cannot supply a usable output. See [configuration](CONFIGURATION.md).

Direct output components also support `'2x #c:ingots/iron'` and `'1000x #c:water'`. Bare output tags default to one item or 1000 mB. NuclearCraft item outputs store the item and count; they do not preserve arbitrary stack components. Fluid outputs likewise store fluid and amount. There is no chance field on these processor outputs.

```js
ServerEvents.recipes(event => {
  event.custom({
    type: 'nuclearcraft:manufactory',
    item_inputs: [{ item: 'minecraft:raw_iron', count: 1 }],
    fluid_inputs: [],
    item_outputs: [{ tag: 'c:ingots/iron', count: 2 }],
    fluid_outputs: [],
    process_time: 200,
    energy_per_tick: 40
  }).id('kubejs:nc/raw_iron_to_ingots')
})
```

## Particle, coolant, and Kugelblitz recipe functions

These are the five additional explicitly registered recipe functions. Names below are under `event.recipes.nuclearcraft`; arguments are listed in schema order. Defaults refer to omitted optional keys. For positional calls, pass every argument shown to avoid ambiguity around optional arguments. `event.custom` with the corresponding `nuclearcraft:<name>` type accepts the keys listed below.

| Function | Arguments / keys in order |
|---|---|
| `target_chamber` | `particle_input`, `cross_section`, `item_outputs = []`, `fluid_outputs = []`, `particle_outputs = []`, `item_inputs = []`, `fluid_inputs = []`, `released_energy_kev = 0`, `priority = 0` |
| `decay_chamber` | `particle_input`, `particle_outputs = []`, `cross_section`, `released_energy_kev = 0`, `priority = 0` |
| `collision_chamber` | `particle_inputs`, `particle_outputs = []`, `cross_section`, `released_energy_kev = 0`, `ordered_inputs = false`, `priority = 0` |
| `accelerator_coolant` | `input_fluid`, `output_fluid`, `heat_per_mb`, `minimum_temperature_k` (optional) |
| `kugelblitz_chamber` | `input`, `output`, `time_modifier = 1.0`, `power_modifier = 1.0` |

For raw `event.custom` decay/collision recipes, include `particle_outputs` explicitly: their serializers require the array even though the KubeJS schemas default it to `[]`. Collision recipes require exactly two particle inputs; `ordered_inputs: false` accepts either beam order. Particle recipe priority is an integer; cross section is a double and released energy is a long in keV. Accelerator coolant `heat_per_mb` and optional temperature in kelvin are longs; omitting the temperature leaves it unspecified. Kugelblitz `input` is a sized item ingredient and `output` is an item output.

### Particle value formats

Particle ingredients and stacks use objects, not item/fluid shorthand strings. Species must exist in NuclearCraft's particle registry. Use full namespaced particle IDs in recipe objects.

| Particle ingredient key | Meaning / default |
|---|---|
| `species` | Required non-empty array of accepted particle IDs |
| `amount` | Required positive particle count (long) |
| `minimum_energy` | Minimum energy per particle in keV; default `0` |
| `maximum_energy` | Required maximum energy per particle in keV; at least the minimum |
| `minimum_focus` | Minimum focus; default `0.0`, finite and non-negative |

| Particle output key | Meaning / default |
|---|---|
| `particle` | Particle ID; required for a non-empty stack |
| `amount` | Required non-negative particle count (long) |
| `energy` | Energy per particle in keV; default `0`, non-negative |
| `focus` | Default `0.0`, finite and non-negative |

```js
// kubejs/server_scripts/nc_particle_recipes.js
ServerEvents.recipes(event => {
  const neutron = {
    species: ['nuclearcraft:neutron'],
    amount: 100,
    minimum_energy: 0,
    maximum_energy: 1000000,
    minimum_focus: 0.1
  }

  event.recipes.nuclearcraft.decay_chamber(
    neutron,
    [{ particle: 'nuclearcraft:proton', amount: 100, energy: 100, focus: 0.4 }],
    0.5, 0, 0
  ).id('kubejs:nc/example_decay')

  event.custom({
    type: 'nuclearcraft:target_chamber',
    particle_input: neutron,
    item_inputs: [{ item: 'minecraft:iron_ingot', count: 1 }],
    item_outputs: [{ item: 'minecraft:gold_ingot', count: 1 }],
    particle_outputs: [],
    cross_section: 0.25,
    released_energy_kev: 0,
    priority: 0
  }).id('kubejs:nc/example_target')

  event.custom({
    type: 'nuclearcraft:collision_chamber',
    particle_inputs: [neutron, neutron],
    particle_outputs: [
      { particle: 'nuclearcraft:proton', amount: 100, energy: 100, focus: 0.4 }
    ],
    cross_section: 0.25,
    ordered_inputs: false
  }).id('kubejs:nc/example_collision')

  event.recipes.nuclearcraft.accelerator_coolant(
    Fluid.of('minecraft:water', 1),
    Fluid.of('nuclearcraft:steam', 1),
    100, 300
  ).id('kubejs:nc/example_accelerator_coolant')

  event.recipes.nuclearcraft.kugelblitz_chamber(
    'minecraft:cobblestone', 'minecraft:diamond', 2.0, 1.5
  ).id('kubejs:nc/example_kugelblitz')
})
```

These examples demonstrate formats, not balanced processing chains or physical reactions.

## Recipe builder functions

KubeJS generates fluent setters from the schema keys, converting snake case to camel case. Each setter takes the same value type as its schema key and returns the recipe builder. Only use setters belonging to the selected recipe schema.

| Schema | Available setters |
|---|---|
| All universal processors | `.itemInputs(list)`, `.fluidInputs(list)`, `.itemOutputs(list)`, `.fluidOutputs(list)`, `.processTime(int)`, `.energyPerTick(int)` |
| Target chamber | `.particleInput(ingredient)`, `.crossSection(double)`, `.itemOutputs(list)`, `.fluidOutputs(list)`, `.particleOutputs(list)`, `.itemInputs(list)`, `.fluidInputs(list)`, `.releasedEnergyKev(long)`, `.priority(int)` |
| Decay chamber | `.particleInput(ingredient)`, `.particleOutputs(list)`, `.crossSection(double)`, `.releasedEnergyKev(long)`, `.priority(int)` |
| Collision chamber | `.particleInputs(list)`, `.particleOutputs(list)`, `.crossSection(double)`, `.releasedEnergyKev(long)`, `.orderedInputs(boolean)`, `.priority(int)` |
| Accelerator coolant | `.inputFluid(ingredient)`, `.outputFluid(output)`, `.heatPerMb(long)`, `.minimumTemperatureK(long)` |
| Kugelblitz chamber | `.input(ingredient)`, `.output(output)`, `.timeModifier(double)`, `.powerModifier(double)` |

For example:

```js
ServerEvents.recipes(event => {
  event.recipes.nuclearcraft.manufactory(
    ['minecraft:cobblestone'], [], ['minecraft:gravel'], [], 100, 20
  )
    .processTime(160)
    .energyPerTick(30)
    .id('kubejs:nc/slower_gravel')
})
```

The `.id(resourceId)` function, `event.remove(filter)`, and `event.custom(json)` used throughout this guide are standard KubeJS functions, not additional NuclearCraft events. The registered schemas do not define extra custom builder methods beyond the setters above.

## Other recipe types via event.custom

The following serializers are available but have no dedicated schema in the NuclearCraft KubeJS plugin. Use `event.custom({ type: 'nuclearcraft:<type>', ... })`. All fields are required unless a default is shown. Input/output objects use the formats above; an unsized item ingredient has no count.

| Type | Fields |
|---|---|
| `fission_reactor` | `input` (unsized item ingredient), `output` (item output), `process_time` (int), `power` (int), `heat` (int) |
| `fission_boiling` | `input_fluid`, `output_fluid`, `heat_required` (int) |
| `fusion_reactor` | `input_a`, `input_b` (sized fluid ingredients), `outputs` (fluid output array), `energy` (int), `optimal_temperature` (double), `process_time` (int) |
| `fusion_coolant` | `input_fluid`, `output_fluid`, `cooling_rate` (int) |
| `turbine` | `input_fluid`, `output_fluid`, `power_modifier` (double) |
| `heat_exchanger` | `input_fluid`, `output_fluid`, `heat` (int) |
| `nuclear_blast` | `input` (unsized item ingredient), `output` (item output), `chance` (double, default `1.0`) |
| `nc_ore_veins` | `id` (vein resource ID), `ores` (array of `{ item: <sized item ingredient>, weight: <int> }`), `rarity` (double, default `1.0`) |

Only entries using the universal processor serializer receive the processor schema. Use `event.custom` for `nc_ore_veins`, which uses the vein fields above. The internal vein `id` field and the recipe ID assigned by `.id(...)` are separate values.

```js
// kubejs/server_scripts/nc_custom_recipes.js
ServerEvents.recipes(event => {
  event.custom({
    type: 'nuclearcraft:heat_exchanger',
    input_fluid: { fluid: 'minecraft:water', amount: 100 },
    output_fluid: { fluid: 'nuclearcraft:steam', amount: 100 },
    heat: 1000
  }).id('kubejs:nc/example_heat_exchanger')

  event.custom({
    type: 'nuclearcraft:nc_ore_veins',
    id: 'kubejs:example_iron_vein',
    ores: [
      { item: { item: 'minecraft:iron_ore', count: 1 }, weight: 4 },
      { item: { item: 'minecraft:gold_ore', count: 1 }, weight: 1 }
    ],
    rarity: 1.0
  }).id('kubejs:nc/example_iron_vein')
})
```

## Recipe components and source reference

The plugin registers four recipe component IDs: `nuclearcraft:item_output`, `nuclearcraft:fluid_output`, `nuclearcraft:particle_stack`, and `nuclearcraft:particle_ingredient`. These define value conversion for schemas; they are not additional global JavaScript functions.