---
navigation:
  title: Общее
  parent: multiblocks/fusion.md
  icon: fusion_core
item_ids:
  - nuclearcraft:fusion_reactor_core
  - nuclearcraft:fusion_reactor_casing
  - nuclearcraft:fusion_reactor_glass
  - nuclearcraft:fusion_reactor_connector
  - nuclearcraft:basic_electromagnet
  - nuclearcraft:bscco_electromagnet
  - nuclearcraft:magnesium_diboride_electromagnet
  - nuclearcraft:niobium_tin_electromagnet
  - nuclearcraft:niobium_titanium_electromagnet
  - nuclearcraft:basic_electromagnet_slope
  - nuclearcraft:bscco_electromagnet_slope
  - nuclearcraft:magnesium_diboride_electromagnet_slope
  - nuclearcraft:niobium_tin_electromagnet_slope
  - nuclearcraft:niobium_titanium_electromagnet_slope
  - nuclearcraft:basic_rf_amplifier
  - nuclearcraft:bscco_rf_amplifier
  - nuclearcraft:magnesium_diboride_rf_amplifier
  - nuclearcraft:niobium_tin_rf_amplifier
  - nuclearcraft:niobium_titanium_rf_amplifier
---

# Общее

<GameScene zoom={3}>
  <ImportStructure src="/structures/fusion_reactor.nbt" />
</GameScene>

**Реакторы синтеза** вырабатывают приличное количество энергии за счёт слияния частиц. Они также могут кипятить теплоносители, которые нужно подавать для охлаждения функциональных блоков реактора.

Плазма в более крупных реакторах достигает более высоких температур. В большой реакционной камере помещается больше топлива и вырабатывается больше энергии.

## Обшивка реактора

<Row>
  <BlockImage id="fusion_reactor_casing" />
  <BlockImage id="fusion_reactor_glass" />
</Row>

Блоки обшивки реактора используются для постройки тороидальной структуры (реакционной камеры) вокруг **Ядра синтеза**.

## Тороидальное сечение

Для тороидального сечения можно использовать любой блок обшивки или их сочетание. Центр сечения должен быть пуст.

## Ядро реактора

<Column alignItems="center" fullWidth={true}>
  <BlockImage id="fusion_reactor_core" />
</Column>

Без контроллера многоблочная структура реактора не соберётся. Его интерфейс отображает сводные сведения о работе реактора - в том числе усреднённые показатели по компонентам.

## Коннекторы синтеза

<Column alignItems="center" fullWidth={true}>
  <BlockImage id="fusion_reactor_connector" />
</Column>

**Коннекторы синтеза** соединяют ядро синтеза с тороидальной реакционной камерой. Передают топливо, теплоноситель и энергию.

## Функциональные блоки

**Реакционной камере синтеза** нужны 2 типа функциональных блоков: электромагниты и РЧ-усилители. Эти блоки требуют энергии для работы и теплоносителя для охлаждения. Их нужно ставить в углах сечения тороидальной реакционной камеры.

## Электромагниты

<Row>
  <BlockImage id="basic_electromagnet" />
  <BlockImage id="bscco_electromagnet" />
  <BlockImage id="magnesium_diboride_electromagnet" />
  <BlockImage id="niobium_tin_electromagnet" />
  <BlockImage id="niobium_titanium_electromagnet" />
  <BlockImage id="basic_electromagnet_slope" />
  <BlockImage id="bscco_electromagnet_slope" />
  <BlockImage id="magnesium_diboride_electromagnet_slope" />
  <BlockImage id="niobium_tin_electromagnet_slope" />
  <BlockImage id="niobium_titanium_electromagnet_slope" />
</Row>

**Электромагниты** удерживают плазму в реакционной камере. Чем больше электромагнитное поле, тем выше стабильность плазмы и сечение. И тем меньше теплопотери плазмы.

## РЧ-усилители

<Row>
  <BlockImage id="basic_rf_amplifier" />
  <BlockImage id="bscco_rf_amplifier" />
  <BlockImage id="magnesium_diboride_rf_amplifier" />
  <BlockImage id="niobium_tin_rf_amplifier" />
  <BlockImage id="niobium_titanium_rf_amplifier" />
</Row>

**РЧ-усилители** повышают энергию плазмы радиочастотными волнами (по принципу микроволновки). Иначе говоря, они греют плазму. Остатки повторному разогреву поддаются плохо.

## Простой реактор

Простой проект реактора. Подойдёт для старта с низкотемпературными реакциями.
