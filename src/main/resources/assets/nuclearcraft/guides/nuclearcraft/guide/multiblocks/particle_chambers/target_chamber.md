---
navigation:
  title: Target Chamber
  parent: multiblocks/particle_chambers.md
  icon: target_chamber_controller
item_ids:
  - nuclearcraft:target_chamber_controller
  - nuclearcraft:target_chamber_casing
  - nuclearcraft:target_chamber_casing_glass
  - nuclearcraft:target_chamber_beam_port
  - nuclearcraft:target_chamber_port
  - nuclearcraft:target_chamber_camera
---

# Target Chamber

<GameScene zoom={3}>
  <ImportStructure src="/structures/target_chamber.nbt" />
</GameScene>

Target Chambers are where you smash particle beams into a fixed target of material to induce nuclear reactions. They are useful for all sorts of things, from transmuting elements to creating new particles.

## Construction

Build a shell with equal odd sides, **5 to 11 blocks** across by default. Use **Particle Chamber Casing** on edges and corners and place the **Target Chamber Controller** in a wall. Put a **Target Chamber Camera** at the center. Place four outward-facing **Particle Chamber Beam Ports**, one at the center of each vertical face, and connect each to the camera with **Particle Beam** blocks. At least one detector must be within range of the camera.

## Outputs

Use a **Multitool** to set one beam port to input and the other three to output. Each output shown in the recipe uses its own channel. Check the port modes and channels before connecting downstream machines.

## Detectors

Like all [particle chambers](general.md#detectors), target chambers can have detectors inside to increase the chamber's efficiency at the cost of power.

The target chamber's efficiency `η` modifies the recipe [cross-section](general.md#cross-section) `σ` by `Σ = min(η·σ, 1)`.

## Ports

Use a **Particle Chamber Port** for automated item, fluid, and energy transfer. It shares the controller’s inventory and tanks. Configure the access sides as needed.

## Operation

Supply FE, apply a redstone signal, and use JEI to look up the target material, particle type, energy range, and minimum focus. For example, the recipe for turning Aluminum Ingots into Sodium 22 requires neutrons at **44–80 MeV** with at least **1.0 focus**. It produces Sodium 22, helions, and neutrons.

## Recipe Speed

Each tick, the chamber adds work equal to the number of particles consumed times the effective cross-section. The recipe for turning Aluminum Ingots into Sodium 22 needs **20 Mpu** of work. With its 5% base cross-section and 290% chamber efficiency, the effective cross-section is 14.5%. A 10 kpu/t beam adds 1.45 kpu of work per tick.

## Output Particles

Particle byproducts are emitted during irradiation. Their amounts scale with the incoming beam, effective cross-section, and the amount in each output row. Connect each output to a compatible receiver to collect its particles.
