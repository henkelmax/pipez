package de.maxhenkel.pipez.types

import net.minecraft.core.Direction

data class PipeSideConnection(
    val up: Boolean = false,
    val down: Boolean = false,
    val north: Boolean = false,
    val south: Boolean = false,
    val west: Boolean = false,
    val east: Boolean = false,
) {
    fun isConnected(side: Direction): Boolean {
        return isConnected(PipeSide.fromDirection(side))
    }
    fun isConnected(side: PipeSide): Boolean {
        return when (side) {
            PipeSide.UP -> up
            PipeSide.DOWN -> down
            PipeSide.NORTH -> north
            PipeSide.SOUTH -> south
            PipeSide.WEST -> west
            PipeSide.EAST -> east
        }
    }
}