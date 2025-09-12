package tn.mbs.memory.procedures;

import tn.naizo.jauml.JaumlConfigLib;

import net.minecraft.commands.CommandSourceStack;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.DoubleArgumentType;

public class SetupIronSpellsResistProcedure {
	public static void execute(CommandContext<CommandSourceStack> arguments) {
		String dir = "";
		String file = "";
		dir = "motp/attributes";
		file = "attribute_" + Math.round(DoubleArgumentType.getDouble(arguments, "number"));
		if (JaumlConfigLib.createConfigFile(dir, file)) {
			JaumlConfigLib.createConfigFile(dir, file);
		}
		JaumlConfigLib.setStringValue(dir, file, "display_name", "Spell Power");
		JaumlConfigLib.addStringToArray(dir, file, "cmd_to_exc", "/attribute @p irons_spellbooks:spell_resist base set [param(0.02)]");
		JaumlConfigLib.setStringValue(dir, file, "on_level_event", "");
		JaumlConfigLib.setStringValue(dir, file, "tip_to_display", "Influences the spell power of the player");
		JaumlConfigLib.setNumberValue(dir, file, "init_val_attribute", 50);
		JaumlConfigLib.setNumberValue(dir, file, "max_level", 200);
		JaumlConfigLib.setNumberValue(dir, file, "base_value_per_point", 1);
		JaumlConfigLib.setBooleanValue(dir, file, "lock", false);
	}
}