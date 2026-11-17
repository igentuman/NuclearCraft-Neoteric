# Linear Accelerator

The Linear Accelerator creates a particle beam from an item or fluid source, or
boosts a beam supplied by another machine. RF amplifier sections raise particle
energy, quadrupole electromagnets improve focus, and coolers remove heat.
The output can feed another accelerator or a particle chamber.

This guide describes the NeoForge 1.21.1 implementation. Values below use the
default configuration; modpacks can change components, sources, and coolant recipes.

## Blocks and connections

| Block | Purpose |
| --- | --- |
| Linear Accelerator Controller | Exactly one per structure; displays assembly status, beam statistics, temperature, and control signal. |
| Accelerator Casing | Shell, including every edge and corner. |
| Accelerator Casing Glass | Optional replacement for shell blocks away from edges and corners. |
| Accelerator Port | Service access for FE, fluids, items, and redstone control. |
| Accelerator Ion Source Port | Optional beam endpoint for inserting source items or source fluid. Does not accept FE and does not allow automated extraction. |
| Accelerator Beam Port | Beam input or output at an end face's center. |
| Particle Beam | Continuous centerline inside the machine and straight connections between machines. |
| RF Amplifier | Adds voltage when installed as a complete eight-block section. |
| Electromagnet | A straight electromagnet used in a dipole pair or quadrupole. Sloped electromagnets are not accepted. |
| Electromagnet Yoke | Optional interior filler. |
| Cooler | Passive cooling when its placement rules are satisfied. |

The controller has one source-item slot and three fluid tanks: source fluid,
coolant input, and heated coolant output. Service ports access the controller's
shared storage. Configure the relevant item/fluid sides and slots for your
transport setup, and extract heated coolant continuously.

## Building the structure

Build a straight, horizontal **5 × 5 × L** tube, with all measurements including
the shell. Either horizontal axis can be the long axis. The default permitted
length is **6-100 blocks**, giving **L − 2** interior beam sections.

1. Build the casing frame and close all six faces. Every edge and corner must
   use ordinary Accelerator Casing. Glass, service ports, and the controller
   belong in face interiors.
2. Install exactly one controller on a vertical face, facing outward.
3. Place an endpoint at the exact center of each 5 × 5 end face. Both endpoints
   must face outward along the tube.
4. Fill the centerline between the endpoints with Particle Beam blocks.
5. Build the surrounding interior sections using the patterns below.
6. Add service ports for power and coolant. Open the controller and resolve any
   assembly error before starting the beam.

There are two endpoint arrangements:

| Mode | Endpoints |
| --- | --- |
| Local source | One Ion Source Port and one Beam Port. The Beam Port is assigned output automatically. The source can be at either end. |
| Beam booster | Two Beam Ports. Assembly assigns one input and one output; inspect their modes before connecting the upstream machine. |

An Ion Source Port elsewhere on the shell is invalid, as is a Beam Port away
from the center of an end face. Two Ion Source Ports are not allowed.

### Interior sections

Each diagram shows the interior **3 × 3** cross-section looking along the beam.
A one-block shell surrounds it. `B` is Particle Beam, `R` is an RF Amplifier,
`M` is an Electromagnet, and `.` is air, Electromagnet Yoke, or a Cooler.

```text
RF section     Quadrupole      Vertical dipole  Horizontal dipole  Filler section
R R R          . M .           . M .            . . .              . . .
R B R          M B M           . B .            M B M              . B .
R R R          . M .           . M .            . . .              . . .
```

- An RF section requires all eight amplifiers to be the same tier.
- A quadrupole requires four same-tier magnets directly above, below, left,
  and right of the beam.
- A dipole requires two same-tier magnets on opposite sides of the beam.
- Magnets cannot occupy the four diagonal positions around the beam.
- RF amplifiers and magnets cannot share a section. Different sections may use
  different tiers or patterns, and filler-only sections are valid.

Assembly does not require RF or magnet sections. To increase a charged
particle's energy, include RF sections; use quadrupoles to counter focus loss.
Dipoles are accepted, but their field does not add energy or focus in a linear
accelerator.

### Small starter layout

With the default size preset, a **5 × 5 × 6** accelerator has four interior
sections. A useful starting arrangement is:

```text
Ion Source → RF → Quadrupole → RF → Filler/coolers → Beam Output
```

Use 16 Basic RF Amplifiers, four Basic Electromagnets, and four Particle Beam
blocks for the interior. Fill unused positions with air, yokes, or suitably
placed coolers. Supply hydrogen to produce protons, connect FE through a service
port, and provide coolant and an output route.

This arrangement has 400,000 V total voltage. At signal 15, a default proton
source starting at 0 keV leaves at **400 keV**. Processing costs **22,000 FE per
active tick**: 10,000 base FE, 16 × 500 FE for the amplifiers, and 4 × 1,000 FE
for the magnets. Its component heat production is **900 heat per active tick**.

## Particle sources

Local source injection requires an assembled Ion Source Port. Supply either a
recognized item or source fluid. A usable item takes priority over fluid.
Default sources start at **0 keV** with **0.4 focus**.

| Source item ID (`nuclearcraft:`) | Particle | Total particles per item |
| --- | --- | ---: |
| `source_calcium_48` | Calcium-48 ion | 5,000,000 |
| `source_iridium_192` | Positron | 1,000,000 |
| `tungsten_filament` | Electron | 50,000,000 |
| `antideuterium` | Antideuteron | 50,000,000 |
| `antihelium` | Antialpha | 50,000,000 |
| `antihelium3` | Antihelion | 50,000,000 |
| `antihydrogen` | Antiproton | 50,000,000 |
| `antitritium` | Antitriton | 50,000,000 |

Item sources emit up to **10,000 particles per tick** while available. Charge
is tracked on the item; an exhausted dedicated source item can remain in the
slot and need removal.

| Source fluid | Particle |
| --- | --- |
| Diborane | Boron ion |
| Hydrogen | Proton |
| Deuterium | Deuteron |
| Tritium | Triton |
| Helium | Alpha |

Fluid injection consumes **1 mB per tick** for **10,000 particles**. Source
fluid and coolant use separate tanks.

## Power, control, and beam quality

A positive control signal enables acceleration. Signal 15 gives full voltage;
lower signals scale the energy gain proportionally. The machine uses the
highest redstone strength at the controller or its service ports unless a
computer override is active. Signal 0 stops source injection and acceleration.

For each processed packet:

```text
energy gain (keV) = floor(total voltage × |particle charge| / 1000 × signal / 15)
FE cost          = base energy requirement + sum of all magnet/amplifier power costs
```

Each complete RF section contributes one amplifier's voltage, rather than eight
times that voltage. All eight blocks contribute to FE demand. Each magnet
section likewise contributes one component's heat value, while every magnet
contributes its power cost. Lowering the signal reduces energy gain but does
not reduce this per-packet FE cost or component heat production.

Quadrupole fields add together and increase focus according to particle charge.
Focus loss increases with beam length, particle count, and charge magnitude:

```text
loss = beam_attenuation × distance × (1 + |charge| × sqrt(particle count / beam_scaling))
output focus = max(0, input focus − interior loss + total quadrupole field × |charge|)
```

The interior distance is `L − 2`. External connections apply another focus loss
for the intervening Particle Beam blocks. Short connections preserve focus.
Neutral particles receive no RF energy gain or quadrupole focus gain.

### Connecting the output

Align the output and receiving input ports so they face each other. Fill every
intervening block with Particle Beam. Air gaps, obstructions, unloaded chunks,
and a receiving port facing the wrong way prevent transfer.

By default, the receiving port must be within **16 blocks of the output port**,
which permits at most 15 intervening Particle Beam blocks. The receiving
structure must be assembled and its port must accept input.

**Beams are transient.** Input and unused output are cleared each machine tick;
there is no persistent particle buffer. Source material and FE can be consumed
even when the next machine cannot receive the output. Complete and enable the
downstream path before switching on the source.

## Cooling and overheat protection

Passive coolers remove heat only if their placement rules are met. Read each
cooler's tooltip: rules count face-adjacent blocks, including components in
neighboring sections. An incorrectly placed cooler is accepted as filler but
provides no cooling.

Fluid cooling converts coolant into a heated output:

| Coolant input | Output | Recipe amount, input → output | Heat removed per input mB |
| --- | --- | --- | ---: |
| Water | Steam | 100 mB → 100 mB | 1 |
| Technical Water | High Pressure Steam | 100 mB → 100 mB | 1 |
| Liquid Nitrogen | Nitrogen | 1 mB → 1 mB | 1,000 |
| Liquid Helium | Helium | 1 mB → 1 mB | 1,000 |

Cooling is limited by available coolant and heated-output space. Passive
cooling runs before fluid cooling; both continue while assembled even with
signal 0 or insufficient FE. Fluid cooling operates in whole recipe amounts,
so water needs at least 100 mB available for an operation.

Temperature is stored heat divided by heat capacity. Heat capacity is the
entire 5 × 5 × L bounding volume multiplied by `heat_capacity_per_block`.
The lowest temperature limit among installed magnets and amplifiers sets the
machine's limit; upgrading a component can lower its allowed temperature.

Reaching the limit triggers an overheat shutdown. The default cooldown is
**1,200 ticks (60 seconds at 20 TPS)**. The timer is refreshed while the machine
remains too hot, then counts down once cooling brings it below the limit.
Keep coolant flowing and extract the heated output during recovery.

## Troubleshooting

| Symptom | What to check |
| --- | --- |
| Structure will not assemble | Use a horizontal 5 × 5 tube within the selected length range, casing on every edge/corner, exactly one outward-facing controller, and outward-facing endpoints at the end-face centers. |
| Interior validation fails | Keep the centerline continuous. Use complete same-tier RF rings or valid magnet pairs/quadrupoles; do not mix magnets and amplifiers in one section. |
| No beam is produced | Check assembly, positive control signal, FE for the full processing cost, overheat cooldown, and an available source or incoming beam. Local injection requires an Ion Source Port. |
| Fluid source is not consumed | Check that it is in the source tank, is a recognized source, and is not being superseded by an item source. |
| Beam energy does not rise | Add complete RF sections, increase the control signal, and check that the particle is charged. Dipoles and quadrupoles do not add voltage. |
| Beam focus is too low | Add quadrupoles, use stronger magnets, or shorten the accelerator and its external connections. |
| Downstream machine receives nothing | Check port modes, facing, continuous Particle Beam blocks, connection reach, loaded chunks, and whether the receiver accepts that beam. |
| Temperature keeps rising | Check cooler placement rules, coolant supply, and heated-output capacity. A full output tank stops fluid cooling. |
| Accelerator stays off after cooling | Wait for the overheat timer to expire and check for a computer override holding the signal at 0. |

## Configuration and automation

Settings are in the `[accelerators]` section of
`config/nuclearcraft/multiblocks.toml`:

| Setting | Default | Effect |
| --- | ---: | --- |
| `scale_preset` | 1 | Outside length: preset 1 = 6-100, preset 2 = 60-1,000, preset 3 = 600-10,000. Cross-section stays 5 × 5. |
| `base_energy_requirement` | 10,000 | Base FE cost per processed packet, before component costs. |
| `beam_attenuation` | 0.02 | Focus loss coefficient. |
| `beam_scaling` | 10,000 | Particle-count scale used in focus loss. |
| `heat_capacity_per_block` | 25,000 | Heat capacity per block of bounding volume. |
| `overheat_cooldown_ticks` | 1,200 | Recovery delay after overheating. |
| `beam_connection_reach` | 16 | Maximum output-to-receiver distance. |

ComputerCraft and OpenComputers integrations expose `getParticleInfo()`,
`getStats()`, `getTemperature()`, `getMaxTemperature()`, `isOverheated()`,
`getOverheatCooldown()`, and `getControlSignal()`. Use `getBeamPortsInfo()` to
inspect port positions/modes and `setBeamPortMode(id, mode)` to select `input`,
`output`, or `disabled` with a 1-based port ID.

`setEnergyPercentage(percentage)` sets a computer control override, rounded to
a signal from 0-15; values below 5% select 0. Call `releaseControl()` to return
to redstone control. This percentage controls the voltage fraction, not the
FE cost. `getCoolingRate()` reports heat removed by fluid coolant, excluding
passive coolers.

## Implementation references

- [Structure validation](../src/main/java/igentuman/nc/multiblock/accelerator/LinearAcceleratorValidator.java)
- [Interior section rules](../src/main/java/igentuman/nc/multiblock/accelerator/AcceleratorSlice.java)
- [Source injection and processing](../src/main/java/igentuman/nc/multiblock/accelerator/LinearAcceleratorLogic.java)
- [Energy and focus calculations](../src/main/java/igentuman/nc/multiblock/accelerator/LinearParticleProcessor.java)
- [Cooling and control](../src/main/java/igentuman/nc/multiblock/accelerator/AbstractAcceleratorLogic.java)
- [Default particle sources](../src/main/java/igentuman/nc/particle/ParticleSourceCatalog.java)
- [Default coolant recipes](../src/main/java/igentuman/nc/datagen/recipe/particle/AcceleratorCoolantRecipes.java)
