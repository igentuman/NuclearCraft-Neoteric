# Engineer's Crafting Table

The Engineer's Crafting Table combines a searchable storage terminal, a normal
3×3 crafting grid, and powered autocrafting. Teach it recipes with Crafting
Patterns, then request an item: it can craft missing intermediate ingredients
and send processing steps to NuclearCraft machines connected through a universal
pipe network.

## Getting started

You will need:

- An Engineer's Crafting Table and an FE power supply.
- At least one NuclearCraft storage container item for ingredients and results.
- Blank Crafting Patterns for recipes you want to automate.
- For machine recipes: the appropriate processors, universal pipes and pipe
  connectors, plus barrels for any fluids involved.

1. Place the table and connect FE power.
2. Open it and put NuclearCraft container items in the nine storage slots.
   These slots hold the storage items themselves, including their contents.
3. Add ingredients to those containers. You can fill them before inserting them,
   or deposit items through the terminal afterward.
4. Use the normal crafting grid for manual recipes, or open the Pattern Encoder
   to teach the table recipes for autocrafting.

## Storage and manual crafting

The nine storage slots accept NuclearCraft **containers** for items and
**barrels** for fluids. Ordinary chests and other mods' storage items cannot be
inserted here. A chest placed beside the table does not become part of its
ingredient supply.

The terminal combines the contents of all inserted containers into one item
list. Use the search box to filter by item name, and scroll to browse results.
Barrel fluids are available to autocrafting, but the main terminal list displays
items.

| Control | Action |
| --- | --- |
| Left-click a stored item with an empty cursor | Take up to one stack onto the cursor. |
| Right-click a stored item with an empty cursor | Take one item. |
| Shift-click a stored item with an empty cursor | Move up to one stack into your inventory. |
| Left-click the terminal list while holding items | Deposit the held stack, as space permits. |
| Right-click the terminal list while holding items | Deposit one item. |
| Shift-click an ordinary item in your inventory | Deposit it into the inserted containers. |
| Shift-click a container or barrel in your inventory | Put the storage item into an available storage slot. |

The view button cycles through **S — Stored**, **C — Craftable**, and
**B — Both**. Stored shows items you have; Craftable shows outputs from installed
patterns; Both combines them. A craftable entry means the table knows a pattern,
but does not guarantee that ingredients or a suitable machine are available.

The 3×3 grid on the main screen works like a normal crafting table: put real
ingredients in the grid and take the result. With JEI/EMI installed, recipe
transfer can fill this grid from the inserted item containers. Manual crafting
does not require an encoded pattern.

## Encoding a crafting recipe

1. Click **Open Pattern Encoder** on the main screen.
2. Put blank Crafting Patterns into the blank-pattern slot.
3. Select **Crafting mode** with the mode button.
4. Lay out the recipe in the encoder's 3×3 grid. These are ghost slots: clicking
   with a held item records a sample without consuming it. Click a ghost slot
   with an empty cursor to clear it. You can also use JEI/EMI recipe transfer.
5. Check the result preview, then click the **→** encode button.

Encoding consumes one blank pattern and stores the encoded pattern in the first
empty pattern slot. The table holds **72 patterns**; scroll through the pattern
area to see them all. If those slots are full, the new pattern goes to your
inventory, or drops if your inventory is full. It must be installed in the
table's pattern slots before the table can use it.

Encode every intermediate recipe you want the table to make. For example, to
craft chests starting with logs, install patterns for logs → planks and planks →
chest. Patterns record specific ingredient items, so provide the same variant
used when encoding, or encode another pattern for the variant you have.

## Requesting an autocraft

1. Put the required raw materials in the inserted containers and barrels.
2. Switch the main terminal to **C — Craftable**.
3. Click the item you want with an empty cursor to open **Confirm Craft**.
4. Adjust **QTY** with **+** and **−**. Hold Shift to change it by 10 at a time.
5. Click **Confirm**.

Use Craftable view when you already have some of the target item: in Stored or
Both view, clicking a stocked entry withdraws it instead of opening a request.

The table plans the required steps, uses available stock, and makes missing
intermediates from installed patterns. QTY counts target items, rather than
recipe executions; recipes with multiple outputs can produce surplus. Existing
target stock can satisfy part of the request.

If planning fails, **Crafting Denied** lists missing items and fluids. Add the
missing materials or teach the table how to make them, then request again. If
the plan exceeds planner limits, try a smaller request or make intermediates in
separate batches.

The table handles one job at a time. Keep it powered and its storage installed
while the job runs. Closing the screen does not stop the job. Finished items,
surplus ingredients and collected byproducts return to the inserted storage;
withdraw the result from the terminal afterward.

### Example: chests from logs

1. Encode a crafting pattern for one type of log → four matching planks.
2. Encode a crafting pattern for eight of those planks → one chest.
3. Put at least four matching logs in an inserted container.
4. In Craftable view, select the chest, set QTY to 2 and confirm.
5. The table makes the planks, then the two chests, and puts the results in
   its inserted containers.

## Autocrafting with processors

Processing patterns let a crafting chain include supported NuclearCraft
processors. The table supplies ingredients, reserves a machine for the recipe,
and collects its outputs. The processor still performs the actual processing
and needs its own power and normal operating conditions.

### Connect the machines

Build a connected universal pipe network beside the table, with pipe connectors
beside the processors:

```text
Engineer's Crafting Table — universal pipe network — pipe connector — processor
```

At least one pipe-network block must directly touch the table. Each processor
must directly touch a connector on a network reachable from the table. Placing a
processor beside the table alone does not link it.

Enable **item** transport on the processor's connector for recipes with item
inputs or outputs. Enable **fluid** transport for recipes with fluid inputs or
outputs. Mixed recipes need both. Supply FE to the processors as well as the
table. Start with empty processor inputs so the table can place its order, and
avoid competing automation that inserts unrelated ingredients or removes the
job's outputs.

More than one suitable processor can share the work. Keep the table, network
and machines in loaded chunks while crafting.

### Encode a processing pattern

1. Open the Pattern Encoder and switch to **Processing mode**.
2. Use JEI/EMI recipe transfer to select the exact processor recipe. You can
   also place item samples in the ghost grid to search for a matching recipe.
3. Check the processor, ingredient amounts, fluid requirements and output
   previews before encoding. Manual item selection can match several recipes;
   JEI/EMI is preferable for choosing an exact recipe or a fluid-only recipe.
4. Insert a blank pattern and click **→** to encode it.
5. Load the required items into inserted containers and fluids into inserted
   barrels. Leave space for item and fluid results.
6. Request an item output from Craftable view as usual. Processing steps can
   also supply intermediates for other installed patterns.

Fluid-only intermediate steps can participate in a chain, although the main
terminal does not offer fluid outputs as clickable crafting requests. Fill or
empty barrels outside the table as needed; the table exposes item storage and
energy to external automation, but has no external fluid connection of its own.

Processing support covers the table's supported NuclearCraft processors;
**Pumps and Ore Leachers are excluded**. Arbitrary third-party machines and
multiblock controllers are not processing destinations for these patterns.

## Power and output space

The table stores **10,000 FE** and accepts up to **1,000 FE/t**. It consumes
**100 FE/t while idle** and **200 FE per active job tick**, including ticks spent
waiting during a job. Allow for that continuous demand, in addition to the
processors' own energy costs. A job pauses when the table lacks enough energy.

Leave room in item containers and suitable barrels for all outputs. Item
overflow drops into the world beside the table. Fluid overflow stays buffered
and prevents the job from finishing until storage can accept it.

## Troubleshooting

| Problem | What to check |
| --- | --- |
| Ingredients do not appear | Use NuclearCraft container items in the storage slots. Nearby chests and your player inventory are not the autocrafting supply. Fluids do not appear in the main item list. |
| Clicking takes items instead of starting a craft | Switch to **C — Craftable** and click with an empty cursor. |
| Encoding does nothing | Supply a blank pattern and select a valid recipe with a result preview. Check the encoder mode. |
| An output is listed, but crafting is denied | Check exact ingredient variants, missing intermediate patterns, fluid supplies and processor connections. A listed pattern alone does not prove the job can run. |
| A processing recipe is unavailable to the planner | Check that the correct supported processor is connected through a pipe connector, with the required item/fluid transport enabled. Re-encode patterns after recipe changes. |
| A job stops advancing | Check table and machine power, loaded chunks, empty machine inputs, machine operating conditions and output capacity. A busy or blocked machine may prevent dispatch. |
| A second request does not start | Let the current job finish, including returning buffered fluids to barrels. |
| The progress bar looks finished, but results are missing | Machine work or fluid return may still be pending; check the processors and barrel capacity. |
| Results appear on the ground | The inserted item containers ran out of usable space. |

