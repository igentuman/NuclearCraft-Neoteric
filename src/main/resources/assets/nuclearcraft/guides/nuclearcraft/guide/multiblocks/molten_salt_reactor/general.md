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

The **Molten Salt Reactor** burns **TRISO fuel pebbles**. **FLiBe molten salt** carries heat away from the fuel cells; you manage both **temperature** and **reactivity**. The reactor does **not** make power directly: it heats cold salt, which you pipe to a Heat Exchanger and then a Turbine. Keep cold salt flowing in and hot salt flowing out to cool the core.

## MSR Controller

<Column alignItems="center" fullWidth={true}>
  <BlockImage id="msr_controller" />
</Column>

Place exactly one **Controller** in the shell to form the reactor. It runs the simulation and owns the salt tanks and pebble slots. Its GUI shows temperature, reactivity, depletion, the loaded TRISO count and capacity, and separate salt input/output limits in buckets per tick. A **redstone signal** (or computer enable) starts the reaction; cooling can continue after it is switched off.

## Shell

<Column alignItems="center" fullWidth={true}>
  <BlockImage id="fission_reactor_casing" />
</Column>

The MSR reuses the fission shell. Edges and corners are **Reactor Casing**; walls can be Casing, **Reactor Glass**, or both. By default, each outer dimension must be **7 to 11 blocks**; the server configuration can change these limits.

## Fuel Cell

<Column alignItems="center" fullWidth={true}>
  <BlockImage id="msr_fuel_cell" />
</Column>

Fill the **entire interior** with **MSR Fuel Cells** - no other interior block is allowed. Each cell adds salt capacity and room for **10 TRISO pebbles** by default. The Controller shows the total pebble capacity; both capacities can be changed in the server configuration.

## MSR Port

<Column alignItems="center" fullWidth={true}>
  <BlockImage id="msr_port" />
</Column>

**Ports** move everything in and out: cold salt in, hot salt out, pebbles in, depleted pebbles out. One port type carries both items and fluid. A port also emits a comparator signal you can switch between **Temperature** and **Depletion**.

## Fuel & Salt

Fuel is loaded as **TRISO pebbles** - the `_tr` fuel variants (Thorium, LEU-235, HEU-235, Plutonium, and so on). A spent pebble leaves a **depleted pebble** in the output slot. The carrier salt is **FLiBe Molten Salt**, pumped in cold and out hot.

## Running It

1. Build the shell, fill the interior with Fuel Cells, add one Controller and at least one Port.
2. Pipe **cold FLiBe** in, provide a route for **hot FLiBe** out, and load **TRISO pebbles**.
3. Apply redstone. Fuel and salt can start producing heat without a fixed minimum load. Reactivity approaches its new level gradually after startup or changes to fuel or salt; the **critical** status appears only after reactivity builds up.
4. Watch the temperature and adjust both salt limits. More salt increases reactivity smoothly until the combined cold and hot salt reaches roughly **30%** of capacity. The input limit admits cold salt; the output limit lets hot salt leave. If the hot tank fills or cold salt runs out, cooling slows and heat accumulates.
5. Heating begins to reduce reactivity above a displayed temperature of **1200**, but it does not guarantee safety. Keep the reading below **2000**: sustained overheating melts down the core. Switching off redstone stops the reaction immediately while available cold salt can still carry away stored heat.
6. Send the **hot salt** to a Heat Exchanger -> Turbine for power, and pull **depleted pebbles** from the output.

The reactor can also be read and driven from **ComputerCraft / OpenComputers** (type `nc_msr_reactor`).
