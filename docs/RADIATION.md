# Radiation

NuclearCraft-Neoteric ships a full chunk-based radiation system. Radiation propagates
between chunks, decays naturally, accumulates in players from both the environment and
their inventory, and is mitigated by armor, effects, and consumable cleansers.

## Storage and Propagation

- **Per-dimension data:** `RadiationManager extends SavedData` keyed `nc_world_radiation`.
- **Per-chunk:** `WorldRadiation.chunkRadiation: Map<Long, Long>` packs
  `(radiation: int32, timestamp: int32)` per chunk.
- **Diffusion:** every refresh, **25% of a chunk's radiation** is split evenly across its
  **8 neighbors**.
- **Decay:** `DECAY_SPEED` pRad/s (default `2000`).
- **Natural floor:** `NATURAL_RADIATION` (default `20` pRad) plus biome and dimension
  overrides.
- **Update interval:** `update_interval` ticks (default `40`).

## Sources

- **Reactor leaks:** Fission reactors emit `recipeInfo.radiation / 10000` per tick into
  their chunk via `RadiationManager.addRadiation` (see
  [`FISSION_REACTOR.md`](FISSION_REACTOR.md)).
- **Items:** `ItemRadiation` maps fuel rods, isotope items, and RTG cells to per-tick rates.
  Examples (default values, configurable):
    - Uranium ingot: `0.00007`
    - Cf-250: `3.0`
- **Mekanism integration:** when `MEKANISM_RADIATION_INTEGRATION = true`, sources are
  mirrored both ways through Mekanism's radiation system.

## Player Effects

Each player has a `PlayerRadiation` capability (`PlayerRadiationProvider`).

```
radiation += chunkRadiation × GAIN_SPEED_FOR_PLAYER + inventoryDose
radiation -= DECAY_SPEED_FOR_PLAYER  // per second
```

- `GAIN_SPEED_FOR_PLAYER = 0.1`
- `DECAY_SPEED_FOR_PLAYER = 50` uRad/s
- `maxPlayerRadiation = 500,000,000`

### Contamination thresholds

| Threshold | Effects applied (scaled) |
|---|---|
| 22% | Weakness, Confusion |
| 44% | + Glowing, Unluck |
| 66% | + Poison |
| 100% | + Blindness, **instant death** |

### Mob damage

Mobs in chunks above `ENTITY_RADIATION_THRESHOLD = 5,000,000` take damage each refresh
(`RadiationManager.damageEntities`).

## Mitigation

### Armor shielding (`armor_shielding` config + `ItemShielding`)

Built-in armor sets:

| Set | Helmet / Chest / Legs / Boots |
|---|---|
| `hazmat_*` | 3 / 5 / 4 / 3 |
| `hev_*` | 5 / 7 / 6 / 5 |
| Mekanism hazmat / MekaSuit | included by default |

Custom armor can be added via config.

### Effects

- `radiation_resistance` potion effect adds `(amplifier + 1) × 2` shielding while active.

### Cleansing items (`radiation_removal_items`)

Consuming these removes the listed amount of accumulated radiation:

| Item | Removes (pRad) |
|---|---|
| `minecraft:golden_carrot` | `1×10⁷` |
| `minecraft:golden_apple` | `1×10⁸` |
| `minecraft:enchanted_golden_apple` | `2.5×10⁹` |
| `nuclearcraft:radaway` | `5×10¹⁰` |

## Configuration (`radiation` namespace)

| Key | Default | Notes |
|---|---|---|
| `enabled` | `true` | Master switch |
| `background_radiation` | `20` | Natural floor (pRad) |
| `decay_speed` | `2000` | Chunk decay per second |
| `decay_speed_for_player` | `50` | Player decay per second |
| `gain_speed_for_player` | `0.1` | Multiplier on chunk → player |
| `items_radiation` | - | Map of item → pRad/tick |
| `radiation_removal_items` | - | Map of item → pRad removed |
| `armor_shielding` | - | Map of armor item → shielding |
| `biome_radiation` | - | Per-biome overrides |
| `dimension_radiation` | - | Per-dimension overrides |
| `update_interval` | `40` | Ticks between refresh passes |
| `mekanism_radiation_integration` | `true` | Bridge with Mekanism |

## Geiger Counter

`GeigerCounterItem` ticks every 20 game ticks. When held, it:

- Reads the player's chunk radiation from `WorldRadiation`.
- Displays it as an action-bar message (`message.nc.geiger_radiation_measure`) formatted
  via `formatRads`.
- Plays a configurable click sound (`GeigerSound`) whose rate scales with the dose.

## Related Files

- `radiation/RadiationManager.java`, `radiation/WorldRadiation.java`
- `radiation/data/PlayerRadiation*.java`
- `radiation/ItemRadiation.java`, `radiation/ItemShielding.java`,
  `radiation/RadiationCleaningItems.java`, `radiation/FluidRadiation.java`
- `handler/config/RadiationConfig.java`
- `item/GeigerCounterItem.java`, `item/RadAwayItem.java`
- `client/sound/GeigerSound.java`
