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
import de.maxhenkel.pipez.utils.GasUtils;
import de.maxhenkel.pipez.utils.MekanismUtils;
import mekanism.api.Action;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.IChemicalHandler;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.capabilities.Capability;
import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

public class GasPipeType extends PipeType<Chemical> {

    public static final GasPipeType INSTANCE = new GasPipeType();

    @Override
    public String getKey() {
        return "Gas";
    }

    @Override
    public Capability<?> getCapability() {
        return ModCapabilities.GAS_HANDLER_CAPABILITY;
    }

    @Override
    public Capability<?>[] getCapabilities() {
        if (!MekanismUtils.isMekanismInstalled()) {
            return new Capability[0];
        }
        return GasUtils.getChemicalCapabilities();
    }

    @Override
    public Filter<Chemical> createFilter() {
        return new GasFilter();
    }

    @Override
    public String getTranslationKey() {
        return "tooltip.pipez.gas";
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(ModBlocks.GAS_PIPE.get());
    }

    @Override
    public Component getTransferText(@Nullable Upgrade upgrade) {
        return Component.translatable("tooltip.pipez.rate.gas", getRate(upgrade));
    }

    @Override
    public void tick(PipeLogicTileEntity tileEntity) {
        if (!MekanismUtils.isMekanismInstalled()) {
            return;
        }
        for (Direction side : Direction.values()) {
            if (!tileEntity.isExtracting(side)) {
                continue;
            }
            if (!tileEntity.shouldWork(side, this)) {
                continue;
            }
            PipeTileEntity.Connection extractingConnection = tileEntity.getExtractingConnection(side);
            if (extractingConnection == null) {
                continue;
            }
            for (ChemicalType type : ChemicalType.values()) {
                IChemicalHandler gasHandler = extractingConnection.getChemicalHandler(type, tileEntity.getLevel()).orElse(null);
                if (gasHandler == null) {
                    continue;
                }
                tickHandler(tileEntity, gasHandler, side, type);
            }
        }
    }

    private void tickHandler(PipeLogicTileEntity tileEntity, IChemicalHandler gasHandler, Direction side, ChemicalType type) {
        List<PipeTileEntity.Connection> connections = tileEntity.getSortedConnections(side, this);
        if (tileEntity.getDistribution(side, this).equals(UpgradeTileEntity.Distribution.ROUND_ROBIN)) {
            insertEqually(tileEntity, side, connections, gasHandler, type);
        } else {
            insertOrdered(tileEntity, side, connections, gasHandler, type);
        }
    }

    protected void insertEqually(PipeLogicTileEntity tileEntity, Direction side, List<PipeTileEntity.Connection> connections, IChemicalHandler gasHandler, ChemicalType type) {
        if (connections.isEmpty()) {
            return;
        }
        long completeAmount = getRate(tileEntity, side);
        long mbToTransfer = completeAmount;
        boolean[] connectionsFull = new boolean[connections.size()];
        int p = tileEntity.getRoundRobinIndex(side, this) % connections.size();
        while (mbToTransfer > 0 && hasNotInserted(connectionsFull)) {
            PipeTileEntity.Connection connection = connections.get(p);
            IChemicalHandler destination = connection.getChemicalHandler(type, tileEntity.getLevel()).orElse(null);
            boolean hasInserted = false;
            if (destination != null && !connectionsFull[p]) {
                for (int j = 0; j < gasHandler.getTanks(); j++) {
                    ChemicalStack gasInTank = gasHandler.getChemicalInTank(j);
                    ChemicalStack simulatedExtract = gasHandler.extractChemical(GasUtils.createChemicalStack(gasInTank.getType(), Math.min(Math.max(completeAmount / getConnectionsNotFullCount(connectionsFull), 1), mbToTransfer)), Action.SIMULATE);
                    if (simulatedExtract.isEmpty()) {
                        continue;
                    }
                    if (canInsert(connection, simulatedExtract, tileEntity.getFilters(side, this)) == tileEntity.getFilterMode(side, this).equals(UpgradeTileEntity.FilterMode.BLACKLIST)) {
                        continue;
                    }
                    ChemicalStack stack = transfer(gasHandler, destination, simulatedExtract);
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

    protected void insertOrdered(PipeLogicTileEntity tileEntity, Direction side, List<PipeTileEntity.Connection> connections, IChemicalHandler gasHandler, ChemicalType type) {
        long mbToTransfer = getRate(tileEntity, side);

        connectionLoop:
        for (PipeTileEntity.Connection connection : connections) {
            IChemicalHandler destination = connection.getChemicalHandler(type, tileEntity.getLevel()).orElse(null);
            if (destination == null) {
                continue;
            }

            for (int i = 0; i < gasHandler.getTanks(); i++) {
                if (mbToTransfer <= 0) {
                    break connectionLoop;
                }
                ChemicalStack gasInTank = gasHandler.getChemicalInTank(i);
                ChemicalStack simulatedExtract = gasHandler.extractChemical(GasUtils.createChemicalStack(gasInTank.getType(), mbToTransfer), Action.SIMULATE);
                if (simulatedExtract.isEmpty()) {
                    continue;
                }
                if (canInsert(connection, simulatedExtract, tileEntity.getFilters(side, this)) == tileEntity.getFilterMode(side, this).equals(UpgradeTileEntity.FilterMode.BLACKLIST)) {
                    continue;
                }
                ChemicalStack stack = transfer(gasHandler, destination, simulatedExtract);
                mbToTransfer -= stack.getAmount();
            }
        }
    }

    private ChemicalStack transfer(IChemicalHandler source, IChemicalHandler destination, ChemicalStack transfer) {
        ChemicalStack extracted = source.extractChemical(transfer, Action.SIMULATE);
        ChemicalStack gasStack = destination.insertChemical(extracted, Action.EXECUTE);
        return source.extractChemical(GasUtils.createChemicalStack(extracted.getType(), extracted.getAmount() - gasStack.getAmount()), Action.EXECUTE);
    }

    private boolean canInsert(PipeTileEntity.Connection connection, ChemicalStack stack, List<Filter<?>> filters) {
        for (Filter<Chemical> filter : filters.stream().map(filter -> (Filter<Chemical>) filter).filter(Filter::isInvert).filter(f -> matchesConnection(connection, f)).collect(Collectors.toList())) {
            if (matches(filter, stack)) {
                return false;
            }
        }
        List<Filter<Chemical>> collect = filters.stream().map(filter -> (Filter<Chemical>) filter).filter(f -> !f.isInvert()).filter(f -> matchesConnection(connection, f)).collect(Collectors.toList());
        if (collect.isEmpty()) {
            return true;
        }
        for (Filter<Chemical> filter : collect) {
            if (matches(filter, stack)) {
                return true;
            }
        }
        return false;
    }

    private boolean matches(Filter<Chemical> filter, ChemicalStack stack) {
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

    @Override
    public int getRate(@Nullable Upgrade upgrade) {
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
