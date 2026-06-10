# Target Chamber

The Target Chamber receives one or more particle beams from [Accelerators](ACCELERATORS.md)
and reacts them with target items or fluids. It is how you produce isotopes, doped wafers,
superheavy elements, and the antimatter chain.

## Multiblock Structure

- **Shape:** cube with **odd, equal dimensions** (e.g. 5×5×5, 7×7×7, ...).
- **Center block:** exactly one `target_chamber_camera` at the geometric center.
- **Beam lines:** from the camera, a straight line of `particle_beam` blocks must extend in
  each of the **four horizontal directions** and terminate at a `target_chamber_beam_port`
  on the wall. Up to **4 simultaneous beam inputs/outputs**.
- **Inner volume:** populated with `DetectorBlock` instances (mix and match).
- **Outer shell:** `target_chamber_casing`, `target_chamber_casing_glass`, ports
  (tag `target_chamber_casing`).

### Block list

| Block | Role |
|---|---|
| `target_chamber_casing` / `..._glass` | Outer shell |
| `target_chamber_port` | Item/fluid I/O |
| `target_chamber_beam_port` | Particle I/O (mode switchable: INPUT / OUTPUT) |
| `target_chamber_camera` | Center; controller |
| `particle_beam` | Beam line between camera and beam ports |
| Detector blocks (see below) | Inner volume; affect efficiency / power draw |

## Detectors

Detectors fill the interior. Each contributes to detection efficiency `η` and consumes power.

| Detector | η | RF/tick | Tier |
|---|---|---|---|
| `silicon_tracker` | 0.150 | 2000 | 1 |
| `bubble_chamber` | 0.075 | 200 | 2 |
| `wire_chamber` | 0.100 | 1000 | 2 |
| `em_calorimeter` | 0.050 | 200 | 3 |
| `hadron_calorimeter` | 0.025 | 100 | 4 |

Each detector has an adjacency rule (`DetectorDef.isValid`); invalid placements do not count.

## Recipes (`target_chamber`)

Recipes combine:

- **Target inputs:** items and/or fluids placed in the chamber.
- **Incoming particle stack:** type, minimum energy, focus, amount.

And produce:

- **Item / fluid output**
- **Outgoing particles** (routed back through beam ports - usable for chained reactions)

### Example recipe families

- **Activation:** proton + Os → Ir-192 + neutron
- **Spallation:** antiproton + actinide ingot → fission waste; drives the heavy-metal
  transmutation table (U, Th, Pu, Am, Cm, Bk, Cf, Np, Pb, Hg, Au, Pt, Ir, Os, W, Hf, ...)
- **Capture:** proton + Be → Li-6 + alpha
- **Doping:** boron_ion + silicon_wafer → silicon_p_doped
- **Superheavy synthesis:** Ca-48 + Bk-248 → Cn-291 (+ α, n, 2 νₑ)
- **Pion reactions:** π⁻ + Al → Na-22
- **Fusion side-reactions:** deuterium + proton fluid → He-3 + n

Antimatter inputs (`antideuterium`, `antihydrogen`, `antihelium`, ...) are **consumed** here;
the chamber does not produce antimatter directly. Antimatter feedstock comes from
[`ParticleSources`](ACCELERATORS.md) - typically obtained via item/fluid synthesis.

## Recipe Schema in KubeJS

The Target Chamber gets an extended recipe schema with extra keys (`crossSection`, `maxEnergy`).
See [`KUBEJS_SUPPORT.md`](KUBEJS_SUPPORT.md) and
[`PARTICLE_KUBEJS_INTEGRATION.md`](PARTICLE_KUBEJS_INTEGRATION.md).

## Automation

ComputerCraft / OC2 peripheral (`nc_target_chamber`) exposes:

- `isFormed`, `hasRecipe`, `getRecipeProgress`
- `enableReactor` / `disableReactor`
- `getEnergyPerTick`, `getEnergyStored`
- `voidFuel`, `getFuelInSlot`

See [`COMPUTERS.md`](COMPUTERS.md).
