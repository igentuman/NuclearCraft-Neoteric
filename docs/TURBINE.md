# Turbine

The Steam Turbine consumes pressurized fluids (steam, high-pressure steam, exhaust steam,
or fusion coolant outputs) and produces Forge Energy through a rotor + coil arrangement.
It is the standard partner for Fission boiling mode, Fusion coolant loops, and any modpack
steam source.

## Multiblock Structure

- **Shape:** rectangular box; the cross-section perpendicular to the rotor axis must be a
  **square with odd side length** (e.g. 3×3, 5×5, 7×7).
- **Controller:** exactly one `turbine_controller` on a casing face.
- **Bearings:** exactly **two** `turbine_bearing` blocks, one at each end of the rotor axis.
- **Rotor:** a straight column of `turbine_rotor_shaft` blocks between the bearings.
- **Blades:** 4 blades per shaft segment, one on each side, expanding into the chamber.
  **Total blade count must be a multiple of 4.**
- **Coils:** `turbine_*_coil` blocks on the outer faces (any `TurbineCoilBE`).
- **Hull:** `turbine_casing`, `turbine_glass`, and `turbine_port` (fluid I/O).

### Block list

| Block | Role |
|---|---|
| `turbine_controller` | Brain; place on hull |
| `turbine_casing` / `turbine_glass` | Outer shell |
| `turbine_port` | Fluid input/output |
| `turbine_bearing` | Endpoints of the rotor axis (2× required) |
| `turbine_rotor_shaft` | Connects the two bearings |
| `turbine_<blade>` | Rotor blades (4 per shaft segment) |
| `turbine_<coil>_coil` | Outer coil; must pass placement rule to count |

## Blades

| Blade | Efficiency | Expansion |
|---|---|---|
| `basic_rotor_blade` | 95 | 120 |
| `steel_rotor_blade` | 100 | 140 |
| `extreme_rotor_blade` | 110 | 160 |
| `sic_sic_cmc_rotor_blade` | 125 | 180 |

Each blade contributes a flow value (`TurbineBladeBE.getFlow`). The controller sums all flows
into `flow`; `bladesEfficiency = flow / bladeCount`.

## Coils

| Coil | Base eff. | Placement rule |
|---|---|---|
| `copper` | 110 | adjacent to a `gold` coil |
| `magnesium` | 86 | adjacent to a `turbine_bearing` |
| `silver` | 112 | adjacent to a `magnesium` or `gold` coil |
| `gold` | 104 | adjacent to a `beryllium` coil |
| `beryllium` | 90 | adjacent to a `magnesium` coil |
| `aluminum` | 98 | adjacent to a `gold`/`magnesium`/`beryllium`/`copper` coil |

Placement rules use the same operators as fission heat sinks (`>`, `<`, `=`, `-`, `^`) and
can be overridden via `TurbineConfig.PLACEMENT_RULES`. Invalid coils are inactive.

`coilsEfficiency` is the average `getRealEfficiency` of all coils; `activeCoils` is the count
of valid ones.

## Fluid Recipes

Recipes live under `data/nuclearcraft/recipes/turbine_controller/`:

- `steam`
- `high_pressure_steam`
- `exhaust_steam`

Plus any custom recipes registered (e.g. heated fusion coolants). Each recipe carries a
`powerModifier` that multiplies energy output.

Tank capacity scales as roughly `(log(h × w × d))² × 1,000,000 mB`.

## Rotor Mechanic

Per tick:

```
maxFlow = flow × BLADE_FLOW × (log10(flow))^2.8

coilsDrag = max(1, 100 / coilsEfficiency × log(log10(activeCoils + 4) + 2))

realFlow = min(maxFlow, tankAmount) / coilsDrag

rotationSpeed = (rotationSpeed × 4 + realFlow / (flow × BLADE_FLOW)) / 5
```

`rotationSpeed` is smoothed so the turbine spins up gradually.

## Energy Output

```
efficiencyRate = log10(activeCoils) × coilsEfficiency × bladesEfficiency / 1000

energyPerTick =
    sqrt((realFlow + 1)(realFlow + 2) / 2)
  × TURBINE_CONFIG.ENERGY_GEN
  × efficiencyRate
  × ENERGY_GENERATION.GENERATION_MULTIPLIER
  × recipe.powerModifier
  / 2
```

Internal energy buffer: `100,000,000 FE`.

## Automation

The formed turbine exposes a ComputerCraft / OC2 peripheral with enable/disable and energy
stats - see [`COMPUTERS.md`](COMPUTERS.md). Fluid ports are configured per-side via right-click.
