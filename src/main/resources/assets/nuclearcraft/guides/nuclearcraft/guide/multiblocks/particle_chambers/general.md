---
navigation:
  title: General Info
  parent: multiblocks/particle_chambers.md
  icon: target_chamber_controller
  position: 0
---

# Particle Chambers

Particle Chambers are the multiblocks where you actually do things with particles: bombard items and fluids, collide particles together, and create new ones. They are the second key ingredient in any accelerator system (the first being the accelerators themselves).

## Construction

The outside (casing) of all particle chambers is made out of Particle Chamber Casing or Particle Chamber Glass. In the casing you need:

- The Particle Chamber's controller
- At least one Particle Chamber Energy Port

Inside particle chambers there are particle chamber beam blocks and the particle chamber block itself.

The arrangement of these depends on the type of particle chamber. Detector blocks can be placed around them.

Below: a typical arrangement of a target chamber with 3 output beam ports.

## Detectors

Detectors are placed inside particle chambers to increase the chamber's efficiency. The chamber's efficiency affects how the particle chamber works. Generally, the higher the efficiency, the faster items are crafted and the more particles are output. There are different types of detectors; each increases the efficiency and the power usage of the chamber as specified on their tooltips.

They must be placed a certain distance from a particle chamber block to work, as shown on their tooltip.

Below: a typical arrangement of a target chamber with detectors.

You can check if detectors are in valid positions by sneak right-clicking the controller of a formed particle chamber multiblock with the multitool.

**Note:** Detectors can increase the energy use of the particle chamber drastically. The grid does not extend credit.

## Cross-Section

Efficiency and Cross-Section are 2 important values for particle chambers. Cross-Section `σ` depends on the recipe performed (can be seen in JEI); it can be thought of as the percentage of particles that perform the reaction. Efficiency `η` depends on the particle chamber and modifies the cross-section. This new 'effective' cross-section `Σ` is capped at 100%, which means all particles perform the reaction.

Because of this, detectors can only increase the effective cross-section up to this level; any further detectors do nothing and are in fact a waste of power.
