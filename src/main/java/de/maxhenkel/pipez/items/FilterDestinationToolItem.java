package de.maxhenkel.pipez.items;

import de.maxhenkel.pipez.DirectionalPosition;
import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.ModItemGroups;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.List;

public class FilterDestinationToolItem extends Item {

    public FilterDestinationToolItem() {
        super(new Properties().tab(ModItemGroups.TAB_PIPEZ).stacksTo(1));
        setRegistryName(new ResourceLocation(Main.MODID, "filter_destination_tool"));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        tooltip.add(new TranslationTextComponent("tooltip.pipez.filter_destination_tool").withStyle(TextFormatting.GRAY));
        DirectionalPosition dst = getDestination(stack);
        if (dst != null) {
            tooltip.add(new TranslationTextComponent("tooltip.pipez.filter_destination_tool.destination", number(dst.getPos().getX()), number(dst.getPos().getY()), number(dst.getPos().getZ()), new TranslationTextComponent("message.pipez.direction." + dst.getDirection().getName()).withStyle(TextFormatting.GREEN)).withStyle(TextFormatting.GRAY));
        }
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }

    private IFormattableTextComponent number(int num) {
        return new StringTextComponent(String.valueOf(num)).withStyle(TextFormatting.GREEN);
    }

    @Nullable
    public static DirectionalPosition getDestination(ItemStack stack) {
        if (!stack.hasTag()) {
            return null;
        }
        CompoundNBT tag = stack.getTag();
        if (!tag.contains("Destination", Constants.NBT.TAG_COMPOUND)) {
            return null;
        }
        DirectionalPosition dest = new DirectionalPosition();
        dest.deserializeNBT(tag.getCompound("Destination"));
        return dest;
    }

    public static void setDestination(ItemStack stack, DirectionalPosition dest) {
        CompoundNBT tag = stack.getOrCreateTag();
        tag.put("Destination", dest.serializeNBT());
    }

}
