---
navigation:
  title: Decay Chamber
  parent: multiblocks/particle_chambers.md
  icon: decay_chamber_controller
item_ids:
  - nuclearcraft:decay_chamber_controller
---

# Decay Chamber

<GameScene zoom={3}>
  <ImportStructure src="/structures/decay_chamber.nbt" />
</GameScene>

The Decay Chamber takes a single particle beam and lets it fall apart, splitting heavier particles into lighter constituents. Where the Target Chamber builds things up, the Decay Chamber takes them apart.

## Construction

Like all [particle chambers](general.md#construction), the Decay Chamber is a hollow cube of casing or glass with odd, equal sides, from 5×5×5 up to 11×11×11. At the center sits a Particle Chamber Camera, joined to a Particle Chamber Beam Port on each of the four horizontal faces by a line of Particle Beam blocks.

## Ports

One beam port runs in input mode to receive the beam; the others run in output mode and carry away the lighter particles split out of it. Shift right-click a beam port with the multitool to switch its mode.

Add at least one Particle Chamber Port for energy, plus optional item and fluid ports.

## Detectors

Like all [particle chambers](general.md#detectors), detectors placed inside raise the chamber's efficiency `η` at the cost of power, scaling the recipe [cross-section](general.md#cross_section) `σ`.

## Operation

A decay recipe takes one incoming particle stack — a type, a minimum energy and an amount — and yields several lighter particles. Browse JEI for the full list and the energies involved.
