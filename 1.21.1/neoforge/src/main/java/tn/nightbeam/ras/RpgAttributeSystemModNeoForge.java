package tn.nightbeam.ras;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tn.nightbeam.ras.init.RpgAttributeSystemModAttributes;
import tn.nightbeam.ras.init.RpgAttributeSystemModItems;
import tn.nightbeam.ras.neoforge.NeoForgeNetworking;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

@Mod(RpgAttributeSystemMod.MOD_ID)
public class RpgAttributeSystemModNeoForge {
    public static final Logger LOGGER = LogManager.getLogger(RpgAttributeSystemModNeoForge.class);

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM,
            RpgAttributeSystemMod.MOD_ID);
    private static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(BuiltInRegistries.ATTRIBUTE,
            RpgAttributeSystemMod.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister
            .create(Registries.CREATIVE_MODE_TAB, RpgAttributeSystemMod.MOD_ID);

    // Creative Tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> RAS_CREATIVE_TAB = CREATIVE_TABS.register(
            "ras_creative_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("item_group.rpg_attribute_system.ras_creative_tab"))
                    .icon(() -> new ItemStack(RpgAttributeSystemModItems.TOME_OF_ASCENSION.get()))
                    .displayItems((parameters, tabData) -> {
                        tabData.accept(RpgAttributeSystemModItems.TOME_OF_ASCENSION.get());
                        tabData.accept(RpgAttributeSystemModItems.SCROLL_OF_REBIRTH.get());
                    }).build());

    public RpgAttributeSystemModNeoForge(IEventBus modEventBus) {
        RpgAttributeSystemMod.init();

        // Register Items and Attributes using DeferredRegister
        RpgAttributeSystemModItems.register((name, item) -> ITEMS.register(name, item));
        RpgAttributeSystemModAttributes.register((name, attribute) -> ATTRIBUTES.register(name, () -> attribute));

        ITEMS.register(modEventBus);
        ATTRIBUTES.register(modEventBus);
        CREATIVE_TABS.register(modEventBus);

        // Register menus
        tn.nightbeam.ras.neoforge.RpgAttributeSystemModMenusNeoForge.REGISTRY.register(modEventBus);

        // Register data attachments (replaces capabilities)
        tn.nightbeam.ras.neoforge.NeoForgeDataAttachments.ATTACHMENT_TYPES.register(modEventBus);

        modEventBus.addListener(this::setup);
        modEventBus.addListener(NeoForgeNetworking::register);

        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(tn.nightbeam.ras.neoforge.NeoForgeEvents.class);
    }

    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("RPG Attribute System NeoForge setup complete");
    }

    // Work queue for server-side scheduled tasks
    private static final Collection<AbstractMap.SimpleEntry<Runnable, Integer>> workQueue = new ConcurrentLinkedQueue<>();

    public static void queueServerWork(int tick, Runnable action) {
        workQueue.add(new AbstractMap.SimpleEntry<>(action, tick));
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
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
