package de.maxhenkel.pipez;

import de.maxhenkel.pipez.blocks.ModBlocks;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class ModItemGroups {

    public static final ItemGroup TAB_PIPEZ = new ItemGroup("pipez") {

        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ModBlocks.ITEM_PIPE);
        }

    };

}
