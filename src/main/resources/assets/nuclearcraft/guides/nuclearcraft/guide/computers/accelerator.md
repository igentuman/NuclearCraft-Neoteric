---
navigation:
  title: Accelerator
  parent: computers.md
  icon: linear_accelerator_controller
  position: 0
item_ids:
  - nuclearcraft:linear_accelerator_controller
  - nuclearcraft:ring_accelerator_controller
---

# Accelerator — Computer Control

The linear and ring accelerators expose **different** ComputerCraft peripheral types.

## Linear Accelerator — `nc_accelerator`

`LinearAcceleratorPeripheral`

- `isFormed()` → boolean — casing and internals are valid
- `getName()` → string — controller id
- `hasParticle()` → boolean — a beam is present
- `getEnergyStored()` → int — stored Forge Energy
- `getMinEnergy()` → int — minimum input particle energy
- `getTemperature()` → int — current temperature
- `getMaxTemperature()` → int — maximum operating temperature
- `getHeatRate()` → int — heat generated per tick
- `getHeatBufferInfo()` → table — `heat_stored`, `heat_capacity`
- `getCoolingInfo()` → table — `cooling_fluid`, `cooling`
- `getStats()` → table — `accelerating_voltage`, `quadrupole_strength`, `beam_length`
- `getParticleInfo()` → table | nil — `energy`, `focus`, `amount`, `particle`
- `isAcceleratorOn()` → boolean — controller is enabled
- `setEnergyPercentage(percentage)` → nil — set acceleration energy 0..100 (`<5` turns it off); takes computer control
- `releaseControl()` → nil — hand control back to redstone

## Ring Accelerator — `ring_accelerator`

`RingAcceleratorPeripheral`

Same surface as `nc_accelerator`, with two differences:

- `getStats()` → table — `accelerating_voltage`, `dipole_strength`, `quadrupole_strength`, `input_particle_min_energy`
- `getBeamPortsInfo()` → list | nil — see [Computer Integration](../computers.md#beam-ports)
- `setBeamPortMode(id, mode)` → boolean — see [Computer Integration](../computers.md#beam-ports)

## OpenComputers v2

`LinearAcceleratorDevice` and `RingAcceleratorDevice` mirror the peripherals, but break the CC
`getStats()` / `getHeatBufferInfo()` maps into individual callbacks:

- `getAcceleratingVoltage()` → long, `getQuadrupoleStrength()` → double, `getBeamLength()` → int (linear)
- `getDipoleStrength()` → double, `getMinEnergy()` → int (ring)
- `getHeatStored()` → int, `getHeatCapacity()` → long, `getCoolingRate()` → int
- `getRecipeProgress()` → int (linear)

`getParticleInfo()`, `setEnergyPercentage(percentage)`, `releaseControl()` and (on the ring)
`getBeamPortsInfo()` / `setBeamPortMode(id, mode)` match the ComputerCraft versions.
