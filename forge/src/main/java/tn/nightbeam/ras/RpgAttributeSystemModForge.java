package tn.nightbeam.ras;

import net.minecraft.world.entity.Entity;

import tn.nightbeam.ras.init.RpgAttributeSystemModItems;
import tn.nightbeam.ras.init.RpgAttributeSystemModAttributes;
import tn.nightbeam.ras.init.RpgAttributeSystemModMenusForge;
import tn.nightbeam.ras.init.RpgAttributeSystemModTabs;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.ai.attributes.Attribute;

import java.util.function.Supplier;
import java.util.function.Function;
import java.util.function.BiConsumer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.List;
import java.util.Collection;
import java.util.ArrayList;
import java.util.AbstractMap;

@Mod("rpg_attribute_system")
public class RpgAttributeSystemModForge {
    public static final Logger LOGGER = LogManager.getLogger(RpgAttributeSystemModForge.class);
    public static final String MODID = "rpg_attribute_system";

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    private static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES,
            MODID);

    public RpgAttributeSystemModForge() {
        FMLJavaModLoadingContext context = FMLJavaModLoadingContext.get();
        IEventBus bus = context.getModEventBus();

        RpgAttributeSystemMod.init();
        MinecraftForge.EVENT_BUS.register(this);

        // Register Common Items/Attributes
        RpgAttributeSystemModItems.register((name, item) -> ITEMS.register(name, item));
        RpgAttributeSystemModAttributes.register((name, attribute) -> ATTRIBUTES.register(name, () -> attribute));

        ITEMS.register(bus);
        ATTRIBUTES.register(bus);

        RpgAttributeSystemModTabs.REGISTRY.register(bus);
        RpgAttributeSystemModMenusForge.REGISTRY.register(bus);

        bus.addListener(this::setup);
    }

    private void setup(final net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent event) {
        addNetworkMessage(tn.nightbeam.ras.network.AttributeConfigSyncPacket.class,
                tn.nightbeam.ras.network.AttributeConfigSyncPacket::encode,
                tn.nightbeam.ras.network.AttributeConfigSyncPacket::new,
                (message, contextSupplier) -> {
                    NetworkEvent.Context context = contextSupplier.get();
                    context.enqueueWork(() -> {
                        tn.nightbeam.ras.network.AttributeConfigSyncPacket.handle(message, () -> null);
                    });
                    context.setPacketHandled(true);
                });
        addNetworkMessage(tn.nightbeam.ras.network.GenericButtonActionPacket.class,
                tn.nightbeam.ras.network.GenericButtonActionPacket::encode,
                tn.nightbeam.ras.network.GenericButtonActionPacket::new,
                (message, contextSupplier) -> {
                    NetworkEvent.Context context = contextSupplier.get();
                    context.enqueueWork(() -> {
                        tn.nightbeam.ras.network.GenericButtonActionPacket.handleAction(context.getSender(),
                                message.getButtonID(), message.getX(), message.getY(), message.getZ()); // We access
                                                                                                        // fields
                                                                                                        // directly if
                        // public or via getters. Ah, fields
                        // are private.
                    });
                    context.setPacketHandled(true);
                });
    }

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel PACKET_HANDLER = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MODID, MODID), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);
    private static int messageID = 0;

    public static <T> void addNetworkMessage(Class<T> messageType, BiConsumer<T, FriendlyByteBuf> encoder,
            Function<FriendlyByteBuf, T> decoder, BiConsumer<T, Supplier<NetworkEvent.Context>> messageConsumer) {
        PACKET_HANDLER.registerMessage(messageID, messageType, encoder, decoder, messageConsumer);
        messageID++;
    }

    private static final Collection<AbstractMap.SimpleEntry<Runnable, Integer>> workQueue = new ConcurrentLinkedQueue<>();

    public static void queueServerWork(int tick, Runnable action) {
        if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER)
            workQueue.add(new AbstractMap.SimpleEntry<>(action, tick));
    }

    @SubscribeEvent
    public void tick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            List<AbstractMap.SimpleEntry<Runnable, Integer>> actions = new ArrayList<>();
            workQueue.forEach(work -> {
                work.setValue(work.getValue() - 1);
                if (work.getValue() == 0)
                    actions.add(work);
            });
            actions.forEach(e -> e.getKey().run());
            workQueue.removeAll(actions);
        }
    }
}
