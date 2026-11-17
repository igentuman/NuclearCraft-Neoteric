# Fission reactors

## Basic information

A solid-fuel fission reactor is a hollow cuboid containing fuel cells,
moderators and heat sinks. It consumes fission fuel, produces depleted fuel,
and generates either Forge Energy (FE/RF) or hot fluid in steam mode.

The default outside dimensions are **3-26 blocks along each axis**, including
the casing. The reactor does not have to be a cube. Build a complete shell of
Fission Reactor Casing and/or Fission Reactor Glass, with one Fission Reactor
Controller and any ports needed for automation. Put fuel cells and cooling
blocks inside; unused interior spaces may remain air. Server configuration and
modpack scripts can change sizes, fuels, recipes and cooling values.

Heat generation and cooling determine whether a reactor can run continuously.
If heat generation exceeds cooling, stored heat rises. Exceeding heat capacity
causes a meltdown and can damage the reactor and its surroundings. Check the
controller's heat, cooling and stored-heat readings with the fuel you actually
intend to use.

## Reactor Designer and online hub

Use the **Fission Reactor Designer** to plan a layout before spending materials.
You can arrange fuel cells, moderators, heat sinks and irradiation chambers,
select a fuel, and compare estimated power, heat, cooling and irradiation.
Save layouts to revisit or adjust them later.

The designer also offers an **online Designs Hub** for browsing and loading
shared reactor designs and uploading your own. Hub access can be disabled in
configuration. Treat shared designs as starting points: check their fuel and
cooling requirements against your modpack before building them.

## Building and running a reactor

1. Choose a fission fuel in JEI or EMI. Check its power, heat, depletion time
   and depleted-fuel output.
2. Build the shell, controller and ports. Install at least one Solid Fuel Cell
   inside, then add moderators and heat sinks for your planned layout.
3. Open the controller and resolve any structure validation errors. A complete
   shell must enclose only supported reactor interior blocks or air.
4. Insert fuel into the controller's fuel input. Fuel cells share this supply;
   you do not load fuel into each individual cell.
5. Connect energy extraction to the controller or a reactor port. Supply any
   active heat-sink coolant before starting.
6. Watch the reactor through startup until its readings settle. Reactivity
   ramps up over time, so the first few ticks do not show its full output.
7. Extract depleted fuel and keep the output slot clear. Automate fuel input
   and depleted-fuel extraction through ports for continuous operation.

More fuel cells increase fuel-processing speed. Their arrangement also matters:
cells link along straight axis-aligned lines when adjacent or separated by up
to **four consecutive moderator blocks**. Air and other blocks interrupt these
links. Linked cells increase power and heat, with heat rising more sharply.
Adding cells without redesigning cooling can make a previously safe layout
overheat.

## Moderators and passive heat sinks

**Graphite Blocks** and **Beryllium Blocks** are the default moderators. Besides
linking fuel cells, moderators directly beside cells add power and heat bonuses.
A moderator used to satisfy a heat sink's moderator requirement must be active
by touching a fuel cell. A block placed elsewhere in the interior does not
automatically qualify.

Passive heat sinks require no fluid supply. They cool only when their placement
rules are satisfied. Rules use face-adjacent neighbors, sometimes requiring
particular arrangements or other valid heat sinks. Check the heat sink's
tooltip and the designer before placing it. For example, a Water Heat Sink
needs a neighboring fuel cell or active moderator, while a Redstone Heat Sink
needs a neighboring fuel cell.

Leave a cooling margin when choosing fuel. Stored heat and the balance between
heat generation and cooling also affect reactor performance, so power and fuel
depletion are not determined by cell count alone.

## Active heat sinks

Active heat sinks offer stronger cooling but **consume coolant continuously**.
They still need to satisfy their placement rules. An Empty Active Heat Sink
does not provide cooling by itself; install the appropriate finished variant.

| Active heat sink | Coolant | Cooling per valid sink, by default |
| --- | --- | --- |
| Water | Water | 250 heat/t |
| Redstone | Molten Redstone | 270 heat/t |
| Cryotheum | Cryotheum | 480 heat/t |
| Enderium | Molten Enderium | 360 heat/t |
| Liquid Nitrogen | Liquid Nitrogen | 555 heat/t |
| Liquid Helium | Liquid Helium | 420 heat/t |

Supply the matching fluid to the reactor's **active coolant tank** through the
controller or a reactor port. The sinks use shared reactor tanks: there is no
need to run pipes through the interior to individual sinks. Configure the
relevant tank and face for fluid input using side configuration.

By default, each valid active sink consumes **10 mB/t** of its coolant. Four
Active Water Heat Sinks therefore require 40 mB/t, or 800 mB/s at 20 TPS. The
tank must contain the full demand for all valid sinks of that coolant type
each tick; if it does not, that entire group contributes no cooling that tick.
Consumed coolant is removed without a fluid output.

Keep a reserve and monitor supply. Losing coolant can turn a balanced reactor
into one that overheats. Water used for active cooling and water used for
boiling occupy separate tanks; supplying one does not supply the other.

## Irradiation

Irradiation uses the reactor to transmute materials in **Irradiator** machines.
The internal irradiation chambers create irradiation connections; the separate
Irradiator machine holds the recipe ingredients and outputs.

1. Inside the reactor, build a straight line with these three adjacent blocks:
   **Solid Fuel Cell → Moderator → Irradiation Chamber**.
   The line can run along any of the six block directions. It requires exactly
   one moderator between the cell and chamber.
2. Use a **Pile-Driver Irradiation Chamber** for five times the effective
   irradiation contribution per qualifying connection. A normal chamber
   contributes one. A chamber can have qualifying connections on several faces.
3. Place an **Irradiator** in the reactor shell, where it can access the formed
   reactor. Open its screen and insert the ingredients from an Irradiator recipe
   in JEI or EMI. Supply recipe fluids and extract products at this machine.
4. Run the reactor with fuel. The Irradiator draws its processing ability from
   the host reactor and does not need an external FE supply.

Irradiation strength depends on the qualifying connections and the current
fuel's heat and depletion time. Irradiator speed also scales with reactor
reactivity. Each irradiation connection adds **15 heat/t before the moderation
level multiplier**, so include irradiation in the cooling budget.

Recipes include Thorium Dust → TBP Dust, Bismuth Dust → Polonium Dust, and
Lithium → Irradiated Lithium. Check the recipe viewer for the available recipes
and their exact ingredients.

## Moderation level

The controller's **moderation level** is an output throttle from **0% to 100%**.
It is separate from the physical moderator blocks in the layout and from
reactivity, which ramps up during operation.

Lower moderation scales down generated power, generated heat and steam output.
Cooling is not reduced by this setting. Set it through a Moderator redstone
port or computer control.

**Lowering moderation does not directly slow fuel depletion.** It can change
depletion indirectly through the heat balance, but it is not a proportional
fuel-saving setting. Use the reactor switch or computer shutdown to stop the
reaction. Moderation level is also not a direct control of Irradiator speed.

## Redstone control

**Sneak-right-click a reactor port with an empty hand** to cycle its redstone
mode. The selected mode appears in the action bar.

| Port mode | Use |
| --- | --- |
| None | No redstone monitoring or control. |
| Energy | Comparator output shows the stored-energy fraction. |
| Heat | Comparator output shows stored heat relative to heat capacity. |
| Progress | Comparator output shows fuel depletion progress. |
| Items | Comparator output shows how full the fuel input stack is. |
| Switch | A signal greater than zero allows operation; no signal stops it. |
| Moderator | Input signal strength sets moderation from 0% to 100%, rounded to 10% steps. |

Use a comparator on a monitoring port to read its 0-15 signal. For example,
combine a Heat monitoring port with a separate Switch port to build a shutdown
circuit. A lever on a Switch port gives simple manual control. Full signal
strength on a Moderator port gives 100% moderation; zero gives 0%.

The reactor starts with redstone activation enabled and 100% moderation.
Cycling a port away from Switch releases its shutdown control; cycling away
from Moderator restores 100% moderation. Avoid competing input ports driving
the same setting, or a redstone port repeatedly overwriting a computer setting.

## Computer control

CC:Tweaked and OpenComputers v2 expose reactor monitoring and control through
the controller. Connect your computer integration to the Fission Reactor
Controller. The following method names are shared by both integrations:

| Methods | Purpose |
| --- | --- |
| `isFormed()`, `isReactorEnabled()`, `isRedstoneActivated()` | Check structure, computer enable state and redstone permission separately. |
| `enableReactor()`, `disableReactor()` | Release or apply computer shutdown. Redstone must also allow operation. |
| `getHeatStored()`, `getMaxHeatCapacity()` | Monitor stored heat and its limit. |
| `getHeat()`, `getCooling()`, `getEnergyPerTick()` | Read heat generation, cooling and FE output per tick. |
| `getModerationLevel()`, `setModerationLevel(percent)` | Read or set the throttle; input is clamped to 0-100. |
| `getReactivity()`, `getDepletionProgress()` | Read startup reactivity and fuel depletion progress. |
| `getFuelCellsCount()`, `getHeatSinksCount()`, `getModeratorsCount()`, `getIrradiatorLines()` | Inspect the reactor layout. |
| `isSteamMode()`, `toggleSteamMode()`, `getSteamRate()` | Check or request output mode and read hot-fluid output in mB/t. |
| `voidFuel()` | Discard the input fuel and the fuel currently being burned. |

A simple CC:Tweaked heat cutoff, with the controller connected on the right:

```lua
local reactor = peripheral.wrap("right")
while true do
    if reactor.isFormed() then
        local capacity = reactor.getMaxHeatCapacity()
        if capacity > 0 and reactor.getHeatStored() >= capacity * 0.8 then
            reactor.disableReactor()
        end
    end
    sleep(0.5)
end
```

This leaves the reactor shut down for inspection; call `enableReactor()` when
ready to restart. Shutdown stops generation, but stored heat is retained: do
not assume switching off will drain the heat buffer.

## Steam mode

Use the controller's mode button to switch between energy and steam mode.
The switch takes **200 ticks (about 10 seconds at 20 TPS)** to complete.
Steam mode can be disabled by server configuration.

In steam mode the reactor produces hot fluid instead of FE, and its energy
connections are unavailable. Supply the **boiling input tank** and extract from
the **hot-fluid output tank** through the controller or ports. Default recipes
are:

| Boiling input | Output | Heat requirement per recipe operation |
| --- | --- | --- |
| 10 mB Water | 10 mB Steam | 100 |
| 10 mB Helium | 10 mB Hot Helium | 400 |

Send Steam to a turbine, or process Hot Helium through a heat exchanger for a
larger heat-transfer setup. The controller's steam-rate reading reports the
hot-fluid output rate, including when the output is Hot Helium.

Production depends on available cooling throughput, the reactor's heat
multiplier, moderation level, boiling recipe, input supply and output space.
An empty boiling input or full output tank stops hot-fluid production;
**it does not automatically stop fuel consumption or heat generation**. Keep
normal heat-sink cooling available and arrange shutdown control for supply or
output failures. Boiling fluid is separate from the coolant consumed by active
heat sinks.

## Modpack customization

See [KubeJS events and recipes](KUBEJS.md) for custom
[fission fuels](KUBEJS.md#fission-fuels),
[heat sinks and placement rules](KUBEJS.md#heat-sinks), and
[fission boiling recipes](KUBEJS.md#other-recipe-types-via-eventcustom).
Irradiator recipes use the processor recipe API in the same guide. Global
reactor settings are covered in [Mod configuration](CONFIGURATION.md).
