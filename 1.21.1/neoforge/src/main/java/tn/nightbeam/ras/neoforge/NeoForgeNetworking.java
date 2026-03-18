package tn.nightbeam.ras.neoforge;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import tn.nightbeam.ras.RpgAttributeSystemMod;
import tn.nightbeam.ras.config.AttributeData;
import tn.nightbeam.ras.network.PlayerVariables;
import tn.nightbeam.ras.platform.Services;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.List;

public class NeoForgeNetworking {
    public static final String PROTOCOL_VERSION = "1";

    // Payload types
    public static final ResourceLocation SYNC_VARS_ID = ResourceLocation
            .fromNamespaceAndPath(RpgAttributeSystemMod.MOD_ID, "sync_vars");
    public static final ResourceLocation SYNC_CONFIG_ID = ResourceLocation
            .fromNamespaceAndPath(RpgAttributeSystemMod.MOD_ID, "sync_config");
    public static final ResourceLocation BUTTON_ACTION_ID = ResourceLocation
            .fromNamespaceAndPath(RpgAttributeSystemMod.MOD_ID, "button_action");
    public static final ResourceLocation MENU_UPDATE_ID = ResourceLocation
            .fromNamespaceAndPath(RpgAttributeSystemMod.MOD_ID, "menu_update");
    public static final ResourceLocation OPEN_STATS_ID = ResourceLocation
            .fromNamespaceAndPath(RpgAttributeSystemMod.MOD_ID, "open_stats");

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(RpgAttributeSystemMod.MOD_ID).versioned(PROTOCOL_VERSION);

        // Server -> Client
        registrar.playToClient(SyncVarsPayload.TYPE, SyncVarsPayload.CODEC, NeoForgeNetworking::handleSyncVars);
        registrar.playToClient(SyncConfigPayload.TYPE, SyncConfigPayload.CODEC, NeoForgeNetworking::handleSyncConfig);

        // Bidirectional (both directions)
        registrar.playBidirectional(MenuUpdatePayload.TYPE, MenuUpdatePayload.CODEC,
                NeoForgeNetworking::handleMenuUpdate);

        // Client -> Server
        registrar.playToServer(ButtonActionPayload.TYPE, ButtonActionPayload.CODEC,
                NeoForgeNetworking::handleButtonAction);
        registrar.playToServer(OpenStatsPayload.TYPE, OpenStatsPayload.CODEC, NeoForgeNetworking::handleOpenStats);
    }

    // ==================== Payload Records ====================

    public record SyncVarsPayload(CompoundTag vars) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<SyncVarsPayload> TYPE = new CustomPacketPayload.Type<>(
                SYNC_VARS_ID);
        public static final StreamCodec<FriendlyByteBuf, SyncVarsPayload> CODEC = StreamCodec.of(
                (buf, payload) -> buf.writeNbt(payload.vars),
                buf -> new SyncVarsPayload(buf.readNbt()));

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record SyncConfigPayload(List<AttributeData> attributes) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<SyncConfigPayload> TYPE = new CustomPacketPayload.Type<>(
                SYNC_CONFIG_ID);
        public static final StreamCodec<FriendlyByteBuf, SyncConfigPayload> CODEC = StreamCodec.of(
                SyncConfigPayload::encode,
                SyncConfigPayload::decode);

        private static void encode(FriendlyByteBuf buf, SyncConfigPayload payload) {
            buf.writeInt(payload.attributes.size());
            for (AttributeData data : payload.attributes) {
                buf.writeInt(data.attributeId);
                buf.writeDouble(data.baseIncrement);
                buf.writeDouble(data.maxLevel);
                buf.writeBoolean(data.isLocked);
                buf.writeUtf(data.iconPath != null ? data.iconPath : "");
                buf.writeUtf(data.displayName != null ? data.displayName : "");
            }
        }

        private static SyncConfigPayload decode(FriendlyByteBuf buf) {
            int size = buf.readInt();
            List<AttributeData> attributes = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                int attributeId = buf.readInt();
                double baseIncrement = buf.readDouble();
                double maxLevel = buf.readDouble();
                boolean isLocked = buf.readBoolean();
                String iconPath = buf.readUtf();
                String displayName = buf.readUtf();
                attributes
                        .add(new AttributeData(attributeId, baseIncrement, maxLevel, isLocked, iconPath, displayName));
            }
            return new SyncConfigPayload(attributes);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ButtonActionPayload(int buttonID, int x, int y, int z) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<ButtonActionPayload> TYPE = new CustomPacketPayload.Type<>(
                BUTTON_ACTION_ID);
        public static final StreamCodec<FriendlyByteBuf, ButtonActionPayload> CODEC = StreamCodec.of(
                (buf, payload) -> {
                    buf.writeInt(payload.buttonID);
                    buf.writeInt(payload.x);
                    buf.writeInt(payload.y);
                    buf.writeInt(payload.z);
                },
                buf -> new ButtonActionPayload(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt()));

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record MenuUpdatePayload(int elementType, String name, Object elementState) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<MenuUpdatePayload> TYPE = new CustomPacketPayload.Type<>(
                MENU_UPDATE_ID);
        public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, MenuUpdatePayload> CODEC = StreamCodec
                .of(
                        MenuUpdatePayload::encode,
                        MenuUpdatePayload::decode);

        private static void encode(net.minecraft.network.RegistryFriendlyByteBuf buf, MenuUpdatePayload payload) {
            buf.writeInt(payload.elementType);
            buf.writeUtf(payload.name);
            if (payload.elementType == 0) {
                buf.writeUtf((String) payload.elementState);
            } else if (payload.elementType == 1) {
                buf.writeBoolean((Boolean) payload.elementState);
            }
        }

        private static MenuUpdatePayload decode(net.minecraft.network.RegistryFriendlyByteBuf buf) {
            int elementType = buf.readInt();
            String name = buf.readUtf();
            Object state = null;
            if (elementType == 0) {
                state = buf.readUtf();
            } else if (elementType == 1) {
                state = buf.readBoolean();
            }
            return new MenuUpdatePayload(elementType, name, state);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record OpenStatsPayload() implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<OpenStatsPayload> TYPE = new CustomPacketPayload.Type<>(
                OPEN_STATS_ID);
        public static final StreamCodec<FriendlyByteBuf, OpenStatsPayload> CODEC = StreamCodec.of(
                (buf, payload) -> {
                },
                buf -> new OpenStatsPayload());

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    // ==================== Handlers ====================

    private static void handleSyncVars(SyncVarsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() != null) {
                PlayerVariables vars = Services.PLATFORM.getPlayerVariables(context.player());
                vars.readNBT(payload.vars);
            }
        });
    }

    private static void handleSyncConfig(SyncConfigPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            tn.nightbeam.ras.util.AttributeManager.setClientCache(payload.attributes);
        });
    }

    private static void handleButtonAction(ButtonActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                tn.nightbeam.ras.network.GenericButtonActionPacket.handleAction(
                        serverPlayer, payload.buttonID, payload.x, payload.y, payload.z);
            }
        });
    }

    private static void handleMenuUpdate(MenuUpdatePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            // Server-side handling
            if (context.player() instanceof ServerPlayer serverPlayer) {
                if (serverPlayer.containerMenu instanceof tn.nightbeam.ras.init.MenuAccessor menu) {
                    menu.getMenuState().put(payload.elementType + ":" + payload.name, payload.elementState);
                }
            } else {
                // Client-side handling
                if (net.minecraft.client.Minecraft
                        .getInstance().screen instanceof tn.nightbeam.ras.init.ScreenAccessor accessor) {
                    accessor.updateMenuState(payload.elementType, payload.name, payload.elementState);
                }
            }
        });
    }

    private static void handleOpenStats(OpenStatsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                tn.nightbeam.ras.network.OpenStatsMenuPacket.handle(serverPlayer);
            }
        });
    }
}
