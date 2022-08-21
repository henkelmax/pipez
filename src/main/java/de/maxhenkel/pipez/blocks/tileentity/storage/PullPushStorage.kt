package de.maxhenkel.pipez.blocks.tileentity.storage

import de.maxhenkel.pipez.Main
import de.maxhenkel.pipez.blocks.tileentity.PipeLogicTileEntity
import de.maxhenkel.pipez.blocks.tileentity.types.EnergyPipeType
import net.minecraft.core.Direction
import kotlin.math.abs

abstract class PullPushStorage(protected val pipe: PipeLogicTileEntity, protected val side: Direction) {
    protected open var lastReceived = 0L

    protected open var tickCount = 0L

    open fun tick() {
        if (tickCount > lastReceived + 20) {
            pull()
        }
        tickCount += 1
    }

    abstract fun pull()

    protected open fun updatePushed() {
        lastReceived = tickCount
    }
}