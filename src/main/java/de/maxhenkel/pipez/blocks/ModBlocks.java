package de.maxhenkel.pipez.blocks;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.ModList;

public class ModBlocks {

    public static final ItemPipeBlock ITEM_PIPE = new ItemPipeBlock();
    public static final FluidPipeBlock FLUID_PIPE = new FluidPipeBlock();
    public static final EnergyPipeBlock ENERGY_PIPE = new EnergyPipeBlock();
    public static final UniversalPipeBlock UNIVERSAL_PIPE = new UniversalPipeBlock();
    public static final GasPipeBlock GAS_PIPE = new GasPipeBlock();

    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(
                ITEM_PIPE,
                FLUID_PIPE,
                ENERGY_PIPE,
                UNIVERSAL_PIPE
        );
        if (ModList.get().isLoaded("mekanism")) {
            event.getRegistry().register(GAS_PIPE);
        }
    }

    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
                ITEM_PIPE.toItem(),
                FLUID_PIPE.toItem(),
                ENERGY_PIPE.toItem(),
                UNIVERSAL_PIPE.toItem()
        );
        if (ModList.get().isLoaded("mekanism")) {
            event.getRegistry().register(GAS_PIPE.toItem());
        }
    }

}
