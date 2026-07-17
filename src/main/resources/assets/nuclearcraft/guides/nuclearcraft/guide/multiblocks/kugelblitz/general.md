---
navigation:
  title: General Info
  parent: multiblocks/kugelblitz.md
  icon: chamber_terminal
item_ids:
  - nuclearcraft:chamber_terminal
  - nuclearcraft:neutronium_frame
  - nuclearcraft:event_horizon_stabilizer
  - nuclearcraft:quantum_flux_regulator
  - nuclearcraft:quantum_transformer
  - nuclearcraft:chamber_port
---

# General Info

<GameScene zoom={3}>
  <ImportStructure src="/structures/kugelblitz_chamber.nbt" />
</GameScene>

A **Kugelblitz** is a type of black hole formed from the energy of light, rather than from the collapse of a massive star. According to Einstein's theory of relativity, energy and mass are interchangeable, as described by `m = E/c²`. This means that a sufficiently concentrated beam of light can create a gravitational field strong enough to form a black hole.

The **Kugelblitz chamber** is a symmetric 11x11x11 spherical multiblock structure. It is used to create a black hole by firing an **EXPL** laser burst from all 6 sides at the same moment in time. (Containment manual, page 1: do not improvise.)

## Corners

<Column alignItems="center" fullWidth={true}>
  <BlockImage id="neutronium_frame" />
</Column>

Neutronium Frame is used mostly in the corners of the chamber. It is a frame block that holds the structure together.

## Walls

<Column alignItems="center" fullWidth={true}>
  <BlockImage id="event_horizon_stabilizer" />
</Column>

Stabilizers allow the chamber to keep the black hole as stable as possible. They need to be placed in the walls of the chamber.

## Energy and Transformation

<Row>
  <BlockImage id="quantum_flux_regulator" />
  <BlockImage id="quantum_transformer" />
</Row>

The chamber harnesses black hole evaporation energy and quantum fields to generate power and transform one item into another. More Flux Regulators give better energy output. More Quantum Transformers give better transformation speed.

## Chamber Port

<Column alignItems="center" fullWidth={true}>
  <BlockImage id="chamber_port" />
</Column>

The Chamber Port is used to transfer items, fluids, and energy into and out of the reactor. The port can also be used for redstone control and computers.

## Evaporation and Feeding

A black hole loses its mass during evaporation. To feed the black hole you need to supply Subliquid Matter. If the black hole's mass gets too low, it will evaporate. If it gets too high, it will collapse.

## Quantum transformation

In JEI you can see which items can be produced by the quantum transformation process. Those products are also allowed as inputs. To start the transformation, you need to find the correct quantum frequency. The player has to discover which items can be produced by the process. Transformation pairs are bound to the world seed.

## Simple Reactor

This is a simple chamber design. It is a good starting point. Best fuel for this chamber is HEU-235.

## Design Considerations

When designing a reactor, the usage of a [reactor planner](https://github.com/ThizThizzyDizzy/nc-reactor-generator/releases) is recommended. Reactor planners assist with the design of the reactor, providing feedback on design rules, heat control, and predicted output.
