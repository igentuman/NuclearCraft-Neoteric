# Target Chamber

The Target Chamber bombards an item or fluid target with a particle beam. Depending
on the recipe, it produces a new material, outgoing particles, or both. Detectors
increase reaction efficiency and consume Forge Energy (FE). A redstone signal
enables processing.

This guide describes the NeoForge 26.1.2 implementation and default settings.
Use JEI or EMI for crafting recipes, target materials, beam species, energy
windows, focus requirements, and products available in your modpack.

## Building the multiblock

Build a closed, one-block-thick cubic shell with **outside dimensions of
5 × 5 × 5, 7 × 7 × 7, 9 × 9 × 9, or 11 × 11 × 11**. All three dimensions must
match and must be odd. Configuration can narrow this range.

| Block | Placement and purpose |
| --- | --- |
| Particle Chamber Casing | All shell edges and corners; also usable on faces. |
| Particle Chamber Glass | Optional replacement for casing within faces, away from edges and corners. |
| Target Chamber Controller | Exactly one, in a vertical shell face, facing outward. Keep it clear of the face center reserved for a beam port. |
| Particle Chamber Beam Port | Exactly four, at the centers of the four vertical faces, facing outward. |
| Target Chamber Camera | Exactly one, at the geometric center of the cube. |
| Particle Beam | Straight internal paths from the camera to all four beam ports, on the camera's horizontal layer. |
| Detectors | Interior positions within their allowed distance of the camera; at least one detector must be in range. |
| Particle Chamber Port | Optional service ports within shell faces for FE, items, fluids, and control. Recommended for automation. |

Leave unused interior positions as air. Extra Particle Beam blocks are allowed,
but arbitrary machinery, casing, or additional cameras inside will invalidate
the structure. Detectors cannot replace the camera or required beam paths.

For a **5 × 5 × 5** starter chamber, the middle horizontal layer looks like this:

```text
CCPCC
C.B.C
PBKBP
C.B.C
CCPCC

C = casing      P = beam port, facing outward
B = beam        K = camera      . = unused interior
```

This diagram shows only the middle layer. Complete the shell above and below it,
place the controller and service ports in other face positions, and add a detector
directly above or below the camera for a simple valid placement.

## Detectors and power

Detector range uses **Manhattan distance** from the camera: add the absolute X,
Y, and Z offsets. A detector one block above and one block sideways is distance
2, not distance 1. A detector outside its range is allowed inside the structure,
but contributes neither efficiency nor power cost. At least one must be in range
for assembly to succeed.

| Detector | Maximum distance | Efficiency added | FE per processing tick |
| --- | ---: | ---: | ---: |
| Silicon Tracker | 1 | 0.15 | 20,000 |
| Bubble Chamber | 2 | 0.075 | 2,000 |
| Wire Chamber | 2 | 0.10 | 10,000 |
| EM Calorimeter | 3 | 0.05 | 2,000 |
| Hadron Calorimeter | 5 | 0.025 | 1,000 |

Chamber efficiency starts at **1.0** and adds every in-range detector's
contribution. Operating FE cost is the sum of those detectors' costs; the Target
Chamber has no additional base power cost. Power is charged when a matching beam
is processed, rather than merely because the chamber is assembled or enabled.

A larger shell gives more room for detectors. Size alone does not increase
efficiency, and moving detectors farther away can put them out of range.

## Connecting and starting the chamber

1. Complete the structure and open the controller to check its assembly status.
2. Use a **Multitool** to set one beam port to **Input** and the other three to
   **Output**. New beam ports start **Disabled**. This one-input, three-output
   arrangement provides all channels shown by the Target Chamber interface.
3. Connect the accelerator's output to the chamber input with a straight line of
   Particle Beam blocks. The two ports must face each other. Beam connections
   are subject to the configured accelerator beam connection reach.
4. Connect output beam lines to any downstream machines needed for the recipe's
   particle products.
5. Supply FE through a Particle Chamber Port and insert the recipe's item target
   or fill its fluid input. Service ports share the controller's inventory,
   tanks, and energy storage; configure their accessed sides as needed.
6. Apply a redstone signal to the controller. A linked service port supplying
   a control signal can also enable processing. Clear any forced shutdown.
7. Start the incoming beam and monitor irradiation progress and particle
   readouts in the controller. Extract finished items or fluids through the
   service ports to keep output space available.

All four physical beam ports are required by the structure validator, even if
a recipe uses fewer outgoing particle channels. Port modes do not by themselves
guarantee that processing can run. Use one input: the current Target Chamber
logic processes only its first input channel.

## Irradiation and products

A beam must match the recipe's particle species, fall within its minimum and
maximum energy, and meet its minimum focus. Higher energy does not automatically
mean faster processing: exceeding the recipe's energy window stops it matching.
When multiple recipes match, the highest priority wins; equal priorities use
recipe ID order.

The chamber removes the target material from the input when it starts a reaction.
It then accumulates irradiation work from matching incoming particles:

```text
yield factor = clamp(cross section × chamber efficiency, 0, 1)
work added = consumed particle amount × yield factor
```

The recipe's required particle amount is its work threshold. Reaching that
threshold allows the material product to be emitted, provided the item output
slot or fluid output tank has room. The chamber has one item input and output
slot and one fluid input and output tank. It completes at most one material
batch per processing tick and carries excess work forward.

For example, the default aluminum-to-silicon recipe accepts protons at
**1,500-2,000 keV** with focus **1.0**, has cross section **0.02**, and requires
**50,000,000 work**. With one Silicon Tracker, efficiency is 1.15 and the yield
factor is 0.023. A beam delivering 1,000,000 matching protons each processing
tick adds 23,000 work per tick, taking about 2,174 processing ticks for the first
silicon product while consuming 20,000 FE per processing tick.

Particle products are emitted during irradiation, rather than waiting for the
material progress bar to finish. Each product's amount scales with incoming
particle amount and the same yield factor; fractional particle yields carry
forward. Each particle product row uses a separate output channel.

**Particles are transient.** Incoming particles left unused and outgoing
particles that cannot transfer are discarded at the end of the chamber tick.
Disabled processing, insufficient FE, an unsuitable beam, or an unavailable
downstream destination can therefore waste beam production. Keep material output
space free as well: a blocked material output prevents batch completion but
does not stop irradiation from consuming incoming particles.

## Troubleshooting

| Symptom | Check |
| --- | --- |
| Structure will not assemble | Equal odd outside dimensions; solid casing on every edge and corner; exactly one controller and central camera; four outward-facing beam ports at face centers; uninterrupted internal beam cross. |
| No valid detectors | Place at least one detector within its Manhattan range of the camera, away from required beam paths. |
| No incoming beam readout | Input mode, outward port facing, opposing source port, complete straight external beam line, connection reach, and operating accelerator. |
| Beam appears but progress does not advance | Redstone/control signal, forced shutdown, FE, target material, particle species, energy window, and focus. Small work increments may take several ticks to change the displayed percentage. |
| Target disappears before a product appears | Target material is reserved at reaction start; continue supplying the matching beam until irradiation completes. |
| Progress reaches 100% without a material product | Empty the item output slot or fluid output tank and continue supplying a matching beam. |
| Particle products disappear | Configure enough output ports and connect compatible downstream inputs; particle outputs do not accumulate in a persistent buffer. |
| More detectors do not improve yield | Check detector range and whether cross section × efficiency has already reached the yield cap of 1. |

## Configuration and custom recipes

The `particle_chambers` section of `multiblocks.toml` contains `target_min_size`
and `target_max_size`, defaulting to 5 and 11. The odd-cube requirement still
applies within those limits. See [Mod configuration](CONFIGURATION.md) for
configuration file locations and reload behavior.

Target Chamber recipes use the `nuclearcraft:target_chamber` recipe type.
Modpack authors can set target inputs, particle requirements, material and
particle outputs, cross section, released energy, and priority. See
[KubeJS events and recipes](KUBEJS.md) for the schema and an example.
