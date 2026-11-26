---
navigation:
  title: In Situ Leaching
  parent: machines.md
  icon: leacher
item_ids:
  - nuclearcraft:leacher
  - nuclearcraft:analyzer
---

# In Situ Leaching

<GameScene zoom={3}>
  <ImportStructure src="/structures/leacher.nbt" />
</GameScene>

<Column alignItems="center" fullWidth={true}>
  <BlockImage id="leacher" />
</Column>

This is the perfect way of getting resources from ores without having to mine them. In this method, you pump acid into the ground; the acid dissolves minerals, and the leacher pumps the solution back up. Surface integrity and groundwater quality are addressed in a later pamphlet.

## Modes

<Column alignItems="center" fullWidth={true}>
  <BlockImage id="analyzer" />
</Column>

The **Leacher** accepts two sources:

- A **filled Minecraft map** lets the Leacher extract supported ore blocks below it in its own chunk.
- A **Research Paper** from an **Analyzer** identifies a virtual ore vein. Put Paper in the Analyzer to survey its chunk, then place the Leacher in the chunk recorded on the Research Paper.

## Placement

<Row>
  <BlockImage id="leacher" />
  <BlockImage id="pump" />
</Row>

Place the Leacher block and it will highlight the required placements for pumps. After all blocks are placed, insert the data source and supply some acid and energy. It will start leaching.
