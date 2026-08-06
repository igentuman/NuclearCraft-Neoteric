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

The Leacher operates in 2 modes. The first mode requires an analyzed map; in this mode it pumps real resources from the ground. The second mode requires a research paper containing mineral vein information. Use the Analyzer to analyze chunks; if a chunk contains a mineral vein, that information is written onto the research paper. In this mode, the Leacher pulls resources from deep underground and must be placed in the exact chunk containing the vein.

## Placement

<Row>
  <BlockImage id="leacher" />
  <BlockImage id="pump" />
</Row>

Place the Leacher block and it will highlight the required placements for pumps. After all blocks are placed, insert the data source and supply some acid and energy. It will start leaching.
