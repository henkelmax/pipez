package de.maxhenkel.pipez.blocks.tileentity.storage

import de.maxhenkel.pipez.blocks.tileentity.PipeLogicTileEntity
import de.maxhenkel.pipez.blocks.tileentity.types.GasPipeType
import de.maxhenkel.pipez.blocks.tileentity.types.ItemPipeType
import de.maxhenkel.pipez.types.PipeSide
import mekanism.api.Action
import mekanism.api.chemical.gas.GasStack
import mekanism.api.chemical.gas.IGasHandler
import net.minecraft.core.Direction
import net.minecraft.world.item.ItemStack
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.IFluidHandler
import net.minecraftforge.items.IItemHandler
import kotlin.math.max

class PipeItemStorage(pipe: PipeLogicTileEntity, side: Direction) : PullPushStorage(pipe, side), IItemHandler {

    override fun tick() {
        super.tick()
    }

    override fun pull() {
        ItemPipeType.INSTANCE.ejectMaterial(pipe, side)
    }

    override fun getSlots(): Int {
        return 1
    }

    override fun getStackInSlot(slot: Int): ItemStack {
        return ItemStack.EMPTY
    }

    override fun insertItem(slot: Int, stack: ItemStack, simulate: Boolean): ItemStack {
        val amounts = stack.count
        val result = ItemPipeType.INSTANCE.receiveMaterial(
            pipe, PipeSide.fromDirection(side), stack, simulate
        )
        val left = amounts - result.toInt()
        return if (left <= 0) {
            ItemStack.EMPTY
        } else {
            updatePushed()
            ItemStack(stack.item, left)
        }
    }

    override fun extractItem(slot: Int, amount: Int, simulate: Boolean): ItemStack {
        return ItemStack.EMPTY
    }

    override fun getSlotLimit(slot: Int): Int {
        return Int.MAX_VALUE
    }

    override fun isItemValid(slot: Int, stack: ItemStack): Boolean {
        return true
    }

}