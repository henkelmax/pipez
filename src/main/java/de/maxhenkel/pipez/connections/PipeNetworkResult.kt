package de.maxhenkel.pipez.connections

import de.maxhenkel.pipez.blocks.tileentity.PipeTileEntity
import de.maxhenkel.pipez.types.AbsoluteDirection
import de.maxhenkel.pipez.types.IntPos
import de.maxhenkel.pipez.types.PipeSideConnection

data class PipeNetworkResult(
    val destinations:List<AbsoluteDirection>,
    val connections:Map<IntPos, PipeSideConnection>,
    val pipeTiles:List<IntPos>,
)