package de.maxhenkel.pipez.types

import net.minecraft.core.Direction

enum class PipeSide(val index:Int) {
    DOWN(0),
    UP(1),
    NORTH(2),
    SOUTH(3),
    WEST(4),
    EAST(5);

    fun toDirection(): Direction {
        return when (this) {
            UP -> Direction.UP
            DOWN -> Direction.DOWN
            NORTH -> Direction.NORTH
            SOUTH -> Direction.SOUTH
            EAST -> Direction.EAST
            WEST -> Direction.WEST
        }
    }

    companion object {
        fun fromDirection(direction: Direction): PipeSide {
            return when (direction) {
                Direction.UP -> UP
                Direction.DOWN -> DOWN
                Direction.NORTH -> NORTH
                Direction.SOUTH -> SOUTH
                Direction.EAST -> EAST
                Direction.WEST -> WEST
            }
        }
    }
}