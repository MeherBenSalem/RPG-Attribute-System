package tn.nightbeam.ras.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import tn.nightbeam.ras.RpgAttributeSystemMod;
import tn.nightbeam.ras.procedures.OrbOfLevelingProcedureProcedure;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.component.TooltipDisplay;
import java.util.function.Consumer;

public class TomeOfAscensionItem extends Item {
    public TomeOfAscensionItem(String itemName) {
        super(new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM,
                        Identifier.fromNamespaceAndPath(RpgAttributeSystemMod.MOD_ID, itemName)))
                .stacksTo(64)
                .fireResistant()
                .rarity(Rarity.EPIC));
    }

    @Override
    public boolean isFoil(ItemStack itemstack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack itemstack, Item.TooltipContext context, TooltipDisplay tooltipDisplay,
            Consumer<Component> tooltipAdder, TooltipFlag flag) {
        super.appendHoverText(itemstack, context, tooltipDisplay, tooltipAdder, flag);
        tooltipAdder.accept(Component.translatable("item.rpg_attribute_system.tome_of_ascension.description_0"));
    }

    @Override
    public InteractionResult use(Level world, Player entity, InteractionHand hand) {
        InteractionResult ar = super.use(world, entity, hand);
        OrbOfLevelingProcedureProcedure.execute(world, entity);
        return ar;
    }
}
