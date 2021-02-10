package de.maxhenkel.pipez.blocks;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;

public class ModBlocks {

    public static final ItemPipeBlock ITEM_PIPE = new ItemPipeBlock();
    public static final FluidPipeBlock FLUID_PIPE = new FluidPipeBlock();

    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(
                ITEM_PIPE,
                FLUID_PIPE
        );
    }

    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
                ITEM_PIPE.toItem(),
                FLUID_PIPE.toItem()
        );
    }

}
