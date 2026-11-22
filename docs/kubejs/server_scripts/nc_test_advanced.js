// Artificial reactions for API testing; these are not balanced gameplay recipes.
ServerEvents.recipes(event => {
  const neutron = {
    species: ['nuclearcraft:neutron'], amount: 100,
    minimum_energy: 0, maximum_energy: 1000000, minimum_focus: 0.1
  }
  const proton = {
    particle: 'nuclearcraft:proton', amount: 100, energy: 100, focus: 0.4
  }

  event.recipes.nuclearcraft.target_chamber(
    neutron, 0.25, ['minecraft:gold_ingot'], [], [],
    ['minecraft:iron_ingot'], [], 0, 100
  ).id('kubejs:nc_test/target_chamber')

  event.recipes.nuclearcraft.decay_chamber(
    neutron, [proton], 0.5
  ).id('kubejs:nc_test/decay_chamber')

  event.recipes.nuclearcraft.collision_chamber(
    [neutron, neutron], [proton], 0.25, 0, false, 100
  ).id('kubejs:nc_test/collision_chamber')

  event.recipes.nuclearcraft.accelerator_coolant(
    Fluid.of('minecraft:water', 1), Fluid.of('nuclearcraft:steam', 1), 100, 300
  ).id('kubejs:nc_test/accelerator_coolant')

  event.recipes.nuclearcraft.kugelblitz_chamber(
    'minecraft:cobblestone', 'minecraft:diamond', 2.0, 1.5
  ).id('kubejs:nc_test/kugelblitz_chamber')

  event.custom({
    type: 'nuclearcraft:heat_exchanger',
    input_fluid: { fluid: 'minecraft:water', amount: 100 },
    output_fluid: { fluid: 'nuclearcraft:steam', amount: 100 }, heat: 1000
  }).id('kubejs:nc_test/heat_exchanger')

  event.custom({
    type: 'nuclearcraft:nc_ore_veins', id: 'kubejs:nc_test_iron_vein',
    ores: [
      { item: { item: 'minecraft:iron_ore', count: 1 }, weight: 4 },
      { item: { item: 'minecraft:gold_ore', count: 1 }, weight: 1 }
    ], rarity: 1.0
  }).id('kubejs:nc_test/ore_vein')

  console.info('[NC test] Particle, coolant, Kugelblitz, heat exchanger and vein recipes submitted')
})
