package de.maxhenkel.pipez.blocks.tileentity;

import de.maxhenkel.corelib.CachedValue;
import de.maxhenkel.corelib.inventory.ItemListInventory;
import de.maxhenkel.corelib.item.ItemUtils;
import de.maxhenkel.pipez.Filter;
import de.maxhenkel.pipez.Upgrade;
import de.maxhenkel.pipez.items.UpgradeItem;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public abstract class UpgradeTileEntity extends PipeTileEntity {

    protected NonNullList<ItemStack> upgradeInventory;
    protected CachedValue<Distribution>[] distributions;
    protected CachedValue<RedstoneMode>[] redstoneModes;
    protected CachedValue<FilterMode>[] filterModes;
    protected CachedValue<List<Filter<?>>>[] filters;

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

    private List<Filter<?>> deserializeFilters(ItemStack stack) {
        if (stack.isEmpty()) {
            return Collections.emptyList();
        }
        CompoundNBT compound = stack.getTag();
        if (compound == null) {
            return Collections.emptyList();
        }
        if (!compound.contains(getFilterKey(), Constants.NBT.TAG_LIST)) {
            return Collections.emptyList();
        }
        ListNBT list = compound.getList(getFilterKey(), Constants.NBT.TAG_COMPOUND);
        List<Filter<?>> filters = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Filter<?> filter = createFilter();
            filter.deserializeNBT(list.getCompound(i));
            filters.add(filter);
        }
        return filters;
    }

    private void serializeFilters(ItemStack stack, List<Filter<?>> filters) {
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

    public abstract Filter<?> createFilter();

    public abstract String getFilterKey();

    public List<Filter<?>> getFilters(Direction side) {
        return filters[side.getIndex()].get();
    }

    public void setFilters(Direction side, List<Filter<?>> f) {
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
