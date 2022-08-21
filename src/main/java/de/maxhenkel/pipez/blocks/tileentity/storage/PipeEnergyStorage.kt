package de.maxhenkel.pipez.blocks.tileentity.storage

import de.maxhenkel.pipez.blocks.tileentity.PipeLogicTileEntity
import de.maxhenkel.pipez.blocks.tileentity.types.EnergyPipeType
import de.maxhenkel.pipez.blocks.tileentity.types.FluidPipeType
import de.maxhenkel.pipez.types.EnergyMaterial
import de.maxhenkel.pipez.types.PipeSide
import net.minecraft.core.Direction
import net.minecraftforge.energy.IEnergyStorage
import kotlin.math.abs

class PipeEnergyStorage(pipe: PipeLogicTileEntity, side: Direction) : PullPushStorage(pipe, side), IEnergyStorage {
    override fun tick() {
        super.tick()
    }

    override fun pull() {
        EnergyPipeType.INSTANCE.ejectMaterial(pipe, side)
    }

    override fun receiveEnergy(maxReceive: Int, simulate: Boolean): Int {
        updatePushed()
        return EnergyPipeType.INSTANCE.receiveMaterial(
            pipe, PipeSide.fromDirection(side), EnergyMaterial(maxReceive), simulate).toInt()
    }

    override fun extractEnergy(maxExtract: Int, simulate: Boolean): Int {
        return 0
    }

    override fun getEnergyStored(): Int {
        return 0
    }

    override fun getMaxEnergyStored(): Int {
        return Int.MAX_VALUE
    }

    override fun canExtract(): Boolean {
        return false
    }

    override fun canReceive(): Boolean {
        return true
    }
}