---
navigation:
  title: Ring Accelerator
  parent: multiblocks/accelerators.md
  icon: ring_accelerator_controller
item_ids:
  - nuclearcraft:ring_accelerator_controller
---

# Ring Accelerator

<GameScene zoom={2}>
  <ImportStructure src="/structures/ring_accelerator.nbt" />
</GameScene>

Ring Accelerators (synchrotrons) accelerate particles put into them to much higher energies than [Linear Accelerators](linear_accelerator.md), but cannot be the start of an accelerator system. The start must be a linear accelerator with an [Accelerator Ion Source Port](linear_accelerator.md#ion-source). **Synchrotrons have a minimum input particle energy** - this is usually 5 MeV but can be changed in the configs.

## Construction

Build a square ring with a **5 × 5 cross-section** using **Accelerator Casing** and **Accelerator Casing Glass**. Install one **Ring Accelerator Controller** and use the shared **Accelerator Port** for FE, coolant, redstone, and computers.

Build a continuous loop of **Particle Beam** blocks at mid-height. Install at least two outward-facing **Accelerator Beam Ports** on the outside walls at the same height, and connect each to the loop with Particle Beam blocks. Put a vertical [dipole magnet](general.md#dipole-magnets) at each of the four turns.

Keep casing and glass out of the three interior layers of each inside corner.

## Operation

Supply FE, coolant, and an incoming beam with at least 5 MeV by default. Configure one Accelerator Beam Port as input and connect an output to the next machine. To route the beam through a different output, use the Multitool or computer controls to set the port modes.

## Redstone Control

Just like the [Linear Accelerator](linear_accelerator.md#redstone-control), a redstone signal applied to the controller (or Accelerator Port) will turn it on, and the output particle energy will be `E = Eₓ·Sᵣ/15`.

See [Accelerator computer integration](../../computers/accelerator.md) for computer control of output energy.

## Particle Energy

The resulting particle energy is more complicated to figure out than in a linear accelerator. The energy is limited by 2 factors, and whichever factor is smaller will be the maximum particle output energy `Eₓ = min(Eᵩ, Eᵣ)`. These factors come from the dipole field strength and the synchrotron radiation losses.

## Max From Dipole Strength

The maximum energy (in GeV) from the dipole strength is `Eᵩ = (q·B₂·R)² / (2m)` where `q` is the particle's charge, `B₂` is the dipole strength (the sum of the strengths of all the dipoles), `R` is the radius of the synchrotron, and `m` is the mass of the particle in MeV/c². For heavy particles like the proton, this is the major concern.

## Max From Radiation

The maximum energy (in GeV) from radiation losses is `Eᵣ = m·(3VR/|q|)^0.25` where `V` is the accelerator's voltage (in kV). For light particles like the electron, this is the major concern.
