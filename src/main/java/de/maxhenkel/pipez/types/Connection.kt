package de.maxhenkel.pipez.types

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction

data class Connection(
    val pos:BlockPos,
    val direction: Direction,
    val distance: Int,
)