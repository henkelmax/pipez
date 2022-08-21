package de.maxhenkel.pipez.blocks.tileentity

import de.maxhenkel.pipez.Main
import de.maxhenkel.pipez.blocks.tileentity.types.EnergyPipeType
import de.maxhenkel.pipez.blocks.tileentity.types.FluidPipeType
import de.maxhenkel.pipez.blocks.tileentity.types.GasPipeType
import de.maxhenkel.pipez.blocks.tileentity.types.ItemPipeType
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockState
import net.minecraftforge.fml.ModList

class UniversalPipeTileEntity(
    pos:BlockPos,
    state:BlockState,
) : PipeLogicTileEntity(
    ModTileEntities.UNIVERSAL_PIPE, if (Main.isMekanismLoaded()) {
        arrayOf(ItemPipeType.INSTANCE, FluidPipeType.INSTANCE, EnergyPipeType.INSTANCE, GasPipeType.INSTANCE)
    } else {
        arrayOf(ItemPipeType.INSTANCE, FluidPipeType.INSTANCE, EnergyPipeType.INSTANCE)
    }, pos, state
)