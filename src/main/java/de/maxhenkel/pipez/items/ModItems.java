package de.maxhenkel.pipez.items;

import de.maxhenkel.pipez.DirectionalPosition;
import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.Upgrade;
import de.maxhenkel.pipez.blocks.ModBlocks;
import de.maxhenkel.pipez.datacomponents.EnergyData;
import de.maxhenkel.pipez.datacomponents.FluidData;
import de.maxhenkel.pipez.datacomponents.GasData;
import de.maxhenkel.pipez.datacomponents.ItemData;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    private static final DeferredRegister.Items ITEM_REGISTER = DeferredRegister.createItems(Main.MODID);

    public static final DeferredHolder<Item, UpgradeItem> BASIC_UPGRADE = ITEM_REGISTER.registerItem(Upgrade.BASIC.getName() + "_upgrade", p -> new UpgradeItem(Upgrade.BASIC, p));
    public static final DeferredHolder<Item, UpgradeItem> IMPROVED_UPGRADE = ITEM_REGISTER.registerItem(Upgrade.IMPROVED.getName() + "_upgrade", p -> new UpgradeItem(Upgrade.IMPROVED, p));
    public static final DeferredHolder<Item, UpgradeItem> ADVANCED_UPGRADE = ITEM_REGISTER.registerItem(Upgrade.ADVANCED.getName() + "_upgrade", p -> new UpgradeItem(Upgrade.ADVANCED, p));
    public static final DeferredHolder<Item, UpgradeItem> ULTIMATE_UPGRADE = ITEM_REGISTER.registerItem(Upgrade.ULTIMATE.getName() + "_upgrade", p -> new UpgradeItem(Upgrade.ULTIMATE, p));
    public static final DeferredHolder<Item, UpgradeItem> INFINITY_UPGRADE = ITEM_REGISTER.registerItem(Upgrade.INFINITY.getName() + "_upgrade", p -> new UpgradeItem(Upgrade.INFINITY, p));
    public static final DeferredHolder<Item, WrenchItem> WRENCH = ITEM_REGISTER.registerItem("wrench", WrenchItem::new);
    public static final DeferredHolder<Item, FilterDestinationToolItem> FILTER_DESTINATION_TOOL = ITEM_REGISTER.registerItem("filter_destination_tool", FilterDestinationToolItem::new);

    public static final DeferredHolder<Item, BlockItem> ITEM_PIPE = ITEM_REGISTER.registerSimpleBlockItem(ModBlocks.ITEM_PIPE);
    public static final DeferredHolder<Item, BlockItem> FLUID_PIPE = ITEM_REGISTER.registerSimpleBlockItem(ModBlocks.FLUID_PIPE);
    public static final DeferredHolder<Item, BlockItem> ENERGY_PIPE = ITEM_REGISTER.registerSimpleBlockItem(ModBlocks.ENERGY_PIPE);
    public static final DeferredHolder<Item, BlockItem> UNIVERSAL_PIPE = ITEM_REGISTER.registerSimpleBlockItem(ModBlocks.UNIVERSAL_PIPE);
    public static final DeferredHolder<Item, BlockItem> GAS_PIPE = ITEM_REGISTER.registerSimpleBlockItem(ModBlocks.GAS_PIPE);

    private static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPE_REGISTER = DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE, Main.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<DirectionalPosition>> DIRECTIONAL_POSITION_DATA_COMPONENT = DATA_COMPONENT_TYPE_REGISTER.register("directional_position", () -> DataComponentType.<DirectionalPosition>builder().persistent(DirectionalPosition.CODEC).networkSynchronized(DirectionalPosition.STREAM_CODEC).build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemData>> ITEM_DATA_COMPONENT = DATA_COMPONENT_TYPE_REGISTER.register("item", () -> DataComponentType.<ItemData>builder().persistent(ItemData.CODEC).networkSynchronized(ItemData.STREAM_CODEC).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<FluidData>> FLUID_DATA_COMPONENT = DATA_COMPONENT_TYPE_REGISTER.register("fluid", () -> DataComponentType.<FluidData>builder().persistent(FluidData.CODEC).networkSynchronized(FluidData.STREAM_CODEC).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<GasData>> GAS_DATA_COMPONENT = DATA_COMPONENT_TYPE_REGISTER.register("gas", () -> DataComponentType.<GasData>builder().persistent(GasData.CODEC).networkSynchronized(GasData.STREAM_CODEC).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<EnergyData>> ENERGY_DATA_COMPONENT = DATA_COMPONENT_TYPE_REGISTER.register("energy", () -> DataComponentType.<EnergyData>builder().persistent(EnergyData.CODEC).networkSynchronized(EnergyData.STREAM_CODEC).build());

    public static void init(IEventBus eventBus) {
        ITEM_REGISTER.register(eventBus);
        DATA_COMPONENT_TYPE_REGISTER.register(eventBus);
    }

}
