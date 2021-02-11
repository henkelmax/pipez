package de.maxhenkel.pipez.blocks.tileentity;

import de.maxhenkel.corelib.energy.EnergyUtils;
import de.maxhenkel.pipez.Filter;
import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.Upgrade;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;
import java.util.List;

public class EnergyPipeTileEntity extends UpgradeLogicTileEntity<Void> {

    public EnergyPipeTileEntity() {
        super(ModTileEntities.ENERGY_PIPE);
    }

    @Override
    public boolean canInsert(TileEntity tileEntity, Direction direction) {
        return tileEntity.getCapability(CapabilityEnergy.ENERGY, direction).isPresent();
    }

    @Override
    public Filter<Void> createFilter() {
        return new Filter<Void>() {
        };
    }

    @Override
    public String getFilterKey() {
        return "";
    }

    @Override
    public boolean hasFilter() {
        return false;
    }

    @Override
    public Distribution getDefaultDistribution() {
        return Distribution.ROUND_ROBIN;
    }

    @Override
    public RedstoneMode getDefaultRedstoneMode() {
        return RedstoneMode.IGNORED;
    }

    @Override
    public FilterMode getDefaultFilterMode() {
        return FilterMode.WHITELIST;
    }

    @Override
    public void tick() {
        super.tick();

        if (world.isRemote) {
            return;
        }

        for (Direction side : Direction.values()) {
            if (!isExtracting(side)) {
                continue;
            }
            if (!shouldWork(side)) {
                continue;
            }
            IEnergyStorage energyStorage = getEnergyStorage(pos.offset(side), side.getOpposite());
            if (energyStorage == null || !energyStorage.canExtract()) {
                continue;
            }

            List<Connection> connections = getSortedConnections(side);

            if (getDistribution(side).equals(Distribution.ROUND_ROBIN)) {
                insertEqually(side, connections, energyStorage);
            } else {
                insertOrdered(side, connections, energyStorage);
            }
        }
    }

    private final int[] rrIndex = new int[Direction.values().length];

    protected void insertEqually(Direction side, List<Connection> connections, IEnergyStorage energyStorage) {
        if (connections.isEmpty()) {
            return;
        }
        int completeAmount = getAmount(side);
        int energyToTransfer = completeAmount;
        boolean[] connectionsFull = new boolean[connections.size()];
        int p = rrIndex[side.getIndex()] % connections.size();
        while (energyToTransfer > 0 && hasNotInserted(connectionsFull)) {
            Connection connection = connections.get(p);
            IEnergyStorage destination = getEnergyStorage(connection.getPos(), connection.getDirection());
            boolean hasInserted = false;
            if (destination != null && destination.canReceive()) {
                int simulatedExtract = energyStorage.extractEnergy(Math.min(Math.max(completeAmount / connections.size(), 1), energyToTransfer), true);
                if (simulatedExtract > 0) {
                    int transferred = EnergyUtils.pushEnergy(energyStorage, destination, simulatedExtract);
                    if (transferred > 0) {
                        energyToTransfer -= transferred;
                        hasInserted = true;
                    }
                }
            }
            if (!hasInserted) {
                connectionsFull[p] = true;
            }
            p = (p + 1) % connections.size();
        }

        rrIndex[side.getIndex()] = p;
    }

    protected void insertOrdered(Direction side, List<Connection> connections, IEnergyStorage energyStorage) {
        int energyToTransfer = getAmount(side);

        for (Connection connection : connections) {
            if (energyToTransfer <= 0) {
                break;
            }
            IEnergyStorage destination = getEnergyStorage(connection.getPos(), connection.getDirection());
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

    private boolean hasNotInserted(boolean[] inventoriesFull) {
        for (boolean b : inventoriesFull) {
            if (!b) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    private IEnergyStorage getEnergyStorage(BlockPos pos, Direction direction) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity == null) {
            return null;
        }
        return tileEntity.getCapability(CapabilityEnergy.ENERGY, direction).orElse(null);
    }

    public int getAmount(Direction direction) {
        Upgrade upgrade = getUpgrade(direction);
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
