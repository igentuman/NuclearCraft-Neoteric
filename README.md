# NuclearCraft-Neoteric

Re-creation of the classic NuclearCraft mod for modern Minecraft (NeoForge) 26.1.2 / Java 25 port;
NuclearCraft-Neoteric brings a full nuclear-tech progression chain to the game: ore processing,
fission reactors with configurable fuels and heat sinks, molten salt reactors, fusion cores,
particle accelerators, target chambers, kugelblitz (black-hole) reactors, steam turbines,
in-situ leaching, and a chunk-based world radiation system that actually rewards (and punishes)
how you build.

[![Build Mod Job](https://github.com/igentuman/NuclearCraft-Neoteric/actions/workflows/build.yml/badge.svg?branch=26.1.2)](https://github.com/igentuman/NuclearCraft-Neoteric/actions/workflows/build.yml)
[![Downloads](https://cf.way2muchnoise.eu/full_840010_downloads.svg)](https://www.curseforge.com/minecraft/mc-mods/nuclearcraft-neoteric)
[![MC Versions](https://cf.way2muchnoise.eu/versions/840010.svg)](https://www.curseforge.com/minecraft/mc-mods/nuclearcraft-neoteric)

## Overview

- **Mod id:** `nuclearcraft`
- **Target Minecraft:** 26.1.2 (NeoForge 26.1.2.129, Java 25)
- **Energy:** Forge Energy (FE/RF); Mekanism and GregTech CE: Unofficial interop where present
- **Downloads:** https://curseforge.com/minecraft/mc-mods/nuclearcraft-neoteric
- **Dev builds:** GitHub Actions tab → latest artifact

## Documentation
- [Mod configuration](docs/CONFIGURATION.md)
- [KubeJS events and recipes](docs/KUBEJS.md)
- [Engineer's Crafting Table and autocrafting](docs/ENGINEERS_CRAFTING_TABLE.md)
- [Radiation](https://github.com/igentuman/Nuclear-Radiation/blob/26.1.2/README.md)
- [Armor, tools, weapons and bomb](docs/ARMOR_TOOLS_WEAPONS_AND_BOMB.md)
- [World generation](docs/WORLD_GENERATION.md)
- [Processors](docs/PROCESSORS.md)
- [Ore Leacher](docs/LEACHER.md)
- [Fission reactors](docs/FISSION_REACTOR.md)
- [Heat exchanger](docs/HEAT_EXCHANGER.md)
- [Fusion reactor](docs/FUSION_REACTOR.md)
- [Steam turbine](docs/TURBINE.md)
- [Kugelblitz](docs/KUGELBLITZ.md)
- [Linear accelerator](docs/LINEAR_ACCELERATOR.md)
- [Ring accelerator](docs/RING_ACCELERATOR.md)
- [Beam diverter](docs/BEAM_DIVERTER.md)
- [Target chamber](docs/TARGET_CHAMBER.md)
- [Collision chamber](docs/COLLISION_CHAMBER.md)
- [Decay chamber](docs/DECAY_CHAMBER.md)


## Features

- **Ore processing chain** - crushers, washers, centrifuges, electrolyzers, infusers, melters and assemblers feeding a deep materials tree (uranium, thorium, plutonium, americium, curium, californium and beyond).
- **Fission reactors** - multiblock cores with configurable solid fuels, moderators, reflectors, heat sinks, steam mode, neutron flux, and meltdown consequences.
- **Molten Salt Reactors** - liquid-fuel cores: pump carrier salt through a chamber of fuel pebbles, manage coupled temperature/reactivity with a self-stabilizing negative feedback, and ship the heat out as hot salt to a heat exchanger.
- **Fusion reactors** - toroidal plasma ring built from electromagnets and RF amplifiers, fed by fuel/coolant recipes.
- **Particle accelerators** - linear and (WIP)toroidal beam lines with magnets, RF cavities and configurable particle sources.
- **Target chamber** - beam-driven transmutation, detector readouts, antimatter production chain.
- **Kugelblitz chamber** - 11×11×11 photon-ignition chamber for mini cool looking blackhole.
- **Steam turbines** - rotor shafts, blade tiers, coil materials, steam/exhaust recipes and a transparent energy formula.
- **In-situ leaching** - ore-vein system with leachers, pumps and research papers as an alternative to traditional mining.
- **World radiation** - since 1.21 radiation moved to separate mod Nuclear Radiation
- **World generation** - new biome, new dimension with custom structures. Mini boss. Ores
- **Anomalies and crystals** - Environmental hazards in Wastelands and buffs.
- **Nuclear weapons** - Pu-239 / fission and fusion bombs with simulated blast, fallout and structural damage.
- **Engineer's Crafting Table** - powered autocrafting terminal: load storage containers and encoded patterns, feed it power, and it assembles to spec one operation per tick. Scriptable via ComputerCraft and OpenComputers v2 peripherals.
- **Universal pipes** - single conduit that transports items, fluids and energy at once; pipe connectors set per-medium pull/push/auto modes and optional redstone gating.
- **Energy & storage** - Forge Energy (FE/RF) throughout, with Mekanism and GregTech CE:U interop, plus voiding/buffering storage tiers.
- **Mod integrations** - JEI, EMI, AE2, Refined Storage, ComputerCraft, OpenComputers v2, TIS-3D, KubeJS, Patchouli, Ponder.
- **In-game guide** - Patchouli, GuideMe, Ponders.

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

## Development setup

Use the checked-in Gradle 9.9.0 wrapper. Java 25 is selected by the toolchain;
set the IDE project SDK to 25 and delegate builds/runs to Gradle.

```bash
./gradlew preparePortingEnvironment   # resolve toolchain/artifacts and expand metadata, no NC compilation
./gradlew build                      # requires the Java API port
./gradlew runClient                  # also runServer, runGameTestServer, runData after porting
```

The default `core` runtime includes Nuclear Radiation. Use `-PdevProfile=integrations`
to load optional integration mods or `-PdevProfile=full` for performance/modpack tools too.
Available optional APIs remain on the compile classpath in every profile.
A sibling `../Nuclear-Radiation-26.1` checkout is substituted automatically; CI can use
`-PradiationProjectDir=<path>`. Use `-PusePublishedRadiation=true` to resolve the pinned release.