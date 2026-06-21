# Molten Salt Reactor (MSR) — Gameplay & Implementation Design

> Design document for the MSR controller in NuclearCraft-Neoteric.
> Focus: active **operation** of a liquid-fuel reactor where the two coupled stats — **temperature** and **reactivity** — drive everything, and a **shared internal volume** (salt + coolant + pebbles) is the resource the player must manage. Single port, no pumps, no pressure-lock control.

---

## 1. Overview

The MSR is a closed reactor that shifts gameplay from static block design (heatsinks/moderators) to **flow and volume management**. Its interior is built from **MSR fuel cell blocks only**.

It holds:

* **Molten salt (FLiBe)** — fluid carrier medium for fission. Pumped in via pipes through the single port.
* **Coolant salt** — fluid heat-removal medium. *Not yet implemented* — intended to be a boron compound (sodium fluoroborate, NaBF₄–NaF). For now the coolant tank accepts FLiBe as a placeholder.
* **TRISO fuel pebbles** — items inserted into the chamber. Each pebble occupies a fixed volume (1 pebble = 100 mB).

The reactor has **one global internal volume**. Salt, coolant, and pebbles all draw from it. When it is full, nothing more can be inserted until the player extracts liquid or pulls depleted pebbles out.

Two stats matter and feed back into each other every tick:

* **Temperature** — rises with fission heat, falls with cooling.
* **Reactivity** — strength of the chain reaction; has a **negative temperature coefficient** (hotter → less reactive), so the reactor self-stabilizes.

`pressure` still exists but is a **simulated, display-only gauge** — it no longer locks anything.

---

## 2. High-level gameplay goals

* Active operator experience: watch temperature/reactivity, feed salt and pebbles, pull waste, keep volume from filling.
* Self-stabilizing core (NTC) that rewards understanding rather than twitch reactions — but still melts down if grossly overfueled with no cooling.
* Per-pebble fuel life: high-enriched fuels burn hot and fast, low-enriched fuels run long and steady.
* Volume as the central constraint: you cannot top off everything at once; trade salt headroom against coolant headroom against pebble count.
* Reward automation (ComputerCraft / OC / redstone) for steady-state operation and waste cycling.

---

## 3. Core systems & variables

Each reactor instance simulates a compact set of variables that interact each tick (or every N ticks).

### Core variables (stored in BE)

* `pebbleCount` (count) — live (still-fissioning) TRISO pebbles in the chamber.
* `depletedPebbleCount` (count) — burned-out pebbles still occupying volume until extracted.
* `saltVolume` (mB) — FLiBe carrier salt currently inside.
* `coolantVolume` (mB) — coolant salt currently inside (placeholder fluid for now).
* `temperature` (°C) — core temperature (double).
* `reactivity` (≥0) — chain-reaction multiplier; product of base criticality and feedbacks.
* `impurity` (0..1) — fission-product poisoning of the salt; reduced by swapping salt.
* `pressure` (display only) — derived stress indicator, no control effect.
* `isCritical` (bool) — whether fission is currently self-sustaining.

### Volume model (the central constraint)

```
pebbleVolume   = (pebbleCount + depletedPebbleCount) * VOLUME_PER_PEBBLE   # 100 mB each
occupiedVolume = saltVolume + coolantVolume + pebbleVolume
freeVolume     = globalVolume - occupiedVolume
```

* `globalVolume` is derived from reactor size: `globalVolume = fuelCellsCount * VOLUME_PER_FUEL_CELL`.
* Individual tanks have their own caps (`maxSaltVolume`, `maxCoolantVolume`), but the **global cap is the real limit**.
* Example: `globalVolume = 15000`, salt tank cap `10000`, coolant tank cap `10000` → you can never fill both tanks fully; pebbles eat into the same budget.
* Any insert (salt, coolant, or pebble) is rejected when it would exceed `freeVolume`. The player must extract liquid or pull depleted pebbles to make room.

### Derived/auxiliary parameters (per fuel type)

For each TRISO pebble type (from `FuelDef` / `ItemFuel`):

* `forge_energy` — FE/tick per pebble at nominal reactivity.
* `heat` — heat/tick per pebble at nominal reactivity.
* `criticality` — base neutron multiplication contribution.
* `depletion` — burn-rate basis (ticks-to-deplete derived from it).
* `efficiency` — power-generation efficiency modifier.
* `enrichmentClass` — `HE` (high-enriched) or `LE` (low-enriched); selects the criticality-decay exponent.

Fuel mixing: aggregate reactivity sums per-pebble effective criticality; heat/power sum per pebble.

---

## 4. Numerical model (equations and example constants)

Compact, implementable model. Temperature and reactivity are the two coupled stats; everything else feeds them.

### Example constants (tuned starting points)

```text
# Temperature & feedback
T_ambient        = 20.0       # °C
T_ref            = 650.0      # °C, NTC feedback-neutral operating point
maxTemperature   = 2000.0     # °C, meltdown threshold
alphaT           = -0.0008    # /°C, NEGATIVE temperature coefficient of reactivity
thermalMassBase  = 1000000.0  # base heat capacity (salt+coolant add inertia on top)

# Reactivity
criticalityThreshold = 1000.0 # Σ effective criticality needed for baseReactivity = 1.0
poisonCoeff          = 1.0    # impurity → reactivity penalty weight
optimalDensity       = 0.025  # pebbles per mB of salt (concentration sweet spot)
concentrationModifier= 0.08   # how much density deviation shifts reactivity

# Cooling
coolingEfficiency    = 0.9    # coolant heat-removal effectiveness
coolingDivisor       = 10000.0# scales coolant*ΔT into heat units

# Fuel burn / non-linear criticality decay
gammaHE = 2.0                 # high-enriched: criticality drops fast with burnup
gammaLE = 0.5                 # low-enriched: criticality holds longer
impurityRatePerPebble = 0.001 # impurity added when a pebble fully depletes

# Volume
VOLUME_PER_PEBBLE     = 100   # mB occupied per pebble
VOLUME_PER_FUEL_CELL  = 500   # mB of global volume contributed per fuel cell block
maxSaltVolume         = 10000 # mB salt tank cap (individual)
maxCoolantVolume      = 10000 # mB coolant tank cap (individual)
minSaltForCriticality = 500   # mB minimum salt to sustain reaction (FIXED floor — never scaled by reactor size)
minPebblesForCriticality = 10 # pebble count minimum for criticality (FIXED floor — never scaled by reactor size)

# Energy conversion (matches mod FE system)
feGenerationMultiplier = 10.0 # from FISSION_CONFIG.FE_GENERATION_MULTIPLIER
baseEnergyTier         = 4    # GT-compatible output tier
```

### Fuel system: FLiBe + TRISO pebbles

1. **Molten salt (FLiBe, fluid)** — carrier medium. Must be present for the chain reaction and for heat transport.
2. **TRISO pebbles (items)** — contain the fissile material. Each occupies 100 mB of the global volume.

The reaction sustains only when, **in order of importance**:
- aggregate `baseReactivity >= 1.0` — the **primary gate**, driven by the summed effective criticality of the loaded pebbles (`Σ effCriticality / criticalityThreshold`). Criticality is a property of the **fuel**, not the reactor size.
- `pebbleCount >= minPebblesForCriticality` — a **fixed floor**, never scaled by reactor/chamber size.
- `saltVolume >= minSaltForCriticality` — a **fixed floor**, never scaled by reactor/chamber size.
- the controller is enabled (redstone/controller).

### Fuel pebble properties

```java
forge_energy    // FE/tick per pebble at nominal reactivity
heat            // heat/tick per pebble at nominal reactivity
criticality     // base neutron-multiplication contribution
depletion       // burn-rate basis (→ ticks to deplete)
efficiency      // power efficiency modifier
enrichmentClass // HE or LE → criticality-decay exponent
```

**Example pebble parameters** (illustrative):

| Pebble Type      | forge_energy | heat | criticality | depletion | efficiency | class | Notes |
|------------------|-------------:|-----:|------------:|----------:|-----------:|:-----:|-------|
| Thorium-232 TR   | 800          | 1.2  | 80          | 150       | 95         | LE    | Breeder, long life |
| Uranium-235 TR   | 1500         | 2.0  | 150         | 80        | 90         | LE    | Standard fission |
| Plutonium-239 TR | 2000         | 2.5  | 200         | 120       | 92         | HE    | High energy, fades fast |
| Americium-242 TR | 2200         | 2.8  | 220         | 140       | 85         | HE    | Exotic, hottest, shortest |

### Per-pebble non-linear criticality decay

Each pebble tracks burnup `b ∈ [0,1]`. Its **effective** criticality declines non-linearly, with the exponent set by enrichment class:

```text
effCriticality_i = criticality_i * (1 - b_i) ^ gamma(class_i)
   gamma(HE) = gammaHE (≈2.0)  → convex: drops fast
   gamma(LE) = gammaLE (≈0.5)  → concave: holds longer
```

Burnup advances faster when the reactor runs hot/reactive:

```text
b_i += depletionPerTick_i * reactivity
depletionPerTick_i derived from fuel `depletion` (e.g. 1 / (depletion * 20))
```

Effect: HE pebbles deliver big early power then fade quickly (frequent reload); LE pebbles run long and flat. A mixed load lets the player shape the power curve.

### Coupled temperature ↔ reactivity (the core loop)

**Reactivity → temperature** (reactivity sets the fission rate, which produces heat):

```text
heatProduced = Σ_pebbles ( heat_i * effCriticality_i * reactivity )
powerProduced= Σ_pebbles ( forge_energy_i * effCriticality_i * reactivity * efficiency_i )

heatRemoved  = coolantVolume * coolingEfficiency * (temperature - T_ambient) / coolingDivisor
netHeat      = heatProduced - heatRemoved
thermalMass  = thermalMassBase + saltVolume + coolantVolume
temperature += netHeat / thermalMass
temperature  = max(temperature, T_ambient)
```

**Temperature → reactivity** (negative temperature coefficient — the self-stabilizing feedback):

```text
baseReactivity  = (Σ effCriticality_i) / criticalityThreshold      # ≥1.0 ⇒ supercritical tendency
thermalFeedback = clamp(1 + alphaT * (temperature - T_ref), 0, 2)  # hotter ⇒ lower; colder ⇒ higher
poisonFeedback  = 1 - impurity * poisonCoeff
pebbleDensity   = pebbleCount / max(saltVolume, 1)
concFactor      = 1 + (pebbleDensity - optimalDensity) * concentrationModifier

reactivity = baseReactivity * thermalFeedback * poisonFeedback * concFactor
```

**Why this works (equilibrium intuition):**
- Cold start (`T < T_ref`): `thermalFeedback > 1` → reactivity high → heats quickly.
- As `T` rises past `T_ref`: `thermalFeedback < 1` → reactivity falls → heating slows.
- The core settles at an equilibrium `T*` where `heatProduced(T*) = heatRemoved(T*)`. Stable without micromanagement.
- **Player raises power** by adding pebbles (higher `baseReactivity` → higher `T*`, more heat + FE) or by **increasing cooling** (drops `T` → NTC raises reactivity → more fissions at the same temperature → more power). This is real load-following: pull more heat out and the reactor leans in.
- **Failure mode:** if `baseReactivity` is so high that even at high `T` (small but positive `thermalFeedback`) `heatProduced` exceeds maximum `heatRemoved`, temperature climbs to `maxTemperature` → meltdown. NTC saves mild excursions, not gross overfueling with dead cooling.

### Tick update pseudocode

```text
# 0. Volume / criticality gate
pebbleVolume = (pebbleCount + depletedPebbleCount) * VOLUME_PER_PEBBLE
isCritical = baseReactivity >= 1.0                     # PRIMARY gate: aggregate pebble criticality
          and pebbleCount >= minPebblesForCriticality  # fixed floor
          and saltVolume  >= minSaltForCriticality     # fixed floor
          and enabledByController

# 1. Reactivity (uses last tick's temperature)
baseReactivity  = sum(effCriticality_i) / criticalityThreshold
thermalFeedback = clamp(1 + alphaT*(temperature - T_ref), 0, 2)
poisonFeedback  = 1 - impurity*poisonCoeff
concFactor      = 1 + (pebbleCount/max(saltVolume,1) - optimalDensity)*concentrationModifier
reactivity      = baseReactivity * thermalFeedback * poisonFeedback * concFactor

# 2. Fission: heat, power, per-pebble burnup
heatProduced = 0; powerProduced = 0
if isCritical:
    for pebble in pebbles:
        eff = pebble.criticality * (1 - pebble.b)^gamma(pebble.class)
        heatProduced  += pebble.heat * eff * reactivity
        powerProduced += pebble.forge_energy * eff * reactivity * pebble.efficiency
        pebble.b += pebble.depletionPerTick * reactivity
        if pebble.b >= 1.0:
            move pebble to depleted (item out queue); depletedPebbleCount += 1
            impurity = min(1, impurity + impurityRatePerPebble)

# 3. Temperature integrate (NTC closes the loop next tick)
heatRemoved = coolantVolume * coolingEfficiency * (temperature - T_ambient) / coolingDivisor
temperature += (heatProduced - heatRemoved) / (thermalMassBase + saltVolume + coolantVolume)
temperature  = max(temperature, T_ambient)

# 4. Energy output
energyPerTick = round(powerProduced * feGenerationMultiplier)
energyStorage.receive(energyPerTick)

# 5. Display-only pressure gauge (no lock)
pressure = (temperature - T_ambient)*0.015 + impurity*100

# 6. Meltdown check
if temperature >= maxTemperature:
    triggerCatastrophicFailure()
```

### Notes on tuning

* `alphaT` controls how strongly the core self-stabilizes. Larger magnitude (e.g. `-0.0015`) = stiffer, harder to push to high power; smaller (e.g. `-0.0003`) = looser, more meltdown risk.
* `T_ref` sets the natural operating temperature. Power output tracks the equilibrium `T*` above `T_ref`.
* `gammaHE`/`gammaLE` set the HE-vs-LE life curves. Widen the gap for sharper fuel-choice tradeoffs.
* `criticalityThreshold` sets how many pebbles (and how good) you need to go critical — the floor of the progression curve.
* `concentrationModifier` controls the density side-effect: pulling salt out raises `pebbleDensity` and nudges reactivity up. Keep modest so it is a lever, not a trap.
* `VOLUME_PER_FUEL_CELL` ties reactor size to how much headroom the player has to juggle salt/coolant/pebbles.

---

## 5. The single MSR port, volume & flow logic

There is **one MSR port block type**. There are **no pump blocks** — external pipes (Mekanism, vanilla hoppers for items, etc.) push/pull through the port. A port's role (which fluid/item, in or out) is configured on the port/controller, not by distinct block types.

### What flows through the port

* **Salt in** — FLiBe into `saltVolume`.
* **Coolant in** — coolant salt into `coolantVolume` (FLiBe placeholder until the boron coolant exists).
* **Salt out** — extract FLiBe (frees volume; raises `pebbleDensity` → mild reactivity bump; swap to cut `impurity`).
* **Coolant out** — extract coolant (frees volume).
* **Pebble in** — TRISO pebbles into the chamber (item).
* **Depleted pebble out** — burned-out pebbles (item), freeing their 100 mB.

### Volume gating (replaces pressure-lock)

There is **no port lock**. The only gate is free volume:

```text
freeVolume = globalVolume - (saltVolume + coolantVolume + (pebbleCount+depletedPebbleCount)*VOLUME_PER_PEBBLE)
accepted   = min(requested, freeVolume)   # for any insert
```

* Inserts are clamped to `freeVolume`; when `freeVolume == 0`, all inserts return 0 until the player extracts something.
* Extraction is always allowed (it is how you make room).
* Because individual tank caps (10000) sum above `globalVolume` (15000), the player must decide how to split the budget between salt headroom, coolant headroom, and pebble count.

### Salt extraction tradeoff

Pulling salt frees volume and lets you swap out poisoned salt (lowers `impurity`), but raising `pebbleDensity` nudges `concFactor` (and thus reactivity) upward. Net effect is small/manageable by design — it is a lever, not the old pressure-relief trap.

---

## 6. Player interactions and challenges (gameplay loop)

**Core loop:** load pebbles → add salt → reach criticality → let NTC settle temperature → tune cooling/pebbles for target power → pull depleted pebbles and swap salt to free volume and cut poisoning → repeat.

### Player activities

* **Pebble loading:** insert TRISO pebbles until past `minPebblesForCriticality` and `baseReactivity ≥ 1.0`. Mix HE/LE to shape the power curve.
* **Salt supply:** pipe FLiBe in. Salt is required for criticality and carries heat.
* **Cooling control:** add/remove coolant to set the equilibrium temperature. More cooling → NTC raises reactivity → more power (until you run out of fuel headroom).
* **Volume management:** the chamber fills with salt + coolant + pebbles + depleted pebbles. Keep `freeVolume` available or inputs stall.
* **Waste cycling:** extract depleted pebbles (frees 100 mB each) and send them to a Reprocessor. Swap poisoned salt to reset `impurity`.
* **Monitoring:** watch temperature, reactivity, free volume, pebble count/density, impurity, power output.

---

## 7. GUI / monitoring / player feedback

The GUI must show the living, self-balancing nature of the core.

### Suggested GUI elements

* **Top bar:** reactor name, status (Idle / Heating / Stable / Overheating / Meltdown).
* **Left column (gauges):**
    * Temperature (numeric + colored bar; mark `T_ref` and `maxTemperature`).
    * Reactivity (numeric; show whether thermal feedback is currently boosting or damping).
    * **Free volume** gauge (mB free / globalVolume) — the central resource.
    * Pressure gauge — clearly labeled **simulated / indicator only**.
* **Center (graph):** time-series (last 60 s) of Temperature (red), Reactivity (yellow), Power (green).
* **Right column (resources):**
    * Live pebble count + depleted pebble count + density (pebbles/mB).
    * Salt volume / cap, coolant volume / cap.
    * Impurity level.
* **Bottom controls:** enable/shutdown toggle, optional auto-extract-depleted toggle, automation API link.

### Warnings & audio

* Temperature approaching `maxTemperature`: flash catastrophic warning.
* Free volume near zero: "Chamber full — extract liquid or depleted pebbles."
* Impurity high: "Salt poisoned — swap salt to restore reactivity."

---

## 8. Emergency events & failures

* **Salt solidification (freeze):** if temperature falls below `freezeTemperature` (e.g. 450 °C) while fuel is loaded, some salt becomes non-circulating (temporarily removed from active volume / reactivity). Fix: reheat (let fission resume) or remix.
* **Catastrophic failure (meltdown):** if `temperature >= maxTemperature`, containment breach: localized explosion, block damage, radiation release, partial loss of BE state. Reuse `FissionControllerBE` explosion/radiation handling.

### Safety systems (optional)

* **Thermal-inertia upgrades:** raise `thermalMassBase` to slow temperature swings.
* **Emergency drain (manual):** dump most salt/pebbles into connected storage to kill the reaction fast.

---

## 9. Maintenance, fuel cycle & reprocessing

1. **Pebble loading:** TRISO pebbles inserted as items.
2. **Salt supply:** FLiBe piped in.
3. **Operation:** reaction self-sustains and self-stabilizes via NTC.
4. **Waste extraction:** pull depleted pebbles (frees volume); swap poisoned salt.
5. **Reprocessing:** depleted pebbles → **Reprocessor**, recovering a portion of fissile content (new pebbles / fissile salt), producing contaminants, costing power and time.

### Reprocessing tradeoffs

* **Efficiency:** recover 50–80% depending on tech tier.
* **Time & power cost:** multi-tick, power-hungry.
* **Volume pressure:** depleted pebbles occupy chamber volume until extracted — neglect it and inputs stall.

---

## 10. Progression and advanced mechanics

### Existing NuclearCraft systems to leverage

* **ComputerCraft: Tweaked** (`MSRPeripheral`, pattern from `SidedFissionReactorPeripheral`):
  `getTemperature()`, `getReactivity()`, `getPebbleCount()`, `getSaltVolume()`, `getCoolantVolume()`, `getFreeVolume()`, `getImpurity()`, `isCritical()`, plus enable/shutdown and extract-depleted controls.
* **OpenComputers 2** (`MSRDevice`, already stubbed): component access to the same metrics.
* **KubeJS** (`NCKubeJSEvents`): register custom pebble fuels with custom criticality/heat/decay class.
* **Mekanism:** pipes for fluid transfer; radiation-system integration.
* **Minecraft:** redstone enable/disable; hoppers for pebble I/O; JEI/EMI recipe display.

### Progression tiers

* **Early:** small reactor, LE uranium/thorium pebbles, manual operation.
* **Mid:** larger reactor (more fuel cells → more volume), HE plutonium fuels, real coolant salt once implemented.
* **Late:** automation via CC/OC, exotic HE fuels, breeding loops, thermal-inertia and volume upgrades.

### Tunable upgrades

* **More fuel cells:** raise `globalVolume` — more headroom and higher peak power.
* **Coolant salt (boron, NaBF₄–NaF):** better `coolingEfficiency` than FLiBe placeholder — once implemented.
* **Thermal-inertia upgrades:** raise `thermalMassBase` for gentler temperature response.
* **Breeding fuels:** certain LE/thorium pebbles slowly gain fissile content during operation.

---

## 11. Implementation notes: block entity design & methods

Structure `MSRControllerBE` following `FissionControllerBE` patterns. (Criticality and multiblock validation now match this spec; the legacy pressure-lock + 4-tank/4-port model is still the target to migrate toward — see the migration note below.)

### Core fields (use `@NBTField` for persistence)

```java
@NBTField public int    pebbleCount = 0;           // live pebbles
@NBTField public int    depletedPebbleCount = 0;   // burned, still occupying volume
@NBTField public double saltVolume = 0.0;          // mB FLiBe
@NBTField public double coolantVolume = 0.0;       // mB coolant salt (placeholder fluid)
@NBTField public double temperature = 20.0;        // °C
@NBTField public double reactivity = 0.0;          // chain-reaction multiplier
@NBTField public double impurity = 0.0;            // 0..1
@NBTField public double pressure = 0.0;            // display only
@NBTField public boolean isCritical = false;
@NBTField public boolean enabledByController = false;
@NBTField public boolean hasRedstoneSignal = false;
@NBTField public int    fuelCellsCount = 0;        // drives globalVolume + pebble cap

// derived (not persisted)
double globalVolume;       // fuelCellsCount * VOLUME_PER_FUEL_CELL
double freeVolume;         // globalVolume - occupied
double baseReactivity;     // Σ effCriticality / criticalityThreshold
double heatPerTick;
int    energyPerTick;
```

Per-pebble state lives in `ReactorPebble` (already present): track `burnedFraction b`, `criticality`, `heat`, `forge_energy`, `efficiency`, and `enrichmentClass` → decay exponent.

### Key methods

* `tickServer()` — orchestrates: `syncInternalState()` → `computeReactivity()` → `updateFission()` → `updateTemperature()` → `updatePressure()` (display) → `checkFailures()` → energy store → block-state update.
* `computeReactivity()` — `baseReactivity * thermalFeedback * poisonFeedback * concFactor` (equations in §4).
* `updateFission()` — per-pebble loop: accumulate heat/power from `effCriticality * reactivity`, advance burnup, retire depleted pebbles into `depletedPebbleCount`, bump `impurity`.
* `updateTemperature()` — integrate `(heatProduced - heatRemoved)/thermalMass`; clamp to `[T_ambient, maxTemperature]`.
* `updatePressure()` — derived gauge only, no lock.
* `checkFailures()` — meltdown at `maxTemperature`; auto-shutdown if below criticality; salt-freeze event.
* **Volume API** (replaces pressure gating):
  * `freeVolume()` — `globalVolume - occupied`.
  * `tryInputFluid(FluidStack, type)` — clamp to `freeVolume`; route to salt or coolant tank; return accepted.
  * `tryOutputFluid(amount, type)` — extract salt/coolant; always allowed.
  * `tryInsertPebble(ItemStack)` — accept if `freeVolume >= VOLUME_PER_PEBBLE` and under pebble cap.
  * `tryExtractDepletedPebble()` — pop a depleted pebble item, free 100 mB.
* `getMaxPebbleCapacity()` — `fuelCellsCount * PEBBLES_PER_FUEL_CELL`.

### Multiblock structure

* Controller block (with pebble in / depleted out slots in its GUI).
* **Interior: MSR fuel cell blocks only** (no heatsinks/moderators). Count drives `globalVolume` and pebble capacity.
* Casing/frame blocks form the shell.
* **One MSR port block type** (mode-configurable) for all fluid/item I/O via pipes — no pump blocks.

### Migration note (current code → this spec)

**Done:** criticality and reactivity now match §4 — `baseReactivity = Σ effCriticality / CRITICALITY_THRESHOLD` is the primary criticality gate, with `MIN_PEBBLES_FOR_CRITICALITY` / `MIN_SALT_FOR_CRITICALITY` as **fixed floors** (chamber-volume scaling removed), layered with NTC `thermalFeedback`, impurity, and concentration factors. Multiblock structure validation (`MSRMultiblock`) is complete.

**Still pending:** `MSRControllerBE` retains `PRESSURE_MAX`/`portsLocked`, four 50000-mB tanks (salt/coolant/depleted-out/salt-out), and drains depleted to an `irradiated_sodium` fluid. To finish: remove pressure-lock gating, collapse to salt + coolant tanks under a shared `globalVolume`, replace depleted-salt fluid with depleted-pebble items + `depletedPebbleCount`, add the volume API, and refine per-pebble decay from the current linear `(1 - burnup)` to the `^gamma(class)` curve.

### Threading & rate

Run heavy physics every `N` ticks (e.g. 5) and scale rates accordingly to cut CPU cost.

---

## 11.1 NuclearCraft-Neoteric architectural integration

### File structure (current)

```
src/main/java/igentuman/nc/block/fission/MSRControllerBlock.java
src/main/java/igentuman/nc/block/fission/entity/MSRControllerBE.java
src/main/java/igentuman/nc/container/MSRControllerContainer.java
src/main/java/igentuman/nc/client/gui/fission/MSRControllerScreen.java
src/main/java/igentuman/nc/multiblock/fission/MSRMultiblock.java
src/main/java/igentuman/nc/multiblock/fission/MSRController.java
src/main/java/igentuman/nc/multiblock/fission/FissionReactorRegistration.java   # msrBlocks(), MSR_* maps
src/main/java/igentuman/nc/compat/oc2/MSRDevice.java
src/main/java/igentuman/nc/util/ReactorPebble.java
```

### Config integration (`MSRConfig`, in `FissionConfig`)

```java
public static class MSRConfig {
    public final ForgeConfigSpec.ConfigValue<Double>  T_REF;
    public final ForgeConfigSpec.ConfigValue<Double>  ALPHA_T;            // negative
    public final ForgeConfigSpec.ConfigValue<Double>  MAX_TEMPERATURE;
    public final ForgeConfigSpec.ConfigValue<Double>  CRITICALITY_THRESHOLD;
    public final ForgeConfigSpec.ConfigValue<Double>  GAMMA_HE;
    public final ForgeConfigSpec.ConfigValue<Double>  GAMMA_LE;
    public final ForgeConfigSpec.ConfigValue<Integer> VOLUME_PER_PEBBLE;
    public final ForgeConfigSpec.ConfigValue<Integer> VOLUME_PER_FUEL_CELL;
    public final ForgeConfigSpec.ConfigValue<Integer> PEBBLES_PER_FUEL_CELL;
    public final ForgeConfigSpec.ConfigValue<Double>  COOLING_EFFICIENCY;
    public final ForgeConfigSpec.ConfigValue<Integer> MIN_SIZE;
    public final ForgeConfigSpec.ConfigValue<Integer> MAX_SIZE;
    public final ForgeConfigSpec.ConfigValue<Double>  EXPLOSION_RADIUS;
}
```

### Fluids

* FLiBe already exists in `NCFluids` / `NC_MATERIALS`. Use it for `saltVolume`.
* Coolant salt (boron, NaBF₄–NaF) is a **future fluid** — register when implemented; until then the coolant tank accepts FLiBe.

### Energy

FE via `CustomEnergyStorage` (already wired): `energyPerTick = powerProduced * feGenerationMultiplier`, output tier 4, amperage 64.

### Particles / sound

Reuse `NCParticles` (radiation) and `NCSounds` (loop on operation, alarm near meltdown), following `FissionControllerBE`.

---

## 12. Balancing suggestions and example scenarios

Start conservative; iterate via playtests.

### Example small reactor

* `fuelCellsCount = 30` → `globalVolume = 15000` mB.
* Salt tank cap 10000, coolant tank cap 10000 (can't fill both).
* LE uranium load: ~20 pebbles → `baseReactivity ≈ 3` → stable around `T* ≈ 900–1100 °C` with moderate cooling.
* `maxTemperature = 2000 °C`.

### Playtest expectations

* Cold start ramps over ~10–20 s to a stable equilibrium; no oscillation thanks to NTC.
* Adding cooling visibly raises power (load-following) — the "aha" moment.
* HE pebbles spike power then fade in a fraction of LE pebble lifetime — players learn to blend.
* Neglecting depleted-pebble extraction fills volume and stalls salt/pebble input — the volume-management lesson.

---

## 13. Optional expansions and variants

* **Real coolant salt loop:** implement NaBF₄–NaF with higher `coolingEfficiency`; gate behind progression.
* **Breeding fuels:** LE/thorium pebbles slowly gain fissile content (negative effective burnup early).
* **Two-loop variant:** secondary transfer-salt loop for higher fidelity (more complex; optional).

---

## Closing notes

The MSR's identity is the **coupled temperature/reactivity pair** (self-stabilizing via a negative temperature coefficient) plus a **shared internal volume** the player constantly juggles. Single port, no pumps, no pressure-lock — the tension comes from feedback physics and volume budgeting, not artificial gates. Constants here are starting points; playtesting will tighten them.
