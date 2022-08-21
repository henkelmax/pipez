package de.maxhenkel.pipez.blocks.tileentity

import de.maxhenkel.pipez.blocks.tileentity.types.FluidPipeType
import de.maxhenkel.pipez.blocks.tileentity.types.GasPipeType
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockState

class GasPipeTileEntity(
    pos: BlockPos,
    state: BlockState,
) : PipeLogicTileEntity(ModTileEntities.GAS_PIPE, arrayOf(GasPipeType.INSTANCE), pos, state)