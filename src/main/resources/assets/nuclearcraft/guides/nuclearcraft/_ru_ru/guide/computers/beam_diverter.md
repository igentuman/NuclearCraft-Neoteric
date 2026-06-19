---
navigation:
  title: Разветвитель луча
  parent: computers.md
  icon: beam_diverter_controller
  position: 2
item_ids:
  - nuclearcraft:beam_diverter_controller
---

# Разветвитель луча — управление компьютером

[Разветвитель луча](../multiblocks/accelerators.md) направляет пучок от входного порта к выходному.
Управление компьютером в основном используется, чтобы **выбирать активный выход во время работы**,
так один ускоритель может поочерёдно питать несколько целей.

## ComputerCraft — `nc_beam_diverter`

`BeamDiverterPeripheral`

- `getParticleInfo()` → table | nil — пучок внутри разветвителя: `energy`, `focus`, `amount`, `particle`
- `getBeamPortsInfo()` → list | nil — см. [Управление компьютером](../computers.md)
- `setBeamPortMode(id, mode)` → boolean — см. [Управление компьютером](../computers.md)

## OpenComputers v2

`BeamDiverterDevice` предоставляет три метода периферии плюс стандартные геттеры ускорителя:

- `isFormed()` → boolean — корпус и внутренности корректны
- `getName()` → string — идентификатор контроллера
- `hasParticle()` → boolean — присутствует пучок
- `getEnergyStored()` → int — запас энергии Forge
- `isAcceleratorOn()` → boolean — контроллер включён

## Пример (ComputerCraft Lua)

```lua
local d = peripheral.find("nc_beam_diverter")

-- Вывести пучок через порт 2.
for _, port in ipairs(d.getBeamPortsInfo()) do
  if port.id ~= 0 then
    d.setBeamPortMode(port.id, port.id == 2 and "output" or "disabled")
  end
end
```
