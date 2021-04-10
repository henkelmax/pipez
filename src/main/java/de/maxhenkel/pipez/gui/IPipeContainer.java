package de.maxhenkel.pipez.gui;

import de.maxhenkel.pipez.blocks.tileentity.PipeLogicTileEntity;
import net.minecraft.util.Direction;

public interface IPipeContainer {

    PipeLogicTileEntity getPipe();

    Direction getSide();

}
