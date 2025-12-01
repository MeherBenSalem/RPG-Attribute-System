package tn.nightbeam.ras.client.gui;

import tn.nightbeam.ras.world.inventory.PlayerStatsGUIMenu;
import tn.nightbeam.ras.procedures.ReturnNextAttributeTwoProcedure;
import tn.nightbeam.ras.procedures.ReturnNextAttributeThreeProcedure;
import tn.nightbeam.ras.procedures.ReturnNextAttributeTenProcedure;
import tn.nightbeam.ras.procedures.ReturnNextAttributeSixthProcedure;
import tn.nightbeam.ras.procedures.ReturnNextAttributeSeventhProcedure;
import tn.nightbeam.ras.procedures.ReturnNextAttributeOneProcedure;
import tn.nightbeam.ras.procedures.ReturnNextAttributeNineProcedure;
import tn.nightbeam.ras.procedures.ReturnNextAttributeForthProcedure;
import tn.nightbeam.ras.procedures.ReturnNextAttributeFifthProcedure;
import tn.nightbeam.ras.procedures.ReturnNextAttributeEightProcedure;
import tn.nightbeam.ras.procedures.ReturnGlobalSectionsDisplayProcedure;
import tn.nightbeam.ras.procedures.ReturnExtraPointsProcedure;
import tn.nightbeam.ras.procedures.ReturnCurrentModifierProcedure;
import tn.nightbeam.ras.procedures.ReturnCurrentLevelProcedure;
import tn.nightbeam.ras.procedures.ReturnCurrentAttributeTwoProcedure;
import tn.nightbeam.ras.procedures.ReturnCurrentAttributeThreeProcedure;
import tn.nightbeam.ras.procedures.ReturnCurrentAttributeTenProcedure;
import tn.nightbeam.ras.procedures.ReturnCurrentAttributeSixthProcedure;
import tn.nightbeam.ras.procedures.ReturnCurrentAttributeSeventhProcedure;
import tn.nightbeam.ras.procedures.ReturnCurrentAttributeOneProcedure;
import tn.nightbeam.ras.procedures.ReturnCurrentAttributeNineProcedure;
import tn.nightbeam.ras.procedures.ReturnCurrentAttributeForthProcedure;
import tn.nightbeam.ras.procedures.ReturnCurrentAttributeFifthProcedure;
import tn.nightbeam.ras.procedures.ReturnCurrentAttributeEightProcedure;
import tn.nightbeam.ras.procedures.ReturnAttributeTwoTipProcedure;
import tn.nightbeam.ras.procedures.ReturnAttributeThreeTipProcedure;
import tn.nightbeam.ras.procedures.ReturnAttributeTenTipProcedure;
import tn.nightbeam.ras.procedures.ReturnAttributeSixthTipProcedure;
import tn.nightbeam.ras.procedures.ReturnAttributeSeventhTipProcedure;
import tn.nightbeam.ras.procedures.ReturnAttributeOneTipProcedure;
import tn.nightbeam.ras.procedures.ReturnAttributeNineTipProcedure;
import tn.nightbeam.ras.procedures.ReturnAttributeForthTipProcedure;
import tn.nightbeam.ras.procedures.ReturnAttributeFifthTipProcedure;
import tn.nightbeam.ras.procedures.ReturnAttributeEightTipProcedure;
import tn.nightbeam.ras.procedures.IsAt99Procedure;
import tn.nightbeam.ras.procedures.IsAt95Procedure;
import tn.nightbeam.ras.procedures.IsAt90Procedure;
import tn.nightbeam.ras.procedures.IsAt85Procedure;
import tn.nightbeam.ras.procedures.IsAt80Procedure;
import tn.nightbeam.ras.procedures.IsAt75Procedure;
import tn.nightbeam.ras.procedures.IsAt70Procedure;
import tn.nightbeam.ras.procedures.IsAt65Procedure;
import tn.nightbeam.ras.procedures.IsAt60Procedure;
import tn.nightbeam.ras.procedures.IsAt5Procedure;
import tn.nightbeam.ras.procedures.IsAt55Procedure;
import tn.nightbeam.ras.procedures.IsAt50Procedure;
import tn.nightbeam.ras.procedures.IsAt45Procedure;
import tn.nightbeam.ras.procedures.IsAt40Procedure;
import tn.nightbeam.ras.procedures.IsAt35Procedure;
import tn.nightbeam.ras.procedures.IsAt30Procedure;
import tn.nightbeam.ras.procedures.IsAt25Procedure;
import tn.nightbeam.ras.procedures.IsAt20Procedure;
import tn.nightbeam.ras.procedures.IsAt15Procedure;
import tn.nightbeam.ras.procedures.IsAt10Procedure;
import tn.nightbeam.ras.procedures.IsAt0Procedure;
import tn.nightbeam.ras.procedures.DisplayLogicLockAttribute9Procedure;
import tn.nightbeam.ras.procedures.DisplayLogicLockAttribute8Procedure;
import tn.nightbeam.ras.procedures.DisplayLogicLockAttribute7Procedure;
import tn.nightbeam.ras.procedures.DisplayLogicLockAttribute6Procedure;
import tn.nightbeam.ras.procedures.DisplayLogicLockAttribute5Procedure;
import tn.nightbeam.ras.procedures.DisplayLogicLockAttribute4Procedure;
import tn.nightbeam.ras.procedures.DisplayLogicLockAttribute3Procedure;
import tn.nightbeam.ras.procedures.DisplayLogicLockAttribute2Procedure;
import tn.nightbeam.ras.procedures.DisplayLogicLockAttribute1Procedure;
import tn.nightbeam.ras.procedures.DisplayLogicLockAttribute10Procedure;
import tn.nightbeam.ras.procedures.DisplayLogicAttribute9Procedure;
import tn.nightbeam.ras.procedures.DisplayLogicAttribute8Procedure;
import tn.nightbeam.ras.procedures.DisplayLogicAttribute7Procedure;
import tn.nightbeam.ras.procedures.DisplayLogicAttribute6Procedure;
import tn.nightbeam.ras.procedures.DisplayLogicAttribute5Procedure;
import tn.nightbeam.ras.procedures.DisplayLogicAttribute4Procedure;
import tn.nightbeam.ras.procedures.DisplayLogicAttribute3Procedure;
import tn.nightbeam.ras.procedures.DisplayLogicAttribute2Procedure;
import tn.nightbeam.ras.procedures.DisplayLogicAttribute1Procedure;
import tn.nightbeam.ras.procedures.DisplayLogicAttribute10Procedure;
import tn.nightbeam.ras.procedures.CurrentXpToLevelProcedure;
import tn.nightbeam.ras.network.PlayerStatsGUIButtonMessage;
import tn.nightbeam.ras.init.RpgAttributeSystemModScreens;
import tn.nightbeam.ras.RpgAttributeSystemMod;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.GuiGraphics;

import java.util.stream.Collectors;
import java.util.Arrays;

import com.mojang.blaze3d.systems.RenderSystem;

public class PlayerStatsGUIScreen extends AbstractContainerScreen<PlayerStatsGUIMenu> implements RpgAttributeSystemModScreens.ScreenAccessor {
	private final Level world;
	private final int x, y, z;
	private final Player entity;
	private boolean menuStateUpdateActive = false;
	ImageButton imagebutton_button_notclicked;
	ImageButton imagebutton_button_notclicked1;
	ImageButton imagebutton_button_notclicked2;
	ImageButton imagebutton_button_notclicked3;
	ImageButton imagebutton_button_notclicked4;
	ImageButton imagebutton_button_notclicked5;
	ImageButton imagebutton_button_notclicked6;
	ImageButton imagebutton_button_notclicked7;
	ImageButton imagebutton_button_notclicked9;
	ImageButton imagebutton_button_for_stats;
	ImageButton imagebutton_button_left;
	ImageButton imagebutton_button_right;
	ImageButton imagebutton_button_notclicked8;

	public PlayerStatsGUIScreen(PlayerStatsGUIMenu container, Inventory inventory, Component text) {
		super(container, inventory, text);
		this.world = container.world;
		this.x = container.x;
		this.y = container.y;
		this.z = container.z;
		this.entity = container.entity;
		this.imageWidth = 234;
		this.imageHeight = 166;
	}

	@Override
	public void updateMenuState(int elementType, String name, Object elementState) {
		menuStateUpdateActive = true;
		menuStateUpdateActive = false;
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(guiGraphics);
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		boolean customTooltipShown = false;
		if (mouseX > leftPos + 35 && mouseX < leftPos + 74 && mouseY > topPos + -1 && mouseY < topPos + 10) {
			guiGraphics.renderTooltip(font, Component.translatable("gui.rpg_attribute_system.player_stats_gui.tooltip_represents_your_overall_progress"), mouseX, mouseY);
			customTooltipShown = true;
		}
		if (mouseX > leftPos + 125 && mouseX < leftPos + 214 && mouseY > topPos + 2 && mouseY < topPos + 13) {
			guiGraphics.renderTooltip(font, Component.translatable("gui.rpg_attribute_system.player_stats_gui.tooltip_indicates_the_number_of_points_y"), mouseX, mouseY);
			customTooltipShown = true;
		}
		if (DisplayLogicAttribute1Procedure.execute(entity))
			if (mouseX > leftPos + -46 && mouseX < leftPos + -22 && mouseY > topPos + 25 && mouseY < topPos + 49) {
				String hoverText = ReturnAttributeOneTipProcedure.execute();
				if (hoverText != null) {
					guiGraphics.renderComponentTooltip(font, Arrays.stream(hoverText.split("\n")).map(Component::literal).collect(Collectors.toList()), mouseX, mouseY);
				}
				customTooltipShown = true;
			}
		if (DisplayLogicAttribute1Procedure.execute(entity))
			if (mouseX > leftPos + 84 && mouseX < leftPos + 98 && mouseY > topPos + 32 && mouseY < topPos + 45) {
				String hoverText = ReturnNextAttributeOneProcedure.execute(entity);
				if (hoverText != null) {
					guiGraphics.renderComponentTooltip(font, Arrays.stream(hoverText.split("\n")).map(Component::literal).collect(Collectors.toList()), mouseX, mouseY);
				}
				customTooltipShown = true;
			}
		if (DisplayLogicAttribute2Procedure.execute(entity))
			if (mouseX > leftPos + 126 && mouseX < leftPos + 150 && mouseY > topPos + 25 && mouseY < topPos + 49) {
				String hoverText = ReturnAttributeTwoTipProcedure.execute();
				if (hoverText != null) {
					guiGraphics.renderComponentTooltip(font, Arrays.stream(hoverText.split("\n")).map(Component::literal).collect(Collectors.toList()), mouseX, mouseY);
				}
				customTooltipShown = true;
			}
		if (DisplayLogicAttribute2Procedure.execute(entity))
			if (mouseX > leftPos + 257 && mouseX < leftPos + 268 && mouseY > topPos + 33 && mouseY < topPos + 44) {
				String hoverText = ReturnNextAttributeTwoProcedure.execute(entity);
				if (hoverText != null) {
					guiGraphics.renderComponentTooltip(font, Arrays.stream(hoverText.split("\n")).map(Component::literal).collect(Collectors.toList()), mouseX, mouseY);
				}
				customTooltipShown = true;
			}
		if (DisplayLogicAttribute3Procedure.execute(entity))
			if (mouseX > leftPos + -46 && mouseX < leftPos + -22 && mouseY > topPos + 56 && mouseY < topPos + 80) {
				String hoverText = ReturnAttributeThreeTipProcedure.execute();
				if (hoverText != null) {
					guiGraphics.renderComponentTooltip(font, Arrays.stream(hoverText.split("\n")).map(Component::literal).collect(Collectors.toList()), mouseX, mouseY);
				}
				customTooltipShown = true;
			}
		if (DisplayLogicAttribute3Procedure.execute(entity))
			if (mouseX > leftPos + 85 && mouseX < leftPos + 96 && mouseY > topPos + 63 && mouseY < topPos + 74) {
				String hoverText = ReturnNextAttributeThreeProcedure.execute(entity);
				if (hoverText != null) {
					guiGraphics.renderComponentTooltip(font, Arrays.stream(hoverText.split("\n")).map(Component::literal).collect(Collectors.toList()), mouseX, mouseY);
				}
				customTooltipShown = true;
			}
		if (DisplayLogicAttribute4Procedure.execute(entity))
			if (mouseX > leftPos + 125 && mouseX < leftPos + 149 && mouseY > topPos + 57 && mouseY < topPos + 81) {
				String hoverText = ReturnAttributeForthTipProcedure.execute();
				if (hoverText != null) {
					guiGraphics.renderComponentTooltip(font, Arrays.stream(hoverText.split("\n")).map(Component::literal).collect(Collectors.toList()), mouseX, mouseY);
				}
				customTooltipShown = true;
			}
		if (DisplayLogicAttribute4Procedure.execute(entity))
			if (mouseX > leftPos + 258 && mouseX < leftPos + 270 && mouseY > topPos + 63 && mouseY < topPos + 74) {
				String hoverText = ReturnNextAttributeForthProcedure.execute(entity);
				if (hoverText != null) {
					guiGraphics.renderComponentTooltip(font, Arrays.stream(hoverText.split("\n")).map(Component::literal).collect(Collectors.toList()), mouseX, mouseY);
				}
				customTooltipShown = true;
			}
		if (DisplayLogicAttribute5Procedure.execute(entity))
			if (mouseX > leftPos + -46 && mouseX < leftPos + -22 && mouseY > topPos + 89 && mouseY < topPos + 113) {
				String hoverText = ReturnAttributeFifthTipProcedure.execute();
				if (hoverText != null) {
					guiGraphics.renderComponentTooltip(font, Arrays.stream(hoverText.split("\n")).map(Component::literal).collect(Collectors.toList()), mouseX, mouseY);
				}
				customTooltipShown = true;
			}
		if (DisplayLogicAttribute6Procedure.execute(entity))
			if (mouseX > leftPos + 124 && mouseX < leftPos + 148 && mouseY > topPos + 89 && mouseY < topPos + 113) {
				String hoverText = ReturnAttributeSixthTipProcedure.execute();
				if (hoverText != null) {
					guiGraphics.renderComponentTooltip(font, Arrays.stream(hoverText.split("\n")).map(Component::literal).collect(Collectors.toList()), mouseX, mouseY);
				}
				customTooltipShown = true;
			}
		if (DisplayLogicAttribute7Procedure.execute(entity))
			if (mouseX > leftPos + -46 && mouseX < leftPos + -22 && mouseY > topPos + 121 && mouseY < topPos + 145) {
				String hoverText = ReturnAttributeSeventhTipProcedure.execute();
				if (hoverText != null) {
					guiGraphics.renderComponentTooltip(font, Arrays.stream(hoverText.split("\n")).map(Component::literal).collect(Collectors.toList()), mouseX, mouseY);
				}
				customTooltipShown = true;
			}
		if (DisplayLogicAttribute5Procedure.execute(entity))
			if (mouseX > leftPos + 84 && mouseX < leftPos + 95 && mouseY > topPos + 95 && mouseY < topPos + 106) {
				String hoverText = ReturnNextAttributeFifthProcedure.execute(entity);
				if (hoverText != null) {
					guiGraphics.renderComponentTooltip(font, Arrays.stream(hoverText.split("\n")).map(Component::literal).collect(Collectors.toList()), mouseX, mouseY);
				}
				customTooltipShown = true;
			}
		if (DisplayLogicAttribute6Procedure.execute(entity))
			if (mouseX > leftPos + 258 && mouseX < leftPos + 270 && mouseY > topPos + 94 && mouseY < topPos + 106) {
				String hoverText = ReturnNextAttributeSixthProcedure.execute(entity);
				if (hoverText != null) {
					guiGraphics.renderComponentTooltip(font, Arrays.stream(hoverText.split("\n")).map(Component::literal).collect(Collectors.toList()), mouseX, mouseY);
				}
				customTooltipShown = true;
			}
		if (DisplayLogicAttribute7Procedure.execute(entity))
			if (mouseX > leftPos + 86 && mouseX < leftPos + 97 && mouseY > topPos + 127 && mouseY < topPos + 138) {
				String hoverText = ReturnNextAttributeSeventhProcedure.execute(entity);
				if (hoverText != null) {
					guiGraphics.renderComponentTooltip(font, Arrays.stream(hoverText.split("\n")).map(Component::literal).collect(Collectors.toList()), mouseX, mouseY);
				}
				customTooltipShown = true;
			}
		if (DisplayLogicAttribute8Procedure.execute(entity))
			if (mouseX > leftPos + 124 && mouseX < leftPos + 148 && mouseY > topPos + 121 && mouseY < topPos + 145) {
				String hoverText = ReturnAttributeEightTipProcedure.execute();
				if (hoverText != null) {
					guiGraphics.renderComponentTooltip(font, Arrays.stream(hoverText.split("\n")).map(Component::literal).collect(Collectors.toList()), mouseX, mouseY);
				}
				customTooltipShown = true;
			}
		if (DisplayLogicAttribute8Procedure.execute(entity))
			if (mouseX > leftPos + 259 && mouseX < leftPos + 270 && mouseY > topPos + 127 && mouseY < topPos + 140) {
				String hoverText = ReturnNextAttributeEightProcedure.execute(entity);
				if (hoverText != null) {
					guiGraphics.renderComponentTooltip(font, Arrays.stream(hoverText.split("\n")).map(Component::literal).collect(Collectors.toList()), mouseX, mouseY);
				}
				customTooltipShown = true;
			}
		if (DisplayLogicAttribute9Procedure.execute(entity))
			if (mouseX > leftPos + -46 && mouseX < leftPos + -22 && mouseY > topPos + 153 && mouseY < topPos + 177) {
				String hoverText = ReturnAttributeNineTipProcedure.execute();
				if (hoverText != null) {
					guiGraphics.renderComponentTooltip(font, Arrays.stream(hoverText.split("\n")).map(Component::literal).collect(Collectors.toList()), mouseX, mouseY);
				}
				customTooltipShown = true;
			}
		if (DisplayLogicAttribute10Procedure.execute(entity))
			if (mouseX > leftPos + 125 && mouseX < leftPos + 149 && mouseY > topPos + 153 && mouseY < topPos + 177) {
				String hoverText = ReturnAttributeTenTipProcedure.execute();
				if (hoverText != null) {
					guiGraphics.renderComponentTooltip(font, Arrays.stream(hoverText.split("\n")).map(Component::literal).collect(Collectors.toList()), mouseX, mouseY);
				}
				customTooltipShown = true;
			}
		if (DisplayLogicAttribute9Procedure.execute(entity))
			if (mouseX > leftPos + 85 && mouseX < leftPos + 98 && mouseY > topPos + 159 && mouseY < topPos + 173) {
				String hoverText = ReturnNextAttributeNineProcedure.execute(entity);
				if (hoverText != null) {
					guiGraphics.renderComponentTooltip(font, Arrays.stream(hoverText.split("\n")).map(Component::literal).collect(Collectors.toList()), mouseX, mouseY);
				}
				customTooltipShown = true;
			}
		if (DisplayLogicAttribute10Procedure.execute(entity))
			if (mouseX > leftPos + 257 && mouseX < leftPos + 270 && mouseY > topPos + 158 && mouseY < topPos + 171) {
				String hoverText = ReturnNextAttributeTenProcedure.execute(entity);
				if (hoverText != null) {
					guiGraphics.renderComponentTooltip(font, Arrays.stream(hoverText.split("\n")).map(Component::literal).collect(Collectors.toList()), mouseX, mouseY);
				}
				customTooltipShown = true;
			}
		if (!customTooltipShown)
			this.renderTooltip(guiGraphics, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/bg.png"), this.leftPos + -61, this.topPos + -21, 0, 0, 350, 210, 350, 210);
		if (DisplayLogicAttribute1Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/bg_attributes.png"), this.leftPos + -16, this.topPos + 22, 0, 0, 97, 30, 97, 30);
		}
		if (DisplayLogicAttribute1Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/iconbg.png"), this.leftPos + -46, this.topPos + 25, 0, 0, 24, 24, 24, 24);
		}
		if (DisplayLogicAttribute1Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/att_1.png"), this.leftPos + -42, this.topPos + 29, 0, 0, 16, 16, 16, 16);
		}
		if (DisplayLogicLockAttribute1Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/bg_attributes_locked.png"), this.leftPos + -16, this.topPos + 22, 0, 0, 97, 30, 97, 30);
		}
		if (DisplayLogicAttribute2Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/bg_attributes.png"), this.leftPos + 155, this.topPos + 23, 0, 0, 97, 30, 97, 30);
		}
		if (DisplayLogicAttribute2Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/iconbg.png"), this.leftPos + 125, this.topPos + 26, 0, 0, 24, 24, 24, 24);
		}
		if (DisplayLogicAttribute2Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/att_2.png"), this.leftPos + 129, this.topPos + 30, 0, 0, 16, 16, 16, 16);
		}
		if (DisplayLogicLockAttribute2Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/bg_attributes_locked.png"), this.leftPos + 155, this.topPos + 23, 0, 0, 97, 30, 97, 30);
		}
		if (DisplayLogicAttribute3Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/bg_attributes.png"), this.leftPos + -16, this.topPos + 54, 0, 0, 97, 30, 97, 30);
		}
		if (DisplayLogicAttribute3Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/iconbg.png"), this.leftPos + -46, this.topPos + 56, 0, 0, 24, 24, 24, 24);
		}
		if (DisplayLogicAttribute3Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/att_3.png"), this.leftPos + -42, this.topPos + 60, 0, 0, 16, 16, 16, 16);
		}
		if (DisplayLogicLockAttribute3Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/bg_attributes_locked.png"), this.leftPos + -16, this.topPos + 54, 0, 0, 97, 30, 97, 30);
		}
		if (DisplayLogicAttribute4Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/iconbg.png"), this.leftPos + 125, this.topPos + 57, 0, 0, 24, 24, 24, 24);
		}
		if (DisplayLogicAttribute4Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/att_4.png"), this.leftPos + 129, this.topPos + 61, 0, 0, 16, 16, 16, 16);
		}
		if (DisplayLogicAttribute5Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/bg_attributes.png"), this.leftPos + -16, this.topPos + 86, 0, 0, 97, 30, 97, 30);
		}
		if (DisplayLogicAttribute7Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/bg_attributes.png"), this.leftPos + -16, this.topPos + 118, 0, 0, 97, 30, 97, 30);
		}
		if (DisplayLogicAttribute9Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/bg_attributes.png"), this.leftPos + -16, this.topPos + 150, 0, 0, 97, 30, 97, 30);
		}
		if (DisplayLogicAttribute4Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/bg_attributes.png"), this.leftPos + 155, this.topPos + 54, 0, 0, 97, 30, 97, 30);
		}
		if (DisplayLogicAttribute6Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/bg_attributes.png"), this.leftPos + 155, this.topPos + 86, 0, 0, 97, 30, 97, 30);
		}
		if (DisplayLogicAttribute8Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/bg_attributes.png"), this.leftPos + 155, this.topPos + 118, 0, 0, 97, 30, 97, 30);
		}
		if (DisplayLogicAttribute10Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/bg_attributes.png"), this.leftPos + 155, this.topPos + 150, 0, 0, 97, 30, 97, 30);
		}
		if (DisplayLogicAttribute5Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/iconbg.png"), this.leftPos + -46, this.topPos + 90, 0, 0, 24, 24, 24, 24);
		}
		if (DisplayLogicAttribute6Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/iconbg.png"), this.leftPos + 125, this.topPos + 89, 0, 0, 24, 24, 24, 24);
		}
		if (DisplayLogicAttribute7Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/iconbg.png"), this.leftPos + -46, this.topPos + 121, 0, 0, 24, 24, 24, 24);
		}
		if (DisplayLogicAttribute8Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/iconbg.png"), this.leftPos + 125, this.topPos + 121, 0, 0, 24, 24, 24, 24);
		}
		if (DisplayLogicAttribute9Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/iconbg.png"), this.leftPos + -46, this.topPos + 153, 0, 0, 24, 24, 24, 24);
		}
		if (DisplayLogicAttribute10Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/iconbg.png"), this.leftPos + 125, this.topPos + 153, 0, 0, 24, 24, 24, 24);
		}
		if (DisplayLogicAttribute5Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/att_5.png"), this.leftPos + -42, this.topPos + 94, 0, 0, 16, 16, 16, 16);
		}
		if (DisplayLogicAttribute6Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/att_6.png"), this.leftPos + 129, this.topPos + 93, 0, 0, 16, 16, 16, 16);
		}
		if (DisplayLogicAttribute7Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/att_7.png"), this.leftPos + -42, this.topPos + 125, 0, 0, 16, 16, 16, 16);
		}
		if (DisplayLogicAttribute8Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/att_8.png"), this.leftPos + 130, this.topPos + 125, 0, 0, 16, 16, 16, 16);
		}
		if (DisplayLogicAttribute9Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/att_9.png"), this.leftPos + -42, this.topPos + 157, 0, 0, 16, 16, 16, 16);
		}
		if (DisplayLogicAttribute10Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/att_10.png"), this.leftPos + 130, this.topPos + 157, 0, 0, 16, 16, 16, 16);
		}
		if (IsAt0Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/ui_bar_0.png"), this.leftPos + -46, this.topPos + -1, 0, 0, 80, 12, 80, 12);
		}
		if (IsAt5Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/ui_bar_5.png"), this.leftPos + -46, this.topPos + -1, 0, 0, 80, 12, 80, 12);
		}
		if (IsAt10Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/ui_bar_10.png"), this.leftPos + -46, this.topPos + -1, 0, 0, 80, 12, 80, 12);
		}
		if (IsAt15Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/ui_bar_15.png"), this.leftPos + -46, this.topPos + -1, 0, 0, 80, 12, 80, 12);
		}
		if (IsAt20Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/ui_bar_20.png"), this.leftPos + -46, this.topPos + -1, 0, 0, 80, 12, 80, 12);
		}
		if (IsAt25Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/ui_bar_25.png"), this.leftPos + -46, this.topPos + -1, 0, 0, 80, 12, 80, 12);
		}
		if (IsAt30Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/ui_bar_30.png"), this.leftPos + -46, this.topPos + -1, 0, 0, 80, 12, 80, 12);
		}
		if (IsAt35Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/ui_bar_35.png"), this.leftPos + -46, this.topPos + -1, 0, 0, 80, 12, 80, 12);
		}
		if (IsAt40Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/ui_bar_40.png"), this.leftPos + -46, this.topPos + -1, 0, 0, 80, 12, 80, 12);
		}
		if (IsAt45Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/ui_bar_45.png"), this.leftPos + -46, this.topPos + -1, 0, 0, 80, 12, 80, 12);
		}
		if (IsAt50Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/ui_bar_50.png"), this.leftPos + -46, this.topPos + -1, 0, 0, 80, 12, 80, 12);
		}
		if (IsAt55Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/ui_bar_55.png"), this.leftPos + -46, this.topPos + -1, 0, 0, 80, 12, 80, 12);
		}
		if (IsAt60Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/ui_bar_60.png"), this.leftPos + -46, this.topPos + -1, 0, 0, 80, 12, 80, 12);
		}
		if (IsAt65Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/ui_bar_65.png"), this.leftPos + -46, this.topPos + -1, 0, 0, 80, 12, 80, 12);
		}
		if (IsAt70Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/ui_bar_70.png"), this.leftPos + -46, this.topPos + -1, 0, 0, 80, 12, 80, 12);
		}
		if (IsAt75Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/ui_bar_75.png"), this.leftPos + -46, this.topPos + -1, 0, 0, 80, 12, 80, 12);
		}
		if (IsAt80Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/ui_bar_80.png"), this.leftPos + -46, this.topPos + -1, 0, 0, 80, 12, 80, 12);
		}
		if (IsAt85Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/ui_bar_85.png"), this.leftPos + -46, this.topPos + -1, 0, 0, 80, 12, 80, 12);
		}
		if (IsAt90Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/ui_bar_90.png"), this.leftPos + -46, this.topPos + -1, 0, 0, 80, 12, 80, 12);
		}
		if (IsAt95Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/ui_bar_95.png"), this.leftPos + -46, this.topPos + -1, 0, 0, 80, 12, 80, 12);
		}
		if (IsAt99Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/ui_bar_99.png"), this.leftPos + -46, this.topPos + -1, 0, 0, 80, 12, 80, 12);
		}
		if (DisplayLogicLockAttribute4Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/bg_attributes_locked.png"), this.leftPos + 156, this.topPos + 54, 0, 0, 97, 30, 97, 30);
		}
		if (DisplayLogicLockAttribute5Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/bg_attributes_locked.png"), this.leftPos + -16, this.topPos + 86, 0, 0, 97, 30, 97, 30);
		}
		if (DisplayLogicLockAttribute6Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/bg_attributes_locked.png"), this.leftPos + 155, this.topPos + 86, 0, 0, 97, 30, 97, 30);
		}
		if (DisplayLogicLockAttribute7Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/bg_attributes_locked.png"), this.leftPos + -16, this.topPos + 118, 0, 0, 97, 30, 97, 30);
		}
		if (DisplayLogicLockAttribute8Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/bg_attributes_locked.png"), this.leftPos + 155, this.topPos + 118, 0, 0, 97, 30, 97, 30);
		}
		if (DisplayLogicLockAttribute9Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/bg_attributes_locked.png"), this.leftPos + -16, this.topPos + 150, 0, 0, 97, 30, 97, 30);
		}
		if (DisplayLogicLockAttribute10Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("rpg_attribute_system:textures/screens/bg_attributes_locked.png"), this.leftPos + 155, this.topPos + 150, 0, 0, 97, 30, 97, 30);
		}
		RenderSystem.disableBlend();
	}

	@Override
	public boolean keyPressed(int key, int b, int c) {
		if (key == 256) {
			this.minecraft.player.closeContainer();
			return true;
		}
		return super.keyPressed(key, b, c);
	}

	@Override
	protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		guiGraphics.drawString(this.font, Component.translatable("gui.rpg_attribute_system.player_stats_gui.label_level"), 37, 1, -16777216, false);
		guiGraphics.drawString(this.font, ReturnCurrentLevelProcedure.execute(entity), 68, 1, -1, false);
		guiGraphics.drawString(this.font, Component.translatable("gui.rpg_attribute_system.player_stats_gui.label_available_points"), 125, 2, -16777216, false);
		guiGraphics.drawString(this.font, ReturnExtraPointsProcedure.execute(entity), 207, 2, -1, false);
		if (DisplayLogicAttribute1Procedure.execute(entity))
			guiGraphics.drawString(this.font, ReturnCurrentAttributeOneProcedure.execute(entity), -8, 33, -1, false);
		if (DisplayLogicAttribute2Procedure.execute(entity))
			guiGraphics.drawString(this.font, ReturnCurrentAttributeTwoProcedure.execute(entity), 164, 34, -1, false);
		if (DisplayLogicAttribute3Procedure.execute(entity))
			guiGraphics.drawString(this.font, ReturnCurrentAttributeThreeProcedure.execute(entity), -8, 65, -1, false);
		if (DisplayLogicAttribute4Procedure.execute(entity))
			guiGraphics.drawString(this.font, ReturnCurrentAttributeForthProcedure.execute(entity), 164, 66, -1, false);
		if (DisplayLogicAttribute5Procedure.execute(entity))
			guiGraphics.drawString(this.font, ReturnCurrentAttributeFifthProcedure.execute(entity), -8, 97, -1, false);
		if (DisplayLogicAttribute6Procedure.execute(entity))
			guiGraphics.drawString(this.font, ReturnCurrentAttributeSixthProcedure.execute(entity), 164, 97, -1, false);
		if (DisplayLogicAttribute7Procedure.execute(entity))
			guiGraphics.drawString(this.font, ReturnCurrentAttributeSeventhProcedure.execute(entity), -8, 128, -1, false);
		if (DisplayLogicAttribute8Procedure.execute(entity))
			guiGraphics.drawString(this.font, ReturnCurrentAttributeEightProcedure.execute(entity), 164, 129, -1, false);
		if (DisplayLogicAttribute9Procedure.execute(entity))
			guiGraphics.drawString(this.font, ReturnCurrentAttributeNineProcedure.execute(entity), -8, 162, -1, false);
		if (DisplayLogicAttribute10Procedure.execute(entity))
			guiGraphics.drawString(this.font, ReturnCurrentAttributeTenProcedure.execute(entity), 164, 162, -1, false);
		guiGraphics.drawString(this.font, ReturnCurrentModifierProcedure.execute(entity), 259, 14, -1, false);
		guiGraphics.drawString(this.font, CurrentXpToLevelProcedure.execute(entity), -43, 1, -16777216, false);
	}

	@Override
	public void init() {
		super.init();
		imagebutton_button_notclicked = new ImageButton(this.leftPos + 83, this.topPos + 31, 16, 14, 0, 0, 14, ResourceLocation.parse("rpg_attribute_system:textures/screens/atlas/imagebutton_button_notclicked.png"), 16, 28, e -> {
			int x = PlayerStatsGUIScreen.this.x;
			int y = PlayerStatsGUIScreen.this.y;
			if (DisplayLogicAttribute1Procedure.execute(entity)) {
				RpgAttributeSystemMod.PACKET_HANDLER.sendToServer(new PlayerStatsGUIButtonMessage(0, x, y, z));
				PlayerStatsGUIButtonMessage.handleButtonAction(entity, 0, x, y, z);
			}
		});
		this.addRenderableWidget(imagebutton_button_notclicked);
		imagebutton_button_notclicked1 = new ImageButton(this.leftPos + 255, this.topPos + 31, 16, 14, 0, 0, 14, ResourceLocation.parse("rpg_attribute_system:textures/screens/atlas/imagebutton_button_notclicked1.png"), 16, 28, e -> {
			int x = PlayerStatsGUIScreen.this.x;
			int y = PlayerStatsGUIScreen.this.y;
			if (DisplayLogicAttribute2Procedure.execute(entity)) {
				RpgAttributeSystemMod.PACKET_HANDLER.sendToServer(new PlayerStatsGUIButtonMessage(1, x, y, z));
				PlayerStatsGUIButtonMessage.handleButtonAction(entity, 1, x, y, z);
			}
		});
		this.addRenderableWidget(imagebutton_button_notclicked1);
		imagebutton_button_notclicked2 = new ImageButton(this.leftPos + 83, this.topPos + 62, 16, 14, 0, 0, 14, ResourceLocation.parse("rpg_attribute_system:textures/screens/atlas/imagebutton_button_notclicked2.png"), 16, 28, e -> {
			int x = PlayerStatsGUIScreen.this.x;
			int y = PlayerStatsGUIScreen.this.y;
			if (DisplayLogicAttribute3Procedure.execute(entity)) {
				RpgAttributeSystemMod.PACKET_HANDLER.sendToServer(new PlayerStatsGUIButtonMessage(2, x, y, z));
				PlayerStatsGUIButtonMessage.handleButtonAction(entity, 2, x, y, z);
			}
		});
		this.addRenderableWidget(imagebutton_button_notclicked2);
		imagebutton_button_notclicked3 = new ImageButton(this.leftPos + 256, this.topPos + 61, 16, 14, 0, 0, 14, ResourceLocation.parse("rpg_attribute_system:textures/screens/atlas/imagebutton_button_notclicked3.png"), 16, 28, e -> {
			int x = PlayerStatsGUIScreen.this.x;
			int y = PlayerStatsGUIScreen.this.y;
			if (DisplayLogicAttribute4Procedure.execute(entity)) {
				RpgAttributeSystemMod.PACKET_HANDLER.sendToServer(new PlayerStatsGUIButtonMessage(3, x, y, z));
				PlayerStatsGUIButtonMessage.handleButtonAction(entity, 3, x, y, z);
			}
		});
		this.addRenderableWidget(imagebutton_button_notclicked3);
		imagebutton_button_notclicked4 = new ImageButton(this.leftPos + 83, this.topPos + 95, 16, 14, 0, 0, 14, ResourceLocation.parse("rpg_attribute_system:textures/screens/atlas/imagebutton_button_notclicked4.png"), 16, 28, e -> {
			int x = PlayerStatsGUIScreen.this.x;
			int y = PlayerStatsGUIScreen.this.y;
			if (DisplayLogicAttribute5Procedure.execute(entity)) {
				RpgAttributeSystemMod.PACKET_HANDLER.sendToServer(new PlayerStatsGUIButtonMessage(4, x, y, z));
				PlayerStatsGUIButtonMessage.handleButtonAction(entity, 4, x, y, z);
			}
		});
		this.addRenderableWidget(imagebutton_button_notclicked4);
		imagebutton_button_notclicked5 = new ImageButton(this.leftPos + 256, this.topPos + 94, 16, 14, 0, 0, 14, ResourceLocation.parse("rpg_attribute_system:textures/screens/atlas/imagebutton_button_notclicked5.png"), 16, 28, e -> {
			int x = PlayerStatsGUIScreen.this.x;
			int y = PlayerStatsGUIScreen.this.y;
			if (DisplayLogicAttribute6Procedure.execute(entity)) {
				RpgAttributeSystemMod.PACKET_HANDLER.sendToServer(new PlayerStatsGUIButtonMessage(5, x, y, z));
				PlayerStatsGUIButtonMessage.handleButtonAction(entity, 5, x, y, z);
			}
		});
		this.addRenderableWidget(imagebutton_button_notclicked5);
		imagebutton_button_notclicked6 = new ImageButton(this.leftPos + 83, this.topPos + 125, 16, 14, 0, 0, 14, ResourceLocation.parse("rpg_attribute_system:textures/screens/atlas/imagebutton_button_notclicked6.png"), 16, 28, e -> {
			int x = PlayerStatsGUIScreen.this.x;
			int y = PlayerStatsGUIScreen.this.y;
			if (DisplayLogicAttribute7Procedure.execute(entity)) {
				RpgAttributeSystemMod.PACKET_HANDLER.sendToServer(new PlayerStatsGUIButtonMessage(6, x, y, z));
				PlayerStatsGUIButtonMessage.handleButtonAction(entity, 6, x, y, z);
			}
		});
		this.addRenderableWidget(imagebutton_button_notclicked6);
		imagebutton_button_notclicked7 = new ImageButton(this.leftPos + 256, this.topPos + 127, 16, 14, 0, 0, 14, ResourceLocation.parse("rpg_attribute_system:textures/screens/atlas/imagebutton_button_notclicked7.png"), 16, 28, e -> {
			int x = PlayerStatsGUIScreen.this.x;
			int y = PlayerStatsGUIScreen.this.y;
			if (DisplayLogicAttribute8Procedure.execute(entity)) {
				RpgAttributeSystemMod.PACKET_HANDLER.sendToServer(new PlayerStatsGUIButtonMessage(7, x, y, z));
				PlayerStatsGUIButtonMessage.handleButtonAction(entity, 7, x, y, z);
			}
		});
		this.addRenderableWidget(imagebutton_button_notclicked7);
		imagebutton_button_notclicked9 = new ImageButton(this.leftPos + 256, this.topPos + 158, 16, 14, 0, 0, 14, ResourceLocation.parse("rpg_attribute_system:textures/screens/atlas/imagebutton_button_notclicked9.png"), 16, 28, e -> {
			int x = PlayerStatsGUIScreen.this.x;
			int y = PlayerStatsGUIScreen.this.y;
			if (DisplayLogicAttribute10Procedure.execute(entity)) {
				RpgAttributeSystemMod.PACKET_HANDLER.sendToServer(new PlayerStatsGUIButtonMessage(8, x, y, z));
				PlayerStatsGUIButtonMessage.handleButtonAction(entity, 8, x, y, z);
			}
		});
		this.addRenderableWidget(imagebutton_button_notclicked9);
		imagebutton_button_for_stats = new ImageButton(this.leftPos + -74, this.topPos + -4, 13, 13, 0, 0, 13, ResourceLocation.parse("rpg_attribute_system:textures/screens/atlas/imagebutton_button_for_stats.png"), 13, 26, e -> {
			int x = PlayerStatsGUIScreen.this.x;
			int y = PlayerStatsGUIScreen.this.y;
			if (ReturnGlobalSectionsDisplayProcedure.execute()) {
				RpgAttributeSystemMod.PACKET_HANDLER.sendToServer(new PlayerStatsGUIButtonMessage(9, x, y, z));
				PlayerStatsGUIButtonMessage.handleButtonAction(entity, 9, x, y, z);
			}
		});
		this.addRenderableWidget(imagebutton_button_for_stats);
		imagebutton_button_left = new ImageButton(this.leftPos + 251, this.topPos + 14, 6, 8, 0, 0, 8, ResourceLocation.parse("rpg_attribute_system:textures/screens/atlas/imagebutton_button_left.png"), 6, 16, e -> {
			int x = PlayerStatsGUIScreen.this.x;
			int y = PlayerStatsGUIScreen.this.y;
			if (true) {
				RpgAttributeSystemMod.PACKET_HANDLER.sendToServer(new PlayerStatsGUIButtonMessage(10, x, y, z));
				PlayerStatsGUIButtonMessage.handleButtonAction(entity, 10, x, y, z);
			}
		});
		this.addRenderableWidget(imagebutton_button_left);
		imagebutton_button_right = new ImageButton(this.leftPos + 272, this.topPos + 14, 6, 8, 0, 0, 8, ResourceLocation.parse("rpg_attribute_system:textures/screens/atlas/imagebutton_button_right.png"), 6, 16, e -> {
			int x = PlayerStatsGUIScreen.this.x;
			int y = PlayerStatsGUIScreen.this.y;
			if (true) {
				RpgAttributeSystemMod.PACKET_HANDLER.sendToServer(new PlayerStatsGUIButtonMessage(11, x, y, z));
				PlayerStatsGUIButtonMessage.handleButtonAction(entity, 11, x, y, z);
			}
		});
		this.addRenderableWidget(imagebutton_button_right);
		imagebutton_button_notclicked8 = new ImageButton(this.leftPos + 83, this.topPos + 158, 16, 14, 0, 0, 14, ResourceLocation.parse("rpg_attribute_system:textures/screens/atlas/imagebutton_button_notclicked8.png"), 16, 28, e -> {
			int x = PlayerStatsGUIScreen.this.x;
			int y = PlayerStatsGUIScreen.this.y;
			if (DisplayLogicAttribute9Procedure.execute(entity)) {
				RpgAttributeSystemMod.PACKET_HANDLER.sendToServer(new PlayerStatsGUIButtonMessage(12, x, y, z));
				PlayerStatsGUIButtonMessage.handleButtonAction(entity, 12, x, y, z);
			}
		});
		this.addRenderableWidget(imagebutton_button_notclicked8);
	}
}