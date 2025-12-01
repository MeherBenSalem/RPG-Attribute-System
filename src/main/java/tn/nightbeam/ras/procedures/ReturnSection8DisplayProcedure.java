package tn.nightbeam.ras.procedures;

import tn.naizo.jauml.JaumlConfigLib;

import net.minecraftforge.registries.ForgeRegistries;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;

public class ReturnSection8DisplayProcedure {
	public static String execute(Entity entity) {
		if (entity == null)
			return "";
		String filename = "";
		String dir = "";
		dir = "motp/display";
		filename = "attribute_8";
		return JaumlConfigLib.getStringValue(dir, filename, "display_name") + ""
				+ new java.text.DecimalFormat("##.#").format(((LivingEntity) entity)
						.getAttribute(ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(JaumlConfigLib.getStringValue(dir, filename, "attribute_namespace"), JaumlConfigLib.getStringValue(dir, filename, "attribute_name")))).getBaseValue()
						* JaumlConfigLib.getNumberValue(dir, filename, "display_modifer"));
	}
}