package de.maxhenkel.pipez.gui;

import de.maxhenkel.corelib.inventory.ContainerBase;
import de.maxhenkel.pipez.Filter;
import de.maxhenkel.pipez.blocks.tileentity.UpgradeTileEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.Direction;

public class FilterContainer extends ContainerBase {

    private UpgradeTileEntity pipe;
    private Direction side;
    private Filter<?> filter;

    public FilterContainer(int id, IInventory playerInventory, UpgradeTileEntity pipe, Direction side, Filter<?> filter) {
        super(Containers.FILTER, id, playerInventory, null);
        this.pipe = pipe;
        this.side = side;
        this.filter = filter;

        addPlayerInventorySlots();
    }

    public UpgradeTileEntity getPipe() {
        return pipe;
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
}
