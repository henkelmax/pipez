package de.maxhenkel.pipez.gui;

import de.maxhenkel.corelib.inventory.ContainerBase;
import de.maxhenkel.pipez.blocks.tileentity.PipeLogicTileEntity;
import de.maxhenkel.pipez.gui.sprite.ExtractUISprite;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

public class ExtractContainer extends ContainerBase implements IPipeContainer {

    private PipeLogicTileEntity pipe;
    private Direction side;
    private int index;

    public ExtractContainer(int id, Container playerInventory, PipeLogicTileEntity pipe, Direction side, int index) {
        super(Containers.EXTRACT.get(), id, playerInventory, null);
        this.pipe = pipe;
        this.side = side;
        this.index = index;

        addSlot(new UpgradeSlot(pipe.getUpgradeInventory(), side.get3DDataValue(), ExtractUISprite.UPGRADE_SLOT.x, ExtractUISprite.UPGRADE_SLOT.y));

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
    protected void addPlayerInventorySlots() {
        if (playerInventory != null) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 9; j++) {
                    addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18 + ExtractUISprite.INVENTORY_OFFSET.x, 84 + i * 18 + ExtractUISprite.INVENTORY_OFFSET.y));
                }
            }

            for (int k = 0; k < 9; k++) {
                addSlot(new Slot(playerInventory, k, 8 + k * 18 + ExtractUISprite.INVENTORY_OFFSET.x, 142 + ExtractUISprite.INVENTORY_OFFSET.y));
            }
        }
    }

    @Override
    public int getInvOffset() {
        return ExtractUISprite.INVENTORY_OFFSET.y;
    }

    @Override
    public boolean stillValid(Player player) {
        return !pipe.isRemoved();
    }
}
