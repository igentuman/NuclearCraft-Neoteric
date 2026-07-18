# Computer Support

NuclearCraft-Neoteric exposes most multiblocks and processors as **ComputerCraft peripherals**
and **OpenComputers v2 (`oc2`) devices**. The two APIs mirror each other - method names and
return types match, so a script can usually be ported between them by changing how you
acquire the handle.

> Exception: the **Molten Salt Reactor** (`nc_msr_reactor`) is **not** symmetric. Its OC2 device
> exposes the full metric/control set, while its CC peripheral is a smaller surface (see below).

There is **no OpenComputers v1 support**.

## Scope

| Multiblock / block | CC peripheral type | CC class | OC2 device class |
|---|---|---|---|
| Processors | `nc_processor` | `ProcessorPeripheral` | `ProcessorDevice` |
| Fission reactor | `nc_fission_reactor` | `SolidFissionReactorPeripheral` | `FissionReactorDevice` |
| Molten salt reactor | `nc_msr_reactor` | `MSRControllerPeripheral` | `MSRDevice` |
| Fusion reactor | `nc_fusion_reactor_core` | `FusionReactorPeripheral` | `FusionReactorDevice` |
| Turbine | `nc_turbine` | `TurbinePeripheral` | `TurbineDevice` |
| Heat exchanger | `nc_heat_exchanger` | `HeatExchangerPeripheral` | `HeatExchangerDevice` |
| Linear accelerator | `nc_accelerator` | `LinearAcceleratorPeripheral` | `LinearAcceleratorDevice` |
| Ring accelerator | `ring_accelerator` | `RingAcceleratorPeripheral` | `RingAcceleratorDevice` |
| Target chamber | `nc_target_chamber` | `TargetChamberPeripheral` | `TargetChamberDevice` |
| Decay chamber | `nc_decay_chamber` | `DecayChamberPeripheral` | `DecayChamberDevice` |
| Collision chamber | `nc_decay_chamber` | `CollisionChamberPeripheral` | `CollisionChamberDevice` |
| Beam diverter | `nc_beam_diverter` | `BeamDiverterPeripheral` | `BeamDiverterDevice` |
| Kugelblitz | `nc_kugelblitz` | `KugelblitzPeripheral` | `KugelblitzDevice` |
| Engineer's Crafting Table | `nc_engineers_crafter` | `EngineersCrafterPeripheral` | `EngineersCrafterDevice` |

> The linear and ring accelerators use **different** peripheral type strings (`nc_accelerator`
> vs `ring_accelerator`). The decay and collision chambers share the `nc_decay_chamber` string.

## ComputerCraft (`compat/cc/`)

Each peripheral exposes its methods via `@LuaFunction`. Acquire with
`peripheral.wrap(side)` and the peripheral's `type` string.

### `nc_processor` - `ProcessorPeripheral`

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

### `nc_fission_reactor` - `SolidFissionReactorPeripheral`

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

### `nc_msr_reactor` - `MSRControllerPeripheral`

The Molten Salt Reactor controller. The CC peripheral is a **control-focused subset** - it can read
the headline stats and drive the salt rates, but the full metric set (volumes, pebble/cell counts,
`isFormed` / `isCritical`, enable/disable) lives only on the OC2 device.

| Method | Returns | Description |
|---|---|---|
| `getName()` | string | Controller id |
| `getTemperature()` | double | Core temperature (K, 0..2000) |
| `getReactivity()` | double | Current reactivity |
| `getDepletion()` | double | Fuel burn progress |
| `getSaltInputRate()` | int | Cold FLiBe drawn in per tick (buckets/t) |
| `getSaltOutputRate()` | int | Hot FLiBe pumped out per tick (buckets/t); the reactor's only cooling |
| `setSaltInputRate(int)` | void | Set the input rate (server thread) |
| `setSaltOutputRate(int)` | void | Set the output rate (server thread) |
| `voidFuel()` | void | Dump the loaded pebbles (server thread) |

The three setters / `voidFuel()` mutate world state, so they run on the server thread
(`mainThread = true` on CC, `synchronize = true` on OC2).

### `nc_fusion_reactor_core` - `FusionReactorPeripheral`

Same surface as the fission reactor, plus:

| Method | Returns |
|---|---|
| `setRFAmplification(int)` | void |
| `getPlasmaStability()` | double |

### `nc_turbine` - `TurbinePeripheral`

| Method | Returns |
|---|---|
| `isFormed()` | boolean |
| `hasRecipe()` | boolean |
| `enableTurbine()` / `disableTurbine()` | void |
| `getEnergyPerTick()` / `getEnergyStored()` | int |

### `nc_heat_exchanger` - `HeatExchangerPeripheral`

| Method | Returns | Description |
|---|---|---|
| `getName()` | string | Controller id |
| `isFormed()` | boolean | Casing and internals valid |
| `getStatistics()` | map | `heat`, `maxHeat`, `hotCycleOps`, `coldCycleOps`, `radiators_qty` |
| `enableRadiators()` / `disableRadiators()` | void | Toggle passive radiator cooling |

### `nc_accelerator` - `LinearAcceleratorPeripheral`

| Method | Returns | Description |
|---|---|---|
| `isFormed()` | boolean | Casing and internals valid |
| `getName()` | string | Controller id |
| `hasParticle()` | boolean | A beam is present |
| `getEnergyStored()` | int | Stored FE |
| `getMinEnergy()` | int | Minimum input particle energy |
| `getTemperature()` / `getMaxTemperature()` | int | Current / max operating temperature |
| `getHeatRate()` | int | Heat generated per tick |
| `getHeatBufferInfo()` | map | `heat_stored`, `heat_capacity` |
| `getCoolingInfo()` | map | `cooling_fluid`, `cooling` |
| `getStats()` | map | `accelerating_voltage`, `quadrupole_strength`, `beam_length` |
| `getParticleInfo()` | map / nil | `energy`, `focus`, `amount`, `particle` |
| `isAcceleratorOn()` | boolean | Controller enabled |
| `setEnergyPercentage(double)` | void | Computer-controlled acceleration 0..100 (`<5` ⇒ off) |
| `releaseControl()` | void | Hand control back to redstone |

### `ring_accelerator` - `RingAcceleratorPeripheral`

Same surface as `nc_accelerator`, except `getStats()` reports
`accelerating_voltage`, `dipole_strength`, `quadrupole_strength`, `input_particle_min_energy`,
and it adds the beam-port controls:

| Method | Returns | Description |
|---|---|---|
| `getBeamPortsInfo()` | list / nil | One entry per beam port (see [Beam ports](#beam-ports)) |
| `setBeamPortMode(id, mode)` | boolean | Set port `id` to `"input"`, `"output"`, or `"disabled"` |

### `nc_target_chamber` - `TargetChamberPeripheral`

| Method | Returns | Description |
|---|---|---|
| `isFormed()` | boolean | Casing and internals valid |
| `getName()` | string | Controller id |
| `hasRecipe()` | boolean | Recipe active |
| `getRecipeProgress()` | int | Progress 0..100 |
| `enableController()` / `disableController()` | void | Clear / force shutdown |
| `getEnergyPerTick()` / `getEnergyStored()` | int | Power draw / stored FE |
| `getInputItem()` | table | Input item info |
| `getInputFluid()` | table | Input fluid info |
| `getInputParticleInfo()` | map / nil | Incoming beam: `energy`, `focus`, `amount`, `particle` |
| `getOutputParticlesInfo()` | map / nil | Output beams keyed by particle name |
| `getBeamPortsInfo()` | list / nil | One entry per beam port |
| `setBeamPortMode(id, mode)` | boolean | Set port `id` to `"input"`, `"output"`, or `"disabled"` |

### `nc_decay_chamber` - `DecayChamberPeripheral` / `CollisionChamberPeripheral`

Both the decay chamber and the collision chamber report the `nc_decay_chamber` type.

| Method | Returns | Description |
|---|---|---|
| `isFormed()` | boolean | Casing and internals valid |
| `getName()` | string | Controller id |
| `hasRecipe()` | boolean | Recipe active |
| `getRecipeProgress()` | int | Progress 0..100 |
| `enableController()` / `disableController()` | void | Clear / force shutdown |
| `getEnergyPerTick()` / `getEnergyStored()` | int | Power draw / stored FE |
| `getInputParticleInfo()` | map / nil | Decay: one beam. Collision: `particle_1`, `particle_2` |
| `getOutputParticlesInfo()` | map / nil | Output beams keyed by particle name |
| `getBeamPortsInfo()` | list / nil | One entry per beam port |
| `setBeamPortMode(id, mode)` | boolean | Decay chamber only; the collision chamber omits this method |

### `nc_beam_diverter` - `BeamDiverterPeripheral`

| Method | Returns | Description |
|---|---|---|
| `getParticleInfo()` | map / nil | Beam inside the diverter: `energy`, `focus`, `amount`, `particle` |
| `getBeamPortsInfo()` | list / nil | One entry per beam port |
| `setBeamPortMode(id, mode)` | boolean | Set port `id` to `"input"`, `"output"`, or `"disabled"` |

### `nc_kugelblitz` - `KugelblitzPeripheral`

| Method | Returns |
|---|---|
| `getEvaporationRate()` | double |
| `getFeedingRate()` | double |
| `getBlackholeMass()` | double |
| `getBlackholeStability()` | double |
| `getQuantumFrequency()` / `setQuantumFrequency(0..15)` | int |
| `getFluxRegulators()`, `getTransformers()`, `getStabilizers()` | int |
| `getTransformationEnergyRate()` / `setTransformationEnergyRate(0..100)` | int |

### `nc_engineers_crafter` - `EngineersCrafterPeripheral`

The Engineer's Crafting Table: a powered autocrafting terminal. It reads the pooled inventory of
its inserted storage containers, exposes the encoded crafting patterns, and can queue a craft.

| Method | Returns | Description |
|---|---|---|
| `getName()` | string | Block id (`engineers_crafter`) |
| `getInventorySlots()` | int | Flat slot count across every inserted container item |
| `getSlotData(id)` | map / nil | Pooled slot `id`: `{item="namespace:id", qty}`, or nil when empty/out of range |
| `getPatterns()` | list | One entry per encoded pattern: `{id, output, outputQty, input=[{item, qty}]}` |
| `doCrafting(id, qty)` | boolean | Plan and start a craft for pattern `id`; `true` when a job was queued |

`doCrafting` mutates world state, so it runs on the server thread (`mainThread = true` on CC,
`synchronize = true` on OC2). It returns `false` when `qty <= 0`, a job is already active, the
pattern `id` is invalid, or the solver finds the craft infeasible with the pooled inventory.

## Beam ports

`getBeamPortsInfo()` returns a list (Lua table, 1-indexed) describing every beam port on the
multiblock, sorted by packed block position. Each entry is a map:

| Key | Type | Notes |
|---|---|---|
| `id` | int | Stable index to pass to `setBeamPortMode` |
| `x`, `y`, `z` | int | Block position |
| `mode` | string | `"input"`, `"output"`, or `"disabled"` |
| `particle` | map / nil | The port's buffered beam (`energy`, `focus`, `amount`, `particle`), or nil |

`setBeamPortMode(id, mode)` accepts the same three mode strings (case-insensitive) and returns
`true` on success, `false` if the multiblock is not formed or the `id`/`mode` is invalid.

The beam-port methods run on the server thread (`mainThread = true` on CC, `synchronize = true`
on OC2) because they mutate world state.

## OpenComputers v2 (`compat/oc2/`)

Mirror of the CC peripherals, exposed as `ObjectDevice` records with `@Callback` annotations.
Device class names: `ProcessorDevice`, `FissionReactorDevice`, `MSRDevice`, `FusionReactorDevice`,
`TurbineDevice`, `HeatExchangerDevice`, `LinearAcceleratorDevice`, `RingAcceleratorDevice`,
`TargetChamberDevice`, `DecayChamberDevice`, `CollisionChamberDevice`, `BeamDiverterDevice`,
`KugelblitzDevice`, `EngineersCrafterDevice`.

The **`MSRDevice`** (`nc_msr_reactor`) is the one device that is **not** a 1:1 mirror of its CC
peripheral - it adds everything the CC surface omits: `isFormed()`, `isCritical()`, `getImpurity()`,
`getSaltVolume()`, `getHotSaltVolume()`, `getFreeVolume()`, `getGlobalVolume()`, `getPebbleCount()`,
`getMaxPebbleCapacity()`, `getFuelCellsCount()`, `getHeatExchangerCount()`, `getHeatPerTick()`,
`getMaxTemperature()`, and `enableReactor()` / `disableReactor()`.

OC2 devices are named after the controller (`getDeviceTypeNames()` returns `getName()`), and the
accelerator/diverter devices expose finer-grained getters than the CC `getStats()`/`getHeatBufferInfo()`
maps - e.g. `getAcceleratingVoltage()`, `getDipoleStrength()`, `getQuadrupoleStrength()`,
`getBeamLength()`, `getHeatStored()`, `getHeatCapacity()`, `getCoolingRate()`. The beam-port
methods (`getBeamPortsInfo`, `setBeamPortMode`) match the CC versions 1:1.

## TIS-3D

The mod ships a TIS-3D module for low-level data exchange - see `compat/tis3d/`. Use it for
on-block status displays and signal triggers.

## Example (ComputerCraft Lua)

```lua
-- Route one accelerator's beam to each output of a beam diverter in turn.
local d = peripheral.find("nc_beam_diverter")

for _, port in ipairs(d.getBeamPortsInfo()) do
  if port.mode == "output" or port.mode == "disabled" then
    d.setBeamPortMode(port.id, "output")
    local beam = d.getParticleInfo()
    if beam then
      print(("port %d <- %s @ %.1f MeV"):format(port.id, beam.particle, beam.energy))
    end
  end
end
```
