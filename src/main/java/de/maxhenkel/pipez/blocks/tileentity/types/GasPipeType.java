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
import de.maxhenkel.pipez.datacomponents.GasData;
import de.maxhenkel.pipez.items.ModItems;
import de.maxhenkel.pipez.utils.MekanismUtils;
import mekanism.api.Action;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.BlockCapability;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class GasPipeType extends PipeType<Chemical, GasData> {

    public static final GasPipeType INSTANCE = new GasPipeType();

    @Override
    public BlockCapability<?, Direction> getCapability() {
        return ModCapabilities.CHEMICAL_HANDLER_CAPABILITY;
    }

    @Override
    public Filter<?, Chemical> createFilter() {
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
            if (!tileEntity.shouldTickWithBackoff(side, this, 1)) {
                continue;
            }
            if (!tileEntity.isExtracting(side)) {
                continue;
            }
            if (!tileEntity.shouldWork(side, this)) {
                continue;
            }
            PipeTileEntity.Connection extractingConnection = tileEntity.getExtractingConnection(side);
            if (extractingConnection == null) {
                tileEntity.onTransferFailed(side, this);
                continue;
            }
            IChemicalHandler chemicalHandler = extractingConnection.getChemicalHandler();
            if (chemicalHandler == null) {
                tileEntity.onTransferFailed(side, this);
                continue;
            }
            boolean success = tickHandler(tileEntity, chemicalHandler, side);
            if (success) {
                tileEntity.onTransferSuccess(side, this);
            } else {
                tileEntity.onTransferFailed(side, this);
            }
        }
    }

    private boolean tickHandler(PipeLogicTileEntity tileEntity, IChemicalHandler gasHandler, Direction side) {
        List<PipeTileEntity.Connection> connections = tileEntity.getSortedConnections(side, this);
        if (tileEntity.getDistribution(side, this).equals(UpgradeTileEntity.Distribution.ROUND_ROBIN)) {
            return insertEqually(tileEntity, side, connections, gasHandler);
        } else {
            return insertOrdered(tileEntity, side, connections, gasHandler);
        }
    }

    protected boolean insertEqually(PipeLogicTileEntity tileEntity, Direction side, List<PipeTileEntity.Connection> connections, IChemicalHandler gasHandler) {
        if (connections.isEmpty()) {
            return false;
        }
        long completeAmount = getRate(tileEntity, side);
        long mbToTransfer = completeAmount;
        boolean[] connectionsFull = new boolean[connections.size()];
        int p = tileEntity.getRoundRobinIndex(side, this) % connections.size();
        while (mbToTransfer > 0 && hasNotInserted(connectionsFull)) {
            PipeTileEntity.Connection connection = connections.get(p);
            IChemicalHandler destination = connection.getChemicalHandler();
            boolean hasInserted = false;
            if (destination != null && !connectionsFull[p]) {
                for (int j = 0; j < gasHandler.getChemicalTanks(); j++) {
                    ChemicalStack gasInTank = gasHandler.getChemicalInTank(j);
                    ChemicalStack simulatedExtract = gasHandler.extractChemical(new ChemicalStack(gasInTank.getChemical(), Math.min(Math.max(completeAmount / getConnectionsNotFullCount(connectionsFull), 1), mbToTransfer)), Action.SIMULATE);
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
        return mbToTransfer < completeAmount;
    }

    protected boolean insertOrdered(PipeLogicTileEntity tileEntity, Direction side, List<PipeTileEntity.Connection> connections, IChemicalHandler gasHandler) {
        long mbToTransfer = getRate(tileEntity, side);
        long initialAmount = mbToTransfer;

        connectionLoop:
        for (PipeTileEntity.Connection connection : connections) {
            IChemicalHandler destination = connection.getChemicalHandler();
            if (destination == null) {
                continue;
            }

            for (int i = 0; i < gasHandler.getChemicalTanks(); i++) {
                if (mbToTransfer <= 0) {
                    break connectionLoop;
                }
                ChemicalStack gasInTank = gasHandler.getChemicalInTank(i);
                ChemicalStack simulatedExtract = gasHandler.extractChemical(new ChemicalStack(gasInTank.getChemical(), mbToTransfer), Action.SIMULATE);
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
        return mbToTransfer < initialAmount;
    }

    private ChemicalStack transfer(IChemicalHandler source, IChemicalHandler destination, ChemicalStack transfer) {
        ChemicalStack extracted = source.extractChemical(transfer, Action.SIMULATE);
        ChemicalStack gasStack = destination.insertChemical(extracted, Action.EXECUTE);
        long amount = extracted.getAmount() - gasStack.getAmount();
        if (amount <= 0) {
            return ChemicalStack.EMPTY;
        }
        return source.extractChemical(new ChemicalStack(extracted.getChemical(), amount), Action.EXECUTE);
    }

    private boolean canInsert(PipeTileEntity.Connection connection, ChemicalStack stack, List<Filter<?, ?>> filters) {
        boolean hasNonInvertFilter = false;
        for (int i = 0; i < filters.size(); i++) {
            @SuppressWarnings("unchecked")
            Filter<?, Chemical> filter = (Filter<?, Chemical>) filters.get(i);
            if (!matchesConnection(connection, filter)) {
                continue;
            }
            if (filter.isInvert()) {
                if (matches(filter, stack)) {
                    return false;
                }
            } else {
                hasNonInvertFilter = true;
                if (matches(filter, stack)) {
                    return true;
                }
            }
        }
        return !hasNonInvertFilter;
    }

    private boolean matches(Filter<?, Chemical> filter, ChemicalStack stack) {
        return filter.getTag() == null || filter.getTag().contains(stack.getChemical());
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

    @Override
    public DataComponentType<GasData> getDataComponentType() {
        return ModItems.GAS_DATA_COMPONENT.get();
    }

    private static final GasData DEFAULT = new GasData(UpgradeTileEntity.FilterMode.WHITELIST, UpgradeTileEntity.RedstoneMode.IGNORED, UpgradeTileEntity.Distribution.ROUND_ROBIN, Collections.emptyList());

    @Override
    public GasData defaultData() {
        return DEFAULT;
    }

}
