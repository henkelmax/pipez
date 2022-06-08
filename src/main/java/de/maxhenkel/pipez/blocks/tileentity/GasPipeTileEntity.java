package de.maxhenkel.pipez.blocks.tileentity;

import de.maxhenkel.pipez.blocks.tileentity.types.GasPipeType;
import de.maxhenkel.pipez.blocks.tileentity.types.PipeType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class GasPipeTileEntity extends PipeLogicTileEntity {

    public GasPipeTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntities.GAS_PIPE.get(), new PipeType[]{GasPipeType.INSTANCE}, pos, state);
    }

}
