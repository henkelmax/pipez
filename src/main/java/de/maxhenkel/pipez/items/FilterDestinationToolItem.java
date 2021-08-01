package de.maxhenkel.pipez.items;

import de.maxhenkel.pipez.DirectionalPosition;
import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.ModItemGroups;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.List;

public class FilterDestinationToolItem extends Item {

    public FilterDestinationToolItem() {
        super(new Properties().tab(ModItemGroups.TAB_PIPEZ).stacksTo(1));
        setRegistryName(new ResourceLocation(Main.MODID, "filter_destination_tool"));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        tooltip.add(new TranslatableComponent("tooltip.pipez.filter_destination_tool").withStyle(ChatFormatting.GRAY));
        DirectionalPosition dst = getDestination(stack);
        if (dst != null) {
            tooltip.add(new TranslatableComponent("tooltip.pipez.filter_destination_tool.destination", number(dst.getPos().getX()), number(dst.getPos().getY()), number(dst.getPos().getZ()), new TranslatableComponent("message.pipez.direction." + dst.getDirection().getName()).withStyle(ChatFormatting.GREEN)).withStyle(ChatFormatting.GRAY));
        }
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }

    private MutableComponent number(int num) {
        return new TextComponent(String.valueOf(num)).withStyle(ChatFormatting.GREEN);
    }

    @Nullable
    public static DirectionalPosition getDestination(ItemStack stack) {
        if (!stack.hasTag()) {
            return null;
        }
        CompoundTag tag = stack.getTag();
        if (!tag.contains("Destination", Constants.NBT.TAG_COMPOUND)) {
            return null;
        }
        DirectionalPosition dest = new DirectionalPosition();
        dest.deserializeNBT(tag.getCompound("Destination"));
        return dest;
    }

    public static void setDestination(ItemStack stack, DirectionalPosition dest) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.put("Destination", dest.serializeNBT());
    }

}
