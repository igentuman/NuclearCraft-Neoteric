---
navigation:
  title: Processor Upgrades
  parent: machines.md
  icon: upgrade_speed
item_ids:
  - nuclearcraft:upgrade_speed
  - nuclearcraft:upgrade_energy
  - nuclearcraft:upgrade_stack
  - nuclearcraft:upgrade_quantum
---

# Processor Upgrades

Most processors have upgrade slots on the side of the GUI. Stack upgrade items in those slots to change behavior.

Slot 0 takes **Energy Upgrades**. Slot 1 takes speed-type upgrades. Single-slot processors take any speed-type.

## Speed Upgrade

<Column alignItems="center" fullWidth={true}>
  <ItemImage id="upgrade_speed" />
</Column>

**Speed Upgrade** adds `+1 speed` per item. Recipe runs faster, but energy cost grows quadratically with speed. Stack up to 64 per slot.

## Energy Upgrade

<Column alignItems="center" fullWidth={true}>
  <ItemImage id="upgrade_energy" />
</Column>

**Energy Upgrade** reduces energy cost (subtracts quadratically) and grows the internal energy buffer (`x N / 10`). Required to keep heavily speed-boosted processors affordable.

## Stack Upgrade

<Column alignItems="center" fullWidth={true}>
  <ItemImage id="upgrade_stack" />
</Column>

**Stack Upgrade** enables parallel processing: `ceil(count / 4)` recipes per tick, capped at 32. Acts like speed plus parallel I/O. More energy-efficient than raw speed.

## Quantum Upgrade

<Column alignItems="center" fullWidth={true}>
  <ItemImage id="upgrade_quantum" />
</Column>

**Quantum Upgrade** = `x5 speed` and `1 parallel recipe per item`. End-game tier. Energy cost scales hard. Pair with many Energy Upgrades.

## The Math

```
speedMult = upgrades + 1 (x5 for quantum)
parallel = ceil(stack / 4) or quantum count
energyMult = max(speedMult, (speedMult - 1)^2 + speedMult - energyUp^2)
```

Energy upgrades cancel speed cost quadratically. Balance the two.

## GregTech Integration

When GT energy cap is enabled, every `N` Energy Upgrades bumps the processor's accepted EU tier by `+1`. N comes from config `ENERGY_UPGRADES_NEEDED_TO_NEXT_TIER`. Hover the upgrade to see current N.
