package de.maxhenkel.pipez.connections

import de.maxhenkel.pipez.Main
import de.maxhenkel.pipez.blocks.tileentity.PipeTileEntity
import de.maxhenkel.pipez.types.IntPos
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level

class PipeNetworkQueue {
    companion object {
        val INSTANCE = PipeNetworkQueue()
    }

    private val queues:HashMap<Level, MutableList<BlockPos>> = hashMapOf()
    fun enqueuePipeBlockChanged(world: Level, pos: BlockPos, highPriority: Boolean = false) {
        val queue = queues.computeIfAbsent(world) { arrayListOf() }
        if (highPriority) {
            queue.add(0, pos)
        } else {
            queue.add(pos)
        }
    }
    fun taskTick() {
        val levels = queues.keys.toSet()
        for (level in levels) {
            val originalQueue = queues.computeIfAbsent(level) { arrayListOf() }
            if (originalQueue.isEmpty()) {
                continue
            }
            val queue = originalQueue.toList()
            originalQueue.clear()

            val completedPipes = arrayListOf<IntPos>()

            for (pos in queue) {
                if (level.getBlockEntity(BlockPos(pos.x, pos.y, pos.z)) !is PipeTileEntity) {
                    if (completedPipes.any {it.x == pos.x && it.z == pos.z && it.y == pos.y}) {
                        continue
                    }
                }
                val result = PipeNetworkManager.updatePipeNetworkAt(level, pos)
                result?.pipeTiles?.also {
                    completedPipes.addAll(it)
                }
            }
        }
    }

    fun clear() {
        this.queues.clear()
    }
}