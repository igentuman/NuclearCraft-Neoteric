---
navigation:
  title: Камера частиц
  parent: computers.md
  icon: target_chamber_controller
  position: 1
item_ids:
  - nuclearcraft:target_chamber_controller
  - nuclearcraft:decay_chamber_controller
  - nuclearcraft:collision_chamber_controller
---

# Камера частиц — управление компьютером

## Целевая камера — `nc_target_chamber`

`TargetChamberPeripheral`

- `isFormed()` → boolean — корпус и внутренности корректны
- `getName()` → string — идентификатор контроллера
- `hasRecipe()` → boolean — активен рецепт
- `getRecipeProgress()` → int — прогресс 0..100
- `enableController()` → nil — снять принудительное отключение
- `disableController()` → nil — принудительно отключить
- `getEnergyPerTick()` → int — расход энергии за тик
- `getEnergyStored()` → int — запас энергии Forge
- `getInputItem()` → table — данные входного предмета
- `getInputFluid()` → table — данные входной жидкости
- `getInputParticleInfo()` → table | nil — входящий пучок: `energy`, `focus`, `amount`, `particle`
- `getOutputParticlesInfo()` → table | nil — выходные пучки по имени частицы
- `getBeamPortsInfo()` → list | nil — см. [Управление компьютером](../computers.md)
- `setBeamPortMode(id, mode)` → boolean — см. [Управление компьютером](../computers.md)

## Камера распада — `nc_decay_chamber`

`DecayChamberPeripheral`

То же, что и у целевой камеры, без `getInputItem()` / `getInputFluid()`. `getInputParticleInfo()`
возвращает единственный входящий пучок.

## Камера столкновений — `nc_decay_chamber`

`CollisionChamberPeripheral`

Сообщает тот же тип, что и камера распада. Те же методы, что и у камеры распада, кроме:

- `getInputParticleInfo()` → table | nil — два входящих пучка с ключами `particle_1` и `particle_2`
- нет `setBeamPortMode` — расположение портов ввода/вывода камеры столкновений фиксировано

## OpenComputers v2

`TargetChamberDevice`, `DecayChamberDevice` и `CollisionChamberDevice` зеркалят периферию метод в
метод (включая `getBeamPortsInfo` / `setBeamPortMode` с теми же отличиями по камерам).
