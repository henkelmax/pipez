package de.maxhenkel.pipez.items;

import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.ModItemGroups;
import de.maxhenkel.pipez.Upgrade;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class UpgradeItem extends Item {

    private final Upgrade tier;

    public UpgradeItem(Upgrade tier) {
        super(new Properties().group(ModItemGroups.TAB_PIPEZ));
        this.tier = tier;
        setRegistryName(new ResourceLocation(Main.MODID, tier.getName() + "_upgrade"));
    }

    public Upgrade getTier() {
        return tier;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        CompoundNBT tag = stack.getTag();
        if (tag != null) {
            List<IFormattableTextComponent> list = new ArrayList<>();
            if (tag.contains("Item", Constants.NBT.TAG_COMPOUND)) {
                list.add(new TranslationTextComponent("tooltip.pipez.upgrade.configured.item"));
            }
            if (tag.contains("Energy", Constants.NBT.TAG_COMPOUND)) {
                list.add(new TranslationTextComponent("tooltip.pipez.upgrade.configured.energy"));
            }
            if (tag.contains("Fluid", Constants.NBT.TAG_COMPOUND)) {
                list.add(new TranslationTextComponent("tooltip.pipez.upgrade.configured.fluid"));
            }
            if (tag.contains("Gas", Constants.NBT.TAG_COMPOUND)) {
                list.add(new TranslationTextComponent("tooltip.pipez.upgrade.configured.gas"));
            }

            if (!list.isEmpty()) {
                IFormattableTextComponent types = list.stream().reduce((text1, text2) -> text1.appendString(", ").append(text2)).get();
                tooltip.add(new TranslationTextComponent("tooltip.pipez.upgrade.configured", types.mergeStyle(TextFormatting.WHITE)).mergeStyle(TextFormatting.YELLOW));
            }
        }
    }

    public static ItemStack upgradeData(ItemStack stack) {
        if (!(stack.getItem() instanceof UpgradeItem)) {
            return stack;
        }

        if (!stack.hasTag()) {
            return stack;
        }

        boolean isOld = false;

        CompoundNBT oldTag = stack.getTag();
        CompoundNBT newTag = new CompoundNBT();

        CompoundNBT item = new CompoundNBT();
        newTag.put("Item", item);

        CompoundNBT energy = new CompoundNBT();
        newTag.put("Energy", energy);

        CompoundNBT fluid = new CompoundNBT();
        newTag.put("Fluid", fluid);

        CompoundNBT gas = new CompoundNBT();
        newTag.put("Gas", gas);

        if (oldTag.contains("RedstoneMode", Constants.NBT.TAG_BYTE)) {
            item.putByte("RedstoneMode", oldTag.getByte("RedstoneMode"));
            energy.putByte("RedstoneMode", oldTag.getByte("RedstoneMode"));
            fluid.putByte("RedstoneMode", oldTag.getByte("RedstoneMode"));
            gas.putByte("RedstoneMode", oldTag.getByte("RedstoneMode"));
            isOld = true;
        }

        if (oldTag.contains("Distribution", Constants.NBT.TAG_BYTE)) {
            item.putByte("Distribution", oldTag.getByte("Distribution"));
            energy.putByte("Distribution", oldTag.getByte("Distribution"));
            fluid.putByte("Distribution", oldTag.getByte("Distribution"));
            gas.putByte("Distribution", oldTag.getByte("Distribution"));
            isOld = true;
        }

        if (oldTag.contains("FilterMode", Constants.NBT.TAG_BYTE)) {
            item.putByte("FilterMode", oldTag.getByte("FilterMode"));
            energy.putByte("FilterMode", oldTag.getByte("FilterMode"));
            fluid.putByte("FilterMode", oldTag.getByte("FilterMode"));
            gas.putByte("FilterMode", oldTag.getByte("FilterMode"));
            isOld = true;
        }

        if (oldTag.contains("ItemFilters", Constants.NBT.TAG_LIST)) {
            item.put("Filters", oldTag.getList("ItemFilters", Constants.NBT.TAG_COMPOUND));
            isOld = true;
        }

        if (oldTag.contains("FluidFilters", Constants.NBT.TAG_LIST)) {
            fluid.put("Filters", oldTag.getList("FluidFilters", Constants.NBT.TAG_COMPOUND));
            isOld = true;
        }

        if (oldTag.contains("GasFilters", Constants.NBT.TAG_LIST)) {
            gas.put("Filters", oldTag.getList("GasFilters", Constants.NBT.TAG_COMPOUND));
            isOld = true;
        }

        if (isOld) {
            stack.setTag(newTag);
        }
        return stack;
    }

}
