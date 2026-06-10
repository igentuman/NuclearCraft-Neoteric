# Kugelblitz Chamber

The Kugelblitz is an endgame multiblock that traps a small artificial black hole and
extracts Hawking-style evaporation energy. It produces enormous amounts of Forge Energy
and hosts hidden transmutation recipes — but a poorly fed black hole vaporizes itself, and
an overfed one detonates.

## Multiblock Structure

A **fixed 9×9×9 cube**. The shell is built from specialized parts; the interior must be
hollow at the center.

### Required blocks

| Block | Role |
|---|---|
| `chamber_terminal` | Controller; one only |
| `chamber_port` | Optional I/O (fluid/energy) |
| `quantum_transformer` | Wall block; primary structural part |
| `quantum_flux_regulator` | Wall block; affects evaporation rate |
| `event_horizon_stabilizer` | Wall block; affects stability |
| `photon_concentrator` | One on each of the 6 face centers; receives the ignition pulse |
| `neutronium_frame` | Frame/corner blocks of the outer 7×7 face frames |
| `EXPL` (laser emitter) | Dedicated laser source block for ignition pulses |
| `black_hole` | Auto-placed by the terminal at the geometric center on ignition |

### Layout rules

- Six **identical 5×5 walls**, each centered on one face and surrounded by a 7×7 frame of
  `neutronium_frame` / `chamber_terminal` / `chamber_port`.
- Each face must hold exactly one `photon_concentrator` at its center.
- Inner volume must be hollow: a sphere of radius 4 of air around the center.
- The center block becomes the `black_hole` block entity on ignition.

## Ignition

1. Build the chamber.
2. Provide `subliquid_matter` to the terminal (input slot 0).
3. Fire **simultaneous high-RF laser pulses** into all 6 photon concentrators
   (e.g. with `EXPL` emitters or Mekanism laser amplifiers).
4. When all 6 pulses register `gotLaserBurst`, the terminal spawns the black hole and
   seeds it with up to `~4,000,000` mass per burst.

## Mass & Stability

- **Mass range:** `MIN_MASS = 1×10⁸` to `MAX_MASS = 1×10¹⁰`.
- Feeding adds `fluid_amount × 10` mass per tick.
- Mass below `MIN_MASS` → black hole evaporates and the structure goes inert.
- Mass above `MAX_MASS` → meltdown (TNT-style explosion).
- **Stability** (0..100) decays at extremes, recovers in the mid-range, and is buffered by
  `event_horizon_stabilizer` count.

## Power Output

```
energyPerTick =
    massRatio
  × 5000
  × log(energyConvertionRate × log(fluxRegulators × 4) + 1)
  × KugelblitzConfig multipliers
```

Internal buffer: `100,000,000 FE`. GregTech energy tier is configurable.

## Hidden Transmutation Recipes (`kugelblitz_chamber`)

The terminal exposes a **quantum frequency** slider (0..15). The mod seeds a hidden
frequency per ingredient that the player has to guess; if the slider matches when an item
is inserted, a curated item is produced. Pool spans vanilla/forge tags (ingots, gems,
raw materials, `xenorium_298`, `dragon_breath`, etc.). See `KugelblitzRecipes.java`.

## Configuration

`KugelblitzConfig`:

- `energy_convertion_rate`
- explosion radius for meltdown
- energy buffer / tier overrides

## Automation

ComputerCraft / OC2 peripheral (`nc_kugelblitz`) exposes:

- `getEvaporationRate`, `getFeedingRate`, `getBlackholeMass`, `getBlackholeStability`
- `getQuantumFrequency` / `setQuantumFrequency(0..15)`
- `getFluxRegulators`, `getTransformers`, `getStabilizers`
- `getTransformationEnergyRate` / `setTransformationEnergyRate(0..100)`

See [`COMPUTERS.md`](COMPUTERS.md).
