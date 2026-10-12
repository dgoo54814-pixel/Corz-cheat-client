# Corz-Inspired Fabric Client — Minecraft 26.1.2 (source v0.2.0)

An original, unofficial client inspired by the **appearance** of the screenshots provided by the user. **No CORZ private source, branding assets, subscription, authentication or "cracked" features are included.** Use your own legitimate Minecraft account and the official launcher/Fabric.

## New modules in this version

### Schematic Auto Build (partial, opt-in)
- Reads **Sponge v2/v3 `.schem`** files from `.minecraft/config/corzstyle/schematics/`.
- Use `/corz load example_v2.schem` to load a file, and **B** or `/corz build` to start/pause.
- The anchor is placed **two blocks east and south** of your position when you first start.
- Uses ordinary client/server right-click interactions, at most **1 block per 5 client ticks**.
- Requires the exact block in your **hotbar**, a reachable position (**4.5 blocks**), an adjacent support block, permission to build, and a server that accepts the placement.
- **Not a full Litematica printer**: does not auto-craft, pathfind, move your character, rotate complex blockstates, handle block entities or survival logistics, or work on inaccessible blocks. Nondefault blockstates (`[...]`) are skipped. It never writes blocks directly to the server.
- Limit: 200,000 positions per schematic; 16 MB input `.schem` file limit. A v2 and v3 **example** are in `/examples/` — copy one to the schematics folder first. Use `/corz status` for progress.

### AH Sell (opt-in)
- Enable **AH Sell** in the DonutSMP/Misc category.
- Set a price by typing `/corz ah 2500` (substitute your actual price).
- Hold the item you want to list and press **H**. The mod sends the normal `/ah sell 2500` command.
- It does **not** bypass server rules, handle inventory automatically, verify server confirmation, or guarantee sale. Price defaults to 1000 and is saved in `.minecraft/config/corzstyle.properties`.

### RTP Base Finder (heuristic, opt-in)
- Enable **RTP Base Finder** in the DonutSMP/Misc category.
- Press **J** or `/corz rtp` to request `/rtp` once; scan starts only after a meaningful position change. There's a **20-second cooldown**.
- Press **K** or `/corz scan` for a manual scan around where you stand.
- Scans **client-loaded blocks only** within a bounded area; detects indicators such as chests, barrels, beds, crafting tables and furnaces. A heuristic score may produce false positives or miss hidden bases.
- Candidates are logged to `.minecraft/config/corzstyle/base-candidates.csv`.
- **Not a seed cracker, x-ray, packet exploit, remote world downloader, or guarantee of locating bases.** A server may block `/rtp` or forbid automation.

## Existing project features

Black-and-purple ClickGUI; category navigation; search, scrolling, toggles and settings saved to `.minecraft/config/corzstyle.properties`. Seven earlier coded modules remain: Aim Assist, Trigger Bot, AutoClicker, Key Pearl, Auto XP, Auto Sprint and Fullbright. Other displayed modules explicitly say `SOON` rather than pretend to function.

### Hotkeys

| Key | Action |
|---|---|
| Right Shift | ClickGUI (open/close) |
| V | Key Pearl (when enabled) |
| B | Start/pause schematic builder (when enabled and loaded) |
| H | Sell held item for configured AH price (when enabled) |
| J | Send `/rtp` and begin local base scan after teleport (when enabled) |
| K | Scan nearby loaded blocks (when enabled) |

Hotkeys can be reassigned under **Minecraft Settings → Controls → Key Binds**.

### Client commands

`/corz load example_v2.schem` — load schematic (quote names containing spaces)

`/corz build` — start/pause build

`/corz ah 2500` — set AH price (does NOT send listing until you press H)

`/corz rtp` — request one RTP and scan after teleport

`/corz scan` — manual scan

`/corz status` — show builder and helper status

## Building / installing

**This ZIP is source, not a Minecraft mod JAR.** Don't put the ZIP in `.minecraft/mods`.

1. Install **JDK 25** and **Gradle 9.4+** (or import the Gradle project through a compatible IDE; no Gradle wrapper binary is bundled).
2. Extract the ZIP, open the inner `corz-inspired-26.1.2-source` folder as a Gradle project, then run `gradle build` from that directory.
3. When the build passes, place `build/libs/corz-inspired-client-0.2.0.jar` into `.minecraft/mods`.
4. Install **Fabric Loader for Minecraft 26.1.2**, plus **Fabric API 0.155.3+26.1.2** as a separate `.jar` in `.minecraft/mods`.
5. Start the matching Fabric profile using a legitimate Minecraft account.

## Verification and limitations

- Pure-Java schematic parser **compiled and passed two local test schematics** (v2/v3) under Java 21.
- **Full Minecraft/Fabric project is NOT compiled or game-tested in this environment.** Java 25 and Gradle/dependency downloads were unavailable here; API/build errors are possible. No working JAR or no-error guarantee is claimed.
- Backend-only CORZ features (base listings, licensing) are not cloned.
- For the two example schematics, only the simple default cube blocks are supported by auto-build.
- Auto actions may be prohibited by some servers. Test on singleplayer or a server where you have permission.

References:
- https://docs.fabricmc.net/26.1.2/develop/
- https://github.com/SpongePowered/Schematic-Specification/blob/master/versions/schematic-2.md
- https://github.com/SpongePowered/Schematic-Specification/blob/master/versions/schematic-3.md
