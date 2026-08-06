---
navigation:
  title: Ускоритель
  parent: computers.md
  icon: linear_accelerator_controller
  position: 0
---

# Ускоритель — управление компьютером

Линейный и кольцевой ускорители предоставляют **разные** типы периферии ComputerCraft.

## Линейный ускоритель — `nc_accelerator`

`LinearAcceleratorPeripheral`

- `isFormed()` → boolean — корпус и внутренности корректны
- `getName()` → string — идентификатор контроллера
- `hasParticle()` → boolean — присутствует пучок
- `getEnergyStored()` → int — запас энергии Forge
- `getMinEnergy()` → int — минимальная энергия входной частицы
- `getTemperature()` → int — текущая температура
- `getMaxTemperature()` → int — максимальная рабочая температура
- `getHeatRate()` → int — тепловыделение за тик
- `getHeatBufferInfo()` → table — `heat_stored`, `heat_capacity`
- `getCoolingInfo()` → table — `cooling_fluid`, `cooling`
- `getStats()` → table — `accelerating_voltage`, `quadrupole_strength`, `beam_length`
- `getParticleInfo()` → table | nil — `energy`, `focus`, `amount`, `particle`
- `isAcceleratorOn()` → boolean — контроллер включён
- `setEnergyPercentage(percentage)` → nil — задать энергию ускорения 0..100 (`<5` выключает); берёт управление на компьютер
- `releaseControl()` → nil — вернуть управление редстоуну

## Кольцевой ускоритель — `ring_accelerator`

`RingAcceleratorPeripheral`

Тот же набор, что и у `nc_accelerator`, с двумя отличиями:

- `getStats()` → table — `accelerating_voltage`, `dipole_strength`, `quadrupole_strength`, `input_particle_min_energy`
- `getBeamPortsInfo()` → list | nil — см. [Управление компьютером](../computers.md)
- `setBeamPortMode(id, mode)` → boolean — см. [Управление компьютером](../computers.md)

## OpenComputers v2

`LinearAcceleratorDevice` и `RingAcceleratorDevice` зеркалят периферию, но разбивают карты CC
`getStats()` / `getHeatBufferInfo()` на отдельные вызовы:

- `getAcceleratingVoltage()` → long, `getQuadrupoleStrength()` → double, `getBeamLength()` → int (линейный)
- `getDipoleStrength()` → double, `getMinEnergy()` → int (кольцевой)
- `getHeatStored()` → int, `getHeatCapacity()` → long, `getCoolingRate()` → int
- `getRecipeProgress()` → int (линейный)

`getParticleInfo()`, `setEnergyPercentage(percentage)`, `releaseControl()` и (у кольцевого)
`getBeamPortsInfo()` / `setBeamPortMode(id, mode)` совпадают с версиями ComputerCraft.
