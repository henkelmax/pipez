package de.maxhenkel.pipez.items;

import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.Upgrade;
import de.maxhenkel.pipez.blocks.ModBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    private static final DeferredRegister<Item> ITEM_REGISTER = DeferredRegister.create(BuiltInRegistries.ITEM, Main.MODID);

    public static final DeferredHolder<Item, UpgradeItem> BASIC_UPGRADE = ITEM_REGISTER.register(Upgrade.BASIC.getName() + "_upgrade", () -> new UpgradeItem(Upgrade.BASIC));
    public static final DeferredHolder<Item, UpgradeItem> IMPROVED_UPGRADE = ITEM_REGISTER.register(Upgrade.IMPROVED.getName() + "_upgrade", () -> new UpgradeItem(Upgrade.IMPROVED));
    public static final DeferredHolder<Item, UpgradeItem> ADVANCED_UPGRADE = ITEM_REGISTER.register(Upgrade.ADVANCED.getName() + "_upgrade", () -> new UpgradeItem(Upgrade.ADVANCED));
    public static final DeferredHolder<Item, UpgradeItem> ULTIMATE_UPGRADE = ITEM_REGISTER.register(Upgrade.ULTIMATE.getName() + "_upgrade", () -> new UpgradeItem(Upgrade.ULTIMATE));
    public static final DeferredHolder<Item, UpgradeItem> INFINITY_UPGRADE = ITEM_REGISTER.register(Upgrade.INFINITY.getName() + "_upgrade", () -> new UpgradeItem(Upgrade.INFINITY));
    public static final DeferredHolder<Item, WrenchItem> WRENCH = ITEM_REGISTER.register("wrench", WrenchItem::new);
    public static final DeferredHolder<Item, FilterDestinationToolItem> FILTER_DESTINATION_TOOL = ITEM_REGISTER.register("filter_destination_tool", FilterDestinationToolItem::new);

    public static final DeferredHolder<Item, Item> ITEM_PIPE = ITEM_REGISTER.register("item_pipe", () -> ModBlocks.ITEM_PIPE.get().toItem());
    public static final DeferredHolder<Item, Item> FLUID_PIPE = ITEM_REGISTER.register("fluid_pipe", () -> ModBlocks.FLUID_PIPE.get().toItem());
    public static final DeferredHolder<Item, Item> ENERGY_PIPE = ITEM_REGISTER.register("energy_pipe", () -> ModBlocks.ENERGY_PIPE.get().toItem());
    public static final DeferredHolder<Item, Item> UNIVERSAL_PIPE = ITEM_REGISTER.register("universal_pipe", () -> ModBlocks.UNIVERSAL_PIPE.get().toItem());
    public static final DeferredHolder<Item, Item> GAS_PIPE = ITEM_REGISTER.register("gas_pipe", () -> ModBlocks.GAS_PIPE.get().toItem());

    public static void init(IEventBus eventBus) {
        ITEM_REGISTER.register(eventBus);
    }

}
