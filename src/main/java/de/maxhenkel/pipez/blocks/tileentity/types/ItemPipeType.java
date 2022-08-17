package de.maxhenkel.pipez.blocks.tileentity.types;

import de.maxhenkel.corelib.item.ItemUtils;
import de.maxhenkel.pipez.Filter;
import de.maxhenkel.pipez.ItemFilter;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ItemPipeType extends PipeType<Item> {

    public static final ItemPipeType INSTANCE = new ItemPipeType();

    @Override
    public String getKey() {
        return "Item";
    }

    @Override
    public boolean canInsert(BlockEntity tileEntity, Direction direction) {
        return tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction).isPresent();
    }

    @Override
    public Filter<Item> createFilter() {
        return new ItemFilter();
    }

    @Override
    public UpgradeTileEntity.Distribution getDefaultDistribution() {
        return UpgradeTileEntity.Distribution.NEAREST;
    }

    @Override
    public String getTranslationKey() {
        return "tooltip.pipez.item";
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(ModBlocks.ITEM_PIPE);
    }

    @Override
    public Component getTransferText(@Nullable Upgrade upgrade) {
        return new TranslatableComponent("tooltip.pipez.rate.item", getRate(upgrade), getSpeed(upgrade));
    }

    @Override
    public void tick(PipeLogicTileEntity tileEntity) {
        Level worldLevel = tileEntity.getLevel();
        long gameTime = worldLevel.getGameTime();
        for (Direction side : Direction.values()) {
            if (!tileEntity.isExtracting(side)) {
                continue;
            }
            if (!tileEntity.shouldWork(side, this)) {
                continue;
            }
            if (gameTime % getSpeed(tileEntity, side) != 0) {
                continue;
            }
            var itemHandler = CapabilityCacheOld.getInstance().getItemCapabilityResult(
                    worldLevel,
                    tileEntity.getBlockPos().relative(side),
                    side.getOpposite()
            );
            if (itemHandler == null) {
                continue;
            }

            // Check there is any item to put of.
            boolean isEmptyItem = true;
            for (int i = 0; i < itemHandler.getSlots(); i += 1) {
                if (!itemHandler.getStackInSlot(i).isEmpty()) {
                    isEmptyItem = false;
                    break;
                }
            }
            if (isEmptyItem) {
                continue;
            }

            List<PipeTileEntity.Connection> connections = tileEntity.getConnections(); // tileEntity.getSortedConnections(side, this);
            var distribution = tileEntity.getDistribution(side, this); // UpgradeTileEntity.Distribution.ROUND_ROBIN...

            if (distribution.equals(UpgradeTileEntity.Distribution.ROUND_ROBIN)) {
                insertEqually(tileEntity, side, connections, itemHandler);
                // insertEquality(tileEntity, side, connections, gasHandler);
            } else {
                if (distribution.equals(UpgradeTileEntity.Distribution.RANDOM)) {
                    connections = new ArrayList<>(connections);
                    Collections.shuffle(connections);
                }
                insertOrdered(tileEntity, side, connections, itemHandler,
                        distribution.equals(UpgradeTileEntity.Distribution.FURTHEST));
            }
        }
    }

    protected void insertEqually(PipeLogicTileEntity tileEntity, Direction side, List<PipeTileEntity.Connection> connections, IItemHandler itemHandler) {
        if (connections.isEmpty()) {
            return;
        }
        int itemsToTransfer = getRate(tileEntity, side);
        boolean[] inventoriesFull = new boolean[connections.size()];
        int p = tileEntity.getRoundRobinIndex(side, this) % connections.size();
        while (itemsToTransfer > 0 && hasNotInserted(inventoriesFull)) {
            PipeTileEntity.Connection connection = connections.get(p);
            IItemHandler destination = getItemHandler(tileEntity.getLevel(), connection.getPos(), connection.getDirection());
            boolean hasInserted = false;
            if (destination != null && !inventoriesFull[p] && !isFull(destination)) {
                for (int j = 0; j < itemHandler.getSlots(); j++) {
                    ItemStack simulatedExtract = itemHandler.extractItem(j, 1, true);
                    if (simulatedExtract.isEmpty()) {
                        continue;
                    }
                    if (canInsert(connection, simulatedExtract, tileEntity.getFilters(side, this)) == tileEntity.getFilterMode(side, this).equals(UpgradeTileEntity.FilterMode.BLACKLIST)) {
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

        tileEntity.setRoundRobinIndex(side, this, p);
    }

    protected void insertOrdered(PipeLogicTileEntity tileEntity, Direction side, List<PipeTileEntity.Connection> connections, IItemHandler itemHandler, boolean inverted) {
        int itemsToTransfer = getRate(tileEntity, side);

        ArrayList<ItemStack> nonFittingItems = new ArrayList<>();

        connectionLoop:
        for (int k = 0; k < connections.size(); k += 1) {
            var connection = connections.get(inverted ? (connections.size() - k - 1) : k);
            nonFittingItems.clear();
            IItemHandler destination = getItemHandler(tileEntity.getLevel(), connection.getPos(), connection.getDirection());
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
                if (nonFittingItems.stream().anyMatch(stack -> ItemUtils.isStackable(stack, simulatedExtract))) {
                    continue;
                }
                if (canInsert(connection, simulatedExtract, tileEntity.getFilters(side, this)) == tileEntity.getFilterMode(side, this).equals(UpgradeTileEntity.FilterMode.BLACKLIST)) {
                    continue;
                }
                ItemStack stack = ItemHandlerHelper.insertItem(destination, simulatedExtract, false);
                int insertedAmount = simulatedExtract.getCount() - stack.getCount();
                if (insertedAmount <= 0) {
                    nonFittingItems.add(simulatedExtract);
                }
                itemsToTransfer -= insertedAmount;
                itemHandler.extractItem(i, insertedAmount, false);
            }
        }
    }

    private boolean isFull(IItemHandler itemHandler) {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack stackInSlot = itemHandler.getStackInSlot(i);
            if (stackInSlot.getCount() < itemHandler.getSlotLimit(i)) {
                return false;
            }
        }
        return true;
    }

    private boolean canInsert(PipeTileEntity.Connection connection, ItemStack stack, List<Filter<?>> filters) {
        for (Filter<Item> filter : filters.stream().map(filter -> (Filter<Item>) filter).filter(Filter::isInvert).filter(f -> matchesConnection(connection, f)).collect(Collectors.toList())) {
            if (matches(filter, stack)) {
                return false;
            }
        }
        List<Filter<Item>> collect = filters.stream().map(filter -> (Filter<Item>) filter).filter(f -> !f.isInvert()).filter(f -> matchesConnection(connection, f)).collect(Collectors.toList());
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
        CompoundTag metadata = filter.getMetadata();
        if (metadata == null) {
            return filter.getTag() == null || filter.getTag().contains(stack.getItem());
        }
        if (filter.isExactMetadata()) {
            if (deepExactCompare(metadata, stack.getTag())) {
                return filter.getTag() == null || filter.getTag().contains(stack.getItem());
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
            return filter.getTag() == null || filter.getTag().contains(stack.getItem());
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
    private IItemHandler getItemHandler(Level level, BlockPos pos, Direction direction) {
        return CapabilityCacheOld.getInstance().getItemCapabilityResult(level, pos, direction, true);
    }

    public int getSpeed(PipeLogicTileEntity tileEntity, Direction direction) {
        return getSpeed(tileEntity.getUpgrade(direction));
    }

    public int getSpeed(@Nullable Upgrade upgrade) {
        return 2000;
    }

    @Override
    public int getRate(@Nullable Upgrade upgrade) {
        return 1;
    }
}
