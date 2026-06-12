# Repository Guidelines

## Project Structure & Module Organization
This repository contains a Forge 1.20.1 reimplementation of the NuclearCraft mod written in Java 17 under base package `igentuman.nc`.
- **Domain Packages**: Code is organized by domain rather than layer (e.g., `./src/main/java/igentuman/nc/block/`, `./src/main/java/igentuman/nc/content/`, `./src/main/java/igentuman/nc/item/`, `./src/main/java/igentuman/nc/fluid/`, `./src/main/java/igentuman/nc/entity/`, `./src/main/java/igentuman/nc/multiblock/` for controllers and validation, `./src/main/java/igentuman/nc/compat/` for integrations like Applied Energistics 2, ComputerCraft, and Mekanism, and `./src/main/java/igentuman/nc/mixin/` for Sponge Mixins).
- **Resources & Assets**: Standard assets and configs live in `./src/main/resources/`. English translations must be added via `./src/main/java/igentuman/nc/datagen/NCLanguageProvider.java` and compiled using datagen; never modify `./src/main/resources/assets/nuclearcraft/lang/en_us.json` by hand. Russian translations `./src/main/resources/assets/nuclearcraft/lang/ru_ru.json` are edited directly.
- **Generated Data**: Committed under `./src/generated/resources/`.

## Build, Test, and Development Commands
Run these Gradle wrapper commands for compilation and execution:
- `./gradlew build` — Complete build (runs tests, outputs shadowed Jar to `./build/libs`)
- `./gradlew runClient` — Launches dev client in `./run/`
- `./gradlew runServer` — Launches dev server in `./server/`
- `./gradlew runData` — Runs data generation, outputting to `./src/generated/resources/`
- `./gradlew test` — Runs JUnit 5 test suite
- `./gradlew test --tests <FQCN>` — Runs a specific test
- `./gradlew shadowJar reobfShadowJar` — Builds reobfuscated jar without JarJar packaging

## Coding Style & Naming Conventions
- **Java 17 & UTF-8**: Enforced for compiling.
- **Mixins**: Declare connector classes in `./src/main/resources/META-INF/mods.toml`, place under `igentuman.nc.mixin`, and register in `./src/main/resources/nuclearcraft.mixins.json`.
- **Versioning**: Maintain mod/dependency versions inside `./gradle.properties`. Do not hardcode versions in `./build.gradle`.
- **In-game Text Style**: Localized text features dry retro-futuristic gallows humor. Keep historical or flavor references concise and localized to values.

## Testing Guidelines
Utilize JUnit 5 and Mockito. Mirror the production package layout inside `./src/test/java`. Prioritize test cases for multiblock validation, recipe logic, and radiation calculations.

## Commit & Pull Request Guidelines
- **Commit Format**: Keep commit messages short, imperative, and reference issue numbers directly (e.g., `Voiding storage containers #273`). Avoid Conventional Commits prefixes like `feat:` or `fix:`.
- **PRs**: Target branch `1.20` and ensure the GitHub Actions build is green.
