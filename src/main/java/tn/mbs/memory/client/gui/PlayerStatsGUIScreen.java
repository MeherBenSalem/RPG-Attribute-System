package tn.mbs.memory.client.gui;

import tn.mbs.memory.world.inventory.PlayerStatsGUIMenu;
import tn.mbs.memory.procedures.ReturnNextAttributeTwoProcedure;
import tn.mbs.memory.procedures.ReturnNextAttributeThreeProcedure;
import tn.mbs.memory.procedures.ReturnNextAttributeTenProcedure;
import tn.mbs.memory.procedures.ReturnNextAttributeSixthProcedure;
import tn.mbs.memory.procedures.ReturnNextAttributeSeventhProcedure;
import tn.mbs.memory.procedures.ReturnNextAttributeOneProcedure;
import tn.mbs.memory.procedures.ReturnNextAttributeNineProcedure;
import tn.mbs.memory.procedures.ReturnNextAttributeForthProcedure;
import tn.mbs.memory.procedures.ReturnNextAttributeFifthProcedure;
import tn.mbs.memory.procedures.ReturnNextAttributeEightProcedure;
import tn.mbs.memory.procedures.ReturnGlobalSectionsDisplayProcedure;
import tn.mbs.memory.procedures.ReturnExtraPointsProcedure;
import tn.mbs.memory.procedures.ReturnCurrentModifierProcedure;
import tn.mbs.memory.procedures.ReturnCurrentLevelProcedure;
import tn.mbs.memory.procedures.ReturnCurrentAttributeTwoProcedure;
import tn.mbs.memory.procedures.ReturnCurrentAttributeThreeProcedure;
import tn.mbs.memory.procedures.ReturnCurrentAttributeTenProcedure;
import tn.mbs.memory.procedures.ReturnCurrentAttributeSixthProcedure;
import tn.mbs.memory.procedures.ReturnCurrentAttributeSeventhProcedure;
import tn.mbs.memory.procedures.ReturnCurrentAttributeOneProcedure;
import tn.mbs.memory.procedures.ReturnCurrentAttributeNineProcedure;
import tn.mbs.memory.procedures.ReturnCurrentAttributeForthProcedure;
import tn.mbs.memory.procedures.ReturnCurrentAttributeFifthProcedure;
import tn.mbs.memory.procedures.ReturnCurrentAttributeEightProcedure;
import tn.mbs.memory.procedures.ReturnAttributeTwoTipProcedure;
import tn.mbs.memory.procedures.ReturnAttributeThreeTipProcedure;
import tn.mbs.memory.procedures.ReturnAttributeTenTipProcedure;
import tn.mbs.memory.procedures.ReturnAttributeSixthTipProcedure;
import tn.mbs.memory.procedures.ReturnAttributeSeventhTipProcedure;
import tn.mbs.memory.procedures.ReturnAttributeOneTipProcedure;
import tn.mbs.memory.procedures.ReturnAttributeNineTipProcedure;
import tn.mbs.memory.procedures.ReturnAttributeForthTipProcedure;
import tn.mbs.memory.procedures.ReturnAttributeFifthTipProcedure;
import tn.mbs.memory.procedures.ReturnAttributeEightTipProcedure;
import tn.mbs.memory.procedures.PlayerNameProcedure;
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
import tn.mbs.memory.procedures.GetThePlayerModelProcedure;
import tn.mbs.memory.procedures.DisplayLogicLockAttribute9Procedure;
import tn.mbs.memory.procedures.DisplayLogicLockAttribute8Procedure;
import tn.mbs.memory.procedures.DisplayLogicLockAttribute7Procedure;
import tn.mbs.memory.procedures.DisplayLogicLockAttribute6Procedure;
import tn.mbs.memory.procedures.DisplayLogicLockAttribute5Procedure;
import tn.mbs.memory.procedures.DisplayLogicLockAttribute4Procedure;
import tn.mbs.memory.procedures.DisplayLogicLockAttribute3Procedure;
import tn.mbs.memory.procedures.DisplayLogicLockAttribute2Procedure;
import tn.mbs.memory.procedures.DisplayLogicLockAttribute1Procedure;
import tn.mbs.memory.procedures.DisplayLogicLockAttribute10Procedure;
import tn.mbs.memory.procedures.DisplayLogicAttribute9Procedure;
import tn.mbs.memory.procedures.DisplayLogicAttribute8Procedure;
import tn.mbs.memory.procedures.DisplayLogicAttribute7Procedure;
import tn.mbs.memory.procedures.DisplayLogicAttribute6Procedure;
import tn.mbs.memory.procedures.DisplayLogicAttribute5Procedure;
import tn.mbs.memory.procedures.DisplayLogicAttribute4Procedure;
import tn.mbs.memory.procedures.DisplayLogicAttribute3Procedure;
import tn.mbs.memory.procedures.DisplayLogicAttribute2Procedure;
import tn.mbs.memory.procedures.DisplayLogicAttribute1Procedure;
import tn.mbs.memory.procedures.DisplayLogicAttribute10Procedure;
import tn.mbs.memory.procedures.CurrentXpToLevelProcedure;
import tn.mbs.memory.network.PlayerStatsGUIButtonMessage;
import tn.mbs.memory.init.MemoryOfThePastModScreens;
import tn.mbs.memory.MemoryOfThePastMod;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.GuiGraphics;

import java.util.stream.Collectors;
import java.util.Arrays;

import com.mojang.blaze3d.systems.RenderSystem;

public class PlayerStatsGUIScreen extends AbstractContainerScreen<PlayerStatsGUIMenu> implements MemoryOfThePastModScreens.ScreenAccessor {
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
		if (GetThePlayerModelProcedure.execute(world, x, y, z, entity) instanceof LivingEntity livingEntity) {
			InventoryScreen.renderEntityInInventoryFollowsAngle(guiGraphics, this.leftPos + -11, this.topPos + 137, 60, 0f + (float) Math.atan((this.leftPos + -11 - mouseX) / 40.0), (float) Math.atan((this.topPos + 88 - mouseY) / 40.0),
					livingEntity);
		}
		boolean customTooltipShown = false;
		if (mouseX > leftPos + -44 && mouseX < leftPos + -5 && mouseY > topPos + 147 && mouseY < topPos + 158) {
			guiGraphics.renderTooltip(font, Component.translatable("gui.memory_of_the_past.player_stats_gui.tooltip_represents_your_overall_progress"), mouseX, mouseY);
			customTooltipShown = true;
		}
		if (mouseX > leftPos + 78 && mouseX < leftPos + 167 && mouseY > topPos + -8 && mouseY < topPos + 3) {
			guiGraphics.renderTooltip(font, Component.translatable("gui.memory_of_the_past.player_stats_gui.tooltip_indicates_the_number_of_points_y"), mouseX, mouseY);
			customTooltipShown = true;
		}
		if (DisplayLogicAttribute1Procedure.execute())
			if (mouseX > leftPos + 38 && mouseX < leftPos + 62 && mouseY > topPos + 20 && mouseY < topPos + 44) {
				String hoverText = ReturnAttributeOneTipProcedure.execute();
				if (hoverText != null) {
					guiGraphics.renderComponentTooltip(font, Arrays.stream(hoverText.split("\n")).map(Component::literal).collect(Collectors.toList()), mouseX, mouseY);
				}
				customTooltipShown = true;
			}
		if (DisplayLogicAttribute1Procedure.execute())
			if (mouseX > leftPos + 129 && mouseX < leftPos + 143 && mouseY > topPos + 26 && mouseY < topPos + 39) {
				String hoverText = ReturnNextAttributeOneProcedure.execute(entity);
				if (hoverText != null) {
					guiGraphics.renderComponentTooltip(font, Arrays.stream(hoverText.split("\n")).map(Component::literal).collect(Collectors.toList()), mouseX, mouseY);
				}
				customTooltipShown = true;
			}
		if (DisplayLogicAttribute2Procedure.execute())
			if (mouseX > leftPos + 160 && mouseX < leftPos + 184 && mouseY > topPos + 20 && mouseY < topPos + 44) {
				String hoverText = ReturnAttributeTwoTipProcedure.execute();
				if (hoverText != null) {
					guiGraphics.renderComponentTooltip(font, Arrays.stream(hoverText.split("\n")).map(Component::literal).collect(Collectors.toList()), mouseX, mouseY);
				}
				customTooltipShown = true;
			}
		if (DisplayLogicAttribute2Procedure.execute())
			if (mouseX > leftPos + 250 && mouseX < leftPos + 261 && mouseY > topPos + 26 && mouseY < topPos + 37) {
				String hoverText = ReturnNextAttributeTwoProcedure.execute(entity);
				if (hoverText != null) {
					guiGraphics.renderComponentTooltip(font, Arrays.stream(hoverText.split("\n")).map(Component::literal).collect(Collectors.toList()), mouseX, mouseY);
				}
				customTooltipShown = true;
			}
		if (DisplayLogicAttribute3Procedure.execute())
			if (mouseX > leftPos + 38 && mouseX < leftPos + 62 && mouseY > topPos + 52 && mouseY < topPos + 76) {
				String hoverText = ReturnAttributeThreeTipProcedure.execute();
				if (hoverText != null) {
					guiGraphics.renderComponentTooltip(font, Arrays.stream(hoverText.split("\n")).map(Component::literal).collect(Collectors.toList()), mouseX, mouseY);
				}
				customTooltipShown = true;
			}
		if (DisplayLogicAttribute4Procedure.execute())
			if (mouseX > leftPos + 160 && mouseX < leftPos + 184 && mouseY > topPos + 51 && mouseY < topPos + 75) {
				String hoverText = ReturnAttributeForthTipProcedure.execute();
				if (hoverText != null) {
					guiGraphics.renderComponentTooltip(font, Arrays.stream(hoverText.split("\n")).map(Component::literal).collect(Collectors.toList()), mouseX, mouseY);
				}
				customTooltipShown = true;
			}
		if (DisplayLogicAttribute5Procedure.execute())
			if (mouseX > leftPos + 38 && mouseX < leftPos + 62 && mouseY > topPos + 83 && mouseY < topPos + 107) {
				String hoverText = ReturnAttributeFifthTipProcedure.execute();
				if (hoverText != null) {
					guiGraphics.renderComponentTooltip(font, Arrays.stream(hoverText.split("\n")).map(Component::literal).collect(Collectors.toList()), mouseX, mouseY);
				}
				customTooltipShown = true;
			}
		if (DisplayLogicAttribute6Procedure.execute())
			if (mouseX > leftPos + 160 && mouseX < leftPos + 184 && mouseY > topPos + 83 && mouseY < topPos + 107) {
				String hoverText = ReturnAttributeSixthTipProcedure.execute();
				if (hoverText != null) {
					guiGraphics.renderComponentTooltip(font, Arrays.stream(hoverText.split("\n")).map(Component::literal).collect(Collectors.toList()), mouseX, mouseY);
				}
				customTooltipShown = true;
			}
		if (DisplayLogicAttribute7Procedure.execute())
			if (mouseX > leftPos + 39 && mouseX < leftPos + 63 && mouseY > topPos + 114 && mouseY < topPos + 138) {
				String hoverText = ReturnAttributeSeventhTipProcedure.execute();
				if (hoverText != null) {
					guiGraphics.renderComponentTooltip(font, Arrays.stream(hoverText.split("\n")).map(Component::literal).collect(Collectors.toList()), mouseX, mouseY);
				}
				customTooltipShown = true;
			}
		if (DisplayLogicAttribute3Procedure.execute())
			if (mouseX > leftPos + 129 && mouseX < leftPos + 140 && mouseY > topPos + 58 && mouseY < topPos + 69) {
				String hoverText = ReturnNextAttributeThreeProcedure.execute(entity);
				if (hoverText != null) {
					guiGraphics.renderComponentTooltip(font, Arrays.stream(hoverText.split("\n")).map(Component::literal).collect(Collectors.toList()), mouseX, mouseY);
				}
				customTooltipShown = true;
			}
		if (DisplayLogicAttribute4Procedure.execute())
			if (mouseX > leftPos + 250 && mouseX < leftPos + 262 && mouseY > topPos + 58 && mouseY < topPos + 69) {
				String hoverText = ReturnNextAttributeForthProcedure.execute(entity);
				if (hoverText != null) {
					guiGraphics.renderComponentTooltip(font, Arrays.stream(hoverText.split("\n")).map(Component::literal).collect(Collectors.toList()), mouseX, mouseY);
				}
				customTooltipShown = true;
			}
		if (DisplayLogicAttribute5Procedure.execute())
			if (mouseX > leftPos + 129 && mouseX < leftPos + 140 && mouseY > topPos + 89 && mouseY < topPos + 100) {
				String hoverText = ReturnNextAttributeFifthProcedure.execute(entity);
				if (hoverText != null) {
					guiGraphics.renderComponentTooltip(font, Arrays.stream(hoverText.split("\n")).map(Component::literal).collect(Collectors.toList()), mouseX, mouseY);
				}
				customTooltipShown = true;
			}
		if (DisplayLogicAttribute6Procedure.execute())
			if (mouseX > leftPos + 249 && mouseX < leftPos + 261 && mouseY > topPos + 88 && mouseY < topPos + 100) {
				String hoverText = ReturnNextAttributeSixthProcedure.execute(entity);
				if (hoverText != null) {
					guiGraphics.renderComponentTooltip(font, Arrays.stream(hoverText.split("\n")).map(Component::literal).collect(Collectors.toList()), mouseX, mouseY);
				}
				customTooltipShown = true;
			}
		if (DisplayLogicAttribute7Procedure.execute())
			if (mouseX > leftPos + 129 && mouseX < leftPos + 140 && mouseY > topPos + 121 && mouseY < topPos + 132) {
				String hoverText = ReturnNextAttributeSeventhProcedure.execute(entity);
				if (hoverText != null) {
					guiGraphics.renderComponentTooltip(font, Arrays.stream(hoverText.split("\n")).map(Component::literal).collect(Collectors.toList()), mouseX, mouseY);
				}
				customTooltipShown = true;
			}
		if (DisplayLogicAttribute8Procedure.execute())
			if (mouseX > leftPos + 160 && mouseX < leftPos + 184 && mouseY > topPos + 114 && mouseY < topPos + 138) {
				String hoverText = ReturnAttributeEightTipProcedure.execute();
				if (hoverText != null) {
					guiGraphics.renderComponentTooltip(font, Arrays.stream(hoverText.split("\n")).map(Component::literal).collect(Collectors.toList()), mouseX, mouseY);
				}
				customTooltipShown = true;
			}
		if (DisplayLogicAttribute8Procedure.execute())
			if (mouseX > leftPos + 250 && mouseX < leftPos + 261 && mouseY > topPos + 121 && mouseY < topPos + 134) {
				String hoverText = ReturnNextAttributeEightProcedure.execute(entity);
				if (hoverText != null) {
					guiGraphics.renderComponentTooltip(font, Arrays.stream(hoverText.split("\n")).map(Component::literal).collect(Collectors.toList()), mouseX, mouseY);
				}
				customTooltipShown = true;
			}
		if (DisplayLogicAttribute9Procedure.execute())
			if (mouseX > leftPos + 37 && mouseX < leftPos + 61 && mouseY > topPos + 146 && mouseY < topPos + 170) {
				String hoverText = ReturnAttributeNineTipProcedure.execute();
				if (hoverText != null) {
					guiGraphics.renderComponentTooltip(font, Arrays.stream(hoverText.split("\n")).map(Component::literal).collect(Collectors.toList()), mouseX, mouseY);
				}
				customTooltipShown = true;
			}
		if (DisplayLogicAttribute10Procedure.execute())
			if (mouseX > leftPos + 160 && mouseX < leftPos + 184 && mouseY > topPos + 147 && mouseY < topPos + 171) {
				String hoverText = ReturnAttributeTenTipProcedure.execute();
				if (hoverText != null) {
					guiGraphics.renderComponentTooltip(font, Arrays.stream(hoverText.split("\n")).map(Component::literal).collect(Collectors.toList()), mouseX, mouseY);
				}
				customTooltipShown = true;
			}
		if (DisplayLogicAttribute9Procedure.execute())
			if (mouseX > leftPos + 128 && mouseX < leftPos + 141 && mouseY > topPos + 151 && mouseY < topPos + 165) {
				String hoverText = ReturnNextAttributeNineProcedure.execute(entity);
				if (hoverText != null) {
					guiGraphics.renderComponentTooltip(font, Arrays.stream(hoverText.split("\n")).map(Component::literal).collect(Collectors.toList()), mouseX, mouseY);
				}
				customTooltipShown = true;
			}
		if (DisplayLogicAttribute10Procedure.execute())
			if (mouseX > leftPos + 249 && mouseX < leftPos + 262 && mouseY > topPos + 152 && mouseY < topPos + 165) {
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
		guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/bg.png"), this.leftPos + -61, this.topPos + -23, 0, 0, 350, 210, 350, 210);
		if (DisplayLogicAttribute1Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/bg_attributes.png"), this.leftPos + 49, this.topPos + 17, 0, 0, 97, 30, 97, 30);
		}
		if (DisplayLogicAttribute1Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/iconbg.png"), this.leftPos + 38, this.topPos + 20, 0, 0, 24, 24, 24, 24);
		}
		if (DisplayLogicAttribute1Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/att_1.png"), this.leftPos + 42, this.topPos + 24, 0, 0, 17, 17, 17, 17);
		}
		if (DisplayLogicAttribute2Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/bg_attributes.png"), this.leftPos + 170, this.topPos + 17, 0, 0, 97, 30, 97, 30);
		}
		if (DisplayLogicAttribute2Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/iconbg.png"), this.leftPos + 160, this.topPos + 20, 0, 0, 24, 24, 24, 24);
		}
		if (DisplayLogicAttribute2Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/att_2.png"), this.leftPos + 164, this.topPos + 24, 0, 0, 16, 16, 16, 16);
		}
		if (DisplayLogicAttribute3Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/bg_attributes.png"), this.leftPos + 49, this.topPos + 48, 0, 0, 97, 30, 97, 30);
		}
		if (DisplayLogicAttribute5Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/bg_attributes.png"), this.leftPos + 49, this.topPos + 79, 0, 0, 97, 30, 97, 30);
		}
		if (DisplayLogicAttribute7Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/bg_attributes.png"), this.leftPos + 49, this.topPos + 111, 0, 0, 97, 30, 97, 30);
		}
		if (DisplayLogicAttribute9Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/bg_attributes.png"), this.leftPos + 49, this.topPos + 143, 0, 0, 97, 30, 97, 30);
		}
		if (DisplayLogicAttribute4Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/bg_attributes.png"), this.leftPos + 170, this.topPos + 48, 0, 0, 97, 30, 97, 30);
		}
		if (DisplayLogicAttribute6Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/bg_attributes.png"), this.leftPos + 170, this.topPos + 79, 0, 0, 97, 30, 97, 30);
		}
		if (DisplayLogicAttribute8Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/bg_attributes.png"), this.leftPos + 170, this.topPos + 111, 0, 0, 97, 30, 97, 30);
		}
		if (DisplayLogicAttribute10Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/bg_attributes.png"), this.leftPos + 170, this.topPos + 143, 0, 0, 97, 30, 97, 30);
		}
		if (DisplayLogicAttribute3Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/iconbg.png"), this.leftPos + 38, this.topPos + 52, 0, 0, 24, 24, 24, 24);
		}
		if (DisplayLogicAttribute4Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/iconbg.png"), this.leftPos + 160, this.topPos + 51, 0, 0, 24, 24, 24, 24);
		}
		if (DisplayLogicAttribute5Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/iconbg.png"), this.leftPos + 38, this.topPos + 83, 0, 0, 24, 24, 24, 24);
		}
		if (DisplayLogicAttribute6Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/iconbg.png"), this.leftPos + 160, this.topPos + 83, 0, 0, 24, 24, 24, 24);
		}
		if (DisplayLogicAttribute7Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/iconbg.png"), this.leftPos + 38, this.topPos + 114, 0, 0, 24, 24, 24, 24);
		}
		if (DisplayLogicAttribute8Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/iconbg.png"), this.leftPos + 160, this.topPos + 114, 0, 0, 24, 24, 24, 24);
		}
		if (DisplayLogicAttribute9Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/iconbg.png"), this.leftPos + 38, this.topPos + 146, 0, 0, 24, 24, 24, 24);
		}
		if (DisplayLogicAttribute10Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/iconbg.png"), this.leftPos + 160, this.topPos + 147, 0, 0, 24, 24, 24, 24);
		}
		if (DisplayLogicAttribute3Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/att_3.png"), this.leftPos + 42, this.topPos + 56, 0, 0, 16, 16, 16, 16);
		}
		if (DisplayLogicAttribute4Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/att_4.png"), this.leftPos + 164, this.topPos + 55, 0, 0, 17, 17, 17, 17);
		}
		if (DisplayLogicAttribute5Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/att_5.png"), this.leftPos + 42, this.topPos + 87, 0, 0, 16, 16, 16, 16);
		}
		if (DisplayLogicAttribute6Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/att_6.png"), this.leftPos + 164, this.topPos + 87, 0, 0, 16, 16, 16, 16);
		}
		if (DisplayLogicAttribute7Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/att_7.png"), this.leftPos + 43, this.topPos + 118, 0, 0, 16, 16, 16, 16);
		}
		if (DisplayLogicAttribute8Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/att_8.png"), this.leftPos + 164, this.topPos + 118, 0, 0, 16, 16, 16, 16);
		}
		if (DisplayLogicAttribute9Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/att_9.png"), this.leftPos + 42, this.topPos + 150, 0, 0, 16, 16, 16, 16);
		}
		if (DisplayLogicAttribute10Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/att_10.png"), this.leftPos + 164, this.topPos + 150, 0, 0, 16, 16, 16, 16);
		}
		if (IsAt0Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/ui_bar_0.png"), this.leftPos + -52, this.topPos + 162, 0, 0, 80, 12, 80, 12);
		}
		if (IsAt5Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/ui_bar_5.png"), this.leftPos + -52, this.topPos + 162, 0, 0, 80, 12, 80, 12);
		}
		if (IsAt10Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/ui_bar_10.png"), this.leftPos + -52, this.topPos + 162, 0, 0, 80, 12, 80, 12);
		}
		if (IsAt15Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/ui_bar_15.png"), this.leftPos + -52, this.topPos + 162, 0, 0, 80, 12, 80, 12);
		}
		if (IsAt20Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/ui_bar_20.png"), this.leftPos + -52, this.topPos + 162, 0, 0, 80, 12, 80, 12);
		}
		if (IsAt25Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/ui_bar_25.png"), this.leftPos + -52, this.topPos + 162, 0, 0, 80, 12, 80, 12);
		}
		if (IsAt30Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/ui_bar_30.png"), this.leftPos + -52, this.topPos + 162, 0, 0, 80, 12, 80, 12);
		}
		if (IsAt35Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/ui_bar_35.png"), this.leftPos + -52, this.topPos + 162, 0, 0, 80, 12, 80, 12);
		}
		if (IsAt40Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/ui_bar_40.png"), this.leftPos + -52, this.topPos + 162, 0, 0, 80, 12, 80, 12);
		}
		if (IsAt45Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/ui_bar_45.png"), this.leftPos + -52, this.topPos + 162, 0, 0, 80, 12, 80, 12);
		}
		if (IsAt50Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/ui_bar_50.png"), this.leftPos + -52, this.topPos + 162, 0, 0, 80, 12, 80, 12);
		}
		if (IsAt55Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/ui_bar_55.png"), this.leftPos + -52, this.topPos + 162, 0, 0, 80, 12, 80, 12);
		}
		if (IsAt60Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/ui_bar_60.png"), this.leftPos + -52, this.topPos + 162, 0, 0, 80, 12, 80, 12);
		}
		if (IsAt65Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/ui_bar_65.png"), this.leftPos + -52, this.topPos + 162, 0, 0, 80, 12, 80, 12);
		}
		if (IsAt70Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/ui_bar_70.png"), this.leftPos + -52, this.topPos + 162, 0, 0, 80, 12, 80, 12);
		}
		if (IsAt75Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/ui_bar_75.png"), this.leftPos + -52, this.topPos + 162, 0, 0, 80, 12, 80, 12);
		}
		if (IsAt80Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/ui_bar_80.png"), this.leftPos + -52, this.topPos + 162, 0, 0, 80, 12, 80, 12);
		}
		if (IsAt85Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/ui_bar_85.png"), this.leftPos + -52, this.topPos + 162, 0, 0, 80, 12, 80, 12);
		}
		if (IsAt90Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/ui_bar_90.png"), this.leftPos + -52, this.topPos + 162, 0, 0, 80, 12, 80, 12);
		}
		if (IsAt95Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/ui_bar_95.png"), this.leftPos + -52, this.topPos + 162, 0, 0, 80, 12, 80, 12);
		}
		if (IsAt99Procedure.execute(entity)) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/ui_bar_99.png"), this.leftPos + -52, this.topPos + 162, 0, 0, 80, 12, 80, 12);
		}
		if (DisplayLogicLockAttribute1Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/bg_attributes_locked.png"), this.leftPos + 49, this.topPos + 17, 0, 0, 97, 30, 97, 30);
		}
		if (DisplayLogicLockAttribute2Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/bg_attributes_locked.png"), this.leftPos + 171, this.topPos + 17, 0, 0, 97, 30, 97, 30);
		}
		if (DisplayLogicLockAttribute3Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/bg_attributes_locked.png"), this.leftPos + 49, this.topPos + 48, 0, 0, 97, 30, 97, 30);
		}
		if (DisplayLogicLockAttribute4Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/bg_attributes_locked.png"), this.leftPos + 171, this.topPos + 49, 0, 0, 97, 30, 97, 30);
		}
		if (DisplayLogicLockAttribute5Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/bg_attributes_locked.png"), this.leftPos + 48, this.topPos + 79, 0, 0, 97, 30, 97, 30);
		}
		if (DisplayLogicLockAttribute6Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/bg_attributes_locked.png"), this.leftPos + 171, this.topPos + 79, 0, 0, 97, 30, 97, 30);
		}
		if (DisplayLogicLockAttribute7Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/bg_attributes_locked.png"), this.leftPos + 49, this.topPos + 112, 0, 0, 97, 30, 97, 30);
		}
		if (DisplayLogicLockAttribute8Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/bg_attributes_locked.png"), this.leftPos + 171, this.topPos + 112, 0, 0, 97, 30, 97, 30);
		}
		if (DisplayLogicLockAttribute9Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/bg_attributes_locked.png"), this.leftPos + 49, this.topPos + 143, 0, 0, 97, 30, 97, 30);
		}
		if (DisplayLogicLockAttribute10Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/bg_attributes_locked.png"), this.leftPos + 171, this.topPos + 143, 0, 0, 97, 30, 97, 30);
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
		guiGraphics.drawString(this.font, Component.translatable("gui.memory_of_the_past.player_stats_gui.label_level"), -41, 147, -1, false);
		guiGraphics.drawString(this.font, ReturnCurrentLevelProcedure.execute(entity), -1, 147, -1, false);
		guiGraphics.drawString(this.font, Component.translatable("gui.memory_of_the_past.player_stats_gui.label_available_points"), 79, -8, -1, false);
		guiGraphics.drawString(this.font, ReturnExtraPointsProcedure.execute(entity), 169, -8, -1, false);
		guiGraphics.drawString(this.font, PlayerNameProcedure.execute(entity), -28, -2, -1, false);
		if (DisplayLogicAttribute1Procedure.execute())
			guiGraphics.drawString(this.font, ReturnCurrentAttributeOneProcedure.execute(entity), 63, 29, -1, false);
		if (DisplayLogicAttribute2Procedure.execute())
			guiGraphics.drawString(this.font, ReturnCurrentAttributeTwoProcedure.execute(entity), 186, 28, -1, false);
		if (DisplayLogicAttribute3Procedure.execute())
			guiGraphics.drawString(this.font, ReturnCurrentAttributeThreeProcedure.execute(entity), 63, 59, -1, false);
		if (DisplayLogicAttribute4Procedure.execute())
			guiGraphics.drawString(this.font, ReturnCurrentAttributeForthProcedure.execute(entity), 186, 60, -1, false);
		if (DisplayLogicAttribute5Procedure.execute())
			guiGraphics.drawString(this.font, ReturnCurrentAttributeFifthProcedure.execute(entity), 63, 91, -1, false);
		if (DisplayLogicAttribute6Procedure.execute())
			guiGraphics.drawString(this.font, ReturnCurrentAttributeSixthProcedure.execute(entity), 186, 91, -1, false);
		if (DisplayLogicAttribute7Procedure.execute())
			guiGraphics.drawString(this.font, ReturnCurrentAttributeSeventhProcedure.execute(entity), 63, 123, -1, false);
		if (DisplayLogicAttribute8Procedure.execute())
			guiGraphics.drawString(this.font, ReturnCurrentAttributeEightProcedure.execute(entity), 185, 124, -1, false);
		if (DisplayLogicAttribute9Procedure.execute())
			guiGraphics.drawString(this.font, ReturnCurrentAttributeNineProcedure.execute(entity), 63, 155, -1, false);
		if (DisplayLogicAttribute10Procedure.execute())
			guiGraphics.drawString(this.font, ReturnCurrentAttributeTenProcedure.execute(entity), 185, 156, -1, false);
		guiGraphics.drawString(this.font, ReturnCurrentModifierProcedure.execute(entity), 248, 4, -1, false);
		guiGraphics.drawString(this.font, CurrentXpToLevelProcedure.execute(entity), -50, 165, -16777216, false);
	}

	@Override
	public void init() {
		super.init();
		imagebutton_button_notclicked = new ImageButton(this.leftPos + 127, this.topPos + 26, 16, 14, 0, 0, 14, ResourceLocation.parse("memory_of_the_past:textures/screens/atlas/imagebutton_button_notclicked.png"), 16, 28, e -> {
			int x = PlayerStatsGUIScreen.this.x;
			int y = PlayerStatsGUIScreen.this.y;
			if (DisplayLogicAttribute1Procedure.execute()) {
				MemoryOfThePastMod.PACKET_HANDLER.sendToServer(new PlayerStatsGUIButtonMessage(0, x, y, z));
				PlayerStatsGUIButtonMessage.handleButtonAction(entity, 0, x, y, z);
			}
		});
		this.addRenderableWidget(imagebutton_button_notclicked);
		imagebutton_button_notclicked1 = new ImageButton(this.leftPos + 247, this.topPos + 25, 16, 14, 0, 0, 14, ResourceLocation.parse("memory_of_the_past:textures/screens/atlas/imagebutton_button_notclicked1.png"), 16, 28, e -> {
			int x = PlayerStatsGUIScreen.this.x;
			int y = PlayerStatsGUIScreen.this.y;
			if (DisplayLogicAttribute2Procedure.execute()) {
				MemoryOfThePastMod.PACKET_HANDLER.sendToServer(new PlayerStatsGUIButtonMessage(1, x, y, z));
				PlayerStatsGUIButtonMessage.handleButtonAction(entity, 1, x, y, z);
			}
		});
		this.addRenderableWidget(imagebutton_button_notclicked1);
		imagebutton_button_notclicked2 = new ImageButton(this.leftPos + 127, this.topPos + 57, 16, 14, 0, 0, 14, ResourceLocation.parse("memory_of_the_past:textures/screens/atlas/imagebutton_button_notclicked2.png"), 16, 28, e -> {
			int x = PlayerStatsGUIScreen.this.x;
			int y = PlayerStatsGUIScreen.this.y;
			if (DisplayLogicAttribute3Procedure.execute()) {
				MemoryOfThePastMod.PACKET_HANDLER.sendToServer(new PlayerStatsGUIButtonMessage(2, x, y, z));
				PlayerStatsGUIButtonMessage.handleButtonAction(entity, 2, x, y, z);
			}
		});
		this.addRenderableWidget(imagebutton_button_notclicked2);
		imagebutton_button_notclicked3 = new ImageButton(this.leftPos + 247, this.topPos + 57, 16, 14, 0, 0, 14, ResourceLocation.parse("memory_of_the_past:textures/screens/atlas/imagebutton_button_notclicked3.png"), 16, 28, e -> {
			int x = PlayerStatsGUIScreen.this.x;
			int y = PlayerStatsGUIScreen.this.y;
			if (DisplayLogicAttribute4Procedure.execute()) {
				MemoryOfThePastMod.PACKET_HANDLER.sendToServer(new PlayerStatsGUIButtonMessage(3, x, y, z));
				PlayerStatsGUIButtonMessage.handleButtonAction(entity, 3, x, y, z);
			}
		});
		this.addRenderableWidget(imagebutton_button_notclicked3);
		imagebutton_button_notclicked4 = new ImageButton(this.leftPos + 127, this.topPos + 88, 16, 14, 0, 0, 14, ResourceLocation.parse("memory_of_the_past:textures/screens/atlas/imagebutton_button_notclicked4.png"), 16, 28, e -> {
			int x = PlayerStatsGUIScreen.this.x;
			int y = PlayerStatsGUIScreen.this.y;
			if (DisplayLogicAttribute5Procedure.execute()) {
				MemoryOfThePastMod.PACKET_HANDLER.sendToServer(new PlayerStatsGUIButtonMessage(4, x, y, z));
				PlayerStatsGUIButtonMessage.handleButtonAction(entity, 4, x, y, z);
			}
		});
		this.addRenderableWidget(imagebutton_button_notclicked4);
		imagebutton_button_notclicked5 = new ImageButton(this.leftPos + 247, this.topPos + 88, 16, 14, 0, 0, 14, ResourceLocation.parse("memory_of_the_past:textures/screens/atlas/imagebutton_button_notclicked5.png"), 16, 28, e -> {
			int x = PlayerStatsGUIScreen.this.x;
			int y = PlayerStatsGUIScreen.this.y;
			if (DisplayLogicAttribute6Procedure.execute()) {
				MemoryOfThePastMod.PACKET_HANDLER.sendToServer(new PlayerStatsGUIButtonMessage(5, x, y, z));
				PlayerStatsGUIButtonMessage.handleButtonAction(entity, 5, x, y, z);
			}
		});
		this.addRenderableWidget(imagebutton_button_notclicked5);
		imagebutton_button_notclicked6 = new ImageButton(this.leftPos + 127, this.topPos + 120, 16, 14, 0, 0, 14, ResourceLocation.parse("memory_of_the_past:textures/screens/atlas/imagebutton_button_notclicked6.png"), 16, 28, e -> {
			int x = PlayerStatsGUIScreen.this.x;
			int y = PlayerStatsGUIScreen.this.y;
			if (DisplayLogicAttribute7Procedure.execute()) {
				MemoryOfThePastMod.PACKET_HANDLER.sendToServer(new PlayerStatsGUIButtonMessage(6, x, y, z));
				PlayerStatsGUIButtonMessage.handleButtonAction(entity, 6, x, y, z);
			}
		});
		this.addRenderableWidget(imagebutton_button_notclicked6);
		imagebutton_button_notclicked7 = new ImageButton(this.leftPos + 247, this.topPos + 120, 16, 14, 0, 0, 14, ResourceLocation.parse("memory_of_the_past:textures/screens/atlas/imagebutton_button_notclicked7.png"), 16, 28, e -> {
			int x = PlayerStatsGUIScreen.this.x;
			int y = PlayerStatsGUIScreen.this.y;
			if (DisplayLogicAttribute8Procedure.execute()) {
				MemoryOfThePastMod.PACKET_HANDLER.sendToServer(new PlayerStatsGUIButtonMessage(7, x, y, z));
				PlayerStatsGUIButtonMessage.handleButtonAction(entity, 7, x, y, z);
			}
		});
		this.addRenderableWidget(imagebutton_button_notclicked7);
		imagebutton_button_notclicked9 = new ImageButton(this.leftPos + 247, this.topPos + 151, 16, 14, 0, 0, 14, ResourceLocation.parse("memory_of_the_past:textures/screens/atlas/imagebutton_button_notclicked9.png"), 16, 28, e -> {
			int x = PlayerStatsGUIScreen.this.x;
			int y = PlayerStatsGUIScreen.this.y;
			if (DisplayLogicAttribute10Procedure.execute()) {
				MemoryOfThePastMod.PACKET_HANDLER.sendToServer(new PlayerStatsGUIButtonMessage(8, x, y, z));
				PlayerStatsGUIButtonMessage.handleButtonAction(entity, 8, x, y, z);
			}
		});
		this.addRenderableWidget(imagebutton_button_notclicked9);
		imagebutton_button_for_stats = new ImageButton(this.leftPos + -46, this.topPos + -3, 13, 13, 0, 0, 13, ResourceLocation.parse("memory_of_the_past:textures/screens/atlas/imagebutton_button_for_stats.png"), 13, 26, e -> {
			int x = PlayerStatsGUIScreen.this.x;
			int y = PlayerStatsGUIScreen.this.y;
			if (ReturnGlobalSectionsDisplayProcedure.execute()) {
				MemoryOfThePastMod.PACKET_HANDLER.sendToServer(new PlayerStatsGUIButtonMessage(9, x, y, z));
				PlayerStatsGUIButtonMessage.handleButtonAction(entity, 9, x, y, z);
			}
		});
		this.addRenderableWidget(imagebutton_button_for_stats);
		imagebutton_button_left = new ImageButton(this.leftPos + 240, this.topPos + 4, 6, 8, 0, 0, 8, ResourceLocation.parse("memory_of_the_past:textures/screens/atlas/imagebutton_button_left.png"), 6, 16, e -> {
			int x = PlayerStatsGUIScreen.this.x;
			int y = PlayerStatsGUIScreen.this.y;
			if (true) {
				MemoryOfThePastMod.PACKET_HANDLER.sendToServer(new PlayerStatsGUIButtonMessage(10, x, y, z));
				PlayerStatsGUIButtonMessage.handleButtonAction(entity, 10, x, y, z);
			}
		});
		this.addRenderableWidget(imagebutton_button_left);
		imagebutton_button_right = new ImageButton(this.leftPos + 261, this.topPos + 4, 6, 8, 0, 0, 8, ResourceLocation.parse("memory_of_the_past:textures/screens/atlas/imagebutton_button_right.png"), 6, 16, e -> {
			int x = PlayerStatsGUIScreen.this.x;
			int y = PlayerStatsGUIScreen.this.y;
			if (true) {
				MemoryOfThePastMod.PACKET_HANDLER.sendToServer(new PlayerStatsGUIButtonMessage(11, x, y, z));
				PlayerStatsGUIButtonMessage.handleButtonAction(entity, 11, x, y, z);
			}
		});
		this.addRenderableWidget(imagebutton_button_right);
		imagebutton_button_notclicked8 = new ImageButton(this.leftPos + 127, this.topPos + 152, 16, 14, 0, 0, 14, ResourceLocation.parse("memory_of_the_past:textures/screens/atlas/imagebutton_button_notclicked8.png"), 16, 28, e -> {
			int x = PlayerStatsGUIScreen.this.x;
			int y = PlayerStatsGUIScreen.this.y;
			if (DisplayLogicAttribute9Procedure.execute()) {
				MemoryOfThePastMod.PACKET_HANDLER.sendToServer(new PlayerStatsGUIButtonMessage(12, x, y, z));
				PlayerStatsGUIButtonMessage.handleButtonAction(entity, 12, x, y, z);
			}
		});
		this.addRenderableWidget(imagebutton_button_notclicked8);
	}
}