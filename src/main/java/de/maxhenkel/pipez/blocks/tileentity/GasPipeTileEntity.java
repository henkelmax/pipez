package de.maxhenkel.pipez.blocks.tileentity;

import de.maxhenkel.pipez.Filter;
import de.maxhenkel.pipez.GasFilter;
import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.Upgrade;
import de.maxhenkel.pipez.capabilities.ModCapabilities;
import mekanism.api.Action;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

public class GasPipeTileEntity extends UpgradeLogicTileEntity<Gas> {

    public GasPipeTileEntity() {
        super(ModTileEntities.GAS_PIPE);
    }

    @Override
    public boolean canInsert(TileEntity tileEntity, Direction direction) {
        return tileEntity.getCapability(ModCapabilities.GAS_HANDLER_CAPABILITY, direction).isPresent();
    }

    @Override
    public Filter<Gas> createFilter() {
        return new GasFilter();
    }

    @Override
    public String getFilterKey() {
        return "GasFilters";
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
            IGasHandler gasHandler = getGasHandler(pos.offset(side), side.getOpposite());
            if (gasHandler == null) {
                continue;
            }

            List<Connection> connections = getSortedConnections(side);

            if (getDistribution(side).equals(Distribution.ROUND_ROBIN)) {
                insertEqually(side, connections, gasHandler);
            } else {
                insertOrdered(side, connections, gasHandler);
            }
        }
    }

    private final int[] rrIndex = new int[Direction.values().length];

    protected void insertEqually(Direction side, List<Connection> connections, IGasHandler gasHandler) {
        if (connections.isEmpty()) {
            return;
        }
        long completeAmount = getRate(side);
        long mbToTransfer = completeAmount;
        boolean[] connectionsFull = new boolean[connections.size()];
        int p = rrIndex[side.getIndex()] % connections.size();
        while (mbToTransfer > 0 && hasNotInserted(connectionsFull)) {
            Connection connection = connections.get(p);
            IGasHandler destination = getGasHandler(connection.getPos(), connection.getDirection());
            boolean hasInserted = false;
            if (destination != null) {
                for (int j = 0; j < gasHandler.getTanks(); j++) {
                    GasStack gasInTank = gasHandler.getChemicalInTank(j);
                    GasStack simulatedExtract = gasHandler.extractChemical(new GasStack(gasInTank.getType(), Math.min(Math.max(completeAmount / connections.size(), 1), mbToTransfer)), Action.SIMULATE);
                    if (simulatedExtract.isEmpty()) {
                        continue;
                    }
                    if (canInsert(connection, simulatedExtract, getFilters(side)) == getFilterMode(side).equals(FilterMode.BLACKLIST)) {
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

        rrIndex[side.getIndex()] = p;
    }

    protected void insertOrdered(Direction side, List<Connection> connections, IGasHandler gasHandler) {
        long mbToTransfer = getRate(side);

        connectionLoop:
        for (Connection connection : connections) {
            IGasHandler destination = getGasHandler(connection.getPos(), connection.getDirection());
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
                if (canInsert(connection, simulatedExtract, getFilters(side)) == getFilterMode(side).equals(FilterMode.BLACKLIST)) {
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

    private boolean canInsert(Connection connection, GasStack stack, List<Filter<Gas>> filters) {
        for (Filter<Gas> filter : filters.stream().filter(Filter::isInvert).filter(f -> matchesConnection(connection, f)).collect(Collectors.toList())) {
            if (matches(filter, stack)) {
                return false;
            }
        }
        List<Filter<Gas>> collect = filters.stream().filter(f -> !f.isInvert()).filter(f -> matchesConnection(connection, f)).collect(Collectors.toList());
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
        return filter.getTag() == null || stack.getType().isIn(filter.getTag());
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
    private IGasHandler getGasHandler(BlockPos pos, Direction direction) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity == null) {
            return null;
        }
        return tileEntity.getCapability(ModCapabilities.GAS_HANDLER_CAPABILITY, direction).orElse(null);
    }

    @Override
    public int getRate(Direction direction) {
        Upgrade upgrade = getUpgrade(direction);
        if (upgrade == null) {
            return Main.SERVER_CONFIG.gasPipeAmount.get();
        }
        switch (upgrade) {
            case BASIC:
                return Main.SERVER_CONFIG.gasPipeAmountBasic.get();
            case IMPROVED:
                return Main.SERVER_CONFIG.gasPipeAmountImproved.get();
            case ADVANCED:
                return Main.SERVER_CONFIG.gasPipeAmountAdvanced.get();
            case ULTIMATE:
                return Main.SERVER_CONFIG.gasPipeAmountUltimate.get();
            case INFINITY:
            default:
                return Integer.MAX_VALUE;
        }
    }

}
