# Heat Exchanger

The Heat Exchanger is a fluid multiblock built around a shared **heat buffer**. It runs two
independent recipe loops at once:

- a **hot loop** that cools a hot coolant and **deposits** the released heat into the buffer, and
- a **cold loop** that condenses spent steam by **drawing** heat back out of the buffer.

Radiators in the shell bleed surplus heat away passively. The net effect: hot reactor coolant
goes in one side, gets recycled, and the banked heat is spent condensing turbine exhaust back
into water on the other side.

## Multiblock Structure

- **Shape:** cuboid shell, **3×3×3** up to **11×11×11** (`MIN_SIZE` / `MAX_SIZE`). Non-cube
  shapes are allowed (e.g. 5×6×10).
- **Corners/edges:** must be `heat_exchanger_casing`.
- **Walls:** any casing-tag block — casing, controller, ports, radiators.
- **Controller:** exactly one `heat_exchanger_controller`.
- **Interior:** air and/or `heat_exchanger`
- The interior count **N** drives throughput and heat capacity.

### Block list

| Block | Role |
|---|---|
| `heat_exchanger_controller` | Brain; owns the heat buffer and energy storage |
| `heat_exchanger_casing` | Shell; corners must be casing |
| `heat_exchanger_hot_coolant_port` | Fluid I/O for the **hot** loop (hot-in / hot-out) |
| `heat_exchanger_cold_coolant_port` | Fluid I/O for the **cold** loop (cold-in / cold-out) |
| `heat_exchanger_radiator` | Shell block; passive heat bleed from the buffer |
| `heat_exchanger` | Interior block (`N`); scales speed and heat capacity |

## Heat Buffer

The controller stores `heat` in `[0, maxHeat]`:

```
maxHeat = HEAT_CAPACITY_PER_BLOCK × N
```

Heat enters from hot recipes, leaves through cold recipes and radiators.

## Recipes

Recipes live under `data/nuclearcraft/recipes/heat_exchanger_controller/`. Each carries a signed
`heat` value that classifies it and sets how much heat one operation moves:

- **`heat > 0` — hot recipe.** Flows through Hot Coolant Ports. Adds `heat` to the buffer per op.
  Stalls when the buffer is full.
- **`heat < 0` — cold recipe.** Flows through Cold Coolant Ports. Removes `|heat|` from the buffer
  per op. Stalls when the buffer is empty.

`isHot()` / `isCold()` are derived from the sign; `getHeat()` reuses the recipe's radiation slot
(no extra serialized field), so network sync is free.

### Default recipes

| Recipe | Input → Output | `heat` | Loop |
|---|---|---|---|
| `flibe_hot_molten_salt` | Hot FLiBe Molten Salt → FLiBe Molten Salt | **+600** | hot |
| `hot_helium` | Hot Helium → Helium | **+400** | hot |
| `low_quality_steam` | Low-Quality Steam → Technical Water | **−300** | cold |
| `exhaust_steam` | Exhaust Steam → Water | **−100** | cold |

## Tanks & Ports

One content handler, four fluid tanks (input region first):

| Index | Tank | Mode |
|---|---|---|
| 0 | hot-in | INPUT |
| 1 | cold-in | INPUT |
| 2 | hot-out | OUTPUT |
| 3 | cold-out | OUTPUT |

- Each input tank only accepts fluids that are inputs of its loop's recipes
  (`setAllowedInputFluids`).
- **Hot Coolant Ports** expose tanks **0 + 2**; **Cold Coolant Ports** expose tanks **1 + 3**.
  A hot fluid piped into a cold port (or vice versa) is rejected.
- Tank capacity scales with the interior: `(N + 1) × FLUID_CAPACITY` mB per tank.

## Processing

Each server tick, while formed, redstone-powered, and `energyStored ≥ energyPerTick`, the
controller runs both loops independently (hot, then cold):

```
opsCap   = floor(THROUGHPUT_PER_BLOCK × N / inputAmount)
ops      = min(opsCap, inputOnHand, outputRoom, heatRoomOrStored)
```

- Hot loop heat room: `(maxHeat − heat) / heat`.
- Cold loop stored heat: `heat / |heat|`.
- On a successful op batch: drain input tank, fill output tank, then
  `heat += ops × recipeHeat` (clamped to `[0, maxHeat]`).
- A stalled loop (no room / no heat / blocked output) does **not** block the other loop.
- Block-state `powered` = (hot ran) OR (cold ran).

`N = 0` → the multiblock forms but does nothing.

## Radiators

Passive, **ungated** cooling: applies every tick the structure is formed, regardless of redstone
or stored energy.

```
heat = max(0, heat − radiatorCount × RADIATOR_COOLING)
```

Radiators are how you shed heat the cold loop can't consume fast enough — without them, a busy hot
loop fills the buffer and stalls. They are counted in `HeatExchangerMultiblock.processOuterBlock`.

> Note: the in-game radiator tooltip (`heat_exchanger.radiator.descr`) currently reads **1000 H/t**,
> but `RADIATOR_COOLING` defaults to **100**. Reconcile the tooltip or the config value.

## Energy

Energy is a **standby cost**, never a per-recipe price. The store is a pure consumer
(`CustomEnergyStorage`, no extract).

```
energyPerTick = N × ENERGY_PER_BLOCK
```

While formed and redstone-powered, the controller drains `energyPerTick`. If
`energyStored < energyPerTick`, **both loops halt** for that tick (radiator cooling still runs).
No redstone signal → idle.

## Config (`NuclearCraft/heat_exchanger.toml`)

| Key | Default | Meaning |
|---|---|---|
| `min_size` / `max_size` | 3 / 11 | Shell bounds |
| `energy_per_block` | 200 | Standby FE/t per interior block |
| `throughput_per_block` | 100.0 | Recipe progress units per tick per interior block |
| `energy_capacity` | 10,000,000 | Internal FE buffer |
| `fluid_capacity_per_block` | 10,000 | Tank capacity (mB) per interior block, per tank |
| `radiator_cooling` | 100 | H/t removed per radiator (passive) |
| `heat_capacity_per_block` | 100,000 | Heat buffer capacity per interior block |

## Typical Loop

1. A reactor produces hot coolant (Hot Helium, Hot FLiBe Molten Salt).
2. Pipe it into a **Hot Coolant Port** → it's cooled back to usable coolant; the heat banks in the
   buffer.
3. The banked heat powers the **cold loop**: feed turbine Exhaust Steam / Low-Quality Steam into a
   **Cold Coolant Port** → it condenses to Water / Technical Water, ready to boil again.
4. Add **radiators** to dump any heat the cold loop can't keep up with.
