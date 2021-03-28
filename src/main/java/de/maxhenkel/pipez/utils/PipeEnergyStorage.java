package de.maxhenkel.pipez.utils;

import de.maxhenkel.pipez.blocks.tileentity.PipeLogicTileEntity;
import de.maxhenkel.pipez.blocks.tileentity.types.EnergyPipeType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class PipeEnergyStorage implements IEnergyStorage {

    protected PipeLogicTileEntity pipe;
    protected Direction side;

    public PipeEnergyStorage(PipeLogicTileEntity pipe, Direction side) {
        this.pipe = pipe;
        this.side = side;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        TileEntity te = pipe.getLevel().getBlockEntity(pipe.getBlockPos().relative(side));
        if (te == null) {
            return EnergyPipeType.INSTANCE.receive(pipe, side, maxReceive, simulate);
        }
        LazyOptional<IEnergyStorage> capability = te.getCapability(CapabilityEnergy.ENERGY, side.getOpposite());
        if (capability.isPresent() && capability.orElse(null).canExtract()) {
            return 0;
        }
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
        return 0;
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
