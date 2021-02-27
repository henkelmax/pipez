package de.maxhenkel.pipez.blocks.tileentity;

import de.maxhenkel.pipez.blocks.tileentity.types.*;
import net.minecraftforge.fml.ModList;

public class UniversalPipeTileEntity extends PipeLogicTileEntity {

    public UniversalPipeTileEntity() {
        super(ModTileEntities.UNIVERSAL_PIPE, ModList.get().isLoaded("mekanism") ? new PipeType[]{ItemPipeType.INSTANCE, FluidPipeType.INSTANCE, EnergyPipeType.INSTANCE, GasPipeType.INSTANCE} : new PipeType[]{ItemPipeType.INSTANCE, FluidPipeType.INSTANCE, EnergyPipeType.INSTANCE});
    }

}
