package de.maxhenkel.pipez.items;

import de.maxhenkel.pipez.DirectionalPosition;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class FilterDestinationToolItem extends Item {

    public FilterDestinationToolItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> consumer, TooltipFlag flag) {
        consumer.accept(Component.translatable("tooltip.pipez.filter_destination_tool").withStyle(ChatFormatting.GRAY));
        DirectionalPosition dst = getDestination(stack);
        if (dst != null) {
            consumer.accept(Component.translatable("tooltip.pipez.filter_destination_tool.destination", number(dst.getPos().getX()), number(dst.getPos().getY()), number(dst.getPos().getZ()), Component.translatable("message.pipez.direction." + dst.getDirection().getName()).withStyle(ChatFormatting.GREEN)).withStyle(ChatFormatting.GRAY));
        }
        super.appendHoverText(stack, context, tooltipDisplay, consumer, flag);
    }

    private MutableComponent number(int num) {
        return Component.literal(String.valueOf(num)).withStyle(ChatFormatting.GREEN);
    }

    @Nullable
    public static DirectionalPosition getDestination(ItemStack stack) {
        return stack.get(ModItems.DIRECTIONAL_POSITION_DATA_COMPONENT);
    }

    public static void setDestination(ItemStack stack, DirectionalPosition dest) {
        stack.set(ModItems.DIRECTIONAL_POSITION_DATA_COMPONENT, dest);
    }

}
