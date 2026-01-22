package tn.nightbeam.ras.network;

import tn.nightbeam.ras.RpgAttributeSystemModForge;

import tn.nightbeam.ras.init.RpgAttributeSystemModScreens;
import tn.nightbeam.ras.init.MenuAccessor;
import tn.nightbeam.ras.RpgAttributeSystemMod;

import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.client.Minecraft;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class MenuStateUpdateMessage {
	private final int elementType;
	private final String name;
	private final Object elementState;

	public MenuStateUpdateMessage(FriendlyByteBuf buffer) {
		this.elementType = buffer.readInt();
		this.name = buffer.readUtf();
		Object elementState = null;
		if (elementType == 0) {
			elementState = buffer.readUtf();
		} else if (elementType == 1) {
			elementState = buffer.readBoolean();
		}
		this.elementState = elementState;
	}

	public MenuStateUpdateMessage(int elementType, String name, Object elementState) {
		this.elementType = elementType;
		this.name = name;
		this.elementState = elementState;
	}

	public static void buffer(MenuStateUpdateMessage message, FriendlyByteBuf buffer) {
		buffer.writeInt(message.elementType);
		buffer.writeUtf(message.name);
		if (message.elementType == 0) {
			buffer.writeUtf((String) message.elementState);
		} else if (message.elementType == 1) {
			buffer.writeBoolean((boolean) message.elementState);
		}
	}

	public static void handler(MenuStateUpdateMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
		if (message.name.length() > 256 || message.elementState instanceof String string && string.length() > 8192)
			return;
		NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> {
			if (context.getSender() != null && context.getSender().containerMenu instanceof MenuAccessor menu) {
				menu.getMenuState().put(message.elementType + ":" + message.name, message.elementState);
			}
			// Client side logic needs careful check. The handler runs on network thread,
			// enqueueWork runs on main thread.
			// On client, context.getDirection() gives direction (CLIENTBOUND/SERVERBOUND).

			if (!context.getDirection().getReceptionSide().isServer()) {
				// We are on client. Minecraft.getInstance() is valid here (inside enqueueWork).
				if (Minecraft.getInstance().screen instanceof tn.nightbeam.ras.init.ScreenAccessor accessor) {
					accessor.updateMenuState(message.elementType, message.name, message.elementState);
				}
			}
		});
		context.setPacketHandled(true);
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		RpgAttributeSystemModForge.addNetworkMessage(MenuStateUpdateMessage.class, MenuStateUpdateMessage::buffer,
				MenuStateUpdateMessage::new, MenuStateUpdateMessage::handler);
	}
}
