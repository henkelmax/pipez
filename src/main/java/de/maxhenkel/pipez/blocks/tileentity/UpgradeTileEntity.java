package de.maxhenkel.pipez.blocks.tileentity;

import de.maxhenkel.corelib.inventory.ItemListInventory;
import de.maxhenkel.corelib.item.ItemUtils;
import de.maxhenkel.pipez.Filter;
import de.maxhenkel.pipez.Upgrade;
import de.maxhenkel.pipez.blocks.tileentity.configuration.DistributionCache;
import de.maxhenkel.pipez.blocks.tileentity.configuration.FilterCache;
import de.maxhenkel.pipez.blocks.tileentity.configuration.FilterModeCache;
import de.maxhenkel.pipez.blocks.tileentity.configuration.RedstoneModeCache;
import de.maxhenkel.pipez.blocks.tileentity.types.PipeType;
import de.maxhenkel.pipez.items.UpgradeItem;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public abstract class UpgradeTileEntity extends PipeTileEntity {

    protected final NonNullList<ItemStack> upgradeInventory;
    protected final ItemListInventory inventory;

    protected RedstoneModeCache redstoneModes;
    protected DistributionCache distributions;
    protected FilterModeCache filterModes;
    protected FilterCache filters;

    public UpgradeTileEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
        upgradeInventory = NonNullList.withSize(Direction.values().length, ItemStack.EMPTY);
        inventory = new ItemListInventory(upgradeInventory, this::invalidateAllCaches);
        initCaches();
    }

    public void initCaches() {
        redstoneModes = new RedstoneModeCache(() -> upgradeInventory, PipeType::getDefaultRedstoneMode, this::invalidateAllCaches);
        distributions = new DistributionCache(() -> upgradeInventory, PipeType::getDefaultDistribution, this::invalidateAllCaches);
        filterModes = new FilterModeCache(() -> upgradeInventory, PipeType::getDefaultFilterMode, this::invalidateAllCaches);
        filters = new FilterCache(() -> upgradeInventory, PipeType::createFilter, this::invalidateAllCaches);
    }

    public void invalidateAllCaches() {
        redstoneModes.invalidate();
        distributions.invalidate();
        filterModes.invalidate();
        filters.invalidate();
        setChanged();
    }

    public ItemStack setUpgradeItem(Direction side, ItemStack upgrade) {
        ItemStack old = upgradeInventory.get(side.get3DDataValue());
        UpgradeItem.upgradeData(upgrade); //TODO Remove after MC update
        upgradeInventory.set(side.get3DDataValue(), upgrade);
        invalidateAllCaches();
        return old;
    }

    public ItemStack getUpgradeItem(Direction side) {
        return upgradeInventory.get(side.get3DDataValue());
    }

    public RedstoneMode getRedstoneMode(Direction side, PipeType pipeType) {
        return redstoneModes.getValue(side, pipeType);
    }

    public void setRedstoneMode(Direction side, PipeType pipeType, RedstoneMode redstoneMode) {
        redstoneModes.setValue(side, pipeType, redstoneMode);
    }

    public Distribution getDistribution(Direction side, PipeType pipeType) {
        return distributions.getValue(side, pipeType);
    }

    public void setDistribution(Direction side, PipeType pipeType, Distribution distribution) {
        distributions.setValue(side, pipeType, distribution);
    }

    public FilterMode getFilterMode(Direction side, PipeType pipeType) {
        return filterModes.getValue(side, pipeType);
    }

    public void setFilterMode(Direction side, PipeType pipeType, FilterMode filterMode) {
        filterModes.setValue(side, pipeType, filterMode);
    }

    public <T> List<Filter<?>> getFilters(Direction side, PipeType<T> pipeType) {
        return filters.getValue(side, pipeType);
    }

    public <T> void setFilters(Direction side, PipeType<T> pipeType, List<Filter<?>> filter) {
        filters.setValue(side, pipeType, filter);
    }

    @Override
    public void setExtracting(Direction side, boolean extracting) {
        super.setExtracting(side, extracting);
        if (!extracting) {
            ItemStack stack = upgradeInventory.get(side.get3DDataValue());
            upgradeInventory.set(side.get3DDataValue(), ItemStack.EMPTY);
            InventoryHelper.dropContents(level, worldPosition, NonNullList.of(ItemStack.EMPTY, stack));
            setChanged();
        }
    }

    @Override
    public void setRemoved() {
        InventoryHelper.dropContents(level, worldPosition, upgradeInventory);
        super.setRemoved();
    }

    public IInventory getUpgradeInventory() {
        return inventory;
    }

    @Override
    public void load(BlockState state, CompoundNBT compound) {
        super.load(state, compound);
        upgradeInventory.clear();
        ItemUtils.readInventory(compound, "Upgrades", upgradeInventory);
        for (ItemStack stack : upgradeInventory) {
            UpgradeItem.upgradeData(stack); //TODO Remove after MC update
        }
        invalidateAllCaches();
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        ItemUtils.saveInventory(compound, "Upgrades", upgradeInventory);
        return super.save(compound);
    }

    @Nullable
    public Upgrade getUpgrade(Direction direction) {
        ItemStack stack = upgradeInventory.get(direction.get3DDataValue());
        if (stack.getItem() instanceof UpgradeItem) {
            return ((UpgradeItem) stack.getItem()).getTier();
        }
        return null;
    }

    public List<PipeTileEntity.Connection> getSortedConnections(Direction side, PipeType pipeType) {
        UpgradeTileEntity.Distribution distribution = getDistribution(side, pipeType);
        switch (distribution) {
            case FURTHEST:
                return getConnections().stream().sorted((o1, o2) -> Integer.compare(o2.getDistance(), o1.getDistance())).collect(Collectors.toList());
            case RANDOM:
                ArrayList<PipeTileEntity.Connection> shuffle = new ArrayList<>(getConnections());
                Collections.shuffle(shuffle);
                return shuffle;
            case NEAREST:
            case ROUND_ROBIN:
            default:
                return getConnections().stream().sorted(Comparator.comparingInt(PipeTileEntity.Connection::getDistance)).collect(Collectors.toList());
        }
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
        IGNORED("ignored"), OFF_WHEN_POWERED("off_when_powered"), ON_WHEN_POWERED("on_when_powered"), ALWAYS_OFF("always_off");
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
