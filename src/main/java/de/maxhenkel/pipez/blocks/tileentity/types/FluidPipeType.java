package de.maxhenkel.pipez.blocks.tileentity.types;

import de.maxhenkel.pipez.Filter;
import de.maxhenkel.pipez.FluidFilter;
import de.maxhenkel.pipez.capabilities.CapabilityCacheOld;
import de.maxhenkel.pipez.types.Upgrade;
import de.maxhenkel.pipez.blocks.ModBlocks;
import de.maxhenkel.pipez.blocks.tileentity.PipeLogicTileEntity;
import de.maxhenkel.pipez.blocks.tileentity.PipeTileEntity;
import de.maxhenkel.pipez.blocks.tileentity.UpgradeTileEntity;
import de.maxhenkel.pipez.events.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FluidPipeType extends PipeType<Fluid> {

    public static final FluidPipeType INSTANCE = new FluidPipeType();

    @Override
    public String getKey() {
        return "Fluid";
    }

    @Override
    public boolean canInsert(BlockEntity tileEntity, Direction direction) {
        return tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, direction).isPresent();
    }

    @Override
    public Filter<Fluid> createFilter() {
        return new FluidFilter();
    }

    @Override
    public String getTranslationKey() {
        return "tooltip.pipez.fluid";
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(ModBlocks.FLUID_PIPE);
    }

    @Override
    public Component getTransferText(@Nullable Upgrade upgrade) {
        return new TranslatableComponent("tooltip.pipez.rate.fluid", getRate(upgrade));
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
            // Check Fluid Extract Part
            var fluidHandler = CapabilityCacheOld.getInstance().getFluidCapabilityResult(
                    tileEntity.getLevel(), tileEntity.getBlockPos().relative(side), side.getOpposite());
            if (fluidHandler == null) {
                continue;
            }
            // Check Extractable Fluid exists
            boolean fluidExist = false;
            for (int i = 0; i < fluidHandler.getTanks(); i += 1) {
                if (!fluidHandler.getFluidInTank(i).isEmpty()) {
                    fluidExist = true;
                    break;
                }
            }
            if (!fluidExist) {
                continue;
            }

            List<PipeTileEntity.Connection> connections = tileEntity.getConnections(); // tileEntity.getSortedConnections(side, this);
            var distribution = tileEntity.getDistribution(side, this); // UpgradeTileEntity.Distribution.ROUND_ROBIN...

            if (distribution.equals(UpgradeTileEntity.Distribution.ROUND_ROBIN)) {
                insertEqually(tileEntity, side, connections, fluidHandler);
            } else {
                if (distribution.equals(UpgradeTileEntity.Distribution.RANDOM)) {
                    connections = new ArrayList<>(connections);
                    Collections.shuffle(connections);
                }
                insertOrdered(tileEntity, side, connections, fluidHandler,
                        distribution.equals(UpgradeTileEntity.Distribution.FURTHEST));
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
            IFluidHandler destination = getFluidHandler(tileEntity.getLevel(), connection.getPos(), connection.getDirection());
            boolean hasInserted = false;
            if (destination != null && !connectionsFull[p]) {
                for (int j = 0; j < fluidHandler.getTanks(); j++) {
                    FluidStack fluidInTank = fluidHandler.getFluidInTank(j);
                    FluidStack simulatedExtract = fluidHandler.drain(new FluidStack(fluidInTank.getFluid(), Math.min(Math.max(completeAmount / getConnectionsNotFullCount(connectionsFull), 1), mbToTransfer), fluidInTank.getTag()), IFluidHandler.FluidAction.SIMULATE);
                    if (simulatedExtract.isEmpty()) {
                        continue;
                    }
                    if (canInsert(connection, simulatedExtract, tileEntity.getFilters(side, this)) == tileEntity.getFilterMode(side, this).equals(UpgradeTileEntity.FilterMode.BLACKLIST)) {
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

    protected void insertOrdered(PipeLogicTileEntity tileEntity, Direction side, List<PipeTileEntity.Connection> connections, IFluidHandler fluidHandler, boolean inverted) {
        int mbToTransfer = getRate(tileEntity, side);

        connectionLoop:
        for (int k = 0; k < connections.size(); k += 1) {
            var connection = connections.get(inverted ? (connections.size() - k - 1) : k);
            IFluidHandler destination = getFluidHandler(tileEntity.getLevel(), connection.getPos(), connection.getDirection());
            if (destination == null) {
                continue;
            }

            for (int i = 0; i < fluidHandler.getTanks(); i++) {
                if (mbToTransfer <= 0) {
                    break connectionLoop;
                }
                FluidStack fluidInTank = fluidHandler.getFluidInTank(i);
                FluidStack simulatedExtract = fluidHandler.drain(new FluidStack(fluidInTank.getFluid(), mbToTransfer, fluidInTank.getTag()), IFluidHandler.FluidAction.SIMULATE);
                if (simulatedExtract.isEmpty()) {
                    continue;
                }
                if (canInsert(connection, simulatedExtract, tileEntity.getFilters(side, this)) == tileEntity.getFilterMode(side, this).equals(UpgradeTileEntity.FilterMode.BLACKLIST)) {
                    continue;
                }
                FluidStack stack = FluidUtil.tryFluidTransfer(destination, fluidHandler, simulatedExtract, true);
                mbToTransfer -= stack.getAmount();
            }
        }
    }

    private boolean canInsert(PipeTileEntity.Connection connection, FluidStack stack, List<Filter<?>> filters) {
        for (Filter<Fluid> filter : filters.stream().map(filter -> (Filter<Fluid>) filter).filter(Filter::isInvert).filter(f -> matchesConnection(connection, f)).collect(Collectors.toList())) {
            if (matches(filter, stack)) {
                return false;
            }
        }
        List<Filter<Fluid>> collect = filters.stream().map(filter -> (Filter<Fluid>) filter).filter(f -> !f.isInvert()).filter(f -> matchesConnection(connection, f)).collect(Collectors.toList());
        if (collect.isEmpty()) {
            return true;
        }
        for (Filter<Fluid> filter : collect) {
            if (matches(filter, stack)) {
                return true;
            }
        }
        return false;
    }

    private boolean matches(Filter<Fluid> filter, FluidStack stack) {
        CompoundTag metadata = filter.getMetadata();
        if (metadata == null) {
            return filter.getTag() == null || filter.getTag().contains(stack.getFluid());
        }
        if (filter.isExactMetadata()) {
            if (deepExactCompare(metadata, stack.getTag())) {
                return filter.getTag() == null || filter.getTag().contains(stack.getFluid());
            } else {
                return false;
            }
        } else {
            CompoundTag stackNBT = stack.getTag();
            if (stackNBT == null) {
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

    @Nullable
    private IFluidHandler getFluidHandler(Level level, BlockPos pos, Direction direction) {
        return CapabilityCacheOld.getInstance().getFluidCapabilityResult(level, pos, direction, true);
    }

    @Override
    public int getRate(@Nullable Upgrade upgrade) {
        return 1;
    }

}
