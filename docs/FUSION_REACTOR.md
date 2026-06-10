# Fusion Reactor

The Fusion Reactor is a high-tier toroidal multiblock that fuses fluid fuel pairs into
massive amounts of Forge Energy. It must be ignited (charged) before it produces power, and
overheating it ends the world for everything around it.

## Multiblock Structure

The reactor is a horizontal ring laid out around a central controller (`fusion_core`).

- **Shape:** torus on the horizontal plane
- **Height:** fixed at **3 blocks**
- **Ring size (`length`):** 1..`MAX_SIZE / 2 + 1`, determined by counting the four
  `fusion_reactor_connector` arms that extend outward from the core. **All four arms must
  match length** for the ring to form.
- The 3×3 cage around the core is auto-filled with `fusion_reactor_core_proxy` blocks
  when the core is placed; do not place those by hand.

### Block list

| Block | Role |
|---|---|
| `fusion_core` | Controller / charge handler / fluid I/O |
| `fusion_reactor_connector` | Four axial arms from the core that set ring `length` |
| `fusion_reactor_casing` / `fusion_reactor_casing_glass` | Outer ring walls (inner, outer, top, bottom) |
| `electromagnet` blocks (multiple tiers) | Generate confinement field, set `maxTemperature` |
| `rf_amplifier` blocks (multiple tiers) | Pump RF into the plasma |
| Inner ring volume | Must be air / plasma |

Each magnet provides `strength`, `efficiency`, `power`, `maxTemperature`. Each RF amplifier
provides `amplification`, `power`, `efficiency`, `maxTemperature`. Mixing higher-tier and
lower-tier parts pulls the effective stats toward the weakest.

## Fuel

Fuel is supplied as **two fluids**, one per input tank. Recipes are shipped under
`data/nuclearcraft/recipes/fusion_core/`. Supported pairs include:

- Deuterium × {Tritium, Helium-3, Hydrogen, Lithium-6, Lithium-7, Quantite}
- Helium-3 × {Lithium-6, Lithium-7, Quantite}
- Hydrogen × {Helium-3, Lithium-6, Lithium-7, Quantite, Tritium}
- Lithium-6 × {Quantite, Xenorium-298}
- Lithium-7 × {Quantite}
- Tritium × {Helium-3, Lithium-6, Lithium-7, Quantite}

## Ignition

1. Power the core's internal energy buffer (`2,048,000,000` FE capacity).
2. Each tick the core drains `(rfAmplifiersPower + magnetsPower) / 2` RF into `chargeAmount`.
3. Charge complete when `chargeAmount >= (rfAmplifiersPower + magnetsPower) × 7`.
4. `functionalBlocksCharge` reaches 100% - the reactor is `isReady()`.
5. Plasma is then heated by `amplifyPlasma()` (RF amp × size × ratios × `RF_AMPLIFICATION_MULTIPLIER`).
6. **Energy production starts only when `plasmaTemperature ≥ 1,000,000`.**

## Output

Energy per tick:

```
energyPerTick =
    recipe.energy
  × min(plasma/optimal, optimal/plasma)         // efficiency vs optimal temperature
  × calculateEfficiency()
  × size
  × log((size + 1)^8) / 8
  × PLASMA_TO_ENERGY_CONVERTION
  × getControlPartsEfficiency()
```

The reactor produces FE only. There is no separate steam-output mode.

## Coolant Recipes

`FusionCoolantRecipe` entries convert an input cooling fluid into a hot variant, deposited
into output tank 7. This is how you turn fusion heat into something a turbine can use -
pipe the hot coolant out and into a Turbine running the matching recipe.

`rarityModifier` on the recipe controls how much heat each operation removes.

## Heat & Meltdown

- Reactor heat accumulates from plasma each tick (`heatLossExchange`).
- Cooling: coolant recipes + larger ring (more passive losses).
- Meltdown trigger: `reactorHeat > maxHeat` **and** `plasma > 10000`, where
  `maxHeat = max(min RF amp max temp, max magnet max temp) × 2`.
- Explosion radius is set by `FusionConfig.EXPLOSION_RADIUS`.

## Scaling

Larger rings:

- Multiply output (`size × log((size+1)^8)/8`)
- Increase passive plasma cooling
- Raise minimum required magnetic field
- Need more total magnet/amp power to charge

A bigger ring is more powerful but harder to ignite and keep stable.

## Automation

The formed reactor exposes a ComputerCraft / OC2 peripheral with start/stop, RF amplification
control, plasma-stability readout, and energy stats. See [`COMPUTERS.md`](COMPUTERS.md).
