---
navigation:
  title: Общее
  parent: multiblocks/fission.md
  icon: fission_reactor_controller
item_ids:
  - nuclearcraft:fission_reactor_controller
  - nuclearcraft:fission_reactor_casing
  - nuclearcraft:fission_reactor_glass
  - nuclearcraft:fission_reactor_port
  - nuclearcraft:fission_reactor_solid_fuel_cell
  - nuclearcraft:fission_reactor_irradiation_chamber
  - nuclearcraft:fission_reactor_pile-driver_irradiation_chamber
  - nuclearcraft:irradiator
  - nuclearcraft:graphite_block
  - nuclearcraft:beryllium_block
  - nuclearcraft:lapis_heat_sink
  - nuclearcraft:aluminum_heat_sink
  - nuclearcraft:arsenic_heat_sink
  - nuclearcraft:boron_heat_sink
  - nuclearcraft:carobbiite_heat_sink
  - nuclearcraft:copper_heat_sink
  - nuclearcraft:cryotheum_heat_sink
  - nuclearcraft:diamond_heat_sink
  - nuclearcraft:emerald_heat_sink
  - nuclearcraft:end_stone_heat_sink
  - nuclearcraft:enderium_heat_sink
  - nuclearcraft:fluorite_heat_sink
  - nuclearcraft:glowstone_heat_sink
  - nuclearcraft:gold_heat_sink
  - nuclearcraft:iron_heat_sink
  - nuclearcraft:lead_heat_sink
  - nuclearcraft:liquid_helium_heat_sink
  - nuclearcraft:liquid_nitrogen_heat_sink
  - nuclearcraft:lithium_heat_sink
  - nuclearcraft:magnesium_heat_sink
  - nuclearcraft:manganese_heat_sink
  - nuclearcraft:nether_brick_heat_sink
  - nuclearcraft:netherite_heat_sink
  - nuclearcraft:obsidian_heat_sink
  - nuclearcraft:prismarine_heat_sink
  - nuclearcraft:purpur_heat_sink
  - nuclearcraft:quartz_heat_sink
  - nuclearcraft:redstone_heat_sink
  - nuclearcraft:silver_heat_sink
  - nuclearcraft:slime_heat_sink
  - nuclearcraft:tin_heat_sink
  - nuclearcraft:villiaumite_heat_sink
  - nuclearcraft:water_heat_sink
---

# Общее

<GameScene zoom={3}>
  <ImportStructure src="/structures/fission_reactor.nbt" />
</GameScene>

**Реакторы деления** вырабатывают тепло за счёт самоподдерживающейся цепной реакции деления топлива. В итоге это тепло преобразуется в электричество. Способ выработки можно переключать между прямым и кипящим режимом. (Чикагская поленница-1 обошлась сквош-кортом; у вас целый чанк.)

**Реактор деления** работает в двух режимах: энергетический и кипящий. В энергетическом режиме реактор сразу вырабатывает энергию. В кипящем - использует выделяемое тепло для кипячения теплоносителя. Подача и отвод теплоносителя осуществляется через **Порт реактора**.

## Обшивка реактора

<Row>
  <BlockImage id="fission_reactor_casing" />
  <BlockImage id="fission_reactor_glass" />
</Row>

Внутренние компоненты реактора размещаются внутри прямоугольного параллелепипеда. Рёбра конструкции должны быть сложены из **Обшивки реактора**, а стены - из **Реакторного стекла**, обшивки или их сочетания.

## Контроллеры реактора

<Column alignItems="center" fullWidth={true}>
  <BlockImage id="fission_reactor_controller" />
</Column>

Без контроллера многоблочная структура реактора не соберётся. Его интерфейс отображает сводные сведения о работе реактора - в том числе усреднённые показатели по компонентам.

## Порт реактора

<Column alignItems="center" fullWidth={true}>
  <BlockImage id="fission_reactor_port" />
</Column>

Порт реактора служит для передачи предметов, жидкостей и энергии в реактор и обратно. Каждый порт можно настроить на ввод или вывод по любому из этих типов. Также поддерживается управление редстоуном и взаимодействие с компьютерами.

## Топливный элемент

<Column alignItems="center" fullWidth={true}>
  <BlockImage id="fission_reactor_solid_fuel_cell" />
</Column>

Топливные элементы преобразуют тепло топлива в энергию - напрямую либо через кипящий режим. Каждый дополнительный элемент увеличивает суммарный расход топлива.

## Модераторы

<Row>
  <BlockImage id="graphite_block" />
  <BlockImage id="beryllium_block" />
</Row>

**Модераторы** замедляют высокоэнергетические нейтроны, испускаемые [Топливными элементами](general.md#fuel_cell), превращая их в тепловые - те, что вызывают новые деления в соседних топливных компонентах. Модераторы должны располагаться вплотную к топливным элементам. Уровень модерации регулируется сигналом редстоуна, подаваемым на **Порт реактора**.

## Расположение модераторов

Дополнительный бонус к эффективности можно получить, разместив несколько топливных элементов вокруг одного блока модератора. От такой конфигурации зависят как прирост эффективности, так и тепловой бонус.

## Теплоотводы

<Row>
  <BlockImage id="lapis_heat_sink" />
  <BlockImage id="aluminum_heat_sink" />
  <BlockImage id="arsenic_heat_sink" />
  <BlockImage id="boron_heat_sink" />
  <BlockImage id="carobbiite_heat_sink" />
  <BlockImage id="copper_heat_sink" />
  <BlockImage id="cryotheum_heat_sink" />
  <BlockImage id="diamond_heat_sink" />
  <BlockImage id="emerald_heat_sink" />
  <BlockImage id="end_stone_heat_sink" />
  <BlockImage id="enderium_heat_sink" />
  <BlockImage id="fluorite_heat_sink" />
  <BlockImage id="glowstone_heat_sink" />
  <BlockImage id="gold_heat_sink" />
  <BlockImage id="iron_heat_sink" />
  <BlockImage id="lead_heat_sink" />
  <BlockImage id="liquid_helium_heat_sink" />
  <BlockImage id="liquid_nitrogen_heat_sink" />
  <BlockImage id="lithium_heat_sink" />
  <BlockImage id="magnesium_heat_sink" />
  <BlockImage id="manganese_heat_sink" />
  <BlockImage id="nether_brick_heat_sink" />
  <BlockImage id="netherite_heat_sink" />
  <BlockImage id="obsidian_heat_sink" />
  <BlockImage id="prismarine_heat_sink" />
  <BlockImage id="purpur_heat_sink" />
  <BlockImage id="quartz_heat_sink" />
  <BlockImage id="redstone_heat_sink" />
  <BlockImage id="silver_heat_sink" />
  <BlockImage id="slime_heat_sink" />
  <BlockImage id="tin_heat_sink" />
  <BlockImage id="villiaumite_heat_sink" />
  <BlockImage id="water_heat_sink" />
</Row>

**Теплоотводы** применяются при проектировании реактора для балансировки **сетевого теплового потока**. Для полностью стабильного реактора инженер должен стремиться к значению 0 HU/t. У каждого теплоотвода - свои правила размещения.

Теплоотводы отбирают тепло у реактора. Когда топливный элемент активен, он выделяет тепло, равное базовому тепловыделению, умноженному на **тепловой множитель**. Множитель определяется числом линий модераторов: одна линия - 100% тепловой эффективности, две - 200%.

## Проверка теплоотводов

Если правила размещения требуют соседства с непроизводящим блоком - например, с другим теплоотводом, - то и тот соседний теплоотвод обязан удовлетворять собственным правилам. В конечном счёте цепочка должна замыкаться на топливный элемент.

## Облучатели

<Row>
  <BlockImage id="fission_reactor_irradiation_chamber" />
  <BlockImage id="irradiator" />
</Row>

Установленные в конце [линии модераторов](general.md#moderator), **Облучатели** используют поток нейтронов для преобразования предметов в облучательной камере. Скорость обработки зависит от количества линий модераторов в реакторе.

## Линия облучения

К одной облучательной камере можно подвести до 6 линий облучения. Каждая дополнительная линия пропорционально ускоряет обработку.

## Камера облучения с забивным копром

<Column alignItems="center" fullWidth={true}>
  <BlockImage id="fission_reactor_pile-driver_irradiation_chamber" />
</Column>

Улучшенная **облучательная камера**, работающая в **5 раз** быстрее обычной. Ставится прямо вместо стандартной камеры в конце любой линии облучения: правила размещения те же, число линий то же - а пропускная способность в пять раз выше. (План по выпуску изотопов сам себя не выполнит.)

## Простой реактор

Это простой проект реактора. У него 1 топливный элемент и 1 линия облучения. Хорошая точка входа в отрасль. Оптимальное топливо - HEU-235.

## Соображения при проектировании

При проектировании реактора рекомендуется пользоваться [планировщиком реакторов](https://github.com/ThizThizzyDizzy/nc-reactor-generator/releases). Он подскажет по правилам размещения, тепловому балансу и прогнозируемой выработке.
