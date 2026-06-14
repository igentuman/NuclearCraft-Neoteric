---
description: Molten Salt Fission Reactor Controller Implementation
alwaysApply: true
---

# Molten Salt Fission Reactor Controller Implementation

This rule tracks the step-by-step implementation of the Molten Salt Fission Reactor (MSR) controller for NuclearCraft-Neoteric. The MSR controller differs from the standard fission reactor by operating with fluid fuel (molten salt) instead of solid fuel cells, with no heatsinks, moderators, or irradiation chambers required.

## Implementation Status

### ✅ Completed

- **Phase 1: Core Classes** - All implemented
  - `MSRControllerBlock.java` - Block with GUI interaction
  - `MSRControllerBE.java` - Block entity with core numerical model simulation
  - `MSRControllerContainer.java` - Container/Menu for GUI slots
  - `MSRControllerScreen.java` - Client-side GUI screen
  - `MSRController.java` - MultiblockController implementation

- **Phase 2: Registration** - Fully integrated
  - Container registered as `MSR_CONTROLLER_CONTAINER` in `FissionReactorRegistration`
  - MSR blocks registered via `msrBlocks()` method
  - Block entities registered
  - Screen registered in `ClientSetup`

- **Phase 3: Core Logic & Validation**
  - `MSRControllerBE.java` - `tickServer()` override implemented with full numerical model from MSR.md
  - `MSRMultiblock.java` - Validation and stats calculation implemented

- **Phase 4: Datagen**
  - Block/item models generated via `NCBlockStates` and `NCItemModels`
  - Recipes for MSR controller, fuel cell, and heat exchanger added to `NCRecipes`
  - Loot tables covered by existing fission blocks logic
  - Lang entries added to `NCLanguageProvider` (WIP prefix removed)

- **Design & Documentation**
  - `MSR.md` - Comprehensive gameplay mechanics and numerical model

### 📋 Next Priority

1. **Test & Balance** - Gameplay testing and numerical balance
2. **JEI/EMI Integration** - Ensure MSR recipes are shown correctly (if needed)
3. **Advanced Mechanics** - Implement salt solidification and catastrophic failure (meltdown) events

---

## Manual Adjustments Made

The implementation diverged from the original template in these ways:

1. **MSRControllerContainer** 
   - Extends `AbstractContainerMenu` instead of `NCContainer` (manual customization)
   - Custom slot handling with `NCSlotItemHandler` for input/output
   - Custom getter methods for container state (heat, energy, efficiency, powered)

2. **MSRControllerScreen**
   - Implements `IProgressScreen` and `IVerticalBarScreen` interfaces
   - Uses `AbstractContainerScreen` instead of custom `NCScreen`

3. **MSRMultiblock**
   - Uses `HashSet<Block>` constructor approach
   - Implements `MultiblockController` pattern from fission reactor

4. **File Organization**
   - Used existing `FissionReactorRegistration` class for registration instead of creating separate `MSRReactorRegistration`
   - Follows fission reactor's pattern with `MSR_BLOCKS`, `MSR_BE`, `MSR_BLOCK_ITEMS` maps

---

## Completion Checklist

Use this checklist to track implementation progress:

### Core Logic Implementation
- [x] Add MSRControllerBE fields for MSR-specific state (pebbleCount, saltVolume, temperature, pressure, etc.)
- [x] Implement `tickServer()` with main simulation loop
- [x] Implement `tickClient()` 
- [x] Implement temperature calculation logic
- [x] Implement reactivity calculation with concentration feedback
- [x] Implement pressure calculation
- [x] Implement criticality check
- [x] Implement fuel processing (consume pebbles, generate energy)
- [x] Implement cooling system
- [x] Implement impurity tracking

### Multiblock Validation
- [x] Complete MSRMultiblock validation logic
- [x] Add corner detection
- [x] Add casing validation
- [x] Add stats calculation from multiblock size
- [x] Integrate with controller update

### Data Generation
- [x] Create block texture file
- [x] Register block model in BlockStateProvider
- [x] Register item model
- [x] Create crafting recipe
- [x] Create loot table
- [x] Add language entries for UI strings

### Testing & Balance
- [ ] Test multiblock formation
- [ ] Test GUI interaction
- [ ] Test energy generation
- [ ] Test pressure lock mechanic
- [ ] Test port I/O
- [ ] Verify recipe obtainability
- [ ] Balance energy output constants
- [ ] Balance heat generation constants
- [ ] Verify criticality thresholds feel right

### Documentation
- [x] Update MSR.md with any final design changes
- [x] Document all new configuration options
- [x] Add comments to complex logic

---

## File Structure Reference

**Current implementation files:**
- `src/main/java/igentuman/nc/block/fission/MSRControllerBlock.java`
- `src/main/java/igentuman/nc/block/fission/entity/MSRControllerBE.java`
- `src/main/java/igentuman/nc/container/MSRControllerContainer.java`
- `src/main/java/igentuman/nc/client/gui/fission/MSRControllerScreen.java`
- `src/main/java/igentuman/nc/multiblock/fission/MSRMultiblock.java`
- `src/main/java/igentuman/nc/multiblock/fission/MSRController.java`
- `src/main/java/igentuman/nc/multiblock/fission/FissionReactorRegistration.java`
- `src/main/java/igentuman/nc/setup/ClientSetup.java`

**Design document:**
- `MSR.md` - Gameplay mechanics and numerical model

**Key config sources:**
- `FissionConfig` - Base thermal constants
- `CommonConfig` - Energy generation settings
