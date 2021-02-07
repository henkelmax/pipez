package de.maxhenkel.pipez.gui;

import de.maxhenkel.pipez.blocks.tileentity.UpgradeTileEntity;
import de.maxhenkel.corelib.inventory.ContainerBase;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.Direction;

public class ExtractContainer extends ContainerBase {

    public ExtractContainer(int id, IInventory playerInventory, UpgradeTileEntity pipe, Direction direction) {
        super(Containers.EXTRACT, id, playerInventory, null);

        addSlot(new UpgradeSlot(pipe.getUpgradeInventory(), direction.getIndex(), 80, 18));

        addPlayerInventorySlots();
    }

    @Override
    public int getInventorySize() {
        return 1;
    }

    @Override
    public int getInvOffset() {
        return -12;
    }
}
