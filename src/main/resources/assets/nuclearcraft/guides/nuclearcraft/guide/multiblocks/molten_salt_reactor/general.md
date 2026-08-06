---
navigation:
  title: General Info
  parent: multiblocks/molten_salt_reactor.md
  icon: msr_controller
item_ids:
  - nuclearcraft:msr_controller
  - nuclearcraft:msr_fuel_cell
  - nuclearcraft:msr_port
---

# General Info

<GameScene zoom={3}>
  <ImportStructure src="/structures/molten_salt_reactor.nbt" />
</GameScene>

The **Molten Salt Reactor** runs on **liquid fuel**. Instead of arranging heat sinks and moderators, you pump **carrier salt** through a chamber packed with **fuel pebbles** and manage two coupled stats - **temperature** and **reactivity**. It does **not** make power directly: it turns cold salt into **hot salt**, which you pipe to a Heat Exchanger and on to a Turbine. Pumping the hot salt out is also the core's **only cooling**.

## MSR Controller

<Column alignItems="center" fullWidth={true}>
  <BlockImage id="msr_controller" />
</Column>

Place exactly one **Controller** in the shell to form the reactor. It runs the simulation and owns the salt tanks and pebble slots. Its GUI reports temperature, reactivity, depletion and the salt input/output rates, and it only runs with a **redstone signal** (or computer enable).

## Shell

<Column alignItems="center" fullWidth={true}>
  <BlockImage id="fission_reactor_casing" />
</Column>

The MSR reuses the fission shell. Edges and corners are **Reactor Casing**; walls can be Casing, **Reactor Glass**, or both. The shell is a cube, from **5x5x5** up to **26x26x26**.

## Fuel Cell

<Column alignItems="center" fullWidth={true}>
  <BlockImage id="msr_fuel_cell" />
</Column>

Fill the **entire interior** with **MSR Fuel Cells** - no other interior block is allowed. The fuel-cell count sets the reactor's salt volume and heat budget, so a bigger core holds more salt and runs harder.

## MSR Port

<Column alignItems="center" fullWidth={true}>
  <BlockImage id="msr_port" />
</Column>

**Ports** move everything in and out: cold salt in, hot salt out, pebbles in, depleted pebbles out. One port type carries both items and fluid. A port also emits a comparator signal you can switch between **Temperature** and **Depletion**.

## Fuel & Salt

Fuel is loaded as **TRISO pebbles** - the `_tr` fuel variants (Thorium, LEU-235, HEU-235, Plutonium, and so on). A spent pebble leaves a **depleted pebble** in the output slot. The carrier salt is **FLiBe Molten Salt**, pumped in cold and out hot.

## Running It

1. Build the shell, fill the interior with Fuel Cells, add one Controller and at least one Port.
2. Pipe **cold FLiBe** in and load **pebbles**.
3. Apply redstone. With enough fuel and salt the core goes **critical** and heats up.
4. Reactivity has a **negative temperature coefficient**, so it falls as the core warms and self-stabilizes. Raise the **salt output rate** to keep temperature off the 2000 K ceiling.
5. Send the **hot salt** to a Heat Exchanger -> Turbine for power, and pull **depleted pebbles** from the output.

The reactor can also be read and driven from **ComputerCraft / OpenComputers** (type `nc_msr_reactor`).
