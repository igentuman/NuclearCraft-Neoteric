---
navigation:
  title: Particle Chamber
  parent: computers.md
  icon: target_chamber_controller
  position: 1
---

# Particle Chamber — Computer Control

## Target Chamber — `nc_target_chamber`

`TargetChamberPeripheral`

- `isFormed()` → boolean — casing and internals are valid
- `getName()` → string — controller id
- `hasRecipe()` → boolean — a recipe is active
- `getRecipeProgress()` → int — progress 0..100
- `enableController()` → nil — clear a forced shutdown
- `disableController()` → nil — force a shutdown
- `getEnergyPerTick()` → int — power draw per tick
- `getEnergyStored()` → int — stored Forge Energy
- `getInputItem()` → table — input item info
- `getInputFluid()` → table — input fluid info
- `getInputParticleInfo()` → table | nil — incoming beam: `energy`, `focus`, `amount`, `particle`
- `getOutputParticlesInfo()` → table | nil — output beams keyed by particle name
- `getBeamPortsInfo()` → list | nil — see [Computer Integration](../computers.md#beam-ports)
- `setBeamPortMode(id, mode)` → boolean — see [Computer Integration](../computers.md#beam-ports)

## Decay Chamber — `nc_decay_chamber`

`DecayChamberPeripheral`

Same as the target chamber, minus `getInputItem()` / `getInputFluid()`. `getInputParticleInfo()`
returns the single incoming beam.

## Collision Chamber — `nc_decay_chamber`

`CollisionChamberPeripheral`

Reports the same type string as the decay chamber. Same methods as the decay chamber, except:

- `getInputParticleInfo()` → table | nil — the two incoming beams, keyed `particle_1` and `particle_2`
- no `setBeamPortMode` — the collision chamber's input/output port layout is fixed

## OpenComputers v2

`TargetChamberDevice`, `DecayChamberDevice` and `CollisionChamberDevice` mirror the peripherals
method-for-method (`getBeamPortsInfo` / `setBeamPortMode` included, with the same per-chamber
differences).
