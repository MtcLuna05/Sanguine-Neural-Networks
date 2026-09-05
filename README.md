# Sanguine Neural Networks 2.0 — Forge 1.20.1

Virtual Sacrificers produce Blood Magic Life Essence from Hostile Neural Networks models. This branch uses Blood Magic; the NeoForge 1.21.1 branch uses Neo Vitae and includes Suffering Incorporated. The multiblock and its ports are not included here.

## Dependencies

- Minecraft 1.20.1 / Forge 47
- HNN 5.3.3 and Placebo 8.6.3
- Blood Magic 3.3.3-45 and its dependencies
- Optional [Extra HNN 1.2.2](https://www.curseforge.com/minecraft/mc-mods/extra-hostile-neural-networks/files/8004392)
- Mekanism is a development runtime dependency only.

## Combined models

A combined model sums the blood output of its four inner models. Its energy cost per tick is `10 × (cost1 + cost2 + cost3 + cost4)`. Each completed combined cycle spends one catalyst use and awards the configured data once.

Extra HNN tiers continue above the final JSON tier: `next = current + 2 × (current − previous)`, calculated for each inner model. For blaze `[0, 125, 250, 375, 750]`, the extra tier outputs are `1500, 3000, 6000, 12000, 24000` LP per inner model.

Set `extra_hnn_models_enabled = false` in `config/sanguine_networks-common.toml` to disable combined-model insertion and operation. Defaults to true; ordinary HNN models remain available.

JEI uses HNN artwork recolored through `color_stripes.png`, with Life Essence fluid outputs and `Uses: ∞` for unlimited catalysts. The original Virtual Sacrificer artwork is preserved, with normalized player inventory spacing. Regenerate imported artwork with `python3 scripts/import_hnn_assets.py` (Pillow required, optional HNN JAR path argument).

## Development

Use Java 17:

```sh
./gradlew build
./gradlew runData
./gradlew -PgameTests runGameTestServer
./gradlew -PgameTests -PwithoutExtraHnn runGameTestServer
```

GameTest fixtures are excluded from the release JAR. Builds produce `build/libs/sanguine_networks-1.20.1-2.0.jar`.

HNN 5.3.3 on 1.20.1 has no block data model API or enabling config. Block-model blood recipes are supported on the 1.21.1 branch only.
