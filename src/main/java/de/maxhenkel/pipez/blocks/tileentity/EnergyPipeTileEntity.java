package de.maxhenkel.pipez.blocks.tileentity;

import de.maxhenkel.pipez.blocks.tileentity.types.EnergyPipeType;
import de.maxhenkel.pipez.blocks.tileentity.types.PipeType;

public class EnergyPipeTileEntity extends PipeLogicTileEntity {

    public EnergyPipeTileEntity() {
        super(ModTileEntities.ENERGY_PIPE, new PipeType[]{EnergyPipeType.INSTANCE});
    }

}
