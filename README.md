# Sanguine Neural Networks — NeoForge 1.21.1

The `1.21.1` branch ports the Virtual Sacrificer from Forge 1.20.1 to Java 21 and NeoForge. It integrates with [Neo Vitae](https://github.com/breakinblocks/NeoVitae) and [Hostile Neural Networks](https://github.com/Shadows-of-Fire/Hostile-Neural-Networks/tree/1.21).

## Dependencies

The development environment pins NeoForge 21.1.249, Hostile Neural Networks 6.5.1, Placebo 9.9.2, and Neo Vitae 1.1.19. Neo Vitae also requires GeckoLib and Modonomicon; the build includes their NeoForge releases. JEI is optional for players and included in development runs. Mekanism 10.7.19.85 is a runtime-only development dependency. Extra Hostile Neural Networks 2.2.5 is also included at runtime for development and declared optional for players.

## Usage

Shift-right-click an Ara Vitae with the wrench, then shift-right-click the Virtual Sacrificer to link it. Supply FE and an entity data model. Generalized predictions and the existing catalysts boost production. Output goes into the altar's main EV tank and respects sacrifice runes. The linked altar must remain loaded in the same dimension.

Crafting uses Tabula Rasa instead of Blank Slate, Tabula Animata instead of Imbued Slate, and a Rune of Sacrifice instead of the Dagger of Sacrifice. The binding reagent remains part of the machine recipe.

## Extra HNN combined models

The Virtual Sacrificer accepts Extra HNN combined models when Extra HNN is installed. Every inner model must have a blood recipe. Energy per tick is **10 × the sum of all four recipe costs**, independent of Extra HNN tier. The whole combined cycle consumes **one catalyst use** and grants the configured data gain once.

Each inner model continues above the final blood amount in its existing JSON recipe: `next = current + 2 × (current − previous)`. Outputs are calculated per inner model, then summed before the usual catalyst and receiving-altar bonuses. For blaze's `0, 125, 250, 375, 750`, the five Extra HNN tiers yield `1500, 3000, 6000, 12000, 24000` EV per inner model. Four Autonomous blaze models therefore produce 6,000 EV before bonuses and cost 40,000 FE/t. Combined models are rejected by Suffering Incorporated, including its model input ports.

Use `-PwithoutExtraHnn` to launch development runs without this optional mod.

## Suffering Incorporated

Build the same hollow 7×7×7 shell as an HNN Data Center, using 49 Neo Vitae blank runes as the floor and blood-stained glass for the walls and ceiling. Place the controller in a wall directly above the floor. Any Neo Vitae altar rune may replace a blank rune; shell runes have no mechanical effect.

The controller holds 25 models and four catalysts. Only Self-Aware models run. Models cycle independently every 300 ticks by default, producing liquid Essentia Vitae (1 mB per EV) into a shared 4,000,000 mB tank. Catalyst multipliers add: four 5× catalysts give 20×. Every completed model cycle spends one use from each active catalyst slot. A loaded catalyst retains its remaining charges even after its item stack is removed; spent catalysts refill from that slot. Infinite catalysts display `Uses: ∞`.

Energy cost per running model is its recipe cost, multiplied by 1×, 1×, 1.5×, 2×, or 2.5× for zero through four active catalyst slots. The energy buffer defaults to 25,000,000 FE. Incomplete structures, insufficient energy, and full output storage pause production.

Replace wall or ceiling glass with **Suffering Incorporated I/O Ports**. Empty-hand right-click cycles **energy → models → catalysts → output**. Model ports accept models; catalyst ports insert/extract catalyst items. Output ports expose the shared fluid tank for extraction. To send output straight to an Ara Vitae, shift-right-click the altar with the wrench, then the output port. Linked transfers respect altar capacity, loaded chunks, and the receiving altar's sacrifice bonuses, just like the Virtual Sacrificer. Shift-right-click the port with an empty hand to unlink it.

The controller recipe uses two Virtual Sacrificers, four blank runes, two blood-stained glass blocks, and a Tabula Spiritus. Four ports use four blank runes, four blood-stained glass blocks, and a comparator. JEI includes construction and operation information for both blocks.

## Feature toggles

In `config/sanguine_networks-common.toml`, both options default to `true`:

- `extra_hnn_models_enabled`: allows combined models in the Virtual Sacrificer. Setting it to `false` rejects new insertions and stops combined models already inside; ordinary HNN models still work.
- `suffering_enabled`: enables the Suffering Incorporated multiblock and its ports. Setting it to `false` stops formation, processing and port transfers while preserving placed blocks, items, energy and fluid. Re-enabling restores normal structure validation.

## HNN artwork and animation

Suffering Incorporated GUI, controller and port artwork, plus the JEI backgrounds, are extracted from the pinned HNN dependency. The Virtual Sacrificer keeps its original block textures, model and GUI artwork. Imported assets use SNN names, including `suffering_incorporated_*`, `suffering_io_port_*` and `jei/virtual_sacrificer.png`. The accent ramp is remapped using the corresponding colors in `textures/block/color_stripes.png`; dimensions, neutral pixels, transparency, model geometry and animation metadata are preserved. Rebuild these assets with Pillow installed:

```sh
python3 scripts/import_hnn_assets.py [path/to/hostile-neural-networks.jar]
```

The importer also generates the Java text/hologram palette. The Data Center renderer follows HNN's original 12 display positions, deterministic random selection, 100-tick animation envelopes and spin. Its GUI uses HNN's layout, left-side status details and redstone button; four catalysts occupy the input row, and liquid EV occupies the output area. HNN's MIT notice is included in the JAR.

## Build and validation

Use Java 21:

```sh
./gradlew build
./gradlew runData
./gradlew -PgameTests runGameTestServer
```

The JAR is written to `build/libs/`. `runClient` and `runServer` launch development environments. GameTest fixtures are enabled only with `-PgameTests` and excluded from the mod JAR.

The runtime tests cover EV production with catalyst/rune bonuses, FE and item capabilities, model progression, persistence, full-altar pausing, invalid models/catalysts, wrench linking, recipe codecs, multiblock formation, decorative runes, all 25 parallel models, additive catalysts, charge exhaustion, infinite uses, port capabilities, and linked output overflow. GUI appearance and JEI rendering still require a client check.

## Datapacks

Recipes now live under `data/<namespace>/recipe/`, with corresponding singular `advancement`, `loot_table`, and `tags/block` directories. The custom `blood` and `catalyst` recipe IDs and JSON fields are retained. Blood recipes require five non-negative amounts, ordered from the lowest HNN tier upward; extra custom tiers use the final amount. The wrench crafting recipe is now `sanguine_networks:wrench`.

This is a code port; no automatic conversion of Blood Magic blocks or old 1.20.1 item NBT is provided.

## Block data models (1.21.1)

HNN's `Enable Block Data Models` option in `config/hostilenetworks.cfg` controls loading its built-in block models. Enable it and restart or reload data. SNN resolves models from HNN's synchronized registry, so client config differences do not hide server-enabled models.

Custom blood recipes can target a block instead of an entity. For example, put this in `data/<namespace>/recipe/blood/iron_ore.json`:

```json
{
  "type": "sanguine_networks:blood",
  "block": "minecraft:iron_ore",
  "blood": [0, 10, 20, 30, 40],
  "energy": 200
}
```

These amounts are an example, not a bundled ore balance. Specify exactly one of `block` or `entity`. HNN block variants are recognized. Block models work in the Virtual Sacrificer and JEI, and Self-Aware block models work in Suffering Incorporated. Extra HNN combined models may contain block models and still use ten times the summed power cost and one catalyst use per combined cycle; combined models remain excluded from the multiblock.

The Forge 1.20.1 branch uses HNN 5.3.3, whose models support entities only.
