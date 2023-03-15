package de.maxhenkel.pipez.events;

import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.blocks.ModBlocks;
import de.maxhenkel.pipez.items.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CreativeTabEvents {

    public static CreativeModeTab TAB_PIPEZ;

    @SubscribeEvent
    public static void onCreativeModeTabRegister(CreativeModeTabEvent.Register event) {
        TAB_PIPEZ = event.registerCreativeModeTab(new ResourceLocation(Main.MODID, "pipez"), builder -> {
            builder
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
                    })
                    .title(Component.translatable("itemGroup.pipez"))
                    .build();
        });
    }

}
