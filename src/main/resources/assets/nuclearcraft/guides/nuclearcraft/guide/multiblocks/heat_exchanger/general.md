---
navigation:
  title: General Info
  parent: multiblocks/heat_exchanger.md
  icon: heat_exchanger_controller
item_ids:
  - nuclearcraft:heat_exchanger_controller
  - nuclearcraft:heat_exchanger_casing
  - nuclearcraft:heat_exchanger_hot_coolant_port
  - nuclearcraft:heat_exchanger_cold_coolant_port
  - nuclearcraft:heat_exchanger_radiator
  - nuclearcraft:heat_exchanger
---

# General Info

<GameScene zoom={3}>
  <ImportStructure src="/structures/heat_exchanger.nbt" />
</GameScene>

The **Heat Exchanger** runs two coolant loops around a shared **heat buffer**. The **hot loop** cools a hot coolant and banks the heat; the **cold loop** spends that banked heat to condense spent steam back into water. Run both at once and the waste heat from one job pays for the other.

## Heat Exchanger Casing

<Column alignItems="center" fullWidth={true}>
  <BlockImage id="heat_exchanger_casing" />
</Column>

The structure is a cuboid shell, **3x3x3** up to **11x11x11**. Non-cube shapes are allowed (for example 5x6x10). All edges and corners are **Heat Exchanger Casing** - there is no glass variant.

## Heat Exchanger Controller

<Column alignItems="center" fullWidth={true}>
  <BlockImage id="heat_exchanger_controller" />
</Column>

Place exactly one **Controller** anywhere in the shell to form the multiblock. It owns the heat buffer and energy storage, and its GUI reports stored heat, the four tanks, energy, and interior block count. The controller only runs while it receives a **redstone signal**.

## Hot Coolant Port

<Column alignItems="center" fullWidth={true}>
  <BlockImage id="heat_exchanger_hot_coolant_port" />
</Column>

**Hot Coolant Ports** handle the hot loop: pipe a hot coolant in (such as Hot Helium or Hot FLiBe Molten Salt) and collect it cooled. Each operation **adds heat to the buffer**. When the buffer is full, the hot loop stalls.

## Cold Coolant Port

<Column alignItems="center" fullWidth={true}>
  <BlockImage id="heat_exchanger_cold_coolant_port" />
</Column>

**Cold Coolant Ports** handle the cold loop: pipe spent steam in (such as Exhaust Steam or Low-Quality Steam) and collect condensed water. Each operation **draws heat from the buffer**. When the buffer is empty, the cold loop stalls. Hot and cold fluids keep to their own ports.

## Radiator

<Column alignItems="center" fullWidth={true}>
  <BlockImage id="heat_exchanger_radiator" />
</Column>

**Radiators** sit in the shell and passively vent heat from the buffer - every tick the structure is formed, with no redstone or energy needed. Use them to shed heat the cold loop can't keep up with, so the hot loop doesn't fill the buffer and jam.

## Interior

<Column alignItems="center" fullWidth={true}>
  <BlockImage id="heat_exchanger" />
</Column>

Fill the interior with **Heat Exchanger** blocks. Each one raises throughput and grows the heat buffer - but also raises the standby energy drawn per tick. An empty interior forms but does nothing.

## Scaling

Let **N** be the number of interior Heat Exchanger blocks.

- **Throughput** scales linearly with **N**: both loops process more per tick.
- **Heat buffer** scales linearly with **N**: a bigger buffer to bank heat in.
- **Standby energy** scales linearly with **N**: drawn while redstone-powered; at 0 FE both loops halt (radiators keep cooling).
- With **N = 0** the multiblock forms but does nothing.
