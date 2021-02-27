package de.maxhenkel.pipez.blocks.tileentity.types;

import de.maxhenkel.corelib.item.ItemUtils;
import de.maxhenkel.pipez.Filter;
import de.maxhenkel.pipez.ItemFilter;
import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.Upgrade;
import de.maxhenkel.pipez.blocks.ModBlocks;
import de.maxhenkel.pipez.blocks.tileentity.PipeLogicTileEntity;
import de.maxhenkel.pipez.blocks.tileentity.PipeTileEntity;
import de.maxhenkel.pipez.blocks.tileentity.UpgradeTileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ItemPipeType extends PipeType<Item> {

    public static final ItemPipeType INSTANCE = new ItemPipeType();

    @Override
    public String getKey() {
        return "Item";
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
    public ITextComponent getTransferText(@Nullable Upgrade upgrade) {
        return new TranslationTextComponent("tooltip.pipez.rate.item", getRate(upgrade), getSpeed(upgrade));
    }

    @Override
    public void tick(PipeLogicTileEntity tileEntity) {
        for (Direction side : Direction.values()) {
            if (tileEntity.getWorld().getGameTime() % getSpeed(tileEntity, side) != 0) {
                continue;
            }
            if (!tileEntity.isExtracting(side)) {
                continue;
            }
            if (!tileEntity.shouldWork(side, this)) {
                continue;
            }
            IItemHandler itemHandler = getItemHandler(tileEntity, tileEntity.getPos().offset(side), side.getOpposite());
            if (itemHandler == null) {
                continue;
            }

            List<PipeTileEntity.Connection> connections = tileEntity.getSortedConnections(side, this);

            if (tileEntity.getDistribution(side, this).equals(UpgradeTileEntity.Distribution.ROUND_ROBIN)) {
                insertEqually(tileEntity, side, connections, itemHandler);
            } else {
                insertOrdered(tileEntity, side, connections, itemHandler);
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
            IItemHandler destination = getItemHandler(tileEntity, connection.getPos(), connection.getDirection());
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

    protected void insertOrdered(PipeLogicTileEntity tileEntity, Direction side, List<PipeTileEntity.Connection> connections, IItemHandler itemHandler) {
        int itemsToTransfer = getRate(tileEntity, side);

        ArrayList<ItemStack> nonFittingItems = new ArrayList<>();

        connectionLoop:
        for (PipeTileEntity.Connection connection : connections) {
            nonFittingItems.clear();
            IItemHandler destination = getItemHandler(tileEntity, connection.getPos(), connection.getDirection());
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
            if (stackInSlot.getCount() < Math.min(stackInSlot.getMaxStackSize(), itemHandler.getSlotLimit(i))) {
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
    private IItemHandler getItemHandler(PipeLogicTileEntity tileEntity, BlockPos pos, Direction direction) {
        TileEntity te = tileEntity.getWorld().getTileEntity(pos);
        if (te == null) {
            return null;
        }
        return te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction).orElse(null);
    }

    public int getSpeed(PipeLogicTileEntity tileEntity, Direction direction) {
        return getSpeed(tileEntity.getUpgrade(direction));
    }

    public int getSpeed(@Nullable Upgrade upgrade) {
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
    public int getRate(@Nullable Upgrade upgrade) {
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
