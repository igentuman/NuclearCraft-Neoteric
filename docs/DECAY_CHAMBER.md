# Decay Chamber

The Decay Chamber converts an incoming particle beam into decay products. It
also supports photon pair production, including electron/positron and
muon/antimuon pairs. Detectors improve reaction yield and add to the chamber's
Forge Energy (FE) cost.

Values below describe the default configuration and recipes in the NeoForge
1.21.1 port. Use JEI or EMI for crafting recipes and the particle recipes
available in your modpack.

## Building the multiblock

Build a closed cube with **outside dimensions of 5, 7, 9, or 11 blocks**. All
three dimensions must match. The shell is one block thick.

The Decay Chamber uses shared **Target Chamber** components; only its controller
is specific to the Decay Chamber.

| Block | Placement and purpose |
| --- | --- |
| Decay Chamber Controller | Exactly one on a vertical shell face, facing outward. Keep it away from edges and the face center reserved for a beam port. |
| Target Chamber Casing | Shell. Required on every edge and corner. |
| Target Chamber Casing Glass | Optional replacement for casing in face interiors. |
| Target Chamber Camera | Exactly one, at the center of the cube. |
| Target Chamber Beam Port | Exactly four: one at the center of each vertical face, facing outward. |
| Particle Beam | Four straight interior arms connecting the camera to the four beam ports at camera height. |
| Detectors | Interior positions outside the required beam arms. At least one must be within its working range of the camera. |
| Target Chamber Port | Optional service port in a face interior. Provides access to the controller's FE storage and supports redstone control. |

Leave unused interior positions as air. Extra Particle Beam blocks are allowed,
but extra cameras and unrelated blocks are not. Detectors cannot replace the
camera or any required beam-arm block.

For a starter build:

1. Build a **5 × 5 × 5** casing shell around a **3 × 3 × 3** interior.
2. Put the camera at the exact center.
3. Replace the center block of each vertical face with an outward-facing beam
   port. Connect each port to the camera with Particle Beam blocks inside.
4. Put a **Silicon Tracker directly above or below the camera**. This is one
   block away, within its working range.
5. Place the controller in a vertical face interior, away from the beam port.
   Optionally add a service port. Keep all edges and corners as solid casing.
6. Open the controller and resolve any structure validation errors.
7. Use the Multitool on the beam ports to select **one Input and three Outputs**.
   Ports start Disabled; cycling modes goes Input → Output → Disabled → Input.
8. Supply FE and a redstone signal, then connect the incoming and outgoing beams.

The middle horizontal layer of this starter chamber looks like this from above:

```text
CCPCC
C.B.C
PBKBP
C.B.C
CCPCC

C = casing       P = outward-facing beam port
B = particle beam
K = camera       . = air
```

Place the controller and optional service port on another layer, and the Silicon
Tracker immediately above or below `K`. The top and bottom layers close the shell.

## Connecting and controlling beams

Connect the upstream machine's output to the chamber's Input port. Connect each
Output port to a downstream particle machine's input. External connections must
be straight Particle Beam corridors with the two ports facing each other. The
default beam connection reach is **16 blocks**.

Use exactly one Input for normal operation: the Decay Chamber processes only
its first input channel. Three Outputs allow recipes with three different
products to deliver every product. A recipe with fewer products leaves unused
output channels empty.

Output channels follow the order of the configured Output ports around the
chamber: front, right, back, left, relative to the controller's outward-facing
front wall, skipping ports set to Input or Disabled. Recipe output order maps
directly onto these channels. Hover over the controller's particle displays to
identify what each channel carries before connecting a processing chain.

Supply FE to the controller or through a linked service port. Apply redstone
power to the controller, or use a linked service port's redstone control.
Processing also requires the controller's forced shutdown to be off.

**The chamber does not buffer beams between ticks.** Unprocessed input and
undelivered output are discarded at the end of the chamber's tick. Missing
connections, unavailable destinations, insufficient FE, or disabled operation
can therefore waste incoming particles. Products without a configured output
channel are also discarded; a blocked output does not stop the reaction.

## Detectors, efficiency, and power

Detector range uses **Manhattan distance** from the camera: add the absolute
offsets along all three axes. For example, one block up and one block sideways
is distance 2. Detectors outside their range may remain inside the structure,
but contribute neither efficiency nor operating cost. At least one detector
must be in range for the chamber to assemble.

| Detector | Maximum distance | Efficiency added | FE/t added |
| --- | ---: | ---: | ---: |
| Silicon Tracker | 1 | 0.15 | 20,000 |
| Bubble Chamber | 2 | 0.075 | 2,000 |
| Wire Chamber | 2 | 0.10 | 10,000 |
| EM Calorimeter | 3 | 0.05 | 2,000 |
| Hadron Calorimeter | 5 | 0.025 | 1,000 |

The default chamber has a **1,000,000 FE** energy buffer. Its operating cost is:

```text
FE per active tick = 2,000 + sum of the in-range detectors' FE/t
Efficiency        = 1 + sum of the in-range detectors' efficiency bonuses
Yield factor      = clamp(recipe cross_section × Efficiency, 0, 1)
```

One Silicon Tracker gives efficiency **1.15** and a cost of **22,000 FE per
active tick**. A reaction with cross section 1 already has full yield; additional
detectors cannot increase it beyond 100%. A cross section of 0.5 gives a yield
factor of **0.575** with that tracker.

The chamber checks that it has enough FE for the whole active-tick cost before
processing. It charges that cost once when at least one whole successful
reaction is produced, regardless of batch size. It does not charge processing
FE on ticks with no successful reaction.

Larger chambers provide more detector positions. Increasing shell size alone
does not add a processing-rate multiplier.

## Recipes and processing

The default recipe set contains **34 recipes**. Every default recipe requires
one input particle per reaction and **minimum focus 1.0**. Input species,
amount, mean energy, and focus must all match the recipe. Ordinary decay recipes
accept any nonnegative input energy; photon pair production has explicit energy
windows.

Some useful examples, in recipe output order:

| Input | Outputs | Cross section | Released energy (keV) |
| --- | --- | ---: | ---: |
| Neutron | Proton + electron antineutrino + electron | 1.0 | 1,489 |
| Antineutron | Positron + electron neutrino + antiproton | 1.0 | 1,489 |
| Neutral pion (`pion_naught`) | 2 photons | 0.98 | 135,000 |
| Positive pion | Antimuon + muon neutrino | 0.99 | 34,000 |
| Negative pion | Muon antineutrino + muon | 0.99 | 34,000 |
| Muon | Electron antineutrino + muon neutrino + electron | 1.0 | 105,489 |
| Antimuon | Positron + muon antineutrino + electron neutrino | 1.0 | 105,489 |
| Triton | Helion + electron antineutrino + electron | 1.0 | 19 |
| Photon at 1,120-230,000 keV | Electron + positron | 1.0 | −1,020 |
| Photon at 233,000 keV or higher | Muon + antimuon | 0.5 | −211,000 |

Energy bounds are inclusive. Photons below 1,120 keV, or in the gap between
230,000 and 233,000 keV, have no default Decay Chamber recipe. Negative released
energy means pair production spends incoming beam energy; it does not generate
FE.

The chamber evaluates a whole batch each tick rather than running a timed
crafting operation:

```text
Available reactions = floor(incoming amount / recipe input amount)
Successful reactions = floor(Available reactions × Yield factor + saved fraction)
```

Fractional yield carries forward between ticks. If the batch produces at least
one whole successful reaction, input for **all available reactions** is consumed;
each output amount is its recipe amount multiplied by successful reactions.
Input left over that tick is discarded, including when fractional yield has not
yet produced a whole reaction.

All products of a batch receive the same mean energy. The current implementation
calculates it as `max(0, floor((incoming mean energy + released energy) /
total emitted particle count))`, using the entire batch's emitted count. Thus
larger batches lower the resulting energy per particle. Default output focus
is **1.0**.

The controller displays one input channel and up to three output channels. These
are beam snapshots, not stored inventory; the Decay Chamber screen has no timed
progress bar.

## Troubleshooting

| Symptom | What to check |
| --- | --- |
| Structure will not assemble | Use an odd cube within the size limits, close the shell, and keep solid casing on every edge and corner. Use exactly one Decay Chamber Controller and one central camera. |
| Missing or incorrectly facing beam port | Put all four ports at the centers of the vertical faces, facing outward. Extra beam ports elsewhere are invalid. |
| Invalid interior or beam | Keep the required horizontal cross of Particle Beam blocks clear of detectors. The center must be the camera. Leave unused interior space as air. |
| No valid detectors | Check Manhattan distance to the camera. A Silicon Tracker must be immediately above or below it because the four horizontal neighbors are beam blocks. |
| Input appears but no output | Check Input/Output modes, redstone, forced shutdown, FE, and the input's species, energy, amount, and focus. Low yields can require several supplied ticks before a whole reaction appears. |
| Some products are missing | Configure three Outputs for three-product recipes and connect every required output to a suitable destination. Products are not buffered. |
| Photon beam does nothing | Check the two pair-production energy windows and minimum focus 1.0. |
| More detectors do not improve yield | Yield is capped at 100%. Detectors outside their range do not contribute. |
| Downstream recipe rejects a product | Check its actual mean energy and focus. Output energy depends on the emitted batch size as well as the input beam and recipe. |

## Configuration and custom recipes

Settings are in `config/nuclearcraft/multiblocks.toml`:

| Section | Setting | Default |
| --- | --- | ---: |
| `particle_chambers` | `decay_min_size` | 5 |
| `particle_chambers` | `decay_max_size` | 11 |
| `particle_chambers` | `decay_base_power` | 2,000 FE/t |
| `accelerators` | `beam_connection_reach` | 16 |

Size settings are limited to 5-11; the structure must still be an odd cube.
Detector bonuses and costs above are the built-in values.

Datapacks and KubeJS can define `nuclearcraft:decay_chamber` recipes with a particle
input, ordered particle outputs, cross section, released energy, and priority.
The highest-priority matching recipe wins; equal priorities are resolved by
recipe ID in ascending order. See [KubeJS support](KUBEJS.md) for the recipe
schema and scripting examples.
