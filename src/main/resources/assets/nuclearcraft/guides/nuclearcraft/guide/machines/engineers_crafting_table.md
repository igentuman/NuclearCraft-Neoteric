---
navigation:
  title: Engineer's Crafting Table
  parent: machines.md
  icon: engineers_crafting_table
item_ids:
  - nuclearcraft:engineers_crafting_table
---

# Engineer's Crafting Table

<Column alignItems="center" fullWidth={true}>
  <BlockImage id="engineers_crafting_table" />
</Column>

A powered autocrafting terminal. Feed it storage containers and a live power feed; it assembles to spec, one operation per tick. (Assembly to spec not guaranteed during scheduled brownouts.)

## Stock

The table has **6** container slots that accept **Storage Containers**. Their combined contents become the crafting stock, aggregated across every inserted container. Refill or swap containers to keep the shelves stocked.

## Pattern Encoder

Open the **Pattern Encoder** to record a recipe onto a **Blank Pattern**. Each encoded pattern maps its inputs to an output. Up to **36** patterns can be loaded at once, and the solver chains them to craft intermediate parts automatically.

## Power

Runs on FE:

- Buffer holds **10,000 FE**, accepts up to **1,000 FE/t**.
- Idle draw is **100 FE/t**.
- Each crafting operation costs **200 FE**.

Starve it and the line halts until power returns.

## Automation

The table exposes a peripheral to **ComputerCraft** and **OpenComputers 2**. Query stock and encoded patterns, then queue crafts programmatically. See the Computers section for the method list.
