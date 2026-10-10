package tn.nightbeam.ras.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import tn.nightbeam.ras.config.MobXpConfig;
import tn.nightbeam.ras.config.MobXpRuleStore;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.procedures.LevelingService;
import tn.nightbeam.ras.util.AttributeManager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Opt-in hosted QA against a fresh disposable client's real server, packets, and entity death events. */
final class RasClientGameplayQa {
    private static final String RUN_ID = System.getProperty("ras.clientQaRunId", "");
    private static final String SOURCE_SHA = System.getProperty("ras.clientQaSourceSha", "");
    private static String lastRequest = "";
    private static boolean busy;
    private static boolean seeded;
    private static final String ENTITY_RULES = """
        {"schema_version":1,"enabled":true,
         "difficulty_weighting":{"enabled":true,"peaceful":1,"easy":1,"normal":1,"hard":1.5},
         "armor_weighting":{"enabled":true,"per_armor_point":0.25,"max_multiplier":5},
         "rules":[{"id":"qa-entity","entity":"minecraft:skeleton","priority":-100,"base_xp":7,"multiplier":2},
                  {"id":"qa-tag","tag":"minecraft:skeletons","priority":100,"base_xp":5,"multiplier":3}]}
        """;
    private static final String TAG_RULES = ENTITY_RULES.replace(
        "{\"id\":\"qa-entity\",\"entity\":\"minecraft:skeleton\",\"priority\":-100,\"base_xp\":7,\"multiplier\":2},", "");

    private RasClientGameplayQa() { }

    static void tick(Minecraft client) {
        if (!Boolean.getBoolean("ras.clientQa") || busy || client.player == null
                || client.level == null || client.getSingleplayerServer() == null) return;
        try {
            Path directory = client.gameDirectory.toPath().toAbsolutePath().normalize();
            if (!RUN_ID.matches("[a-zA-Z0-9_-]{8,100}") || !SOURCE_SHA.matches("[0-9a-f]{40}")
                    || !directory.getFileName().toString().equals(RUN_ID)
                    || !directory.getParent().getFileName().toString().equals("client-qa")
                    || !directory.equals(directory.toRealPath())) {
                throw new IllegalStateException("Gameplay QA requires an explicit isolated non-symlink client-qa run");
            }
            Path requestPath = directory.resolve("ras-client-qa-action.json");
            if (!Files.isRegularFile(requestPath)) return;
            JsonObject request = JsonParser.parseString(Files.readString(requestPath)).getAsJsonObject();
            String id = request.get("request_id").getAsString();
            if (id.equals(lastRequest)) return;
            if (!id.matches("[a-zA-Z0-9_-]{8,100}") || !RUN_ID.equals(request.get("run_id").getAsString())
                    || !SOURCE_SHA.equals(request.get("source_sha").getAsString())) {
                throw new IllegalArgumentException("Gameplay request identity mismatch");
            }
            String action = request.get("action").getAsString();
            if (!java.util.Set.of("seed", "verify-allocation", "kill-entity", "kill-tag", "invalid-reload", "kill-legacy").contains(action)) {
                throw new IllegalArgumentException("Unknown bounded gameplay action");
            }
            lastRequest = id;
            busy = true;
            var uuid = client.player.getUUID();
            client.getSingleplayerServer().execute(() -> {
                JsonObject result = new JsonObject();
                result.addProperty("schema_version", 1);
                result.addProperty("run_id", RUN_ID); result.addProperty("source_sha", SOURCE_SHA);
                result.addProperty("request_id", id); result.addProperty("action", action);
                try {
                    var server = client.getSingleplayerServer();
                    ServerPlayer player = server == null ? null : server.getPlayerList().getPlayer(uuid);
                    if (player == null || server.getPlayerList().getPlayers().size() != 1) {
                        throw new IllegalStateException("Exactly the bound disposable server player is required");
                    }
                    Path config = Services.CONFIG.getConfigDirectory().toAbsolutePath().normalize();
                    if (!config.equals(directory.resolve("config")) || !config.equals(config.toRealPath())) {
                        throw new IllegalStateException("QA must never modify config outside its disposable run");
                    }
                    result.add("before", snapshot(Services.PLATFORM.getPlayerVariables(player)));
                    if (action.equals("seed")) {
                        LevelingService.resetProgress(player);
                        var vars = Services.PLATFORM.getPlayerVariables(player);
                        vars.SparePoints = 6; vars.modifier = 1;
                        Services.CONFIG.setBooleanValue("ras", "settings", "use_vanilla_xp", false);
                        Services.CONFIG.setBooleanValue("ras", "settings", "shared_xp_enabled", false);
                        Services.CONFIG.setNumberValue("ras", "droprate", "default_vp_rates", 1);
                        Services.CONFIG.setStringArray("ras", "droprate", "dimensions_drop_rates", java.util.List.of());
                        Services.PLATFORM.syncPlayerVariables(vars, player);
                        seeded = true;
                        result.addProperty("fixture_setup_only", true);
                    } else {
                        if (!seeded) throw new IllegalStateException("Gameplay seed must run first");
                        if (action.equals("verify-allocation")) {
                            var vars = Services.PLATFORM.getPlayerVariables(player);
                            var agility = AttributeManager.getAttributeData(5);
                            if (agility == null || !close(vars.attributePoints.getOrDefault("attribute_5", 0.0), 1)
                                    || !close(vars.attributes.getOrDefault("attribute_5", Double.NaN), agility.initValue + agility.baseIncrement)
                                    || !close(vars.SparePoints, 5)) {
                                throw new IllegalStateException("Normal UI allocation packet did not produce exactly one server-authoritative Agility point");
                            }
                            var movement = player.getAttribute(Attributes.MOVEMENT_SPEED);
                            if (movement == null || !close(movement.getBaseValue(), agility.initValue + agility.baseIncrement)
                                    || !close(movement.getValue(), agility.initValue + agility.baseIncrement)) {
                                throw new IllegalStateException("Normal allocation did not apply the real Minecraft movement-speed attribute");
                            }
                            result.addProperty("server_movement_speed_base", movement.getBaseValue());
                            result.addProperty("server_movement_speed_value", movement.getValue());
                            result.addProperty("normal_ui_packet_allocation_verified", true);
                            result.addProperty("expected_agility", agility.initValue + agility.baseIncrement);
                        } else {
                            boolean invalid = action.equals("invalid-reload");
                            String fixture = action.equals("kill-legacy") ? MobXpRuleStore.DEFAULT_JSON
                                    : action.equals("kill-entity") ? ENTITY_RULES : TAG_RULES;
                            // Only the disposable config is changed, never a real user's config.
                            Files.writeString(MobXpConfig.path(), fixture);
                            var loaded = MobXpConfig.reload();
                            if (!loaded.loaded() || !loaded.diagnostics().isEmpty()) {
                                throw new IllegalStateException("Known valid QA rules did not load cleanly");
                            }
                            if (invalid) {
                                Files.writeString(MobXpConfig.path(), "{bad-json");
                                if (MobXpConfig.reload().loaded()) throw new IllegalStateException("Invalid reload unexpectedly replaced valid rules");
                                result.addProperty("invalid_reload_retained_previous_rules", true);
                            }
                            server.setDifficulty(Difficulty.HARD, true);
                            var level = (net.minecraft.server.level.ServerLevel) player.level();
                            EntityType<?> skeleton = null;
                            for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
                                if (BuiltInRegistries.ENTITY_TYPE.getKey(type).toString().equals("minecraft:skeleton")) {
                                    skeleton = type;
                                    break;
                                }
                            }
                            if (skeleton == null || !(skeleton.create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND)
                                    instanceof net.minecraft.world.entity.Mob victim)) {
                                throw new IllegalStateException("Real registered Minecraft Skeleton factory returned no mob");
                            }
                            victim.setNoAi(true);
                            victim.setPos(player.getX() + 2, player.getY(), player.getZ());
                            var armor = victim.getAttribute(Attributes.ARMOR);
                            if (armor == null) throw new IllegalStateException("Real mob lacks armor attribute");
                            armor.setBaseValue(2);
                            boolean matchesTag = victim.getType().builtInRegistryHolder().is(
                                    TagKey.create(Registries.ENTITY_TYPE, net.minecraft.resources.Identifier.tryParse("minecraft:skeletons")));
                            if (!matchesTag) throw new IllegalStateException("Real skeleton does not match the selected built-in entity tag");
                            if (!level.addFreshEntity(victim)) throw new IllegalStateException("Real mob was not added to the server world");
                            double before = Services.PLATFORM.getPlayerVariables(player).totalXp;
                            double expected = action.equals("kill-legacy") ? victim.getMaxHealth()
                                    : action.equals("kill-entity") ? 7 * 2 * 1.5 * 1.5 : 5 * 3 * 1.5 * 1.5;
                            result.addProperty("mob", "minecraft:skeleton");
                            result.addProperty("real_tag_match", matchesTag);
                            result.addProperty("difficulty", "hard"); result.addProperty("armor_points", armor.getValue());
                            result.addProperty("expected_reward", expected);
                            try {
                                boolean hurt = victim.hurtServer(level, player.damageSources().playerAttack(player), 10000.0F);
                                double actual = Services.PLATFORM.getPlayerVariables(player).totalXp - before;
                                if (!hurt || victim.isAlive() || !close(actual, expected)) {
                                    throw new IllegalStateException("Real player damage/loader death event reward differs: expected " + expected + ", observed " + actual);
                                }
                                result.addProperty("observed_reward", actual);
                                result.addProperty("normal_loader_death_event_verified", true);
                            } finally { victim.discard(); }
                            // Leave a valid config in the disposable fixture even after malformed-reload testing.
                            if (invalid) Files.writeString(MobXpConfig.path(), fixture);
                        }
                    }
                    result.add("after", snapshot(Services.PLATFORM.getPlayerVariables(player)));
                    result.addProperty("status", "PASS");
                } catch (Exception error) {
                    result.addProperty("status", "FAIL");
                    result.addProperty("reason", error.getClass().getSimpleName() + ": " + error.getMessage());
                }
                result.addProperty("written_at_ms", System.currentTimeMillis());
                try {
                    Path target = directory.resolve("ras-client-qa-gameplay.json");
                    Path temporary = target.resolveSibling(target.getFileName() + ".tmp");
                    Files.writeString(temporary, result.toString() + System.lineSeparator());
                    Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception error) {
                    tn.nightbeam.ras.Constants.LOG.error("[RAS gameplay QA] Failed to record real server evidence", error);
                } finally { client.execute(() -> busy = false); }
            });
        } catch (Exception error) {
            tn.nightbeam.ras.Constants.LOG.error("[RAS gameplay QA] Invalid request", error);
        }
    }

    static JsonObject snapshot(PlayerVariables vars) {
        JsonObject value = new JsonObject();
        value.addProperty("total_xp", vars.totalXp); value.addProperty("level", vars.Level);
        value.addProperty("spare_points", vars.SparePoints); value.addProperty("modifier", vars.modifier);
        JsonObject attributes = new JsonObject(), points = new JsonObject();
        vars.attributes.forEach(attributes::addProperty); vars.attributePoints.forEach(points::addProperty);
        value.add("attributes", attributes); value.add("attribute_points", points);
        return value;
    }

    private static boolean close(double actual, double expected) {
        return Double.isFinite(actual) && Double.isFinite(expected) && Math.abs(actual - expected) <= 1e-8;
    }
}
