---
navigation:
  title: Linear Accelerator
  parent: multiblocks/accelerators.md
  icon: linear_accelerator_controller
item_ids:
  - nuclearcraft:linear_accelerator_controller
---

# Linear Accelerator

<GameScene zoom={3}>
  <ImportStructure src="/structures/linear_accelerator.nbt" />
</GameScene>

Linear Accelerators are used to either create particle beams and increase their energy and focus, or to increase the energy and focus of an existing particle beam.

The change in energy and focus is determined by the [structures](general.md#construction) inside the accelerator.

Example Linear Accelerator.

## Construction

Build a horizontal tube **5 blocks wide and 5 blocks tall**, at least 6 blocks long by default. Use **Accelerator Casing** on all edges. Fill the centerline with **Particle Beam** blocks and place the **Linear Accelerator Controller** in a side wall. Use an **Accelerator Port** for power and coolant.

Place an outward-facing endpoint at the center of each end wall. To produce a new beam, use one **Accelerator Ion Source Port** and one **Accelerator Beam Port**. The Beam Port becomes the output. To boost an existing beam, use two Accelerator Beam Ports and check which is set to input before connecting the upstream accelerator.

## Ion source

Feed a compatible source item or fluid into the **Accelerator Ion Source Port**. For example, Hydrogen produces protons and a Tungsten Filament produces electrons. The port feeds the controller's source slot and source-fluid tank. The coolant tanks are separate.

## Operation

Linear accelerators function and have the same requirements as all [accelerators](general.md#operation), needing energy and coolant to operate. (The Manhattan Project did this with calutrons and a great deal of patience; you have neither.)

## Particle Energy

The maximum outputted Particle Energy is calculated using `Eₓ = E₀ + |q|·V` where `E₀` is the starting energy (normally 0 for an ion source), `q` is the particle's charge, and `V` is the accelerator's voltage - the sum of all the RF amplifier rings' voltages (the structure, not the block), which can be seen in the accelerator's GUI.

## Redstone Control

The output particle energy can be controlled with the strength of the redstone signal according to `E = Eₓ·Sᵣ/15` where `Sᵣ` is the redstone strength. If a redstone strength of 15 (max) is applied to the controller (or Accelerator Port) it will output the maximum energy. Anything less than 15 will output the corresponding fraction of the maximum energy - for example, a redstone strength of 2 is `2/15 = 13.3%` of the max energy.

## Computer Control

See [Accelerator computer integration](../../computers/accelerator.md) for computer control of output energy.
