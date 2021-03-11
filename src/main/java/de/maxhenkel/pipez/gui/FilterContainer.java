package de.maxhenkel.pipez.gui;

import de.maxhenkel.corelib.inventory.ContainerBase;
import de.maxhenkel.pipez.Filter;
import de.maxhenkel.pipez.blocks.tileentity.PipeLogicTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.Direction;

public class FilterContainer extends ContainerBase {

    private PipeLogicTileEntity pipe;
    private Direction side;
    private int index;
    private Filter<?> filter;

    public FilterContainer(int id, IInventory playerInventory, PipeLogicTileEntity pipe, Direction side, int index, Filter<?> filter) {
        super(Containers.FILTER, id, playerInventory, null);
        this.pipe = pipe;
        this.side = side;
        this.index = index;
        this.filter = filter;

        addPlayerInventorySlots();
    }

    public PipeLogicTileEntity getPipe() {
        return pipe;
    }

    public int getIndex() {
        return index;
    }

    public Filter<?> getFilter() {
        return filter;
    }

    public Direction getSide() {
        return side;
    }

    @Override
    public int getInventorySize() {
        return 0;
    }

    @Override
    public int getInvOffset() {
        return 56;
    }

    @Override
    public boolean stillValid(PlayerEntity player) {
        return !pipe.isRemoved();
    }

}
