# Molten Salt Reactor (MSR) — Gameplay & Implementation Design

> Final design document for adding an MSR controller to NuclearCraft-Neoteric.
> Focus: player-facing gameplay, mechanics, numerical model, UI, and implementation notes for `MSRControllerBE` with input/output ports only (fuel/coolant input, depleted fuel output) and a pressure-lock mechanic.


---

## 1. Overview

This document describes a new MSR controller that shifts gameplay from static reactor design to active **operation and flow management**. The MSR is a closed reactor that accepts:

* **Input ports:** for pumps to insert *molten salt* and *coolant salt* (both are fluids). Input fission fuel pebbles (items)
* **Output port:** only for *depleted fuel pebbles* (waste). Extract molten salt.

---

## 2. High-level gameplay goals

* Create an active operator experience: monitoring and controlling flows rather than static block arrangement.
* Make pressure an *emergent* symptom of operation (not a primary micromanaged stat).
* Reward player prediction and rhythm (timed refills/purges) and skilled automation (ComputerCraft / OC / redstone control).
* Keep interactions relatively simple: only pumps + timing, but with deep consequences.

---

## 3. Core systems & variables

Each reactor instance simulates a compact set of variables that interact each tick (or every N ticks):

### Core variables (stored in BE)

* `pebbleCount` (count) — number of TRISO pebbles currently in the reactor chamber.
* `saltVolume` (mB) — current molten salt volume inside reactor (carrier medium for fission).
* `coolantVolume` (mB) — current coolant salt volume (if distinct fluid) inside reactor. If single-loop, treat as mixed fluid.
* `depletedVolume` (mB) — accumulated depleted salt (waste) inside reactor.
* `pebbleDensity` (pebbles/mB) — derived from `pebbleCount / saltVolume`; affects reactivity via concentration factor.
* `temperature` (°C) — core temperature (simulation unit: double).
* `reactivity` (0..2+) — normalized chain reaction multiplier; modulated by thermal feedback, impurity, and concentration.
* `pressure` (unitless or kPa-equivalent) — internal pressure affecting flow locks.
* `impurity` (0..1) — concentration of fission products/poisons accumulated in the salt.
* `isCritical` (bool) — whether reactor currently sustains fission.

### Derived/auxiliary parameters (per fuel type)

For each TRISO pebble type:

* `minPebblesForCriticality` (count) — approximate minimal pebble count for criticality (depends on chamber size).
* `minSaltForCriticality` (mB) — minimal salt volume for criticality (salt must be present for heat transfer and slowing).
* `powerPerPebble` (RF/t) — energy output generated per pebble per tick at nominal reactivity.
* `heatPerPebble` (heat/tick) — heat generated per pebble per tick.
* `depletionRatePerTick` — fraction of pebble consumed per tick at nominal reactivity.
* `impurityRatePerPebble` — impurities accumulated per pebble depleted.

Fuel mixing: reactor computes weighted averages for `powerPerTick` and `heatPerTick` from pebble mix.
Concentration effect: `concentrationFactor = 1.0 + (pebbleDensity - optimalDensity) * concentrationModifier` applies to all pebbles.

---

## 4. Numerical model (equations and example constants)

Below is a compact, implementable model with values tuned to fit NuclearCraft-Neoteric gameplay.

### Example constants (tuned to mod values)

```text
# Temperature & Environment
T_ambient = 20.0            # °C (room temperature)
maxTemperature = 2000.0     # °C, catastrophic failure threshold
thermalMass = 1000000.0     # total heat capacity (from FISSION_CONFIG.HEAT_CAPACITY)

# Pressure System (NEW for MSR)
pressureBase = 0.0          # baseline pressure
pressureMax = 150.0         # lock threshold (tunable, higher = less frequent locks)
pressureUnlock = 120.0      # unlock threshold (hysteresis to prevent oscillation)
pressurePerDegree = 0.015   # pressure increase per °C above ambient
pressurePerDepletedMB = 0.008 # pressure increase per mB of depleted fuel

# Cooling & Heat Transfer
coolingEfficiency = 0.9     # fraction of coolant heat removal effectiveness
decayHeatFactor = 0.15      # fraction of heat remaining after reactor shutdown
activeCoolerConsumption = 10 # mB coolant per tick per active cooler (from config)

# Concentration & Reactivity Feedback (NEW for MSR)
optimalDensity = 0.025      # pebbles per mB of salt (target operating point)
concentrationModifier = 0.08  # how much deviation from optimal affects reactivity
# concentrationFactor = 1.0 + (pebbleDensity - optimalDensity) * concentrationModifier

# Pumping & Flow
pumpFlowRate = 100          # mB per pump operation per tick
maxSaltVolume = 50000       # mB active salt reservoir capacity
maxCoolantVolume = 50000    # mB coolant tank capacity
maxDepletedVolume = 50000   # mB waste tank capacity
minSaltForCriticality = 500 # mB minimum salt to sustain reaction
minPebblesForCriticality = 20 # pebble count minimum for criticality

# Energy Conversion (matches mod FE system)
feGenerationMultiplier = 10.0   # from FISSION_CONFIG.FE_GENERATION_MULTIPLIER
baseEnergyTier = 4              # GT-compatible energy output (configurable)
```

### Fuel System: Molten Salt + TRISO Pebbles

The MSR fuel is a **dual-component system**:

1. **Molten Salt (fluid)** — pumped into the reactor via input ports. Acts as the carrier medium and coolant.
2. **TRISO Pebbles (items)** — inserted directly into the reactor chamber. Contains the fissile material.

The combination of molten salt + pebbles creates a critical mass. The reactor will only sustain fission when:
- Sufficient TRISO pebbles are in the chamber (reaching `baseCriticalVolume` equivalent)
- Sufficient molten salt volume is present to support the chain reaction

### Fuel Pebble Properties (from NuclearCraft TRISO definitions)

Each pebble type is defined with these properties (see FuelDef.java):

```java
forge_energy    // FE per tick per pebble (primary power output when in reaction)
heat            // Heat generated per pebble per tick
criticality     // Neutron multiplication factor (higher = more reactive)
depletion       // Fuel burn rate modifier (higher = faster consumption)
efficiency      // Power generation efficiency modifier
```

**Example pebble parameters** (from mod definitions):

| Pebble Type | forge_energy | heat | criticality | depletion | efficiency | Notes |
|-----------|--------------|------|-------------|-----------|-----------|-------|
| Thorium-232 TR | 800 | 1.2 | 80 | 150 | 95 | Breeder fuel |
| Uranium-235 TR | 1500 | 2.0 | 150 | 80 | 90 | Standard fission |
| Plutonium-239 TR | 2000 | 2.5 | 200 | 120 | 92 | High energy |
| Americium-242 TR | 2200 | 2.8 | 220 | 140 | 85 | Exotic, more pressure |

**Fuel mixing:** When multiple pebble types are loaded in the reactor, properties are weighted by pebble count.

### Tick update pseudocode (per tick; run every game tick or every N ticks with scaled rates)

```text
# 0. Compute concentration & reactivity modifiers
pebbleDensity = pebbleCount / max(saltVolume, 1.0)  # pebbles per mB
concentrationFactor = 1.0 + (pebbleDensity - optimalDensity) * concentrationModifier
# Higher density → higher concentration factor → higher reactivity

# 1. Compute total fission power and heat
if isCritical and pebbleCount >= minPebblesForCriticality and saltVolume >= minSaltForCriticality:
    fissionPower = pebbleCount * powerPerPebble * reactivity
    heatProduced = pebbleCount * heatPerPebble * reactivity
    depletionAmount = pebbleCount * depletionRatePerTick * reactivity
    # Deplete pebbles (accumulate burnedFraction per pebble)
    depletedVolume += depletionAmount * saltPerDepletedPebble  # waste salt
    impurity += impurityRatePerPebble * depletionAmount
    # Remove fully burned pebbles from count
    pebbleCount = max(0, pebbleCount - fully_burned_pebbles_this_tick)
else:
    fissionPower = 0
    heatProduced = 0

# 2. Passive heat removal by coolant
heatRemoved = coolantVolume * coolingEfficiency * coolantHeatCapacityFactor
netHeat = heatProduced - heatRemoved
# update temperature
temperature += netHeat / heatCapacityTotal
temperature = max(temperature, T_ambient)

# 3. Reactive feedback on reactivity (thermal feedback + concentration)
thermalFeedback = f(T)  # lower temp = higher reactivity; higher temp = lower reactivity
# e.g., f(T) = clamp(1 - (temperature - T_optimal) * temperatureFeedbackFactor, 0.1, 2.0)
impurityFeedback = 1 - impurity * impurityEffect
reactivity = baseReactivity * thermalFeedback * impurityFeedback * concentrationFactor
# Note: concentrationFactor spikes when salt is extracted → emergent higher reactivity

# 4. Pressure calculation
pressure = pressureBase + (temperature - T_ambient) * pressurePerDegree + depletedVolume * pressurePerDepletedMB

# 5. Port lock logic (with hysteresis)
if pressure >= pressureMax:
    portsLocked = true
elif pressure <= pressureUnlock:
    portsLocked = false

# 6. Emergency / failure checks
if temperature >= maxTemperature:
    triggerCatastrophicFailure()

# 7. Output power (scale with fissionPower and conversion eff)
energyOutput = fissionPower * conversionFactor
```

**Key mechanic:** when salt is extracted via pumps, `pebbleDensity` increases, raising `concentrationFactor` and thus `reactivity`. This creates the pressure-reactivity tradeoff: extracting salt to reduce pressure causes reactivity to spike unless coolant is managed.

### Notes on tuning

* `depletionRatePerTick` should be small (e.g., 0.0001–0.001 per tick) so fuel lasts many minutes of gameplay depending on siphoning.
* `pressurePerDepletedMB` controls how fast output becomes blocked — higher means frequent draining required.
* `pressurePerDegree` ties temperature control to pressure; raising temperature quickly will push toward lock.
* `concentrationModifier` controls how much pebble density affects reactivity. Tuning this is crucial for the salt extraction mechanic:
  - **Too high** (~0.5+): extracting salt causes runaway reactivity spikes; hard to manage without aggressive cooling.
  - **Too low** (~0.01): salt extraction barely affects reactivity; pressure-relief becomes too easy, undermining challenge.
  - **Recommended starting point** (~0.05–0.1): extracting 500 mB from 2000 mB (25% reduction) raises reactivity by ~10–20%.
* `optimalDensity` (pebbles/mB) — set based on desired "goldilocks" operating point. E.g., 0.025 pebbles/mB = "perfect" state; higher or lower both increase reactivity via concentrationFactor.

---

## 5. Ports, pumps and pressure-lock flow logic

The reactor has **input** and **output** sides but *physical pumps* or external piping determine which fluid is moved.

### Port types & I/O mechanisms

* `IN_SALT` — accepts molten salt fluids (pumped via pipes/pumps). Acts as carrier medium for the reaction.
* `IN_COOLANT` — accepts coolant fluids (may be same registry family or different fluid types).
* `OUT_SALT` — outputs molten salt from the reactor. Players can extract active salt to reduce pressure and manage reactivity.
* `OUT_DEPLETED` — outputs depleted salt waste. Automatically generated during fission.
* **Item Input (chamber slot)** — direct insertion of TRISO pebbles into the reactor chamber. Pebbles settle in the bottom of the chamber until removed or burned.
* **Item Output (hopper-compatible)** — spent/depleted pebbles can be extracted manually or via automation.

### Extracting molten salt & depleted waste

**Molten Salt Extraction (`OUT_SALT`):**
* Removes active salt from the reactor core, **directly reducing pressure** (salt is part of pressure calculation).
* **Strategic tradeoff:** Removing salt *decreases* pressure (good for avoiding lock), but **increases fuel concentration and reactivity** (pebbles per mL of salt increases).
  - Example: 2000 mB salt + 50 pebbles → removing 500 mB salt leaves 50 pebbles in 1500 mB, raising pebble density and reactivity multiplier.
  - Player must balance: extract salt to manage pressure, but watch that rising reactivity doesn't spike temperature uncontrollably.
* Extraction is only possible when ports are unlocked.
* Typical strategy: extract salt when pressure builds, but inject coolant or monitor temperature closely after salt removal.

**Depleted Waste Extraction (`OUT_DEPLETED`):**
* Removes accumulated waste salt, **lowers pressure indirectly** (reduces `depletedVolume` contribution to pressure).
* Does not affect active reactivity directly (waste is inert).
* Useful for long-term pressure management without changing core composition.

---

## 6. Player interactions and challenges (gameplay loop)

**Core loop:** load pebbles → pump salt → reach criticality → manage heat/pressure → extract waste → refill.

### Player activities

* **Pebble loading:** Insert TRISO pebbles directly into the reactor chamber via item input slot until reaching critical mass.
* **Salt supply:** Use pipes/pumps to inject molten salt into the reactor. The salt volume + pebble count must together exceed criticality threshold to sustain reaction.
* **Salt extraction (pressure relief):** Extract active salt via `OUT_SALT` port to reduce pressure when approaching lock threshold. Watch for reactivity spike — may need coolant boost after extraction.
* **Waste management:** Extract depleted salt via `OUT_DEPLETED` port for long-term pressure control (safer than salt extraction). Send to `Reprocessor` to recover fissile content.
* **Monitoring:** Watch GUI gauges for temperature, pressure, reactivity, pebble density (pebbles/mB salt), and power output.
* **Flow control:** Balance salt input/output to maintain optimal reactivity and pressure while avoiding thermal runaway.
* **Timing & rhythm:** React to pressure buildup; extract salt carefully (monitoring temperature response); reinject salt when pressure drops.
* **Pebble maintenance:** Spent/depleted pebbles remain in chamber and must be manually extracted via item output to free space for fresh pebbles.

## 7. GUI / monitoring / player feedback

Design an MSR GUI that communicates the living nature of the reactor clearly and gives players decision-quality information.

### Suggested GUI elements

* **Top bar:** Reactor name, status (Idle / Running / Locked / Emergency).
* **Left column (gauges):**

    * Temperature gauge (numeric + colored bar).
    * Pressure gauge (numeric + colored bar; red when >= pressureMax).
    * Reactivity gauge (numeric 0..100%).
    * **Pebble Density gauge** (pebbles/mB; colored to show if concentration is optimal/too high/too low) — **key for pressure-reactivity management**.
* **Center (graph):** small time-series graph (last 60s) showing Temperature (red), Pressure (orange), Reactivity (yellow), and Power Output (green).
* **Right column (resources):** 
    * Pebble count & density
    * Salt volume (`saltVolume`)
    * Coolant volume (`coolantVolume`)
    * Depleted salt (`depletedVolume`)
    * Impurity level
* **Bottom controls:** buttons for emergency drain (if implemented), toggle auto-extract (if automation allowed), and links to automation API.
* **Status indicator:** warn when extracting salt ("Concentrating fuel — monitor temperature!").

### Warnings & audio

* When ports lock: play distinctive alarm and flash gauge; pumps show failure tooltip: "Blocked by pressure".
* When salt extraction causes reactivity spike (or temperature climbing post-extraction): display warning "High fuel density — inject coolant!".
* When temperature approaches `maxTemperature`, flash catastrophic warning and give option to emergency drain (if allowed by design).

---

## 8. Emergency events & failures

These produce drama and teach players to respect the system.

### Events

* **Salt Solidification (freeze):** if temperature falls below a `freezeTemperature` (e.g., 500 °C) while reactor contains fuel, some fraction of fuel becomes non-circulating (remove active volume). Fix: add external reheating or pumps to remix.
* **Catastrophic Failure:** if `temperature >= maxTemperature` and no emergency system was triggered, cause containment breach: localized explosion, damage to blocks and radiation effect (if you implement radiative spread), and partial loss of BE state. Optionally, degrade reactor casing making pressure tolerance lower for next start.

### Safety systems (optional)

* **Passive expansion tanks** (internal buffer to reduce pressure spikes). Implemented as BE upgrade items.
* **Emergency drain** (manual): instantly transfers most fuel into an external storage (if a connected tank can accept). If ports locked, emergency drain could be allowed as a special-case operation but at cost (massive radiation or block damage) — or keep design consistent: emergency drain only works if ports are unlocked.

---

## 9. Maintenance, fuel cycle & reprocessing

Make reprocessing rewarding but risky.

### Fuel cycle steps

1. **Pebble loading:** TRISO pebbles are inserted directly into the MSR chamber as items. No conversion needed.
2. **Salt supply:** Players pump molten salt into the reactor from external sources (e.g., salt tanks, electrochemistry machines).
3. **Operation:** Once enough pebbles + salt are present, the reaction sustains automatically.
4. **Waste extraction:** Players pump out depleted salt (waste) through the output port when pressure rises.
5. **Reprocessing:** Depleted salt sent to a **Reprocessor** machine, which:
   - Recovers a portion of fissile content (returned as molten salt or new pebbles)
   - Produces waste items/blocks and contaminants
   - Takes power and time

### Reprocessing tradeoffs

* **Efficiency:** recover 50–80% of fissile content from depleted salt depending on tech tier.
* **Time & power cost:** reprocessing consumed power and takes multiple ticks.
* **Pebble removal:** spent pebbles in the chamber produce diminishing returns and must be manually extracted to free space for new ones.
* **Risk:** reprocessing on-line while reactor runs could introduce delays or contamination events (if implemented).

---

## 10. Progression and advanced mechanics

Layers that unlock as players progress. Integration with existing mod systems:

### Existing NuclearCraft Systems to Leverage

* **ComputerCraft: Tweaked** integration (already present in mod via `SidedFissionReactorPeripheral`):
  * Expose MSR stats: `getTemperature()`, `getPressure()`, `getReactivity()`, `getPebbleCount()`, `getSaltVolume()`, `getDepletedVolume()`, `isLocked()`
  * Allow script-based pump control and emergency shutdown
  * Implement automation scripts for smooth operation and pressure management
  
* **OpenComputers 2** support (already present via `FissionReactorDevice`):
  * Component-based access to reactor metrics
  * Real-time monitoring dashboards
  
* **KubeJS** integration (already present via `NCKubeJSEvents`):
  * Register custom MSR fuel types with custom burn rates and heat profiles
  * Create custom recipes for fuel conversion via scripting
  * Example: `FuelDef` can be created dynamically in KubeJS

* **Mekanism** compatibility:
  * Use Mekanism pumps and pipes for fluid transfer (already compatible)
  * Integrate with Mekanism radiation system if active
  * Consider heat exchange with Mekanism heat machinery

* **Minecraft features:**
  * Redstone signals (from `hasRedstoneSignal` in FissionControllerBE) for simple on/off control
  * Hopper support for item input/output
  * JEI recipe integration (already present, can extend for MSR)

### Progression Tiers

* **Early Game:** Basic MSR with standard uranium fuels, manual pump control
* **Mid Game:** Unlock advanced casings (increase `pressureMax` and `pumpFlowRate`), access to plutonium fuels
* **Late Game:** Breeding fuels (thorium), automation via ComputerCraft, exotic fuel types
* **Advanced:** Pressure reduction upgrades, emergency drain systems, custom cooling loops

### Tunable Upgrades

* **Better Casings & Ports:** increase `pressureMax` (100 → 200) and `pumpFlowRate` (100 → 150 mB/tick); improves salt extraction speed
* **Higher throughput pumps:** move more fluid per tick (up to 200 mB/tick for late-game pumps); enables faster pressure relief
* **Concentration buffers (internal):** reduce `concentrationModifier` effect (e.g., 0.1 → 0.05) by internal volume expansion, making salt extraction less risky
* **Thermal inertia upgrades:** increase `thermalMass` to slow down temperature response, giving players more reaction time after salt extraction
* **Advanced coolants:** different fluids with `coolingEfficiency` modifiers (0.7 to 1.2 range); helps manage reactivity spikes
* **Gas separators:** reduce `pressurePerDepletedMB` effect by 50% via external machine (similar to active coolers in Fission); alternative pressure relief path
* **Salt buffer tanks (internal):** allow larger salt reserves, increasing minimum salt volume before criticality drops
* **Breeding mechanics:** certain fuels (thorium pebbles) slowly increase fissile content during operation (~0.1% per 1000 ticks)
* **Automation API exposure:** ComputerCraft/OC read gauges and control pumps (but keep `portsLocked` safety - pumps fail when locked)

---

## 11. Implementation notes: block entity design & methods

Structure `MSRControllerBE` following NuclearCraft-Neoteric patterns from `FissionControllerBE`. Key fields and methods:

### Class Structure (pattern from existing code)

Extend `MultiblockControllerBE` and implement `IEnergyStorage` capability:

```java
public class MSRControllerBE extends MultiblockControllerBE {
    public static final String NAME = "msr_reactor_controller";
    public final SidedContentHandler contentHandler;         // handles I/O ports
    public final CustomEnergyStorage energyStorage;           // FE storage
    protected final LazyOptional<IEnergyStorage> energy;
    // ... fields ...
}
```

### Core Fields (use @NBTField for persistence)

```java
@NBTField
public double saltVolume = 0.0;           // mB, molten salt volume in chamber
@NBTField
public int pebbleCount = 0;               // number of TRISO pebbles in chamber
@NBTField
public double coolantVolume = 0.0;        // mB, stored in coolant tank
@NBTField
public double depletedVolume = 0.0;       // mB, depleted salt waste
@NBTField
public double temperature = 20.0;         // °C
@NBTField
public double reactivity = 1.0;           // 0..2+, chain reaction multiplier
@NBTField
public double pressure = 0.0;             // unitless pressure units
@NBTField
public double impurity = 0.0;             // 0..1, accumulated fission products in salt
@NBTField
public boolean portsLocked = false;       // pressure-lock state
@NBTField
public boolean isCritical = false;        // sustained fission flag

List<FuelPebble> pebbles;                 // in-chamber pebble inventory (items)
Map<FuelType, Double> pebbleMix;          // runtime mix of pebbles by count (not persisted, derived)
int tickCounter = 0;                      // for update scheduling

// From FissionControllerBE pattern
@NBTField
public double heatPerTick = 0;
@NBTField
public int energyPerTick = 0;
@NBTField
public double heatMultiplier = 1.0;
@NBTField
public boolean powered = false;
@NBTField
public boolean hasRedstoneSignal = false;
```

### Pebble & Fuel Properties Helper Classes

```java
class FuelPebble {
    public FuelType type;                  // e.g., U-235, Pu-239, Th-232
    public double burnedFraction = 0.0;    // 0..1, tracks pebble depletion
    // ... other state ...
}

class FuelProperties {
    public double powerPerPebble;          // FE/tick per pebble
    public double heatPerPebble;           // heat/tick per pebble
    public double depletionRatePerTick;    // 0..1 fraction of pebble consumed per tick
    public double gasGenerationRate;       // pressure contribution per pebble burned
    public int minPebblesForCriticality;   // pebble count needed for criticality threshold
    public double impurityRate;            // accumulated per pebble burned
    public double criticaltySaltModifier;  // how much salt volume affects criticality (e.g., 0.05 = 5% boost per mB)
}
```

### Key Methods (similar to FissionControllerBE)

**Initialization:**
* `initializePorts()` — setup multiblock port connections (from parent)
* `contentHandler()` — return I/O handler for fluid/item slots

**Main Simulation:**
* `tickServer()` — called each tick, orchestrates simulation
  * Calls `updateFission()`, `updateTemperature()`, `updatePressure()`, `checkFailures()`
  * Manages `portsLocked` state changes
  * Emits particles and sounds on state changes
  * Stores energy to `energyStorage`

**Physics Simulation:**
* `updateFission()` — compute power/heat based on `isCritical`, pebble mix, reactivity, and salt volume
  * Check if `pebbleCount >= minPebblesForCriticality AND saltVolume >= minSaltForCriticality` for criticality
  * Salt volume can substitute partially for pebble count (via criticaltySaltModifier)
  * Apply thermal feedback: `reactivity *= f(temperature)` (lower temp = higher reactivity)
  * Apply impurity feedback: `reactivity *= (1 - impurity * impurityEffectFactor)`
  * Consume pebbles: mark with `burnedFraction` accumulation; remove when fully depleted
  * Output: `heatPerTick`, `energyPerTick`, updated `depletedVolume` (converted salt), `impurity`

* `updateTemperature()` — heat balance simulation
  * `netHeat = heatProduced - heatRemoved`
  * `heatRemoved = coolantVolume * coolingEfficiency * coolantHeatCapacity`
  * `temperature += netHeat / thermalMass`
  * Clamp to `[T_ambient, maxTemperature]`

* `updatePressure()` — pressure from temperature and depleted fuel
  * `pressure = (temperature - T_ambient) * pressurePerDegree + depletedVolume * pressurePerDepletedMB`
  * Hysteresis logic for `portsLocked` state

**Safety & Failure:**
* `checkFailures()` — monitor for catastrophic conditions
  * If `temperature >= maxTemperature`: trigger meltdown, explosion (configurable radius)
  * If `pebbleCount < minPebblesForCriticality`: auto-shutdown (reaction dies)
  * Similar to FissionControllerBE explosion handling

**Fluid & Item Interaction:**
* `canAcceptFluids()` — return `!portsLocked` (gate fluid transfer)
* `getFluidTanks()` — provide access to 3 tanks: salt, coolant, depleted
* `tryInsertPebble(ItemStack)` — add pebble to chamber if space available; return excess
* `tryExtractDepletedPebbles()` — allow manual removal of spent pebbles
* Integrate with `SidedContentHandler` for pump I/O and item slots (from parent pattern)

**Automation API:**
* Implement CC peripheral methods (if using existing compat layer):
  * `getTemperature()`, `getPressure()`, `getReactivity()`, `getPebbleCount()`, `getSaltVolume()`, `getDepletedVolume()`, `isCritical()`, `isLocked()`
  * `insertPebbles(count)` — attempt to insert pebbles (if accessible via hopper)
  * `extractDepleted()` — manual trigger for waste extraction
* Or register OC2 device (similar to `FissionReactorDevice`)

### Multiblock Structure

Similar to `FissionReactorMultiblock`:
* Controller block at center (with item slot for pebble input)
* Casing blocks (variable size, min 3x3x3, max configurable)
* Port blocks for fluid input/output:
  * **Salt input port** (molten salt — main coolant/carrier)
  * **Coolant input port** (optional, can be same as salt)
  * **Salt output port** (active reactor salt — **NEW**: allows strategic extraction for pressure relief with reactivity tradeoff)
  * **Depleted output port** (waste salt output)
* Optional upgrade slots for casings or internal devices

Use existing `MultiblockPortBE` pattern for port handling.
* `tryInputFluid(FluidStack, type)` — called by pump code; returns amount accepted (0 if `portsLocked`). Routes to appropriate tank based on fluid type.
* `tryOutputFluid(FluidStack, type)` — extract salt (type=SALT) or depleted (type=DEPLETED); both blocked when `portsLocked`. 
  - **Extracting salt triggers concentration feedback:** removing saltVolume increases pebbleDensity, raising reactivity multiplier next tick.
  - Returns amount extracted (0 if `portsLocked` or tanks empty).
* `computeReactivity()` — returns reactive multiplier from `baseReactivity * thermalFeedback * impurityFeedback * concentrationFactor`.
  - `concentrationFactor = 1.0 + (pebbleDensity - optimalDensity) * concentrationModifier`
  - Spikes when salt is extracted.
* `computePressure()` — compute pressure and set `portsLocked` based on thresholds.
* `onPortsLocked()` / `onPortsUnlocked()` — visual/audio events for client updates.
* `onSaltExtracted()` — optional event fired when salt is extracted (for HUD warnings).
* `serializeNBT()` / `deserializeNBT()` — persist full state.
* `getGuiData()` — send monitored values to client (include pebble density).

### Pump integration

Implement pumps to call the BE's input/output APIs. When sealed (portsLocked), pumps simply get 0 transfer and optionally the client shows a tooltip "Blocked by pressure".

### Threading & rate

Consider updating the heavy physics every `N` ticks (e.g., every 5 ticks) and scale rates accordingly. This reduces CPU overhead.

---

## 11.1 NuclearCraft-Neoteric Architectural Integration

### Recommended File Structure

```
src/main/java/igentuman/nc/block/msr/
  ├── MSRControllerBlock.java           # Block class extending MultiblockControllerBlock
  ├── entity/
  │   └── MSRControllerBE.java          # Main block entity with physics simulation
  ├── MSRMultiblock.java                # Multiblock definition & validation
  └── MSRRegistration.java              # Registry bindings (similar to FissionReactorRegistration)

src/main/java/igentuman/nc/compat/
  ├── cc/
  │   └── MSRPeripheral.java            # ComputerCraft peripheral (copy pattern from FissionReactor)
  └── oc2/
      └── MSRDevice.java                # OpenComputers 2 device (copy pattern from FissionReactorDevice)

src/main/java/igentuman/nc/client/gui/
  └── msr/
      └── MSRControllerScreen.java      # GUI with gauges and time-series graph

src/generated/resources/
  ├── assets/nuclearcraft/blockstates/msr_reactor_controller.json
  ├── assets/nuclearcraft/models/block/msr_reactor_controller.json
  ├── assets/nuclearcraft/models/item/msr_reactor_controller.json
  └── data/nuclearcraft/tags/blocks/msr_*.json
```

### Config Integration

Add to `FissionConfig.java` or create `MSRConfig.java`:

```java
public static class MSRConfig {
    public final ForgeConfigSpec.ConfigValue<Double> PRESSURE_MAX;
    public final ForgeConfigSpec.ConfigValue<Double> PRESSURE_UNLOCK;
    public final ForgeConfigSpec.ConfigValue<Double> PRESSURE_PER_DEGREE;
    public final ForgeConfigSpec.ConfigValue<Double> PRESSURE_PER_DEPLETED_MB;
    public final ForgeConfigSpec.ConfigValue<Integer> MIN_SIZE;
    public final ForgeConfigSpec.ConfigValue<Integer> MAX_SIZE;
    public final ForgeConfigSpec.ConfigValue<Double> EXPLOSION_RADIUS;
    // ... register in builder
}
```

### Fluid System Integration

MSR fuels are already defined in `NCFuel` system (from config JSON):
* Use `FuelManager.all()` to access registered fuels
* For molten salt conversion: create `MSRFuel` wrapper around `FuelDef` with fluid equivalents
* Store fuel fluid tags in `src/generated/resources/data/forge/tags/fluids/`

Example fuel properties mapping:

```
Standard Fuel (forge_energy: 1500, heat: 2.0)
  → Molten Salt Fuel (power: 1.5 RF/mB, heat: 0.002 °C/mB/tick)
  → Criticality: 150 units from base FuelDef
```

### Energy System (CustomEnergyStorage)

MSR will output FE through standard Forge capabilities:
* Capacity: configurable (suggest 500k-1M RF for mid-game)
* Tier: use GT-compatible tier or vanilla RF
* Output amperage: 16 (from FissionControllerBE pattern)
* Example: `10 RF/mB fuel * fuelVolume * reactivity * feGenerationMultiplier`

### Particle System (from NcParticleTypes.RADIATION)

Emit radiation particles during operation:
* Normal: occasional green particles
* Overheated: red/orange particles
* Pressure-locked: blue/white particles (indicator)
* Meltdown: violent particle burst

Use existing `NCParticles` system already in mod.

### Sound Events

Add to `NCSounds.java`:
* `MSR_OPERATION` — loop sound during normal operation
* `MSR_PRESSURE_LOCK` — alert when ports lock
* `MSR_EMERGENCY` — alarm sound when approaching meltdown

Use existing patterns from `FissionControllerBE` (plays `FISSION_REACTOR` sound).

### Recipe Integration (JEI/EMI)

For recipes showing fuel conversion to molten salt:
* Add `MSRFuelConversion` recipe type extending `NcRecipe`
* Register in `JEIPlugin.getRecipeTypes()`
* Example: "Uranium-235 Pebble → Molten Uranium-235 (1500 mB)"

Use existing `IngredientCreatorAccess` for fluid representations.

---

## 12. Balancing suggestions and example scenarios

Tuning is crucial. Start with conservative numbers and iterate playtests.

### Example scenario values (per small reactor, gameplay-scale)

* `baseCriticalVolume` = 8000 mB (8 buckets) — reactor needs ~8 buckets equivalent fissile mix to go critical.
* `powerPerMB` average = 0.6 RF/t per mB at reactivity=1.0.
* `heatPerMB` average = 0.05 °C per tick per mB.
* `depletionRatePerTick` average = 0.00012 (so ~1% depletion in ~83 ticks)
* `pressurePerDepletedMB` = 0.015
* `pressurePerDegree` = 0.025
* `pressureMax` = 100, `pressureUnlock` = 85
* `maxTemperature` = 1400°C

#### Playtest expectations

* A mid-run will require extracting depleted fuel every few minutes of real time depending on pump rates and reactor size.
* Players learn rhythm: run high-power bursts (push to ~80% pressure), then extract and refill.

---

## 13. Optional expansions and variants

* **Allow emergency overrides:** a costly procedure that temporarily allows extraction while overpressure but with consequences (radiation leak, block damage) — adds dramatic decisions.
* **Introduce passive internal gas capture:** a built-in upgrade that slowly converts `depletedVolume` pressure effect into a gas buffer that can be purged more safely.
* **Multiple loop reactor variant:** primary (fuel) + secondary (transfer salt) where secondary loop pressure is what locks pumps (more complex). Use if you want higher simulation fidelity.

---

## Closing notes

This model keeps player interactions simple (pumps + timing) while producing emergent, engaging operator gameplay. Pressure-lock as a flow control gate makes the mechanic tactile and teaches players to plan and automate. The numbers given are starting points; playtesting will tell you which constants need tightening.