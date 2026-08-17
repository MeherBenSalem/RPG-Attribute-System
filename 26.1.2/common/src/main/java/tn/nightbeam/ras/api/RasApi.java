package tn.nightbeam.ras.api;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import tn.nightbeam.ras.init.RpgAttributeSystemModAttributes;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.procedures.LevelingService;

public final class RasApi {
    private RasApi() {
    }

    /** Returns true when RPG Attribute System classes are loaded (always true inside this mod). */
    public static boolean isAvailable() {
        return true;
    }

    public static int getLevel(Player player) {
        if (!(player instanceof ServerPlayer)) {
            return 0;
        }
        LevelingService.initializeOrMigrate(player);
        PlayerVariables vars = Services.PLATFORM.getPlayerVariables(player);
        return (int) Math.floor(vars.Level);
    }

    public static CombatSnapshot getCombatSnapshot(Player player) {
        if (!(player instanceof LivingEntity living)) {
            return CombatSnapshot.EMPTY;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            LevelingService.initializeOrMigrate(serverPlayer);
        }
        int rpgLevel = readRpgLevel(living);
        return new CombatSnapshot(
                rpgLevel,
                readAttribute(living, Attributes.MAX_HEALTH),
                readAttribute(living, Attributes.ATTACK_DAMAGE),
                readAttribute(living, Attributes.ARMOR),
                readAttribute(living, Attributes.ARMOR_TOUGHNESS),
                readAttribute(living, Attributes.MOVEMENT_SPEED)
        );
    }

    public static RespecResult respec(Player player) {
        return respec(player, RespecOptions.defaults());
    }

    public static RespecResult respec(Player player, RespecOptions options) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return RespecResult.FAILED;
        }
        return tn.nightbeam.ras.procedures.RespecService.tryRespec(serverPlayer, options);
    }

    public static TemplateResult applyTemplate(Player player, String templateId) {
        return tn.nightbeam.ras.procedures.TemplateService.apply(player, templateId, false);
    }

    public static TemplateResult applyTemplate(Player player, String templateId, boolean adminOverride) {
        return tn.nightbeam.ras.procedures.TemplateService.apply(player, templateId, adminOverride);
    }

    private static int readRpgLevel(LivingEntity living) {
        AttributeInstance instance = living.getAttribute(Holder.direct(RpgAttributeSystemModAttributes.RPG_LEVEL.get()));
        return instance != null ? (int) Math.floor(instance.getValue()) : 0;
    }

    private static double readAttribute(LivingEntity living, Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute) {
        AttributeInstance instance = living.getAttribute(attribute);
        return instance != null ? instance.getValue() : 0.0;
    }
}
