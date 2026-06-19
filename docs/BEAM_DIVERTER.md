# Beam Diverter

The Beam Diverter is a small accelerator-family multiblock that **redirects a particle beam**.
It takes a beam in through one port and emits it through another, letting you route a single
accelerator's output to several downstream [Particle Chambers](PARTICLE_CHAMBERS.md) or to a
[Ring Accelerator](ACCELERATORS.md) without rebuilding the beam line.

## Structure

- **Shape:** fixed **5×5×5** cube (`minHeight = maxHeight = 5`, likewise for width and depth).
  Any other size fails validation with `WRONG_PROPORTIONS`.
- **Outer shell:** `accelerator_casing` (corners are mandatory) and `accelerator_casing_glass`,
  identical to the rest of the accelerator family. The `beam_diverter_controller` and the four
  beam ports are mounted in the shell.
- **Beam ports:** exactly **four** `accelerator_beam_port` blocks, one at the **center of each
  vertical wall** (north, south, east, west), i.e. two blocks out from the core on each
  horizontal axis. A wall missing its central beam port fails with `NO_PORT`.

## Core

The inner 3×3×3 around the center holds the diverter optics:

| Position (relative to center) | Block | Purpose |
|---|---|---|
| Center + the 4 horizontal neighbours | `particle_beam` | Beam cross linking all four ports |
| Directly above and below center | `electromagnet` (any tier) | Dipole that bends the beam |
| Everything else in the 3×3×3 | `electromagnet_yoke` | Field return path |

The electromagnet tier sets the diverter's `dipoleStrength`. Efficiency is fixed at **100 %**.

## Beam Ports — input / output rules

Each `accelerator_beam_port` carries a `port_mode` of `input`, `output`, or `disabled`
(cycled with the NuclearCraft Multitool, or set by redstone / a computer).

- **Exactly one** port must be `input` — more or fewer fails with `WRONG_INNER`.
- **At least one** port must be `output`.
- Remaining ports may be `output` or `disabled`.

The diverter pulls the incoming beam from the input port and pushes it out of the **first
active output** port.

## Beam routing and energy cost

- **Straight-through** (output port faces directly opposite the input): the beam only pays the
  normal focus loss of a 3-block segment.
- **90° turn** (output on a perpendicular wall): the beam additionally pays a corner energy
  loss that scales with dipole strength —
  `cornerEnergyLoss(beam, 160 × (ln(dipoleStrength × 10) + 0.2))`. Stronger electromagnets make
  cheaper turns.

The controller consumes Forge Energy each tick it routes a beam (`BASE_POWER` from the
accelerator config); with no stored energy it idles and passes nothing.

## Automation

The Beam Diverter exposes a ComputerCraft peripheral (`nc_beam_diverter`) and an OpenComputers
v2 device. Beyond reading the in-flight particle, the key use is **switching which port is the
output at runtime**, so one accelerator can feed many targets in sequence:

- `getParticleInfo()` — the beam currently inside the diverter
- `getBeamPortsInfo()` — list of every port: `id`, `x`/`y`/`z`, `mode`, and its `particle`
- `setBeamPortMode(id, mode)` — set a port to `"input"`, `"output"`, or `"disabled"`

See [`COMPUTERS.md`](COMPUTERS.md) for the full method list and the OC2 mirror.
