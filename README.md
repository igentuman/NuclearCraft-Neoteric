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

## Features

- **Ore processing chain** - crushers, washers, centrifuges, electrolyzers, infusers, melters and assemblers feeding a deep materials tree (uranium, thorium, plutonium, americium, curium, californium and beyond).
- **Fission reactors** - multiblock cores with configurable solid fuels, moderators, reflectors, heat sinks, steam mode, neutron flux, and meltdown consequences.
- **(WIP) Molten Salt Reactors** - liquid-fuel cores with input/output port logic, salt loops and thermal feedback.
- **Fusion reactors** - toroidal plasma ring built from electromagnets and RF amplifiers, fed by fuel/coolant recipes.
- **Particle accelerators** - linear and (WIP)toroidal beam lines with magnets, RF cavities and configurable particle sources.
- **Target chamber** - beam-driven transmutation, detector readouts, antimatter production chain.
- **Kugelblitz reactor** - 11×11×11 photon-ignition chamber for mini cool looking blackhole.
- **Steam turbines** - rotor shafts, blade tiers, coil materials, steam/exhaust recipes and a transparent energy formula.
- **In-situ leaching** - ore-vein system with leachers, pumps and research papers as an alternative to traditional mining.
- **World radiation** - chunk-level radiation field with decay, diffusion, shielding, player effects, dosimetry and geiger counters.
- **World generation** - new biome, new dimension with custom structures. Mini boss. Ores
- **Nuclear weapons** - Pu-239 / fission and fusion bombs with simulated blast, fallout and structural damage.
- **Energy & storage** - Forge Energy (FE/RF) throughout, with Mekanism and GregTech CE:U interop, plus voiding/buffering storage tiers.
- **Mod integrations** - JEI, EMI, AE2, Refined Storage, ComputerCraft, OpenComputers v2, TIS-3D, KubeJS, Patchouli, Ponder.
- **In-game guide** - Patchouli Operator's Handbook covering every multiblock and recipe path.

## Documentation

System reference docs live in [`docs/`](docs):

- [Fission Reactor](docs/FISSION_REACTOR.md) - multiblock layout, heat sinks, moderators, fuels, steam mode, meltdown
- [Fusion Reactor](docs/FUSION_REACTOR.md) - toroidal ring, magnets, RF amplifiers, plasma, coolant recipes. Decent energy source
- [Turbine](docs/TURBINE.md) - Produce energy with steam cycle
- [Kugelblitz](docs/KUGELBLITZ.md) - Confinment for blackhole. Harness energy and use unique recipe chains
- [Accelerators](docs/ACCELERATORS.md) - linear & toroidal beam lines, magnets, RF cavities, particle sources
- [Beam Diverter](docs/BEAM_DIVERTER.md) - 5×5×5 multiblock that redirects a particle beam between input and output ports
- [Particle Chambers](docs/PARTICLE_CHAMBERS.md) - Target, Decay and Collision chambers for beam reactions, detectors, and particle manipulation
- [Heat Exchanger](docs/HEAT_EXCHANGER.md) - Cooldown or heat up liquids
- [Leacher](docs/LEACHER.md) - in-situ ore leaching, pumps, research papers, ore-vein system
- [Computers](docs/COMPUTERS.md) - ComputerCraft and OpenComputers v2 peripherals
- [KubeJS Support](docs/KUBEJS_SUPPORT.md) - recipe schemas, events, custom fuel and particle-source registration
- [Radiation](docs/RADIATION.md) - chunk radiation, decay, shielding, player effects, geiger counter

Modpack/scripting references:
- [Custom Fuels Guide](docs/MODPACK_DEVELOPER_GUIDE_CUSTOM_FUELS.md)
- [Particle KubeJS Integration](docs/PARTICLE_KUBEJS_INTEGRATION.md)

## Credits

[tomdodd4598](https://github.com/tomdodd4598) - original NuclearCraft

[Lach01298](https://github.com/Lach01298) - original QMD

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
