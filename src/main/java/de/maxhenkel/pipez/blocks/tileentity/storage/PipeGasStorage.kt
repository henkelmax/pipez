package de.maxhenkel.pipez.blocks.tileentity.storage

import de.maxhenkel.pipez.Main
import de.maxhenkel.pipez.blocks.tileentity.PipeLogicTileEntity
import de.maxhenkel.pipez.blocks.tileentity.types.EnergyPipeType
import de.maxhenkel.pipez.blocks.tileentity.types.GasPipeType
import de.maxhenkel.pipez.blocks.tileentity.types.ItemPipeType
import de.maxhenkel.pipez.types.PipeSide
import mekanism.api.Action
import mekanism.api.chemical.gas.GasStack
import mekanism.api.chemical.gas.IGasHandler
import net.minecraft.core.Direction
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.IFluidHandler
import kotlin.math.max

class PipeGasStorage(pipe: PipeLogicTileEntity, side: Direction) : PullPushStorage(pipe, side), IGasHandler {
    override fun tick() {
        super.tick()
    }

    override fun pull() {
        GasPipeType.INSTANCE.ejectMaterial(pipe, side)
    }

    override fun getTanks(): Int {
        return 1
    }

    override fun getChemicalInTank(tank: Int): GasStack {
        return GasStack.EMPTY
    }

    override fun setChemicalInTank(tank: Int, stack: GasStack) {
        Main.LOGGER.error("Setting chemical in pipe does not supported!")
    }

    override fun getTankCapacity(tank: Int): Long {
        return Long.MAX_VALUE // is this alright...?
    }

    override fun isValid(tank: Int, stack: GasStack): Boolean {
        return true
    }

    override fun insertChemical(tank: Int, stack: GasStack, action: Action): GasStack {
        val result = GasPipeType.INSTANCE.receiveMaterial(
            pipe, PipeSide.fromDirection(side), stack, simulate = when (action) {
                Action.SIMULATE -> true
                Action.EXECUTE -> false
            }
        )
        val amount = max(0, stack.amount - result)
        if (amount > 0) {
            updatePushed()
        }
        return GasStack(stack, amount)
    }

    override fun extractChemical(tank: Int, amount: Long, action: Action): GasStack {
        return GasStack.EMPTY
    }
}