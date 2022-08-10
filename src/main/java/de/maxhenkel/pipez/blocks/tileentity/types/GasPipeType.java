package de.maxhenkel.pipez.blocks.tileentity.types;

import de.maxhenkel.pipez.Filter;
import de.maxhenkel.pipez.GasFilter;
import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.Upgrade;
import de.maxhenkel.pipez.blocks.ModBlocks;
import de.maxhenkel.pipez.blocks.tileentity.PipeLogicTileEntity;
import de.maxhenkel.pipez.blocks.tileentity.PipeTileEntity;
import de.maxhenkel.pipez.blocks.tileentity.UpgradeTileEntity;
import de.maxhenkel.pipez.capabilities.CapabilityCache;
import de.maxhenkel.pipez.capabilities.ModCapabilities;
import de.maxhenkel.pipez.events.ServerTickEvents;
import de.maxhenkel.pipez.utils.TankInfo;
import mekanism.api.Action;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class GasPipeType extends PipeType<Gas> {

    public static final GasPipeType INSTANCE = new GasPipeType();

    protected Logger logger = LogManager.getLogger(Main.MODID);

    @Override
    public String getKey() {
        return "Gas";
    }

    @Override
    public boolean canInsert(BlockEntity tileEntity, Direction direction) {
        return tileEntity.getCapability(ModCapabilities.GAS_HANDLER_CAPABILITY, direction).isPresent();
    }

    @Override
    public Filter<Gas> createFilter() {
        return new GasFilter();
    }

    @Override
    public String getTranslationKey() {
        return "tooltip.pipez.gas";
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(ModBlocks.GAS_PIPE);
    }

    @Override
    public Component getTransferText(@Nullable Upgrade upgrade) {
        return new TranslatableComponent("tooltip.pipez.rate.gas", getRate(upgrade));
    }

    @Override
    public void tick(PipeLogicTileEntity tileEntity) {
        var worldLevel = tileEntity.getLevel();

        for (Direction side : Direction.values()) {
            if (!tileEntity.isExtracting(side)) {
                continue;
            }
            if (!tileEntity.shouldWork(side, this)) {
                continue;
            }
            // Check there is gas
            var gasHandler = CapabilityCache.getInstance().getGasCapabilityResult(worldLevel, tileEntity.getBlockPos().relative(side), side.getOpposite());
            if (gasHandler == null) {
                continue;
            }

            // Check there is any item to put of.
            boolean hasGas = false;
            for (int i = 0; i < gasHandler.getTanks(); i += 1) {
                if (!gasHandler.getChemicalInTank(i).isEmpty()) {
                    hasGas = true;
                    break;
                }
            }
            if (!hasGas) {
                continue;
            }
            // Connections loop
            List<PipeTileEntity.Connection> connections = tileEntity.getConnections(); // tileEntity.getSortedConnections(side, this);
            var distribution = tileEntity.getDistribution(side, this); // UpgradeTileEntity.Distribution.ROUND_ROBIN...

            if (distribution.equals(UpgradeTileEntity.Distribution.ROUND_ROBIN)) {
                insertEqually(tileEntity, side, connections, gasHandler);
                // insertEquality(tileEntity, side, connections, gasHandler);
            } else {
                if (distribution.equals(UpgradeTileEntity.Distribution.RANDOM)) {
                    connections = new ArrayList<>(connections);
                    Collections.shuffle(connections);
                }
                insertOrdered(tileEntity, side, connections, gasHandler,
                        distribution.equals(UpgradeTileEntity.Distribution.FURTHEST));
            }
        }
    }

    protected void insertEquality(PipeLogicTileEntity tileEntity, Direction side, List<PipeTileEntity.Connection> connections, IGasHandler gasHandler) {
        // Not working and Incomplete code
        var level = tileEntity.getLevel();
        // 1. If there isn't any connection, skip.
        if (connections.isEmpty()) {
            return;
        }
        // 2. Get connections
        var validDestinations = new ArrayList<IGasHandler>();
        for (PipeTileEntity.Connection connection : connections) {
            var cacheConnection = CapabilityCache.getInstance().getGasCapabilityResult(level, connection.getPos(), connection.getDirection());
            if (cacheConnection != null) {
                validDestinations.add(cacheConnection);
            }
        }
        // 3. Iterate gases
        int tanksSize = gasHandler.getTanks();
        int destSize = validDestinations.size();
        long maxTransfer = getRate(tileEntity, side);
        for (int i = 0; i < tanksSize; i += 1) {
            // Gas type 0~n
            GasStack gasInTank = gasHandler.getChemicalInTank(i);
            gasInTank = new GasStack(gasInTank, Math.min(gasInTank.getAmount(), maxTransfer));
            GasStack testGas = new GasStack(gasInTank, 1L);
            // check destinations
            TankInfo tanksInfo[] = new TankInfo[destSize];
            for (int k = 0; k < validDestinations.size(); k += 1) {
                var info = new TankInfo();
                var destHandler = validDestinations.get(i);
                info.availableSize = 0;
                info.slot = -1;
                // Loop tanks
                for (int tankIndex = 0; tankIndex < destHandler.getTanks(); tankIndex += 1) {
                    if (destHandler.isValid(tankIndex, testGas)) {
                        info.availableSize = Math.max(0L, destHandler.getTankCapacity(tankIndex) - destHandler.getChemicalInTank(tankIndex).getAmount());
                        info.slot = tankIndex;
                        break;
                    }
                }
                tanksInfo[k] = info;
            }
            // Round-robin?
            var sortedList = Arrays.stream(tanksInfo).sorted(Comparator.comparingLong(TankInfo::getAvailableSize)).toList();
            var stairValue = 0L;
            var leftAmount = gasInTank.getAmount();
            for (int k = 0; k < sortedList.size(); k += 1) {
                var tankInfo = sortedList.get(k);
                if (tankInfo.availableSize <= 0) {
                    continue;
                }
                // Check flatten
                var leftSize = sortedList.size() - k;
                var flatValue = leftAmount / leftSize + stairValue;
                // can fill equal
                if (tankInfo.availableSize >= flatValue) {
                    for (int j = k; j < sortedList.size(); j += 1) {
                        sortedList.get(j).fillSize = flatValue;
                        leftAmount -= flatValue;
                    }
                    break;
                } else {
                    // cannot fill equal so next iteration
                    tankInfo.fillSize = tankInfo.availableSize;
                    stairValue += tankInfo.fillSize;
                    leftAmount -= leftSize * tankInfo.fillSize;
                }
            }
            // Push
            var extractedGas = gasHandler.extractChemical(i, maxTransfer - leftAmount, Action.EXECUTE);
            for (TankInfo info : sortedList) {
                validDestinations.get(info.index).insertChemical(
                        info.slot,
                        new GasStack(extractedGas, info.fillSize),
                        Action.EXECUTE
                );
            }
            logger.log(org.apache.logging.log4j.Level.DEBUG, "Extract: " + extractedGas.getAmount());
            // Reduce max transfer
            maxTransfer = maxTransfer - (gasInTank.getAmount() - leftAmount);
        }
    }

    protected void insertEqually(PipeLogicTileEntity tileEntity, Direction side, List<PipeTileEntity.Connection> connections, IGasHandler gasHandler) {
        if (connections.isEmpty()) {
            return;
        }
        long completeAmount = getRate(tileEntity, side);
        long mbToTransfer = completeAmount;
        boolean[] connectionsFull = new boolean[connections.size()];
        int p = tileEntity.getRoundRobinIndex(side, this) % connections.size();
        while (mbToTransfer > 0 && hasNotInserted(connectionsFull)) {
            PipeTileEntity.Connection connection = connections.get(p);
            IGasHandler destination = getGasHandler(tileEntity.getLevel(), connection.getPos(), connection.getDirection());
            boolean hasInserted = false;
            if (destination != null && !connectionsFull[p]) {
                for (int j = 0; j < gasHandler.getTanks(); j++) {
                    GasStack gasInTank = gasHandler.getChemicalInTank(j);
                    GasStack simulatedExtract = gasHandler.extractChemical(new GasStack(gasInTank.getType(), Math.min(Math.max(completeAmount / getConnectionsNotFullCount(connectionsFull), 1), mbToTransfer)), Action.SIMULATE);
                    if (simulatedExtract.isEmpty()) {
                        continue;
                    }
                    if (canInsert(connection, simulatedExtract, tileEntity.getFilters(side, this)) == tileEntity.getFilterMode(side, this).equals(UpgradeTileEntity.FilterMode.BLACKLIST)) {
                        continue;
                    }
                    GasStack stack = transfer(gasHandler, destination, simulatedExtract);
                    if (stack.getAmount() > 0) {
                        mbToTransfer -= stack.getAmount();
                        hasInserted = true;
                        break;
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

    protected void insertOrdered(PipeLogicTileEntity tileEntity, Direction side, List<PipeTileEntity.Connection> connections, IGasHandler gasHandler, boolean invert) {
        long mbToTransfer = getRate(tileEntity, side);

        connectionLoop:
        for (int k = 0; k < connections.size(); k += 1) {
            PipeTileEntity.Connection connection;
            if (invert) {
                connection = connections.get(connections.size() - k - 1);
            } else {
                connection = connections.get(k);
            }

            IGasHandler destination = getGasHandler(tileEntity.getLevel(), connection.getPos(), connection.getDirection());
            if (destination == null) {
                continue;
            }

            for (int i = 0; i < gasHandler.getTanks(); i++) {
                if (mbToTransfer <= 0) {
                    break connectionLoop;
                }
                GasStack gasInTank = gasHandler.getChemicalInTank(i);
                GasStack simulatedExtract = gasHandler.extractChemical(new GasStack(gasInTank.getType(), mbToTransfer), Action.SIMULATE);
                if (simulatedExtract.isEmpty()) {
                    continue;
                }
                if (canInsert(connection, simulatedExtract, tileEntity.getFilters(side, this)) == tileEntity.getFilterMode(side, this).equals(UpgradeTileEntity.FilterMode.BLACKLIST)) {
                    continue;
                }
                GasStack stack = transfer(gasHandler, destination, simulatedExtract);
                mbToTransfer -= stack.getAmount();
            }
        }
    }

    private GasStack transfer(IGasHandler source, IGasHandler destination, GasStack transfer) {
        GasStack extracted = source.extractChemical(transfer, Action.SIMULATE);
        GasStack gasStack = destination.insertChemical(extracted, Action.EXECUTE);
        return source.extractChemical(new GasStack(extracted.getType(), extracted.getAmount() - gasStack.getAmount()), Action.EXECUTE);
    }

    private boolean canInsert(PipeTileEntity.Connection connection, GasStack stack, List<Filter<?>> filters) {
        for (Filter<Gas> filter : filters.stream().map(filter -> (Filter<Gas>) filter).filter(Filter::isInvert).filter(f -> matchesConnection(connection, f)).collect(Collectors.toList())) {
            if (matches(filter, stack)) {
                return false;
            }
        }
        List<Filter<Gas>> collect = filters.stream().map(filter -> (Filter<Gas>) filter).filter(f -> !f.isInvert()).filter(f -> matchesConnection(connection, f)).collect(Collectors.toList());
        if (collect.isEmpty()) {
            return true;
        }
        for (Filter<Gas> filter : collect) {
            if (matches(filter, stack)) {
                return true;
            }
        }
        return false;
    }

    private boolean matches(Filter<Gas> filter, GasStack stack) {
        return filter.getTag() == null || filter.getTag().contains(stack.getType());
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
    private IGasHandler getGasHandler(Level level, BlockPos pos, Direction direction) {
        return CapabilityCache.getInstance().getGasCapabilityResult(level, pos, direction, true);
    }

    @Override
    public int getRate(@Nullable Upgrade upgrade) {
        if (upgrade == null) {
            return ServerTickEvents.gasPipeAmount;
        }
        switch (upgrade) {
            case BASIC:
                return ServerTickEvents.gasPipeAmountBasic;
            case IMPROVED:
                return ServerTickEvents.gasPipeAmountImproved;
            case ADVANCED:
                return ServerTickEvents.gasPipeAmountAdvanced;
            case ULTIMATE:
                return ServerTickEvents.gasPipeAmountUltimate;
            case INFINITY:
                return Integer.MAX_VALUE;
            default:
                return 1;
        }
    }

}
