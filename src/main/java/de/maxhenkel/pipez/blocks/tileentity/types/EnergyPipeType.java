package de.maxhenkel.pipez.blocks.tileentity.types;

import de.maxhenkel.corelib.energy.EnergyUtils;
import de.maxhenkel.pipez.Filter;
import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.Upgrade;
import de.maxhenkel.pipez.blocks.ModBlocks;
import de.maxhenkel.pipez.blocks.tileentity.PipeLogicTileEntity;
import de.maxhenkel.pipez.blocks.tileentity.PipeTileEntity;
import de.maxhenkel.pipez.blocks.tileentity.UpgradeTileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class EnergyPipeType extends PipeType<Void> {

    public static final EnergyPipeType INSTANCE = new EnergyPipeType();

    @Override
    public String getKey() {
        return "Energy";
    }

    @Override
    public boolean canInsert(TileEntity tileEntity, Direction direction) {
        return tileEntity.getCapability(CapabilityEnergy.ENERGY, direction).isPresent();
    }

    @Override
    public boolean hasFilter() {
        return false;
    }

    @Override
    public Filter<Void> createFilter() {
        return new Filter<Void>() {
        };
    }

    @Override
    public String getTranslationKey() {
        return "tooltip.pipez.energy";
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(ModBlocks.ENERGY_PIPE);
    }

    @Override
    public ITextComponent getTransferText(@Nullable Upgrade upgrade) {
        return new TranslationTextComponent("tooltip.pipez.rate.energy", getRate(upgrade));
    }

    @Override
    public void tick(PipeLogicTileEntity tileEntity) {

    }

    public void pullEnergy(PipeLogicTileEntity tileEntity, Direction side) {
        if (!tileEntity.isExtracting(side)) {
            return;
        }
        if (!tileEntity.shouldWork(side, this)) {
            return;
        }
        IEnergyStorage energyStorage = getEnergyStorage(tileEntity, tileEntity.getBlockPos().relative(side), side.getOpposite());
        if (energyStorage == null || !energyStorage.canExtract()) {
            return;
        }

        List<PipeTileEntity.Connection> connections = tileEntity.getSortedConnections(side, this);

        if (tileEntity.getDistribution(side, this).equals(UpgradeTileEntity.Distribution.ROUND_ROBIN)) {
            insertEqually(tileEntity, side, connections, energyStorage);
        } else {
            insertOrdered(tileEntity, side, connections, energyStorage);
        }
    }

    public int receive(PipeLogicTileEntity tileEntity, Direction side, int amount, boolean simulate) {
        if (!tileEntity.isExtracting(side)) {
            return 0;
        }
        if (!tileEntity.shouldWork(side, this)) {
            return 0;
        }

        List<PipeTileEntity.Connection> connections = tileEntity.getSortedConnections(side, this);

        int maxTransfer = Math.min(getRate(tileEntity, side), amount);

        if (tileEntity.getDistribution(side, this).equals(UpgradeTileEntity.Distribution.ROUND_ROBIN)) {
            return receiveEqually(tileEntity, side, connections, maxTransfer, simulate);
        } else {
            return receiveOrdered(tileEntity, side, connections, maxTransfer, simulate);
        }
    }

    protected void insertEqually(PipeLogicTileEntity tileEntity, Direction side, List<PipeTileEntity.Connection> connections, IEnergyStorage energyStorage) {
        if (connections.isEmpty()) {
            return;
        }
        int completeAmount = getRate(tileEntity, side);
        int energyToTransfer = completeAmount;

        int p = tileEntity.getRoundRobinIndex(side, this) % connections.size();

        List<IEnergyStorage> destinations = new ArrayList<>(connections.size());
        for (int i = 0; i < connections.size(); i++) {
            int index = (i + p) % connections.size();

            PipeTileEntity.Connection connection = connections.get(index);
            IEnergyStorage destination = getEnergyStorage(tileEntity, connection.getPos(), connection.getDirection());
            if (destination != null && destination.canReceive() && destination.receiveEnergy(1, true) >= 1) {
                destinations.add(destination);
            }
        }

        for (IEnergyStorage destination : destinations) {
            int simulatedExtract = energyStorage.extractEnergy(Math.min(Math.max(completeAmount / destinations.size(), 1), energyToTransfer), true);
            if (simulatedExtract > 0) {
                int transferred = EnergyUtils.pushEnergy(energyStorage, destination, simulatedExtract);
                if (transferred > 0) {
                    energyToTransfer -= transferred;
                }
            }

            p = (p + 1) % connections.size();

            if (energyToTransfer <= 0) {
                break;
            }
        }

        tileEntity.setRoundRobinIndex(side, this, p);
    }

    protected int receiveEqually(PipeLogicTileEntity tileEntity, Direction side, List<PipeTileEntity.Connection> connections, int maxReceive, boolean simulate) {
        if (connections.isEmpty() || maxReceive <= 0) {
            return 0;
        }
        int actuallyTransferred = 0;
        int energyToTransfer = maxReceive;
        int p = tileEntity.getRoundRobinIndex(side, this) % connections.size();

        List<IEnergyStorage> destinations = new ArrayList<>(connections.size());
        for (int i = 0; i < connections.size(); i++) {
            int index = (i + p) % connections.size();

            PipeTileEntity.Connection connection = connections.get(index);
            IEnergyStorage destination = getEnergyStorage(tileEntity, connection.getPos(), connection.getDirection());
            if (destination != null && destination.canReceive() && destination.receiveEnergy(1, true) >= 1) {
                destinations.add(destination);
            }
        }

        for (IEnergyStorage destination : destinations) {
            int maxTransfer = Math.min(Math.max(maxReceive / destinations.size(), 1), energyToTransfer);
            int extracted = destination.receiveEnergy(Math.min(maxTransfer, maxReceive), simulate);
            if (extracted > 0) {
                energyToTransfer -= extracted;
                actuallyTransferred += extracted;
            }

            p = (p + 1) % connections.size();

            if (energyToTransfer <= 0) {
                break;
            }
        }

        if (!simulate) {
            tileEntity.setRoundRobinIndex(side, this, p);
        }

        return actuallyTransferred;
    }

    protected void insertOrdered(PipeLogicTileEntity tileEntity, Direction side, List<PipeTileEntity.Connection> connections, IEnergyStorage energyStorage) {
        int energyToTransfer = getRate(tileEntity, side);

        for (PipeTileEntity.Connection connection : connections) {
            if (energyToTransfer <= 0) {
                break;
            }
            IEnergyStorage destination = getEnergyStorage(tileEntity, connection.getPos(), connection.getDirection());
            if (destination == null || !destination.canReceive()) {
                continue;
            }

            int simulatedExtract = energyStorage.extractEnergy(energyToTransfer, true);
            if (simulatedExtract > 0) {
                int extract = EnergyUtils.pushEnergy(energyStorage, destination, simulatedExtract);
                energyToTransfer -= extract;
            }
        }
    }

    protected int receiveOrdered(PipeLogicTileEntity tileEntity, Direction side, List<PipeTileEntity.Connection> connections, int maxReceive, boolean simulate) {
        int actuallyTransferred = 0;
        int energyToTransfer = maxReceive;

        for (PipeTileEntity.Connection connection : connections) {
            if (energyToTransfer <= 0) {
                break;
            }
            IEnergyStorage destination = getEnergyStorage(tileEntity, connection.getPos(), connection.getDirection());
            if (destination == null || !destination.canReceive()) {
                continue;
            }

            int extracted = destination.receiveEnergy(Math.min(energyToTransfer, maxReceive), simulate);
            energyToTransfer -= extracted;
            actuallyTransferred += extracted;
        }
        return actuallyTransferred;
    }

    private boolean hasNotInserted(boolean[] inventoriesFull) {
        for (boolean b : inventoriesFull) {
            if (!b) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    private IEnergyStorage getEnergyStorage(PipeLogicTileEntity tileEntity, BlockPos pos, Direction direction) {
        TileEntity te = tileEntity.getLevel().getBlockEntity(pos);
        if (te == null) {
            return null;
        }
        return te.getCapability(CapabilityEnergy.ENERGY, direction).orElse(null);
    }

    @Override
    public int getRate(@Nullable Upgrade upgrade) {
        if (upgrade == null) {
            return Main.SERVER_CONFIG.energyPipeAmount.get();
        }
        switch (upgrade) {
            case BASIC:
                return Main.SERVER_CONFIG.energyPipeAmountBasic.get();
            case IMPROVED:
                return Main.SERVER_CONFIG.energyPipeAmountImproved.get();
            case ADVANCED:
                return Main.SERVER_CONFIG.energyPipeAmountAdvanced.get();
            case ULTIMATE:
                return Main.SERVER_CONFIG.energyPipeAmountUltimate.get();
            case INFINITY:
            default:
                return Integer.MAX_VALUE;
        }
    }

}
