package de.maxhenkel.pipez.utils;

import de.maxhenkel.pipez.blocks.tileentity.PipeLogicTileEntity;
import de.maxhenkel.pipez.blocks.tileentity.types.EnergyPipeType;
import net.minecraft.util.Direction;
import net.minecraftforge.energy.IEnergyStorage;

public class PipeEnergyStorage implements IEnergyStorage {

    protected PipeLogicTileEntity pipe;
    protected Direction side;
    protected long lastReceived;

    public PipeEnergyStorage(PipeLogicTileEntity pipe, Direction side) {
        this.pipe = pipe;
        this.side = side;
    }

    public void tick() {
        if (pipe.getLevel().getGameTime() - lastReceived > 1) {
            EnergyPipeType.INSTANCE.pullEnergy(pipe, side);
        }
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        lastReceived = pipe.getLevel().getGameTime();
        return EnergyPipeType.INSTANCE.receive(pipe, side, maxReceive, simulate);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return 0;
    }

    @Override
    public int getMaxEnergyStored() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return true;
    }
}
