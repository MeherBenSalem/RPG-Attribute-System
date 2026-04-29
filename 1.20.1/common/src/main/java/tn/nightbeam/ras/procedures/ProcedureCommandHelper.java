package tn.nightbeam.ras.procedures;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ProcedureCommandHelper {
    private static final Pattern SIMPLE_GIVE = Pattern
            .compile("(?i)^/?give\\s+(?:@s|@p)\\s+([a-z0-9_.-]+:[a-z0-9_/.-]+)(?:\\s+(\\d+))?\\s*$");

    private ProcedureCommandHelper() {
    }

    public static void executeAsEntity(Entity entity, String command) {
        executeAsEntity(entity, command, false);
    }

    public static void executeReward(Entity entity, String command) {
        executeAsEntity(entity, command, true);
    }

    private static void executeAsEntity(Entity entity, String command, boolean preferDirectGive) {
        if (entity == null || command == null || command.isBlank() || entity.level().isClientSide()
                || entity.getServer() == null) {
            return;
        }
        String normalizedCommand = normalizeForSelfTarget(entity, command);
        if (preferDirectGive && tryGiveDirectly(entity, normalizedCommand)) {
            return;
        }

        CommandSourceStack source = new CommandSourceStack(CommandSource.NULL, entity.position(),
                entity.getRotationVector(), entity.level() instanceof ServerLevel ? (ServerLevel) entity.level() : null,
                4, entity.getName().getString(), entity.getDisplayName(), entity.level().getServer(), entity)
                .withSuppressedOutput();
        entity.getServer().getCommands().performPrefixedCommand(source, normalizedCommand);
    }

    private static boolean tryGiveDirectly(Entity entity, String command) {
        if (!(entity instanceof Player player)) {
            return false;
        }

        Matcher matcher = SIMPLE_GIVE.matcher(command.trim());
        if (!matcher.matches()) {
            return false;
        }

        ResourceLocation itemId = ResourceLocation.tryParse(matcher.group(1));
        if (itemId == null) {
            return false;
        }

        Item item = BuiltInRegistries.ITEM.get(itemId);
        if (item == Items.AIR && !"minecraft:air".equals(itemId.toString())) {
            return false;
        }

        int count = 1;
        if (matcher.group(2) != null) {
            try {
                count = Integer.parseInt(matcher.group(2));
            } catch (NumberFormatException ignored) {
                return false;
            }
        }
        if (count <= 0) {
            return true;
        }

        ItemStack stack = new ItemStack(item, count);
        boolean inserted = player.getInventory().add(stack);
        if (!inserted || !stack.isEmpty()) {
            player.drop(stack, false);
        }
        return true;
    }

    private static String normalizeForSelfTarget(Entity entity, String command) {
        if (!(entity instanceof Player)) {
            return command.trim();
        }

        return command.trim()
                .replaceFirst("(?i)(^/?attribute\\s+)@p\\b", "$1@s")
                .replaceFirst("(?i)(^/?effect\\s+give\\s+)@p\\b", "$1@s")
                .replaceFirst("(?i)(^/?give\\s+)@p\\b", "$1@s")
                .replaceFirst("(?i)(^/?xp\\s+add\\s+)@p\\b", "$1@s")
                .replaceFirst("(?i)(^/?experience\\s+(?:add|set)\\s+)@p\\b", "$1@s");
    }
}
