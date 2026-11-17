ServerEvents.recipes(event => {
  event.recipes.nuclearcraft.manufactory(
    ['minecraft:cobblestone'], [], ['minecraft:gravel'], [], 100, 20
  ).processTime(60).energyPerTick(10).id('kubejs:nc_test/manufactory')

  event.recipes.nuclearcraft.alloy_smelter(
    ['minecraft:iron_ingot', 'minecraft:gold_ingot'], [],
    ['2x minecraft:copper_ingot'], [], 100, 20
  ).id('kubejs:nc_test/alloy_smelter')

  event.recipes.nuclearcraft.melter(
    ['minecraft:snowball'], [], [], [Fluid.of('minecraft:water', 250)], 60, 10
  ).id('kubejs:nc_test/melter')

  event.recipes.nuclearcraft.fluid_infuser(
    ['minecraft:dirt'], [Fluid.of('minecraft:water', 250)],
    ['minecraft:clay_ball'], [], 60, 10
  ).id('kubejs:nc_test/fluid_infuser')

  event.recipes.nuclearcraft.chemical_reactor(
    [], [Fluid.of('minecraft:water', 100), Fluid.of('minecraft:lava', 100)],
    [], [Fluid.of('nuclearcraft:steam', 200)], 100, 20
  ).id('kubejs:nc_test/chemical_reactor')

  event.recipes.nuclearcraft.pressurizer(
    ['2x #c:ingots/iron'], [], ['2x #c:ingots/copper'], [], 60, 10
  ).id('kubejs:nc_test/tag_output')

  event.custom({
    type: 'nuclearcraft:rock_crusher',
    item_inputs: [{ item: 'minecraft:mossy_cobblestone', count: 1 }],
    fluid_inputs: [],
    item_outputs: [
      { item: 'minecraft:gravel', count: 1 },
      { item: 'minecraft:sand', count: 1 },
      { tag: 'c:ingots/iron', count: 1 }
    ],
    fluid_outputs: [], process_time: 100, energy_per_tick: 20
  }).id('kubejs:nc_test/custom_rock_crusher')

  // Remove the previous loaded test recipe, then add its replacement once.
  event.remove({ id: 'kubejs:nc_test/replacement' })
  event.shapeless('2x minecraft:stick', ['minecraft:dead_bush'])
    .id('kubejs:nc_test/replacement')

  event.shapeless('nuclearcraft:fuel_kubejs_test_235',
    ['minecraft:iron_ingot', 'minecraft:redstone'])
    .id('kubejs:nc_test/craft_fuel')
  event.shapeless('nuclearcraft:kubejs_test_heat_sink',
    ['minecraft:copper_ingot', 'minecraft:water_bucket'])
    .id('kubejs:nc_test/craft_heat_sink')

  event.custom({
    type: 'nuclearcraft:fission_reactor',
    input: { item: 'nuclearcraft:fuel_kubejs_test_235' },
    output: { item: 'nuclearcraft:depleted_fuel_kubejs_test_235', count: 1 },
    process_time: 240, power: 300, heat: 55
  }).id('kubejs:nc_test/fission_fuel')

  console.info('[NC test] Processor, crafting, replacement and fission recipes submitted')
})
