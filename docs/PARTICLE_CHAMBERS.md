# Particle Chambers

Particle Chambers are advanced multiblocks that receive, manipulate, or react particle beams from [Accelerators](ACCELERATORS.md). There are three distinct types of particle chambers, each with a different role in the particle physics chain:

1. **Target Chamber**: Used to react particle beams with target items or fluids.
2. **Decay Chamber**: Used to split particles into lighter constituent particles.
3. **Collision Chamber**: Used to smash particles together to produce new ones.

---

## Shared Multiblock Components & Blocks

All three particle chamber structures use the **exact same set of blocks** for their construction (casings, glass, ports, detectors, etc.), with the **only difference being the controller block** you place on the outer wall:

- **Target Chamber** requires a `target_chamber_controller` block.
- **Decay Chamber** requires a `decay_chamber_controller` block.
- **Collision Chamber** requires a `collision_chamber_controller` block.

### Shared Block List

| Block | Role |
|---|---|
| `target_chamber_casing` / `..._glass` | Shared outer shell casing blocks for all chambers |
| `target_chamber_port` | Shared item/fluid I/O port for all chambers |
| `target_chamber_beam_port` | Shared particle I/O port (mode switchable: INPUT / OUTPUT) |
| `target_chamber_camera` | Geometric center block for Target/Decay; focus for Collision |
| `particle_beam` | Beam lines inside the chamber connecting cameras to beam ports |
| Detector blocks (see below) | Fill inner volume; affect efficiency / power draw |

---

## Target Chamber

The Target Chamber reacts particle beams with target items or fluids. It is how you produce isotopes, doped wafers, superheavy elements, and the antimatter chain.

### Target Chamber Structure

- **Controller block:** `target_chamber_controller`
- **Shape:** Cube with **odd, equal dimensions** (e.g. 5×5×5, 7×7×7, ...).
- **Center block:** Exactly one `target_chamber_camera` at the geometric center.
- **Beam lines:** From the camera, a straight line of `particle_beam` blocks must extend in each of the **four horizontal directions** and terminate at a `target_chamber_beam_port` on the wall. Up to **4 simultaneous beam inputs/outputs**.
- **Inner volume:** Populated with `DetectorBlock` instances (mix and match).
- **Outer shell:** built from casing blocks and ports.

---

## Decay Chamber

The Decay Chamber is used to **split** particles into lighter or different constituent particles.

### Decay Chamber Structure

- **Controller block:** `decay_chamber_controller`
- **Shape:** Cube with **odd, equal dimensions** (e.g. 5×5×5, 7×7×7, ...).
- **Center block:** Exactly one `target_chamber_camera` at the geometric center.
- **Beam lines:** From the camera, a straight line of `particle_beam` blocks must extend in each of the **four horizontal directions** and terminate at a `target_chamber_beam_port` on the wall. Up to **4 simultaneous beam inputs/outputs**.
- **Inner volume:** Populated with `DetectorBlock` instances (mix and match).
- **Outer shell:** built from casing blocks and ports.

---

## Collision Chamber

The Collision Chamber is used to **smash** particles together and produce new particles through high-energy collisions.

### Collision Chamber Structure

- **Controller block:** `collision_chamber_controller`
- **Shape:** Rectangular prisms or cubes (does not require equal odd dimensions; accepts rectangular ratios). Width and height must be between 5 and 11, while depth can range from 5 up to configured maximums.
- **Beam Axis (Depth Axis):** 
  - Runs through the chamber center along the controller's horizontal facing axis.
  - The interior slots must consist of `particle_beam` blocks containing **at least 2** `target_chamber_camera` blocks.
  - Both end caps of this axis on the walls must be `target_chamber_beam_port` blocks set to **INPUT** mode (to inject the two opposing colliding beams).
- **Output Ports (Side Walls):**
  - Requires **exactly 4** `target_chamber_beam_port` blocks set to **OUTPUT** mode placed on the side (width) walls (**exactly 2** per side wall).
  - Each output port must reach a `target_chamber_camera` along a straight run of `particle_beam` blocks.
- **Inner volume:** Populated with `DetectorBlock` instances (mix and match).
- **Outer shell:** built from casing blocks and ports.

---

## Detectors

Detectors fill the interior of any of the particle chambers. Each contributes to detection efficiency `η` and consumes power.

| Detector | η | RF/tick | Tier |
|---|---|---|---|
| `silicon_tracker` | 0.150 | 2000 | 1 |
| `bubble_chamber` | 0.075 | 200 | 2 |
| `wire_chamber` | 0.100 | 1000 | 2 |
| `em_calorimeter` | 0.050 | 200 | 3 |
| `hadron_calorimeter` | 0.025 | 100 | 4 |

Each detector has an adjacency rule (`DetectorDef.isValid`); invalid placements do not count.

---

## Recipes (`target_chamber`, `decay_chamber`, `collision_chamber`)

Recipes combine incoming particle stacks and inputs to produce outputs:

### Target Chamber Recipes
- **Target inputs:** items and/or fluids placed in the chamber.
- **Incoming particle stack:** type, minimum energy, focus, amount.
- **Produces:** Item/fluid output, and optional outgoing particles (routed back through beam ports - usable for chained reactions).
- **Example families:**
  - **Activation:** proton + Os → Ir-192 + neutron
  - **Spallation:** antiproton + actinide ingot → fission waste
  - **Capture:** proton + Be → Li-6 + alpha
  - **Doping:** boron_ion + silicon_wafer → silicon_p_doped
  - **Superheavy synthesis:** Ca-48 + Bk-248 → Cn-291 (+ α, n, 2 νₑ)
  - **Pion reactions:** π⁻ + Al → Na-22
  - **Fusion side-reactions:** deuterium + proton fluid → He-3 + n

Antimatter inputs (`antideuterium`, `antihydrogen`, `antihelium`, ...) are **consumed** in the target chamber; the chamber does not produce antimatter directly. Antimatter feedstock comes from [`ParticleSources`](ACCELERATORS.md) - typically obtained via item/fluid synthesis.

### Decay Chamber Recipes
- **Incoming particle stack:** type, minimum energy, focus, amount.
- **Produces:** Multiple lighter/constituent particles (splits particles).

### Collision Chamber Recipes
- **Two incoming particle stacks:** colliding beams from both inputs.
- **Produces:** New/heavy particles smashed out of the high-energy collision.

---

## Recipe Schema in KubeJS

The Particle Chambers get an extended recipe schema with extra keys (`crossSection`, `maxEnergy`).
See [`KUBEJS_SUPPORT.md`](KUBEJS_SUPPORT.md) and [`PARTICLE_KUBEJS_INTEGRATION.md`](PARTICLE_KUBEJS_INTEGRATION.md).

---

## Automation

ComputerCraft / OC2 peripheral (`nc_target_chamber`) exposes:

- `isFormed`, `hasRecipe`, `getRecipeProgress`
- `enableReactor` / `disableReactor`
- `getEnergyPerTick`, `getEnergyStored`
- `voidFuel`, `getFuelInSlot`

See [`COMPUTERS.md`](COMPUTERS.md).
