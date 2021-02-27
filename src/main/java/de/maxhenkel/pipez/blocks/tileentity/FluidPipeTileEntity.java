package de.maxhenkel.pipez.blocks.tileentity;

import de.maxhenkel.pipez.blocks.tileentity.types.FluidPipeType;
import de.maxhenkel.pipez.blocks.tileentity.types.PipeType;

public class FluidPipeTileEntity extends PipeLogicTileEntity {

    public FluidPipeTileEntity() {
        super(ModTileEntities.FLUID_PIPE, new PipeType[]{FluidPipeType.INSTANCE});
    }

}
