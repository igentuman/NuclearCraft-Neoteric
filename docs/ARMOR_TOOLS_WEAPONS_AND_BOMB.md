# Armor, tools, weapons and bomb

This guide covers equipment implemented in NuclearCraft: Neoteric for NeoForge
1.21.1. Values below are defaults; modpack recipes and server configuration can
change them. Use JEI/EMI for the recipes available in your pack.

## Armor

Both sets include a helmet, chestplate, leggings and boots. Defense values are
armor points: two points fill one armor icon.

| Set | Helmet | Chestplate | Leggings | Boots | Full set defense | Toughness per piece | Knockback resistance per piece |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Tough Alloy | 3 | 8 | 6 | 3 | 20 | 3.5 | 0.2 |
| HEV | 3 | 7 | 5 | 3 | 18 | 4.0 | 0.3 |

### Tough Alloy armor

Craft Tough Alloy armor from Tough Alloy ingots using the usual armor patterns.
Repair it with Tough Alloy ingots. It requires no power and has an
enchantability of 15.

### HEV armor

Craft each HEV piece from the corresponding Tough Alloy armor piece, Extreme
Alloy plates and Lithium-Ion Cells. HEV armor is unbreakable and has an
enchantability of 25.

Each piece stores two independent charges:

- **FE:** 1,000,000 FE per piece by default. Charge it in a powered Charging
  Station. The item bar shows FE charge.
- **Quantite Energy (QE):** up to 10,000 per piece. Supply Quantite Energy fluid
  to a Charging Station to replenish this charge. The tooltip shows QE
  separately from FE.

Bonuses apply while the piece is worn and has the corresponding charge.

| Piece | With FE charge | With QE charge |
| --- | --- | --- |
| Helmet | Water Breathing | Night Vision and Water Breathing |
| Chestplate | Absorption II | Flight |
| Leggings | Speed II | No additional individual effect |
| Boots | No individual FE effect | Jump Boost II |

Wearing all four pieces with QE remaining adds **1 armor point**, bringing the
set to 19. Each worn piece consumes 1 QE per second; the chestplate consumes
another 1 QE per second while flying. Keep track of chestplate charge during
flight: its flight permission ends when QE runs out. The FE effects currently
check for a positive charge without consuming FE in the armor tick.

Radiation mechanics belong to the separate
[Nuclear Radiation mod](https://github.com/igentuman/Nuclear-Radiation/blob/1.21.1/README.md).
Consult its documentation for radiation protection rules in your pack.

## Tools

### Spaxelhoes

Spaxelhoes are general mining tools that use their material's mining speed on
all blocks and count as a correct tool for drops. Their current implementation
extends a pickaxe; the name does not imply axe stripping or hoe tilling actions.

| Tool | Durability | Mining speed | Enchantability | Repair ingredient | Obtaining |
| --- | --- | --- | --- | --- | --- |
| Tough Alloy Spaxelhoe | 10,000 | 12 | 22 | Tough Alloy ingot | Assembler: 5 Tough Alloy plates + 2 iron ingots |
| Thorium Spaxelhoe | 10,000 | 10 | 18 | Thorium ingot | Crafting: 5 Thorium plates + 2 iron ingots |

Both items are fire resistant. Unbreakable blocks still cannot be mined normally.

### QNP

The QNP is an FE-powered mining tool with area and vein mining modes. Assemble
it from 3 HSLA Steel plates, a Basic Electric Circuit, a Lithium-Ion Cell, a
Magnesium Diboride Coil and an Actuator.

- Capacity: **2,097,152 FE** by default.
- Base mining cost: **400 FE per block** harvested by its mining routine.
- Powered mining speed: **20**; insufficient energy reduces it to **0.1**.
- Unbreaking reduces the per-block cost to the base cost divided by
  `sqrt(level + 1)`, rounded down.

Charge it in a Charging Station, or right-click a block that exposes extractable
FE. **Sneak-right-click in the air** to cycle through:

`1 block → 3×3 → 3×3×3 → 5×5 → 5×5×5 → 7×7 → vein miner`

Flat areas follow the face you mine. Area mining targets blocks mineable with
a pickaxe or shovel and skips block entities and unbreakable blocks. Vein mode
follows face-connected blocks with the same block state, starting from a block
in the common ores tag. Drops use the tool's enchantments; additional drops
are collected and dropped in front of the player.

Larger modes require an energy reserve for their full area before each harvest
step, even if some positions are empty. The reserve check uses the base cost
and requires **more than** the calculated amount; Unbreaking reduces actual
consumption but does not reduce that reserve requirement. Recharge or select a
smaller mode if mining slows down while FE remains.

### Multitool

Assemble a Multitool from 3 Steel plates, Lead dust, a Motor and an Actuator.
It serves as a wrench for supported blocks, including beam port mode controls
and storage side configuration, and as a remote detonator.

To link a detonator, right-click TNT or a Pu-239 bomb. You must be the player
who placed the Pu-239 bomb. The tooltip shows the linked coordinates. See the
bomb instructions below for remote arming.

## Q36 Quantite Disruptor

The Q36 is a ranged weapon with Pulse and Beam modes. Its Assembler recipe uses
2 Basic Processors, a Basic Electric Circuit, 8 iron bars and 4 BSCCO Coils.

Hold it in your **main hand**. Right-click to switch modes; left-click to fire.
Holding the attack control fires again when the cooldown expires.

| Mode | Default damage | Charge per shot | Cooldown at 20 TPS | Behavior |
| --- | --- | --- | --- | --- |
| Pulse | 50 | 200 QE | 10 ticks / 0.5 s | Fires a projectile that damages the entity it hits. |
| Beam | 200 | 2,000 QE | 60 ticks / 3 s | Instant ray with a default range of 64 blocks and impact area damage. |

Damage values are health points before applicable damage handling; two health
points equal one heart. Beam impact damage also affects nearby living entities
within 3 blocks, excluding the shooter and the directly hit entity. When the
beam does not hit an entity, it can destroy blocks near its endpoint, with a
default break radius of 2 blocks. Unbreakable blocks are skipped, and block
break cancellation events are respected.

The Q36 holds **100,000 QE**, enough for 500 Pulse shots or 50 Beam shots at
default costs. A new item with no stored charge data starts full. Recharge it
with **Quantite Energy fluid in a Charging Station**; FE alone does not recharge
the weapon. It cannot be repaired or enchanted.

Both firing modes have special interactions with anomaly entities. Their
response is handled by the anomaly rather than normal weapon damage.

## Charging Station

Insert the equipment into the Charging Station's item slot. Supply FE for the
QNP and HEV FE charge, and Quantite Energy fluid for Q36 and HEV QE charge.
The station can charge both HEV stores in the same tick when supplied with both.

Quantite Energy fluid is produced in the Irradiator from Quantite and
1,000 mB of Subliquid Matter, yielding 1,000 mB of Quantite Energy.

The station converts **1 mB into 50 charge**, consuming up to 20 mB per tick for
Q36 or HEV. From empty, a Q36 needs 2,000 mB and each HEV piece needs 200 mB.
FE charging transfers up to 500,000 FE per tick, subject to available power and
the item's receive limit.

## Pu-239 bomb

The implemented bomb is the **Pu-239 bomb** (`nuclearcraft:pu_239_bomb`). Craft it
with a Pu-239 Core in the center surrounded by 8 Compression Charges. Follow
the core and pit recipes in JEI/EMI for its component chain.

### Placement and arming

Place the bomb yourself. Dispenser or fake-player placement is rejected and
returns the block as an item. The bomb records the placing player's ownership.

There are two ways to arm it:

1. **Redstone:** apply a signal to the bomb. Its default fuse is **60 ticks
   (3 seconds at 20 TPS)**. Removing the signal does not cancel an armed fuse.
2. **Multitool:** right-click your bomb to link it, then move away within the
   same dimension. Right-click the air once to request remote activation and
   again within 60 ticks to confirm. Confirmation starts the bomb's normal
   fuse; it does not bypass the fuse delay.

The remote link stores coordinates without a dimension, so use it in the
dimension where you linked the bomb. Armed bombs force-load their chunk to
keep the fuse running.

### Blast and fallout

Default terrain blast radii are **196 blocks horizontally** and **64 blocks
vertically**. The simulation destroys and transforms terrain over multiple
ticks; it does not complete all block changes in a single tick. Ground can
become wasteland terrain, and nuclear blast recipes can define block
transformations.

**Entity damage extends beyond the terrain blast.** Its ellipsoid uses the
terrain radii plus 100 blocks: **296 horizontally and 164 vertically** with
default settings. Damage falls with distance, walls attenuate it, and surviving
targets can be set on fire. The terrain radius is not a safe viewing distance.

With Nuclear Radiation installed and bomb radiation enabled, detonation also
creates a persistent radiation source at the epicenter and deposits fallout
in affected chunks. Without that mod, the terrain and damage simulation still
operates.

### Configuration

See [Mod configuration](CONFIGURATION.md) for config locations and reload rules.

| File | Settings relevant to this guide |
| --- | --- |
| `common.toml` | QNP capacity and base mining cost; HEV FE capacity per piece. |
| `q36.toml` | Shot costs, damage, cooldowns, beam range, block break radius and visual effect duration. |
| `bomb.toml` | Blast radii, fuse, block operations per tick, chunk resend rate, fast block writes and fallout settings. |
| `entries.toml` | Availability of individual equipment entries. |

Bomb fast block writes are enabled by default. They improve processing speed
but skip normal neighbor updates, fluid flow, falling-block triggers and block
events inside the blast. Disable `fast_block_writes` to use the slower normal
block update path. Processing and chunk resend limits affect how quickly the
blast completes and becomes visible to clients.