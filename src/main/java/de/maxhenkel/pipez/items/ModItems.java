package de.maxhenkel.pipez.items;

import de.maxhenkel.pipez.Upgrade;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;

public class ModItems {

    public static final UpgradeItem BASIC_UPGRADE = new UpgradeItem(Upgrade.BASIC);
    public static final UpgradeItem IMPROVED_UPGRADE = new UpgradeItem(Upgrade.IMPROVED);
    public static final UpgradeItem ADVANCED_UPGRADE = new UpgradeItem(Upgrade.ADVANCED);
    public static final UpgradeItem ULTIMATE_UPGRADE = new UpgradeItem(Upgrade.ULTIMATE);

    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
                BASIC_UPGRADE,
                IMPROVED_UPGRADE,
                ADVANCED_UPGRADE,
                ULTIMATE_UPGRADE
        );
    }

}
