package de.maxhenkel.pipez.blocks.tileentity;

import de.maxhenkel.pipez.blocks.tileentity.types.EnergyPipeType;
import de.maxhenkel.pipez.blocks.tileentity.types.FluidPipeType;
import de.maxhenkel.pipez.blocks.tileentity.types.ItemPipeType;
import de.maxhenkel.pipez.blocks.tileentity.types.PipeType;
import de.maxhenkel.pipez.utils.DummyFluidHandler;
import de.maxhenkel.pipez.utils.DummyItemHandler;
import de.maxhenkel.pipez.utils.PipeEnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;

import javax.annotation.Nullable;

public abstract class PipeLogicTileEntity extends UpgradeTileEntity {

    protected PipeType<?, ?>[] types;
    protected final int[][] rrIndex;

    protected PipeEnergyStorage[] energyStorages;

    private int recursionDepth;

    public PipeLogicTileEntity(BlockEntityType<?> tileEntityTypeIn, PipeType<?, ?>[] types, BlockPos pos, BlockState state) {
        super(tileEntityTypeIn, pos, state);
        this.types = types;
        rrIndex = new int[Direction.values().length][types.length];
        energyStorages = new PipeEnergyStorage[Direction.values().length];
    }

    @Nullable
    public <T> T onRegisterCapability(BlockCapability<T, Direction> capability, @Nullable Direction side) {
        if (side == null) {
            return null;
        }
        if (!isExtracting(side)) {
            return null;
        }
        if (capability == Capabilities.EnergyStorage.BLOCK && hasType(EnergyPipeType.INSTANCE)) {
            if (side != null) {
                if (energyStorages[side.get3DDataValue()] == null) {
                    energyStorages[side.get3DDataValue()] = new PipeEnergyStorage(this, side);
                }
                return (T) energyStorages[side.get3DDataValue()];
            }
        } else if (capability == Capabilities.FluidHandler.BLOCK && hasType(FluidPipeType.INSTANCE)) {
            if (side != null) {
                return (T) DummyFluidHandler.INSTANCE;
            }
        } else if (capability == Capabilities.ItemHandler.BLOCK && hasType(ItemPipeType.INSTANCE)) {
            if (side != null) {
                return (T) DummyItemHandler.INSTANCE;
            }
        }
        return null;
    }

    public boolean hasType(PipeType<?, ?> type) {
        for (PipeType<?, ?> t : types) {
            if (t == type) {
                return true;
            }
        }
        return false;
    }

    public int getRoundRobinIndex(Direction direction, PipeType<?, ?> pipeType) {
        return rrIndex[direction.get3DDataValue()][getIndex(pipeType)];
    }

    public void setRoundRobinIndex(Direction direction, PipeType<?, ?> pipeType, int value) {
        rrIndex[direction.get3DDataValue()][getIndex(pipeType)] = value;
    }

    public boolean isEnabled(Direction side, PipeType<?, ?> pipeType) {
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

    public boolean shouldWork(Direction side, PipeType<?, ?> pipeType) {
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
        return level.hasNeighborSignal(worldPosition);
    }

    public PipeType<?, ?>[] getPipeTypes() {
        return types;
    }

    public int getIndex(PipeType<?, ?> pipeType) {
        for (int i = 0; i < getPipeTypes().length; i++) {
            PipeType<?, ?> type = getPipeTypes()[i];
            if (type == pipeType) {
                return i;
            }
        }
        return 0;
    }

    @Override
    public void tick() {
        super.tick();

        if (level.isClientSide) {
            return;
        }

        for (PipeType<?, ?> type : getPipeTypes()) {
            type.tick(this);
        }

        if (hasType(EnergyPipeType.INSTANCE)) {
            for (Direction side : Direction.values()) {
                if (isExtracting(side)) {
                    if (energyStorages[side.get3DDataValue()] != null) {
                        energyStorages[side.get3DDataValue()].tick();
                    }
                }
            }
        }
    }

    public void invalidateCapabilities() {
        if (level != null) {
            level.invalidateCapabilities(worldPosition);
        }
        if (!hasType(EnergyPipeType.INSTANCE)) {
            return;
        }
        for (Direction dir : Direction.values()) {
            if (!isExtracting(dir)) {
                //TODO Check if this causes issues when reloading or other edge cases
                energyStorages[dir.get3DDataValue()] = null;
            }
        }
    }

    @Override
    public void setExtracting(Direction side, boolean extracting) {
        super.setExtracting(side, extracting);
        invalidateCapabilities();
    }

    @Override
    protected void loadAdditional(ValueInput valueInput) {
        super.loadAdditional(valueInput);
        invalidateCapabilities();
    }

    @Override
    public void setRemoved() {
        invalidateCapabilities();
        super.setRemoved();
    }

    @Override
    public boolean canInsert(Level level, Connection connection) {
        for (PipeType<?, ?> type : types) {
            for (BlockCapability<?, Direction> provider : type.getCapabilities()) {
                if (connection.getCapability(provider) != null) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean pushRecursion() {
        if (recursionDepth >= 1) {
            return true;
        }
        recursionDepth++;
        return false;
    }

    public void popRecursion() {
        recursionDepth--;
    }

}
