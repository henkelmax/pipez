package de.maxhenkel.pipez.blocks.tileentity;

import de.maxhenkel.pipez.blocks.tileentity.types.EnergyPipeType;
import de.maxhenkel.pipez.blocks.tileentity.types.PipeType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class EnergyPipeTileEntity extends PipeLogicTileEntity {

    public EnergyPipeTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntities.ENERGY_PIPE, new PipeType[]{EnergyPipeType.Companion.getINSTANCE()}, pos, state);
    }

}
