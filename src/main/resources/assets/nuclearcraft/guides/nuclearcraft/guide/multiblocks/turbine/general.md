---
navigation:
  title: General Info
  parent: multiblocks/turbine.md
  icon: turbine_controller
item_ids:
  - nuclearcraft:turbine_controller
  - nuclearcraft:turbine_casing
  - nuclearcraft:turbine_glass
  - nuclearcraft:turbine_port
  - nuclearcraft:turbine_copper_coil
  - nuclearcraft:turbine_magnesium_coil
  - nuclearcraft:turbine_silver_coil
  - nuclearcraft:turbine_gold_coil
  - nuclearcraft:turbine_beryllium_coil
  - nuclearcraft:turbine_aluminum_coil
---

# General Info

<GameScene zoom={3}>
  <ImportStructure src="/structures/turbine.nbt" />
</GameScene>

## Turbine Casing

<Row>
  <BlockImage id="turbine_glass" />
  <BlockImage id="turbine_casing" />
</Row>

The interior components of the turbine are contained within a rectangular prism. The turbine must have edges consisting of **Turbine Casing**, while the walls of the turbine can be **Turbine Glass**, Turbine Casing, or both.

## Turbine Controller

<Column alignItems="center" fullWidth={true}>
  <BlockImage id="turbine_controller" />
</Column>

Without a controller, the turbine multiblock will not form. Its GUI will show information about the turbine, such as the averages of relevant components' stats.

## Turbine Port

<Column alignItems="center" fullWidth={true}>
  <BlockImage id="turbine_port" />
</Column>

The Turbine Port is used to transfer fluids and energy into and out of the turbine. It can be configured to input or output fluids and energy. The port can also be used for redstone control and computers.

## Coils

<Row>
  <BlockImage id="turbine_copper_coil" />
  <BlockImage id="turbine_magnesium_coil" />
  <BlockImage id="turbine_silver_coil" />
  <BlockImage id="turbine_gold_coil" />
  <BlockImage id="turbine_beryllium_coil" />
  <BlockImage id="turbine_aluminum_coil" />
</Row>

**Coils** are used to convert the rotor's kinetic energy into electricity. All coils have different efficiencies and placement rules.

## Simple Turbine

This is a simple turbine design. A good starting point - pair it with steam from your reactor and watch the kilowatts roll in.
