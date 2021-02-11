package de.maxhenkel.pipez.blocks.tileentity;

import de.maxhenkel.pipez.Filter;
import de.maxhenkel.pipez.FluidFilter;
import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.Upgrade;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

public class FluidPipeTileEntity extends UpgradeLogicTileEntity<Fluid> {

    public FluidPipeTileEntity() {
        super(ModTileEntities.FLUID_PIPE);
    }

    @Override
    public boolean canInsert(TileEntity tileEntity, Direction direction) {
        return tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, direction.getOpposite()).isPresent();
    }

    @Override
    public Filter<Fluid> createFilter() {
        return new FluidFilter();
    }

    @Override
    public String getFilterKey() {
        return "FluidFilters";
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
            IFluidHandler fluidHandler = getFluidHandler(pos.offset(side), side.getOpposite());
            if (fluidHandler == null) {
                continue;
            }

            List<Connection> connections = getSortedConnections(side);

            if (getDistribution(side).equals(Distribution.ROUND_ROBIN)) {
                insertEqually(side, connections, fluidHandler);
            } else {
                insertOrdered(side, connections, fluidHandler);
            }
        }
    }

    private final int[] rrIndex = new int[Direction.values().length];

    protected void insertEqually(Direction side, List<Connection> connections, IFluidHandler fluidHandler) {
        if (connections.isEmpty()) {
            return;
        }
        int completeAmount = getAmount(side);
        int mbToTransfer = completeAmount;
        boolean[] connectionsFull = new boolean[connections.size()];
        int p = rrIndex[side.getIndex()] % connections.size();
        while (mbToTransfer > 0 && hasNotInserted(connectionsFull)) {
            Connection connection = connections.get(p);
            IFluidHandler destination = getFluidHandler(connection.getPos(), connection.getDirection());
            boolean hasInserted = false;
            if (destination != null) {
                for (int j = 0; j < fluidHandler.getTanks(); j++) {
                    FluidStack fluidInTank = fluidHandler.getFluidInTank(j);
                    FluidStack simulatedExtract = fluidHandler.drain(new FluidStack(fluidInTank.getFluid(), Math.min(Math.max(completeAmount / connections.size(), 1), mbToTransfer), fluidInTank.getTag()), IFluidHandler.FluidAction.SIMULATE);
                    if (simulatedExtract.isEmpty()) {
                        continue;
                    }
                    if (canInsert(connection, simulatedExtract, getFilters(side)) == getFilterMode(side).equals(FilterMode.BLACKLIST)) {
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

        rrIndex[side.getIndex()] = p;
    }

    protected void insertOrdered(Direction side, List<Connection> connections, IFluidHandler fluidHandler) {
        int mbToTransfer = getAmount(side);

        connectionLoop:
        for (Connection connection : connections) {
            IFluidHandler destination = getFluidHandler(connection.getPos(), connection.getDirection());
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
                if (canInsert(connection, simulatedExtract, getFilters(side)) == getFilterMode(side).equals(FilterMode.BLACKLIST)) {
                    continue;
                }
                FluidStack stack = FluidUtil.tryFluidTransfer(destination, fluidHandler, simulatedExtract, true);
                mbToTransfer -= stack.getAmount();
            }
        }
    }

    private boolean canInsert(Connection connection, FluidStack stack, List<Filter<Fluid>> filters) {
        for (Filter<Fluid> filter : filters.stream().filter(Filter::isInvert).filter(f -> matchesConnection(connection, f)).collect(Collectors.toList())) {
            if (matches(filter, stack)) {
                return false;
            }
        }
        List<Filter<Fluid>> collect = filters.stream().filter(f -> !f.isInvert()).filter(f -> matchesConnection(connection, f)).collect(Collectors.toList());
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
        CompoundNBT metadata = filter.getMetadata();
        if (metadata == null) {
            return filter.getTag() == null || stack.getFluid().isIn(filter.getTag());
        }
        if (filter.isExactMetadata()) {
            if (deepExactCompare(metadata, stack.getTag())) {
                return filter.getTag() == null || stack.getFluid().isIn(filter.getTag());
            } else {
                return false;
            }
        } else {
            CompoundNBT stackNBT = stack.getTag();
            if (stackNBT == null) {
                return metadata.size() <= 0;
            }
            if (!deepFuzzyCompare(metadata, stackNBT)) {
                return false;
            }
            return filter.getTag() == null || stack.getFluid().isIn(filter.getTag());
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
    private IFluidHandler getFluidHandler(BlockPos pos, Direction direction) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity == null) {
            return null;
        }
        return tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, direction).orElse(null);
    }

    public int getAmount(Direction direction) {
        Upgrade upgrade = getUpgrade(direction);
        if (upgrade == null) {
            return Main.SERVER_CONFIG.fluidPipeAmount.get();
        }
        switch (upgrade) {
            case BASIC:
                return Main.SERVER_CONFIG.fluidPipeAmountBasic.get();
            case IMPROVED:
                return Main.SERVER_CONFIG.fluidPipeAmountImproved.get();
            case ADVANCED:
                return Main.SERVER_CONFIG.fluidPipeAmountAdvanced.get();
            case ULTIMATE:
                return Main.SERVER_CONFIG.fluidPipeAmountUltimate.get();
            case INFINITY:
            default:
                return Integer.MAX_VALUE;
        }
    }

}
