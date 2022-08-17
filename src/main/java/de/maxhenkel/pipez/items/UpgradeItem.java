package de.maxhenkel.pipez.items;

import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.ModItemGroups;
import de.maxhenkel.pipez.types.Upgrade;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class UpgradeItem extends Item {

    private final Upgrade tier;

    public UpgradeItem(Upgrade tier) {
        super(new Properties().tab(ModItemGroups.INSTANCE.getTAB_PIPEZ()));
        this.tier = tier;
        setRegistryName(new ResourceLocation(Main.MODID, tier.getUpgradeName() + "_upgrade"));
    }

    public Upgrade getTier() {
        return tier;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        CompoundTag tag = stack.getTag();
        if (tag != null) {
            List<MutableComponent> list = new ArrayList<>();
            if (tag.contains("Item", Tag.TAG_COMPOUND)) {
                list.add(new TranslatableComponent("tooltip.pipez.upgrade.configured.item"));
            }
            if (tag.contains("Energy", Tag.TAG_COMPOUND)) {
                list.add(new TranslatableComponent("tooltip.pipez.upgrade.configured.energy"));
            }
            if (tag.contains("Fluid", Tag.TAG_COMPOUND)) {
                list.add(new TranslatableComponent("tooltip.pipez.upgrade.configured.fluid"));
            }
            if (tag.contains("Gas", Tag.TAG_COMPOUND)) {
                list.add(new TranslatableComponent("tooltip.pipez.upgrade.configured.gas"));
            }

            if (!list.isEmpty()) {
                MutableComponent types = list.stream().reduce((text1, text2) -> text1.append(", ").append(text2)).get();
                tooltip.add(new TranslatableComponent("tooltip.pipez.upgrade.configured", types.withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.YELLOW));
            }
        }
    }

}
