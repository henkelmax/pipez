package de.maxhenkel.pipez.items;

import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.ModItemGroups;
import de.maxhenkel.pipez.Upgrade;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

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
}
