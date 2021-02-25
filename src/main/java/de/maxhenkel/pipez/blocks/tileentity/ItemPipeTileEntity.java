package de.maxhenkel.pipez.blocks.tileentity;

import de.maxhenkel.pipez.Filter;
import de.maxhenkel.pipez.ItemFilter;
import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.Upgrade;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

public class ItemPipeTileEntity extends UpgradeLogicTileEntity<Item> {

    public ItemPipeTileEntity() {
        super(ModTileEntities.ITEM_PIPE);
    }

    @Override
    public boolean canInsert(TileEntity tileEntity, Direction direction) {
        return tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction).isPresent();
    }

    @Override
    public Filter<Item> createFilter() {
        return new ItemFilter();
    }

    @Override
    public String getFilterKey() {
        return "ItemFilters";
    }

    @Override
    public Distribution getDefaultDistribution() {
        return Distribution.NEAREST;
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
            if (world.getGameTime() % getSpeed(side) != 0) {
                continue;
            }
            if (!isExtracting(side)) {
                continue;
            }
            if (!shouldWork(side)) {
                continue;
            }
            IItemHandler itemHandler = getItemHandler(pos.offset(side), side.getOpposite());
            if (itemHandler == null) {
                continue;
            }

            List<Connection> connections = getSortedConnections(side);

            if (getDistribution(side).equals(Distribution.ROUND_ROBIN)) {
                insertEqually(side, connections, itemHandler);
            } else {
                insertOrdered(side, connections, itemHandler);
            }
        }
    }

    private final int[] rrIndex = new int[Direction.values().length];

    protected void insertEqually(Direction side, List<Connection> connections, IItemHandler itemHandler) {
        if (connections.isEmpty()) {
            return;
        }
        int itemsToTransfer = getRate(side);
        boolean[] inventoriesFull = new boolean[connections.size()];
        int p = rrIndex[side.getIndex()] % connections.size();
        while (itemsToTransfer > 0 && hasNotInserted(inventoriesFull)) {
            Connection connection = connections.get(p);
            IItemHandler destination = getItemHandler(connection.getPos(), connection.getDirection());
            boolean hasInserted = false;
            if (destination != null && !inventoriesFull[p] && !isFull(destination)) {
                for (int j = 0; j < itemHandler.getSlots(); j++) {
                    ItemStack simulatedExtract = itemHandler.extractItem(j, 1, true);
                    if (simulatedExtract.isEmpty()) {
                        continue;
                    }
                    if (canInsert(connection, simulatedExtract, getFilters(side)) == getFilterMode(side).equals(FilterMode.BLACKLIST)) {
                        continue;
                    }
                    ItemStack stack = ItemHandlerHelper.insertItem(destination, simulatedExtract, false);
                    int insertedAmount = simulatedExtract.getCount() - stack.getCount();
                    if (insertedAmount > 0) {
                        itemsToTransfer -= insertedAmount;
                        itemHandler.extractItem(j, insertedAmount, false);
                        hasInserted = true;
                        break;
                    }
                }
            }
            if (!hasInserted) {
                inventoriesFull[p] = true;
            }
            p = (p + 1) % connections.size();
        }

        rrIndex[side.getIndex()] = p;
    }

    protected void insertOrdered(Direction side, List<Connection> connections, IItemHandler itemHandler) {
        int itemsToTransfer = getRate(side);

        connectionLoop:
        for (Connection connection : connections) {
            IItemHandler destination = getItemHandler(connection.getPos(), connection.getDirection());
            if (destination == null) {
                continue;
            }
            if (isFull(destination)) {
                continue;
            }
            for (int i = 0; i < itemHandler.getSlots(); i++) {
                if (itemsToTransfer <= 0) {
                    break connectionLoop;
                }
                ItemStack simulatedExtract = itemHandler.extractItem(i, itemsToTransfer, true);
                if (simulatedExtract.isEmpty()) {
                    continue;
                }
                if (canInsert(connection, simulatedExtract, getFilters(side)) == getFilterMode(side).equals(FilterMode.BLACKLIST)) {
                    continue;
                }
                ItemStack stack = ItemHandlerHelper.insertItem(destination, simulatedExtract, false);
                int insertedAmount = simulatedExtract.getCount() - stack.getCount();
                itemsToTransfer -= insertedAmount;
                itemHandler.extractItem(i, insertedAmount, false);
            }
        }
    }

    private boolean isFull(IItemHandler itemHandler) {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack stackInSlot = itemHandler.getStackInSlot(i);
            if (stackInSlot.getCount() < Math.min(stackInSlot.getMaxStackSize(), itemHandler.getSlotLimit(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean canInsert(Connection connection, ItemStack stack, List<Filter<Item>> filters) {
        for (Filter<Item> filter : filters.stream().filter(Filter::isInvert).filter(f -> matchesConnection(connection, f)).collect(Collectors.toList())) {
            if (matches(filter, stack)) {
                return false;
            }
        }
        List<Filter<Item>> collect = filters.stream().filter(f -> !f.isInvert()).filter(f -> matchesConnection(connection, f)).collect(Collectors.toList());
        if (collect.isEmpty()) {
            return true;
        }
        for (Filter<Item> filter : collect) {
            if (matches(filter, stack)) {
                return true;
            }
        }
        return false;
    }

    private boolean matches(Filter<Item> filter, ItemStack stack) {
        CompoundNBT metadata = filter.getMetadata();
        if (metadata == null) {
            return filter.getTag() == null || stack.getItem().isIn(filter.getTag());
        }
        if (filter.isExactMetadata()) {
            if (deepExactCompare(metadata, stack.getTag())) {
                return filter.getTag() == null || stack.getItem().isIn(filter.getTag());
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
            return filter.getTag() == null || stack.getItem().isIn(filter.getTag());
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
    private IItemHandler getItemHandler(BlockPos pos, Direction direction) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity == null) {
            return null;
        }
        return tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction).orElse(null);
    }

    public int getSpeed(Direction direction) {
        Upgrade upgrade = getUpgrade(direction);
        if (upgrade == null) {
            return Main.SERVER_CONFIG.itemPipeSpeed.get();
        }
        switch (upgrade) {
            case BASIC:
                return Main.SERVER_CONFIG.itemPipeSpeedBasic.get();
            case IMPROVED:
                return Main.SERVER_CONFIG.itemPipeSpeedImproved.get();
            case ADVANCED:
                return Main.SERVER_CONFIG.itemPipeSpeedAdvanced.get();
            case ULTIMATE:
                return Main.SERVER_CONFIG.itemPipeSpeedUltimate.get();
            case INFINITY:
            default:
                return 1;
        }
    }

    @Override
    public int getRate(Direction direction) {
        Upgrade upgrade = getUpgrade(direction);
        if (upgrade == null) {
            return Main.SERVER_CONFIG.itemPipeAmount.get();
        }
        switch (upgrade) {
            case BASIC:
                return Main.SERVER_CONFIG.itemPipeAmountBasic.get();
            case IMPROVED:
                return Main.SERVER_CONFIG.itemPipeAmountImproved.get();
            case ADVANCED:
                return Main.SERVER_CONFIG.itemPipeAmountAdvanced.get();
            case ULTIMATE:
                return Main.SERVER_CONFIG.itemPipeAmountUltimate.get();
            case INFINITY:
            default:
                return Integer.MAX_VALUE;
        }
    }

}
