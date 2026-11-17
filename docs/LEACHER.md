# Ore Leacher

The Ore Leacher extracts underground resources and dissolves them into slurry.
Start by surveying a chunk with an Analyzer, then build a Leacher and four Pumps
in that chunk. Supply Forge Energy (FE) and Aqua Regia Acid, and send the slurry
to further processing.

There are two ways to select the ore source:

| Source item | What the Leacher extracts |
| --- | --- |
| Research Paper from an Analyzer | A finite, virtual ore vein in the surveyed chunk. These resources are separate from the world's placed ore blocks. |
| Filled Minecraft map | Real ore blocks below the Leacher, inside its own chunk. Extracted blocks are removed from the world. |

Use JEI or EMI for machine crafting recipes, available vein compositions, and
the processing recipes in your modpack. Amounts below describe the default recipes.

## Finding a vein with the Analyzer

1. Place an **Analyzer** in the chunk you want to survey. Press **F3 + G** to show
   chunk borders. The Analyzer surveys its own chunk; its height does not select
   a different vein.
2. Connect FE and put **one Paper** in its input slot.
3. Wait for the **Research Paper** output. Without upgrades, the recipe takes
   **500 ticks (25 seconds)** at **200 FE/t**.
4. Hover over the Research Paper. Its tooltip shows the vein type, or **No Vein**,
   and the survey position. Although the tooltip calls this “Chunk Position,”
   the displayed X/Y/Z values are the Analyzer's block coordinates.
5. Keep papers for useful veins. If the result is No Vein, move the Analyzer to
   another chunk and survey again. Moving within the same chunk does not change
   its vein assignment.

After a paper survey, that Analyzer stops processing at its surveyed position.
Move it to a different position before using it again. Take out the Research
Paper before relocating the machine.

A vein can contain several materials. Check the **Ore Veins** category in JEI or
EMI to see its possible ores and their weights. The Research Paper identifies
the vein; it does not report how much remains. Extraction gradually depletes the
chunk's virtual reserve, and surveying it again does not replenish it.

## Using a world map

### Recording surveyed chunks

Use your world map or waypoint mod to mark useful survey locations. Record the
vein name and the Research Paper's X/Z position, and keep track of the dimension
where you made the survey. Return to that chunk to build the Leacher; the paper's
exact block position and Y level are not required.

The Analyzer's current map analysis does **not** draw virtual veins on a map or
automatically add waypoints to an external world map. Use Research Papers to
discover virtual veins and add your own markers.

### Using a Minecraft map as the ore source

For extraction from actual ore blocks instead of a virtual vein:

1. Use an Empty Map to create a **filled Minecraft map**.
2. To analyze it, put it in a powered Analyzer. The default analysis recipe takes
   **1,100 ticks (55 seconds)** at **500 FE/t** and returns the map with its map
   identity preserved and an analysis flag attached. Use an Analyzer that has
   not stopped after a paper survey, or relocate it first.
3. Put the filled map in the Leacher's **ore-source/catalyst slot**. Build the
   same four-Pump arrangement and supply FE and acid as described below.

The current Leacher accepts a filled map without checking its analysis flag.
It scans **its own 16 × 16 chunk below the machine**, down to the world's minimum
height. The map's center, explored area, and zoom do not change this range.
Only ore blocks with a matching Leacher recipe are extracted, then processed
into slurry. Place the Leacher above the ores you want to reach.

## Building the leaching site

Bring **one Leacher, four Pumps, the Research Paper or filled map, an FE supply,
and Aqua Regia Acid**. You also need fluid transport and storage or processing
for the output slurry.

1. Place the **Leacher anywhere inside the target chunk**. For Research Paper
   mode, use the chunk and dimension you surveyed. It does not need to stand in
   the center or at the Analyzer's original height.
2. Place **one Pump at each of the four block corners of that same chunk**.
   A chunk is 16 × 16 blocks, so the corner blocks are 15 blocks apart along
   each edge. Do not place the Pumps across the border in neighboring chunks.
3. For a simple layout, place all four Pumps at the **same Y level as the
   Leacher**. Each Pump may also be one block above or one block below it.
4. Put **two solid blocks directly beneath each Pump**. Leave the Pumps idle,
   with their collector inputs empty. A Pump running a collection recipe is
   invalid for leaching. The Pumps do not need acid lines or power to satisfy
   the leaching check; the Leacher receives the acid and FE.
5. Open the Leacher and check its four Pump indicators. Hover over an indicator
   to see whether that Pump is valid. Validation refreshes about every four
   seconds, so allow a short delay after changing the layout.

For example, in the chunk covering X **0-15** and Z **0-15**, with the Leacher at
Y **64**, a valid same-height Pump layout is:

| Pump | X | Y | Z |
| --- | --- | --- | --- |
| Corner 1 | 0 | 64 | 0 |
| Corner 2 | 0 | 64 | 15 |
| Corner 3 | 15 | 64 | 15 |
| Corner 4 | 15 | 64 | 0 |

Each Pump in this example needs solid support blocks at Y **63** and **62**.
The Leacher can occupy any other block inside that chunk at Y 64.

## Starting extraction

1. Insert the surveyed **Research Paper** into the Leacher's ore-source/catalyst
   slot, or insert a filled map for physical ore extraction. The source item is
   retained while the machine runs.
2. Connect **FE to the Leacher**.
3. Feed **Aqua Regia Acid** into its input tank. The default Chemical Reactor
   recipe combines **250 mB Nitric Acid + 750 mB Hydrochloric Acid** into
   **1,000 mB Aqua Regia Acid**.
4. Connect the slurry output tank to storage or downstream processors. Configure
   the input and output tanks' faces in **Side Configuration** as needed; see
   [Processors](PROCESSORS.md#slot-and-side-configuration).
5. Allow processing to run. Each default recipe uses **one internally gathered
   ore + 250 mB Aqua Regia Acid** to produce **1,000 mB of that material's slurry**,
   taking **200 ticks (10 seconds)** at **50 FE/t**.

The Leacher gathers the ore itself; its ore input is internal. A mixed vein can
produce different slurries over time, so keep draining the output tank to allow
the next material to process.

To recover dusts, send slurry through a **Centrifuge**, then a **Crystallizer**:

- **1,000 mB slurry → 800 mB clean slurry**, plus 50 mB Hydrochloric Acid,
  50 mB Nitric Acid, and 10 mB Calcium Sulfate Solution. Make room for every
  Centrifuge output.
- **400 mB clean slurry → 2 dusts** in the Crystallizer.

## Troubleshooting

| Symptom | What to check |
| --- | --- |
| **Pumps Error** | All four corner Pumps must be in the Leacher's chunk, within one Y level of it, on two solid blocks, and idle. Wait for validation to refresh. |
| **Wrong Position** | With a Research Paper, place the Leacher in the surveyed chunk. Use the dimension where you surveyed. |
| **No Source** | Insert the Research Paper in the ore-source slot and check that it has survey data. A paper reporting No Vein cannot supply virtual ore. |
| **No Acid** | Fill the input tank with Aqua Regia Acid. Other fluids do not satisfy the extraction recipes. |
| Has acid but no progress | Check FE, the required acid amount, redstone control, output space, and fluid side settings. |
| Research Paper mode stops producing ore | The virtual vein may be depleted. Survey another chunk; a new paper for the same chunk does not reset its reserve. |
| Map mode produces nothing | Check for supported ore blocks below the Leacher inside its own chunk. Ores above it or in neighboring chunks are outside the scan. |

Virtual vein generation, size, rarity, and ore selection are controlled by
`common.toml`; see [Mod configuration](CONFIGURATION.md). Physical ore generation
is separate; see [World generation](WORLD_GENERATION.md).
