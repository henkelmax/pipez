package de.maxhenkel.pipez.blocks.tileentity;

import de.maxhenkel.pipez.DirectionalPosition;
import de.maxhenkel.pipez.Filter;
import de.maxhenkel.pipez.ItemFilter;
import de.maxhenkel.pipez.Upgrade;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class ItemPipeTileEntity extends UpgradeLogicTileEntity<Item> {

    public ItemPipeTileEntity() {
        super(ModTileEntities.ITEM_PIPE);
    }

    @Override
    public boolean canInsert(TileEntity tileEntity, Direction direction) {
        return tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite()).isPresent();
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
        int itemsToTransfer = getAmount(side);
        boolean[] inventoriesFull = new boolean[connections.size()];
        int p = rrIndex[side.getIndex()] % connections.size();
        while (itemsToTransfer > 0 && hasNotInserted(inventoriesFull)) {
            Connection connection = connections.get(p);
            IItemHandler destination = getItemHandler(connection.getPos(), connection.getDirection());
            boolean hasInserted = false;
            if (destination != null) {
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
        int itemsToTransfer = getAmount(side);

        connectionLoop:
        for (Connection connection : connections) {
            IItemHandler destination = getItemHandler(connection.getPos(), connection.getDirection());
            if (destination == null) {
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

    private boolean matchesConnection(Connection connection, Filter<Item> filter) {
        if (filter.getDestination() == null) {
            return true;
        }
        return filter.getDestination().equals(new DirectionalPosition(connection.getPos(), connection.getDirection()));
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

    private boolean deepExactCompare(INBT meta, INBT item) {
        if (meta instanceof CompoundNBT) {
            if (!(item instanceof CompoundNBT)) {
                return false;
            }
            CompoundNBT c = (CompoundNBT) meta;
            CompoundNBT i = (CompoundNBT) item;
            Set<String> allKeys = new HashSet<>();
            allKeys.addAll(c.keySet());
            allKeys.addAll(i.keySet());
            for (String key : allKeys) {
                if (c.contains(key)) {
                    if (i.contains(key)) {
                        INBT nbt = c.get(key);
                        if (!deepExactCompare(nbt, i.get(key))) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
            return true;
        } else if (meta instanceof ListNBT) {
            ListNBT l = (ListNBT) meta;
            if (!(item instanceof ListNBT)) {
                return false;
            }
            ListNBT il = (ListNBT) item;
            if (!l.stream().allMatch(inbt -> il.stream().anyMatch(inbt1 -> deepExactCompare(inbt, inbt1)))) {
                return false;
            }
            if (!il.stream().allMatch(inbt -> l.stream().anyMatch(inbt1 -> deepExactCompare(inbt, inbt1)))) {
                return false;
            }
            return true;
        } else {
            return meta != null && meta.equals(item);
        }
    }

    private boolean deepFuzzyCompare(INBT meta, INBT item) {
        if (meta instanceof CompoundNBT) {
            if (!(item instanceof CompoundNBT)) {
                return false;
            }
            CompoundNBT c = (CompoundNBT) meta;
            CompoundNBT i = (CompoundNBT) item;
            for (String key : c.keySet()) {
                INBT nbt = c.get(key);
                if (i.contains(key, nbt.getId())) {
                    if (!deepFuzzyCompare(nbt, i.get(key))) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
            return true;
        } else if (meta instanceof ListNBT) {
            ListNBT l = (ListNBT) meta;
            if (!(item instanceof ListNBT)) {
                return false;
            }
            ListNBT il = (ListNBT) item;
            return l.stream().allMatch(inbt -> il.stream().anyMatch(inbt1 -> deepFuzzyCompare(inbt, inbt1)));
        } else {
            return meta != null && meta.equals(item);
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

    public List<Connection> getSortedConnections(Direction side) {
        Distribution distribution = getDistribution(side);
        switch (distribution) {
            case FURTHEST:
                return getConnections().stream().sorted((o1, o2) -> Integer.compare(o2.getDistance(), o1.getDistance())).collect(Collectors.toList());
            case RANDOM:
                ArrayList<Connection> shuffle = new ArrayList<>(getConnections());
                Collections.shuffle(shuffle);
                return shuffle;
            case NEAREST:
            case ROUND_ROBIN:
            default:
                return getConnections().stream().sorted(Comparator.comparingInt(Connection::getDistance)).collect(Collectors.toList());
        }
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
            return 20;
        }
        switch (upgrade) {
            case BASIC:
                return 15;
            case IMPROVED:
                return 10;
            case ADVANCED:
                return 5;
            case ULTIMATE:
            default:
                return 1;
        }
    }

    public int getAmount(Direction direction) {
        Upgrade upgrade = getUpgrade(direction);
        if (upgrade == null) {
            return 4;
        }
        switch (upgrade) {
            case BASIC:
                return 8;
            case IMPROVED:
                return 16;
            case ADVANCED:
                return 32;
            case ULTIMATE:
            default:
                return 64;
        }
    }

}
