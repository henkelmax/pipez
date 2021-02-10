package de.maxhenkel.pipez.items;

import de.maxhenkel.pipez.Upgrade;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;

public class ModItems {

    public static final UpgradeItem BASIC_UPGRADE = new UpgradeItem(Upgrade.BASIC);
    public static final UpgradeItem IMPROVED_UPGRADE = new UpgradeItem(Upgrade.IMPROVED);
    public static final UpgradeItem ADVANCED_UPGRADE = new UpgradeItem(Upgrade.ADVANCED);
    public static final UpgradeItem ULTIMATE_UPGRADE = new UpgradeItem(Upgrade.ULTIMATE);
    public static final UpgradeItem INFINITY_UPGRADE = new UpgradeItem(Upgrade.INFINITY);
    public static final WrenchItem WRENCH = new WrenchItem();
    public static final FilterDestinationToolItem FILTER_DESTINATION_TOOL = new FilterDestinationToolItem();

    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
                BASIC_UPGRADE,
                IMPROVED_UPGRADE,
                ADVANCED_UPGRADE,
                ULTIMATE_UPGRADE,
                INFINITY_UPGRADE,
                WRENCH,
                FILTER_DESTINATION_TOOL
        );
    }

}
