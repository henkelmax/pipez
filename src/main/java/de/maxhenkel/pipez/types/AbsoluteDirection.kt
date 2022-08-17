package de.maxhenkel.pipez.types

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level

data class AbsoluteDirection(
    val dimensionName: String,
    val position: IntPos,
    val direction: Direction,
) {
    companion object {
        fun from(level: Level, position: BlockPos, direction: Direction):AbsoluteDirection {
            return AbsoluteDirection(
                dimensionName = level.dimension().location().path,
                position = IntPos(position.x, position.y, position.z),
                direction,
            )
        }
    }
}