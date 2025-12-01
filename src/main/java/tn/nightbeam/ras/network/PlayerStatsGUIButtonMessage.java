package tn.nightbeam.ras.network;

import tn.nightbeam.ras.procedures.RemoveModiferCountProcedure;
import tn.nightbeam.ras.procedures.OpenAttributesDisplayGUIProcedure;
import tn.nightbeam.ras.procedures.AddPointsAttribute9Procedure;
import tn.nightbeam.ras.procedures.AddPointsAttribute8Procedure;
import tn.nightbeam.ras.procedures.AddPointsAttribute7Procedure;
import tn.nightbeam.ras.procedures.AddPointsAttribute6Procedure;
import tn.nightbeam.ras.procedures.AddPointsAttribute5Procedure;
import tn.nightbeam.ras.procedures.AddPointsAttribute4Procedure;
import tn.nightbeam.ras.procedures.AddPointsAttribute3Procedure;
import tn.nightbeam.ras.procedures.AddPointsAttribute2Procedure;
import tn.nightbeam.ras.procedures.AddPointsAttribute1Procedure;
import tn.nightbeam.ras.procedures.AddPointsAttribute10Procedure;
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
		if (buttonID == 0) {

			AddPointsAttribute1Procedure.execute(world, entity);
		}
		if (buttonID == 1) {

			AddPointsAttribute2Procedure.execute(world, entity);
		}
		if (buttonID == 2) {

			AddPointsAttribute3Procedure.execute(world, entity);
		}
		if (buttonID == 3) {

			AddPointsAttribute4Procedure.execute(world, entity);
		}
		if (buttonID == 4) {

			AddPointsAttribute5Procedure.execute(world, entity);
		}
		if (buttonID == 5) {

			AddPointsAttribute6Procedure.execute(world, entity);
		}
		if (buttonID == 6) {

			AddPointsAttribute7Procedure.execute(world, entity);
		}
		if (buttonID == 7) {

			AddPointsAttribute8Procedure.execute(world, entity);
		}
		if (buttonID == 8) {

			AddPointsAttribute10Procedure.execute(world, entity);
		}
		if (buttonID == 9) {

			OpenAttributesDisplayGUIProcedure.execute(world, x, y, z, entity);
		}
		if (buttonID == 10) {

			RemoveModiferCountProcedure.execute(entity);
		}
		if (buttonID == 11) {

			AddModiferCountProcedure.execute(entity);
		}
		if (buttonID == 12) {

			AddPointsAttribute9Procedure.execute(world, entity);
		}
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		RpgAttributeSystemMod.addNetworkMessage(PlayerStatsGUIButtonMessage.class, PlayerStatsGUIButtonMessage::buffer, PlayerStatsGUIButtonMessage::new, PlayerStatsGUIButtonMessage::handler);
	}
}