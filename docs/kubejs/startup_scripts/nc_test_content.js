// Restart the client after changing this file.
NuclearCraftEvents.registerFissionFuel(event => {
  event.register('kubejs', 'test-235')
    .forgeEnergy(300).heat(55).criticality(100)
    .depletion(240).efficiency(100).isotopes(235, 238)
    .recipeModifiers(1.0, 1.0, 1.0)

  event.override('uranium', 'leu-235', fuel => {
    fuel.forgeEnergy(280).heat(45)
  })
  console.info('[NC test] Fuel registered; LEU-235 overridden to 280 FE / 45 heat')
})

NuclearCraftEvents.registerHeatSink(event => {
  event.register('kubejs_test', 140, ['fission_reactor_solid_fuel_cell'])
  event.override('water', sink => {
    sink.heat(75)
  })
  console.info('[NC test] Heat sink registered; water cooling overridden to 75')
})

NuclearCraftEvents.registerParticleSource(event => {
  event.item('minecraft:iron_ingot', 'electron', 1000000, 100, 0.5)
  event.fluid('minecraft:water', 'proton', 15000, 100, 0.6)

  // Exercise removal on temporary assignments, preserving built-in sources.
  event.item('minecraft:stick', 'electron', 1000)
  event.removeItem('minecraft:stick')
  event.fluid('minecraft:lava', 'proton')
  event.removeFluid('minecraft:lava')
  console.info('[NC test] Iron/water sources registered; temporary stick/lava sources removed')
})
