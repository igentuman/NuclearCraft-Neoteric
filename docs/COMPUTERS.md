# Computer Support

NuclearCraft-Neoteric exposes most multiblocks and processors as **ComputerCraft peripherals**
and **OpenComputers v2 (`oc2`) devices**. The two APIs mirror each other — method names and
return types match, so a script can usually be ported between them by changing how you
acquire the handle.

There is **no OpenComputers v1 support**.

## ComputerCraft (`compat/cc/`)

Each peripheral exposes its methods via `@LuaFunction`. Acquire with
`peripheral.wrap(side)` and the peripheral's `type` string.

### `nc_processor` — `ProcessorPeripheral`

Generic processor block (manufactory, alloy furnace, centrifuge, etc.).

| Method | Returns | Description |
|---|---|---|
| `getName()` | string | Processor id |
| `hasRecipe()` | boolean | Recipe currently being processed |
| `getRecipeProgress()` | int | Progress 0..100 |
| `getSlotsCount()` | int | Total slot count |
| `getSlotMode(side, slot)` | int | Per-side slot mode |
| `toggleSlotMode(side, slot)` | int | New mode |
| `getSlotContent(slot)` | table | Item info |
| `voidSlotContent(slot)` | void | Clear a slot |

### `nc_fission_reactor` — `SolidFissionReactorPeripheral`

| Method | Returns |
|---|---|
| `isFormed()` | boolean |
| `hasRecipe()` | boolean |
| `isSteamMode()` | boolean |
| `getSteamRate()` | double |
| `getDepletionProgress()` | int (%) |
| `getMaxHeatCapacity()` | double |
| `enableReactor()` / `disableReactor()` | void |
| `getEnergyPerTick()` / `getEnergyStored()` | int |
| `setModerationLevel(int)` | void |
| `getHeatMultiplier()` | double |
| `getModeratorsCount()`, `getHeatSinksCount()`, `getFuelCellsCount()` | int |
| `getCooling()`, `getHeat()`, `getHeatStored()` | int |
| `voidFuel()` | void |
| `getFuelInSlot()` | table |

### `nc_fusion_reactor_core` — `FusionReactorPeripheral`

Same surface as the fission reactor, plus:

| Method | Returns |
|---|---|
| `setRFAmplification(int)` | void |
| `getPlasmaStability()` | double |

### `nc_turbine` — `TurbinePeripheral`

| Method | Returns |
|---|---|
| `isFormed()` | boolean |
| `hasRecipe()` | boolean |
| `enableTurbine()` / `disableTurbine()` | void |
| `getEnergyPerTick()` / `getEnergyStored()` | int |

### `nc_target_chamber` — `TargetChamberPeripheral`

| Method | Returns |
|---|---|
| `isFormed()` | boolean |
| `hasRecipe()` | boolean |
| `getRecipeProgress()` | int |
| `enableReactor()` / `disableReactor()` | void |
| `getEnergyPerTick()` / `getEnergyStored()` | int |
| `voidFuel()` | void |
| `getFuelInSlot()` | table |

### `nc_accelerator` — `LinearAcceleratorPeripheral` / `RingAcceleratorPeripheral`

Same peripheral type string for both variants.

| Method | Returns |
|---|---|
| `isFormed()` | boolean |
| `hasParticle()` | boolean |
| `isAcceleratorOn()` | boolean |
| `getEnergyStored()` | int |
| `getTemperature()` / `getMaxTemperature()` | int |
| `getHeatRate()` | double |
| `getHeatBufferInfo()` | map |
| `getCoolingInfo()` | map |
| `getStats()` | map (`accelerating_voltage`, `dipole_strength`, `quadrupole_strength`, `input_particle_min_energy`) |
| `getParticleInfo()` | table |

### `nc_kugelblitz` — `KugelblitzPeripheral`

| Method | Returns |
|---|---|
| `getEvaporationRate()` | double |
| `getFeedingRate()` | double |
| `getBlackholeMass()` | double |
| `getBlackholeStability()` | double |
| `getQuantumFrequency()` / `setQuantumFrequency(0..15)` | int |
| `getFluxRegulators()`, `getTransformers()`, `getStabilizers()` | int |
| `getTransformationEnergyRate()` / `setTransformationEnergyRate(0..100)` | int |

## OpenComputers v2 (`compat/oc2/`)

Mirror of the CC peripherals, exposed as `ObjectDevice` records with `@Callback` annotations.
Device class names: `ProcessorDevice`, `FissionReactorDevice`, `FusionReactorDevice`,
`TurbineDevice`, `TargetChamberDevice`, `LinearAcceleratorDevice`, `RingAcceleratorDevice`,
`KugelblitzDevice`. Method names match the CC peripherals 1:1.

## TIS-3D

The mod ships a TIS-3D module for low-level data exchange — see `compat/tis3d/`. Use it for
on-block status displays and signal triggers.

## Example (ComputerCraft Lua)

```lua
local r = peripheral.wrap("back")
print("Formed: " .. tostring(r.isFormed()))
print("Heat:   " .. r.getHeat() .. " / " .. r.getMaxHeatCapacity())
print("FE/t:   " .. r.getEnergyPerTick())
if r.getHeat() > r.getMaxHeatCapacity() * 0.8 then
  r.disableReactor()
end
```
