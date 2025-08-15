# ParticleStack KubeJS Integration

This document describes the KubeJS integration for ParticleStack in NuclearCraft Neoteric.

### Target Chamber Recipe Example
```javascript
ServerEvents.recipes(event => {
    event.custom({
        type: 'nuclearcraft:target_chamber',
        input: [
            {
                item: 'nuclearcraft:americium_242'
            }
        ],
        inputParticles: [
            {
                particle: 'antideuteron',
                amount: 1000000,
                meanEnergy: 1,
                focus: 1.0
            }
        ],
        output: [
            {
                item: 'nuclearcraft:americium_spallation_waste'
            }
        ],
        outputParticles: [
            {
                particle: 'pion_plus',
                amount: 1,
                meanEnergy: 4,
                focus: 1.0
            },
            {
                particle: 'pion_naught',
                amount: 1,
                meanEnergy: 4,
                focus: 1.0
            },
            {
                particle: 'pion_minus',
                amount: 1,
                meanEnergy: 4,
                focus: 1.0
            }
        ],
        crossSection: 1.0,
        maxEnergy: 10000000,
        powerModifier: 1.0,
        timeModifier: 1.0,
        radiation: 1.0
    })
})
```

## Particle Formats

### JSON Object Format (Recommended)
```javascript
{
    particle: 'proton',      // Particle name (string)
    amount: 100,             // Number of particles (integer)
    meanEnergy: 1000,        // Average energy of particles (long)
    focus: 0.8               // Focus value (double, represents inverse area of beam)
}
```