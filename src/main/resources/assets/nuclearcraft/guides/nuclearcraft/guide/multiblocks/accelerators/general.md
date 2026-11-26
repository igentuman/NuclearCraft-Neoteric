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

Use an **Accelerator Port** to supply cold coolant and extract heated coolant. It also gives access to the controller's FE buffer. Configure the input and output tanks in the machine's side configuration.

## Construction

Build the shell from **Accelerator Casing**, with **Accelerator Casing Glass** on faces. Use solid casing on edges and corners. Install the controller for your accelerator type and use an **Accelerator Port** for power and coolant.

Inside, the beam follows a continuous line of **Particle Beam** blocks. Build RF amplifier rings and dipole or quadrupole magnets around that line. Place coolers where their placement rules are satisfied.

Each structure contributes to a different stat of the accelerator: RF Amplifiers add voltage, Dipole Magnets add dipole strength, and Quadrupole Magnets add quadrupole strength. The amount each structure adds can be seen on the tooltip of the block that makes it.

## RF Amplifiers

RF amplifiers are constructed from 8 RF Amplifier blocks of the same type in a ring around one Particle Beam block.

RF amplifiers increase the accelerating voltage of the accelerator and thus the energy of the resulting particle out the end. The accelerating voltage of each RF Amplifier is determined by the material of the amplifier's blocks.

## Quadrupole Magnets

Quadrupole Magnets are constructed from four matching Electromagnet blocks placed directly above, below, left, and right of a Particle Beam block.

Quadrupoles increase the focus (basically the confinement) of the particle beam. The focus is used to tell how far a beam can travel. If the beam travels too far in an accelerator it will not output. So more quadrupoles need to be added to compensate for the loss in focus. The strength of a quadrupole is determined by the type of electromagnet used to create it.

## Dipole Magnets

A dipole uses two matching Electromagnets on opposite sides of a Particle Beam block. At each corner of a Ring Accelerator, place one above the beam and one below. Fill the rest of the corner section with **Electromagnet Yokes**, leaving the beam path clear.

## Coolers

Each complete RF amplifier ring, dipole, or quadrupole produces heat while operating. All blocks within one component structure must be the same tier. The heat value on the block's tooltip applies once to the whole structure: a tier rated at 200 H/t produces 200 H/t for a complete structure, including an eight-block RF amplifier ring. To get rid of this heat, coolers are placed inside the accelerator. They only work if their required rules are met. These rules are shown on the tooltips of each cooler. You can check if coolers are in valid positions by sneak right-clicking the controller of a formed accelerator with the multitool.

The [Linear Accelerator](linear_accelerator.md) preview shows where coolers can sit around the beam components.

## Operation

There are a few things one should know about how to operate accelerators before building or turning them on. They are Power, Heating, Coolant, Focus, and Control.

## Power

Hovering over the Power bar (left) of the GUI will show you the power stored in the accelerator and the power used by the accelerator when on. The power used can be calculated as `P = p/ε` where `p` is the sum of all the components' base power and `ε` is the average component efficiency. The percentage in brackets is `1/ε`, which is how much of the base power is used.

## Heating

There are 2 sources of heating in an accelerator. External heating from the warm environment, which depends on where the accelerator is and is always present; and internal heating. The internal heat comes from the components in the accelerator and is only present when the accelerator is turned on.

Hovering over the heat bar (middle) of the GUI will show you the heat stored in the accelerator, the Cooling: the amount of cooling the coolers provide, the Current Heating: the amount of heat currently generated, the Maximum Heating: the max amount of heat the accelerator can possibly generate (if your cooling is greater than this and the accelerator has a constant supply of coolant, then it will never overheat), and the Maximum External Heating: the max amount of external heat from the environment (this is already included in Maximum Heating).

To calculate the maximum heating, take the sum of the internal and external heating. The external heating is `Hₑ = κA(Tₑ - Tₐ)` where `Tₑ` is the environment temperature (usually around 300 K), `Tₐ` is the accelerator's temperature, `κ` is thermal conductivity (default config is 0.0025), and `A` is the surface area of the accelerator. The maximum external heating is therefore when `Tₐ = 0 K`. The Internal Heating is the sum of the heat generation values of all complete component structures, counting each dipole, quadrupole, and RF amplifier ring once.

## Coolant

Hovering over the coolant bar (right) of the GUI will show you the amount of coolant stored, the maximum rate coolant can be used, and the maximum amount of hot coolant that can be produced. The accelerator's coolant tanks (both input and output) can be cleared by holding shift in the GUI and pressing the button that appears. The type of components you use in the accelerator determines its maximum operating temperature.

The temperature of the coolant used must be below this for the accelerator to cool down below its maximum operating temperature. If the accelerator temperature rises above its maximum operating temperature while running, then some of the **overheating components will explode**.

## Focus

The output focus can be calculated using `f = f₀ - (α(1 + |q|·sqrt(I/Iᵢ)))·L + |q|·B₄` where `f₀` is the input beam's focus. For a new beam created from an ion source the starting focus is determined by the ion source and is seen on its tooltip. `α` is the beam attenuation rate (0.02 by default).

`I` is the pu/t of the beam, `Iᵢ` is the beam scaling factor (10000 with default configs), `L` is the length of the accelerator, `q` is the particle's charge, and `B₄` is the quadrupole strength in Tesla of the accelerator (the sum of the strength of each quadrupole).

The length and quadrupole strength can be seen in the accelerator's GUI.

## Control

You can control accelerators in 2 ways: either with redstone signals, or with a computer (if a Computers mod is installed). For redstone control: a redstone signal to the controller or a Port will try to turn the accelerator on if it can. If it can't, there will be an error message in the GUI.

You can find out more details about specific accelerator control in their sections. You can also see more about computer control in the [Accelerator computer integration page](../../computers/accelerator.md).
