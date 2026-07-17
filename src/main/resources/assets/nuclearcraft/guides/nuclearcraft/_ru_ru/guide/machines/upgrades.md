---
navigation:
  title: Улучшения процессоров
  parent: machines.md
  icon: upgrade_speed
item_ids:
  - nuclearcraft:upgrade_speed
  - nuclearcraft:upgrade_energy
  - nuclearcraft:upgrade_stack
  - nuclearcraft:upgrade_quantum
---

# Улучшения процессоров

У большинства процессоров на стороне интерфейса есть слоты улучшений. Складывайте улучшения в эти слоты, чтобы изменить поведение.

Слот 0 принимает **улучшения энергии**. Слот 1 - улучшения скоростного типа. Однослотовые процессоры принимают любое скоростное.

## Улучшение скорости

<Column alignItems="center" fullWidth={true}>
  <ItemImage id="upgrade_speed" />
</Column>

**Улучшение скорости** даёт `+1 скорости` за предмет. Рецепт идёт быстрее, но расход энергии растёт квадратично от скорости. До 64 штук в слот.

## Улучшение энергии

<Column alignItems="center" fullWidth={true}>
  <ItemImage id="upgrade_energy" />
</Column>

**Улучшение энергии** снижает расход энергии (квадратично) и увеличивает внутренний буфер энергии (`x N / 10`). Нужно, чтобы держать в разумных пределах процессоры с сильным разгоном.

## Улучшение пакетной обработки

<Column alignItems="center" fullWidth={true}>
  <ItemImage id="upgrade_stack" />
</Column>

**Улучшение пакетной обработки** включает параллельные рецепты: `ceil(count / 4)` рецептов за тик, не более 32. Работает как скорость + параллельный ввод/вывод. Энергоэффективнее простого ускорения.

## Квантовое улучшение

<Column alignItems="center" fullWidth={true}>
  <ItemImage id="upgrade_quantum" />
</Column>

**Квантовое улучшение** = `x5 скорости` и `1 параллельный рецепт на предмет`. Конечный уровень. Энергозатраты растут жёстко. Сочетайте с большим числом улучшений энергии.

## Математика

```
speedMult = upgrades + 1 (x5 for quantum)
parallel = ceil(stack / 4) or quantum count
energyMult = max(speedMult, (speedMult - 1)^2 + speedMult - energyUp^2)
```

Улучшения энергии квадратично компенсируют расход. Балансируйте оба типа.

## Интеграция с GregTech

При включённом ограничении по энергии GT каждые `N` улучшений энергии повышают принимаемый процессором уровень EU на `+1`. N задаётся в конфиге `ENERGY_UPGRADES_NEEDED_TO_NEXT_TIER`. Наведите на улучшение, чтобы увидеть текущее N.
