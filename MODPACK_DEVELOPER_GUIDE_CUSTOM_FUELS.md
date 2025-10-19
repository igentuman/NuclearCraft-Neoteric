# Modpack Developer Guide: Adding Custom Fission Fuels

This guide explains how to add custom fission reactor fuels to NuclearCraft-Neoteric using KubeJS integration.

## Table of Contents
1. [Overview](#overview)
2. [Quick Start](#quick-start)
3. [Fuel Properties Explained](#fuel-properties-explained)
4. [What Gets Generated Automatically](#what-gets-generated-automatically)
5. [File Structure](#file-structure)
6. [Advanced Usage](#advanced-usage)
7. [Examples](#examples)

---

## Overview

NuclearCraft-Neoteric provides a powerful KubeJS integration that allows modpack developers to add custom fission fuels without modifying the mod itself. When you register a custom fuel, the mod automatically generates:

- **Fuel items** (base and 4 variants: oxide, nitride, zirconium alloy, TRISO)
- **Depleted fuel items** (base and 4 variants)
- **Fission reactor recipes** (for base and 3 variants - TRISO excluded)
- **Crafting recipes** (shapeless recipes from isotopes)
- **Smelting recipes** (variants back to base fuel)
- **Item tags** (for recipe compatibility)

**Important:** Processing recipes (melter, ingot former, isotope separator, fluid infuser, assembler) are **NOT** automatically generated. You must create these yourself if needed.

## Quick Start

### Prerequisites
- KubeJS installed in your modpack
- Basic understanding of JavaScript and KubeJS events

### Basic Example

Create a file in your modpack at: `kubejs/startup_scripts/custom_fuels.js`

```javascript
NCKJSEvents.RegisterFissionFuel(event => {
    // Register a custom plutonium fuel
    event.registerFuel(
        'plutonium',        // group (fuel family)
        'hep-242',          // name (fuel type)
        1200,               // forgeEnergy (FE/t produced)
        400,                // heat (heat generated)
        45,                 // criticality (affects reactor behavior - not used currently)
        180,                // depletion (how long fuel lasts)
        110,                // efficiency (affects overall performance - not used currently)
        242,                // isotope1 (first isotope ID)
        239                 // isotope2 (second isotope ID)
    )
})
```

That's it! The mod will automatically generate all necessary items, recipes, and variants.

### Generated files
Checkout generated files in `kubejs/assets/nuclearcraft/` folder

---

## Fuel Properties Explained

### Group (String)
The fuel family or element type. Examples: `"uranium"`, `"thorium"`, `"plutonium"`, `"americium"`

**Naming Convention:**
- Use lowercase
- No spaces or special characters
- Should represent the base element or fuel family

### Name (String)
The specific fuel type within the group. Examples: `"heu-235"`, `"leu-233"`, `"tbu"`

**Naming Convention:**
- Use lowercase
- Hyphens are allowed
- Common prefixes:
  - `he` = High Enriched
  - `le` = Low Enriched
  - Followed by element abbreviation and isotope number

### Forge Energy (Integer)
Energy produced per tick in Forge Energy (FE/t).

**Typical Values:**
- Low enriched fuels: 120-300 FE/t
- High enriched fuels: 960-1200 FE/t
- Special fuels: Can be higher

**Balance Considerations:**
- Higher values = more power but may require better cooling
- Scales with reactor size and configuration

### Heat (Double)
Heat generated per tick.

**Typical Values:**
- Low enriched: 18-60
- High enriched: 300-400
- Affects cooling requirements

**Important:**
- Heat multipliers from config apply
- LEU fuels (starting with 'l') get 2x heat multiplier in boiling mode
- Must be balanced with energy output

### Criticality (Integer) (not used right now) 
Affects how easily the fuel sustains a chain reaction.

**Typical Values:**
- Low criticality: 200-240 (harder to start, needs more fuel cells)
- Medium criticality: 100-150
- High criticality: 39-51 (easier to start, fewer cells needed)

**Reactor Impact:**
- Lower values = need more fuel cells for criticality
- Higher values = can run with fewer fuel cells
- Affects minimum reactor size

### Depletion (Integer)
How long the fuel lasts before becoming depleted.

**Typical Values:**
- Fast burning: 133-180
- Medium burning: 240
- Slow burning: 720+

**Important:**
- Higher values = fuel lasts longer
- Affected by depletion multiplier in config
- Measured in internal ticks

### Efficiency (Integer) (not used right now)
Overall efficiency rating of the fuel.

**Typical Values:**
- Standard: 100
- Efficient: 105-115
- Very efficient: 120-125

**Impact:**
- Affects overall reactor performance
- Higher efficiency = better energy/heat ratio
- Used in reactor calculations

### Isotope1 & Isotope2 (Integer)
The two isotopes that make up the fuel.

**Examples:**
- Uranium-235 fuel: isotopes 235 and 238
- Thorium fuel: isotopes 230 and 232
- Plutonium-239 fuel: isotopes 239 and 240

**Important:**
- Must correspond to actual isotopes in the mod
- Used for crafting recipes
- Determines fuel pellet recipe ratios

---

## What Gets Generated Automatically

When you register a custom fuel, NuclearCraft automatically generates the following items and recipes. **Note:** Only fission reactor recipes are generated - processing machine recipes (melter, assembler, etc.) must be added manually.

### 1. Items
For each fuel, the following items are created:

#### Base Fuel Items
- `fuel_<group>_<name>` - Base fuel pellet
- `fuel_<group>_<name>_ox` - Oxide variant
- `fuel_<group>_<name>_ni` - Nitride variant
- `fuel_<group>_<name>_za` - Zirconium Alloy variant
- `fuel_<group>_<name>_tr` - TRISO variant

#### Depleted Fuel Items
- `depleted_fuel_<group>_<name>` - Base depleted fuel
- `depleted_fuel_<group>_<name>_ox` - Oxide depleted
- `depleted_fuel_<group>_<name>_ni` - Nitride depleted
- `depleted_fuel_<group>_<name>_za` - Zirconium Alloy depleted
- `depleted_fuel_<group>_<name>_tr` - TRISO depleted

**Example:** For `plutonium` / `hep-239`:
- `nuclearcraft:fuel_plutonium_hep_239`
- `nuclearcraft:fuel_plutonium_hep_239_ox`
- `nuclearcraft:fuel_plutonium_hep_239_ni`
- `nuclearcraft:fuel_plutonium_hep_239_za`
- `nuclearcraft:fuel_plutonium_hep_239_tr`
- (and corresponding depleted variants)

### 2. Fission Reactor Recipes
Recipes for using the fuel in fission reactors:

**Location:** `data/nuclearcraft/recipes/fission_reactor_controller/`

**Files Generated:**
- `<group>_<name>.json` - Base fuel recipe
- `<group>_<name>_ox.json` - Oxide variant recipe
- `<group>_<name>_ni.json` - Nitride variant recipe
- `<group>_<name>_za.json` - Zirconium Alloy variant recipe

**Recipe Format:**
```json
{
  "type": "nuclearcraft:fission_reactor_controller",
  "input": [
    {
      "item": "nuclearcraft:fuel_plutonium_hep_239"
    }
  ],
  "output": [
    {
      "item": "nuclearcraft:depleted_fuel_plutonium_hep_239"
    }
  ],
  "powerModifier": 1.0,
  "radiation": 1.0,
  "timeModifier": 1.0
}
```

**Note:** TRISO variants (_tr) do NOT get fission reactor recipes generated automatically.

**If fission reactor controller recipes are not created automatically**, you can add them manually using the following server script:

```javascript
ServerEvents.recipes(event => {
    // Add fission reactor controller recipe for custom fuel
    event.custom({
        type: 'nuclearcraft:fission_reactor_controller',
        input: [
            { item: 'nuclearcraft:fuel_plutonium_hep_242' }
        ],
        output: [
            { item: 'nuclearcraft:depleted_fuel_plutonium_hep_242' }
        ],
        powerModifier: 1.0,
        radiation: 1.0,
        timeModifier: 1.0
    })
    
    // Add recipes for variants (oxide, nitride, zirconium alloy)
    event.custom({
        type: 'nuclearcraft:fission_reactor_controller',
        input: [
            { item: 'nuclearcraft:fuel_plutonium_hep_242_ox' }
        ],
        output: [
            { item: 'nuclearcraft:depleted_fuel_plutonium_hep_242_ox' }
        ],
        powerModifier: 1.0,
        radiation: 1.0,
        timeModifier: 1.0
    })
})
```

Place this script in `kubejs/server_scripts/` (e.g., `kubejs/server_scripts/fission_recipes.js`).

## Important: Recipes NOT Auto-Generated

**The mod does NOT automatically generate the following recipes:**
- Fuel crafting recipes
- Melter recipes (solid fuel → molten fuel)
- Ingot Former recipes (molten fuel → solid fuel)
- Isotope Separator recipes (fuel → isotopes)
- Fuel Reprocessor recipes (depleted fuel → isotopes)
- Fluid Infuser recipes (base fuel + fluid → variant fuel)
- Assembler recipes (base fuel + materials → TRISO fuel)

**As a modpack developer, you must create these recipes yourself using KubeJS or other recipe modification tools if you want these processing chains to work with your custom fuels.**

### Example: Adding Processing Recipes via KubeJS

If you want your custom fuel to be processable, you'll need to add recipes like this in `kubejs/server_scripts/`:

```javascript
ServerEvents.recipes(event => {
    // Melter recipe - solid to molten
    event.custom({
        type: 'nuclearcraft:melter',
        input: [{ item: 'nuclearcraft:fuel_plutonium_hep_242' }],
        output: [{ fluid: 'nuclearcraft:fuel_plutonium_hep_242', amount: 1000 }],
        timeModifier: 1.0,
        powerModifier: 1.0,
        radiation: 1.0
    })
    
    // Ingot Former recipe - molten to solid
    event.custom({
        type: 'nuclearcraft:ingot_former',
        input: [{ fluid: 'nuclearcraft:fuel_plutonium_hep_242', amount: 1000 }],
        output: [{ item: 'nuclearcraft:fuel_plutonium_hep_242' }],
        timeModifier: 1.0,
        powerModifier: 1.0,
        radiation: 1.0
    })
    
    // Fluid Infuser recipe - create oxide variant
    event.custom({
        type: 'nuclearcraft:fluid_infuser',
        input: [
            { item: 'nuclearcraft:fuel_plutonium_hep_242' },
            { fluid: 'nuclearcraft:oxygen', amount: 1000 }
        ],
        output: [{ item: 'nuclearcraft:fuel_plutonium_hep_242_ox' }],
        timeModifier: 1.0,
        powerModifier: 1.0,
        radiation: 1.0
    })
    
    // Assembler recipe - create TRISO variant
    event.custom({
        type: 'nuclearcraft:assembler',
        input: [
            { item: 'nuclearcraft:fuel_plutonium_hep_242', count: 9 },
            { tag: 'forge:dusts/graphite' },
            { tag: 'forge:ingots/pyrolitic_carbon' },
            { tag: 'forge:ingots/silicon_carbide' }
        ],
        output: [{ item: 'nuclearcraft:fuel_plutonium_hep_242_tr', count: 9 }],
        timeModifier: 1.0,
        powerModifier: 1.0,
        radiation: 1.0
    })
})
```
---

### Your KubeJS Scripts Location
Place your custom fuel scripts in:
```
kubejs/
└── startup_scripts/
    └── custom_fuels.js  (or any .js file)
```

**Important:** Fuel registration must happen in **startup scripts**, not server scripts!

---

## Advanced Usage

### Custom Recipe Modifiers

You can customize how the fuel behaves in recipes using recipe modifiers:

```javascript
NCKJSEvents.RegisterFissionFuel(event => {
    event.registerFuel(
        'plutonium',
        'super-239',
        1500,               // forgeEnergy
        500,                // heat
        40,                 // criticality
        200,                // depletion
        120,                // efficiency
        239,                // isotope1
        240,                // isotope2
        0.8,                // timeModifier (20% faster)
        1.2,                // powerModifier (20% more power)
        1.5                 // radiationModifier (50% more radiation)
    )
})
```

#### Recipe Modifiers Explained

**timeModifier** (default: 1.0)
- Values < 1.0: Fuel depletes faster
- Values > 1.0: Fuel lasts longer
- Example: 0.5 = fuel lasts half as long

**powerModifier** (default: 1.0)
- Multiplies energy output
- Example: 1.5 = 50% more energy

**radiationModifier** (default: 1.0)
- Affects radiation produced
- Example: 2.0 = double radiation

### Using Double Values

For more precise control, you can use double values for criticality, depletion, and efficiency:

```javascript
NCKJSEvents.RegisterFissionFuel(event => {
    event.registerFuel(
        'thorium',
        'tbu-advanced',
        150,                // forgeEnergy
        25.5,               // heat (double)
        234.7,              // criticality (double)
        720.5,              // depletion (double)
        127.3,              // efficiency (double)
        230,                // isotope1
        232                 // isotope2
    )
})
```

### Special Fuel Types

Some fuel types don't generate all variants. To create a special fuel (like xenorium or quantite):

```javascript
NCKJSEvents.RegisterFissionFuel(event => {
    // Use a group name matching "xenorium.*" or "quantite.*"
    event.registerFuel(
        'xenorium',         // Special group
        'xen-300',
        2000,
        600,
        30,
        100,
        130,
        300,
        298
    )
})
```

**Note:** Special fuels only generate base variants, not oxide/nitride/zirconium alloy variants.

---

## Examples

### Example 1: Balanced Uranium Fuel

```javascript
NCKJSEvents.RegisterFissionFuel(event => {
    // Medium enriched uranium - balanced stats
    event.registerFuel(
        'uranium',
        'meu-235',
        600,                // Moderate power
        150,                // Moderate heat
        75,                 // Medium criticality
        200,                // Standard depletion
        105,                // Slightly efficient
        235,
        238
    )
})
```

### Example 2: High-Power Plutonium Fuel

```javascript
NCKJSEvents.RegisterFissionFuel(event => {
    // High power plutonium with increased radiation
    event.registerFuel(
        'plutonium',
        'hep-241-advanced',
        1400,               // High power
        450,                // High heat
        42,                 // Good criticality
        160,                // Burns faster
        115,                // Good efficiency
        241,
        240,
        0.9,                // 10% faster depletion
        1.1,                // 10% more power
        1.3                 // 30% more radiation
    )
})
```

### Example 3: Long-Lasting Thorium Fuel

```javascript
NCKJSEvents.RegisterFissionFuel(event => {
    // Efficient, long-lasting thorium fuel
    event.registerFuel(
        'thorium',
        'tbu-extended',
        140,                // Low power
        20,                 // Low heat
        220,                // Low criticality (needs more cells)
        900,                // Very long lasting
        130,                // Very efficient
        230,
        232,
        1.5,                // 50% longer lasting
        1.0,                // Normal power
        0.8                 // 20% less radiation
    )
})
```

### Example 4: Multiple Custom Fuels

```javascript
NCKJSEvents.RegisterFissionFuel(event => {
    // Add multiple fuels at once
    
    // Neptunium fuel line
    event.registerFuel('neptunium', 'hen-237', 1100, 380, 48, 190, 112, 237, 236)
    event.registerFuel('neptunium', 'len-237', 280, 65, 95, 190, 108, 237, 236)
    
    // Curium fuel line
    event.registerFuel('curium', 'hecm-244', 1250, 420, 44, 170, 114, 244, 243)
    event.registerFuel('curium', 'lecm-244', 310, 70, 88, 170, 110, 244, 243)
    
    // Experimental high-efficiency fuel
    event.registerFuel(
        'experimental',
        'ultra-fuel',
        2000,
        700,
        35,
        120,
        140,
        252,
        251,
        0.7,                // Burns 30% faster
        1.5,                // 50% more power
        2.0                 // Double radiation
    )
})
```

### Example 5: Modpack-Specific Balanced Fuel

```javascript
NCKJSEvents.RegisterFissionFuel(event => {
    // Custom fuel balanced for expert modpack
    // Requires rare isotopes but provides excellent performance
    event.registerFuel(
        'californium',
        'hecf-252',
        1800,               // Very high power
        550,                // Very high heat
        38,                 // Excellent criticality
        140,                // Burns quickly
        125,                // Excellent efficiency
        252,
        251,
        0.8,                // 20% faster burn
        1.25,               // 25% more power
        1.8                 // 80% more radiation
    )
})
```

---

## Troubleshooting

### Fuel Not Appearing
1. Make sure your script is in `kubejs/startup_scripts/`
2. Check the console for errors
3. Verify KubeJS is installed and loaded
4. Run `/reload` command in-game

### Recipes Not Generated
1. **Fission reactor recipes** are automatically generated for base and variant fuels (except TRISO)
2. **Crafting recipes** from isotopes are automatically generated
3. **Smelting recipes** for variants are automatically generated
4. **Processing recipes** (melter, assembler, etc.) are **NOT** automatically generated - you must add these manually via KubeJS
5. Ensure isotopes exist in the mod
6. Check that group and name don't conflict with existing fuels

### Fuel Not Working in Reactor
1. Verify the fuel item exists: `/give @p nuclearcraft:fuel_<group>_<name>`
2. Check that fission reactor recipe was generated
3. Ensure reactor is properly constructed
4. Check criticality requirements

### Can't Process Fuel (Melter, Assembler, etc.)
**This is expected!** Processing recipes are NOT automatically generated. You must:
1. Create custom recipes in `kubejs/server_scripts/` (see the "Important: Processing Recipes NOT Auto-Generated" section above)
2. Use KubeJS to add melter, ingot former, fluid infuser, assembler, and isotope separator recipes
3. Reference the example recipes provided in this guide

### Console Errors
Look for messages like:
```
[NuclearCraft]: Registered custom fission fuels via KubeJS
```

If you don't see this, your event registration may not be working.

---

## Best Practices

### Naming Conventions
- Use lowercase for group and name
- Use hyphens for separators in names
- Keep names descriptive but concise
- Follow existing naming patterns (heu, leu, etc.)

### Balance Considerations
1. **Energy vs Heat**: Higher energy should mean higher heat
2. **Criticality**: Lower values need bigger reactors
3. **Depletion**: Balance fuel lifetime with power output
4. **Efficiency**: Don't make fuels too efficient (breaks progression)

### Testing
1. Test in creative mode first
2. Build a test reactor for each fuel type
3. Monitor heat and energy output
4. Check depletion time
5. Verify all variants work

### Documentation
Document your custom fuels for players:
- List all custom fuels
- Explain their properties
- Provide reactor designs
- Note any special requirements

---

## Reference: Default Fuel Values

### Uranium Fuels
```javascript
// HEU-233: High power, high heat
forge_energy: 1152, heat: 360, criticality: 39, depletion: 133, efficiency: 115

// HEU-235: High power, high heat
forge_energy: 960, heat: 300, criticality: 51, depletion: 240, efficiency: 105

// LEU-233: Low power, low heat
forge_energy: 288, heat: 60, criticality: 78, depletion: 133, efficiency: 110

// LEU-235: Low power, low heat
forge_energy: 240, heat: 50, criticality: 102, depletion: 240, efficiency: 100
```

### Thorium Fuel
```javascript
// TBU: Very low power, very low heat, long lasting
forge_energy: 120, heat: 18, criticality: 234, depletion: 720, efficiency: 125
```

Use these as reference points for balancing your custom fuels.

---

Happy modpack developing!