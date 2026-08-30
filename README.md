# XP Tonic — NeoForge + Fabric, MC 1.21.1 + 26.2

Hostile mobs rarely drop a **Potion of XP Boost**. Drinking it multiplies by five every point of experience
you gain — mobs, ores, furnaces, fishing, breeding. When it runs out, you go looking for another
bottle.

## The three bottles

Same shape as vanilla's own potion families (`Potions.STRENGTH`), so the trade-off is one players
already know. All three are called "Potion of XP Boost"; the tooltip is what tells them apart,
exactly as vanilla's long and strong Strength potions share one name.

| | duration | multiplier | extra XP at 50/min | share of drops |
|---|---|---|---|---|
| **XP Boost** | 8:00 | x5 | ~1600 | 60% |
| **XP Boost II** | 8:00 | x6 | ~2000 | 25% |
| **XP Boost +** | 20:00 | x5 | ~4000 | 15% |

Rarity and payoff line up: the marathon bottle is worth the most, so it is the hardest to get.
These durations are far longer than the vanilla potions they are shaped after — vanilla prices its
3:00 for something you brew on demand, while this one drops about once in forty kills.

**Rarity is the only axis.** One roll decides whether anything drops, at vanilla's own rare-drop
rate — **2.5%, plus one point per level of Looting**, so 5.5% at Looting III, the same numbers
behind a zombie's iron ingot. A second roll picks the bottle. Nothing depends on which mob died or
where, so there is no rule to document and nothing to maintain as Minecraft adds mobs.

At Looting III that works out to roughly 3.3% for the baseline, 1.4% for the concentrated one and
0.8% for the marathon — about one in 120 kills.

## The rules

- **Drop:** hostile mobs (`MobCategory.MONSTER`) killed by a player. Drowning, suffocation and
  fall-damage farms do not qualify — the killing blow has to come from the player, directly or
  through a projectile.
- **Re-drinking** refreshes the duration; the multiplier never stacks. Drinking a II over a I gives
  x3 for 1:30, per vanilla's amplifier rules.
- **Never multiplies a cost.** Enchanting and anvil work reach `giveExperiencePoints` as negative
  amounts; those pass through untouched.

**The liquid pulses.** Vanilla's experience orb is not an animated texture — it is one static
sprite whose vertex colours ride two offset sines, green pinned at full while red sweeps. The
bottle borrows that cycle, so it shifts green to yellow and back every two seconds. It costs no
sprite and no client code: 1.21.1 and 26.2 reach the tint by different routes but both end at
`PotionContents.getColorOptional`, so one mixin in `common/` covers every target.

Client-side install required (`MOB_EFFECT` and `POTION` are static registries), but the mod ships
no client code — the bottle reuses the vanilla potion model, and the only client asset is the
18x18 effect icon.

**Fabric builds require Fabric API.** Not for convenience: Fabric Loader on its own does not expose
a mod's `assets/` as a resource pack — that is `fabric-resource-loader-v0`. Without it a Fabric
client mounts `vanilla` only and silently ignores every texture and lang file in the mod, which
shows up in game as a missing-texture effect icon and raw translation keys for item names. NeoForge
needs no equivalent. Watch the `Reloading ResourceManager:` line in the log to tell at a glance
whether the mod's pack was picked up.

## Build

Needs JDK 25 for the Gradle process itself, even for the 1.21.1 line whose toolchain is Java 21.

```powershell
$env:JAVA_HOME = 'C:\path\to\jdk-25'
```

```bash
./gradlew build                             # 26.2 (the default line)
./gradlew build -Pminecraft_version=1.21.1  # the other line of MATRIX
```

In PowerShell the property has to be quoted — `'-Pminecraft_version=1.21.1'` — or the shell mangles
it and Gradle reads `1`. Four jars come out, one per loader x version.

## Test it in game

`./gradlew runClient`, then in a creative world:

```
/give @s minecraft:potion[minecraft:potion_contents={potion:"betterexp:xp_boost"}]
/give @s minecraft:potion[minecraft:potion_contents={potion:"betterexp:long_xp_boost"}]
/give @s minecraft:potion[minecraft:potion_contents={potion:"betterexp:strong_xp_boost"}]
```

All three are in the creative inventory too, along with their splash, lingering and tipped-arrow
variants — vanilla generates those for every registered potion.

**The multiplier, measured.** `/xp add ... points` calls the exact method the mod hooks, so this is
a number, not an impression:

```
/data get entity @s XpTotal
/xp add @s 10 points
/data get entity @s XpTotal
```

+10 with no effect, +50 under `xp_boost`, +60 under `strong_xp_boost`.

**The guard rail.** `/xp add @s -10 points` must move XpTotal by -10, effect or no effect. Same
thing for real: enchanting with and without the effect must cost the same levels.

**The drop, without grinding.** The variant depends on where the mob dies:

```
/summon minecraft:zombie ~ ~ ~2
/damage @e[type=zombie,limit=1,sort=nearest] 100 minecraft:player_attack by @s
```

```
/damage @e[type=zombie,limit=1,sort=nearest] 100 minecraft:drown
```

The first is eligible, the second never drops. A cow killed by the player never drops either.

At 2% those commands need ~100 zombies to prove anything. To check the *mechanism* in 30 seconds,
set `BASE_CHANCE` to `1.0F` in `PotionDrop.java`, rebuild, and repeat: the player kill drops every
time, the drowning and the cow never do. Then put `0.02F` back. `/enchant @s minecraft:looting 3`
covers the Looting side.

Worth walking through one at a time:

| Case | Expected |
|------|----------|
| Kill hostiles with and without Looting | drops at roughly 2.5% / 3.5% / 4.5% / 5.5% |
| Let a mob drown or fall | no drop, ever |
| Kill a passive mob | no drop |
| Drink, then mine coal, smelt, fish, breed | every source pays five times |
| Drink, then enchant or rename at an anvil | the level cost is unchanged |
| Drink again while boosted | duration resets, multiplier does not stack |
| Die while boosted | effect is gone, like any vanilla effect |
| Two players, one boosted | only the boosted one is multiplied |

## Where things live

`common/` holds everything: registration, the multiplier, the drop, and the two mixins that hook
them in. `fabric/` and `neoforge/` are entry points plus one Fabric-only mixin — see
`BuiltInRegistriesMixin` for why Fabric needs its own registration window.

The mod id stays `betterexp` — it predates the name and is baked into registry ids, package
names and asset paths. Renaming it after release would break existing worlds, so it stays.

The build's cross-version invariants are documented in comments where they bite — `MATRIX` and
the conditional Loom plugin id in `build.gradle` and `fabric/build.gradle`, and the registration
window in `BuiltInRegistriesMixin`.
