# Anomaly System & Resonite Crystals

Environmental hazards confined to the **Wasteland** dimension, plus the loot/progression layer (resonite shards → analyzed resonite crystals) that grows out of them. This document describes the system as implemented; for the original design rationale see the git history of this file.

---

## 1. Overview

Anomalies are stationary (or blink-relocating) hazards that are **not** ordinary mobs:

- Deterministic, seed-based placement — identical seeds get identical layouts.
- They never wander or despawn, and `hurt`/explosions cannot kill them (`AnomalyEntity` neutralises every vanilla damage/despawn path).
- Each is removed only by its variant-specific **counterplay** (radioactive: never).
- Each applies a radius-based effect on an interval, server-side; clients get billboard FX, ambient particles, a looping sound, and an optional post-process shader.
- On resolution, most variants drop **resonite shards**.

Six variants: **Gravitational, Electric, Radioactive, Burning, Psycho, Teleporting**.

Entity registry ids / display names:

| Registry id | Display name |
|---|---|
| `gravitational_anomaly` | Gravitational Anomaly |
| `electric_anomaly` | Electrical Anomaly |
| `radioactive_anomaly` | Radioactive Anomaly |
| `burning_anomaly` | Thermal Anomaly |
| `psycho_anomaly` | Psi Anomaly |
| `teleporting_anomaly` | Spatial Anomaly |

---

## 2. Entity model

`AnomalyEntity` (abstract, extends `Mob`) — `entity/anomaly/AnomalyEntity.java`:

- Constructor: `setNoAi`, `setPersistenceRequired`, `setNoGravity`, `setInvulnerable`, `xpReward = 0`. Attributes: 1024 max health (never exposed), 0 movement/follow, full knockback resistance.
- Synced data: `DATA_STATE` (byte: `IDLE/ACTIVE/CHARGING/RESOLVING`), `DATA_INTENSITY` (float). Gravitational adds `DATA_ABSORBED` (int) so the client can scale its render with absorbed mass.
- NBT: `CellId` (placement-cell identity), `AnomalyState`, `AnomalyIntensity`, plus per-variant fields (`AbsorbedBlocks`, `ExplosionTimer`, `BlinkTimer`/`AnchorX`/`AnchorZ`).
- Invulnerability overrides: `hurt` → false, `isInvulnerableTo` → true, `ignoreExplosion` → true, not pushable/collidable, `removeWhenFarAway` → false, drops no loot/xp via the vanilla path.
- `tick()` splits client (ambient particles + looping sound) from server (`serverTick`).
- `serverTick`: if its cell is marked destroyed → `discard()`; pin to `surface + 2`; `applyEffects()`; poll `checkCounterplay()` on `counterplayInterval()` (default 10t) and call `onCounterplayResolved()` when true.
- Resolution (`onCounterplayResolved`): mark the cell destroyed in `AnomalySavedData`, `dropShards()`, broadcast the dissipation FX event, `discard()`.

Scan work is staggered per entity (`onInterval` mixes `tickCount + getId()`) so many loaded anomalies don't spike on the same tick.

---

## 3. Variants — behaviour & counterplay

All values are config defaults (`CommonConfig.AnomalyConfig`, TOML section `anomalies.*`).

| Variant | Effect (server, throttled) | Counterplay | Key defaults                                                                                                                                                  |
|---|---|---|---------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Gravitational** | Pulls entities toward centre within `radius`; per-tick chance to fling living entities upward; 5 magic dmg within 2 blocks. Absorbs `BlockItem` item-entities and (if `block_absorb`) random nearby blocks; radius and pull grow with absorbed mass (`mass_scale`). | Self-collapse: at `absorb_threshold` absorbed it explodes (`explosion_power`) and resolves. Or just leave the area. | `radius 12`, `pull 0.3`, `launch_chance 0.05`, `launch_power 2.0`, `absorb_threshold 320`, `absorb_interval_ticks 50`, `explosion_power 18`, `mass_scale 1.0` |
| **Electric** | Every `strike_interval_ticks`, visual lightning + `damage` (lightning) on a random living entity in `radius`. | Place an energy block (battery) within reach offering ≥ `discharge_fe_threshold` free FE; it discharges into the battery and dissipates. | `radius 28`, `damage 16`, `strike_interval_ticks 50`, `discharge_fe_threshold 100,000,000` (100 MFE)                                                          |
| **Radioactive** | Every `emit_interval_ticks`, irradiates living entities in `radius` with distance falloff. Players: adds `dose`×falloff to their radiation capability. Non-players: 2–20 radiation damage. | **None — permanent.** Drops no shards. | `radius 48`, `dose 100,000`, `emit_interval_ticks 20`                                                                                                         |
| **Burning** | Every 20t sets living entities in `radius` on fire (5s) + 2–6 fire dmg; optionally ignites blocks (`block_ignite`). Periodic small explosion (`periodic_explosion`, `explosion_radius`) every `explosion_min_ticks`–`explosion_max_ticks` at a random nearby point. | Flood it: ≥ `water_blocks_to_neutralize` water source blocks within 3 blocks extinguishes it. | `radius 10`, `explosion_radius 2.0`, `explosion_min/max_ticks 200/400`, `water_blocks_to_neutralize 8`                                                        |
| **Psycho** | Every `interval_ticks` applies Blindness, Nausea, Weakness, Slowness (`effect_amplifier`) to **players** in `radius` (non-players, creative, spectator exempt). Periodically conjures Vexes (cap `vex_max_nearby`, `vex_lifetime_ticks`) while a player is near. | **Q36 beam, direct hit.** | `radius 48`, `interval_ticks 40`, `effect_amplifier 0`, `vex_max_nearby 6`, `vex_lifetime_ticks 1200`                                                         |
| **Teleporting** | Every 10t flings living entities within `victim_radius` to a random safe spot 32–`victim_max_distance` away. Self-blinks every `self_blink_min/max_ticks` within `self_blink_radius` of its spawn anchor, hovering `hover_offset` above the surface. | **Q36 beam.** | `victim_radius 5`, `victim_max_distance 100`, `self_blink 60–120t`, `self_blink_radius 12`, `hover_offset 3`                                                  |

Q36 counterplay (`Psycho`, `Teleporting`) is wired through `AnomalyEntity.onQ36Hit(...)`, called directly from the Q36 beam / pulse hit path — it bypasses the `mobProjectile` damage-source ambiguity. Direct hit only.

---

## 4. Spawning & persistence

- **`AnomalyPlacement`** (`world/anomaly/`) — pure, side-agnostic, unit-tested (`AnomalyPlacementTest`). Partitions the XZ plane into `cell_size` squares; a SplitMix64-style hash of `(worldSeed, cx, cz)` decides whether a cell holds an anomaly (`spawn_chance_per_cell`), which variant (weighted), and the in-cell offset. `cellId = ChunkPos.asLong(cx, cz)`.
- **`AnomalySpawnManager`** — server-tick driver, runs every 40t, **Wasteland only**. For each player, scans cells within `activation_cell_radius`; for each planned anomaly it skips destroyed cells, requires the column chunk loaded (surface Y), dedups against already-present anomalies in the cell, then spawns at surface (+`hover_offset` for teleporting).
- **`AnomalySavedData`** — `SavedData` on the Wasteland `ServerLevel`; stores the sparse set of destroyed cell ids. Destroyed cells never respawn. Placement is deterministic, so live positions are not persisted — only the destroyed exceptions.

---

## 5. Resonite shards & crystals

### 5.1 Items
- `resonite_shard` — plain `Item`, no NBT. The universal anomaly drop and crafting ingredient.
- `resonite_crystal` — `ResoniteCrystalItem`. Raw until analyzed; gains `analyzed`/`rarity`/`effect` NBT in the Analyzer. Carries both the passive buff and the FE battery.

### 5.2 Pipeline
1. Resolve an anomaly → it drops `shard_drop_min`–`shard_drop_max` shards (chance `shard_drop_chance`), as an **invulnerable** `ItemEntity` so a gravitational collapse blast / burning fire can't consume the reward. Radioactive anomalies never resolve, so they drop nothing.
2. Craft 9 shards (3×3) → 1 raw `resonite_crystal`.
3. Analyze the raw crystal in the **Analyzer** processor (`nuclearcraft:analyzer`; recipe time 8.0, power 16). `AnalyzerBE.handleCrystalAnalyze` rolls rarity + a random beneficial effect via `CrystalAnalysis.applyAnalysis` and writes the NBT. Re-analyzing an already-analyzed crystal **preserves** its NBT (no reroll), so feeding one back never destroys an artifact.

### 5.3 Rarity (`ShardRarity`)

| Rarity | Amplifier | FE/t | Vanilla rarity | Tint | Default roll weight |
|---|---|---|---|---|---|
| Common | 0 | 100 | Common | white | 85 |
| Rare | 1 | 500 | Rare | cyan | 8 |
| Epic | 2 | 1000 | Epic | magenta | 6 |
| Legendary | 3 | 5000 | Epic (gold name + foil) | gold | 1 |

Rarity scales the buff **amplifier** and the FE output only. Weights are config (`shards.rarity_weight_*`); the roll sums the configured weights.

### 5.4 Analyzed crystal — passive buff
`CrystalBuffEvents` (`PlayerTickEvent`, server-side, gated on `anomalies.enabled`): every `buff_refresh_ticks` it scans the player's main inventory and equipped **Curios** slots (soft compat via `CuriosHelper`, guarded by `ModList.isLoaded("curios")`). For each distinct granted effect the **strongest rarity wins** (no stacking); the effect is (re)applied at `buff_duration_ticks`, refreshing in place to avoid the Health-Boost reapply flicker.

Effect pool = all vanilla `BENEFICIAL` effects minus `buff_effect_blacklist` (defaults: hero_of_the_village, dolphins_grace, conduit_power) plus the mod's effects. Custom effects:
- `radiation_resistance` — reused; already honoured by the dose calc.
- `max_health_boost` ("Vitality") — `MaxHealthBoost`, +4 HP per amplifier via an attribute modifier.
- `quickdraw_boost` ("Quickdraw") — `QuickdrawBoost`, a marker effect; `CrystalBuffEvents.onUseItemTick` trims bow/crossbow draw by `amplifier + 1` ticks per tick.

A handful of effects carry a Patreon-patron flavor line in the tooltip (`PATRON_BY_EFFECT`).

### 5.5 Analyzed crystal — infinite FE battery
`CrystalEnergyProvider` / `CrystalEnergy` (`handler/`) attach a Forge `ENERGY` capability: always reads full, never stores or decrements, extract-only, supplying the rarity's FE/t. A raw crystal exposes the cap but outputs 0. Any machine pulling from an adjacent slot gets effectively free energy — intended; balance via a per-tick draw cap if needed.

---

## 6. Rendering & FX

- Client renderers in `client/renderer/anomaly/` (`AnomalyRenderer`, `GravitationalAnomalyRenderer`): per-variant billboard sprite from `textures/particle/anomaly/<name>.png`, sized by state/intensity; gravitational additionally renders growth from the synced absorbed count.
- Ambient particles per variant (`AnomalyType.ambientParticle()`), emitted in `clientTick`.
- Looping ambient sound per variant (`AnomalyAmbientSound` / `SoundHandler`, `NCSounds.getAnomalySound`).
- Post-process shader (`shaders/post/anomaly.json` + `shaders/program/anomaly.*`, `AnomalyShader`) for distortion/glow, toggled by `anomalies.shader`.

---

## 7. Config reference (`anomalies.*`)

Master: `enabled` (true), `shader` (true), `spawn_chance_per_cell` (0.45), `cell_size` (128), `activation_cell_radius` (2).
Per-variant gates under `anomalies.variants.*` (`enable_*`, `weight_*`). Per-variant tuning under `anomalies.{gravitational,electric,radioactive,burning,psycho,teleporting}.*` (see §3). Shard/crystal tuning under `anomalies.shards.*` (drop counts/chance, rarity weights, `buff_refresh_ticks`, `buff_duration_ticks`, `buff_effect_blacklist`).

World-edit vectors are gated for server admins: `gravitational.block_absorb`, `gravitational.explosion_on_collapse`, `burning.block_ignite`, `burning.periodic_explosion`.

---

## 8. Files

**Entities / world:** `entity/anomaly/{AnomalyType, AnomalyEntity, Gravitational/Electric/Radioactive/Burning/Psycho/Teleporting AnomalyEntity}`, `world/anomaly/{AnomalyPlacement, AnomalySpawnManager, AnomalySavedData}`, `test/.../AnomalyPlacementTest`.

**Crystals / buffs:** `item/{ResoniteCrystalItem, ShardRarity, CrystalAnalysis}`, `handler/CrystalEnergyProvider`, `handler/event/server/CrystalBuffEvents`, `compat/curios/CuriosHelper`, `effect/{MaxHealthBoost, QuickdrawBoost}`, `block/entity/processor/AnalyzerBE`.

**Client:** `client/renderer/AnomalyShader`, `client/renderer/anomaly/*`, `client/sound/AnomalyAmbientSound`, `setup/ClientSetup`, `client/setup/EntityRenderHandler`, `handler/event/client/ColorHandler`.

**Registration / config / datagen:** `setup/registration/{Entities, NCItems, NCSounds, CreativeTabs}`, `setup/Registration` (effects), `handler/config/CommonConfig` (`AnomalyConfig`), `datagen/{NCLanguageProvider, models/NCItemModels, recipes/NCRecipes, recipes/recipes/AnalyzerRecipes}`, `item/Q36Item` + `entity/Q36PulseProjectile` (counterplay hook), `handler/event/server/WorldEvents` (spawn tick).

**Resources:** `shaders/post/anomaly.json`, `shaders/program/anomaly.{json,vsh,fsh}`, `sounds/entity/anomaly/*.ogg`, `textures/particle/anomaly/*.png`, `textures/item/resonite_*.png`, analyzer + crystal recipe JSON.

---

## 9. In-game documentation

The feature is documented in both shipped guidebooks (English + Russian):

- **Patchouli** — category `anomalies` with entries `overview`, `types`, `resonite` under `assets/nuclearcraft/patchouli_books/nuclearcraft/{en_us,ru_ru}/`.
- **GuideME** — top-level `anomalies` section (`field-guide`, `types`, `resonite`) under `assets/nuclearcraft/guides/nuclearcraft/{guide,_ru_ru/guide}/`.

In-game strings live in `NCLanguageProvider` (English, datagen) and `lang/ru_ru.json` (Russian, hand-edited).
