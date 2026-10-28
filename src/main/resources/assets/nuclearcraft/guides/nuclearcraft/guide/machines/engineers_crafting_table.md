---
navigation:
  title: Engineer's Crafting Table
  parent: machines.md
  icon: engineers_crafting_table
item_ids:
  - nuclearcraft:engineers_crafting_table
  - nuclearcraft:crafting_pattern
---

# Engineer's Crafting Table

<Column alignItems="center" fullWidth={true}>
  <BlockImage id="engineers_crafting_table" />
</Column>

A powered autocrafting terminal. Feed it storage containers and a live power feed; it assembles to spec, one operation per tick. Hook it to a pipe network and it will also run recipes on any processor it can reach. (Assembly to spec not guaranteed during scheduled brownouts.)

## Stock

The table has **9** storage slots that accept **Storage Containers** and **Barrels**. Container contents become the item stock and barrel contents become the fluid stock, each aggregated across every inserted block. Refill or swap them to keep the shelves stocked.

Fluids are never listed in the terminal grid; they are consumed and produced silently by processing patterns. A barrel that is filled while sitting in the table keeps its content when placed back down.

## Pattern Encoder

Open the **Pattern Encoder** to record a recipe onto a **Blank Pattern**. Up to **72** patterns can be loaded at once, and the solver chains them to craft intermediate parts automatically. The encoder has two modes, switched with the button under the blank-pattern slot:

- **Crafting mode** (crafting table icon) records a 3x3 grid recipe. Place ghost items in the grid, or press **+** on a recipe in JEI/EMI.
- **Processing mode** (furnace icon) records a machine recipe together with the processor that runs it. Press **+** on any processor recipe in JEI/EMI, or place the recipe's item inputs in the grid and the encoder picks the first matching recipe. The preview shows the processor, fluid inputs, and every secondary output.

Encoded patterns carry a small crafting-table or furnace badge so the two kinds are easy to tell apart in the pattern grid.

## Processing Patterns

A processing pattern only counts as craftable while a processor of the right type is reachable. The table finds processors through **Universal Pipes**:

1. Put a **Pipe Connector** against the table.
2. Put a connector against each processor you want the table to use, and enable **Items** and/or **Fluids** on it. A connector that only has **Energy** enabled is ignored; the table will not use that processor.
3. Encode a processing pattern for a recipe of that processor type.

The connector's **Direction** and the processor's own side configuration do not matter for autocrafting: the table inserts inputs straight into the processor's input slots and tanks, and takes the recipe's outputs straight out of its output slots and tanks. The processor still needs its own power. Several processors of the same type are used in parallel, and runs of identical operations are batched.

Keep in mind:

- A processor needs empty inputs and must finish its current recipe before accepting a job. Once assigned, it is reserved for that crafter until its batch finishes. Existing outputs and catalyst bonuses are returned to the crafter's storage.
- Every output of a processing pattern can supply downstream recipes, including secondary items and fluids.

- If a processor has **Push** configured on an output and ships the result elsewhere, the table never sees it and the job cancels after a timeout.
- Fluid outputs need a barrel with room. Until one is available the job stays open and holds the fluid.
- Removing a processor mid-job cancels the job; whatever it held is lost with the block.

## Power

Runs on FE:

- Buffer holds **10,000 FE**, accepts up to **1,000 FE/t**.
- Idle draw is **100 FE/t**.
- Each crafting operation costs **200 FE**. Processing operations cost the same per step, on top of what the processor itself uses.

Starve it and the line halts until power returns.

## Automation

The table exposes a peripheral to **ComputerCraft** and **OpenComputers 2**. Query stock, stored fluids and encoded patterns (including their processor and fluid inputs), then queue crafts programmatically. See the Computers section for the method list.
