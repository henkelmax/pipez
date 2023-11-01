package de.maxhenkel.pipez;

import de.maxhenkel.pipez.blocks.ModBlocks;
import de.maxhenkel.pipez.items.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegistryObject;

public class ModCreativeTabs {

    private static final DeferredRegister<CreativeModeTab> TAB_REGISTER = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Main.MODID);

    public static final RegistryObject<CreativeModeTab> TAB_PIPEZ = TAB_REGISTER.register("pipez", () -> {
        return CreativeModeTab.builder()
                .icon(() -> new ItemStack(ModBlocks.ITEM_PIPE.get()))
                .displayItems((features, output) -> {
                    output.accept(new ItemStack(ModBlocks.ITEM_PIPE.get()));
                    output.accept(new ItemStack(ModBlocks.FLUID_PIPE.get()));
                    output.accept(new ItemStack(ModBlocks.ENERGY_PIPE.get()));
                    output.accept(new ItemStack(ModBlocks.UNIVERSAL_PIPE.get()));
                    output.accept(new ItemStack(ModBlocks.GAS_PIPE.get()));

                    output.accept(new ItemStack(ModItems.BASIC_UPGRADE.get()));
                    output.accept(new ItemStack(ModItems.IMPROVED_UPGRADE.get()));
                    output.accept(new ItemStack(ModItems.ADVANCED_UPGRADE.get()));
                    output.accept(new ItemStack(ModItems.ULTIMATE_UPGRADE.get()));
                    output.accept(new ItemStack(ModItems.INFINITY_UPGRADE.get()));

                    output.accept(new ItemStack(ModItems.WRENCH.get()));
                    output.accept(new ItemStack(ModItems.FILTER_DESTINATION_TOOL.get()));
                })
                .title(Component.translatable("itemGroup.pipez"))
                .build();
    });

    public static void init() {
        TAB_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

}
