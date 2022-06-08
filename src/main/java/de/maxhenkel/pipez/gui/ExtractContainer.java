package de.maxhenkel.pipez.gui;

import de.maxhenkel.corelib.inventory.ContainerBase;
import de.maxhenkel.pipez.blocks.tileentity.PipeLogicTileEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;

public class ExtractContainer extends ContainerBase implements IPipeContainer {

    private PipeLogicTileEntity pipe;
    private Direction side;
    private int index;

    public ExtractContainer(int id, Container playerInventory, PipeLogicTileEntity pipe, Direction side, int index) {
        super(Containers.EXTRACT.get(), id, playerInventory, null);
        this.pipe = pipe;
        this.side = side;
        this.index = index;

        addSlot(new UpgradeSlot(pipe.getUpgradeInventory(), side.get3DDataValue(), 9, 81));

        addPlayerInventorySlots();
    }

    @Override
    public PipeLogicTileEntity getPipe() {
        return pipe;
    }

    @Override
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
    public boolean stillValid(Player player) {
        return !pipe.isRemoved();
    }
}
