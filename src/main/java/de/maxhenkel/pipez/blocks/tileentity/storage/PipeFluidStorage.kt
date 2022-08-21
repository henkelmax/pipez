package de.maxhenkel.pipez.blocks.tileentity.storage

import de.maxhenkel.pipez.blocks.tileentity.PipeLogicTileEntity
import de.maxhenkel.pipez.blocks.tileentity.types.FluidPipeType
import de.maxhenkel.pipez.types.PipeSide
import net.minecraft.core.Direction
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.IFluidHandler

class PipeFluidStorage(pipe: PipeLogicTileEntity, side: Direction) : PullPushStorage(pipe, side), IFluidHandler {
    override fun tick() {
        super.tick()
    }

    override fun pull() {
        FluidPipeType.INSTANCE.ejectMaterial(pipe, side)
    }

    override fun getTanks(): Int {
        return 1
    }

    override fun getFluidInTank(tank: Int): FluidStack {
        return FluidStack.EMPTY
    }

    override fun getTankCapacity(tank: Int): Int {
        return Int.MAX_VALUE
    }

    override fun isFluidValid(tank: Int, stack: FluidStack): Boolean {
        return true
    }

    override fun fill(resource: FluidStack, action: IFluidHandler.FluidAction): Int {
        updatePushed()
        return FluidPipeType.INSTANCE.receiveMaterial(
            pipe, PipeSide.fromDirection(side), resource, simulate = when (action) {
                IFluidHandler.FluidAction.EXECUTE -> false
                IFluidHandler.FluidAction.SIMULATE -> true
            }).toInt()
    }

    override fun drain(resource: FluidStack?, action: IFluidHandler.FluidAction?): FluidStack {
        return FluidStack.EMPTY
    }

    override fun drain(maxDrain: Int, action: IFluidHandler.FluidAction?): FluidStack {
        return FluidStack.EMPTY
    }

}