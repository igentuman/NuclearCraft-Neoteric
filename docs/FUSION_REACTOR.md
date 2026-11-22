# Fusion reactor

The Fusion Reactor heats two fluid fuels into plasma, generates Forge Energy
(FE/RF), and produces fluid reaction products. Electromagnets confine the plasma;
RF Amplifiers heat it. A separate coolant supply removes heat from the reactor's
hardware and becomes a heated output fluid.

Successful operation means keeping **plasma near the fuel recipe's optimal
temperature while keeping reactor heat below its limit**. These are different
readings: hotter plasma is useful up to a point, but hot hardware loses efficiency
and can cause an explosion.

This guide describes the current NeoForge 26.1.2 implementation. Use JEI or EMI
for crafting, fuel pairs, optimal temperatures, and coolant recipes in your pack.

## Building the ring

The reactor is a horizontal square ring with a hollow plasma channel and a
**3 × 3 cross-section**. The whole structure is three blocks tall, with the
Fusion Reactor Core in the center of the bottom layer.

| Block | Purpose and placement |
| --- | --- |
| Fusion Reactor Core | Central controller. Placing it in clear space creates a 3 × 3 × 3 core assembly automatically. |
| Fusion Reactor Connector | Four equal, straight connector arms from the core, at the middle height. Their length determines ring size. |
| Fusion Reactor Casing / Glass | Inner and outer walls, floor, and roof around the plasma channel. Glass can replace casing. |
| Electromagnet / Electromagnet Slope | Functional blocks on the top and bottom edges of the channel. Add magnetic field and influence plasma confinement. Slopes have the same stats as their corresponding full blocks. |
| RF Amplifier | Functional block in the same edge positions. Adds plasma heating. |

Leave the central core's full 3 × 3 × 3 space clear **before placing the core**.
The placed block is at the horizontal center of that space, on its bottom layer.
Right-click the resulting assembly to open the controller. Its outer cells also
provide fluid and energy access.

### Size and exact placement

Let **S** be the reactor size parameter. It is **one plus the number of
connectors in each arm**, rather than the outside width. Starting from the core's
horizontal center, connectors run from distance 2 through distance S in each of
the four cardinal directions, all at the middle height. All arms must have the
same length with no gaps.

| Connectors per arm | S | Outside footprint | Height |
| ---: | ---: | --- | ---: |
| 0 | 1 | 9 × 9 | 3 |
| 1 | 2 | 11 × 11 | 3 |
| 2 | 3 | 13 × 13 | 3 |
| 4 | 5 | 17 × 17 | 3 |
| 8 | 9 | 25 × 25 | 3 |
| 16 | 17 | 41 × 41 | 3 |

The current validator supports **S = 1-17**, including the compact layout with
no connectors. A **one-connector-per-arm, 11 × 11 × 3** build is a straightforward
starting layout. Outside width is `2 × S + 7`.

For an exact plan, measure square rings from the core's horizontal center. A
square ring of radius R consists of positions where `max(abs(x), abs(z)) = R`.
Use y = 0 for the placed core's layer:

| Layer | Required chamber blocks | Plasma / functional positions |
| --- | --- | --- |
| Bottom, y = 0 | Casing/glass square ring at radius S + 2, including corners | Magnets and amplifiers belong at radii S + 1 and S + 3. Omit the four outermost corners. |
| Middle, y = 1 | Inner wall at radius S + 1, including corners; outer wall at radius S + 3, omitting its four corners | The entire square ring at radius S + 2 must be air. Connector arms also lie on this layer. |
| Top, y = 2 | Same casing/glass ring as the bottom | Same functional positions as the bottom. |

Across a straight section, this looks like:

```text
                inward → outward
Top:            [F] [C] [F]
Middle:         [C] [ ] [C]
Bottom:         [F] [C] [F]

C = casing/glass; F = magnet or RF amplifier; blank = plasma channel
```

The functional positions run **along the edges all around the ring**, not just
at its four turns. They do not replace the required walls, floor, or roof.
Unused functional positions can remain empty; filling them with casing adds
no magnetic field or RF amplification. Magnets or amplifiers outside these
positions do not contribute. Keep pipes, cables, and other blocks out of the
plasma channel.

## Why size and functional blocks matter

A larger ring offers more room for functional blocks and increases the energy
output scale. With the same fuel and comparable plasma conditions, the size
factor for generation is **S × ln(S + 1)**. This is a multiplier within the
reaction calculation, not a promise of a fixed FE/t output.

Size also changes plasma heating, heat losses, and confinement requirements.
The confinement calculation compares total magnetic field divided by S against
a requirement proportional to S and the plasma-to-optimal-temperature ratio.
At the same relative plasma temperature, preserving that field comparison
requires total field to grow roughly with **S²**. Extending the ring without
upgrading its magnets can therefore make it harder to run well.

Coolant is also less effective per operation in a larger ring: its cooling
value is divided by S. Plan stronger magnets, enough amplification, and more
coolant transport when expanding. Size alone does not increase the core's tanks
or energy buffer.

### Choosing magnets and amplifiers

| Tier | Field per magnet | RF voltage per amplifier | Component efficiency | Temperature rating |
| --- | ---: | ---: | ---: | ---: |
| Basic | 0.2 | 200,000 | 50% | 350,000 |
| Magnesium Diboride | 0.5 | 500,000 | 80% | 39,000 |
| Niobium Tin | 1.0 | 1,000,000 | 90% | 18,000 |
| Niobium Titanium | 2.0 | 2,000,000 | 95% | 10,000 |
| BSCCO | 4.0 | 4,000,000 | 99% | 104,000 |

Fields and RF voltages add across all correctly placed blocks. Component
efficiency is averaged separately for magnets and amplifiers. Temperature
ratings use the **lowest rating installed in each group**, so mixing tiers can
retain a temperature bottleneck. Higher tiers provide stronger components, but
do not consistently have higher temperature tolerance.

Magnets influence confinement, plasma losses, and conversion performance.
Amplifiers must supply enough heating to overcome plasma losses and approach
the selected recipe's optimum. Replacing every magnet with amplifiers sacrifices
confinement; replacing every amplifier with magnets leaves no plasma heating.
An assembled structure alone does not prove the functional layout can sustain
your chosen fuel.

Hardware heat reduces each group's effective efficiency once it rises above
that group's temperature rating. The current meltdown heat limit is
**twice the larger of the two group ratings**. A component group can therefore
lose efficiency before the overall heat limit is reached. Keep heat comfortably
below both component ratings where possible.

## Fuel, coolant, and connections

Connect fluid lines and energy cables to the core assembly. Use its side
configuration to give input and extraction lines access to the appropriate
tanks. All connections share the same storage:

| Tank, using computer API numbering | Contents |
| --- | --- |
| 1 | Fuel A input |
| 2 | Fuel B input |
| 3 | Coolant input |
| 4-7 | Four separate reaction-product outputs |
| 8 | Heated coolant output |

Each tank holds **100,000 mB**. The energy buffer holds **2,048,000,000 FE**;
the core's declared transfer limits are **1,000,000 FE/t input** and
**100,000,000 FE/t output**. Actual transfer also depends on cables and side
configuration.

Both fuel tanks must contain a valid pair in sufficient recipe quantities.
The pair works in either order. For example, the default Deuterium-Tritium
recipe consumes **250 mB of each**, has an optimum of **816,000,000**, and
produces **65 mB of Helium in each of the four product tanks** per completed
batch, for 260 mB total. Its 220,000 energy value is a recipe parameter; actual
FE/t depends on size, temperature, and confinement.

Fuel is consumed at the beginning of a batch. Processing progresses faster
with higher reaction efficiency; at efficiency 1, the default D-T batch's
200 process units take about 50 ticks. Clear **all four product tanks**, even
when they contain the same fluid. Output space is checked before a new batch;
blocked products prevent continuous operation.

### Cooling loop

| Coolant input per operation | Heated output | Base cooling |
| --- | --- | ---: |
| 10 mB Liquid Nitrogen | 10 mB Nitrogen | 2,500 |
| 10 mB Liquid Helium | 10 mB Helium | 2,500 |
| 1,000 mB Water | 1,000 mB Steam | 2,000 |
| 1,000 mB Technical Water | 1,000 mB High Pressure Steam | 2,500 |

Actual cooling per operation is **base cooling / S**. The reactor performs as
many whole operations as needed and possible from stored coolant and heated
output space. Liquid Nitrogen and Liquid Helium provide much more cooling per
mB than water. Passive heat loss helps, but does not establish that a design
is safe without coolant.

Extract heated coolant continuously. An empty coolant tank **or a full heated
output tank** stops coolant cooling. Return reusable coolant through your
processing chain, or send steam to a turbine as appropriate. The
[Heat Exchanger](HEAT_EXCHANGER.md) can recover heat from supported spent-steam
fluids downstream; check recipes for each part of the loop.

## Starting the reactor

1. Build the core, equal connector arms, chamber, and functional blocks. Open
   the core and resolve structure errors. Verify that magnets and amplifiers
   are being counted.
2. Supply external FE for the initial functional-block charge. Charging must
   reach **100%** before the reaction can heat plasma. A reactor starting cold
   cannot provide its own startup energy.
3. Fill both fuel inputs and the coolant input. Arrange extraction from every
   reaction-product tank and the heated coolant tank before starting.
4. If a computer previously disabled the reactor, call `enableReactor()`.
5. Apply a sustained redstone signal to the core assembly. A lever gives
   strength 15 and is convenient for startup. Charging occurs while enabled
   and powered, followed by plasma heating once fuel is available.
6. Watch charge, plasma temperature, reactor heat, and energy output. There
   is no generated energy below **1,000,000 plasma temperature**. Allow time
   for heating; charge completion is not ignition completion.
7. Once plasma reaches the fuel's optimum, tune RF amplification to keep it
   near that temperature. Check sustained cooling and extraction before leaving
   the reactor running.

The charge target is seven times the combined magnet and effective amplifier
power ratings. Each charging tick draws up to half that combined rating from
stored FE. In this implementation those ratings determine charging; they are
not also continuously deducted as a separate running FE/t bill after charge
is satisfied. The displayed generation rate describes reaction output.

## Maintaining operation with redstone

**Signal 0 stops the reaction. Signals 1-15 permit operation.** The strongest
signal touching any cell of the 3 × 3 × 3 core assembly wins, so a second lever
or nearby powered block can override a weaker control signal.

Before the plasma first reaches the recipe's optimal temperature, heating uses
**full RF amplification**, regardless of positive signal strength or slider
setting. After ignition, effective amplification is:

```text
floor(signal / 0.15)% × GUI amplification%
```

The GUI setting defaults to **50%** and ranges from **1-100%**. At signal 15,
that default gives 50% effective amplification after ignition; at signal 8,
it gives about 26.5%. Redstone and the slider multiply each other. Neither
setting provides a true off switch while redstone remains positive.

Reduce amplification when plasma runs above its optimum; increase it when
plasma cools below the optimum. Excessively hot plasma lowers conversion
efficiency rather than increasing output indefinitely. If plasma falls below
1,000,000, the reactor loses its ignition state and uses full heating again
when a fueled reaction resumes.

### Comparator monitoring

Place a comparator against an outer cell of the core assembly. **Sneak-right-click
an outer core cell with an empty hand** to cycle its comparator source:

| Mode | Signal measures |
| --- | --- |
| Energy | Stored FE / energy capacity |
| Heat, the default | Reactor heat / maximum heat |
| Efficiency | Reaction efficiency, clamped to 0-100% |

The selected mode is shared across the core assembly. Output is
`floor(15 × fraction)`. Efficiency here measures the reaction's temperature
efficiency; it is not a separate measurement of magnetic confinement.

For an overheat cutoff, use Heat mode and a circuit that **removes** the core's
operating signal when the comparator reaches your chosen threshold. For example,
strength 12 corresponds to at least 80% heat. Add a manual reset or hysteresis
so the reactor does not repeatedly restart at the threshold. Keep feedback
wiring from accidentally powering another core cell.

Removing redstone or disabling the reactor stops generation and rapidly cools
the plasma. Reactor heat then falls passively by 1,000 per tick; the normal
coolant conversion loop does not run during this disabled state. Shutdown also
clears unfinished batch progress, without returning fuel already consumed.
Stop early enough to leave a heat margin, and wait for plasma to reach zero
before changing the structure. Breaking a hot operating structure can cause
an explosion; exceeding the heat limit with live plasma can also explode.

## Computer control

CC:Tweaked and OpenComputers 2 expose the fusion API on the **actual controller
block**, at the bottom center of the core assembly. Connect beneath that block
for convenient access. The surrounding core cells proxy fluid and energy
capabilities, but do not expose the fusion computer API themselves.

| Method | Use |
| --- | --- |
| `isFormed()` | Check whether the structure is assembled. |
| `enableReactor()` / `disableReactor()` | Clear or set the computer shutdown override. Positive redstone is still required after enabling. |
| `isReactorEnabled()` | Check the shutdown override; does not mean the reactor is running. |
| `isRunning()` | Check whether the reaction is producing energy. Can be false during startup. |
| `getRedstoneSignal()` | Read the strongest detected core input, 0-15. |
| `getRFAmplification()` / `setRFAmplification(percent)` | Read or set the GUI adjustment, clamped to 1-100. Returns the adjustment, not the effective redstone-scaled value. |
| `getPlasmaTemperature()` | Read current plasma temperature. |
| `getMaxPlasmaTemperature()` | Read the display scale: twice the current recipe's optimum once a reaction updates it. This is not a safe operating target. |
| `getPlasmaStability()` | Read reaction efficiency, despite the method's name. |
| `getHeatStored()` / `getMaxHeatCapacity()` | Read hardware heat and its meltdown limit. |
| `getMagneticFieldStrength()` | Read the total counted magnetic field. |
| `getEnergyPerTick()` / `getEnergy()` | Read generated FE/t, or a table with `stored` and `capacity`. |
| `tanks()` / `getTank(index)` | Inspect fluid `name`, `amount`, and `capacity`; tank indices are 1-8 as listed above. |
| `voidFuel()` | Destroy fuel in both input tanks. Does not undo fuel already committed to a batch. |

For example, this CC:Tweaked program enables an assembled reactor, reports its
condition every second, and trips at 80% reactor heat. Supply redstone, fuel,
coolant, and startup FE separately. The trip exits the program and requires a
deliberate restart:

```lua
local reactor = assert(peripheral.find("fusion_reactor_core"),
    "Connect to the Fusion Reactor Core controller from below")
assert(reactor.isFormed(), "Reactor is not assembled")
assert(reactor.getRedstoneSignal() > 0, "Apply redstone to the core")

reactor.setRFAmplification(50)
reactor.enableReactor()

while true do
    local heat = reactor.getHeatStored()
    local limit = reactor.getMaxHeatCapacity()
    if not reactor.isFormed() or (limit > 0 and heat >= limit * 0.8) then
        reactor.disableReactor()
        print("Reactor tripped. Inspect cooling and structure before restart.")
        break
    end

    print(string.format("Plasma %.0f | heat %.0f / %.0f | %d FE/t",
        reactor.getPlasmaTemperature(), heat, limit,
        reactor.getEnergyPerTick()))
    sleep(1)
end
```

Tune the GUI adjustment manually with this monitor, or extend it to adjust
`setRFAmplification()` in small steps after ignition. Allow time for temperature
to respond and use a band around the optimum to avoid constant oscillation.
A one-second monitor may miss a fast heat rise: choose a larger margin or faster
polling for your build, and retain a physical redstone cutoff. Terminating the
program alone does not call `disableReactor()`.

## Troubleshooting

| Symptom | Checks and remedies |
| --- | --- |
| Structure does not assemble | Equal connector arms without gaps; correct chamber radii and layers; complete required walls; empty plasma channel. Follow the controller's reported error position. |
| Structure assembles but does not charge | Positive redstone, no computer shutdown override, and FE reaching the core's buffer. |
| Charge is full but no reaction | Valid fuel pair in sufficient amounts, room in all product tanks, and counted RF amplifiers in functional positions. |
| Plasma rises but FE/t stays zero | Generation begins at 1,000,000 plasma temperature. If temperature cannot rise further, revisit heating and confinement. |
| Reactor cools sharply after reaching the optimum | Full startup heating has switched to the redstone × GUI setting. Raise that setting if the resulting heating is insufficient. |
| More amplification gives worse output | Plasma may be above the optimum. Reduce heating and inspect hardware heat, magnetic field, and component efficiency. |
| Larger build performs poorly | Increase functional block strength and count, especially magnetic field; scale coolant supply and heated-output extraction. |
| Heat keeps rising despite coolant pipes | Check actual tank contents, a supported coolant recipe, transport rate, and space in tank 8. Larger S reduces cooling per operation. |
| Reaction stops after a few batches | Check both fuel supplies and every product output. Four identical product fluids still occupy four separate tanks. |
| Computer says enabled but reactor is idle | Enabled only clears the shutdown override. Check formation, redstone, charge, fuel, plasma temperature, and output space. |

## Implementation references

The placement and operating details above follow the current
[validator](../src/main/java/igentuman/nc/multiblock/fusion/FusionReactorValidator.java),
[reaction logic](../src/main/java/igentuman/nc/multiblock/fusion/FusionReactorLogic.java),
[core controller](../src/main/java/igentuman/nc/block_entity/fusion/FusionReactorControllerBE.java),
[default recipes](../src/main/java/igentuman/nc/datagen/recipe/processors/FusionReactorRecipes.java),
and [computer API](../src/main/java/igentuman/nc/compat/computer/FusionReactorApi.java).
