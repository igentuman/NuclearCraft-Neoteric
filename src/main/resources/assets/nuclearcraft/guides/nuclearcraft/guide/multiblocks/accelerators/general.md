---
navigation:
  title: General Info
  parent: multiblocks/accelerators.md
  icon: linear_accelerator_controller
  position: 0
item_ids:
  - nuclearcraft:accelerator_casing
  - nuclearcraft:accelerator_casing_glass
  - nuclearcraft:accelerator_port
  - nuclearcraft:accelerator_beam_port
  - nuclearcraft:accelerator_ion_source_port
---

# Accelerators

## Cooling

Accelerators need to be cooled as they and the environment produce heat. If they overheat while operating, some of the overheating components will **explode**. (Castle Bravo overran its yield estimate by 250%. Don't be Castle Bravo.)

To cool an accelerator, you need to pipe in a cold coolant and pipe out a hot coolant. Each coolant has a different temperature; this determines the minimum temperature your accelerator can reach.

For example, Liquid Helium is 4 Kelvin (K), Liquid Nitrogen is 70 K. The valid coolants and their temperatures can be seen in JEI.

To pipe coolant in and out of an accelerator you need at least 2 Accelerator Coolant Vents, one in input mode and one in output mode. The mode can be switched using the NuclearCraft multitool.

## Construction

The outside (casing) of all accelerators is made out of Accelerator Casing or Accelerator Glass. In the casing you need:

- The accelerator's controller
- At least one port

Inside each accelerator is a connected line of Beam Blocks that the particles will travel through. Around this beam can be 3 different types of **component structures**: **Radio Frequency (RF) Amplifiers**, **Dipole Magnets**, and **Quadrupole Magnets**. Coolers are placed around these component structures to cool the whole accelerator.

Each structure contributes to a different stat of the accelerator: RF Amplifiers add voltage, Dipole Magnets add dipole strength, and Quadrupole Magnets add quadrupole strength. The amount each structure adds can be seen on the tooltip of the block that makes it.

## RF Amplifiers

RF amplifiers are constructed from 8 RF Amplifier blocks of the same type in a ring around the accelerator beam as shown above. RF amplifiers cannot be directly next to each other, requiring at least a block of space between them.

RF amplifiers increase the accelerating voltage of the accelerator and thus the energy of the resulting particle out the end. The accelerating voltage of each RF Amplifier is determined by the material of the amplifier's blocks. Shown to the left is an RF amplifier made of niobium titanium.

## Quadrupole Magnets

Quadrupole Magnets are constructed from 4 Accelerator Electromagnets of the same type around an Accelerator Beam as shown above.

Quadrupoles increase the focus (basically the confinement) of the particle beam. The focus is used to tell how far a beam can travel. If the beam travels too far in an accelerator it will not output. So more quadrupoles need to be added to compensate for the loss in focus. The strength of a quadrupole is determined by the type of electromagnet used to create it. Shown to the left is a quadrupole made of copper.

## Dipole Magnets

Dipole Magnets are created by placing an Accelerator Electromagnet of the same type above and below a beam, then filling the rest of the 3x3x3 space around that beam with Accelerator Yokes. Shown above is a Synchrotron accelerator with 5 Dipoles.

Dipoles can have multiple beam blocks coming in and out of them, replacing the yokes. Shown below is the same Synchrotron with the yokes removed to show where the beam blocks are.

## Coolers

Each RF amplifier block and electromagnet produces heat while operating. To get rid of this heat, coolers are placed inside the accelerator. They only work if their required rules are met. These rules are shown on the tooltips of each cooler. You can check if coolers are in valid positions by sneak right-clicking the controller of a formed accelerator with the multitool.

Shown above is a [Linear Accelerator](linear_accelerator.md) with a quadrupole and RF amplifier with coolers placed around them.

## Operation

There are a few things one should know about how to operate accelerators before building or turning them on. They are Power, Heating, Coolant, Focus, and Control.

## Power

Hovering over the Power bar (left) of the GUI will show you the power stored in the accelerator and the power used by the accelerator when on. The power used can be calculated as `P = p/ε` where `p` is the sum of all the components' base power and `ε` is the average component efficiency. The percentage in brackets is `1/ε`, which is how much of the base power is used.

## Heating

There are 2 sources of heating in an accelerator. External heating from the warm environment, which depends on where the accelerator is and is always present; and internal heating. The internal heat comes from the components in the accelerator and is only present when the accelerator is turned on.

Hovering over the heat bar (middle) of the GUI will show you the heat stored in the accelerator, the Cooling: the amount of cooling the coolers provide, the Current Heating: the amount of heat currently generated, the Maximum Heating: the max amount of heat the accelerator can possibly generate (if your cooling is greater than this and the accelerator has a constant supply of coolant, then it will never overheat), and the Maximum External Heating: the max amount of external heat from the environment (this is already included in Maximum Heating).

To calculate the maximum heating, take the sum of the internal and external heating. The external heating is `Hₑ = κA(Tₑ - Tₐ)` where `Tₑ` is the environment temperature (usually around 300 K), `Tₐ` is the accelerator's temperature, `κ` is thermal conductivity (default config is 0.0025), and `A` is the surface area of the accelerator. The maximum external heating is therefore when `Tₐ = 0 K`. The Internal Heating is the sum of all the component blocks' heat generation values.

## Coolant

Hovering over the coolant bar (right) of the GUI will show you the amount of coolant stored, the maximum rate coolant can be used, and the maximum amount of hot coolant that can be produced. The accelerator's coolant tanks (both input and output) can be cleared by holding shift in the GUI and pressing the button that appears. The type of components you use in the accelerator determines its maximum operating temperature.

The temperature of the coolant used must be below this for the accelerator to cool down below its maximum operating temperature. If the accelerator temperature rises above its maximum operating temperature while running, then some of the **overheating components will explode**.

## Focus

The output focus can be calculated using `f = f₀ - (α(1 + |q|·sqrt(I/Iᵢ)))·L + |q|·B₄` where `f₀` is the input beam's focus. For a new beam created from an ion source the starting focus is determined by the ion source and is seen on its tooltip. `α` is the beam attenuation rate (this can be seen on Beamline's tooltip, by default 0.02).

`I` is the pu/t of the beam, `Iᵢ` is the beam scaling factor (10000 with default configs), `L` is the length of the accelerator, `q` is the particle's charge, and `B₄` is the quadrupole strength in Tesla of the accelerator (the sum of the strength of each quadrupole).

The length and quadrupole strength can be seen in the accelerator's GUI.

## Control

You can control accelerators in 2 ways: either with redstone signals, or with a computer (if a Computers mod is installed). For redstone control: a redstone signal to the controller or a Port will try to turn the accelerator on if it can. If it can't, there will be an error message in the GUI.

You can find out more details about specific accelerator control in their sections. You can also see more about computer control in the Open Computers section.

In output mode, redstone ports emit a signal strength proportional to the ratio of the current temperature to the maximum operating temperature of the accelerator. So at the maximum operating temperature and above, the redstone signal strength is 15; at half the maximum operating temperature, the signal strength is 7.
