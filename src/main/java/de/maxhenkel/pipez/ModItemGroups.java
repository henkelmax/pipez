package de.maxhenkel.pipez;

import de.maxhenkel.pipez.blocks.ModBlocks;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModItemGroups {

    public static final CreativeModeTab TAB_PIPEZ = new CreativeModeTab("pipez") {

        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ModBlocks.ITEM_PIPE.get());
        }

    };

}
