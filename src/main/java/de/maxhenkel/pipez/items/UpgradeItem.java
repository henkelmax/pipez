package de.maxhenkel.pipez.items;

import de.maxhenkel.pipez.Upgrade;
import de.maxhenkel.pipez.datacomponents.EnergyData;
import de.maxhenkel.pipez.datacomponents.FluidData;
import de.maxhenkel.pipez.datacomponents.GasData;
import de.maxhenkel.pipez.datacomponents.ItemData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.ArrayList;
import java.util.List;

public class UpgradeItem extends Item {

    public static final Component CONFIGURED_ITEM_TOOLTIP = Component.translatable("tooltip.pipez.upgrade.configured.item");
    public static final Component CONFIGURED_ENERGY_TOOLTIP = Component.translatable("tooltip.pipez.upgrade.configured.energy");
    public static final Component CONFIGURED_FLUID_TOOLTIP = Component.translatable("tooltip.pipez.upgrade.configured.fluid");
    public static final Component CONFIGURED_GAS_TOOLTIP = Component.translatable("tooltip.pipez.upgrade.configured.gas");

    private final Upgrade tier;

    public UpgradeItem(Upgrade tier) {
        super(new Properties());
        this.tier = tier;
    }

    public Upgrade getTier() {
        return tier;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, context, tooltip, flagIn);

        List<MutableComponent> list = new ArrayList<>();

        ItemData itemData = stack.get(ModItems.ITEM_DATA_COMPONENT);
        if (itemData != null) {
            list.add(CONFIGURED_ITEM_TOOLTIP.copy());
        }

        EnergyData energyData = stack.get(ModItems.ENERGY_DATA_COMPONENT);
        if (energyData != null) {
            list.add(CONFIGURED_ENERGY_TOOLTIP.copy());
        }

        FluidData fluidData = stack.get(ModItems.FLUID_DATA_COMPONENT);
        if (fluidData != null) {
            list.add(CONFIGURED_FLUID_TOOLTIP.copy());
        }
        GasData gasData = stack.get(ModItems.GAS_DATA_COMPONENT);
        if (gasData != null) {
            list.add(CONFIGURED_GAS_TOOLTIP.copy());
        }

        if (!list.isEmpty()) {
            MutableComponent types = list.stream().reduce((text1, text2) -> text1.append(", ").append(text2)).get();
            tooltip.add(Component.translatable("tooltip.pipez.upgrade.configured", types.withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.YELLOW));
        }
    }

}
