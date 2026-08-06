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

Target Chambers, like all [particle chambers](general.md#construction), are constructed out of casing or glass and have an energy port. They can also have optional item and fluid ports. They are cubes that can be any odd size from 3 to 9 blocks across. They must have only one Particle Chamber Beam Port in input mode, but can have from 0 to 3 beam ports in output mode. Beam ports can be placed in the center of any horizontal face.

In the center there must be a Particle Chamber block connected to any beam ports via a line of Particle Chamber Beam Blocks.

Below: a size 7 target chamber with 2 beam port outputs and item ports.

## Outputs

There can be 0 to 3 output beams depending on how the target chamber is constructed. The position of the outputs in relation to the input matters: it determines which beam comes out of which beam port. The beams come out 1 to 3 clockwise as viewed from the top, and correspond top-to-bottom in the GUI of the Target Chamber. This can be switched by shift right-clicking one of the 2 side beam ports with the multitool.

Note: the middle output beam will always come out the back.

Generally, neutral particle beams will come out the middle port, as they are not bent by magnetic fields.

## Detectors

Like all [particle chambers](general.md#detectors), target chambers can have detectors inside to increase the chamber's efficiency at the cost of power.

The target chamber's efficiency `η` modifies the recipe [cross-section](general.md#cross_section) `σ` by `Σ = min(η·σ, 1)`.

## Ports

Items can be manually put into and out of target chambers in the GUI, but in order to pipe items and fluids in and out of a target chamber automatically, ports must be used. Ports can be placed on any side of the target chamber. Item ports can be used to pipe items both in and out, while fluid ports must be set to either input or output mode. Items can also be piped in and out of the controller.

## Operation

To show how the target chamber operates, let's look at the example recipe in JEI above. Here neutrons hit aluminum ingots, creating sodium-22, helions, and neutrons.

The first important thing is the range: 43 MeV-80 MeV. This means that the recipe only works if we input a beam of neutrons with an energy in this range.

Other things to note for later are the cross-section: 5%, and the energy released: -42.1 MeV.

Also, if we hover over the input (cyan box) neutron, the focus says 0. This is the minimum focus required for the recipe. Most recipes have a minimum of 0.

## Recipe Speed

If we hover over the input neutron, it tells us the amount is 20 Mpu. This means we need to supply 20 Mpu of neutrons to convert one aluminum into a sodium-22. For a 10 kpu/t beam this will take `20 Mpu / (10 kpu/t) = 2000 t = 100 seconds`. But we can speed this up by either increasing the input beam pu/t or increasing the chamber efficiency. Let's say we have an efficiency of 290%; then we have modified the [cross-section](general.md#cross_section) to `5% × 290% = 14.5%`.

This has decreased the amount of pu to complete the recipe to `20 Mpu / 290% ≈ 6.90 Mpu`. So the recipe completes 2.9× faster, taking 689 t ≈ 34.5 seconds to complete.

Note that the effective cross-section can only be 100% at max, so if we have a chamber efficiency above 2000% (because `5% × 2000% = 100%`) it does not speed up the recipe anymore. The minimum amount of particles needed can be calculated as `a·σ`. So in this case, it is `20 Mpu × 5% = 1 Mpu`.

For normally balanced QMD (i.e. no custom recipe changes) this is always 1 Mpu. Because of this minimum amount of particles to complete the recipe, the maximum speed of a recipe is still dependent on the input pu/t.

## Output Particles

The recipe also has output particles. Hovering over the output neutron in JEI, we see it has an amount of 3 pu. This means that for every input particle we get 3 output neutrons, if at 100% cross-section. The equation for the amount of particles outputted is `aₒ = aᵢ·a·Σ` where `aᵢ` is the amount of particles inputted, `a` is the amount shown in the recipe, and `Σ` is the effective cross-section.

In our example below we are putting in 10 kpu/t and have a chamber efficiency of 290%, so the effective cross-section is 14.5%, therefore the output is `10 kpu/t × 3 pu × 14.5% = 4.35 kpu/t` as can be seen hovering over the output neutrons.

The energy of the output particles is calculated as `E = (E₀ + Q) / n` where `E₀` is the input particle energy, `Q` is the energy released in the recipe and `n` is the total amount of particles released in the recipe.

In our example case we are putting in neutrons at 45 MeV and the energy released is -42.1 MeV. In JEI 3 neutrons and 1 helion is the output so `n = 4`. Thus in this case `(45 MeV + -42.1 MeV) / 4 = 725 keV` as seen in the image to the left.

## Focus Loss

Like in accelerators and beamlines, particles in target chambers lose focus with every block traveled. The distance traveled is the beam length shown in the GUI. The loss in focus is calculated the same as normal but there are a few things to note: the input particle travels half the beam length and so does the output particle.

So in our example the input neutron beam is at 5 focus and the beam length is 5, so the input and output particle both travel 2.5 blocks. If we want to calculate the focus for the output helion then we do `5 - 2.5·0.02·(1 + 0·sqrt(10 kpu/10k)) - 2.5·0.02·(1 + 2·sqrt(1.45 kpu/10k)) ≈ 4.8619` (the GUI rounds it to 4 decimal places).

This is a bit complicated but really just remember to have high focus and you don't really need to worry about the specifics, because too much focus is never a bad thing.
