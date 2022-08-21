package de.maxhenkel.pipez.blocks.tileentity

import de.maxhenkel.pipez.blocks.tileentity.types.FluidPipeType
import de.maxhenkel.pipez.blocks.tileentity.types.GasPipeType
import de.maxhenkel.pipez.blocks.tileentity.types.ItemPipeType
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockState

class ItemPipeTileEntity(
    pos: BlockPos,
    state: BlockState,
) : PipeLogicTileEntity(ModTileEntities.ITEM_PIPE, arrayOf(ItemPipeType.INSTANCE), pos, state)