---
navigation:
  title: Collision Chamber
  parent: multiblocks/particle_chambers.md
  icon: collision_chamber_controller
item_ids:
  - nuclearcraft:collision_chamber_controller
  - nuclearcraft:target_chamber_casing
  - nuclearcraft:target_chamber_casing_glass
  - nuclearcraft:target_chamber_beam_port
  - nuclearcraft:target_chamber_port
  - nuclearcraft:target_chamber_camera
---

# Collision Chamber

<GameScene zoom={3}>
  <ImportStructure src="/structures/collision_chamber.nbt" />
</GameScene>

The Collision Chamber smashes two opposing particle beams head-on and rakes new, heavier particles out of the wreckage. It is the largest and most power-hungry of the particle chambers.

## Construction

Unlike the other chambers, the Collision Chamber is not a cube. It is a long box of [particle chamber](general.md#construction) casing and glass: 5 to 11 blocks wide and tall, and 13 to 21 blocks deep (17 by default).

## Beam Axis

Down the long axis runs a line of Particle Beam blocks holding at least 2 Particle Chamber Cameras. Both ends of that axis are capped by Particle Chamber Beam Ports set to input mode — the two beams enter here and meet in the middle.

## Outputs

Collision products leave through exactly 4 beam ports set to output mode, placed on the two side walls — two per wall. Each output port must reach a camera along a straight run of Particle Beam blocks. Switch a port's mode with the multitool.

## Detectors & Power

Like all [particle chambers](general.md#detectors), detectors inside raise efficiency at the cost of power. The Collision Chamber also draws a hefty base power on its own, so wire in a Particle Chamber Port for energy.

## Operation

A collision recipe takes two incoming particle stacks — one per input beam — and produces new particles forged in the impact. See JEI for the required energies and the products.
