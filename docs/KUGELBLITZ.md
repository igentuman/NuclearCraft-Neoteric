# Kugelblitz Chamber

The Kugelblitz Chamber confines a miniature black hole. It generates Forge Energy
(FE) as the black hole loses mass, and uses quantum transformations to convert
items into other materials. Six charged lasers create the black hole; a steady
supply of Subliquid Matter keeps it alive.

This guide describes the NeoForge 1.21.1 implementation and default settings.
Use JEI or EMI for crafting recipes, the Ponder scene for the chamber layout,
and the [KubeJS guide](KUBEJS.md#particle-coolant-and-kugelblitz-recipe-functions)
for custom transformation recipes.

## Blocks and their purpose

| Block | Purpose |
| --- | --- |
| Kugelblitz Chamber Terminal | The single controller. Shows assembly status, mass, feeding, evaporation, stability, FE output, and transformation progress. Its sliders set energy conversion and quantum frequency. |
| Chamber Port | Connects item transport, Subliquid Matter input, and FE extraction to the terminal's shared storage. Also supports redstone monitoring and control. |
| Neutronium Frame | Structural support for the six frame rings and other casing positions. |
| Photon Concentrator | Receives a laser pulse. Exactly one belongs at the center of each of the six outer wall panels. |
| Quantum Flux Regulator | Increases FE generation and reduces the generation-related mass loss. |
| Quantum Transformer | Improves transformation speed and reduces the additional mass loss associated with an unfinished transformation. |
| Event Horizon Stabilizer | Reduces how often the black hole's stability changes. |
| EXPL (Excited Photon Laser) | External, powered laser aimed at a Photon Concentrator. All six must pulse together to create or restabilize the black hole. |
| Black Hole | Created automatically at the chamber center. Pulls in nearby entities and destroys items or creatures that reach it. |

Only regulators, transformers, and stabilizers in the **six outer wall panels**
contribute to their respective counts. Placing these blocks at the eight inner
corner positions does not add their operating benefits.

## Building the chamber

The chamber occupies an **11 × 11 × 11** space, with six identical **5 × 5**
wall panels. It is a shaped structure rather than a filled cubic shell. Allow
additional space outside for the six lasers, including one above and one below.

Use the Ponder scene as a visual construction guide. For a precise layout, take
the black hole's center as `(0, 0, 0)`:

| Part | Position relative to the center |
| --- | --- |
| Six wall panels | Centered at `(±5, 0, 0)`, `(0, ±5, 0)`, and `(0, 0, ±5)`. Each panel extends two blocks in either direction along its plane. |
| Photon Concentrators | At the six panel centers. |
| Six frame rings | In the planes four blocks from the center. In each plane, place frame blocks where one local coordinate is `±3` and the other is from `−2` through `2`. These are 7 × 7 outlines with their four corners omitted. |
| Eight inner corners | At every combination of `(±3, ±3, ±3)`. Use chamber casing blocks here. |
| Interior | Keep the spherical space within four blocks of the center empty. The black hole appears at the center after ignition. |

Fill the wall panels around their concentrators with regulators, transformers,
stabilizers, or frames. **All six panels must have the same block pattern**, not
just equal block counts. Copy the pattern using world-axis coordinates: top and
bottom use X/Z, east and west use Y/Z, north and south use X/Y.

Replace a frame-ring block with the terminal, and other frame-ring blocks with
ports as needed. This keeps the six panel patterns intact. Exactly one terminal
is allowed. Open it and resolve any assembly error before attempting ignition.

Changing a valid chamber's structure can remove its black hole. Plan the panel
balance and transport connections before starting it.

## Ignition and laser bursts

1. Place six EXPLs outside the chamber, each aimed directly at a concentrator.
   A convenient arrangement puts each laser core **10 blocks from the chamber
   center**, giving a five-block core-to-concentrator distance. Leave room for
   each laser's automatically placed body.
2. Supply FE to every laser. Each needs **10,240,000,000 FE per burst** by
   default: **61,440,000,000 FE** for a complete six-laser burst.
3. Wait until all six lasers are fully charged. Trigger a burst through an EXPL
   screen, which also attempts to activate the other charged lasers, or deliver
   simultaneous redstone signals to all six.
4. All six pulse directions must reach the chamber together. Allow the laser
   animation to complete, then check the terminal for a positive mass reading.

Ignition creates a black hole with approximately **100-200 million mass** and
**100 stability**. Prepare feeding before ignition: this initial mass is close
to the evaporation limit.

A further complete burst adds **2-3.99 million mass** and restores **50-99
stability points**, capped at 100. Lasers can therefore recover stability during
operation, but every recovery burst requires recharging all six lasers.

## Feeding and mass limits

Feed **Subliquid Matter** (`nuclearcraft:subliquid_matter`) through the terminal
or a linked Chamber Port. Produce it in the **Subatomic Liquifier** from supported
metals or Quantite; consult JEI or EMI for the available recipes.

Every operating tick, the chamber consumes **all fluid currently in its input
tank**. Each **1 mB adds 10 mass**. The terminal's Feeding and Evaporation values
are mass gained and lost per tick, so equilibrium requires approximately:

```text
Subliquid Matter input (mB/t) = Evaporation / 10
Mass change per tick = Feeding − Evaporation
```

| Mass | Consequence |
| --- | --- |
| Below **100,000,000** | The black hole evaporates and must be ignited again. |
| Around **4.85 billion** | Most favorable mass region for stability. |
| Above **9 billion** | Additional chance of stability loss. |
| Above **10,000,000,000** | Meltdown; the black hole disappears and causes an explosion with default radius **10**. |

Meter the fluid supply and monitor mass instead of dumping a large reserve into
the tank. Mass loss changes with mass, conversion settings, installed blocks,
active transformations, and low-stability fluctuations. There is no single feed
rate that suits every operating state.

Dropped items and creatures consumed by the black hole **do not add mass**.
Use the fluid input for feeding and the item input for transformations. Keep
players and livestock away from the center and from firing laser beams.

The terminal's **Frequency** slider selects an item transformation frequency.
Its values are unrelated to sculk sensor calibration and do not control emitted
vibrations. Changing this slider does not stabilize the black hole.

The terminal shows the exact stability value. Chamber Ports can report mass
through redstone, but do not offer a stability output mode.

## Stability

Stability ranges from **0 to 100**. It can change every five ticks, with random
losses more likely when mass is far from the favorable middle region. Small
random recoveries are also possible. Event Horizon Stabilizers suppress these
updates: with `S` counted stabilizers, the chance to skip an update is
`min(S / 96, 1)`. They slow both deterioration and natural recovery rather than
directly restoring stability.

When stability falls **below 40**, the active black hole emits vibrations on
**channel 15** (`nuclearcraft:blackhole_vibration`). It emits from the chamber
center every operating tick while unstable. Emission stops when stability
returns to **40 or higher**, or the black hole disappears.

Channel 15 is also used by other events, including explosions and entity deaths.
Calibration selects the frequency, not the source. Position or isolate the sensor
to reduce unrelated triggers.

Below **20 stability**, evaporation can fluctuate in either direction. Below
**10**, additional random mass-loss spikes can occur. These fluctuations make
feeding harder to balance. Zero stability does not itself trigger an explosion;
the explosion condition is excessive mass.

For predictable operation, hold mass near the middle region, include stabilizers
in the panels, and use a charged six-laser burst when stability needs recovery.

## FE generation and the conversion slider

The **Transformation vs Energy Generation** slider sets the energy-conversion
rate. Higher percentages favor FE generation; lower percentages favor quantum
transformation speed. The default rate is **7%**. Keep it above zero for FE
generation; **0% produces no FE** in the current implementation.

FE generation rises with the conversion rate and number of Quantum Flux
Regulators, with diminishing returns from both. **Smaller black holes generate
more FE**, but also evaporate faster and operate closer to the minimum mass.
Higher conversion rates also increase generation-related mass loss. Regulators
help reduce that loss.

For a positive conversion rate, nominal generation is:

```text
FE/t = (10,000,000,000 / max(mass, 100,000,000)) × 1,000
       × (ln(conversion_percent × 50,000) + 1)
       × (ln(max(1, regulator_count) × 5,000) + 1)
       × chamber_generation_multiplier × global_generation_multiplier
```

`ln` is the natural logarithm. Output is rounded down and capped at
2,147,483,647 FE/t. The terminal has a **100,000,000 FE** buffer; extract FE
through the terminal or ports. The displayed Output is the calculated generation
rate, before transformation costs and available buffer space. A full buffer
discards excess generation while mass loss continues.

## Quantum transformation recipes: linked to the seed

Transformations need an active black hole, a matching input item and quantity,
the correct frequency, enough stored FE, and space in the output slot.

There are two kinds of default recipes:

| Recipe group | Behavior |
| --- | --- |
| **1 Tough Alloy Ingot → 2 BSCCO Dust** | Fixed conversion, with a seed-dependent frequency. |
| **1 Stack Upgrade → 1 Quantum Upgrade** | Fixed conversion, with a seed-dependent frequency and twice the base processing time. |
| Material transformation pool | The world seed shuffles outputs among the pool's representative items. Discover which input produces which output in your world. |

The default pool includes iron, gold, copper, netherite, tin, lead, silver,
aluminum, uranium, steel and bronze ingots; diamond, emerald, lapis and ruby;
redstone, glowstone and coal dust; blue, white, red, green and yellow dyes;
Nether Stars, Eyes of Ender, Ender Pearls and Dragon's Breath. Tag-based entries
depend on which items are available in the modpack.

The mapping is **deterministic for a given world seed and recipe pool**, rather
than rerolled for each operation. A mapping can even return the same material.
Changing the modpack's pool can change the mapping. JEI and EMI show an unknown
input for these discovery recipes; their displayed output does not reveal the
actual input mapping in your world.

To discover a transformation:

1. Insert a supported input and try frequency values **0 through 14**. Although
   the control can reach 15, generated target frequencies use only 0-14.
2. When progress starts, record the input and frequency. The input is consumed
   when the recipe starts.
3. Wait for completion and record the actual output. Keep a table for your
   world, then automate the combinations you need.

Frequency is derived from the world seed and the recipe's **declared output
item**, before the material pool shuffle. Custom fixed-output recipes also need
the correct seed-dependent frequency.

A default recipe requires **50 processing steps**, each costing **1,000 FE**;
the Quantum Upgrade recipe requires 100 steps. Recipe modifiers can change both.
More Quantum Transformers and a lower conversion percentage can increase steps
per tick, though speed is rounded down with a minimum of one step per tick.
At 100% energy conversion, transformations can still advance at that minimum.
An unfinished recipe also adds mass loss, reduced by transformer count.

Insufficient stored FE pauses progress. A blocked output slot holds the finished
result until space is available. Extract products regularly.

## Redstone and KubeJS

Chamber Ports offer output modes for stored energy, mass, transformation progress,
and input item quantity. Their input modes can set Frequency directly from a
0-15 redstone signal, or set the conversion percentage from signal strength
(`signal × 100 / 15`, rounded down). Use mass monitoring to regulate feeding.

Modpack authors can add or replace transformations with the
[`kugelblitz_chamber` KubeJS recipe function](KUBEJS.md#particle-coolant-and-kugelblitz-recipe-functions).
It accepts an input, output, time modifier, and power modifier. A recipe whose
representative input and output are the same item joins the seed-shuffled pool;
a different output creates a fixed conversion. Both kinds retain seed-dependent
frequency matching.
