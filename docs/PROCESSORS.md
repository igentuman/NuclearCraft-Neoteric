# Processors

Processors turn items and fluids into materials, components and fuels. Most are
single-block machines powered by Forge Energy (FE/RF), with item slots, fluid
tanks and upgrade slots appropriate to their recipes. This guide covers normal
recipe processors such as the Manufactory, Alloy Smelter, Melter and Electrolyzer.
Special machines such as the Nuclear Furnace, Pump and Ore Leacher have their
own operating requirements and may offer different upgrades.

## Getting started

1. Look up the desired output in JEI or EMI and select its processor recipe.
   Check the machine, ingredient counts, fluid amounts and all outputs.
2. Place that processor and connect an FE supply.
3. Right-click the machine to open its screen. Put the required items in the
   input slots and supply any required fluids to its input tanks.
4. Leave space in every required output slot and tank, including space for
   byproducts.
5. Leave redstone control on **Redstone: Active always** to run automatically.
   The progress bar advances while the machine has a recipe and enough power.
6. Collect the outputs manually or connect storage and configure automatic
   transfer as described below.

Recipes can require only items, only fluids, or both. For example, a Manufactory
has one item input and one item output; a Melter takes an item and outputs fluid;
an Electrolyzer takes fluid and has four fluid outputs. Use the recipe viewer
for the recipes available in your modpack.

## Using the screen

Item inputs and outputs appear on either side of the progress bar. Fluid tanks
occupy similar positions and display their contents; hover over a tank to check
its fluid, amount and capacity. Hover over the energy bar to inspect power.

To transfer fluid manually, pick up a compatible fluid container onto your cursor
and click the tank. Use filled containers to supply fluid and empty containers to
collect it. Pipes can fill and drain tanks continuously.

**Shift-left-click a tank with an empty cursor to void its contents.** This
discards the fluid and can clear a tank containing the wrong ingredient.

The **Side Configuration** button opens the slot configuration screens. The
redstone button switches between two modes:

| Mode | Behavior |
| --- | --- |
| Redstone: Active always | Runs without a redstone signal. |
| Redstone: Active on signal | Runs only while the machine receives a redstone signal. |

## Upgrades

Most normal processors have an **energy upgrade slot** and a **speed upgrade
slot**. Their empty slots show an example upgrade icon. Insert upgrades into
these dedicated slots rather than the recipe inputs. Upgrades remain installed
and are not consumed by processing; the number of items in the stack determines
their strength. Hover over an upgrade stack to see its bonuses.

Speed, Stack and Quantum Upgrades all use the **same speed slot**. Choose one
type for that slot; their effects cannot be combined in a single machine.
Energy Upgrades use the separate energy slot and work alongside any of them.
Only machines with the corresponding upgrade slots accept these upgrades.

In the table below, **n** is the number of installed upgrades of that type.

| Upgrade | Effect |
| --- | --- |
| Speed Upgrade | Processing speed becomes **n + 1** times the base speed. Processes one recipe at a time. |
| Stack Upgrade | Same speed bonus as Speed Upgrades, plus a parallel batch limit of **1 + floor(n / 4)**, capped at 32 operations. |
| Quantum Upgrade | Same speed bonus as Speed Upgrades, plus a parallel batch limit of **n + 1** operations. |
| Energy Upgrade | Reduces the extra power demand from speed and parallel upgrades, and adds **50% of base energy capacity per upgrade**. |

For example, four Speed Upgrades give 5× speed. Four Stack Upgrades give 5× speed
and allow two recipe operations per batch. Four Quantum Upgrades give 5× speed
and allow five operations per batch. Two Energy Upgrades double the machine's
base energy capacity.

Parallel processing repeats the same recipe using multiple sets of ingredients.
The actual batch size depends on available inputs and room for all outputs;
the upgrade's parallel limit is a maximum. Supply enough items and fluids to
make use of it.

Faster processing increases FE/t demand, and parallel upgrades also affect that
demand. Add Energy Upgrades and check the machine's energy display when tuning a
setup. Energy Upgrades reduce the upgrade penalty down to a minimum determined
by processing speed; they do not make processing free. A larger internal buffer
helps absorb fluctuations but still needs an adequate power supply.

## Slot and side configuration

Each recipe item slot and fluid tank has an independent setting for each of the
machine's six faces. This lets one face accept ingredients while another sends
results to storage, or lets different tanks connect to different fluid lines.
These settings control item and fluid access, rather than configuring FE.

1. Open the processor and click **Side Configuration**.
2. Click the item slot or fluid tank you want to configure.
3. Click a face to cycle its mode. Hover over it to read the direction and mode.
4. Repeat for every slot or tank used by your setup.

Directions are relative to the machine: **Front**, **Back**, **Left**, **Right**,
**Up** and **Down**. Use the machine's front as your reference when connecting
pipes or placing adjacent storage.

| Mode | Color | Effect on the selected slot or tank at that face |
| --- | --- | --- |
| INPUT | Blue | Allows an external pipe or other device to insert ingredients. Does not fetch them automatically. |
| PULL | Dark blue | Allows insertion and actively pulls ingredients from an adjacent inventory or tank. |
| OUTPUT | Orange | Allows an external pipe or other device to extract results. Does not send them automatically. |
| PUSH | Yellow | Allows extraction and actively pushes results into an adjacent inventory or tank. |
| DISABLED | Black | Blocks external access through that face for this slot or tank. |

Input slots cycle **INPUT → PULL → DISABLED → INPUT**. Output slots cycle
**OUTPUT → PUSH → DISABLED → OUTPUT**. Initially, recipe inputs use INPUT on all
faces and recipe outputs use OUTPUT on all faces. Disabling one slot on a face
does not disable other slots or tanks on that face, or prevent manual access
through the screen.

### Example: chest-fed Manufactory

1. Place an ingredient chest above the Manufactory and a result chest below it.
2. Select the item input in Side Configuration and set **Up** to **PULL**.
3. Select the item output and set **Down** to **PUSH**.
4. Put a valid ingredient in the upper chest and supply FE. The machine pulls
   ingredients from above and sends completed results below.
5. Disable access on unused faces if other connected automation should not
   interact with those slots.

For an external transport system that already moves items or fluids, INPUT and
OUTPUT are sufficient on the connected faces. With NuclearCraft universal pipes,
also configure the connector's transport types and modes. The processor's slot
settings and the transport system's settings must both allow the transfer.

## Autocrafting

The Engineer's Crafting Table can supply ingredients to supported processors
and collect their results as part of a crafting chain. Connect the machines
through a universal pipe network and pipe connectors, encode processing
patterns, and supply power to both the table and processors.

See [Autocrafting with processors](ENGINEERS_CRAFTING_TABLE.md#autocrafting-with-processors)
for network layout, processing-pattern encoding, fluid storage and machine
requirements. Pumps and Ore Leachers are excluded from processing-pattern
autocrafting.

## Troubleshooting

| Problem | What to check |
| --- | --- |
| Nothing processes | Verify the machine and recipe, ingredient counts, required fluids, FE supply and redstone mode. |
| Processing stalls after adding upgrades | Check FE/t demand and power delivery. Add Energy Upgrades or reduce speed upgrades if necessary. |
| Inputs are available but no new batch starts | Clear blocked outputs and leave room for every item and fluid result. |
| Adjacent storage does not transfer automatically | Use PULL for the selected input or PUSH for the selected output on the touching face. INPUT and OUTPUT require external transport. |
| Only some ingredients or results transfer | Configure every relevant slot and tank separately, and check pipe filters and connector settings. |
| Parallel upgrades do not reach their advertised batch size | Supply multiple complete sets of ingredients and enough output capacity. |
| An upgrade will not insert | Use the matching upgrade slot. Speed, Stack and Quantum Upgrades share one slot; some special machines do not support them. |

Modpacks can change recipes, processing times and energy costs. See
[Mod configuration](CONFIGURATION.md) for the global and per-processor settings
in `processors.toml`.
