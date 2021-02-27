package de.maxhenkel.pipez.blocks.tileentity;

import de.maxhenkel.pipez.blocks.tileentity.types.GasPipeType;
import de.maxhenkel.pipez.blocks.tileentity.types.PipeType;

public class GasPipeTileEntity extends PipeLogicTileEntity {

    public GasPipeTileEntity() {
        super(ModTileEntities.GAS_PIPE, new PipeType[]{GasPipeType.INSTANCE});
    }

}
