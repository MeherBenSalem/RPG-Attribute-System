package tn.nightbeam.ras.procedures;

import tn.nightbeam.ras.platform.Services;
import tn.nightbeam.ras.network.PlayerVariables;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.commands.CommandSourceStack;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.DoubleArgumentType;

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

        {
            PlayerVariables vars = Services.PLATFORM.getPlayerVariables(entity);
            double _setval = vars.currentXpTLevel + AddedXp;
            vars.currentXpTLevel = _setval;
            Services.PLATFORM.syncPlayerVariables(vars, entity);
            if (entity instanceof net.minecraft.world.entity.player.Player player && !world.isClientSide())
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("\u00A7a+" + (int) AddedXp + " VP"), true);
        }
        if (Services.PLATFORM.getPlayerVariables(entity).Level >= Services.CONFIG.getNumberValue("ras", "settings",
                "max_player_level")) {
            return;
        }
        if (world instanceof Level _level) {
            if (!_level.isClientSide()) {
                _level.playSound(null, BlockPos.containing(x, y, z),
                        BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.tryParse("entity.experience_orb.pickup")),
                        SoundSource.NEUTRAL, 1, 1);
            } else {
                _level.playLocalSound(x, y, z,
                        BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.tryParse("entity.experience_orb.pickup")),
                        SoundSource.NEUTRAL, 1, 1, false);
            }
        }
        while (Services.PLATFORM.getPlayerVariables(entity).currentXpTLevel >= Services.PLATFORM
                .getPlayerVariables(entity).nextevelXp) {
            {
                PlayerVariables vars = Services.PLATFORM.getPlayerVariables(entity);
                double _setval = vars.currentXpTLevel - vars.nextevelXp;
                vars.currentXpTLevel = _setval;
                Services.PLATFORM.syncPlayerVariables(vars, entity);
            }
            LevelUpProcedureProcedure.execute(world, entity);
        }
    }
}
