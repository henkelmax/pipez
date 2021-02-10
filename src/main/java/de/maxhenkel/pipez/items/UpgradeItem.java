package de.maxhenkel.pipez.items;

import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.ModItemGroups;
import de.maxhenkel.pipez.Upgrade;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
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

        int itemFilters = getFilterCount(stack, "ItemFilters");
        if (itemFilters > 0) {
            tooltip.add(new TranslationTextComponent("tooltip.pipez.upgrade.filters.item", new StringTextComponent(String.valueOf(itemFilters)).mergeStyle(TextFormatting.WHITE)).mergeStyle(TextFormatting.YELLOW));
        }
        int fluidFilters = getFilterCount(stack, "FluidFilters");
        if (fluidFilters > 0) {
            tooltip.add(new TranslationTextComponent("tooltip.pipez.upgrade.filters.fluid", new StringTextComponent(String.valueOf(fluidFilters)).mergeStyle(TextFormatting.WHITE)).mergeStyle(TextFormatting.BLUE));
        }
    }

    private int getFilterCount(ItemStack stack, String key) {
        if (!stack.hasTag()) {
            return 0;
        }
        CompoundNBT tag = stack.getTag();
        if (!tag.contains(key, Constants.NBT.TAG_LIST)) {
            return 0;
        }
        ListNBT filters = tag.getList(key, Constants.NBT.TAG_COMPOUND);
        return filters.size();
    }
}
