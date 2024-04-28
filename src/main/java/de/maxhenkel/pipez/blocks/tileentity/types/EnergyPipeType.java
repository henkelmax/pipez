package de.maxhenkel.pipez.blocks.tileentity.types;

import de.maxhenkel.corelib.energy.EnergyUtils;
import de.maxhenkel.corelib.helpers.Pair;
import de.maxhenkel.pipez.Filter;
import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.Upgrade;
import de.maxhenkel.pipez.blocks.ModBlocks;
import de.maxhenkel.pipez.blocks.tileentity.PipeLogicTileEntity;
import de.maxhenkel.pipez.blocks.tileentity.PipeTileEntity;
import de.maxhenkel.pipez.blocks.tileentity.UpgradeTileEntity;
import de.maxhenkel.pipez.datacomponents.EnergyData;
import de.maxhenkel.pipez.items.ModItems;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EnergyPipeType extends PipeType<Void, EnergyData> {

    public static final EnergyPipeType INSTANCE = new EnergyPipeType();

    @Override
    public BlockCapability<?, Direction> getCapability() {
        return Capabilities.EnergyStorage.BLOCK;
    }

    @Nullable
    @Override
    public Filter<?, Void> createFilter() {
        return null;
    }

    @Override
    public boolean hasFilter() {
        return false;
    }

    @Override
    public String getTranslationKey() {
        return "tooltip.pipez.energy";
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(ModBlocks.ENERGY_PIPE.get());
    }

    @Override
    public Component getTransferText(@Nullable Upgrade upgrade) {
        return Component.translatable("tooltip.pipez.rate.energy", getRate(upgrade));
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
        PipeTileEntity.Connection extractingConnection = tileEntity.getExtractingConnection(side);
        if (extractingConnection == null) {
            return;
        }
        IEnergyStorage energyStorage = extractingConnection.getEnergyHandler();
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
            IEnergyStorage destination = connection.getEnergyHandler();
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
        if (tileEntity.pushRecursion()) {
            return 0;
        }
        int actuallyTransferred = 0;
        int energyToTransfer = maxReceive;
        int p = tileEntity.getRoundRobinIndex(side, this) % connections.size();

        List<Pair<IEnergyStorage, Integer>> destinations = new ArrayList<>(connections.size());
        for (int i = 0; i < connections.size(); i++) {
            int index = (i + p) % connections.size();

            PipeTileEntity.Connection connection = connections.get(index);
            IEnergyStorage destination = connection.getEnergyHandler();
            if (destination != null && destination.canReceive() && destination.receiveEnergy(1, true) >= 1) {
                destinations.add(new Pair<>(destination, index));
            }
        }

        for (Pair<IEnergyStorage, Integer> destination : destinations) {
            int maxTransfer = Math.min(Math.max(maxReceive / destinations.size(), 1), energyToTransfer);
            int extracted = destination.getKey().receiveEnergy(Math.min(maxTransfer, maxReceive), simulate);
            if (extracted > 0) {
                energyToTransfer -= extracted;
                actuallyTransferred += extracted;
            }

            p = destination.getValue() + 1;

            if (energyToTransfer <= 0) {
                break;
            }
        }

        if (!simulate) {
            tileEntity.setRoundRobinIndex(side, this, p);
        }
        tileEntity.popRecursion();
        return actuallyTransferred;
    }

    protected void insertOrdered(PipeLogicTileEntity tileEntity, Direction side, List<PipeTileEntity.Connection> connections, IEnergyStorage energyStorage) {
        int energyToTransfer = getRate(tileEntity, side);

        for (PipeTileEntity.Connection connection : connections) {
            if (energyToTransfer <= 0) {
                break;
            }
            IEnergyStorage destination = connection.getEnergyHandler();
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
        if (tileEntity.pushRecursion()) {
            return 0;
        }
        int actuallyTransferred = 0;
        int energyToTransfer = maxReceive;

        for (PipeTileEntity.Connection connection : connections) {
            if (energyToTransfer <= 0) {
                break;
            }
            IEnergyStorage destination = connection.getEnergyHandler();
            if (destination == null || !destination.canReceive()) {
                continue;
            }

            int extracted = destination.receiveEnergy(Math.min(energyToTransfer, maxReceive), simulate);
            energyToTransfer -= extracted;
            actuallyTransferred += extracted;
        }
        tileEntity.popRecursion();
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

    @Override
    public DataComponentType<EnergyData> getDataComponentType() {
        return ModItems.ENERGY_DATA_COMPONENT.get();
    }

    private static final EnergyData DEFAULT = new EnergyData(UpgradeTileEntity.FilterMode.WHITELIST, UpgradeTileEntity.RedstoneMode.IGNORED, UpgradeTileEntity.Distribution.ROUND_ROBIN, Collections.emptyList());

    @Override
    public EnergyData defaultData() {
        return DEFAULT;
    }
}
