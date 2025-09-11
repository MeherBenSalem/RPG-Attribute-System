package tn.mbs.memory.client.screens;

import tn.mbs.memory.procedures.YouHavePointsProcedure;
import tn.mbs.memory.procedures.ReturnExtraPointsProcedure;
import tn.mbs.memory.procedures.PressToGetKeyBindNameProcedure;
import tn.mbs.memory.procedures.IsAt99Procedure;
import tn.mbs.memory.procedures.IsAt95Procedure;
import tn.mbs.memory.procedures.IsAt90Procedure;
import tn.mbs.memory.procedures.IsAt85Procedure;
import tn.mbs.memory.procedures.IsAt80Procedure;
import tn.mbs.memory.procedures.IsAt75Procedure;
import tn.mbs.memory.procedures.IsAt70Procedure;
import tn.mbs.memory.procedures.IsAt65Procedure;
import tn.mbs.memory.procedures.IsAt60Procedure;
import tn.mbs.memory.procedures.IsAt5Procedure;
import tn.mbs.memory.procedures.IsAt55Procedure;
import tn.mbs.memory.procedures.IsAt50Procedure;
import tn.mbs.memory.procedures.IsAt45Procedure;
import tn.mbs.memory.procedures.IsAt40Procedure;
import tn.mbs.memory.procedures.IsAt35Procedure;
import tn.mbs.memory.procedures.IsAt30Procedure;
import tn.mbs.memory.procedures.IsAt25Procedure;
import tn.mbs.memory.procedures.IsAt20Procedure;
import tn.mbs.memory.procedures.IsAt15Procedure;
import tn.mbs.memory.procedures.IsAt10Procedure;
import tn.mbs.memory.procedures.IsAt0Procedure;
import tn.mbs.memory.procedures.DisplayXpOverlayProcedure;
import tn.mbs.memory.procedures.DisplayLogicKeybindOverlayProcedure;
import tn.mbs.memory.procedures.CurrentXpToLevelProcedure;

import org.checkerframework.checker.units.qual.h;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.Minecraft;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.platform.GlStateManager;

@Mod.EventBusSubscriber({Dist.CLIENT})
public class LevelOverlayOverlay {
	@SubscribeEvent(priority = EventPriority.NORMAL)
	public static void eventHandler(RenderGuiEvent.Pre event) {
		int w = event.getWindow().getGuiScaledWidth();
		int h = event.getWindow().getGuiScaledHeight();
		Level world = null;
		double x = 0;
		double y = 0;
		double z = 0;
		Player entity = Minecraft.getInstance().player;
		if (entity != null) {
			world = entity.level();
			x = entity.getX();
			y = entity.getY();
			z = entity.getZ();
		}
		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.enableBlend();
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		RenderSystem.setShaderColor(1, 1, 1, 1);
		if (true) {
			if (IsAt0Procedure.execute(entity)) {
				event.getGuiGraphics().blit(ResourceLocation.parse("memory_of_the_past:textures/screens/ui_bar_0.png"), 3, h - 15, 0, 0, 80, 12, 80, 12);
			}
			if (IsAt5Procedure.execute(entity)) {
				event.getGuiGraphics().blit(ResourceLocation.parse("memory_of_the_past:textures/screens/ui_bar_5.png"), 3, h - 15, 0, 0, 80, 12, 80, 12);
			}
			if (IsAt10Procedure.execute(entity)) {
				event.getGuiGraphics().blit(ResourceLocation.parse("memory_of_the_past:textures/screens/ui_bar_10.png"), 3, h - 15, 0, 0, 80, 12, 80, 12);
			}
			if (IsAt15Procedure.execute(entity)) {
				event.getGuiGraphics().blit(ResourceLocation.parse("memory_of_the_past:textures/screens/ui_bar_15.png"), 3, h - 15, 0, 0, 80, 12, 80, 12);
			}
			if (IsAt20Procedure.execute(entity)) {
				event.getGuiGraphics().blit(ResourceLocation.parse("memory_of_the_past:textures/screens/ui_bar_20.png"), 3, h - 15, 0, 0, 80, 12, 80, 12);
			}
			if (IsAt25Procedure.execute(entity)) {
				event.getGuiGraphics().blit(ResourceLocation.parse("memory_of_the_past:textures/screens/ui_bar_25.png"), 3, h - 15, 0, 0, 80, 12, 80, 12);
			}
			if (IsAt30Procedure.execute(entity)) {
				event.getGuiGraphics().blit(ResourceLocation.parse("memory_of_the_past:textures/screens/ui_bar_30.png"), 3, h - 15, 0, 0, 80, 12, 80, 12);
			}
			if (IsAt35Procedure.execute(entity)) {
				event.getGuiGraphics().blit(ResourceLocation.parse("memory_of_the_past:textures/screens/ui_bar_35.png"), 3, h - 15, 0, 0, 80, 12, 80, 12);
			}
			if (IsAt40Procedure.execute(entity)) {
				event.getGuiGraphics().blit(ResourceLocation.parse("memory_of_the_past:textures/screens/ui_bar_40.png"), 3, h - 15, 0, 0, 80, 12, 80, 12);
			}
			if (IsAt45Procedure.execute(entity)) {
				event.getGuiGraphics().blit(ResourceLocation.parse("memory_of_the_past:textures/screens/ui_bar_45.png"), 3, h - 15, 0, 0, 80, 12, 80, 12);
			}
			if (IsAt50Procedure.execute(entity)) {
				event.getGuiGraphics().blit(ResourceLocation.parse("memory_of_the_past:textures/screens/ui_bar_50.png"), 3, h - 15, 0, 0, 80, 12, 80, 12);
			}
			if (IsAt55Procedure.execute(entity)) {
				event.getGuiGraphics().blit(ResourceLocation.parse("memory_of_the_past:textures/screens/ui_bar_55.png"), 3, h - 15, 0, 0, 80, 12, 80, 12);
			}
			if (IsAt60Procedure.execute(entity)) {
				event.getGuiGraphics().blit(ResourceLocation.parse("memory_of_the_past:textures/screens/ui_bar_60.png"), 3, h - 15, 0, 0, 80, 12, 80, 12);
			}
			if (IsAt65Procedure.execute(entity)) {
				event.getGuiGraphics().blit(ResourceLocation.parse("memory_of_the_past:textures/screens/ui_bar_65.png"), 3, h - 15, 0, 0, 80, 12, 80, 12);
			}
			if (IsAt70Procedure.execute(entity)) {
				event.getGuiGraphics().blit(ResourceLocation.parse("memory_of_the_past:textures/screens/ui_bar_70.png"), 3, h - 15, 0, 0, 80, 12, 80, 12);
			}
			if (IsAt75Procedure.execute(entity)) {
				event.getGuiGraphics().blit(ResourceLocation.parse("memory_of_the_past:textures/screens/ui_bar_75.png"), 3, h - 15, 0, 0, 80, 12, 80, 12);
			}
			if (IsAt80Procedure.execute(entity)) {
				event.getGuiGraphics().blit(ResourceLocation.parse("memory_of_the_past:textures/screens/ui_bar_80.png"), 3, h - 15, 0, 0, 80, 12, 80, 12);
			}
			if (IsAt85Procedure.execute(entity)) {
				event.getGuiGraphics().blit(ResourceLocation.parse("memory_of_the_past:textures/screens/ui_bar_85.png"), 3, h - 15, 0, 0, 80, 12, 80, 12);
			}
			if (IsAt90Procedure.execute(entity)) {
				event.getGuiGraphics().blit(ResourceLocation.parse("memory_of_the_past:textures/screens/ui_bar_90.png"), 3, h - 15, 0, 0, 80, 12, 80, 12);
			}
			if (IsAt95Procedure.execute(entity)) {
				event.getGuiGraphics().blit(ResourceLocation.parse("memory_of_the_past:textures/screens/ui_bar_95.png"), 3, h - 15, 0, 0, 80, 12, 80, 12);
			}
			if (IsAt99Procedure.execute(entity)) {
				event.getGuiGraphics().blit(ResourceLocation.parse("memory_of_the_past:textures/screens/ui_bar_99.png"), 3, h - 15, 0, 0, 80, 12, 80, 12);
			}
			if (YouHavePointsProcedure.execute(entity)) {
				event.getGuiGraphics().blit(ResourceLocation.parse("memory_of_the_past:textures/screens/levelup.png"), 4, h - 25, 0, 0, 7, 8, 7, 8);
			}
			if (DisplayLogicKeybindOverlayProcedure.execute()) {
				event.getGuiGraphics().blit(ResourceLocation.parse("memory_of_the_past:textures/screens/bookoverlay.png"), 85, h - 15, 0, 0, 8, 12, 8, 12);
			}
			if (DisplayXpOverlayProcedure.execute())
				event.getGuiGraphics().drawString(Minecraft.getInstance().font,

						CurrentXpToLevelProcedure.execute(entity), 5, h - 12, -16777216, false);
			if (YouHavePointsProcedure.execute(entity))
				event.getGuiGraphics().drawString(Minecraft.getInstance().font,

						ReturnExtraPointsProcedure.execute(entity), 12, h - 25, -1, false);
			if (DisplayLogicKeybindOverlayProcedure.execute())
				event.getGuiGraphics().drawString(Minecraft.getInstance().font,

						PressToGetKeyBindNameProcedure.execute(), 95, h - 13, -1, false);
		}
		RenderSystem.depthMask(true);
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableDepthTest();
		RenderSystem.disableBlend();
		RenderSystem.setShaderColor(1, 1, 1, 1);
	}
}