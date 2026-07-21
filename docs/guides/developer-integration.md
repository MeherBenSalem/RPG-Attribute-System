# Developer Integration Guide

Patterns and best practices for integrating other mods with RPG Attribute System via the public API.

---

## Reading Player Progression

Use `getLevel()` and `getCombatSnapshot()` for any gameplay logic that needs to know a player's RPG progression.

### Mob Scaling

```java
import tn.nightbeam.ras.api.RasApi;
import tn.nightbeam.ras.api.CombatSnapshot;

public void onMobSpawn(Mob mob, Player nearestPlayer) {
    if (!RasApi.isAvailable() || nearestPlayer.level().isClientSide()) return;

    CombatSnapshot snap = RasApi.getCombatSnapshot(nearestPlayer);

    // Scale mob health and damage based on player stats
    double healthBonus = snap.attackDamage() * 3.0;
    double damageBonus = snap.maxHealth() * 0.02;

    mob.getAttribute(Attributes.MAX_HEALTH)
       .setBaseValue(mob.getMaxHealth() + healthBonus);
    mob.setHealth(mob.getMaxHealth());

    mob.getAttribute(Attributes.ATTACK_DAMAGE)
       .setBaseValue(mob.getAttributeBaseValue(Attributes.ATTACK_DAMAGE) + damageBonus);
}
```

### Quest Requirements

```java
public boolean canAcceptQuest(ServerPlayer player, int requiredLevel) {
    if (!RasApi.isAvailable()) return true; // No RAS → no requirement
    return RasApi.getLevel(player) >= requiredLevel;
}
```

### Loot Quality

```java
public ItemStack generateLoot(ServerPlayer player) {
    if (!RasApi.isAvailable()) return ItemStack.EMPTY;

    CombatSnapshot snap = RasApi.getCombatSnapshot(player);

    if (snap.rpgLevel() >= 50 && snap.attackDamage() > 20) {
        return new ItemStack(Items.NETHERITE_INGOT);
    }
    return new ItemStack(Items.IRON_INGOT);
}
```

---

## Triggering Respec from Other Mods

Use `RasApi.respec()` in custom items, quest rewards, or NPC interactions.

### Custom Quest Reward

```java
public void completeQuest(ServerPlayer player) {
    // Grant quest rewards...

    // Offer a free respec as a bonus reward
    RespecResult result = RasApi.respec(player, RespecOptions.admin());
    if (result == RespecResult.SUCCESS) {
        player.sendSystemMessage(Component.literal("§aQuest complete! Your attributes have been reset."));
    }
}
```

### Custom Item with Respec

```java
public void onItemUse(ServerPlayer player, ItemStack item) {
    RespecResult result = RasApi.respec(player, RespecOptions.item());
    if (result == RespecResult.SUCCESS) {
        item.shrink(1);
        player.sendSystemMessage(Component.literal("§aAttributes refunded!"));
    } else {
        player.sendSystemMessage(Component.literal("§cRespec failed: " + result));
    }
}
```

### Handling All Result Values

Always handle every `RespecResult` or `TemplateResult` case to give the player meaningful feedback:

```java
RespecResult result = RasApi.respec(player);
switch (result) {
    case SUCCESS           -> player.sendSystemMessage(Component.literal("§aDone!"));
    case DISABLED          -> player.sendSystemMessage(Component.literal("§cRespec is disabled."));
    case NO_PERMISSION     -> player.sendSystemMessage(Component.literal("§cNo permission."));
    case ON_COOLDOWN       -> player.sendSystemMessage(Component.literal("§cOn cooldown."));
    case INSUFFICIENT_COST -> player.sendSystemMessage(Component.literal("§cCan't afford it."));
    case FAILED            -> player.sendSystemMessage(Component.literal("§cSomething went wrong."));
}
```

---

## Applying Templates from Other Mods

Use templates for class selection systems, NPC trainers, or quest rewards.

### Class Selection System

```java
public class ClassSelectionScreen {
    private static final List<String> CLASSES = List.of("warrior", "mage", "archer", "rogue");

    public void onClassSelected(ServerPlayer player, String className) {
        TemplateResult result = RasApi.applyTemplate(player, className);
        switch (result) {
            case SUCCESS           -> player.sendSystemMessage(Component.literal("§aClass: " + className));
            case NOT_FOUND         -> player.sendSystemMessage(Component.literal("§cUnknown class."));
            case INSUFFICIENT_POINTS -> player.sendSystemMessage(Component.literal("§cNot enough points."));
            case INVALID_TEMPLATE  -> player.sendSystemMessage(Component.literal("§cBad template config."));
            case NO_PERMISSION     -> player.sendSystemMessage(Component.literal("§cNo permission."));
            case DISABLED          -> player.sendSystemMessage(Component.literal("§cTemplates disabled."));
            case FAILED            -> player.sendSystemMessage(Component.literal("§cFailed."));
        }
    }
}
```

### NPC Trainer

```java
public void onTrainerInteract(ServerPlayer player, String skill) {
    // Use adminOverride to bypass point requirements (NPC gives it for free)
    TemplateResult result = RasApi.applyTemplate(player, skill, true);
    if (result == TemplateResult.SUCCESS) {
        player.sendSystemMessage(Component.literal("§eTrainer has taught you " + skill + "!"));
    }
}
```

---

## Defensive Coding

### Always Check Availability

```java
public class MyModIntegration {
    private static final boolean RAS_LOADED = checkRasLoaded();

    private static boolean checkRasLoaded() {
        try {
            return RasApi.isAvailable();
        } catch (NoClassDefFoundError e) {
            return false;
        }
    }

    public void doSomething(Player player) {
        if (!RAS_LOADED) return; // RAS not installed, skip integration

        int level = RasApi.getLevel(player);
        // ...
    }
}
```

### Server-Side Guard

```java
public void onEvent(Player player) {
    if (!(player instanceof ServerPlayer serverPlayer)) return;
    if (!RasApi.isAvailable()) return;

    CombatSnapshot snap = RasApi.getCombatSnapshot(serverPlayer);
    // Safe to use snap
}
```

---

## What NOT to Do

**Don't access `PlayerVariables` directly.** The `PlayerVariables` class and the `Services.PLATFORM` locator are internal. Use `RasApi.getLevel()` and `RasApi.getCombatSnapshot()` instead.

**Don't reimplement RAS scaling math.** `CombatSnapshot` reads values directly from Minecraft `AttributeInstance.getValue()` — after RAS, gear, and effects. Don't try to duplicate the `init_val + points × multiplier` formula.

**Don't assume config file structure.** Config keys and file locations may change between versions. Use the API methods, not direct JSON reading.

**Don't call API methods on the client.** All read/write methods return safe defaults on the client (0, EMPTY, FAILED) but won't produce useful results. Always be on the server side.

---

## See Also

- [API Overview](../api/overview.md) — What's available and server-side requirements
- [API Methods](../api/methods.md) — Full method reference
- [API Examples](../api/examples.md) — Complete code examples
- [Customization Guide](customization.md) — Custom attributes, respec, and templates
