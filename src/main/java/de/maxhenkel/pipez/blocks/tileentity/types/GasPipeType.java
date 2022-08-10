package de.maxhenkel.pipez.blocks.tileentity.types;

import de.maxhenkel.pipez.Filter;
import de.maxhenkel.pipez.GasFilter;
import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.Upgrade;
import de.maxhenkel.pipez.blocks.ModBlocks;
import de.maxhenkel.pipez.blocks.tileentity.PipeLogicTileEntity;
import de.maxhenkel.pipez.blocks.tileentity.PipeTileEntity;
import de.maxhenkel.pipez.blocks.tileentity.UpgradeTileEntity;
import de.maxhenkel.pipez.capabilities.ModCapabilities;
import de.maxhenkel.pipez.events.ServerTickEvents;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.List;
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
        for (Direction side : Direction.values()) {
            if (!tileEntity.isExtracting(side)) {
                continue;
            }
            if (!tileEntity.shouldWork(side, this)) {
                continue;
            }
            IGasHandler gasHandler = getGasHandler(tileEntity.getLevel(), tileEntity.getBlockPos().relative(side), side.getOpposite());
            if (gasHandler == null) {
                continue;
            }

            List<PipeTileEntity.Connection> connections = tileEntity.getSortedConnections(side, this);

            if (tileEntity.getDistribution(side, this).equals(UpgradeTileEntity.Distribution.ROUND_ROBIN)) {
                insertEqually(tileEntity, side, connections, gasHandler);
            } else {
                insertOrdered(tileEntity, side, connections, gasHandler);
            }
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

    protected void insertOrdered(PipeLogicTileEntity tileEntity, Direction side, List<PipeTileEntity.Connection> connections, IGasHandler gasHandler) {
        long mbToTransfer = getRate(tileEntity, side);

        connectionLoop:
        for (PipeTileEntity.Connection connection : connections) {
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
        return ServerTickEvents.capabilityCache.getGasCapabilityResult(level, pos, direction);
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
