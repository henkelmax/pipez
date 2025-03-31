package de.maxhenkel.pipez.items;

import de.maxhenkel.pipez.tags.ModItemTags;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class WrenchItem extends Item {

    public static final Component WRENCH_TOOLTIP = Component.translatable("tooltip.pipez.wrench").withStyle(ChatFormatting.GRAY);

    public WrenchItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> consumer, TooltipFlag flag) {
        consumer.accept(WRENCH_TOOLTIP);
        super.appendHoverText(stack, context, tooltipDisplay, consumer, flag);
    }

    public static boolean isWrench(ItemStack stack) {
        return stack.is(ModItemTags.WRENCH_TAG) || stack.is(ModItemTags.WRENCHES_TAG);
    }

    public static boolean isHoldingWrench(Player player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (isWrench(stack)) {
                return true;
            }
        }
        return false;
    }

}
