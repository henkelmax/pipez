package de.maxhenkel.pipez.blocks.tileentity

import de.maxhenkel.pipez.blocks.tileentity.types.FluidPipeType
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockState

class FluidPipeTileEntity(
    pos: BlockPos,
    state: BlockState,
) : PipeLogicTileEntity(ModTileEntities.FLUID_PIPE, arrayOf(FluidPipeType.INSTANCE), pos, state)