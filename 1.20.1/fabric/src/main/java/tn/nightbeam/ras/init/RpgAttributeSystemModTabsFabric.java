package tn.nightbeam.ras.init;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import tn.nightbeam.ras.RpgAttributeSystemMod;

public class RpgAttributeSystemModTabsFabric {
    public static final CreativeModeTab RAS_CREATIVE_TAB = FabricItemGroup.builder()
            .title(Component.translatable("item_group.rpg_attribute_system.ras_creative_tab"))
            .icon(() -> new ItemStack(RpgAttributeSystemModItems.TOME_OF_ASCENSION.get()))
            .displayItems((parameters, output) -> {
                output.accept(RpgAttributeSystemModItems.TOME_OF_ASCENSION.get());
                output.accept(RpgAttributeSystemModItems.SCROLL_OF_REBIRTH.get());
            })
            .build();

    public static void register() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
                new ResourceLocation(RpgAttributeSystemMod.MOD_ID, "ras_creative_tab"),
                RAS_CREATIVE_TAB);
    }
}
