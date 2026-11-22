# Beam Diverter

The Beam Diverter routes one incoming particle beam to one of three outputs.
Use it to send an accelerator's beam to different machines, or to turn a beam
through 90 degrees. Only one output is active at a time.

This guide describes the current NeoForge 26.1.2 implementation. Use JEI or EMI
for crafting recipes; energy costs and beam attenuation depend on the modpack's
accelerator configuration.

## Building the multiblock

Build a **5 × 5 × 5** cube with a one-block-thick shell and a **3 × 3 × 3**
interior. Other dimensions are invalid.

| Block | Placement |
| --- | --- |
| Accelerator Casing | All shell edges and corners; also usable on faces. |
| Accelerator Casing Glass | Optional on shell faces, away from edges and corners. |
| Beam Diverter Controller | Exactly one, facing outward on a vertical wall, away from edges and the wall's center. |
| Accelerator Beam Port | Exactly four, at the center of each vertical wall, all facing outward. |
| Accelerator Port | Optional service connection on a shell face, away from edges and the four beam-port positions. |
| Particle Beam | Five inside: the center and its four horizontal neighbours. |
| Electromagnet | Two of the same tier, immediately above and below the center. |
| Electromagnet Yoke | Optional in the remaining 20 interior positions; these may also be air. |

The three interior layers below are viewed from above. The surrounding shell
and beam ports are omitted.

```text
Lower layer     Middle layer    Upper layer
 . . .           . B .           . . .
 . M .           B B B           . M .
 . . .           . B .           . . .

B = Particle Beam
M = Electromagnet (matching tier above and below)
. = air or Electromagnet Yoke
```

Put each wall's beam port in line with the middle-layer cross. The controller
shares a wall with the input port; placing it one block below and one block to
the side of that port gives a valid position. Fill every shell position, then
open the controller and resolve any reported assembly error.

## Input and output directions

Port roles are determined by the controller's orientation. Stand outside facing
the controller:

| Port location | Role | Controller output label | Computer channel |
| --- | --- | --- | --- |
| Same wall as the controller | Input | — | — |
| Wall to your right | Turning output | 0 | 1 |
| Opposite wall | Straight output | 1 | 2 |
| Wall to your left | Turning output | 2 | 3 |

Click the controller's **Output** button to cycle the selected output. The
default is output **0**, a turn. The selected port becomes an output and the
other two output ports become disabled. For routing, use the controller's
selection rather than manually changing individual port modes.

Connect the upstream machine's output to the diverter input using a straight
line of **Particle Beam** blocks. Connect each desired diverter output to a
downstream input in the same way. Connected ports must face each other, the
beam corridor must be uninterrupted, and the destination must be within the
configured beam connection reach. Ordinary item or fluid pipes cannot carry
the beam.

## Power and beam behaviour

Supply FE to the controller, or through a linked Accelerator Port. Each routing
operation costs the configured accelerator base energy requirement plus the
power requirements of both electromagnets. Straight routing also consumes FE.
The controller has a **100,000,000 FE** energy capacity.

The current diverter routing logic does **not require redstone** and does not
run the accelerator coolant or thermal cycle. The matching electromagnets are
still required to assemble the structure and determine its bending field and
power cost.

- **Straight output:** accepts charged, neutral, and massless particles. Routing
  preserves their mean energy inside the diverter.
- **Turning outputs:** require a charged particle with positive mass. Neutral
  or massless particles cannot turn; the output button shows an incompatible
  turn warning. Select the straight output for these beams.
- **Losses:** routing applies the focus loss for a three-block beam distance.
  Turns also reduce mean particle energy according to particle charge, mass,
  energy, and magnetic field. External beam corridors apply additional losses.
- **Storage:** beams are transient and are cleared each tick. An unpowered
  diverter or a blocked destination does not retain the beam for later delivery.

Connect and enable the receiving machine before starting the source. Switching
outputs directs subsequent beams to the new destination; it does not divide a
beam between all three destinations.

## Computer control

ComputerCraft exposes the controller as **`beam_diverter_controller`**. OpenComputers v2
also exposes these controller methods:

| Method | Result |
| --- | --- |
| `selectOutput(channel)` | Selects channel **1-3**; returns whether the selection changed. |
| `getSelectedOutput()` | Returns the selected channel, **1-3**. |
| `isIncompatibleTurn()` | Reports whether the incoming particle cannot use the selected turn. |

Computer channels are **one higher** than the controller's displayed output
labels. Selecting the current channel returns `false`; changing channels can
also be refused while the output buffer contains a beam.

```lua
local diverter = assert(peripheral.find("beam_diverter_controller"))

-- Route straight through: computer channel 2, controller label 1.
diverter.selectOutput(2)
print("Selected channel:", diverter.getSelectedOutput())
```

## Troubleshooting

| Symptom | Check |
| --- | --- |
| Structure will not assemble | Outside dimensions are exactly 5 × 5 × 5; all edges are casing; all four wall centers contain outward-facing beam ports. |
| Interior validation error | Five Particle Beam blocks form the horizontal cross; matching electromagnets sit above and below its center; every other interior position is air or yoke. |
| Beam does not arrive | Feed the port on the controller's wall, supply enough FE, select the connected output, and check the receiving machine and beam corridor. |
| Incompatible turn warning | Use straight output 1 in the controller, or channel 2 through a computer, for neutral or massless particles. |
| Manual port modes keep changing | The controller manages the three output ports. Change the selected output in its interface or with `selectOutput`. |
