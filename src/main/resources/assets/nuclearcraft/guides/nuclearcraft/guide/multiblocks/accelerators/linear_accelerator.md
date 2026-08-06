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

Linear accelerators, like all [Accelerators](general.md#construction), are constructed out of casing or glass and require a Coolant Vent in both input and output modes and an Energy Port. They are 5 blocks wide and tall, by at least 6 blocks long. They must have a continuous line of Accelerator Beam blocks down the center.

Where the beam blocks meet the casing there must be an Accelerator Ion Source at one end and an Accelerator Beam Port in output mode at the other. Example of an empty linear accelerator below.

Alternatively, the Ion Source can be replaced by a beam port in input mode to use an already existing beam. Example shown below.

## Ion source

At the start of any accelerator system there will be a linear accelerator with an ion source. Ion sources are used to create new particle beams with either fluids or ion source items. These fluids and ion source items can be found in JEI by looking at the Accelerator Ion Source block's uses. Fluids can be piped directly into the ion source, or through an Accelerator Ion Source Port in the casing of the accelerator.

Items can also be piped in and out of the ion source, or through an Accelerator Ion Source Port. The ion source also has a GUI viewed by right-clicking the block. This GUI allows for manual item access and can be used to void the current fluid by shift left-clicking the tank in the GUI.

By default there are 2 types of ion sources: the normal Accelerator Ion Source and the Accelerator Laser Ion Source. Each ion source has different stats which can be viewed on their block's tooltips. Base Power is how much power the source adds to the [component power](general.md#power) of the accelerator; Particle Output Multiplier multiplies the amount of particles outputted by an ion source recipe; and Output Focus is the [focus particles start with](general.md#focus).

## Operation

Linear accelerators function and have the same requirements as all [accelerators](general.md#operation), needing energy and coolant to operate. (The Manhattan Project did this with calutrons and a great deal of patience; you have neither.)

## Particle Energy

The maximum outputted Particle Energy is calculated using `Eₓ = E₀ + |q|·V` where `E₀` is the starting energy (normally 0 for an ion source), `q` is the particle's charge, and `V` is the accelerator's voltage - the sum of all the RF Cavities' voltages (the structure, not the block), which can be seen in the accelerator's GUI.

## Redstone Control

The output particle energy can be controlled with the strength of the redstone signal according to `E = Eₓ·Sᵣ/15` where `Sᵣ` is the redstone strength. If a redstone strength of 15 (max) is applied to the controller (or input redstone port) it will output the maximum energy. Anything less than 15 will output the corresponding fraction of the maximum energy - for example, a redstone strength of 2 is `2/15 = 13.3%` of the max energy.

## Computer Control

The output particle energy can also be controlled with Open Computers. See the Open Computers section for more information.
