# API Examples

Complete, compilable examples using the RAS public API.

All examples assume you're in server-side code. Wrap API calls in `if (player instanceof ServerPlayer)` or `isAvailable()` guards as appropriate.

---

## Basic Guard Pattern

Always check that RAS is available before calling the API:

```java
import tn.nightbeam.ras.api.RasApi;

public void onServerStart() {
    if (RasApi.isAvailable()) {
        System.out.println("RPG Attribute System is loaded!");
    }
}
```

---

## Reading Player Level

```java
import net.minecraft.server.level.ServerPlayer;
import tn.nightbeam.ras.api.RasApi;

public class MyLevelChecker {
    public static int getRpgLevel(ServerPlayer player) {
        return RasApi.getLevel(player);
    }
}
```

For mixed-side safety:

```java
import net.minecraft.world.entity.player.Player;
import tn.nightbeam.ras.api.RasApi;

public static int safeGetLevel(Player player) {
    if (!RasApi.isAvailable()) return 0;
    return RasApi.getLevel(player);
}
```

---

## Reading Combat Snapshot

Use `CombatSnapshot` for NPC scaling, loot tables, quest requirements, or any balancing that needs the player's actual combat stats after RAS, gear, and effects:

```java
import net.minecraft.server.level.ServerPlayer;
import tn.nightbeam.ras.api.RasApi;
import tn.nightbeam.ras.api.CombatSnapshot;

public class MobScaler {
    public static double calculateMobHealth(ServerPlayer player, double baseHealth) {
        CombatSnapshot snap = RasApi.getCombatSnapshot(player);

        double playerDamage = snap.attackDamage();
        double playerHealth = snap.maxHealth();
        int playerLevel = snap.rpgLevel();

        // Scale mob health based on player combat stats
        double scale = 1.0 + (playerLevel * 0.05) + (playerDamage * 0.1);
        return baseHealth * scale;
    }

    public static void printStats(ServerPlayer player) {
        CombatSnapshot snap = RasApi.getCombatSnapshot(player);
        System.out.printf("Level: %d, HP: %.1f, Damage: %.1f, Armor: %.1f%n",
            snap.rpgLevel(),
            snap.maxHealth(),
            snap.attackDamage(),
            snap.armor()
        );
    }
}
```

---

## Performing a Respec

### Normal Respec (with costs)

```java
import net.minecraft.server.level.ServerPlayer;
import tn.nightbeam.ras.api.RasApi;
import tn.nightbeam.ras.api.RespecResult;

public class RespecHandler {
    public static boolean tryRespec(ServerPlayer player) {
        RespecResult result = RasApi.respec(player);

        switch (result) {
            case SUCCESS:
                player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal("Attributes reset!"));
                return true;
            case DISABLED:
                player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal("Respec is currently disabled."));
                return false;
            case NO_PERMISSION:
                player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal("You don't have permission to respec."));
                return false;
            case ON_COOLDOWN:
                player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal("Respec is on cooldown."));
                return false;
            case INSUFFICIENT_COST:
                player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal("You can't afford to respec."));
                return false;
            default:
                return false;
        }
    }
}
```

### Admin Respec (no restrictions)

```java
import tn.nightbeam.ras.api.RasApi;
import tn.nightbeam.ras.api.RespecOptions;
import tn.nightbeam.ras.api.RespecResult;

public class AdminCommands {
    public static void adminRespec(ServerPlayer target) {
        RespecResult result = RasApi.respec(target, RespecOptions.admin());
        if (result == RespecResult.SUCCESS) {
            System.out.println("Admin respec completed for " + target.getName().getString());
        }
    }
}
```

### Scroll of Rebirth Flow

```java
import tn.nightbeam.ras.api.RasApi;
import tn.nightbeam.ras.api.RespecOptions;
import tn.nightbeam.ras.api.RespecResult;

public class ItemUseHandler {
    public static boolean onScrollUse(ServerPlayer player) {
        RespecResult result = RasApi.respec(player, RespecOptions.item());
        return result == RespecResult.SUCCESS;
    }
}
```

### Custom Respec Options

```java
import tn.nightbeam.ras.api.RespecOptions;
import tn.nightbeam.ras.api.RasApi;

// Skip cooldown but keep cost requirements
RespecOptions customOptions = RespecOptions.defaults().withSkipCooldown();
RasApi.respec(player, customOptions);

// Skip cost but enforce cooldown
RespecOptions noCostOptions = RespecOptions.defaults().withSkipCost();
RasApi.respec(player, noCostOptions);
```

---

## Applying Templates

### Normal Template Application

```java
import net.minecraft.server.level.ServerPlayer;
import tn.nightbeam.ras.api.RasApi;
import tn.nightbeam.ras.api.TemplateResult;

public class ClassSelection {
    public static boolean selectClass(ServerPlayer player, String className) {
        TemplateResult result = RasApi.applyTemplate(player, className);

        switch (result) {
            case SUCCESS:
                player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal("Class selected: " + className));
                return true;
            case NOT_FOUND:
                player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal("Unknown class: " + className));
                return false;
            case INSUFFICIENT_POINTS:
                player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal("Not enough attribute points!"));
                return false;
            case INVALID_TEMPLATE:
                player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal("Template is misconfigured."));
                return false;
            case NO_PERMISSION:
                player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal("You can't select this class."));
                return false;
            case DISABLED:
                player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal("Class selection is disabled."));
                return false;
            default:
                return false;
        }
    }
}
```

### Admin Template Application (bypass point check)

```java
import tn.nightbeam.ras.api.RasApi;
import tn.nightbeam.ras.api.TemplateResult;

public class AdminClassCommands {
    public static void forceClass(ServerPlayer target, String className) {
        TemplateResult result = RasApi.applyTemplate(target, className, true);
        System.out.println("Template apply result: " + result);
    }
}
```

---

## Complete Integration Example

A mod that spawns harder mobs based on the player's RAS progression:

```java
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import tn.nightbeam.ras.api.RasApi;
import tn.nightbeam.ras.api.CombatSnapshot;

public class RpgMobScaler {

    public static void scaleMobToPlayer(Mob mob, ServerPlayer player) {
        if (!RasApi.isAvailable()) return;

        CombatSnapshot snap = RasApi.getCombatSnapshot(player);

        double playerDamage = snap.attackDamage();
        double playerHealth = snap.maxHealth();
        int playerLevel = snap.rpgLevel();

        // Scale mob health: base + percentage of player's damage output
        double healthScale = playerDamage * 2.0;
        mob.getAttribute(Attributes.MAX_HEALTH)
           .setBaseValue(mob.getMaxHealth() + healthScale);
        mob.setHealth(mob.getMaxHealth());

        // Scale mob damage as a fraction of player health
        double damageScale = playerHealth * 0.05;
        mob.getAttribute(Attributes.ATTACK_DAMAGE)
           .setBaseValue(mob.getAttributeBaseValue(Attributes.ATTACK_DAMAGE) + damageScale);

        System.out.printf("Scaled mob for player level %d: +%.1f HP, +%.1f damage%n",
            playerLevel, healthScale, damageScale);
    }
}
```

---

## See Also

- [API Methods](methods.md) — Full method reference with parameters and return values
- [API Overview](overview.md) — What's available and server-side requirements
- [Developer Integration Guide](../guides/developer-integration.md) — Integration patterns for other mods
