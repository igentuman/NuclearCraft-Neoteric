# Collision Chamber

The Collision Chamber combines two opposing particle beams to produce new
particles. Use it for light-nucleus reactions, matter-antimatter collisions,
and the production of heavier particles. It consumes Forge Energy (FE); reaction
energy becomes energy carried by the output particles, not generated FE.

This guide describes the NeoForge 1.21.1 implementation with default settings.
Check JEI or EMI for the recipes available in your modpack.

## Blocks and dimensions

| Block | Purpose |
| --- | --- |
| Collision Chamber Controller | Exactly one, placed in a vertical wall and facing outward. |
| Particle Chamber Casing | Shell, including every edge and corner. |
| Particle Chamber Glass | Optional replacement for casing in wall, floor, or roof faces, away from edges. |
| Particle Chamber Beam Port | Two end inputs and four side outputs. All face outward. |
| Particle Chamber Camera | At least two, on the central beam axis. |
| Particle Beam | Connects the inputs, cameras, and outputs inside the chamber, and links machines outside. |
| Particle Chamber Port | Optional service connection for FE and redstone control. |
| Detectors | Optional interior blocks that improve collection efficiency and increase FE demand. |

Build a closed rectangular shell with a **square cross-section**:

- **Width and height:** 5-11 blocks, including the shell; width must equal height.
- **Length:** 13-21 blocks, including the two end walls.
- **Orientation:** the long beam axis is horizontal.

A **5 × 5 × 17** chamber is a convenient starting layout. The preferred length
setting defaults to 17, but other lengths in the allowed range are valid.

## Building a working chamber

1. Build the shell, leaving the interior empty. Use solid casing on every edge
   and corner. Put the controller on a vertical wall, away from the edges.
2. Place one Beam Port at the center of each end wall, facing outward. These
   are the two **INPUT** ports.
3. Fill the entire interior line between those ports with Particle Beam blocks
   and at least two Cameras. Every block on this axis must be a beam or camera.
4. Place exactly two **OUTPUT** Beam Ports in each of the two long side walls,
   at the height of the central beam axis. Each output must line up across the
   width with a Camera. Connect that camera to the port with an uninterrupted
   straight row of Particle Beam blocks.
5. Add a service port if needed and install detectors around the beam axis.
   Leave all beam corridors clear. Other interior spaces may remain air.
6. Open the controller and resolve any assembly error. Check the port modes:
   end ports must be INPUT and side ports OUTPUT. Newly configured ports receive
   their roles from the structure; use a Multitool to correct changed modes.

The two outputs on one wall must occupy different positions along the length.
Putting opposite output pairs at the same two camera positions is the easiest
layout. Extra cameras are allowed on the central axis, but extra beam ports
elsewhere in the shell are invalid.

### Example: 5 × 5 × 17

Take `(0, 0, 0)` as one outside bottom corner. X runs along the 17-block
length, Y points up, and Z runs across the 5-block width:

| Part | Coordinates |
| --- | --- |
| Shell bounds | X = 0-16, Y = 0-4, Z = 0-4. |
| Input ports | `(0, 2, 2)` facing west; `(16, 2, 2)` facing east. |
| Cameras | `(2, 2, 2)` and `(14, 2, 2)`. |
| Main beam | `(X, 2, 2)` for X = 1-15, except the two camera positions. |
| North outputs | `(2, 2, 0)` and `(14, 2, 0)`, facing north. |
| South outputs | `(2, 2, 4)` and `(14, 2, 4)`, facing south. |
| Output corridors | `(2, 2, 1)`, `(2, 2, 3)`, `(14, 2, 1)`, `(14, 2, 3)`: Particle Beam blocks. |
| Controller | `(8, 1, 4)`, facing south. |
| Optional service port | `(9, 1, 4)`, facing south. |
| Optional Silicon Tracker | `(8, 1, 2)`, one block below the beam axis. |

Close the rest of the shell with casing or permitted glass. This layout also
appears in the Collision Chamber Ponder scene.

## Supplying beams and starting operation

Supply FE through the controller or a linked Particle Chamber Port. The
controller has a **1,000,000 FE** energy buffer. Apply redstone to the controller,
or supply a control signal through a linked service port, and ensure the
controller has not been shut down through computer control.

Connect a particle source machine to each input. External connections must be
straight, with Particle Beam blocks filling every space between the two ports.
The source and receiving ports must face each other. The default maximum
port-to-port distance is **16 blocks**, shared with accelerator beam connections.
Keep the connection and destination chunks loaded.

Both input beams must reach the chamber in the same processing tick. Inputs
and outputs are cleared each tick: **the chamber does not accumulate particles
while waiting for another beam, FE, redstone, or a receiving machine**. Connect
the required output machines before running the sources. Products that cannot
be transferred during that tick are discarded.

The controller screen shows two input channels and four output channels. Use
their particle tooltips to check species, amount, energy, and focus. An output
display records production; it does not guarantee that a downstream machine
accepted those particles.

## Recipes and collection efficiency

A recipe checks each incoming beam's species, amount, energy range, and minimum
focus. Energy limits apply **to each beam separately**. All default recipes
require at least **1.0 focus** and one particle from each input per reaction.
Default recipes allow the two input species in either order.

Examples from the default recipe set:

| Inputs | Energy of each beam | Products per successful reaction | Cross-section |
| --- | --- | --- | ---: |
| Proton + Neutron | 0-30,000 keV | Deuteron + Photon | 0.5 |
| Proton + Proton | 700-10,000 keV | Deuteron + Positron + Electron Neutrino | 0.25 |
| Deuteron + Triton | 700-10,000 keV | Alpha + Neutron | 0.5 |
| Proton + Antiproton | 0-50,000,000 keV | 4 Pion Plus + 4 Pion Naught + 4 Pion Minus | 1.0 |
| Electron + Positron | 232,076-348,114 keV | Muon + Antimuon | 0.10 |

Not every input pair produces a collected reaction. The collection factor is:

```text
efficiency = 1 + sum of valid detector bonuses
symmetry = 1 − abs(Ea − Eb) / (Ea + Eb)
collection factor = clamp(cross-section × efficiency × symmetry, 0, 1)
```

Ea and Eb are the incoming beam energies. Equal, nonzero energies give symmetry
1; unequal energies reduce collection. Two zero-energy beams have symmetry 0,
even if their species and energy ranges match a recipe.

The smaller available input quantity limits the number of reaction attempts.
The chamber multiplies that number by the collection factor, emits whole
successful reactions, and carries the fractional remainder forward. For
example, 100 protons in each beam at the same energy within 700-10,000 keV
produce 25 successful proton-proton reactions without detectors: 25 deuterons,
25 positrons, and 25 electron neutrinos. The other attempts do not yield products.

Output focus is the lower input focus. Output energy depends on the incoming
collision energy, the recipe's released energy, and the total emitted particle
count. Increasing input energy is therefore useful only within the selected
recipe's allowed range.

Products use output channels in recipe order. Channels are assigned along the
chamber's length, with the two sides at each position grouped together. Check
the controller's channel displays when connecting receivers.

**Current output limit:** the structure has four output channels. Some default
recipes list five product entries; the current implementation emits only the
first four entries. A fifth entry is not routed to another port.

## Detectors and FE cost

Detector range is measured across the chamber, from the central beam axis:
`abs(horizontal offset) + abs(vertical offset)`. Position along the length does
not affect range. Detectors outside their range are permitted in the interior
but contribute neither efficiency nor energy cost.

| Detector | Maximum distance from axis | Efficiency bonus per valid block | Added FE/t |
| --- | ---: | ---: | ---: |
| Silicon Tracker | 1 | +0.15 | 20,000 |
| Bubble Chamber | 2 | +0.075 | 2,000 |
| Wire Chamber | 2 | +0.10 | 10,000 |
| EM Calorimeter | 3 | +0.05 | 2,000 |
| Hadron Calorimeter | 5 | +0.025 | 1,000 |

The default operating cost is **2,000 FE/t plus the cost of all valid detectors**.
For example, one valid Silicon Tracker gives efficiency 1.15 at **22,000 FE/t**.
The chamber must hold enough FE for the entire operating cost before processing.
It charges that cost on a tick that consumes inputs and produces particles.

More detectors improve collection until the factor reaches 1. They do not
increase the quantity of incoming particles or remove recipe requirements.
Avoid adding expensive detectors once collection is already capped; their FE
cost still applies. Detectors must never replace cameras or required beam blocks.

## Troubleshooting

| Symptom | What to check |
| --- | --- |
| Structure will not assemble | Square cross-section, allowed outside dimensions, closed shell, solid casing edges, exactly one controller facing outward. |
| Camera or beam layout error | At least two cameras on the central axis; every other axis block is Particle Beam. All four output corridors must lead straight to cameras. |
| Port error | Exactly two end inputs and two outputs on each long side wall, all at axis height and facing outward. Remove extra beam ports. |
| Only one input appears | Check both source machines, input modes, straight external beam corridors, facing, reach, and loaded chunks. Both beams must arrive in the same processing tick. |
| Inputs appear but no products | Check redstone, shutdown state, stored FE, recipe species, each beam's energy, and focus. Avoid two zero-energy beams. Small quantities and low collection can take multiple ticks to yield a whole reaction. |
| Output appears in the GUI but is lost | Connect a formed receiving machine with its port in INPUT mode. Check facing, range, beam corridor, and receiver acceptance; products are not stored for later delivery. |
| Detectors do nothing | Check their distance from the central axis and whether collection is already capped at 1. |
| Fifth recipe product is missing | The current chamber exposes only four output channels; only the first four product entries are emitted. |

## Configuration

Collision settings are in `[particle_chambers]` in
`config/nuclearcraft/multiblocks.toml`:

| Setting | Default | Effect |
| --- | ---: | --- |
| `collision_min_transverse_size` | 5 | Minimum outside width and height. |
| `collision_max_transverse_size` | 11 | Maximum outside width and height. |
| `collision_min_length` | 13 | Minimum outside length. |
| `collision_max_length` | 21 | Maximum outside length. |
| `collision_preferred_length` | 17 | Preferred length value; not a required assembly length. |
| `collision_base_power` | 2,000 | Base operating FE/t before detector costs. |

External beam reach uses `beam_connection_reach` in `[accelerator]` in the same
file, default 16. Datapacks and [KubeJS](KUBEJS.md) can change the available
collision recipes, including input requirements, cross-sections, and products.
