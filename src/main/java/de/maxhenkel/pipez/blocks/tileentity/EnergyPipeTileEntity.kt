package de.maxhenkel.pipez.blocks.tileentity

import de.maxhenkel.pipez.blocks.tileentity.types.EnergyPipeType
import de.maxhenkel.pipez.blocks.tileentity.types.FluidPipeType
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockState

class EnergyPipeTileEntity(
    pos: BlockPos,
    state: BlockState,
) : PipeLogicTileEntity(ModTileEntities.ENERGY_PIPE, arrayOf(EnergyPipeType.INSTANCE), pos, state)