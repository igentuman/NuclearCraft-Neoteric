---
navigation:
  title: General Info
  parent: multiblocks/fusion.md
  icon: fusion_core
item_ids:
  - nuclearcraft:fusion_reactor_core
  - nuclearcraft:fusion_reactor_casing
  - nuclearcraft:fusion_reactor_glass
  - nuclearcraft:fusion_reactor_connector
  - nuclearcraft:basic_electromagnet
  - nuclearcraft:bscco_electromagnet
  - nuclearcraft:magnesium_diboride_electromagnet
  - nuclearcraft:niobium_tin_electromagnet
  - nuclearcraft:niobium_titanium_electromagnet
  - nuclearcraft:basic_electromagnet_slope
  - nuclearcraft:bscco_electromagnet_slope
  - nuclearcraft:magnesium_diboride_electromagnet_slope
  - nuclearcraft:niobium_tin_electromagnet_slope
  - nuclearcraft:niobium_titanium_electromagnet_slope
  - nuclearcraft:basic_rf_amplifier
  - nuclearcraft:bscco_rf_amplifier
  - nuclearcraft:magnesium_diboride_rf_amplifier
  - nuclearcraft:niobium_tin_rf_amplifier
  - nuclearcraft:niobium_titanium_rf_amplifier
---

# General Info

<GameScene zoom={3}>
  <ImportStructure src="/structures/fusion_reactor.nbt" />
</GameScene>

**Fusion reactors** generate decent amounts of energy by fusing particles together. They can also boil coolants, which must be supplied in order to cool down the reactor's function blocks.

Plasma in bigger reactors can reach higher temperatures. A larger reaction chamber can also hold more fuel and produce more energy.

## Reactor Casing

<Row>
  <BlockImage id="fusion_reactor_casing" />
  <BlockImage id="fusion_reactor_glass" />
</Row>

Reactor casing blocks are used to build the toroidal structure (Reaction Chamber) around the **Fusion Core**.

## Toroidal Section

You can use any casing block, or combine them, to build the toroidal section. The center of the section must be empty.

## Reactor Core

<Column alignItems="center" fullWidth={true}>
  <BlockImage id="fusion_reactor_core" />
</Column>

Without a controller, the reactor multiblock will not form. Its GUI shows information about the reactor, such as the averages of relevant components' stats.

## Fusion Connectors

<Column alignItems="center" fullWidth={true}>
  <BlockImage id="fusion_reactor_connector" />
</Column>

**Fusion Connectors** are used to connect the fusion core to the toroidal reaction chamber. They transfer fuel, coolant, and energy.

## Functional Blocks

The **Fusion Reactor Chamber** needs 2 kinds of functional blocks: Electromagnets and RF Amplifiers. These blocks require energy to operate and coolant to keep cool. They must be placed at the corners of the toroidal reaction chamber section.

## Electromagnets

<Row>
  <BlockImage id="basic_electromagnet" />
  <BlockImage id="bscco_electromagnet" />
  <BlockImage id="magnesium_diboride_electromagnet" />
  <BlockImage id="niobium_tin_electromagnet" />
  <BlockImage id="niobium_titanium_electromagnet" />
  <BlockImage id="basic_electromagnet_slope" />
  <BlockImage id="bscco_electromagnet_slope" />
  <BlockImage id="magnesium_diboride_electromagnet_slope" />
  <BlockImage id="niobium_tin_electromagnet_slope" />
  <BlockImage id="niobium_titanium_electromagnet_slope" />
</Row>

**Electromagnets** are used to sustain plasma in the reaction chamber. A bigger electromagnetic field means better plasma stability and cross-section. It also means less plasma heat loss.

## RF Amplifiers

<Row>
  <BlockImage id="basic_rf_amplifier" />
  <BlockImage id="bscco_rf_amplifier" />
  <BlockImage id="magnesium_diboride_rf_amplifier" />
  <BlockImage id="niobium_tin_rf_amplifier" />
  <BlockImage id="niobium_titanium_rf_amplifier" />
</Row>

**RF Amplifiers** are used to increase plasma energy with radio-frequency waves (like a microwave). In other words, they heat up the plasma. The leftovers do not reheat well.

## Simple Reactor

This is a simple reactor design. Good for starting with low-temperature reactions.
