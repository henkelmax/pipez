package de.maxhenkel.pipez

import de.maxhenkel.pipez.blocks.ModBlocks
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.ItemStack

object ModItemGroups {
    val TAB_PIPEZ = object : CreativeModeTab(Main.MODID) {
        override fun makeIcon(): ItemStack {
            return ItemStack(ModBlocks.ITEM_PIPE)
        }
    }
}