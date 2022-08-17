package de.maxhenkel.pipez.types

import net.minecraft.core.BlockPos

data class IntPos(
    val x:Int,
    val y:Int,
    val z:Int,
) {
    fun toBlockPos(): BlockPos {
        return BlockPos(x, y, z)
    }
}