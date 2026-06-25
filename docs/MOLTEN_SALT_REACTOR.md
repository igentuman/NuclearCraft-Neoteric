# Molten Salt Reactor (MSR)

The Molten Salt Reactor is a **liquid-fuel** fission multiblock. Instead of arranging heat sinks and
moderators like the solid-fuel [Fission Reactor](FISSION_REACTOR.md), you pump a **carrier salt**
through a chamber packed with **fuel pebbles** and manage two coupled stats — **temperature** and
**reactivity** — while the chain reaction runs.

The MSR does **not** output Forge Energy directly. It converts cold carrier salt into **hot carrier
salt**, and that hot salt is the product: pipe it to a [Heat Exchanger](HEAT_EXCHANGER.md) to bank
the heat, then drive a [Turbine](TURBINE.md). Pumping the hot salt out is also the reactor's **only
cooling** — stop pumping and the core overheats.

> The MSR shares the fission block family: it reuses `fission_reactor_casing` / `fission_reactor_glass`
> for its shell and lives under the same registry/recipe namespace (`nuclearcraft:msr_controller`).

## Multiblock Structure

- **Shape:** cuboid shell. Bounds are the fission `MIN_SIZE + 2` up to `MAX_SIZE` on every axis
  (default **5×5×5** up to **26×26×26** — the controller tooltip reports the exact bounds in-game).
- **Edges/corners:** `fission_reactor_casing`.
- **Walls:** `fission_reactor_casing` and/or `fission_reactor_glass`. Ports, the controller and an
  optional `irradiator` sit in the walls.
- **Controller:** exactly one `msr_controller`.
- **Interior:** filled **entirely** with `msr_fuel_cell`. No other interior block is allowed — any
  foreign interior block fails validation (`WRONG_INNER`). The interior fuel-cell count drives the
  reactor's volume and heat budget.

### Reference build (7×7×7)

The bundled structure (`molten_salt_reactor.nbt`) is a **7×7×7** cube: a `fission_reactor_casing`
edge frame with `fission_reactor_glass` wall panels, a solid **5×5×5 core of `msr_fuel_cell`** (125
cells), **one `msr_controller`** and **one `irradiator`** stacked on one wall, and **four
`msr_port`** — two on each of the opposite side walls.

### Block list

| Block | Registry id | Role |
|---|---|---|
| Controller | `nuclearcraft:msr_controller` | Brain; runs the simulation, owns the salt tanks and pebble slots |
| Fuel Cell | `nuclearcraft:msr_fuel_cell` | Interior block; the only allowed inner block. Count sets volume & heat budget |
| Port | `nuclearcraft:msr_port` | Item + fluid I/O (pebbles, salt). Proxies the controller's tanks/slots |
| Casing | `nuclearcraft:fission_reactor_casing` | Shell; edges/corners must be casing |
| Glass | `nuclearcraft:fission_reactor_glass` | Shell window |
| Irradiator | `nuclearcraft:irradiator` | Optional wall block; contributes irradiation |

## Fuel: TRISO pebbles

Fuel is supplied as **TRISO pebble items** (the `_tr` fuel variants), not as a fluid. The controller
only accepts items whose subtype is `_tr`; anything else is rejected.

- **Input:** live pebbles, e.g. `nuclearcraft:fuel_thorium_tbu_tr`, `nuclearcraft:fuel_uranium_leu_235_tr`,
  `nuclearcraft:fuel_uranium_heu_235_tr`, `nuclearcraft:fuel_plutonium_lep_239_tr`,
  `nuclearcraft:fuel_plutonium_hep_239_tr`, `nuclearcraft:fuel_americium_lea_242_tr`, … (a `_tr`
  variant exists for every fission fuel).
- **Output:** depleted pebbles, e.g. `nuclearcraft:depleted_fuel_thorium_tbu_tr`.

The `nuclearcraft:msr_controller` recipe type maps each input pebble to its depleted counterpart;
recipes are generated under `data/nuclearcraft/recipes/msr_controller/`.

Pebble capacity equals the interior fuel-cell count.

## Salt

| Fluid | Registry id | Role |
|---|---|---|
| FLiBe Molten Salt | `nuclearcraft:flibe_molten_salt` | **Cold** carrier salt, pumped **in** |
| Hot FLiBe Molten Salt | `nuclearcraft:flibe_hot_molten_salt` | **Hot** carrier salt, pumped **out** |

There is no separate coolant fluid: FLiBe is both the fission carrier and the heat-transport medium.
The simulation converts cold FLiBe into hot FLiBe in place; the controller pulls cold salt into tank
0 and pushes hot salt out of tank 1.

## Simulation

Two coupled stats drive everything, recomputed each server tick while the reactor is formed and
enabled:

- **Reactivity** — strength of the chain reaction, summed from the loaded pebbles' criticality. It
  has a **negative temperature coefficient**: as the core heats up, reactivity falls, so the reactor
  tends to self-stabilize at an equilibrium temperature rather than running away.
- **Temperature** — rises with fission heat, falls as hot salt is pumped out. Capped at
  `MAX_TEMPERATURE = 2000 K`.
- **Impurity** — fission-product poisoning that accumulates as pebbles deplete and drags reactivity
  down. Swapping salt clears it.
- **Depletion** — burn progress of the loaded pebbles; at full depletion a pebble becomes a depleted
  pebble in the output slot.
- **isCritical** — whether fission is currently self-sustaining (enough pebbles, enough salt,
  reactivity high enough, controller enabled).

`maxHeat` scales with the chamber volume. If `temperature` reaches `MAX_TEMPERATURE`, the core melts
down: the fuel cells are converted to **corium** and the usual fission explosion/radiation handling
fires.

## Ports & I/O

There is **one** port block type (`msr_port`); it proxies the controller's combined item and fluid
capabilities, so every port can carry both salt and pebbles. Direction is fixed at the controller's
tanks/slots rather than configured per port:

- **Cold salt in** → tank 0 (`flibe_molten_salt`).
- **Hot salt out** → tank 1 (`flibe_hot_molten_salt`).
- **Pebbles in** → fuel slot.
- **Depleted pebbles out** → output slot.

Use pipes/hoppers from any mod (Mekanism, vanilla hoppers, etc.) against a port face.

### Salt rates

The controller GUI exposes two rates, both in **buckets/tick**:

- **Salt input rate** — how much cold FLiBe is drawn in per tick.
- **Salt output rate** — how much hot FLiBe is pumped out per tick. This is the reactor's **only
  cooling**. Set it too low and the core overheats.

### Redstone

`msr_port` emits an analog (comparator) signal. The signal source toggles between:

- **Temperature** (default) — scaled `0…2000 K`.
- **Depletion** — burn progress.

## Config (`msr_reactor`)

| Key | Default | Meaning |
|---|---|---|
| `volume_per_fuel_cell` | 5000 | mB of internal salt volume contributed per interior fuel cell |
| `pebbles_per_fuel_cell` | 10 | Intended pebbles-per-cell budget (reserved; current capacity = fuel-cell count) |

Reactor size bounds come from the shared fission config (`MIN_SIZE` / `MAX_SIZE`).

## Computer control

The controller is exposed to ComputerCraft and OpenComputers v2 — peripheral type / device name
`nc_msr_reactor`. The two surfaces are **not** symmetric here: OC2 exposes the full metric set
(temperature, reactivity, volumes, pebble/cell counts, enable/disable), while the CC peripheral is a
smaller control surface. See [Computers](COMPUTERS.md#nc_msr_reactor---msrcontrollerperipheral).

## Typical loop

1. Build the cuboid shell, fill the interior with `msr_fuel_cell`, add one `msr_controller` and at
   least one `msr_port`.
2. Pipe **cold FLiBe** into a port and load **TRISO pebbles**.
3. Apply redstone / enable via computer. With enough pebbles and salt, the core goes **critical**,
   heats up, and starts turning cold salt into **hot salt**.
4. Set the **salt output rate** high enough to keep the temperature off the ceiling, and pump the
   **hot FLiBe** to a [Heat Exchanger](HEAT_EXCHANGER.md) (recipe `flibe_hot_molten_salt →
   flibe_molten_salt`, +600 heat) → [Turbine](TURBINE.md) for power.
5. Pull **depleted pebbles** out of the output slot; swap salt when impurity climbs.

## Implementation notes

- The MSR is a **heat/coolant source**, not a direct FE generator — power comes from the
  hot-salt → heat-exchanger → turbine chain.
- FLiBe doubles as carrier and coolant; there is no dedicated coolant salt yet.
- There is no in-mod reprocessor; depleted pebbles are an output item for downstream use.
