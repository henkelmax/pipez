package de.maxhenkel.pipez.gui;

import de.maxhenkel.corelib.inventory.ContainerBase;
import de.maxhenkel.pipez.blocks.tileentity.UpgradeTileEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.Direction;

public class ExtractContainer extends ContainerBase {

    private UpgradeTileEntity pipe;
    private Direction side;

    public ExtractContainer(int id, IInventory playerInventory, UpgradeTileEntity pipe, Direction side) {
        super(Containers.EXTRACT, id, playerInventory, null);
        this.pipe = pipe;
        this.side = side;

        addSlot(new UpgradeSlot(pipe.getUpgradeInventory(), side.getIndex(), 9, 81));

        addPlayerInventorySlots();
    }

    public UpgradeTileEntity getPipe() {
        return pipe;
    }

    public Direction getSide() {
        return side;
    }

    @Override
    public int getInventorySize() {
        return 1;
    }

    @Override
    public int getInvOffset() {
        return 30;
    }
}
