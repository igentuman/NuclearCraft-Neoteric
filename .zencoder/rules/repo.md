---
description: Repository Information Overview
alwaysApply: true
---

# NuclearCraft-Neoteric Information

## Summary
NuclearCraft-Neoteric is a complex nuclear engineering mod for Minecraft, recreating the original NuclearCraft mod for modern Minecraft versions. It adds nuclear reactors, radiation mechanics, processors, and various nuclear-related content to the game.

## Structure
- **src/main/java**: Core mod code with main package `igentuman.nc`
- **src/main/resources**: Assets, data files, and configuration
- **src/test**: JUnit and Mockito tests
- **src/generated**: Generated resources for the mod
- **.github/workflows**: CI/CD configuration for automated builds

## Language & Runtime
**Language**: Java
**Version**: Java 17
**Build System**: Gradle with ForgeGradle
**Package Manager**: Gradle
**Minecraft Version**: 1.20.1
**Forge Version**: 47.4.6

## Dependencies
**Main Dependencies**:
- Minecraft Forge (1.20.1-47.4.6)
- Mekanism (1.20.1-10.4.5.19)
- KubeJS (2001.6.5-build.16)
- Patchouli (1.20.1-84-FORGE)
- CC: Tweaked (1.109.5)
- JEI (15.20.0.105)

**Development Dependencies**:
- JUnit Jupiter (5.9.2)
- Mockito (5.3.1)
- Parchment Mappings (2023.09.03)

## Build & Installation
```bash
# Build the mod
./gradlew build

# Run tests
./gradlew test

# Run client for development
./gradlew runClient

# Run server for development
./gradlew runServer
```

## Testing
**Framework**: JUnit Jupiter with Mockito
**Test Location**: src/test/java/igentuman
**Configuration**: build.gradle
**Run Command**:
```bash
./gradlew test
```

## Features
- Ore Generation
- Nuclear Processors
- Fission Reactor
- Fusion Reactor
- Radiation System
- Energy Blocks
- Solar Panels
- Steam Turbines
- In Situ Leaching
- Wasteland Biome and Dimension
- Computer Integration (CC: Tweaked)
- Patchouli Guidebook Integration
- KubeJS Integration

## CI/CD
**GitHub Actions**: Automated build and test workflow on push/PR to the 1.20 branch
**Build Command**: `./gradlew build`
**Test Command**: `./gradlew test`
**Artifacts**: Built mod JARs