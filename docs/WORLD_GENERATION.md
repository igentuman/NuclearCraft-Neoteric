# World generation

NuclearCraft: Neoteric adds the Wasteland dimension and biome, ruined structures,
a Nuclear Scientist villager profession, and ores for the nuclear-tech progression.
This guide describes the current Minecraft 1.21.1 implementation and its defaults.
Modpacks can change recipes, tags, world-generation data, and configuration.

## The Wasteland

The dimension and its custom biome both use the ID `nuclearcraft:wasteland`.
The dimension's biome source includes the Wasteland and vanilla Desert. The
Wasteland biome is provided through this dimension; the mod does not add it to
the default Overworld biome source.

The Wasteland is hot and dry, with no precipitation, muted sky and vegetation
colors, discolored water, dead bushes, and its own background music. Spiders and
Feral Ghouls appear in its monster spawn list. Underground generation includes
vanilla ores, caves, lakes, and other default underground features.

The dimension generates terrain from Y=0 through Y=255, with sea level at Y=35.
Its surface uses Wasteland Earth over coarse dirt, with bedrock at the bottom.
It has a skylight, a day/night cycle, and a coordinate scale of 1:1 with the
Overworld. Beds work; respawn anchors do not.

### Getting there and returning

Use a **Wasteland Portal** block (`nuclearcraft:wasteland_portal`). The default
processing route is:

1. Process coarse dirt in an **Irradiator** to obtain Wasteland Earth, one for one.
2. Process **32 Wasteland Earth** in a **Pressurizer** to obtain one portal block.
3. Place the block and walk into it to travel. No portal frame or ignition is required.

Walking into a portal outside the Wasteland sends the player to the Wasteland.
Using one inside the Wasteland sends the player to the Overworld. Travel applies
to players who are not riding another entity.

The destination is a surface position at approximately the same X/Z coordinates,
one block north of the source portal. Travel does not create a matching portal
at the destination or remember a linked return portal. Bring a second portal
block to establish a return route, or find a generated portal.

### Ruins and other structures

Wasteland-tagged biomes receive scattered structures and decoration:

| Feature | Examples | Default placement attempt |
| --- | --- | --- |
| Ruins | Foundations, hangars, ruined camps, bunkers, vehicles, supply crates, and industrial remains | On average once per 250 chunks |
| Decoration | Dead trees, rubble, small ruins, and coal remains | On average once per 30 chunks |
| Portal | A portal template | On average once per 400 chunks |
| Boss lair | A dedicated Wasteland boss-lair template | On average once per 800 chunks |

These rates describe feature attempts, not guaranteed structures. Biome checks,
available templates, and terrain can prevent placement. Scattered ruins and
decoration reject terrain with more than five blocks of height variation across
their sampled footprint; bunker templates are placed partly underground.
The portal template also belongs to the random ruins template pool.

Wasteland villages are a separate jigsaw structure,
`nuclearcraft:wasteland_village`, using road and ruined-house templates. Their
structure set uses a spacing of 32 chunks and a separation of 10 chunks. These
values control candidate placement, rather than guaranteeing a village every
32 chunks.

The Wasteland also includes a Feral Ghoul Boss (`nuclearcraft:feral_ghoul_boss`)
and environmental anomalies. Anomalies are activated around players in the
Wasteland dimension at runtime, rather than being fixed chunk-generation
features. Their types, spawn settings, effects, shards, and crystal buffs are
configured under `anomalies` in `common.toml`.

Radiation simulation and protection are supplied by the separate **Nuclear
Radiation** mod. The Radioactive anomaly also deals direct damage, as described
below. See the [configuration guide](CONFIGURATION.md) for the distinction
between the two mods' settings.

## Anomalies and counterplay

Anomalies are persistent environmental hazards. Ordinary weapons and explosions
cannot damage them, and Peaceful difficulty does not remove them. Each type has
its own particles, ambient sound, and visual effect. The visible particle cloud
can be much smaller than the area affected, so identify a hazard before
approaching it.

The table uses default settings. Distances are measured from the anomaly's
center, which normally hovers above the terrain. Electric, Radioactive, Burning,
Psycho, and Teleporting effects do not require line of sight; a wall alone does
not block their range checks.

| Type | Recognition and danger | Counterplay |
| --- | --- | --- |
| Gravitational | Blue appearance and portal particles. Pulls entities inward, randomly launches living targets, damages them within 2 blocks, and consumes nearby items and terrain. Its initial 12-block pull radius grows as it absorbs blocks. | Feed it expendable blocks until it reaches the absorption threshold, then stay clear of the collapse explosion. See the feeding rules below. |
| Electric | Pale-blue sparks and lightning strikes. Every 50 ticks (2.5 seconds), selects one living target within 28 blocks and deals 16 damage before mitigation. | Place an energy store nearby with at least **100,000,000 FE of free capacity** and an input-enabled face. The anomaly discharges into it and disappears. |
| Radioactive | Green particles. Every 20 ticks (1 second), damages living targets within 48 blocks; damage rises from approximately 2 at the edge toward 20 at the center, before mitigation. | Keep outside the 48-block radius and route around it. **There is currently no implemented neutralization method**, including the Q36. |
| Burning | Orange flames. Within 10 blocks, ignites living targets for 5 seconds and deals 2-6 fire damage every second, increasing toward the center. Can ignite terrain and produce explosions every 10-20 seconds. | Place at least **8 water source blocks** within 3 blocks on each axis of its center. Flowing water does not count. Fire Resistance helps against fire damage while setting up, but does not remove the explosion hazard. |
| Psycho | Purple witch particles. Refreshes Blindness, Nausea, Weakness, and Slowness on survival players within 48 blocks every 2 seconds. Also summons Vexes while players are nearby, up to 6 nearby by default. | Hit the anomaly with the **Q36 Quantite Disruptor** to resolve it. Retreat beyond 48 blocks if unprepared. Clearing status effects while still inside only provides temporary relief because they are reapplied. |
| Teleporting | Pink appearance and portal particles. Checks for living targets within 5 blocks every half-second and teleports them roughly 32-100 blocks away. The anomaly itself blinks around its anchor every 3-6 seconds, up to 12 blocks away horizontally. | Hit it with the **Q36 Quantite Disruptor**. Keep clear of its whole roaming area, rather than only its last position. Bring fall protection: destinations can be above ground. |

### Gravitational: overload it, then withdraw

The default collapse threshold is **320 absorbed blocks**. The anomaly removes
nearby terrain periodically, so it may already have progress when you find it.
Dropping cheap block items into its collection area also adds progress, with
an important counting rule: **each dropped item entity adds one**, regardless
of the number of blocks in its stack. A dropped stack of 64 counts as one;
separately dropped items can merge, so do not assume dropping 320 blocks in
bulk completes the threshold. Non-block items are destroyed without adding
progress. Keep valuable loose items away.

As absorption increases, both pull strength and radius increase. With defaults,
the radius can approach **24 blocks** before collapse. The collection area for
dropped items is a box extending that radius on each axis, so loose items near
its corners can be consumed even outside the spherical pull range.

On reaching the threshold, the anomaly collapses with an explosion of default
power **18**, then drops its reward. Use expendable terrain and withdraw well
before the final absorption; the pull radius is not a safe blast-distance
marker. Avoid attempting this beside a base or valuable structures.

### Electric: provide empty storage capacity

The energy store must be within **13 blocks on X/Z and 8 blocks vertically**
of the anomaly's block position. At least one face must expose an FE capability
that accepts input. A full battery does not qualify: the check uses free
capacity in one storage block, rather than adding capacity across a network.

The anomaly attempts to insert the configured discharge amount and resolves
when it finds qualifying storage. The store's receive limit can reduce the
energy actually accepted, so 100 million FE is the required free space, not a
guaranteed energy reward. No lightning rod is required. Set up the storage from
outside the strike radius where possible, then approach only as needed.

### Burning: surround the actual center with water sources

The water check covers a **7×7×7 box centered on the anomaly's block position**.
Its center floats above the surface, so a distant pool or a stream passing
nearby may not qualify. Arrange eight source blocks inside that box and keep
them present until the anomaly resolves; it checks roughly every half-second.
Fire Resistance can help with placement, but retreat from explosions and
remaining fires even after neutralization.

### Radioactive: avoidance is the current counterplay

This anomaly currently applies direct `nuclearcraft:radiation` damage. Its
`dose` setting gates whether damage is applied when positive; it is not passed
to Nuclear Radiation as an accumulated dose by this implementation. Do not
assume RadAway or radiation shielding neutralizes the anomaly or prevents its
direct damage. Mark the location and give it a wide berth.

### Rewards and persistence

Successfully resolving an anomaly through its counterplay drops **1-2 Resonite
Shards** by default. Natural anomalies resolved this way are recorded as
destroyed for their placement cell, preventing the spawn manager from creating
them there again. Leaving and returning does not by itself clear a hazard.

Craft **9 shards** into a **Resonite Crystal**, then process it in an **Analyzer**
to reveal its rarity and effect. Carrying an analyzed crystal in the main
inventory grants its passive buff; supported Curios slots also count. For
multiple crystals granting the same effect, only the strongest amplifier is
used. Existing Vexes, fires, and terrain damage are not undone when an anomaly
is resolved.

### Anomaly configuration

In `config/nuclearcraft/common.toml`, `[anomalies]` controls spawning and visual
shaders. Defaults use 128-block placement cells, with a 45% chance of an anomaly
per cell. `[anomalies.variants]` controls enabled types and selection weights.
Each type's subsection controls its effects and applicable counterplay
thresholds; `[anomalies.shards]` controls rewards, and `[anomalies.crystal]`
controls crystal rarity and buffs.

Setting `anomalies.shader = false` disables the post-processing visuals; it
does not make anomalies harmless. Likewise, the master `enabled` switch stops
the spawn manager but does not remove existing anomaly entities or stop their
own effect ticks.

## Nuclear Scientist villager

The **Nuclear Scientist** profession (`nuclearcraft:nuclear_scientist`) uses an
**Analyzer** as its workstation. An eligible unemployed villager can claim an
available Analyzer and become a Nuclear Scientist through the usual villager
job mechanics.

Scientist houses can be added to the house pools of vanilla **plains, snowy,
savanna, desert, and taiga villages**. Each house has a pool weight of 4, so its
presence in a particular village is not guaranteed. These houses are separate
from the ruined villages in the Wasteland.

### Trades

The following are the default trade entries available at each profession level.
A villager selects offers through the normal trading system; prices can change
with demand and other vanilla modifiers. Tag-based materials use the pack's
preferred matching item, and an offer is skipped if a required item cannot be
resolved.

| Level | Player pays | Player receives |
| --- | --- | --- |
| Novice | 1 emerald | 2 graphite dust |
| Novice | 1 emerald | 2 quartz dust |
| Novice | 8 lead ingots | 1 emerald |
| Apprentice | 6 emeralds | 1 basic plate |
| Apprentice | 8 emeralds | 1 RadAway from Nuclear Radiation |
| Journeyman | 1 neptunium-236 | 4 emeralds |
| Journeyman | 32 emeralds | 2 plutonium-238 |
| Journeyman | 20 emeralds | 2 calcium sulfate dust |
| Expert | 16 platinum plates | 1 elite plate |
| Expert | 32 emeralds + 4 books | 2 Californium HECF-251 fuel |
| Expert | 16 platinum ingots + 4 cobalt ingots | 2 Americium HEA-242 fuel |
| Master | 16 emeralds + 4 cobalt ingots | 2 magnesium diboride coils |
| Master | 16 emeralds + 8 zinc ingots | 2 neutronium ingots |

NuclearCraft also adds a generic **Wandering Trader** offer: **8 emeralds for
1 plutonium-239**, with a maximum of eight uses per offer.

### Village configuration

In `config/nuclearcraft/common.toml`:

```toml
[villages]
scientist_house = true
wandering_trader_trades = true
```

`scientist_house` controls adding scientist houses to vanilla village pools.
It does not disable the profession or its trades. `wandering_trader_trades`
controls the added Wandering Trader offer. House-generation changes affect
newly generated villages.

## Ores

NuclearCraft adds ore features to biomes in `#minecraft:is_overworld` or
`#nuclearcraft:wasteland`. Ore variants replace blocks in the matching vanilla
stone or deepslate ore-replaceable tags. A stone-only ore does not replace
deepslate, even when its configured Y range extends below zero.

| Material | Ore variants | Default Y range | Vein size | Attempts per chunk | Extra attempts in Wasteland dimension |
| --- | --- | --- | --- | --- | --- |
| Uranium | Stone, deepslate | -60 to 60 | 9 | 4 | 4 |
| Thorium | Stone, deepslate | -60 to 60 | 9 | 4 | 4 |
| Boron | Stone, deepslate | -60 to 60 | 9 | 4 | 0 |
| Silver | Stone, deepslate | -60 to 60 | 9 | 4 | 0 |
| Lead | Stone | -60 to 60 | 12 | 6 | 0 |
| Tin | Stone | -60 to 60 | 9 | 4 | 0 |
| Zinc | Stone | -60 to 60 | 9 | 4 | 0 |
| Magnesium | Stone, deepslate | -60 to 60 | 9 | 4 | 0 |
| Lithium | Stone | -60 to 60 | 9 | 4 | 0 |
| Cobalt | Stone, deepslate | -60 to 60 | 9 | 4 | 0 |
| Platinum | Deepslate | -60 to 0 | 9 | 4 | 0 |

Placement samples Y uniformly within the inclusive configured range. Attempts
and vein sizes do not guarantee a particular number of ore blocks: placement
depends on terrain and replaceable blocks. In particular, the Wasteland's
Y=0 lower boundary and lack of ordinary deepslate terrain limit ores whose
configured ranges or variants target deeper Overworld terrain.

Uranium and thorium each receive **8 total attempts per chunk** in the Wasteland
dimension by default: 4 base attempts plus 4 extra. The bonus is checked by
dimension ID, so placing the Wasteland biome in another dimension does not
automatically grant it.

### Ore configuration

Each generated material has a section in `config/nuclearcraft/worldgen.toml`.
For example, the uranium defaults are:

```toml
[uranium]
min_height = -60
max_height = 60
vein_size = 9
veins_per_chunk = 4
wasteland_extra_veins_per_chunk = 4
```

- `min_height` / `max_height`: vertical range for placement attempts.
- `vein_size`: requested vein size, from 1 to 64.
- `veins_per_chunk`: base attempts per chunk, from 0 to 32.
- `wasteland_extra_veins_per_chunk`: extra attempts in the Wasteland dimension,
  from 0 to 32.

Set **`veins_per_chunk = 0`** to disable that material's ore generation,
including its Wasteland bonus. Extra attempts require a positive base count.
Stop the game or server before editing configuration, then restart. Changes
apply to newly generated chunks; existing ores and structures are not replaced.

The Ore Leacher's virtual veins are a separate system. Their generation, size,
rarity, and selection settings belong to `common.toml`, rather than the physical
ore settings above. See [Mod configuration](CONFIGURATION.md).

## World-generation data for modpacks

Datapacks can override the Wasteland dimension, dimension type, noise settings,
biome, placed features, biome modifiers, and village structure data. Relevant
IDs include:

| Data | ID |
| --- | --- |
| Dimension / biome | `nuclearcraft:wasteland` |
| Dimension type | `nuclearcraft:wasteland_type` |
| Noise settings | `nuclearcraft:custom_overworld` |
| Wasteland biome tag | `#nuclearcraft:wasteland` |
| Wasteland village biome tag | `#nuclearcraft:has_structure/wasteland_village` |
| Village structure / structure set | `nuclearcraft:wasteland_village` |
| Ore configured feature / placed feature / biome modifier | `nuclearcraft:<material>_ore` |
| Scattered configured features / placed features / biome modifiers | `nuclearcraft:wasteland_ruins`, `nuclearcraft:wasteland_deco`, `nuclearcraft:wasteland_portal`, `nuclearcraft:wasteland_boss_lair` |

The scattered ruins, decoration, portals, and boss lairs are placed **features**.
The Wasteland village is a registered **structure**, which can be searched for
with `/locate structure nuclearcraft:wasteland_village` while in the Wasteland
and with commands enabled.
