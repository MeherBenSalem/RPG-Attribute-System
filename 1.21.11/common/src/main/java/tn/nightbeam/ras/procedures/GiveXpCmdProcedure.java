package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.network.PlayerVariables;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.commands.CommandSourceStack;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.minecraft.sounds.SoundEvent;

public class GiveXpCmdProcedure {
    public static void execute(LevelAccessor world, double x, double y, double z,
            CommandContext<CommandSourceStack> arguments, Entity entity) {
        if (entity == null)
            return;
        double AddedXp = DoubleArgumentType.getDouble(arguments, "amount");
        execute(world, x, y, z, AddedXp, entity);
    }

    public static void execute(LevelAccessor world, double x, double y, double z, double AddedXp, Entity entity) {
        if (entity == null)
            return;

        LevelingService.addXp(entity, AddedXp);
        if (entity instanceof net.minecraft.world.entity.player.Player player && !world.isClientSide())
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("\u00A7a+" + (int) AddedXp + " VP"), true);
        if (world instanceof Level _level) {
            SoundEvent xpSound = BuiltInRegistries.SOUND_EVENT
                    .get(Identifier.tryParse("entity.experience_orb.pickup"))
                    .map(net.minecraft.core.Holder.Reference::value)
                    .orElse(null);
            if (xpSound == null) {
                return;
            }
            if (!_level.isClientSide()) {
                _level.playSound(null, BlockPos.containing(x, y, z),
                        xpSound, SoundSource.NEUTRAL, 1, 1);
            } else {
                _level.playLocalSound(x, y, z,
                        xpSound, SoundSource.NEUTRAL, 1, 1, false);
            }
        }
    }
}
