# Ring Accelerator

The Ring Accelerator (also called a Synchrotron Accelerator) takes an existing
particle beam and sets its energy using a ring of bending magnets and RF
amplifiers. It needs Forge Energy (FE), a control signal, and enough cooling to
stay below its components' temperature limit.

Start the beam with a Linear Accelerator and its ion source. The ring has no ion
source slot and accepts only **charged particles with positive mass**. By default,
incoming particles must have at least **5 MeV (5,000 keV)** and must not exceed the
ring's maximum energy for that particle type.

This guide describes the NeoForge 1.21.1 implementation and default settings.
Use JEI or EMI for crafting recipes and your modpack's coolant recipes.

## Building the ring

Build a horizontal square ring with a **5 × 5 cross-section**: each side is five
blocks wide and the whole machine is five blocks high. The middle is an open
square, rather than a filled box.

| Size preset | Allowed outside side length | Height |
| --- | --- | --- |
| 1 (default) | 11-100 blocks | 5 blocks |
| 2 | 60-1,000 blocks | 5 blocks |
| 3 | 600-10,000 blocks | 5 blocks |

Both outside horizontal dimensions must be equal. For outside side length **N**,
the central opening is **(N − 10) × (N − 10)**. A minimum-size ring therefore has
an **11 × 11** footprint and a **1 × 1** central opening.

| Block | Placement and purpose |
| --- | --- |
| Ring Accelerator Controller | Exactly one on an outside vertical wall, facing outward. Keep it off the outer edges. |
| Accelerator Casing | Shell; required wherever two or more outside boundary faces meet, including the outer edges and corners. |
| Accelerator Glass | Optional replacement for ordinary shell faces. |
| Particle Beam | Continuous square loop through the middle of the four ring sides. Also used for connections between machines. |
| Electromagnet | Required matching pair above and below the beam at each of the four turns. Optional dipole and quadrupole sections on straight runs. Use full blocks; slopes are not accepted. |
| RF Amplifier | Complete eight-block sections around the beam on straight runs. Supplies accelerating voltage. |
| Electromagnet Yoke | Fills the turn interiors around the beam and magnets; also usable in unused straight-section positions. |
| Cooler | Optional internal passive cooling, subject to its placement rules. |
| Accelerator Port | Service access for FE, coolant, redstone control, and computers. The same port is used by linear and ring accelerators. |
| Accelerator Beam Port | At least two on outside vertical walls, at the beam's height and facing outward. |

Ion Source Ports do not belong in a ring. Use **Accelerator Ports** for services;
there is no separate Ring Accelerator Port in this version.

### Beam loop and straight sections

Number blocks from **0 to N − 1** along both horizontal axes, and **0 to 4** from
bottom to top. The beam is at **y = 2**, two blocks inward from each outside wall.
Its four turns are at horizontal coordinates `(2, 2)`, `(N − 3, 2)`,
`(2, N − 3)`, and `(N − 3, N − 3)`.

The following are cross-sections looking along a straight beam run:

```text
 RF section       Vertical dipole    Quadrupole
 C C C C C        C C C C C          C C C C C
 C A A A C        C . M . C          C . M . C
 C A B A C        C . B . C          C M B M C
 C A A A C        C . M . C          C . M . C
 C C C C C        C C C C C          C C C C C
```

`C` = shell, `B` = Particle Beam, `A` = RF Amplifier, `M` = Electromagnet.
`.` = air, yoke, or a cooler in an allowed position. Shell face blocks may be
replaced as described in the table above.

- An RF section needs **all eight amplifiers of the same tier**.
- A dipole needs **two matching magnets** on opposite beam faces. A horizontal
  pair is also valid on straight runs.
- A quadrupole needs **four matching magnets** directly above, below, left, and
  right of the beam. Its diagonal positions are filler.
- A straight section may contain only filler around its beam block.
- Do not mix RF amplifiers and magnets in one section, leave an incomplete RF
  section, or put magnets in diagonal positions.

Different sections may use different tiers. Include at least one RF section for
a usable ring: without voltage its calculated maximum particle energy is zero.

### Turns and inside corners

At each beam turn, place a matching Electromagnet immediately above and below the
beam. These four vertical dipoles are mandatory. Fill the other positions in each
turn's **3 × 3 × 3 interior** with Electromagnet Yokes. At beam height, positions
directly along either horizontal axis through the turn may contain Particle Beam
blocks instead, allowing a branch toward a Beam Port.

The three-block-high column at each **inside corner**, where the two inner walls
meet, accepts only air, yokes, or coolers. Do not fill it with casing or glass.
In the coordinate system above, these columns use horizontal coordinates
`(4, 4)`, `(N − 5, 4)`, `(4, N − 5)`, and `(N − 5, N − 5)`, at y = 1-3.

For a simple beam connection, put a Beam Port on an outside wall aligned with a
turn. For example, a port at `(0, 2, 2)` can connect through a Particle Beam block
at `(1, 2, 2)` to the turn at `(2, 2, 2)`; coordinates here are `(x, y, z)`.

## Connecting and starting

1. Complete the ring, install the controller, at least two Beam Ports, and service
   ports as needed. Open the controller and resolve any structure errors.
2. Connect FE to the controller or a linked Accelerator Port. Supply coolant to
   the input tank and extract the heated fluid from the output tank.
3. Connect the upstream accelerator's output to a ring Beam Port set to **Input**.
   Connect a ring **Output** port to the next accelerator or particle chamber.
4. Right-click a Beam Port with a supported wrench to cycle **Input → Output →
   Disabled**. Ports receive initial roles when the structure forms; check the
   modes before starting the upstream beam.
5. Apply redstone to the controller or an Accelerator Port. Strength **15** selects
   full energy; strength **0** stops beam processing.
6. Start the upstream beam and check input energy, maximum energy, focus, and
   temperature in the controller.

External beam connections must be **straight**, with the two ports facing each
other and every intervening block a **Particle Beam** block. Default reach is
**16 blocks from source port to receiving port**, allowing up to 15 intervening
beam blocks. Keep the connection and both machines loaded. Use a Beam Diverter
when a connection needs to turn.

Use one input and one enabled output for a simple setup. Additional Beam Ports
are allowed, but automatic transfer uses only the first enabled output in the
machine's port order; it does not split the beam or retry another output if that
connection fails. Disable other outputs when choosing a route.

**Particles are a live beam, not stored inventory.** Input and undelivered output
are cleared each tick. A stopped, unpowered, overheated, or disconnected ring
does not keep incoming particles for later. Stop the upstream source when the
downstream chain is unavailable.

## Energy and focus

The controller calculates maximum energy for the incoming particle species. It
uses the lower of the magnetic bending limit and the synchrotron radiation limit.
Larger rings and stronger dipoles improve bending capacity; RF voltage raises
the radiation limit. Quadrupoles improve focus.

The selected output energy is:

```text
output energy = max(0, floor(maximum energy × signal / 15) − synchrotron loss)
```

The loss uses the input particle energy, mass, and ring radius. This is an energy
target, so a low control signal can produce a beam with **less energy than its
input**. Input acceptance is checked against the full ring maximum, independent
of the selected signal.

Focus decreases with beam length, particle amount, and charge magnitude, and
increases with quadrupole field. External beam connections cause further focus
loss without changing energy or particle count. Keep connections short and add
quadrupoles when the next machine needs higher focus.

For a ring of outside side length **N**, the implementation uses beam length
`4 × (N − 5)` and effective radius `(N − 4) / 2`. Dipole and quadrupole fields are
summed across their respective sections. Each complete RF section contributes
one amplifier's voltage, although all eight blocks consume power.

## Power and cooling

Each successful processing tick costs **10,000 FE plus the sum of the power costs
of all installed magnets and RF amplifiers**, with default settings. Lowering the
redstone signal does not reduce that cost. Processing also adds heat from the
installed optical sections: each complete dipole, quadrupole, or RF section
contributes its section heat once.

Cooling has two independent parts:

- **Cooler blocks** remove heat passively when their placement rules are met.
  Check their tooltips; only face-adjacent neighbors count. A cooler can be valid
  structure filler yet contribute no cooling if its rule is unmet.
- **Coolant fluid** removes heat while converting input fluid to an output fluid.
  Keep both the input supplied and the output tank drained.

| Default coolant | Input per operation | Output per operation | Heat removed |
| --- | ---: | --- | ---: |
| Water | 100 mB | 100 mB Steam | 100 |
| Technical Water | 100 mB | 100 mB High Pressure Steam | 100 |
| Liquid Nitrogen | 1 mB | 1 mB Nitrogen | 1,000 |
| Liquid Helium | 1 mB | 1 mB Helium | 1,000 |

Cooling runs while the structure is formed, including when the control signal is
zero or processing FE is unavailable. Passive cooling runs before fluid cooling.
The default recipes have no minimum operating temperature.

The heat capacity is **25,000 per block position in the five-wide ring volume**,
including internal air positions. Temperature in kelvin is stored heat divided
by this capacity, rounded down. The temperature limit is the **lowest limit among
the installed magnets and amplifiers**; mixing in a temperature-sensitive tier
can lower the whole machine's limit.

At or above that limit the ring stops processing and resets an overheat cooldown.
With defaults it must cool below the limit and then wait **1,200 ticks (60 seconds
at 20 TPS)** before processing resumes. Keep coolant flowing during this wait.

## Computer control

With ComputerCraft or OpenComputers 2 installed, the accelerator API exposes:

| Method | Purpose |
| --- | --- |
| `getBeamPortsInfo()` | Lists port IDs, positions, and modes. |
| `setBeamPortMode(id, mode)` | Sets a port to `"input"`, `"output"`, or `"disabled"`. IDs start at 1; obtain them from the port list. |
| `setEnergyPercentage(percent)` | Overrides redstone with a percentage rounded to one of the 16 signal levels. Values below 5% stop the machine. |
| `releaseControl()` | Returns control to redstone. |
| `getControlSignal()`, `isComputerControlled()` | Reports the current signal and whether an override is active. |
| `getParticleInfo()`, `getStats()` | Reports the displayed beam and optical statistics. |
| `getTemperature()`, `getMaxTemperature()` | Reports current and limiting temperatures in kelvin. |
| `getCoolingRate()` | Reports heat removed by fluid coolant on the last thermal update; excludes passive coolers. |
| `isOverheated()`, `getOverheatCooldown()` | Reports the overheat lockout and remaining cooldown ticks. |

A computer override takes priority over redstone. Call `releaseControl()` if a
lever appears to have no effect after computer control was used.

## Troubleshooting

| Symptom | Check |
| --- | --- |
| Structure will not form | Equal outside sides, allowed size, five-block height, casing on outer edges, exactly one outward-facing controller, and at least two outward-facing Beam Ports at y = 2. |
| Interior or optics error | Continuous beam loop, complete matching RF/magnet sections, four vertical corner dipoles, corner yokes, and no casing/glass in the three-high inside-corner columns. |
| Input energy too low | Upstream beam must reach at least 5 MeV by default. |
| Input energy too high | Improve the ring's limiting field/voltage/radius, or lower the upstream beam energy. |
| Incompatible particle | The species needs nonzero charge and positive mass. Neutral or massless beams cannot be accelerated here. |
| No beam output | Check control signal, full per-tick FE cost, overheat cooldown, port modes, loaded chunks, facing, and the straight Particle Beam connection. |
| Wrong output route | Disable unwanted Output ports; only one output is selected for automatic transfer. |
| Cooling stops | Refill the input, drain the output, check coolant compatibility, and verify passive cooler placement rules. |
| Downstream machine gets too little focus | Shorten the connections, add quadrupoles, or reduce beam amount. |

## Configuration

Settings are in `config/nuclearcraft/multiblocks.toml`, under `[accelerators]`.
The main controls are `scale_preset`, `ring_minimum_input_energy_kev`,
`base_energy_requirement`, `heat_capacity_per_block`, `overheat_cooldown_ticks`,
`beam_connection_reach`, `beam_attenuation`, and `beam_scaling`.
Coolant conversions use `nuclearcraft:accelerator_coolant` recipes and can be
changed by a modpack. See [Configuration](CONFIGURATION.md) and
[KubeJS](KUBEJS.md) for the surrounding configuration and recipe systems.
