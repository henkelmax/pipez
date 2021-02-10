package de.maxhenkel.pipez.blocks.tileentity;

import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;

public abstract class UpgradeLogicTileEntity<T> extends UpgradeTileEntity<T> {

    public UpgradeLogicTileEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    public boolean shouldWork(Direction side) {
        RedstoneMode redstoneMode = getRedstoneMode(side);
        if (redstoneMode.equals(RedstoneMode.OFF_WHEN_POWERED)) {
            return !isRedstonePowered();
        } else if (redstoneMode.equals(RedstoneMode.ON_WHEN_POWERED)) {
            return isRedstonePowered();
        } else {
            return true;
        }
    }

    public boolean isRedstonePowered() {
        return world.isBlockPowered(pos);
    }

}
