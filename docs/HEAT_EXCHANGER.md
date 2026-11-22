# Heat Exchanger

The Heat Exchanger transfers heat between two fluid loops. The hot loop cools
fluids and stores their heat in a shared buffer; the cold loop spends that heat
to turn Water or Technical Water into steam. It consumes Forge Energy (FE) and
requires a redstone signal at the controller to process fluids.

Use JEI or EMI for crafting recipes and the heat exchanger recipes available in
your modpack. Values below describe the default configuration and recipes in the
NeoForge 26.1.2 port.

## Building the multiblock

Build a closed rectangular box with **outside dimensions from 5 to 15 blocks on
each axis**. It does not have to be a cube. The shell is one block thick.

| Block | Placement and purpose |
| --- | --- |
| Heat Exchanger Controller | Exactly one in the shell. Opens the interface and receives the operating redstone signal. Place it in the middle of a face for a simple layout. |
| Heat Exchanger Casing | Shell, including the corners. |
| Heat Exchanger | Interior. Each block adds processing throughput, heat capacity, fluid storage, and operating energy cost. |
| Heat Exchanger Hot Coolant Port | Shell. Accepts hot-loop input and exposes its cooled output. |
| Heat Exchanger Cold Coolant Port | Shell. Accepts cold-loop input and exposes its heated output. |
| Heat Exchanger Radiator | Optional shell block. Discards stored heat when radiators are enabled. |

Use casing at all eight corners and fill every other shell position with casing,
ports, radiators, or the controller. Leave unused interior positions as air;
only **Heat Exchanger** blocks belong inside. No glass shell blocks, internal
pipes, or connections between the interior blocks are required.

For a starter build:

1. Build a **5 × 5 × 5** casing shell, leaving a **3 × 3 × 3** interior.
2. Replace three non-corner shell blocks with one controller, one hot port, and
   one cold port.
3. Put at least **one Heat Exchanger block inside**. The other interior positions
   can remain air. A shell with no interior exchanger blocks cannot process fluids.
4. Open the controller and check that the structure is assembled. Resolve any
   reported validation problem before supplying fluids.
5. Connect FE, fluid input and extraction lines, and a lever or other redstone
   source that powers the controller.

Adding more ports is useful for separate input and extraction lines. All hot
ports share the same hot-loop tanks; all cold ports share the cold-loop tanks.

## Connecting the fluid loops

The controller stores four separate tanks:

| Loop | Input tank | Output tank | External access |
| --- | --- | --- | --- |
| Hot | Hot coolant or spent steam | Cooled coolant or condensed water | Hot Coolant Port |
| Cold | Water or Technical Water | Steam or High Pressure Steam | Cold Coolant Port |

Each port can accept its loop's input and allow extraction of its loop's output.
Use fluid transport configured to insert the input fluid and extract the product;
the ports do not automatically pump fluids into adjacent machines. The input
tanks accept fluids belonging to recipes for their respective loops.

Supply FE to the controller, or through a linked coolant port. Apply the
**redstone signal to the controller itself**. Fluid conversion needs enough
stored FE to cover the full operating cost for that tick.

A typical coolant and steam arrangement is:

```text
Reactor hot coolant → Hot Coolant Port → cooled coolant back to reactor
Water supply       → Cold Coolant Port → Steam to turbine
```

The two loops exchange heat through the buffer, so they can run independently.
The hot loop can charge an empty buffer without cold input. The cold loop can
use previously stored heat while the hot input is empty.

## Default fluid recipes

Every default recipe consumes **10 mB** and produces **10 mB** per operation.
Positive heat is added to the buffer; negative heat is taken from it.

| Loop | Input | Output | Heat per operation |
| --- | --- | --- | ---: |
| Hot | Hot Helium | Helium | +400 H |
| Hot | Exhaust Steam | Water | +100 H |
| Hot | Low Quality Steam | Technical Water | +100 H |
| Hot | Hot Molten FLiBe Salt (`flibe_hot_molten_salt`) | Molten FLiBe Salt (`flibe_molten_salt`) | +1,800 H |
| Cold | Water | Steam | −200 H |
| Cold | Technical Water | High Pressure Steam | −300 H |

For example, cooling **10 mB of Hot Helium** supplies enough heat to turn
**20 mB of Water into Steam**. Cooling **10 mB of Hot Molten FLiBe Salt** supplies
enough heat for **90 mB of Water**, or **60 mB of Technical Water**.

Condensing Exhaust Steam and Low Quality Steam **adds heat**. Feed them into
the hot port. If you only want to condense spent steam, either spend the recovered
heat on the cold loop or enable radiators to dispose of it.

## Throughput, storage, and energy

Let **N** be the number of interior Heat Exchanger blocks. With default settings:

| Property | Capacity or rate |
| --- | --- |
| Maximum input processing rate | **N × 50,000 mB/t for each loop** |
| Operating energy cost | **N × 200 FE/t**, charged once when either or both loops process fluid |
| Shared energy buffer | **1,000,000 FE** |
| Shared heat buffer | **N × 10,000,000 H** |
| Capacity of each of the four fluid tanks | **(N + 1) × 100,000 mB** |

One interior block therefore gives each loop up to **50,000 mB/t**, four
**200,000 mB** tanks, and **10,000,000 H** of heat storage at **200 FE/t** while
processing. Increasing the outer dimensions alone does not increase these rates;
add interior Heat Exchanger blocks to increase N.

The processing rate is an upper limit. Each loop also needs enough input fluid,
output space, and available heat or heat-buffer space. Recipes run in whole
operations: the default recipes need at least 10 mB of input and room for 10 mB
of output. For custom recipes, the operation limit per tick is
`floor(N × throughput_per_block / recipe_input_amount)`.

The hot loop runs before the cold loop each tick, so newly recovered heat can be
used immediately. A full heat buffer stops the hot loop until heat is removed;
an empty buffer stops the cold loop until enough heat is available. The operating
energy cost is based on all installed interior blocks, even when fluid supply
only allows a few operations. If neither loop processes fluid, no processing FE
is consumed.

## Radiators and controller display

Radiators start **disabled**. Once the structure is assembled, use the radiator
button in the controller screen to enable or disable them.

Each enabled radiator removes up to **1,000,000 H/t** from the shared buffer.
Cooling happens before fluid processing and continues while the structure is
formed, including when redstone is off or no FE is available. Radiators consume
no FE and do not produce fluid or energy.

Enable radiators when excess heat is blocking coolant recovery or condensation.
Keep them disabled when you want to preserve heat for steam production. Too much
radiator cooling can leave the cold loop without enough heat to run.

The controller displays the heat buffer, interior block count, and **hot/cold
cycle operations per tick**. The cycle counters count recipes, not mB; multiply
by 10 for the default recipes' fluid rate. The assembled-status tooltip also
shows the radiator count.

## Troubleshooting

| Symptom | What to check |
| --- | --- |
| Structure will not assemble | All outside dimensions are within the configured limits; the shell is closed; there is exactly one controller; corners use casing; the interior contains only air and Heat Exchanger blocks. |
| Both cycle counters stay at zero | The structure is assembled, at least one interior exchanger block is installed, the controller has redstone power, and stored FE covers N × 200 FE for a tick. Check input fluids and output storage too. |
| Hot loop stops with fluid still available | The heat buffer may be full, the cooled-output tank may be full, or the fluid may lack a hot-loop recipe. Spend heat on the cold loop or enable radiators, and extract the output. |
| Cold loop stops | Supply enough heat and the correct input fluid, extract steam, and check whether radiators are removing the heat you need. |
| Port refuses a fluid | Use the correct loop's port and check JEI/EMI for a matching recipe. A tank already containing another fluid must be emptied first. |
| Processing is slower than expected | Check fluid transport rates, available heat, and output space. More exchanger blocks increase the maximum rate but also increase the FE required per active tick. |

## Configuration and automation

Settings are in the `[heat_exchanger]` section of
`config/nuclearcraft/multiblocks.toml`:

| Setting | Default | Effect |
| --- | ---: | --- |
| `min_size` / `max_size` | 5 / 15 | Minimum and maximum outside length on each axis. |
| `energy_per_block` | 200 | FE per interior block per processing tick. |
| `throughput_per_block` | 50,000.0 | Input amount budget per interior block per tick, independently for each loop. |
| `energy_capacity` | 1,000,000 | Controller energy storage in FE. |
| `fluid_capacity_per_block` | 100,000 | Per-tank capacity multiplier; actual capacity is (N + 1) times this value. |
| `radiator_cooling` | 1,000,000 | Heat removed per enabled radiator per tick. |
| `heat_capacity_per_block` | 10,000,000 | Heat storage per interior block. |

See [Mod configuration](CONFIGURATION.md) for file locations and applying changes.
Custom `nuclearcraft:heat_exchanger` recipes use `input_fluid`, `output_fluid`,
and integer `heat` fields; see [KubeJS events and recipes](KUBEJS.md). The heat
sign selects the loop: positive for hot, negative for cold. A zero-heat recipe
belongs to neither loop and will not run.

The ComputerCraft/OpenComputers integration exposes `getStatistics()`,
`isRadiatorsEnabled()`, `enableRadiators()`, and `disableRadiators()`.
Statistics include `heat`, `maxHeat`, `hotCycleOps`, `coldCycleOps`,
`heatExchangers`, `radiators`, and `radiatorsEnabled`.
