package tn.mbs.memory.client.gui;

import tn.mbs.memory.world.inventory.PlayerAttributesViewerGUIMenu;
import tn.mbs.memory.procedures.ReturnSection9DisplayProcedure;
import tn.mbs.memory.procedures.ReturnSection8DisplayProcedure;
import tn.mbs.memory.procedures.ReturnSection7DisplayProcedure;
import tn.mbs.memory.procedures.ReturnSection6DisplayProcedure;
import tn.mbs.memory.procedures.ReturnSection5DisplayProcedure;
import tn.mbs.memory.procedures.ReturnSection4DisplayProcedure;
import tn.mbs.memory.procedures.ReturnSection3DisplayProcedure;
import tn.mbs.memory.procedures.ReturnSection2DisplayProcedure;
import tn.mbs.memory.procedures.ReturnSection1DisplayProcedure;
import tn.mbs.memory.procedures.ReturnSection15DisplayProcedure;
import tn.mbs.memory.procedures.ReturnSection14DisplayProcedure;
import tn.mbs.memory.procedures.ReturnSection13DisplayProcedure;
import tn.mbs.memory.procedures.ReturnSection12DisplayProcedure;
import tn.mbs.memory.procedures.ReturnSection11DisplayProcedure;
import tn.mbs.memory.procedures.ReturnSection10DisplayProcedure;
import tn.mbs.memory.procedures.ReturnExtraPointsProcedure;
import tn.mbs.memory.procedures.ReturnDisplaySection9Procedure;
import tn.mbs.memory.procedures.ReturnDisplaySection8Procedure;
import tn.mbs.memory.procedures.ReturnDisplaySection7Procedure;
import tn.mbs.memory.procedures.ReturnDisplaySection6Procedure;
import tn.mbs.memory.procedures.ReturnDisplaySection5Procedure;
import tn.mbs.memory.procedures.ReturnDisplaySection4Procedure;
import tn.mbs.memory.procedures.ReturnDisplaySection3Procedure;
import tn.mbs.memory.procedures.ReturnDisplaySection2Procedure;
import tn.mbs.memory.procedures.ReturnDisplaySection1Procedure;
import tn.mbs.memory.procedures.ReturnDisplaySection15Procedure;
import tn.mbs.memory.procedures.ReturnDisplaySection14Procedure;
import tn.mbs.memory.procedures.ReturnDisplaySection13Procedure;
import tn.mbs.memory.procedures.ReturnDisplaySection12Procedure;
import tn.mbs.memory.procedures.ReturnDisplaySection11Procedure;
import tn.mbs.memory.procedures.ReturnDisplaySection10Procedure;
import tn.mbs.memory.procedures.ReturnCurrentLevelProcedure;
import tn.mbs.memory.network.PlayerAttributesViewerGUIButtonMessage;
import tn.mbs.memory.init.MemoryOfThePastModScreens;
import tn.mbs.memory.MemoryOfThePastMod;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.GuiGraphics;

import com.mojang.blaze3d.systems.RenderSystem;

public class PlayerAttributesViewerGUIScreen extends AbstractContainerScreen<PlayerAttributesViewerGUIMenu> implements MemoryOfThePastModScreens.ScreenAccessor {
	private final Level world;
	private final int x, y, z;
	private final Player entity;
	private boolean menuStateUpdateActive = false;
	ImageButton imagebutton_button_for_stats;

	public PlayerAttributesViewerGUIScreen(PlayerAttributesViewerGUIMenu container, Inventory inventory, Component text) {
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
		if (mouseX > leftPos + 1 && mouseX < leftPos + 40 && mouseY > topPos + -3 && mouseY < topPos + 8) {
			guiGraphics.renderTooltip(font, Component.translatable("gui.memory_of_the_past.player_attributes_viewer_gui.tooltip_represents_your_overall_progress"), mouseX, mouseY);
			customTooltipShown = true;
		}
		if (mouseX > leftPos + 96 && mouseX < leftPos + 185 && mouseY > topPos + -3 && mouseY < topPos + 8) {
			guiGraphics.renderTooltip(font, Component.translatable("gui.memory_of_the_past.player_attributes_viewer_gui.tooltip_indicates_the_number_of_points_y"), mouseX, mouseY);
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
		if (ReturnDisplaySection1Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/bg_attributes.png"), this.leftPos + -36, this.topPos + 14, 0, 0, 97, 30, 97, 30);
		}
		if (ReturnDisplaySection2Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/bg_attributes.png"), this.leftPos + 71, this.topPos + 14, 0, 0, 97, 30, 97, 30);
		}
		if (ReturnDisplaySection3Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/bg_attributes.png"), this.leftPos + 179, this.topPos + 14, 0, 0, 97, 30, 97, 30);
		}
		if (ReturnDisplaySection4Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/bg_attributes.png"), this.leftPos + -36, this.topPos + 47, 0, 0, 97, 30, 97, 30);
		}
		if (ReturnDisplaySection5Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/bg_attributes.png"), this.leftPos + 71, this.topPos + 47, 0, 0, 97, 30, 97, 30);
		}
		if (ReturnDisplaySection6Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/bg_attributes.png"), this.leftPos + 179, this.topPos + 47, 0, 0, 97, 30, 97, 30);
		}
		if (ReturnDisplaySection7Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/bg_attributes.png"), this.leftPos + -36, this.topPos + 79, 0, 0, 97, 30, 97, 30);
		}
		if (ReturnDisplaySection8Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/bg_attributes.png"), this.leftPos + 71, this.topPos + 79, 0, 0, 97, 30, 97, 30);
		}
		if (ReturnDisplaySection9Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/bg_attributes.png"), this.leftPos + 179, this.topPos + 79, 0, 0, 97, 30, 97, 30);
		}
		if (ReturnDisplaySection10Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/bg_attributes.png"), this.leftPos + -36, this.topPos + 112, 0, 0, 97, 30, 97, 30);
		}
		if (ReturnDisplaySection11Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/bg_attributes.png"), this.leftPos + 71, this.topPos + 112, 0, 0, 97, 30, 97, 30);
		}
		if (ReturnDisplaySection12Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/bg_attributes.png"), this.leftPos + 179, this.topPos + 112, 0, 0, 97, 30, 97, 30);
		}
		if (ReturnDisplaySection13Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/bg_attributes.png"), this.leftPos + -36, this.topPos + 144, 0, 0, 97, 30, 97, 30);
		}
		if (ReturnDisplaySection14Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/bg_attributes.png"), this.leftPos + 71, this.topPos + 144, 0, 0, 97, 30, 97, 30);
		}
		if (ReturnDisplaySection15Procedure.execute()) {
			guiGraphics.blit(ResourceLocation.parse("memory_of_the_past:textures/screens/bg_attributes.png"), this.leftPos + 179, this.topPos + 144, 0, 0, 97, 30, 97, 30);
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
		guiGraphics.drawString(this.font, Component.translatable("gui.memory_of_the_past.player_attributes_viewer_gui.label_level"), 1, -3, -1, false);
		guiGraphics.drawString(this.font, ReturnCurrentLevelProcedure.execute(entity), 39, -3, -1, false);
		guiGraphics.drawString(this.font, Component.translatable("gui.memory_of_the_past.player_attributes_viewer_gui.label_available_points"), 99, -3, -1, false);
		guiGraphics.drawString(this.font, ReturnExtraPointsProcedure.execute(entity), 189, -3, -1, false);
		if (ReturnDisplaySection1Procedure.execute())
			guiGraphics.drawString(this.font, ReturnSection1DisplayProcedure.execute(entity), -29, 24, -1, false);
		if (ReturnDisplaySection2Procedure.execute())
			guiGraphics.drawString(this.font, ReturnSection2DisplayProcedure.execute(entity), 79, 24, -1, false);
		if (ReturnDisplaySection3Procedure.execute())
			guiGraphics.drawString(this.font, ReturnSection3DisplayProcedure.execute(entity), 186, 24, -1, false);
		if (ReturnDisplaySection4Procedure.execute())
			guiGraphics.drawString(this.font, ReturnSection4DisplayProcedure.execute(entity), -29, 57, -1, false);
		if (ReturnDisplaySection5Procedure.execute())
			guiGraphics.drawString(this.font, ReturnSection5DisplayProcedure.execute(entity), 79, 57, -1, false);
		if (ReturnDisplaySection6Procedure.execute())
			guiGraphics.drawString(this.font, ReturnSection6DisplayProcedure.execute(entity), 186, 57, -1, false);
		if (ReturnDisplaySection7Procedure.execute())
			guiGraphics.drawString(this.font, ReturnSection7DisplayProcedure.execute(entity), -29, 89, -1, false);
		if (ReturnDisplaySection8Procedure.execute())
			guiGraphics.drawString(this.font, ReturnSection8DisplayProcedure.execute(entity), 79, 89, -1, false);
		if (ReturnDisplaySection9Procedure.execute())
			guiGraphics.drawString(this.font, ReturnSection9DisplayProcedure.execute(entity), 186, 89, -1, false);
		if (ReturnDisplaySection10Procedure.execute())
			guiGraphics.drawString(this.font, ReturnSection10DisplayProcedure.execute(entity), -29, 122, -1, false);
		if (ReturnDisplaySection11Procedure.execute())
			guiGraphics.drawString(this.font, ReturnSection11DisplayProcedure.execute(entity), 79, 122, -1, false);
		if (ReturnDisplaySection12Procedure.execute())
			guiGraphics.drawString(this.font, ReturnSection12DisplayProcedure.execute(entity), 186, 122, -1, false);
		if (ReturnDisplaySection13Procedure.execute())
			guiGraphics.drawString(this.font, ReturnSection13DisplayProcedure.execute(entity), -29, 154, -1, false);
		if (ReturnDisplaySection14Procedure.execute())
			guiGraphics.drawString(this.font, ReturnSection14DisplayProcedure.execute(entity), 79, 154, -1, false);
		if (ReturnDisplaySection15Procedure.execute())
			guiGraphics.drawString(this.font, ReturnSection15DisplayProcedure.execute(entity), 186, 154, -1, false);
	}

	@Override
	public void init() {
		super.init();
		imagebutton_button_for_stats = new ImageButton(this.leftPos + -46, this.topPos + -3, 13, 13, 0, 0, 13, ResourceLocation.parse("memory_of_the_past:textures/screens/atlas/imagebutton_button_for_stats.png"), 13, 26, e -> {
			int x = PlayerAttributesViewerGUIScreen.this.x;
			int y = PlayerAttributesViewerGUIScreen.this.y;
			if (true) {
				MemoryOfThePastMod.PACKET_HANDLER.sendToServer(new PlayerAttributesViewerGUIButtonMessage(0, x, y, z));
				PlayerAttributesViewerGUIButtonMessage.handleButtonAction(entity, 0, x, y, z);
			}
		});
		this.addRenderableWidget(imagebutton_button_for_stats);
	}
}