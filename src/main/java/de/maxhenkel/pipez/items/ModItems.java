package de.maxhenkel.pipez.items;

import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.Upgrade;
import de.maxhenkel.pipez.blocks.*;
import net.minecraft.world.item.Item;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.ForgeRegistries;
import net.neoforged.neoforge.registries.RegistryObject;

public class ModItems {

    private static final DeferredRegister<Item> ITEM_REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, Main.MODID);

    public static final RegistryObject<UpgradeItem> BASIC_UPGRADE = ITEM_REGISTER.register(Upgrade.BASIC.getName() + "_upgrade", () -> new UpgradeItem(Upgrade.BASIC));
    public static final RegistryObject<UpgradeItem> IMPROVED_UPGRADE = ITEM_REGISTER.register(Upgrade.IMPROVED.getName() + "_upgrade", () -> new UpgradeItem(Upgrade.IMPROVED));
    public static final RegistryObject<UpgradeItem> ADVANCED_UPGRADE = ITEM_REGISTER.register(Upgrade.ADVANCED.getName() + "_upgrade", () -> new UpgradeItem(Upgrade.ADVANCED));
    public static final RegistryObject<UpgradeItem> ULTIMATE_UPGRADE = ITEM_REGISTER.register(Upgrade.ULTIMATE.getName() + "_upgrade", () -> new UpgradeItem(Upgrade.ULTIMATE));
    public static final RegistryObject<UpgradeItem> INFINITY_UPGRADE = ITEM_REGISTER.register(Upgrade.INFINITY.getName() + "_upgrade", () -> new UpgradeItem(Upgrade.INFINITY));
    public static final RegistryObject<WrenchItem> WRENCH = ITEM_REGISTER.register("wrench", WrenchItem::new);
    public static final RegistryObject<FilterDestinationToolItem> FILTER_DESTINATION_TOOL = ITEM_REGISTER.register("filter_destination_tool", FilterDestinationToolItem::new);

    public static final RegistryObject<Item> ITEM_PIPE = ITEM_REGISTER.register("item_pipe", () -> ModBlocks.ITEM_PIPE.get().toItem());
    public static final RegistryObject<Item> FLUID_PIPE = ITEM_REGISTER.register("fluid_pipe", () -> ModBlocks.FLUID_PIPE.get().toItem());
    public static final RegistryObject<Item> ENERGY_PIPE = ITEM_REGISTER.register("energy_pipe", () -> ModBlocks.ENERGY_PIPE.get().toItem());
    public static final RegistryObject<Item> UNIVERSAL_PIPE = ITEM_REGISTER.register("universal_pipe", () -> ModBlocks.UNIVERSAL_PIPE.get().toItem());
    public static final RegistryObject<Item> GAS_PIPE = ITEM_REGISTER.register("gas_pipe", () -> ModBlocks.GAS_PIPE.get().toItem());

    public static void init() {
        ITEM_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

}
