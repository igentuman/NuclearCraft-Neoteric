# KubeJS Support

NuclearCraft-Neoteric ships first-class KubeJS integration via `NuclearCraftKubeJSPlugin`
(`src/main/java/igentuman/nc/compat/kubejs/`). Every recipe type is registered as a generic
recipe schema, plus dedicated events for fuels, particle sources, and gameplay hooks.

## Recipe Types

All entries of `NcRecipeType.ALL_RECIPES` are registered under the `nuclearcraft` namespace.
This includes — non-exhaustive:

- `fission_reactor_controller`
- `target_chamber` *(extended schema with `crossSection`, `maxEnergy`)*
- `particle_chamber`
- `turbine_controller`
- `fusion_core`
- `fusion_coolant`
- Processors: `alloy_furnace`, `centrifuge`, `chemical_reactor`, `decay_hastener`,
  `electric_furnace`, `electrolyser`, `enricher`, `extractor`, `fluid_enricher`,
  `fluid_infuser`, `fuel_reprocessor`, `ingot_former`, `isotope_separator`, `manufactory`,
  `melter`, `ore_washer`, `pressurizer`, `salt_mixer`, `supercooler`, `leacher`, `pump`, ...

## Common Recipe Keys (`NCRecipeJS`)

| Key | Purpose |
|---|---|
| `input` | Item input(s) |
| `inputFluids` | Fluid input(s) |
| `inputParticles` | Particle input(s) — for accelerator/target recipes |
| `output` | Item output(s) |
| `outputFluids` | Fluid output(s) |
| `outputParticles` | Particle output(s) |
| `powerModifier` | Multiplier on energy/tick |
| `timeModifier` | Multiplier on process time |
| `radiation` | Per-tick radiation contribution |
| `crossSection` | *(target_chamber only)* reaction cross-section |
| `maxEnergy` | *(target_chamber only)* upper bound on incoming particle energy |

### Basic recipe example

```js
ServerEvents.recipes(event => {
  event.recipes.nuclearcraft.manufactory({
    input: ['minecraft:iron_ingot'],
    output: ['minecraft:iron_nugget#9'],
    timeModifier: 1.0,
    powerModifier: 1.0
  });
});
```

### Target chamber recipe with particles

See [`PARTICLE_KUBEJS_INTEGRATION.md`](PARTICLE_KUBEJS_INTEGRATION.md) for the full guide.

```js
ServerEvents.recipes(event => {
  event.recipes.nuclearcraft.target_chamber({
    input: ['nuclearcraft:beryllium_ingot'],
    inputParticles: [{ particle: 'proton', amount: 1000, energy: 5000000, focus: 50 }],
    output: ['nuclearcraft:lithium_6_ingot'],
    outputParticles: [{ particle: 'alpha', amount: 1000, energy: 100000, focus: 30 }],
    crossSection: 1.0,
    maxEnergy: 50000000
  });
});
```

## Events (`NCKJSEvents`)

### `RegisterFissionFuel` (startup)

Auto-generates a complete fuel pack: fuel item, depleted variants (`_ox`, `_ni`, `_za`),
TRISO variant, and fission reactor recipes. **Processing-chain recipes are NOT auto-generated.**

```js
NCEvents.registerFissionFuel(event => {
  event.registerFuel(
    'plutonium',     // group
    'plutonium_241', // name
    600,             // forge_energy
    300,             // heat
    1500,            // criticality
    25000,           // depletion
    1.05,            // efficiency
    'plutonium',     // isotope1
    'plutonium_241'  // isotope2
    // optional: timeModifier, powerModifier, radiationModifier
  );
});
```

See [`MODPACK_DEVELOPER_GUIDE_CUSTOM_FUELS.md`](MODPACK_DEVELOPER_GUIDE_CUSTOM_FUELS.md)
for the full walkthrough.

### `registerParticleSourceItem` / `registerParticleSourceFluid` (startup)

Map an item or fluid to a particle stack emitted by the accelerator ion-source port.

```js
NCEvents.registerParticleSourceItem(event => {
  event.add('minecraft:gold_ingot', 'proton', 1000, 5000000, 50);
  //         item                    particle  amount mean_energy focus
});

NCEvents.registerParticleSourceFluid(event => {
  event.add('mekanism:hydrogen', 'proton', 1000);
});
```

### `PlayerEnterBlackhole` (server, cancellable)

Fires when a player enters the event horizon of a [Kugelblitz](KUGELBLITZ.md).

```js
NCEvents.playerEnterBlackhole(event => {
  const player = event.getPlayer();
  const pos    = event.getBlackholePos();
  const level  = event.getLevel();
  // Cancel to deny entry:
  event.cancel();
});
```

## Runtime Recipe Injection

The plugin calls `injectRuntimeRecipes` after fuel events, invalidating the
`NcRecipeType` cache and injecting generated custom-fuel recipes so they show up in JEI/EMI
without a world reload.

## Related Docs

- [`PARTICLE_KUBEJS_INTEGRATION.md`](PARTICLE_KUBEJS_INTEGRATION.md) — particle/target chamber example
- [`MODPACK_DEVELOPER_GUIDE_CUSTOM_FUELS.md`](MODPACK_DEVELOPER_GUIDE_CUSTOM_FUELS.md) — full fuel pack guide
