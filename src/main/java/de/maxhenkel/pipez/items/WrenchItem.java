package de.maxhenkel.pipez.items;

import de.maxhenkel.pipez.tags.ModItemTags;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class WrenchItem extends Item {

    public static final Component WRENCH_TOOLTIP = Component.translatable("tooltip.pipez.wrench").withStyle(ChatFormatting.GRAY);

    public WrenchItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        tooltip.add(WRENCH_TOOLTIP);
        super.appendHoverText(stack, context, tooltip, flagIn);
    }

    public static boolean isWrench(ItemStack stack) {
        return stack.is(ModItemTags.WRENCH_TAG) || stack.is(ModItemTags.WRENCHES_TAG);
    }

    public static boolean isHoldingWrench(Player player) {
        for (ItemStack stack : player.getHandSlots()) {
            if (isWrench(stack)) {
                return true;
            }
        }
        return false;
    }

}
