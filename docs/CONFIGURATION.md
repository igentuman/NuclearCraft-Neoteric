# Mod configuration

NuclearCraft: Neoteric provides TOML configuration files for adjusting progression,
energy balance, world generation, machines, and optional content. This guide
summarizes what can be changed; the generated files contain individual settings,
defaults, and comments.

## Where to find the files

Start the game or dedicated server once to generate the files in
`config/nuclearcraft/`, relative to the Minecraft instance or server directory.
Stop the game or server before editing, then restart to apply the changes.
For multiplayer gameplay settings, edit the server's files; visual settings in
`client.toml` belong to each player's client.

## What can be modified

| File | Configurable features |
| --- | --- |
| `common.toml` | Energy generation, storage, pipes, powered equipment, leaching, villages, anomalies, integrations, and general preferences (see below). |
| `worldgen.toml` | Ore generation height ranges, vein sizes, frequency per chunk, and additional ore veins in the Wasteland. Generation can be disabled per ore. Changes affect newly generated chunks. |
| `materials.toml` | Availability of individual material forms, such as ores, ingots, dusts, plates, nuggets, blocks, and fluids. |
| `entries.toml` | Enable or disable individual non-material content entries, including machines, blocks, items, tools, and armor. Disabled entries remain registered, but are hidden from creative tabs and recipe viewers and their runtime behavior is disabled. |
| `processors.toml` | Processing time and energy consumption, both globally and per processor. Global and per-processor multipliers stack. |
| `multiblocks.toml` | Structure size limits; fission power, heat, cooling, fuel lifetime, steam production, and meltdown explosions; molten salt reactor fuel and salt capacity; turbine output and steam flow; heat exchanger throughput, buffers, and cooling; kugelblitz generation and behavior; accelerator scale, beams, power, and heat; particle chamber sizes and power requirements. |
| `bomb.toml` | Pu-239 bomb blast dimensions, arming delay, detonation processing speed, chunk update behavior, and radiation fallout integration. |
| `q36.toml` | Q36 Quantite Disruptor damage, cooldowns, quantite costs, beam range, block destruction, radiation output, and effect duration. |
| `client.toml` | Nuclear explosion visual quality, mushroom cloud duration, shockwave refraction, and screen flash. |

## General settings in `common.toml`

- **Energy generation:** overall FE generation multiplier, RTG output, solar panel
  output, and decay generator fallback output.
- **Storage and transport:** battery and barrel capacities, lightning charging,
  pipe transfer rates, transfer intervals, and network discovery limits.
- **Equipment:** QNP energy capacity and mining cost, and HEV armor energy capacity.
- **Ore leaching:** virtual vein generation, vein size and rarity, randomized ore
  selection, and Immersive Engineering vein compatibility.
- **World features:** nuclear scientist houses in villages, wandering trader
  trades, and Wasteland anomaly spawning, effects, shards, and crystal buffs.
- **Integration and preferences:** preferred mods for tag-based recipe outputs,
  gas scrubber strength, Designs Hub access and connection settings, and debug logging.

Radiation simulation is configured separately in the **Nuclear Radiation** mod.
NuclearCraft's radiation-related settings control its own sources and equipment,
such as bomb fallout and gas scrubbers, when that mod is installed.
