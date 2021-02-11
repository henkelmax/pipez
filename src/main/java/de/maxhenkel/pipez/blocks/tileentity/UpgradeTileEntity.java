package de.maxhenkel.pipez.blocks.tileentity;

import de.maxhenkel.corelib.CachedValue;
import de.maxhenkel.corelib.inventory.ItemListInventory;
import de.maxhenkel.corelib.item.ItemUtils;
import de.maxhenkel.pipez.DirectionalPosition;
import de.maxhenkel.pipez.Filter;
import de.maxhenkel.pipez.Upgrade;
import de.maxhenkel.pipez.items.UpgradeItem;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class UpgradeTileEntity<T> extends PipeTileEntity {

    protected NonNullList<ItemStack> upgradeInventory;
    protected CachedValue<Distribution>[] distributions;
    protected CachedValue<RedstoneMode>[] redstoneModes;
    protected CachedValue<FilterMode>[] filterModes;
    protected CachedValue<List<Filter<T>>>[] filters;

    public UpgradeTileEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
        upgradeInventory = NonNullList.withSize(Direction.values().length, ItemStack.EMPTY);
        initCaches();
    }

    public void initCaches() {
        distributions = new CachedValue[Direction.values().length];
        redstoneModes = new CachedValue[Direction.values().length];
        filterModes = new CachedValue[Direction.values().length];
        filters = new CachedValue[Direction.values().length];
        for (int i = 0; i < Direction.values().length; i++) {
            int index = i;
            distributions[i] = new CachedValue<>(() -> deserialize(upgradeInventory.get(index), "Distribution", Distribution.class, this::getDefaultDistribution));
            redstoneModes[i] = new CachedValue<>(() -> deserialize(upgradeInventory.get(index), "RedstoneMode", RedstoneMode.class, this::getDefaultRedstoneMode));
            filterModes[i] = new CachedValue<>(() -> deserialize(upgradeInventory.get(index), "FilterMode", FilterMode.class, this::getDefaultFilterMode));
            filters[i] = new CachedValue<>(() -> deserializeFilters(upgradeInventory.get(index)));
        }
    }

    public void invalidateAllCaches() {
        for (int i = 0; i < Direction.values().length; i++) {
            distributions[i].invalidate();
            redstoneModes[i].invalidate();
            filterModes[i].invalidate();
            filters[i].invalidate();
        }
    }

    private static <T extends Enum<? extends ICyclable<T>>> T deserialize(ItemStack stack, String key, Class<T> clazz, Supplier<T> defaultSupplier) {
        if (stack.isEmpty()) {
            return defaultSupplier.get();
        }
        CompoundNBT tag = stack.getTag();
        if (tag == null) {
            return defaultSupplier.get();
        }
        if (!tag.contains(key, Constants.NBT.TAG_BYTE)) {
            return defaultSupplier.get();
        }
        byte b = tag.getByte(key);

        T[] enumConstants = clazz.getEnumConstants();
        if (b < 0 || b >= enumConstants.length) {
            return defaultSupplier.get();
        }
        return enumConstants[b];
    }

    private <T extends Enum<? extends ICyclable<T>>> void serialize(ItemStack stack, String key, T value) {
        if (stack.isEmpty()) {
            return;
        }
        CompoundNBT tag = stack.getOrCreateTag();
        tag.putByte(key, (byte) value.ordinal());
        markDirty();
    }

    private List<Filter<T>> deserializeFilters(ItemStack stack) {
        if (!hasFilter()) {
            return new ArrayList<>();
        }
        if (stack.isEmpty()) {
            return new ArrayList<>();
        }
        CompoundNBT compound = stack.getTag();
        if (compound == null) {
            return new ArrayList<>();
        }
        if (!compound.contains(getFilterKey(), Constants.NBT.TAG_LIST)) {
            return new ArrayList<>();
        }
        ListNBT list = compound.getList(getFilterKey(), Constants.NBT.TAG_COMPOUND);
        List<Filter<T>> filters = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Filter<T> filter = createFilter();
            filter.deserializeNBT(list.getCompound(i));
            filters.add(filter);
        }
        return filters;
    }

    private void serializeFilters(ItemStack stack, List<Filter<T>> filters) {
        if (!hasFilter()) {
            return;
        }
        if (stack.isEmpty()) {
            return;
        }
        CompoundNBT tag = stack.getOrCreateTag();
        ListNBT list = new ListNBT();
        for (Filter<?> filter : filters) {
            list.add(filter.serializeNBT());
        }
        tag.put(getFilterKey(), list);
        markDirty();
    }

    public abstract Filter<T> createFilter();

    public abstract String getFilterKey();

    public boolean hasFilter() {
        return true;
    }

    public List<Filter<T>> getFilters(Direction side) {
        return filters[side.getIndex()].get();
    }

    public void setFilters(Direction side, List<Filter<T>> f) {
        serializeFilters(upgradeInventory.get(side.getIndex()), f);
        filters[side.getIndex()].invalidate();
    }

    public void setDistribution(Direction side, Distribution dist) {
        serialize(upgradeInventory.get(side.getIndex()), "Distribution", dist);
        distributions[side.getIndex()].invalidate();
    }

    public void setRedstoneMode(Direction side, RedstoneMode mode) {
        serialize(upgradeInventory.get(side.getIndex()), "RedstoneMode", mode);
        redstoneModes[side.getIndex()].invalidate();
    }

    public void setFilterMode(Direction side, FilterMode mode) {
        serialize(upgradeInventory.get(side.getIndex()), "FilterMode", mode);
        filterModes[side.getIndex()].invalidate();
    }

    public Distribution getDistribution(Direction side) {
        return distributions[side.getIndex()].get();
    }

    public RedstoneMode getRedstoneMode(Direction side) {
        return redstoneModes[side.getIndex()].get();
    }

    public FilterMode getFilterMode(Direction side) {
        return filterModes[side.getIndex()].get();
    }

    public abstract Distribution getDefaultDistribution();

    public abstract RedstoneMode getDefaultRedstoneMode();

    public abstract FilterMode getDefaultFilterMode();

    @Override
    public void setExtracting(Direction side, boolean extracting) {
        super.setExtracting(side, extracting);
        if (!extracting) {
            ItemStack stack = upgradeInventory.get(side.getIndex());
            upgradeInventory.set(side.getIndex(), ItemStack.EMPTY);
            InventoryHelper.dropItems(world, pos, NonNullList.from(ItemStack.EMPTY, stack));
            markDirty();
        }
    }

    @Override
    public void remove() {
        InventoryHelper.dropItems(world, pos, upgradeInventory);
        super.remove();
    }

    public IInventory getUpgradeInventory() {
        return new ItemListInventory(upgradeInventory, () -> {
            invalidateAllCaches();
            markDirty();
        });
    }

    @Override
    public void read(BlockState state, CompoundNBT compound) {
        super.read(state, compound);
        upgradeInventory.clear();
        ItemUtils.readInventory(compound, "Upgrades", upgradeInventory);
        invalidateAllCaches();
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        ItemUtils.saveInventory(compound, "Upgrades", upgradeInventory);
        return super.write(compound);
    }

    @Nullable
    public Upgrade getUpgrade(Direction direction) {
        ItemStack stack = upgradeInventory.get(direction.getIndex());
        if (stack.getItem() instanceof UpgradeItem) {
            return ((UpgradeItem) stack.getItem()).getTier();
        }
        return null;
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

    public boolean matchesConnection(Connection connection, Filter<T> filter) {
        if (filter.getDestination() == null) {
            return true;
        }
        return filter.getDestination().equals(new DirectionalPosition(connection.getPos(), connection.getDirection()));
    }

    public boolean deepExactCompare(INBT meta, INBT item) {
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

    public boolean deepFuzzyCompare(INBT meta, INBT item) {
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

    public abstract int getRate(Direction direction);

    public enum Distribution implements ICyclable<Distribution> {
        NEAREST("nearest"), FURTHEST("furthest"), ROUND_ROBIN("round_robin"), RANDOM("random");

        private final String name;

        Distribution(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public Distribution cycle() {
            return values()[Math.floorMod(ordinal() + 1, values().length)];
        }
    }

    public enum RedstoneMode implements ICyclable<RedstoneMode> {
        IGNORED("ignored"), OFF_WHEN_POWERED("off_when_powered"), ON_WHEN_POWERED("on_when_powered");
        private final String name;

        RedstoneMode(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public RedstoneMode cycle() {
            return values()[Math.floorMod(ordinal() + 1, values().length)];
        }
    }

    public enum FilterMode implements ICyclable<FilterMode> {
        WHITELIST("whitelist"), BLACKLIST("blacklist");
        private final String name;

        FilterMode(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public FilterMode cycle() {
            return values()[Math.floorMod(ordinal() + 1, values().length)];
        }
    }

    public interface ICyclable<T extends Enum<?>> {
        T cycle();
    }

}
