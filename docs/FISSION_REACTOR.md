# Fission Reactor

The Solid-Fuel Fission Reactor is a configurable multiblock that burns fission fuel rods,
generates heat and Forge Energy (or steam, if boiling mode is enabled), and irradiates items
in dedicated chambers. It is the backbone of mid-game NuclearCraft progression.

> For the molten-salt variant, see [`MSR.md`](../MSR.md).

## Multiblock Structure

The reactor is a hollow cuboid bounded by casing/glass with functional blocks inside.

- **Shape:** hollow cuboid
- **Size:** configurable; default `min_size = 3`, `max_size = 26` (range 3..32)
- **Required:** exactly one `Fission Reactor Controller`
- **Outer shell:** `Fission Reactor Casing` and/or `Fission Reactor Glass`
- **Ports:** `Fission Reactor Port` - placed in the shell, configurable as input/output for items, fluids, and energy
- **Interior:** any combination of fuel cells, heat sinks, moderators, reflectors, and irradiation chambers

### Block list

| Block | Role |
|---|---|
| `fission_reactor_controller` | Brain; place once on the shell, right-click to form |
| `fission_reactor_casing` / `fission_reactor_glass` | Outer hull |
| `fission_reactor_port` | I/O routing (items / fluids / energy) |
| `fission_reactor_solid_fuel_cell` | Burns a fuel rod, emits heat and energy |
| `fission_reactor_irradiation_chamber` | Irradiates items in a beam line; needs a moderator between it and a fuel cell |
| `fission_reactor_pile-driver_irradiation_chamber` | Stronger irradiator (+4 effective irradiation) |
| Heat sinks (see below) | Remove heat from the core |
| Moderator blocks (tag `nuclearcraft:moderators`) | Boost fuel cell yield, enable irradiation paths |

## Fuel Cells, Moderators, Irradiators

Each fuel cell scans up to **4 blocks** in every direction. Moderator blocks chain fuel cells
together: an adjacent fuel cell (directly or through a line of moderators) contributes to the
multiplier of the host cell.

For a cell with `n` linked neighbors:

- `cellsEnergyMult = n + 1`
- `cellsHeatMult = (n + 1)(n + 2) / 2`

Each moderator face attached to an active fuel cell additionally adds:

- `+33.33%` heat (configurable: `MODERATOR_HEAT_MULTIPLIER`)
- `+16.67%` FE (configurable: `MODERATOR_FE_MULTIPLIER`)

Irradiation chambers must lie on a straight axial line from a fuel cell, with a moderator
between them. The Pile-Driver variant counts as 4 irradiation passes.

## Heat Sinks

Heat sinks are defined by JSON in `data/nuclearcraft/heat_sinks/*.json`. Each definition lists:

- `type` - the material/name
- `heat` - base cooling per tick
- `placement_rule` - adjacency constraints

### Placement rule operators

| Op | Meaning |
|---|---|
| `>` | At least N of X adjacent |
| `<` | Less than N |
| `=` | Exactly N |
| `-` | Exactly N on opposite sides (axially paired) |
| `^` | N at corners (diagonally adjacent) |

If a heat sink does not satisfy its rule, it is **inactive** and contributes no cooling.
Validation uses a topological schedule so that sinks depending on other sinks are resolved
in the correct order.

### Active heat sinks

Variants prefixed `active_` (e.g. `active_water`, `active_cryotheum`) require a steady supply
of the matching coolant through the controller's per-type tank (`ACTIVE_HEATSINK_COOLANT_PER_TICK`,
default `10 mB`).

## Output Modes

The reactor runs in one of two modes (right-click the controller to toggle):

### Energy mode (default)

Generated FE per tick is scaled by:

- `FE_GENERATION_MULTIPLIER` (default `10`)
- `HEAT_MULTIPLIER` capped by `HEAT_MULTIPLIER_CAP` (default `3`)
- `cellsEnergyMult` and moderator bonuses
- `reactivityLevel` (0..100), a ramp that prevents instant peak output on startup

### Steam (boiling) mode

Enabled when `BOILING_ENABLED = true`. The controller consumes water/coolant via a
`FissionBoilingRecipe` and emits the corresponding steam variant at `BOILING_MULTIPLIER`
(default `100`) times the heat. Routed through ports as fluid; can drive turbines directly.

## Heat and Meltdown

- **Heat capacity:** `HEAT_CAPACITY` (default `1,000,000`)
- **Exceeding capacity:** the controller explodes with radius `EXPLOSION_RADIUS`
  (default `4`; set `0` to disable)
- **Radiation per tick:** `recipeInfo.radiation / 10000` is emitted into the chunk

See [`RADIATION.md`](RADIATION.md) for how this propagates.

## Configuration

Key entries in `FissionConfig.java` (mod config `nuclearcraft-common.toml`):

- `min_size`, `max_size`
- `fe_generation_multiplier`, `heat_multiplier`, `heat_multiplier_cap`
- `moderator_heat_multiplier`, `moderator_fe_multiplier`
- `heat_capacity`, `explosion_radius`
- `boiling_enabled`, `boiling_multiplier`
- `active_heatsink_coolant_per_tick`

Heat-sink JSONs live in `data/nuclearcraft/heat_sinks/`. Custom fuel and recipe registration
is supported via KubeJS - see [`KUBEJS_SUPPORT.md`](KUBEJS_SUPPORT.md) and
[`MODPACK_DEVELOPER_GUIDE_CUSTOM_FUELS.md`](MODPACK_DEVELOPER_GUIDE_CUSTOM_FUELS.md).

## Automation

- Fuel rods, depleted rods, irradiation items, coolants, and energy can all be routed
  through `fission_reactor_port` blocks.
- Each port has its own face configuration via right-click.
- A formed reactor exposes a ComputerCraft / OpenComputers v2 peripheral -
  see [`COMPUTERS.md`](COMPUTERS.md).
