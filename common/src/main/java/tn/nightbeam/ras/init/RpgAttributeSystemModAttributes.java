package tn.nightbeam.ras.init;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class RpgAttributeSystemModAttributes {
    public static final Attribute RPG_LEVEL_OBJ = new RangedAttribute("attribute.rpg_attribute_system.rpg_level", 0, 0,
            999999).setSyncable(true);
    public static final Supplier<Attribute> RPG_LEVEL = () -> RPG_LEVEL_OBJ;

    public static final Attribute ATTRIBUTE_1_OBJ = new RangedAttribute("attribute.rpg_attribute_system.attribute_1", 0,
            0, 1).setSyncable(true);
    public static final Supplier<Attribute> ATTRIBUTE_1 = () -> ATTRIBUTE_1_OBJ;

    public static final Attribute ATTRIBUTE_2_OBJ = new RangedAttribute("attribute.rpg_attribute_system.attribute_2", 0,
            0, 1).setSyncable(true);
    public static final Supplier<Attribute> ATTRIBUTE_2 = () -> ATTRIBUTE_2_OBJ;

    public static final Attribute ATTRIBUTE_3_OBJ = new RangedAttribute("attribute.rpg_attribute_system.attribute_3", 0,
            0, 1).setSyncable(true);
    public static final Supplier<Attribute> ATTRIBUTE_3 = () -> ATTRIBUTE_3_OBJ;

    public static final Attribute ATTRIBUTE_4_OBJ = new RangedAttribute("attribute.rpg_attribute_system.attribute_4", 0,
            0, 1).setSyncable(true);
    public static final Supplier<Attribute> ATTRIBUTE_4 = () -> ATTRIBUTE_4_OBJ;

    public static final Attribute ATTRIBUTE_5_OBJ = new RangedAttribute("attribute.rpg_attribute_system.attribute_5", 0,
            0, 1).setSyncable(true);
    public static final Supplier<Attribute> ATTRIBUTE_5 = () -> ATTRIBUTE_5_OBJ;

    public static final Attribute ATTRIBUTE_6_OBJ = new RangedAttribute("attribute.rpg_attribute_system.attribute_6", 0,
            0, 1).setSyncable(true);
    public static final Supplier<Attribute> ATTRIBUTE_6 = () -> ATTRIBUTE_6_OBJ;

    public static final Attribute ATTRIBUTE_7_OBJ = new RangedAttribute("attribute.rpg_attribute_system.attribute_7", 0,
            0, 1).setSyncable(true);
    public static final Supplier<Attribute> ATTRIBUTE_7 = () -> ATTRIBUTE_7_OBJ;

    public static final Attribute ATTRIBUTE_8_OBJ = new RangedAttribute("attribute.rpg_attribute_system.attribute_8", 0,
            0, 1).setSyncable(true);
    public static final Supplier<Attribute> ATTRIBUTE_8 = () -> ATTRIBUTE_8_OBJ;

    public static final Attribute ATTRIBUTE_9_OBJ = new RangedAttribute("attribute.rpg_attribute_system.attribute_9", 0,
            0, 1).setSyncable(true);
    public static final Supplier<Attribute> ATTRIBUTE_9 = () -> ATTRIBUTE_9_OBJ;

    public static final Attribute ATTRIBUTE_10_OBJ = new RangedAttribute("attribute.rpg_attribute_system.attribute_10",
            0, 0, 1).setSyncable(true);
    public static final Supplier<Attribute> ATTRIBUTE_10 = () -> ATTRIBUTE_10_OBJ;

    public static void register(BiConsumer<String, Attribute> registerer) {
        registerer.accept("rpg_level", RPG_LEVEL_OBJ);
        registerer.accept("attribute_1", ATTRIBUTE_1_OBJ);
        registerer.accept("attribute_2", ATTRIBUTE_2_OBJ);
        registerer.accept("attribute_3", ATTRIBUTE_3_OBJ);
        registerer.accept("attribute_4", ATTRIBUTE_4_OBJ);
        registerer.accept("attribute_5", ATTRIBUTE_5_OBJ);
        registerer.accept("attribute_6", ATTRIBUTE_6_OBJ);
        registerer.accept("attribute_7", ATTRIBUTE_7_OBJ);
        registerer.accept("attribute_8", ATTRIBUTE_8_OBJ);
        registerer.accept("attribute_9", ATTRIBUTE_9_OBJ);
        registerer.accept("attribute_10", ATTRIBUTE_10_OBJ);
    }
}
