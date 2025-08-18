package de.maxhenkel.pipez.blocks.tileentity.types;

import de.maxhenkel.pipez.*;
import de.maxhenkel.pipez.blocks.ModBlocks;
import de.maxhenkel.pipez.blocks.tileentity.PipeLogicTileEntity;
import de.maxhenkel.pipez.blocks.tileentity.PipeTileEntity;
import de.maxhenkel.pipez.blocks.tileentity.UpgradeTileEntity;
import de.maxhenkel.pipez.datacomponents.FluidData;
import de.maxhenkel.pipez.items.ModItems;
import de.maxhenkel.pipez.utils.ComponentUtils;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FluidPipeType extends PipeType<Fluid, FluidData> {

    public static final FluidPipeType INSTANCE = new FluidPipeType();

    @Override
    public BlockCapability<?, Direction> getCapability() {
        return Capabilities.FluidHandler.BLOCK;
    }

    @Nullable
    @Override
    public Filter<?, Fluid> createFilter() {
        return new FluidFilter();
    }

    @Override
    public String getTranslationKey() {
        return "tooltip.pipez.fluid";
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(ModBlocks.FLUID_PIPE.get());
    }

    @Override
    public Component getTransferText(@Nullable Upgrade upgrade) {
        return Component.translatable("tooltip.pipez.rate.fluid", getRate(upgrade));
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
            PipeTileEntity.Connection extractingConnection = tileEntity.getExtractingConnection(side);
            if (extractingConnection == null) {
                continue;
            }
            IFluidHandler fluidHandler = extractingConnection.getFluidHandler();
            if (fluidHandler == null) {
                continue;
            }

            List<PipeTileEntity.Connection> connections = tileEntity.getSortedConnections(side, this);

            if (tileEntity.getDistribution(side, this).equals(UpgradeTileEntity.Distribution.ROUND_ROBIN)) {
                insertEqually(tileEntity, side, connections, fluidHandler);
            } else {
                insertOrdered(tileEntity, side, connections, fluidHandler);
            }
        }
    }

    protected void insertEqually(PipeLogicTileEntity tileEntity, Direction side, List<PipeTileEntity.Connection> connections, IFluidHandler fluidHandler) {
        if (connections.isEmpty()) {
            return;
        }
        int completeAmount = getRate(tileEntity, side);
        int mbToTransfer = completeAmount;
        boolean[] connectionsFull = new boolean[connections.size()];
        int p = tileEntity.getRoundRobinIndex(side, this) % connections.size();
        while (mbToTransfer > 0 && hasNotInserted(connectionsFull)) {
            PipeTileEntity.Connection connection = connections.get(p);
            IFluidHandler destination = connection.getFluidHandler();
            boolean hasInserted = false;
            if (destination != null && !connectionsFull[p]) {
                for (int j = 0; j < fluidHandler.getTanks(); j++) {
                    FluidStack fluidInTank = fluidHandler.getFluidInTank(j);
                    FluidStack copy = fluidInTank.copy();
                    copy.setAmount(Math.min(Math.max(completeAmount / getConnectionsNotFullCount(connectionsFull), 1), mbToTransfer));
                    FluidStack simulatedExtract = fluidHandler.drain(copy, IFluidHandler.FluidAction.SIMULATE);
                    if (simulatedExtract.isEmpty()) {
                        continue;
                    }
                    if (canInsert(tileEntity.getLevel().registryAccess(), connection, simulatedExtract, tileEntity.getFilters(side, this)) == tileEntity.getFilterMode(side, this).equals(UpgradeTileEntity.FilterMode.BLACKLIST)) {
                        continue;
                    }
                    FluidStack stack = FluidUtil.tryFluidTransfer(destination, fluidHandler, simulatedExtract, true);
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

    protected void insertOrdered(PipeLogicTileEntity tileEntity, Direction side, List<PipeTileEntity.Connection> connections, IFluidHandler fluidHandler) {
        int mbToTransfer = getRate(tileEntity, side);

        connectionLoop:
        for (PipeTileEntity.Connection connection : connections) {
            IFluidHandler destination = connection.getFluidHandler();
            if (destination == null) {
                continue;
            }

            for (int i = 0; i < fluidHandler.getTanks(); i++) {
                if (mbToTransfer <= 0) {
                    break connectionLoop;
                }
                FluidStack fluidInTank = fluidHandler.getFluidInTank(i);
                FluidStack copy = fluidInTank.copy();
                copy.setAmount(mbToTransfer);
                FluidStack simulatedExtract = fluidHandler.drain(copy, IFluidHandler.FluidAction.SIMULATE);
                if (simulatedExtract.isEmpty()) {
                    continue;
                }
                if (canInsert(tileEntity.getLevel().registryAccess(), connection, simulatedExtract, tileEntity.getFilters(side, this)) == tileEntity.getFilterMode(side, this).equals(UpgradeTileEntity.FilterMode.BLACKLIST)) {
                    continue;
                }
                FluidStack stack = FluidUtil.tryFluidTransfer(destination, fluidHandler, simulatedExtract, true);
                mbToTransfer -= stack.getAmount();
            }
        }
    }

    private boolean canInsert(HolderLookup.Provider provider, PipeTileEntity.Connection connection, FluidStack stack, List<Filter<?, ?>> filters) {
        for (Filter<?, Fluid> filter : filters.stream().map(filter -> (Filter<?, Fluid>) filter).filter(Filter::isInvert).filter(f -> matchesConnection(connection, f)).collect(Collectors.toList())) {
            if (matches(provider, filter, stack)) {
                return false;
            }
        }
        List<Filter<?, Fluid>> collect = filters.stream().map(filter -> (Filter<?, Fluid>) filter).filter(f -> !f.isInvert()).filter(f -> matchesConnection(connection, f)).collect(Collectors.toList());
        if (collect.isEmpty()) {
            return true;
        }
        for (Filter<?, Fluid> filter : collect) {
            if (matches(provider, filter, stack)) {
                return true;
            }
        }
        return false;
    }

    private boolean matches(HolderLookup.Provider provider, Filter<?, Fluid> filter, FluidStack stack) {
        CompoundTag metadata = filter.getMetadata();
        if (metadata == null) {
            return filter.getTag() == null || filter.getTag().contains(stack.getFluid());
        }
        CompoundTag stackNBT = ComponentUtils.getTag(provider, stack);
        if (filter.isExactMetadata()) {
            if (deepExactCompare(metadata, stackNBT)) {
                return filter.getTag() == null || filter.getTag().contains(stack.getFluid());
            } else {
                return false;
            }
        } else {
            if (stackNBT.isEmpty()) {
                return metadata.size() <= 0;
            }
            if (!deepFuzzyCompare(metadata, stackNBT)) {
                return false;
            }
            return filter.getTag() == null || filter.getTag().contains(stack.getFluid());
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

    @Override
    public int getRate(@Nullable Upgrade upgrade) {
        if (upgrade == null) {
            return Main.SERVER_CONFIG.fluidPipeAmount.get();
        }
        return switch (upgrade.getId()) {
            case 0 -> Main.SERVER_CONFIG.fluidPipeAmountBasic.get();
            case 1 -> Main.SERVER_CONFIG.fluidPipeAmountImproved.get();
            case 2 -> Main.SERVER_CONFIG.fluidPipeAmountAdvanced.get();
            case 3 -> Main.SERVER_CONFIG.fluidPipeAmountUltimate.get();
            // HowXu: add kubejs support
            case -1 -> Integer.MAX_VALUE;
            default -> upgrade.getKubeJSCustomData().getFluidAmount();
        };
    }

    @Override
    public DataComponentType<FluidData> getDataComponentType() {
        return ModItems.FLUID_DATA_COMPONENT.get();
    }

    private static final FluidData DEFAULT = new FluidData(UpgradeTileEntity.FilterMode.WHITELIST, UpgradeTileEntity.RedstoneMode.IGNORED, UpgradeTileEntity.Distribution.ROUND_ROBIN, Collections.emptyList());

    @Override
    public FluidData defaultData() {
        return DEFAULT;
    }

}
