# NuclearCraft-Neoteric Repository Information

## 📋 Project Overview

**NuclearCraft-Neoteric** is a comprehensive nuclear engineering mod for Minecraft 1.20.1, built on Forge 47.4.0. This project is a modern recreation of the classic NuclearCraft mod, bringing advanced nuclear technology, radiation systems, and complex multiblock structures to modern Minecraft versions.

## 🔧 Technical Specifications

### Core Information
- **Mod ID**: `nuclearcraft`
- **Current Version**: `1.2.5-beta.2`
- **Minecraft Version**: `1.20.1`
- **Forge Version**: `47.4.0`
- **Java Version**: `17`
- **License**: MIT License
- **Author**: igentuman
- **Group**: `igentuman.nc`

### Build System
- **Build Tool**: Gradle with ForgeGradle
- **Mappings**: Parchment (`2023.09.03-1.20.1`)
- **Mixin Support**: Yes (`nuclearcraft.mixins.json`)
- **Shadow JAR**: Enabled for final distribution

## 🏗️ Architecture & Dependencies

### Core Dependencies
- **Minecraft Forge**: `1.20.1-47.4.0`
- **JEI (Just Enough Items)**: `15.20.0.105`
- **Mekanism**: `10.4.5.19` (with generators)
- **ComputerCraft**: `1.109.5`
- **Patchouli**: `84-FORGE` (for guidebook)
- **The One Probe**: `1.20.1-10.0.1-3`

### Optional Integrations
- **KubeJS**: `2001.6.5-build.16`
- **GregTech CEu**: `1.6.4`
- **Immersive Engineering**: Compatibility layer
- **EMI**: Recipe viewer support
- **Open Computers 2**: Computer integration

## 🚀 Key Features

### ✅ Implemented Features
- **Ore Generation**: Custom nuclear ores and materials
- **Processors**: Advanced crafting machines with sided content handling
- **Fluids System**: Nuclear fluids and coolants
- **Fission Reactors**: Complete fission reactor multiblocks
- **Fusion Reactors**: Advanced fusion technology
- **Radiation System**: Realistic radiation mechanics
- **Energy Blocks**: Power generation and storage
- **Solar Panels**: Renewable energy sources
- **Steam Systems**: Steam mode for reactors and turbines
- **In Situ Leaching**: Advanced ore extraction
- **Computer Integration**: CC and OC2 support
- **Custom Blocks**: Cobblestone generators and specialized blocks
- **Custom Items**: Geiger counters, dosimeters, and tools
- **Kugelblitz**: Black hole technology
- **Wasteland Biome & Dimension**: Radioactive environments
- **Custom Villagers**: Nuclear engineer profession
- **Patchouli Guidebook**: Comprehensive documentation

### 🔄 Upcoming Features
- **Particle API**: Advanced particle system
- **Particle Accelerators**: High-energy physics

### 🎯 Future Plans
- **Crafting Automation**: Automated production systems
- **Reactor Design Hub**: Advanced reactor planning tools
- **Heat Exchanger**: Improved thermal management

## 📁 Project Structure

```
src/main/java/igentuman/nc/
├── NuclearCraft.java          # Main mod class
├── api/                       # Public API
├── block/                     # Block implementations
├── client/                    # Client-side code
├── compat/                    # Mod compatibility
├── content/                   # Game content
├── datagen/                   # Data generation
├── entity/                    # Custom entities
├── fluid/                     # Fluid systems
├── handler/                   # Event handlers
├── item/                      # Item implementations
├── multiblock/                # Multiblock structures
├── network/                   # Networking
├── radiation/                 # Radiation system
├── recipes/                   # Recipe systems
├── registry/                  # Registration
├── util/                      # Utilities
└── world/                     # World generation
```

## 🔨 Development Setup

### Prerequisites
- Java 17 JDK
- IntelliJ IDEA (recommended)
- Git

### Building the Project
```bash
# Clone the repository
git clone https://github.com/igentuman/NuclearCraft-Neoteric.git
cd NuclearCraft-Neoteric

# Make gradlew executable (Linux/Mac)
chmod +x ./gradlew

# Build the mod
./gradlew build

# Run client for testing
./gradlew runClient

# Run server for testing
./gradlew runServer

# Generate data
./gradlew runData
```

### Testing
```bash
# Run unit tests
./gradlew test
```

## 🔄 CI/CD Pipeline

The project uses GitHub Actions for continuous integration:

- **Trigger**: Push/PR to `1.20` branch
- **Java Version**: 17 (Temurin distribution)
- **Build Process**: Gradle build with caching
- **Artifacts**: Compiled JAR files uploaded as artifacts
- **Testing**: Automated unit tests

## 📦 Distribution

### Official Releases
- **CurseForge**: [NuclearCraft Neoteric](https://curseforge.com/minecraft/mc-mods/nuclearcraft-neoteric)
- **Development Builds**: Available via GitHub Actions artifacts

### Version History
- **1.2.5-beta.2**: Current development version
- **1.2.1-rc**: Added heat sinks, Kugelblitz, villagers, performance improvements
- **1.0.0**: Initial stable release with turbine rendering fixes and optimizations

## 🤝 Contributing

### Credits
- **tomdodd4598**: Original NuclearCraft mod creator
- **Lach01298**: Original QMD mod creator
- **igentuman**: Current maintainer and developer

### Special Thanks to Patrons
- Noteclip
- Wandering Singularity
- Tom Dodd
- Niv
- Commander Ava
- marcin212
- endleon tiozae
- Ethan Tabler
- PersonBelow Rocks

### Support
- **Patreon**: [igentuman](https://patreon.com/igentuman)
- **PayPal**: [igentuman](https://paypal.me/igentuman)

## 📄 License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## 🔗 Links

- **GitHub Repository**: [NuclearCraft-Neoteric](https://github.com/igentuman/NuclearCraft-Neoteric)
- **CurseForge Page**: [NuclearCraft Neoteric](https://curseforge.com/minecraft/mc-mods/nuclearcraft-neoteric)
- **Build Status**: ![Build Status](https://github.com/igentuman/NuclearCraft-Neoteric/actions/workflows/gradle.yml/badge.svg?branch=1.20)

---

*This repository information was generated automatically based on the project structure and configuration files.*