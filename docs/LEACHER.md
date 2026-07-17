# Leacher (In-Situ Leaching)

The Leacher is a single-block processor that performs chemical extraction of ores from a
chunk. Instead of mining and crushing ore blocks, you dissolve them in place into a fluid
slurry, which then feeds downstream processing chains.

## Placement Requirements

The Leacher works only when:

1. Placed centrally inside the target chunk.
2. Fed `aqua_regia_acid` fluid (consumed `250 mB` per operation).
3. **Four `pump` blocks** are present, one at each chunk corner, **one block above the
   Leacher's Y level**.
4. Each pump must have **2 solid blocks** beneath it (`PumpBE.isInSituValid`).

Invalid corners are highlighted in-world by the `BlockOverlayHandler`.

## Status Codes

| Code | Meaning |
|---|---|
| `0` `NO_ACID` | Acid tank is empty |
| `1` `POSITION_IS_CORRECT` | Ready |
| `2` `WRONG_POSITION` | Not centered in chunk / wrong Y |
| `3` `NO_SOURCE` | No catalyst that points to a vein |
| `4` `PUMPS_ERROR` | One or more pumps invalid |

## Catalyst Slot

The catalyst tells the Leacher *what* and *where* to extract. Accepted items:

| Catalyst | Mode |
|---|---|
| `nuclearcraft:research_paper` | Read NBT `pos` / `vein`, must match current chunk; pulls a weighted random ore from the chunk's vein definition |
| Vanilla `filled_map` | Mines real ore blocks inside the map bounds (destructive; uses fake player `[NC]LEACHER`) |
| `immersiveengineering:coresample` *(if IE loaded)* | Pulls from the IE mineral vein at that location |

## Output

- **Fluid output (default):** `<material>_slurry`, `1000 mB` per recipe. A slurry exists for
  every material in `Materials.slurries()`.
- **Item output:** empty in fluid mode; for the map-catalyst mode it produces real ore stacks.

Slurries feed normal NuclearCraft processing chains (e.g. centrifuge / electrolyser).

## Ore Vein System

- `OreVeinRecipe` JSON files define weighted ore pools per vein type.
- `WorldVeinsManager` (SavedData `nc_world_veins`) tracks per-chunk depletion via
  `WorldVeinOres`.
- `OreVeinProvider` provides deterministic per-position RNG.
- The `Analyzer` block produces `ResearchPaper` items that encode the surveyed chunk and
  its vein contents.

### Workflow

1. **Survey:** craft / power an `Analyzer`, target it at the chunk you want to leach.
2. **Produce research paper:** the Analyzer outputs a `research_paper` with the chunk
   coordinates and vein data baked in.
3. **Place Leacher + 4 pumps:** make sure status is `POSITION_IS_CORRECT`.
4. **Insert research paper** into the catalyst slot, supply `aqua_regia_acid`, supply power.
5. The Leacher streams slurry to its fluid output until the vein depletes.

## Commands

- `/vein_check` - dump the vein assigned to the current chunk (see `VeinCheckCommand`).

## Related Files

- `block/entity/processor/LeacherBE.java`
- `recipes/type/OreVeinRecipe.java`, `util/insitu_leaching/`
- `datagen/recipes/recipes/LeacherRecipes.java`
- `AnalyzerBE.java`, `ResearchPaperItem.java`
