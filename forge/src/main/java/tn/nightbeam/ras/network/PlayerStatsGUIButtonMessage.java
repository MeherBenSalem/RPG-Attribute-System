package tn.nightbeam.ras.network;

import tn.nightbeam.ras.procedures.AddPointsAttributeGenericProcedure;
import tn.nightbeam.ras.procedures.DisplayLogicAttributeGenericProcedure;
import net.minecraft.world.entity.Entity;
import tn.nightbeam.ras.RpgAttributeSystemModForge;

import tn.nightbeam.ras.procedures.RemoveModiferCountProcedure;
import tn.nightbeam.ras.procedures.OpenAttributesDisplayGUIProcedure;
import tn.nightbeam.ras.procedures.AddModiferCountProcedure;
import tn.nightbeam.ras.RpgAttributeSystemMod;

import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class PlayerStatsGUIButtonMessage {
	private final int buttonID, x, y, z;

	public PlayerStatsGUIButtonMessage(FriendlyByteBuf buffer) {
		this.buttonID = buffer.readInt();
		this.x = buffer.readInt();
		this.y = buffer.readInt();
		this.z = buffer.readInt();
	}

	public PlayerStatsGUIButtonMessage(int buttonID, int x, int y, int z) {
		this.buttonID = buttonID;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public static void buffer(PlayerStatsGUIButtonMessage message, FriendlyByteBuf buffer) {
		buffer.writeInt(message.buttonID);
		buffer.writeInt(message.x);
		buffer.writeInt(message.y);
		buffer.writeInt(message.z);
	}

	public static void handler(PlayerStatsGUIButtonMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> {
			Player entity = context.getSender();
			int buttonID = message.buttonID;
			int x = message.x;
			int y = message.y;
			int z = message.z;
			handleButtonAction(entity, buttonID, x, y, z);
		});
		context.setPacketHandled(true);
	}

	public static void handleButtonAction(Player entity, int buttonID, int x, int y, int z) {
		Level world = entity.level();
		// security measure to prevent arbitrary chunk generation
		if (!world.hasChunkAt(new BlockPos(x, y, z)))
			return;

		// Dynamic Attribute Upgrade (ID >= 100)
		if (buttonID >= 100) {
			int attributeId = buttonID - 100;
			// Execute generic add points
			AddPointsAttributeGenericProcedure.execute(world, entity, attributeId);
			return;
		}

		// Special Actions
		if (buttonID == 9) {
			OpenAttributesDisplayGUIProcedure.execute(world, x, y, z, entity);
		}
		if (buttonID == 10) {
			RemoveModiferCountProcedure.execute(entity);
		}
		if (buttonID == 11) {
			AddModiferCountProcedure.execute(entity);
		}
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		RpgAttributeSystemModForge.addNetworkMessage(PlayerStatsGUIButtonMessage.class,
				PlayerStatsGUIButtonMessage::buffer, PlayerStatsGUIButtonMessage::new,
				PlayerStatsGUIButtonMessage::handler);
	}
}
