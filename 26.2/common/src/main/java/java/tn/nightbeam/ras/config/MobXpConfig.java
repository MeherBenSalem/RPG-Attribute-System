package tn.nightbeam.ras.config;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import tn.nightbeam.ras.Constants;
import tn.nightbeam.ras.platform.Services;

import java.nio.file.Path;

/** Server-side adapter; config is read only at startup or explicit reload. */
public final class MobXpConfig {
    private static final MobXpRuleStore STORE = new MobXpRuleStore();
    private MobXpConfig() { }

    public static Path path() {
        return Services.CONFIG.getConfigDirectory().resolve("ras").resolve("mob_xp.json");
    }

    public static void createDefaultIfMissing() {
        try {
            MobXpRuleStore.createDefaultIfMissing(path());
        } catch (Exception error) {
            Constants.LOG.warn("[RPGAS] Could not create default mob_xp.json", error);
        }
    }

    public static MobXpRuleStore.ReloadResult reload() {
        MobXpRuleStore.ReloadResult result = STORE.reload(path());
        for (String diagnostic : result.diagnostics()) Constants.LOG.warn("[RPGAS] {}", diagnostic);
        return result;
    }

    public static double calculate(LivingEntity victim, double legacyRate) {
        MobXpRules rules = STORE.current();
        var armor = victim.getAttribute(Attributes.ARMOR);
        double armorPoints = armor == null ? 0 : armor.getValue();
        return rules.calculate(victim.getMaxHealth(), legacyRate,
                BuiltInRegistries.ENTITY_TYPE.getKey(victim.getType()).toString(),
                tag -> victim.getType().builtInRegistryHolder().is(
                        TagKey.create(Registries.ENTITY_TYPE, Identifier.tryParse(tag))),
                victim.level().getDifficulty().name().toLowerCase(java.util.Locale.ROOT), armorPoints);
    }
}
