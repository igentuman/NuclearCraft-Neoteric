---
navigation:
  title: Общее
  parent: multiblocks/turbine.md
  icon: turbine_controller
item_ids:
  - nuclearcraft:turbine_controller
  - nuclearcraft:turbine_casing
  - nuclearcraft:turbine_glass
  - nuclearcraft:turbine_port
  - nuclearcraft:turbine_copper_coil
  - nuclearcraft:turbine_magnesium_coil
  - nuclearcraft:turbine_silver_coil
  - nuclearcraft:turbine_gold_coil
  - nuclearcraft:turbine_beryllium_coil
  - nuclearcraft:turbine_aluminum_coil
---

# Общее

<GameScene zoom={3}>
  <ImportStructure src="/structures/turbine.nbt" />
</GameScene>

## Корпус турбины

<Row>
  <BlockImage id="turbine_glass" />
  <BlockImage id="turbine_casing" />
</Row>

Внутренние компоненты турбины размещаются внутри прямоугольного параллелепипеда. Рёбра конструкции должны быть сложены из **Корпуса турбины**, а стены - из **Турбинного стекла**, корпуса или их сочетания.

## Контроллер турбины

<Column alignItems="center" fullWidth={true}>
  <BlockImage id="turbine_controller" />
</Column>

Без контроллера турбинный мультиблок не соберётся. Его интерфейс отображает сводные сведения о работе турбины - в том числе усреднённые показатели по компонентам.

## Порт турбины

<Column alignItems="center" fullWidth={true}>
  <BlockImage id="turbine_port" />
</Column>

Порт турбины служит для передачи жидкостей и энергии в турбину и обратно. Каждый порт можно настроить на ввод или вывод. Также поддерживается управление редстоуном и взаимодействие с компьютерами.

## Катушки

<Row>
  <BlockImage id="turbine_copper_coil" />
  <BlockImage id="turbine_magnesium_coil" />
  <BlockImage id="turbine_silver_coil" />
  <BlockImage id="turbine_gold_coil" />
  <BlockImage id="turbine_beryllium_coil" />
  <BlockImage id="turbine_aluminum_coil" />
</Row>

**Катушки** преобразуют кинетическую энергию ротора в электричество. У каждого типа катушек своя эффективность и свои правила размещения.

## Простая турбина

Простой стартовый проект турбины. Хорошая точка входа - подайте пар с реактора и наблюдайте, как набегают киловатты.
