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
        for (Direction side : Direction.values()) {
            if (!tileEntity.isExtracting(side)) {
                continue;
            }
            if (!tileEntity.shouldWork(side, this)) {
                continue;
            }
            IEnergyStorage energyStorage = getEnergyStorage(tileEntity, tileEntity.getBlockPos().relative(side), side.getOpposite());
            if (energyStorage == null || !energyStorage.canExtract()) {
                continue;
            }

            List<PipeTileEntity.Connection> connections = tileEntity.getSortedConnections(side, this);

            if (tileEntity.getDistribution(side, this).equals(UpgradeTileEntity.Distribution.ROUND_ROBIN)) {
                insertEqually(tileEntity, side, connections, energyStorage);
            } else {
                insertOrdered(tileEntity, side, connections, energyStorage);
            }
        }
    }

    protected void insertEqually(PipeLogicTileEntity tileEntity, Direction side, List<PipeTileEntity.Connection> connections, IEnergyStorage energyStorage) {
        if (connections.isEmpty()) {
            return;
        }
        int completeAmount = getRate(tileEntity, side);
        int energyToTransfer = completeAmount;
        boolean[] connectionsFull = new boolean[connections.size()];
        int p = tileEntity.getRoundRobinIndex(side, this) % connections.size();
        while (energyToTransfer > 0 && hasNotInserted(connectionsFull)) {
            PipeTileEntity.Connection connection = connections.get(p);
            IEnergyStorage destination = getEnergyStorage(tileEntity, connection.getPos(), connection.getDirection());
            boolean hasInserted = false;
            if (destination != null && destination.canReceive() && !connectionsFull[p]) {
                int simulatedExtract = energyStorage.extractEnergy(Math.min(Math.max(completeAmount / getConnectionsNotFullCount(connectionsFull), 1), energyToTransfer), true);
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

        tileEntity.setRoundRobinIndex(side, this, p);
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
