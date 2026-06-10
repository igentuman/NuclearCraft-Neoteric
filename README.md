# NuclearCraft-Neoteric

Re-creation of the classic NuclearCraft mod for modern Minecraft (Forge 1.20.1, Java 17).
NuclearCraft-Neoteric brings a full nuclear-tech progression chain to the game: ore processing,
fission reactors with configurable fuels and heat sinks, molten salt reactors, fusion cores,
particle accelerators, target chambers, kugelblitz (black-hole) reactors, steam turbines,
in-situ leaching, and a chunk-based world radiation system that actually rewards (and punishes)
how you build.

[![Build Mod Job](https://github.com/igentuman/NuclearCraft-Neoteric/actions/workflows/gradle.yml/badge.svg?branch=1.20)](https://github.com/igentuman/NuclearCraft-Neoteric/actions/workflows/gradle.yml)
[![Downloads](https://cf.way2muchnoise.eu/full_840010_downloads.svg)](https://www.curseforge.com/minecraft/mc-mods/nuclearcraft-neoteric)
[![MC Versions](https://cf.way2muchnoise.eu/versions/840010.svg)](https://www.curseforge.com/minecraft/mc-mods/nuclearcraft-neoteric)

## Overview

- **Mod id:** `nuclearcraft`
- **Minecraft:** 1.20.1 (Forge)
- **Energy:** Forge Energy (FE/RF); Mekanism and GregTech CE: Unofficial interop where present
- **Integrations:** JEI, EMI, KubeJS, ComputerCraft, OpenComputers v2, AE2, Refined Storage, Mekanism, GregTech CE:U, TIS-3D, Patchouli, Ponder
- **In-game guide:** Patchouli Operator's Handbook
- **Downloads:** https://curseforge.com/minecraft/mc-mods/nuclearcraft-neoteric
- **Dev builds:** GitHub Actions tab → latest artifact

## Documentation

System reference docs live in [`docs/`](docs):

- [Fission Reactor](docs/FISSION_REACTOR.md) — multiblock layout, heat sinks, moderators, fuels, steam mode, meltdown
- [Fusion Reactor](docs/FUSION_REACTOR.md) — toroidal ring, magnets, RF amplifiers, plasma, coolant recipes
- [Turbine](docs/TURBINE.md) — rotor shafts, blades, coils, steam/exhaust recipes, energy formula
- [Kugelblitz](docs/KUGELBLITZ.md) — 9×9×9 chamber, photon ignition, mass feed, evaporation power
- [Accelerators](docs/ACCELERATORS.md) — linear & toroidal beam lines, magnets, RF cavities, particle sources
- [Target Chamber](docs/TARGET_CHAMBER.md) — beam reactions, detectors, transmutation, antimatter chains
- [Leacher](docs/LEACHER.md) — in-situ ore leaching, pumps, research papers, ore-vein system
- [Computers](docs/COMPUTERS.md) — ComputerCraft and OpenComputers v2 peripherals
- [KubeJS Support](docs/KUBEJS_SUPPORT.md) — recipe schemas, events, custom fuel and particle-source registration
- [Radiation](docs/RADIATION.md) — chunk radiation, decay, shielding, player effects, geiger counter

Modpack/scripting references:
- [Custom Fuels Guide](docs/MODPACK_DEVELOPER_GUIDE_CUSTOM_FUELS.md)
- [Particle KubeJS Integration](docs/PARTICLE_KUBEJS_INTEGRATION.md)

## Credits

[tomdodd4598](https://github.com/tomdodd4598) — original NuclearCraft

[Lach01298](https://github.com/Lach01298) — original QMD

## Support

If you want to support the project: [Patreon](https://patreon.com/igentuman) · [PayPal](https://paypal.me/igentuman)

### Special thanks to patrons
```
Noteclip
Wandering Singularity
Tom Dodd
Niv
Commander Ava
marcin212
endleon tiozae
Ethan Tabler
PersonBelow Rocks
```
