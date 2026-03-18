package tn.nightbeam.ras.init;

import net.minecraft.world.item.Item;
import tn.nightbeam.ras.item.TomeOfAscensionItem;
import tn.nightbeam.ras.item.ScrollOfRebirthItem;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class RpgAttributeSystemModItems {
    // Use memoize to ensure singleton pattern if external potential for multi-call
    // exists, though static final structure usually handles it.
    // Guava Suppliers.memoize is safe.
    public static final Supplier<Item> TOME_OF_ASCENSION = com.google.common.base.Suppliers
            .memoize(() -> new TomeOfAscensionItem());
    public static final Supplier<Item> SCROLL_OF_REBIRTH = com.google.common.base.Suppliers
            .memoize(() -> new ScrollOfRebirthItem());

    public static void register(BiConsumer<String, Supplier<Item>> registerer) {
        registerer.accept("tome_of_ascension", TOME_OF_ASCENSION);
        registerer.accept("scroll_of_rebirth", SCROLL_OF_REBIRTH);
    }
}
