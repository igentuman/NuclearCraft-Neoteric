# Accelerators

Particle accelerators take a source item or fluid, spin it up into a high-energy particle
beam, and deliver that beam to a [Target Chamber](TARGET_CHAMBER.md) for nuclear/exotic
reactions. NuclearCraft-Neoteric provides two accelerator variants - a linear collider and
a toroidal (ring) collider - that share a single beam-line architecture.

## Variants

### Linear Accelerator

- **Cross-section:** fixed **5×5** (height = width = 5).
- **Length:** 6..100 (scale 1), up to 10000 at higher scales.
- **Beam-line endpoints:** one end must be `accelerator_ion_source_port`, the other an
  `accelerator_beam_port`. (Two beam ports is also valid for chained setups.)

### Toroidal (Ring) Accelerator

- **Shape:** hollow square torus. Outer 5-thick ring around a hollow inner square offset 4
  from the walls.
- **Beam-line:** four straight segments validated independently - particles loop around
  and accumulate energy.
- **No ion-source port** - beam ports are embedded in the outer ring; the beam is fed
  externally from a linear accelerator and circulated.

## Outer Shell

- `accelerator_casing` (corners required)
- `accelerator_casing_glass`
- `accelerator_*_port` (ion source / beam)
- All shell blocks share tag `accelerator_casing`.

## Beam-Line Cross-Section

Every 1-block slice along the beam runs 8 perimeter positions around a central
`particle_beam` block. Each slice must be one of:

| Slice type | Composition | Function |
|---|---|---|
| **Quadrupole magnet** | 4 `electromagnet` blocks (symmetric around the beam) | Focus |
| **Dipole magnet** | 2 `electromagnet` blocks | Bend (required in rings) |
| **RF amplifier** | 8 `rf_amplifier` blocks | Acceleration (`acceleratingVoltage`) |
| **Cooler** | `cooler` blocks (adjacency-validated) | Passive heat removal |

Mixing magnets and amplifiers in the same slice is rejected.

Each electromagnet contributes `strength`, `maxTemperature`, `heatRate`, `power`, `efficiency`.
Each RF amplifier contributes `amplification`, `maxTemperature`, `power`, `efficiency`.

```
focus = quadStrength + dipoleStrength / 2
maxHeat = (beamLength + 5) × 10000
```

## Cooler Placement

`CoolerDef` placement rules use the same operator language as fission heat sinks:
`>`, `<`, `=`, `-`, `^`. Coolers that fail their rule are inactive.

## Particle Sources

`accelerator_ion_source_port` consumes an item or fluid mapped in `ParticleSources`. Examples:

| Input | Produces |
|---|---|
| `source_calcium_48` (item) | calcium_48_ion |
| `tungsten_filament` (item) | electron |
| `source_iridium_192` (item) | positron |
| `antideuterium` (item) | antideuteron |
| `hydrogen` (fluid) | proton |
| `deuterium` (fluid) | deuteron |
| `tritium` (fluid) | triton |
| `helium` (fluid) | alpha |
| `diborane` (fluid) | boron_ion |

About **80 particle species** are defined in `Particles.java`: quarks/antiquarks, leptons
(electron, positron, muons, taus, neutrinos), bosons (photon, gluon, W±, Z, Higgs),
nucleons and antinucleons, nuclei (deuteron, triton, helion, alpha + antis), pions, kaons,
etas, sigmas, deltas, glueballs, etc.

Custom particle sources can be registered from KubeJS -
see [`KUBEJS_SUPPORT.md`](KUBEJS_SUPPORT.md).

## Output

The accelerator emits a `ParticleStack` (particle type, amount, energy, focus) through an
`accelerator_beam_port`. The port must be directly adjacent to a
`target_chamber_beam_port` on the receiving multiblock.

## Configuration

`AcceleratorConfig`:

- `min_size` / `max_size`
- Per-particle source mappings (overridable via KubeJS)
- Power / heat / efficiency multipliers

## Automation

Both variants expose a ComputerCraft / OC2 peripheral (`nc_accelerator`). Methods include:

- `isFormed`, `hasParticle`, `isAcceleratorOn`
- `getEnergyStored`, `getTemperature`, `getMaxTemperature`, `getHeatRate`
- `getHeatBufferInfo`, `getCoolingInfo`, `getStats`, `getParticleInfo`

See [`COMPUTERS.md`](COMPUTERS.md).
