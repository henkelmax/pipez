package de.maxhenkel.pipez.blocks.tileentity;

import de.maxhenkel.pipez.blocks.tileentity.types.ItemPipeType;
import de.maxhenkel.pipez.blocks.tileentity.types.PipeType;

public class ItemPipeTileEntity extends PipeLogicTileEntity {

    public ItemPipeTileEntity() {
        super(ModTileEntities.ITEM_PIPE, new PipeType[]{ItemPipeType.INSTANCE});
    }
}
