package tn.nightbeam.ras;

import net.fabricmc.api.ModInitializer;
import tn.nightbeam.ras.init.RpgAttributeSystemModAttributes;
import tn.nightbeam.ras.init.RpgAttributeSystemModItems;
import tn.nightbeam.ras.network.*;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import tn.nightbeam.ras.command.RpgAttributeSystemModCommands;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.NbtOps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;

public class RpgAttributeSystemModFabric implements ModInitializer {

    // Public static field for the attachment
    public static final AttachmentType<PlayerVariables> PLAYER_VARIABLES = AttachmentRegistry.createPersistent(
            ResourceLocation.fromNamespaceAndPath(RpgAttributeSystemMod.MOD_ID, "player_variables"),
            Codec.PASSTHROUGH.xmap(
                    dynamic -> {
                        // Convert dynamic to NBT Tag
                        Tag tag = (Tag) dynamic.convert(NbtOps.INSTANCE).getValue();
                        PlayerVariables vars = new PlayerVariables();
                        vars.readNBT(tag);
                        return vars;
                    },
                    vars -> new Dynamic<>(NbtOps.INSTANCE, vars.writeNBT())));

    @Override
    public void onInitialize() {
        RpgAttributeSystemMod.init();

        RpgAttributeSystemModAttributes.register((name, attribute) -> Registry.register(BuiltInRegistries.ATTRIBUTE,
                ResourceLocation.fromNamespaceAndPath(RpgAttributeSystemMod.MOD_ID, name), attribute));

        RpgAttributeSystemModItems.register((name, item) -> Registry.register(BuiltInRegistries.ITEM,
                ResourceLocation.fromNamespaceAndPath(RpgAttributeSystemMod.MOD_ID, name), item.get()));

        // Creative Tab Registration
        net.minecraft.world.item.CreativeModeTab tab = net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup.builder()
                .title(net.minecraft.network.chat.Component
                        .translatable("item_group." + RpgAttributeSystemMod.MOD_ID + ".rpg_attribute_system"))
                .icon(() -> new net.minecraft.world.item.ItemStack(
                        tn.nightbeam.ras.init.RpgAttributeSystemModItems.TOME_OF_ASCENSION.get()))
                .build();

        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
                ResourceLocation.fromNamespaceAndPath(RpgAttributeSystemMod.MOD_ID, "rpg_attribute_system"), tab);

        net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
                .modifyEntriesEvent(net.minecraft.resources.ResourceKey.create(
                        net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB,
                        ResourceLocation.fromNamespaceAndPath(RpgAttributeSystemMod.MOD_ID, "rpg_attribute_system")))
                .register(content -> {
                    content.accept(tn.nightbeam.ras.init.RpgAttributeSystemModItems.TOME_OF_ASCENSION.get());
                    content.accept(tn.nightbeam.ras.init.RpgAttributeSystemModItems.SCROLL_OF_REBIRTH.get());
                });

        tn.nightbeam.ras.events.FabricRpgAttributeSystemModEvents.register();
        tn.nightbeam.ras.init.RpgAttributeSystemModMenusFabric.register();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            RpgAttributeSystemModCommands.register(dispatcher);
        });

        // Register payload types for Fabric 1.21.x networking
        PayloadTypeRegistry.playC2S().register(FabricMenuStateUpdatePayload.TYPE, FabricMenuStateUpdatePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(FabricButtonActionPayload.TYPE, FabricButtonActionPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(FabricOpenStatsMenuPayload.TYPE, FabricOpenStatsMenuPayload.CODEC);

        PayloadTypeRegistry.playS2C().register(FabricMenuStateUpdatePayload.TYPE, FabricMenuStateUpdatePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(FabricSyncVarsPayload.TYPE, FabricSyncVarsPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(FabricSyncConfigPayload.TYPE, FabricSyncConfigPayload.CODEC);

        // Server Network Receivers
        ServerPlayNetworking.registerGlobalReceiver(FabricMenuStateUpdatePayload.TYPE,
                (payload, context) -> {
                    context.server().execute(() -> {
                        if (context.player().containerMenu instanceof tn.nightbeam.ras.init.MenuAccessor menu) {
                            menu.getMenuState().put(payload.elementType() + ":" + payload.name(),
                                    payload.elementState());
                        }
                    });
                });

        ServerPlayNetworking.registerGlobalReceiver(FabricButtonActionPayload.TYPE,
                (payload, context) -> {
                    context.server().execute(() -> {
                        GenericButtonActionPacket.handleAction(context.player(), payload.buttonID(),
                                payload.x(), payload.y(), payload.z());
                    });
                });

        ServerPlayNetworking.registerGlobalReceiver(FabricOpenStatsMenuPayload.TYPE,
                (payload, context) -> {
                    context.server().execute(() -> OpenStatsMenuPacket.handle(context.player()));
                });
    }
}
