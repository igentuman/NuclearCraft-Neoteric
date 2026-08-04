---
navigation:
  title: Reactor Designer
  parent: multiblocks/fission.md
  icon: fission_reactor_designer
item_ids:
  - nuclearcraft:fission_reactor_designer
  - nuclearcraft:fission_reactor_plan
---

# Reactor Designer

<Column alignItems="center" fullWidth={true}>
  <BlockImage id="fission_reactor_designer" />
</Column>

The **Fission Reactor Designer** is a planning terminal for reactor interiors. Sketch a 3D cell grid, pick a fuel, and watch the heat and output numbers update live - all on paper, before a single block of casing is placed. (Vault-Tec advises: simulate first, irradiate later.)

Place the Designer and right-click to open its full-screen editor. Nothing here touches the world - it is a pure sandbox. The design you build is saved onto a [Fission Reactor Plan](reactor_designer.md#save-to-a-plan) for a [Reactor Builder](multiblock_builder.md) to assemble for real.

## Laying Out the Interior

Press **New** to set the interior size (X/Y/Z). You are editing only the **inside** of the reactor - the shell and controller are added automatically at build time. The interior is shown as one flat, top-down grid per Y layer. **Left-click / drag** a cell to place the selected component; **right-click or Shift-click** to erase.

## Component Palette

The scrollable palette on the right holds the **Fuel Cell**, **Irradiation Chamber**, **Pile-Driver Chamber**, every [Heatsink](general.md#heatsinks), and every [Moderator](general.md#moderators). Hover an entry for its name; heatsinks also show their cooling value and placement rule.

## Live Simulation

Every edit re-runs the reactor math and prints **Heat/t**, **Cooling/t**, **Net Heat**, **FE/t**, **Steam/t**, **Irradiation**, and **Meltdown Time**. Aim for a net heat of 0 for a fully stable reactor - meltdown time reads infinite once net heat is zero or negative. Pick your fuel from the searchable dropdown; it drives the heat and energy figures.

## Placement Validation

The simulator checks every component against its own placement rule - the same rules real reactors enforce. Heatsinks that break their rule, and irradiation chambers missing a moderator-and-fuel-cell line, are drawn with a **red outline**. Fix the red before you build. (Better a red border than a smoking crater.)

## Save to a Plan

<Column alignItems="center" fullWidth={true}>
  <ItemImage id="fission_reactor_plan" />
</Column>

**Save** writes the layout, fuel, and headline stats onto a **Fission Reactor Plan** and hands it to you. **Load** reads the Plan in your selected hotbar slot back into the Designer for further editing. The Plan's tooltip shows its size, fuel, and net heat so you can tell blueprints apart.
