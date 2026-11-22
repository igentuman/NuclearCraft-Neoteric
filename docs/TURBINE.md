# Steam Turbine

The turbine turns steam into Forge Energy (FE) and a lower-grade steam output.
Use it after a steam-producing reactor or [Heat Exchanger](HEAT_EXCHANGER.md),
then recover the spent steam to keep your water loop running.

This guide covers the **turbine multiblock**. There is also a single-block
**Steam Turbine** processor, described below. Use JEI or EMI for crafting recipes
and your modpack's available fluid recipes. Values here describe the default
NeoForge 26.1.2 configuration.

## Building the multiblock

Build a closed, one-block-thick shell with **outside dimensions between 5 and
17 blocks on every axis**. The cross-section perpendicular to the rotor must be
an **odd-sized square**: for example, a 5 × 5 cross-section with a length of
5, 6, or more blocks. The rotor can run horizontally or vertically.

| Block | Placement and purpose |
| --- | --- |
| Turbine Controller | Exactly one in the shell. Opens the interface and shows assembly status. |
| Turbine Casing / Turbine Glass | Close the shell around the machinery. |
| Turbine Bearing | Exactly two, centered on opposite end faces. These define the rotor axis. |
| Turbine Rotor Shaft | A continuous straight line through the interior, joining the two bearings. Each shaft must face along the rotor axis. |
| Rotor Blades | Inside, extending outward from the shaft in the four perpendicular directions. |
| Turbine Coils | Shell blocks. Valid coils convert rotor operation into FE. |
| Turbine Port | Shell block for fluid and energy connections. Multiple ports share the controller's storage. |

Keep unused interior spaces as air. Build blades as straight, uninterrupted arms
from the shaft, arranged in crosses of four equal arms. Only blades connected to
the shaft along these straight arms contribute; the counted blade total must be
a multiple of four. Pipes belong outside the shell.

### Starter layout

1. Build a **5 × 5 × 5** casing shell, leaving a **3 × 3 × 3** interior.
2. Replace the center block of each of two opposite faces with a bearing.
3. Join the bearings with **three rotor shafts**, facing along the bearing axis.
4. On each shaft, place one blade in each of the four perpendicular directions:
   **12 blades total**. Leave the four diagonal spaces in each interior slice as air.
5. Replace at least two shell blocks touching a bearing with **Magnesium Coils**.
   Each coil must share a face with the bearing. Two valid coils are the minimum
   for positive FE output; a single valid coil produces none.
6. Replace other shell blocks with one controller and enough ports for steam
   input, spent-steam extraction, and energy extraction. Keep the bearing and
   coil arrangement intact.
7. Open the controller and resolve any assembly error, then connect fluid pipes
   and an energy cable or storage block.

For a horizontal rotor, each interior cross-section looks like this; the shell
surrounds the diagram:

```text
. B .     S = rotor shaft
B S B     B = rotor blade
. B .     . = air
```

## Steam and water loops

The multiblock has one input tank and one output tank. Insert steam and extract
the product through linked Turbine Ports. Use transport that inserts and extracts
the correct fluids, and configure the accessed controller sides to allow the
required fluid and energy transfers.

| Input | Output | Relative recipe power |
| --- | --- | ---: |
| Steam | Exhaust Steam | 0.75 |
| High Pressure Steam | Low Pressure Steam | 1.0 |
| Low Pressure Steam | Low Quality Steam | 0.1 |

All three default recipes convert fluid **1:1**. The power modifier scales energy
generation; it is not a fixed FE/mB value or the total efficiency of the plant.

Two useful arrangements are:

```text
Water → Heat Exchanger → Steam → Turbine → Exhaust Steam
  ↑                                           ↓
  └──────── Heat Exchanger condensation ───────┘

Technical Water → Heat Exchanger → High Pressure Steam → Turbine
       ↑                                                  ↓
       │                                          Low Pressure Steam
       │                                                  ↓
       └── Heat Exchanger ← Low Quality Steam ← Second Turbine
```

The Heat Exchanger's hot loop condenses Exhaust Steam into Water and Low Quality
Steam into Technical Water, recovering heat. Its cold loop spends heat to make
steam again. The exchanger also needs operating FE; condensation alone does not
supply all the heat needed to remake the steam. See the
[Heat Exchanger guide](HEAT_EXCHANGER.md) for its two loops and connections.

The turbine starts processing when assembled, supplied with a matching fluid,
and able to store its fluid output. It needs no startup FE or operating redstone
signal. Keep the output tank drained: a full tank, or one holding a different
output fluid, blocks processing.

**Extract energy continuously.** A full energy buffer does not stop steam
consumption, so steam can be spent without storing the generated FE.

## Blades, coils, and better output

| Blade material | Default efficiency |
| --- | ---: |
| Basic | 95% |
| Steel | 100% |
| Extreme | 110% |
| SiC-SiC CMC | 125% |

More connected blades and better blade materials raise the flow capacity.
Actual flow also depends on steam supply, coil drag, and output tank space.
Increase steam production and pipe throughput alongside turbine size; an
undersupplied large turbine cannot reach its displayed maximum output.

Coils only work when their placement requirements are met. Adjacency means
**sharing a face**, and a neighboring coil must itself be valid.

| Coil | Default efficiency | Must touch at least one |
| --- | ---: | --- |
| Magnesium | 86% | Turbine Bearing |
| Beryllium | 90% | Valid Magnesium Coil |
| Gold | 104% | Valid Beryllium Coil |
| Copper | 110% | Valid Gold Coil |
| Silver | 112% | Valid Magnesium or Gold Coil |
| Aluminum | 98% | Valid Gold, Magnesium, Beryllium, or Copper Coil |

Start with magnesium around the bearings, then extend a valid chain through
beryllium and gold to the other materials. Invalid coils do not contribute to
the active-coil count and can reduce the effective coil efficiency. Check the
controller after changing your arrangement.

The controller shows **FE/t, actual flow, maximum flow, flow ratio, and coil
efficiency**. Hover over the assembled-status indicator for blade count, active
coils, and maximum output. Maximum output is a capacity estimate; actual output
depends on the fluid being processed and the available flow.

With **Create** installed, the bearings can also supply rotational power. Connect
Create machinery at the bearings; rotational speed follows turbine operation.

## Single-block Steam Turbine

The Steam Turbine processor is a compact option before building a multiblock.
Place it, configure fluid input/output sides, supply steam, and connect an energy
receiver. It generates FE from its recipes and sends stored energy to adjacent
receivers.

Its default recipes differ from the multiblock: **Steam → Water** and
**High Pressure Steam → Exhaust Steam**, both at a 1:1 fluid ratio. Check its
JEI/EMI category for processing values. See [Processors](PROCESSORS.md) for
the interface and side configuration.

## Troubleshooting

| Symptom | What to check |
| --- | --- |
| Structure will not assemble | Closed shell, exactly one controller, two centered opposite bearings, odd square cross-section, and all outside dimensions within limits. |
| Rotor or blade error | The shaft must run all the way between bearings and face along their axis. Build straight blade arms from the shaft in complete crosses. |
| Steam flows but no FE is produced | Install at least two valid coils and check coil efficiency. Blades and fluid conversion alone do not produce FE. |
| Steam will not flow | Check assembly, input recipe, side configuration, output tank space, and any forced shutdown set through automation. |
| Output stays below maximum | Check steam supply, pipe throughput, output extraction, blade count, and active coils. |
| Energy storage stays empty | Check energy extraction settings and cable capacity; connected machines may be consuming all generation. |

Turbine size, flow, generation, and Create conversion settings are in the
`[turbine]` section of `config/nuclearcraft/multiblocks.toml`. Modpacks can change
these values and recipes; see [Mod configuration](CONFIGURATION.md).
