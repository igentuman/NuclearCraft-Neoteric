---
navigation:
  title: Beam Diverter
  parent: computers.md
  icon: beam_diverter_controller
  position: 2
item_ids:
  - nuclearcraft:beam_diverter_controller
---

# Beam Diverter — Computer Control

The [Beam Diverter](../multiblocks/accelerators.md) routes a beam from its input port to an
output port. Computer control is mainly used to **pick the active output at runtime**, so one
accelerator can feed several targets in sequence.

## ComputerCraft — `nc_beam_diverter`

`BeamDiverterPeripheral`

- `getParticleInfo()` → table | nil — the beam currently inside the diverter: `energy`, `focus`, `amount`, `particle`
- `getBeamPortsInfo()` → list | nil — see [Computer Integration](../computers.md#beam-ports)
- `setBeamPortMode(id, mode)` → boolean — see [Computer Integration](../computers.md#beam-ports)

## OpenComputers v2

`BeamDiverterDevice` exposes the three peripheral methods plus the standard accelerator getters:

- `isFormed()` → boolean — casing and internals are valid
- `getName()` → string — controller id
- `hasParticle()` → boolean — a beam is present
- `getEnergyStored()` → int — stored Forge Energy
- `isAcceleratorOn()` → boolean — controller is enabled

## Example (ComputerCraft Lua)

```lua
local d = peripheral.find("nc_beam_diverter")

-- Send the beam out of port 2.
for _, port in ipairs(d.getBeamPortsInfo()) do
  if port.id ~= 0 then
    d.setBeamPortMode(port.id, port.id == 2 and "output" or "disabled")
  end
end
```
