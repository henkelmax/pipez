package de.maxhenkel.pipez.items;

import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.ModItemGroups;
import de.maxhenkel.pipez.tags.ModItemTags;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class WrenchItem extends Item {

    public WrenchItem() {
        super(new Properties().group(ModItemGroups.TAB_PIPEZ));
        setRegistryName(new ResourceLocation(Main.MODID, "wrench"));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        tooltip.add(new TranslationTextComponent("tooltip.pipez.wrench").mergeStyle(TextFormatting.GRAY));
        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

    public static boolean isWrench(ItemStack stack) {
        return stack.getItem().isIn(ModItemTags.WRENCH_TAG) || stack.getItem().isIn(ModItemTags.WRENCHES_TAG);
    }

    public static boolean isHoldingWrench(PlayerEntity player) {
        for (ItemStack stack : player.getHeldEquipment()) {
            if (isWrench(stack)) {
                return true;
            }
        }
        return false;
    }

}
