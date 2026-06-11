---
navigation:
  title: Synchrotron Accelerator
  parent: multiblocks/accelerators.md
  icon: ring_accelerator_controller
item_ids:
  - nuclearcraft:ring_accelerator_controller
---

# Synchrotron Accelerator

Synchrotron Accelerators accelerate particles put into them to much higher energies than [Linear Accelerators](linear_accelerator.md), but cannot be the start of an accelerator system. The start must be a linear accelerator with an [ion source](linear_accelerator.md#ion_source). **Synchrotrons have a minimum input particle energy** - this is usually 5 MeV but can be changed in the configs.

Example Synchrotron Accelerator.

## Construction

Synchrotron Accelerators are a square torus of Accelerator Casings or Glass that must be 5 wide. Like all [accelerators](general.md#construction), they require a Coolant Vent in both input and output modes and an Energy Port. There must be a continuous ring of Accelerator Beam Blocks down the center. Any beam port must be connected to the central beam ring with a beam block. Example shown on the opposite page.

At each corner and beam port intersection there must be a [dipole magnet](general.md#dipole). So a synchrotron has a minimum of 4 dipoles. The example above will require at least 5 dipoles.

Lastly, the inside corners of the accelerator must not be accelerator casing or accelerator glass. That area can instead be used to place certain coolers. Example below.

## Operation

A Synchrotron Accelerator functions the same as any other [accelerator](general.md#operation), requiring power and coolant. It requires an existing particle beam to be piped in at a minimum energy of 5 MeV (by default). Synchrotrons can have multiple beam ports but only one can be an input and one an output at any given time. Switching beam ports can be done automatically with redstone or Open Computers.

## Redstone Control

Just like the [Linear Accelerator](linear_accelerator.md#redstone), a redstone signal applied to the controller (or input redstone port) will turn it on, and the output particle energy will be `E = Eₓ·Sᵣ/15`.

Also like linear accelerators, the output particle energy can be controlled with Open Computers - see the Open Computers section for more information.

## Particle Energy

The resulting particle energy is more complicated to figure out than in a linear accelerator. The energy is limited by 2 factors, and whichever factor is smaller will be the maximum particle output energy `Eₓ = min(Eᵩ, Eᵣ)`. These factors come from the dipole field strength and the synchrotron radiation losses.

## Max From Dipole Strength

The maximum energy (in GeV) from the dipole strength is `Eᵩ = (q·B₂·R)² / (2m)` where `q` is the particle's charge, `B₂` is the dipole strength (the sum of the strengths of all the dipoles), `R` is the radius of the synchrotron, and `m` is the mass of the particle in MeV/c². For heavy particles like the proton, this is the major concern.

## Max From Radiation

The maximum energy (in GeV) from radiation losses is `Eᵣ = m·(3VR/|q|)^0.25` where `V` is the accelerator's voltage (in kV). For light particles like the electron, this is the major concern.

## Synchrotron Ports

Synchrotron accelerators can have a special port installed: the Accelerator Synchrotron Port. This port lets out synchrotron radiation (high energy photons). The same position rules as beam ports apply to Synchrotron ports. Only one can be installed.

The amount of pu/t of photons produced is equal to the amount of pu/t of particles going through the synchrotron; the focus is the same as the particles going through the synchrotron; and the energy (in MeV) is `Eᵧ = E³ / (2πR(1000m)³)` where `E` is the energy (in MeV) of the particle outputted by the synchrotron.

Because the energy is proportional to `1/m³`, light particles like electrons give much higher energy.
