package de.maxhenkel.pipez.blocks.tileentity;

import de.maxhenkel.pipez.blocks.tileentity.types.PipeType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;

public abstract class PipeLogicTileEntity extends UpgradeTileEntity {

    protected PipeType<?>[] types;
    protected final int[][] rrIndex;

    public PipeLogicTileEntity(TileEntityType<?> tileEntityTypeIn, PipeType<?>[] types) {
        super(tileEntityTypeIn);
        this.types = types;
        rrIndex = new int[Direction.values().length][types.length];
    }

    public int getRoundRobinIndex(Direction direction, PipeType<?> pipeType) {
        return rrIndex[direction.getIndex()][getIndex(pipeType)];
    }

    public void setRoundRobinIndex(Direction direction, PipeType<?> pipeType, int value) {
        rrIndex[direction.getIndex()][getIndex(pipeType)] = value;
    }

    public boolean isEnabled(Direction side, PipeType<?> pipeType) {
        UpgradeTileEntity.RedstoneMode redstoneMode = getRedstoneMode(side, pipeType);
        return redstoneMode != UpgradeTileEntity.RedstoneMode.ALWAYS_OFF;
    }

    public int getPreferredPipeIndex(Direction side) {
        for (int i = 0; i < types.length; i++) {
            if (isEnabled(side, types[i])) {
                return i;
            }
        }
        return 0;
    }

    public boolean shouldWork(Direction side, PipeType<?> pipeType) {
        RedstoneMode redstoneMode = getRedstoneMode(side, pipeType);
        if (redstoneMode.equals(RedstoneMode.ALWAYS_OFF)) {
            return false;
        } else if (redstoneMode.equals(RedstoneMode.OFF_WHEN_POWERED)) {
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

    public PipeType<?>[] getPipeTypes() {
        return types;
    }

    public int getIndex(PipeType<?> pipeType) {
        for (int i = 0; i < getPipeTypes().length; i++) {
            PipeType<?> type = getPipeTypes()[i];
            if (type == pipeType) {
                return i;
            }
        }
        return 0;
    }

    @Override
    public void tick() {
        super.tick();

        if (world.isRemote) {
            return;
        }

        for (PipeType<?> type : getPipeTypes()) {
            type.tick(this);
        }
    }

    @Override
    public boolean canInsert(TileEntity tileEntity, Direction direction) {
        for (PipeType<?> type : types) {
            if (type.canInsert(tileEntity, direction)) {
                return true;
            }
        }
        return false;
    }
}
