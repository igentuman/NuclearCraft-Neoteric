---
navigation:
  title: Reactor Builder
  parent: multiblocks/fission.md
  icon: multiblock_builder
item_ids:
  - nuclearcraft:multiblock_builder
  - nuclearcraft:fission_reactor_plan
---

# Reactor Builder

<Column alignItems="center" fullWidth={true}>
  <BlockImage id="multiblock_builder" />
</Column>

The **Fission Reactor Builder** is an automated construction machine. Feed it a [Fission Reactor Plan](reactor_designer.md#save-to-a-plan) and it assembles the whole reactor - interior, casing shell, and controller - pulling the blocks it needs out of nearby storage. (Labor-saving, morale-preserving, quota-meeting.)

Place the Builder facing an open area - its facing decides which way the reactor grows out from it. A translucent bounding box previews where the structure will appear. Right-click to open the GUI: a 3D preview, a size readout, and the Load and Build buttons.

## Load a Plan

Hold a **Fission Reactor Plan** in your selected hotbar slot and press **Load Plan**. The saved layout streams into the Builder; the preview and size text update to show exactly what will be built. Plans made in the [Reactor Designer](reactor_designer.md) carry the full interior - the shell is added for you.

## Stock the Materials

The Builder pulls blocks from **connected inventories** - it flood-fills through adjacent container block entities (chests, drawers, and the like) touching the Builder or each other. Stock those chests with the casing, glass, controller, heatsinks, moderators, and fuel cells the plan needs.

## Build

Press **Build**. The Builder wraps your interior in a Reactor Casing and Glass shell, drops in a Controller, tallies every required block, consumes them from the containers, and places the multiblock. If the area is blocked it names the obstructed coordinate; if materials are short it lists exactly what is **missing** and how many. Fix, restock, and try again.
