package de.maxhenkel.pipez.gui;

import de.maxhenkel.corelib.inventory.ContainerBase;
import de.maxhenkel.pipez.Filter;
import de.maxhenkel.pipez.blocks.tileentity.PipeLogicTileEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;

public class FilterContainer extends ContainerBase implements IPipeContainer {

    private PipeLogicTileEntity pipe;
    private Direction side;
    private int index;
    private Filter<?, ?> filter;

    public FilterContainer(int id, Container playerInventory, PipeLogicTileEntity pipe, Direction side, int index, Filter<?, ?> filter) {
        super(Containers.FILTER.get(), id, playerInventory, null);
        this.pipe = pipe;
        this.side = side;
        this.index = index;
        this.filter = filter;

        addPlayerInventorySlots();
    }

    @Override
    public PipeLogicTileEntity getPipe() {
        return pipe;
    }

    public int getIndex() {
        return index;
    }

    public Filter<?, ?> getFilter() {
        return filter;
    }

    @Override
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
    public boolean stillValid(Player player) {
        return !pipe.isRemoved();
    }

}
