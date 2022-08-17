package de.maxhenkel.pipez.blocks.tileentity;

import de.maxhenkel.pipez.blocks.tileentity.types.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.ModList;

public class UniversalPipeTileEntity extends PipeLogicTileEntity {

    public UniversalPipeTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntities.UNIVERSAL_PIPE, ModList.get().isLoaded("mekanism") ? new PipeType[]{ItemPipeType.INSTANCE, FluidPipeType.INSTANCE, EnergyPipeType.Companion.getINSTANCE(), GasPipeType.INSTANCE} : new PipeType[]{ItemPipeType.INSTANCE, FluidPipeType.INSTANCE, EnergyPipeType.Companion.getINSTANCE()}, pos, state);
    }

}
