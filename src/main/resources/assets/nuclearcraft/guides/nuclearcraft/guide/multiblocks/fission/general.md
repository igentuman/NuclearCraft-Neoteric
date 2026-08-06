---
navigation:
  title: General Info
  parent: multiblocks/fission.md
  icon: fission_reactor_controller
item_ids:
  - nuclearcraft:fission_reactor_controller
  - nuclearcraft:fission_reactor_casing
  - nuclearcraft:fission_reactor_glass
  - nuclearcraft:fission_reactor_port
  - nuclearcraft:fission_reactor_solid_fuel_cell
  - nuclearcraft:fission_reactor_irradiation_chamber
  - nuclearcraft:fission_reactor_pile-driver_irradiation_chamber
  - nuclearcraft:irradiator
  - nuclearcraft:graphite_block
  - nuclearcraft:beryllium_block
  - nuclearcraft:lapis_heat_sink
  - nuclearcraft:aluminum_heat_sink
  - nuclearcraft:arsenic_heat_sink
  - nuclearcraft:boron_heat_sink
  - nuclearcraft:carobbiite_heat_sink
  - nuclearcraft:copper_heat_sink
  - nuclearcraft:cryotheum_heat_sink
  - nuclearcraft:diamond_heat_sink
  - nuclearcraft:emerald_heat_sink
  - nuclearcraft:end_stone_heat_sink
  - nuclearcraft:enderium_heat_sink
  - nuclearcraft:fluorite_heat_sink
  - nuclearcraft:glowstone_heat_sink
  - nuclearcraft:gold_heat_sink
  - nuclearcraft:iron_heat_sink
  - nuclearcraft:lead_heat_sink
  - nuclearcraft:liquid_helium_heat_sink
  - nuclearcraft:liquid_nitrogen_heat_sink
  - nuclearcraft:lithium_heat_sink
  - nuclearcraft:magnesium_heat_sink
  - nuclearcraft:manganese_heat_sink
  - nuclearcraft:nether_brick_heat_sink
  - nuclearcraft:netherite_heat_sink
  - nuclearcraft:obsidian_heat_sink
  - nuclearcraft:prismarine_heat_sink
  - nuclearcraft:purpur_heat_sink
  - nuclearcraft:quartz_heat_sink
  - nuclearcraft:redstone_heat_sink
  - nuclearcraft:silver_heat_sink
  - nuclearcraft:slime_heat_sink
  - nuclearcraft:tin_heat_sink
  - nuclearcraft:villiaumite_heat_sink
  - nuclearcraft:water_heat_sink
---

# General Info

<GameScene zoom={3}>
  <ImportStructure src="/structures/fission_reactor.nbt" />
</GameScene>

**Fission reactors** generate heat from the self-sustained nuclear reaction of fission fuel. This heat is ultimately transformed into electricity. The method of electricity generation can be switched between boiling and electric mode. (Chicago Pile-1 made do with a squash court; you have an entire chunk.)

The **Fission Reactor** can operate in 2 modes: energy and boiling. Energy mode means the reactor will produce energy directly. In boiling mode, it will use the produced heat to boil coolant. Use the **Reactor Port** to input/output coolant.

## Reactor Casing

<Row>
  <BlockImage id="fission_reactor_casing" />
  <BlockImage id="fission_reactor_glass" />
</Row>

The interior components of the reactor are contained within a rectangular prism. The reactor must have edges consisting of **Reactor Casing**, while the walls of the reactor can be **Reactor Glass**, Reactor Casing, or both.

## Reactor Controllers

<Column alignItems="center" fullWidth={true}>
  <BlockImage id="fission_reactor_controller" />
</Column>

Without a controller, the reactor multiblock will not form. Its GUI will show information about the reactor, such as the averages of relevant components' stats.

## Reactor Port

<Column alignItems="center" fullWidth={true}>
  <BlockImage id="fission_reactor_port" />
</Column>

The Reactor Port is used to transfer items, fluids, and energy into and out of the reactor. The port can be configured to input or output items, fluids, and energy. It can also be used for redstone control and computers.

## Fuel Cell

<Column alignItems="center" fullWidth={true}>
  <BlockImage id="fission_reactor_solid_fuel_cell" />
</Column>

Fuel Cells are used to convert fuel heat into boiling or direct energy production. Each additional fuel cell multiplies fuel depletion speed.

## Moderators

<Row>
  <BlockImage id="graphite_block" />
  <BlockImage id="beryllium_block" />
</Row>

**Moderators** slow down the high energy neutrons produced by the [Fuel Cells](general.md#fuel_cell) into ones that will cause more fission in other fuel components. Moderators must be placed next to Fuel Cells. You can adjust the moderation level with a redstone signal input to the **Reactor Port**.

## Moderators Placement

You can gain an additional efficiency bonus by placing multiple fuel cells next to one moderator block, providing both efficiency and heat bonuses.

## Heatsinks

<Row>
  <BlockImage id="lapis_heat_sink" />
  <BlockImage id="aluminum_heat_sink" />
  <BlockImage id="arsenic_heat_sink" />
  <BlockImage id="boron_heat_sink" />
  <BlockImage id="carobbiite_heat_sink" />
  <BlockImage id="copper_heat_sink" />
  <BlockImage id="cryotheum_heat_sink" />
  <BlockImage id="diamond_heat_sink" />
  <BlockImage id="emerald_heat_sink" />
  <BlockImage id="end_stone_heat_sink" />
  <BlockImage id="enderium_heat_sink" />
  <BlockImage id="fluorite_heat_sink" />
  <BlockImage id="glowstone_heat_sink" />
  <BlockImage id="gold_heat_sink" />
  <BlockImage id="iron_heat_sink" />
  <BlockImage id="lead_heat_sink" />
  <BlockImage id="liquid_helium_heat_sink" />
  <BlockImage id="liquid_nitrogen_heat_sink" />
  <BlockImage id="lithium_heat_sink" />
  <BlockImage id="magnesium_heat_sink" />
  <BlockImage id="manganese_heat_sink" />
  <BlockImage id="nether_brick_heat_sink" />
  <BlockImage id="netherite_heat_sink" />
  <BlockImage id="obsidian_heat_sink" />
  <BlockImage id="prismarine_heat_sink" />
  <BlockImage id="purpur_heat_sink" />
  <BlockImage id="quartz_heat_sink" />
  <BlockImage id="redstone_heat_sink" />
  <BlockImage id="silver_heat_sink" />
  <BlockImage id="slime_heat_sink" />
  <BlockImage id="tin_heat_sink" />
  <BlockImage id="villiaumite_heat_sink" />
  <BlockImage id="water_heat_sink" />
</Row>

**Heatsinks** are used when designing a reactor, to balance the **Net Heat** of the reactor. The designer should aim for a net heat of 0 HU/t for a fully stable reactor. Each heatsink has specific design rules that it must adhere to.

Heatsinks remove heat from the reactor. When a fuel cell is active, it will produce heat equal to its base heat output times the **Heat Multiplier**. The heat multiplier of a cell is determined by the number of moderator lines. Thus, a cell with a single moderator line will have 100% heat efficiency, and a cell with two moderator lines will have 200% heat efficiency.

## Heat Sink Validation

If a reactor part's placement rule requires a non-fuel-cell block - for example, if it needs another heat sink - that heat sink must be placed according to its own placement rule. The end of the connection chain must always link back to a fuel cell.

## Irradiators

<Row>
  <BlockImage id="fission_reactor_irradiation_chamber" />
  <BlockImage id="irradiator" />
</Row>

When placed at the end of a [Moderator](general.md#moderator) line, **Irradiators** will use the radiative flux to transform items in the Irradiation Chamber. Irradiation speed depends on the number of irradiation lines in the reactor.

## Irradiation Line

You can make up to 6 irradiation lines per irradiation chamber. Each line will increase the speed of irradiation.

## Pile-Driver Irradiation Chamber

<Column alignItems="center" fullWidth={true}>
  <BlockImage id="fission_reactor_pile-driver_irradiation_chamber" />
</Column>

An upgraded **Irradiation Chamber** that runs at **5x** the speed of a standard chamber. Drops in as a direct replacement at the end of any irradiation line - same placement rules, same line count, just five times the throughput. (Production quotas wait for no one.)

## Simple Reactor

This is a simple reactor design. It has 1 fuel cell and 1 irradiation line. It's a good starting point for a reactor. The best fuel for this reactor is HEU-235.

## Design Considerations

When designing a reactor, the usage of a [reactor planner](https://github.com/ThizThizzyDizzy/nc-reactor-generator/releases) is recommended. Reactor planners assist with the design of the reactor, providing feedback on design rules, heat control, and predicted output.
