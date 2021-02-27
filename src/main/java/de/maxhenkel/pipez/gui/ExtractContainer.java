package de.maxhenkel.pipez.gui;

import de.maxhenkel.corelib.inventory.ContainerBase;
import de.maxhenkel.pipez.blocks.tileentity.PipeLogicTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.Direction;

public class ExtractContainer extends ContainerBase {

    private PipeLogicTileEntity pipe;
    private Direction side;
    private int index;

    public ExtractContainer(int id, IInventory playerInventory, PipeLogicTileEntity pipe, Direction side, int index) {
        super(Containers.EXTRACT, id, playerInventory, null);
        this.pipe = pipe;
        this.side = side;
        this.index = index;

        addSlot(new UpgradeSlot(pipe.getUpgradeInventory(), side.getIndex(), 9, 81));

        addPlayerInventorySlots();
    }

    public PipeLogicTileEntity getPipe() {
        return pipe;
    }

    public Direction getSide() {
        return side;
    }

    /**
     * This value is -1 if no specific tab was selected
     *
     * @return the pipeType index
     */
    public int getIndex() {
        return index;
    }

    @Override
    public int getInventorySize() {
        return 1;
    }

    @Override
    public int getInvOffset() {
        return 30;
    }

    @Override
    public boolean canInteractWith(PlayerEntity player) {
        return !pipe.isRemoved();
    }
}
