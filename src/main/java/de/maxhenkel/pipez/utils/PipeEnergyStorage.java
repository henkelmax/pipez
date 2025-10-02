package de.maxhenkel.pipez.utils;

import de.maxhenkel.pipez.blocks.tileentity.PipeLogicTileEntity;
import de.maxhenkel.pipez.blocks.tileentity.types.EnergyPipeType;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class PipeEnergyStorage implements EnergyHandler {

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
    public long getAmountAsLong() {
        return 0;
    }

    @Override
    public long getCapacityAsLong() {
        return Long.MAX_VALUE;
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        //TODO Check if this needs to be rolled back
        lastReceived = pipe.getLevel().getGameTime();
        return EnergyPipeType.INSTANCE.receive(pipe, side, amount, transaction);
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        return 0;
    }
}
