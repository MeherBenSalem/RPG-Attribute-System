package tn.mbs.memory.network;

import tn.mbs.memory.MemoryOfThePastMod;

import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.Capability;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.client.Minecraft;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class MemoryOfThePastModVariables {
	public static double counter = 0;

	@SubscribeEvent
	public static void init(FMLCommonSetupEvent event) {
		MemoryOfThePastMod.addNetworkMessage(PlayerVariablesSyncMessage.class, PlayerVariablesSyncMessage::buffer, PlayerVariablesSyncMessage::new, PlayerVariablesSyncMessage::handler);
	}

	@SubscribeEvent
	public static void init(RegisterCapabilitiesEvent event) {
		event.register(PlayerVariables.class);
	}

	@Mod.EventBusSubscriber
	public static class EventBusVariableHandlers {
		@SubscribeEvent
		public static void onPlayerLoggedInSyncPlayerVariables(PlayerEvent.PlayerLoggedInEvent event) {
			if (!event.getEntity().level().isClientSide())
				((PlayerVariables) event.getEntity().getCapability(PLAYER_VARIABLES_CAPABILITY, null).orElse(new PlayerVariables())).syncPlayerVariables(event.getEntity());
		}

		@SubscribeEvent
		public static void onPlayerRespawnedSyncPlayerVariables(PlayerEvent.PlayerRespawnEvent event) {
			if (!event.getEntity().level().isClientSide())
				((PlayerVariables) event.getEntity().getCapability(PLAYER_VARIABLES_CAPABILITY, null).orElse(new PlayerVariables())).syncPlayerVariables(event.getEntity());
		}

		@SubscribeEvent
		public static void onPlayerChangedDimensionSyncPlayerVariables(PlayerEvent.PlayerChangedDimensionEvent event) {
			if (!event.getEntity().level().isClientSide())
				((PlayerVariables) event.getEntity().getCapability(PLAYER_VARIABLES_CAPABILITY, null).orElse(new PlayerVariables())).syncPlayerVariables(event.getEntity());
		}

		@SubscribeEvent
		public static void clonePlayer(PlayerEvent.Clone event) {
			event.getOriginal().revive();
			PlayerVariables original = ((PlayerVariables) event.getOriginal().getCapability(PLAYER_VARIABLES_CAPABILITY, null).orElse(new PlayerVariables()));
			PlayerVariables clone = ((PlayerVariables) event.getEntity().getCapability(PLAYER_VARIABLES_CAPABILITY, null).orElse(new PlayerVariables()));
			clone.attribute_1 = original.attribute_1;
			clone.attribute_6 = original.attribute_6;
			clone.attribute_5 = original.attribute_5;
			clone.attribute_2 = original.attribute_2;
			clone.attribute_4 = original.attribute_4;
			clone.attribute_3 = original.attribute_3;
			clone.Level = original.Level;
			clone.SparePoints = original.SparePoints;
			clone.currentXpTLevel = original.currentXpTLevel;
			clone.nextevelXp = original.nextevelXp;
			clone.attribute_7 = original.attribute_7;
			clone.attribute_8 = original.attribute_8;
			clone.attribute_9 = original.attribute_9;
			clone.attribute_10 = original.attribute_10;
			clone.modifier = original.modifier;
			if (!event.isWasDeath()) {
			}
		}
	}

	public static final Capability<PlayerVariables> PLAYER_VARIABLES_CAPABILITY = CapabilityManager.get(new CapabilityToken<PlayerVariables>() {
	});

	@Mod.EventBusSubscriber
	private static class PlayerVariablesProvider implements ICapabilitySerializable<Tag> {
		@SubscribeEvent
		public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
			if (event.getObject() instanceof Player && !(event.getObject() instanceof FakePlayer))
				event.addCapability(ResourceLocation.fromNamespaceAndPath("memory_of_the_past", "player_variables"), new PlayerVariablesProvider());
		}

		private final PlayerVariables playerVariables = new PlayerVariables();
		private final LazyOptional<PlayerVariables> instance = LazyOptional.of(() -> playerVariables);

		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
			return cap == PLAYER_VARIABLES_CAPABILITY ? instance.cast() : LazyOptional.empty();
		}

		@Override
		public Tag serializeNBT() {
			return playerVariables.writeNBT();
		}

		@Override
		public void deserializeNBT(Tag nbt) {
			playerVariables.readNBT(nbt);
		}
	}

	public static class PlayerVariables {
		public double attribute_1 = 20.0;
		public double attribute_6 = 0.0;
		public double attribute_5 = 0.1;
		public double attribute_2 = 2.0;
		public double attribute_4 = 0;
		public double attribute_3 = 4.0;
		public double Level = 0;
		public double SparePoints = 1.0;
		public double currentXpTLevel = 0.0;
		public double nextevelXp = 100.0;
		public double attribute_7 = 0.0;
		public double attribute_8 = 0;
		public double attribute_9 = 0;
		public double attribute_10 = 0;
		public double modifier = 1.0;

		public void syncPlayerVariables(Entity entity) {
			if (entity instanceof ServerPlayer serverPlayer)
				MemoryOfThePastMod.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new PlayerVariablesSyncMessage(this));
		}

		public Tag writeNBT() {
			CompoundTag nbt = new CompoundTag();
			nbt.putDouble("attribute_1", attribute_1);
			nbt.putDouble("attribute_6", attribute_6);
			nbt.putDouble("attribute_5", attribute_5);
			nbt.putDouble("attribute_2", attribute_2);
			nbt.putDouble("attribute_4", attribute_4);
			nbt.putDouble("attribute_3", attribute_3);
			nbt.putDouble("Level", Level);
			nbt.putDouble("SparePoints", SparePoints);
			nbt.putDouble("currentXpTLevel", currentXpTLevel);
			nbt.putDouble("nextevelXp", nextevelXp);
			nbt.putDouble("attribute_7", attribute_7);
			nbt.putDouble("attribute_8", attribute_8);
			nbt.putDouble("attribute_9", attribute_9);
			nbt.putDouble("attribute_10", attribute_10);
			nbt.putDouble("modifier", modifier);
			return nbt;
		}

		public void readNBT(Tag tag) {
			CompoundTag nbt = (CompoundTag) tag;
			attribute_1 = nbt.getDouble("attribute_1");
			attribute_6 = nbt.getDouble("attribute_6");
			attribute_5 = nbt.getDouble("attribute_5");
			attribute_2 = nbt.getDouble("attribute_2");
			attribute_4 = nbt.getDouble("attribute_4");
			attribute_3 = nbt.getDouble("attribute_3");
			Level = nbt.getDouble("Level");
			SparePoints = nbt.getDouble("SparePoints");
			currentXpTLevel = nbt.getDouble("currentXpTLevel");
			nextevelXp = nbt.getDouble("nextevelXp");
			attribute_7 = nbt.getDouble("attribute_7");
			attribute_8 = nbt.getDouble("attribute_8");
			attribute_9 = nbt.getDouble("attribute_9");
			attribute_10 = nbt.getDouble("attribute_10");
			modifier = nbt.getDouble("modifier");
		}
	}

	public static class PlayerVariablesSyncMessage {
		private final PlayerVariables data;

		public PlayerVariablesSyncMessage(FriendlyByteBuf buffer) {
			this.data = new PlayerVariables();
			this.data.readNBT(buffer.readNbt());
		}

		public PlayerVariablesSyncMessage(PlayerVariables data) {
			this.data = data;
		}

		public static void buffer(PlayerVariablesSyncMessage message, FriendlyByteBuf buffer) {
			buffer.writeNbt((CompoundTag) message.data.writeNBT());
		}

		public static void handler(PlayerVariablesSyncMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
			NetworkEvent.Context context = contextSupplier.get();
			context.enqueueWork(() -> {
				if (!context.getDirection().getReceptionSide().isServer()) {
					PlayerVariables variables = ((PlayerVariables) Minecraft.getInstance().player.getCapability(PLAYER_VARIABLES_CAPABILITY, null).orElse(new PlayerVariables()));
					variables.attribute_1 = message.data.attribute_1;
					variables.attribute_6 = message.data.attribute_6;
					variables.attribute_5 = message.data.attribute_5;
					variables.attribute_2 = message.data.attribute_2;
					variables.attribute_4 = message.data.attribute_4;
					variables.attribute_3 = message.data.attribute_3;
					variables.Level = message.data.Level;
					variables.SparePoints = message.data.SparePoints;
					variables.currentXpTLevel = message.data.currentXpTLevel;
					variables.nextevelXp = message.data.nextevelXp;
					variables.attribute_7 = message.data.attribute_7;
					variables.attribute_8 = message.data.attribute_8;
					variables.attribute_9 = message.data.attribute_9;
					variables.attribute_10 = message.data.attribute_10;
					variables.modifier = message.data.modifier;
				}
			});
			context.setPacketHandled(true);
		}
	}
}