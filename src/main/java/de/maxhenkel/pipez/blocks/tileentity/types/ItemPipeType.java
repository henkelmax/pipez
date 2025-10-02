package de.maxhenkel.pipez.blocks.tileentity.types;

import de.maxhenkel.pipez.*;
import de.maxhenkel.pipez.blocks.ModBlocks;
import de.maxhenkel.pipez.blocks.tileentity.PipeLogicTileEntity;
import de.maxhenkel.pipez.blocks.tileentity.PipeTileEntity;
import de.maxhenkel.pipez.blocks.tileentity.UpgradeTileEntity;
import de.maxhenkel.pipez.datacomponents.ItemData;
import de.maxhenkel.pipez.items.ModItems;
import de.maxhenkel.pipez.utils.ComponentUtils;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ItemPipeType extends PipeType<Item, ItemData> {

    public static final ItemPipeType INSTANCE = new ItemPipeType();

    @Override
    public BlockCapability<?, Direction> getCapability() {
        return Capabilities.Item.BLOCK;
    }

    @Override
    public Filter<?, Item> createFilter() {
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
        return new ItemStack(ModBlocks.ITEM_PIPE.get());
    }

    @Override
    public Component getTransferText(@Nullable Upgrade upgrade) {
        return Component.translatable("tooltip.pipez.rate.item", getRate(upgrade), getSpeed(upgrade));
    }

    @Override
    public void tick(PipeLogicTileEntity tileEntity) {
        for (Direction side : Direction.values()) {
            if (tileEntity.getLevel().getGameTime() % getSpeed(tileEntity, side) != 0) {
                continue;
            }
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
            ResourceHandler<ItemResource> itemHandler = extractingConnection.getItemHandler();
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

    protected void insertEqually(PipeLogicTileEntity tileEntity, Direction side, List<PipeTileEntity.Connection> connections, ResourceHandler<ItemResource> itemHandler) {
        if (connections.isEmpty()) {
            return;
        }
        int itemsToTransfer = getRate(tileEntity, side);
        boolean[] inventoriesFull = new boolean[connections.size()];
        int p = tileEntity.getRoundRobinIndex(side, this) % connections.size();
        while (itemsToTransfer > 0 && hasNotInserted(inventoriesFull)) {
            PipeTileEntity.Connection connection = connections.get(p);
            ResourceHandler<ItemResource> destination = connection.getItemHandler();
            boolean hasInserted = false;
            if (destination != null && !inventoriesFull[p] && !ResourceHandlerUtil.isFull(destination)) {
                int moved = ResourceHandlerUtil.move(itemHandler, destination, resource -> {
                    return canInsert(tileEntity.getLevel().registryAccess(), connection, resource.toStack(), tileEntity.getFilters(side, this)) != tileEntity.getFilterMode(side, this).equals(UpgradeTileEntity.FilterMode.BLACKLIST);
                }, 1, null);
                if (moved > 0) {
                    hasInserted = true;
                    itemsToTransfer -= moved;
                }
            }
            if (!hasInserted) {
                inventoriesFull[p] = true;
            }
            p = (p + 1) % connections.size();
        }

        tileEntity.setRoundRobinIndex(side, this, p);
    }

    protected void insertOrdered(PipeLogicTileEntity tileEntity, Direction side, List<PipeTileEntity.Connection> connections, ResourceHandler<ItemResource> itemHandler) {
        int itemsToTransfer = getRate(tileEntity, side);

        for (PipeTileEntity.Connection connection : connections) {
            ResourceHandler<ItemResource> destination = connection.getItemHandler();
            if (destination == null) {
                continue;
            }
            if (ResourceHandlerUtil.isFull(destination)) {
                continue;
            }
            int moved = ResourceHandlerUtil.move(itemHandler, destination, resource -> {
                return canInsert(tileEntity.getLevel().registryAccess(), connection, resource.toStack(), tileEntity.getFilters(side, this)) != tileEntity.getFilterMode(side, this).equals(UpgradeTileEntity.FilterMode.BLACKLIST);
            }, itemsToTransfer, null);

            itemsToTransfer -= moved;

            if (itemsToTransfer <= 0) {
                break;
            }
        }
    }

    private boolean canInsert(HolderLookup.Provider provider, PipeTileEntity.Connection connection, ItemStack stack, List<Filter<?, ?>> filters) {
        for (Filter<?, Item> filter : filters.stream().map(filter -> (Filter<?, Item>) filter).filter(Filter::isInvert).filter(f -> matchesConnection(connection, f)).collect(Collectors.toList())) {
            if (matches(provider, filter, stack)) {
                return false;
            }
        }
        List<Filter<?, Item>> collect = filters.stream().map(filter -> (Filter<?, Item>) filter).filter(f -> !f.isInvert()).filter(f -> matchesConnection(connection, f)).collect(Collectors.toList());
        if (collect.isEmpty()) {
            return true;
        }
        for (Filter<?, Item> filter : collect) {
            if (matches(provider, filter, stack)) {
                return true;
            }
        }
        return false;
    }

    private boolean matches(HolderLookup.Provider provider, Filter<?, Item> filter, ItemStack stack) {
        CompoundTag metadata = filter.getMetadata();
        if (metadata == null) {
            return filter.getTag() == null || filter.getTag().contains(stack.getItem());
        }
        CompoundTag stackNBT = ComponentUtils.getTag(provider, stack);
        if (filter.isExactMetadata()) {
            if (deepExactCompare(metadata, stackNBT)) {
                return filter.getTag() == null || filter.getTag().contains(stack.getItem());
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

    public int getSpeed(PipeLogicTileEntity tileEntity, Direction direction) {
        return getSpeed(tileEntity.getUpgrade(direction));
    }

    public int getSpeed(@Nullable Upgrade upgrade) {
        if (upgrade == null) {
            return PipezMod.SERVER_CONFIG.itemPipeSpeed.get();
        }
        switch (upgrade) {
            case BASIC:
                return PipezMod.SERVER_CONFIG.itemPipeSpeedBasic.get();
            case IMPROVED:
                return PipezMod.SERVER_CONFIG.itemPipeSpeedImproved.get();
            case ADVANCED:
                return PipezMod.SERVER_CONFIG.itemPipeSpeedAdvanced.get();
            case ULTIMATE:
                return PipezMod.SERVER_CONFIG.itemPipeSpeedUltimate.get();
            case INFINITY:
            default:
                return 1;
        }
    }

    @Override
    public int getRate(@Nullable Upgrade upgrade) {
        if (upgrade == null) {
            return PipezMod.SERVER_CONFIG.itemPipeAmount.get();
        }
        switch (upgrade) {
            case BASIC:
                return PipezMod.SERVER_CONFIG.itemPipeAmountBasic.get();
            case IMPROVED:
                return PipezMod.SERVER_CONFIG.itemPipeAmountImproved.get();
            case ADVANCED:
                return PipezMod.SERVER_CONFIG.itemPipeAmountAdvanced.get();
            case ULTIMATE:
                return PipezMod.SERVER_CONFIG.itemPipeAmountUltimate.get();
            case INFINITY:
            default:
                return Integer.MAX_VALUE;
        }
    }

    @Override
    public DataComponentType<ItemData> getDataComponentType() {
        return ModItems.ITEM_DATA_COMPONENT.get();
    }

    private static final ItemData DEFAULT = new ItemData(UpgradeTileEntity.FilterMode.WHITELIST, UpgradeTileEntity.RedstoneMode.IGNORED, UpgradeTileEntity.Distribution.NEAREST, Collections.emptyList());

    @Override
    public ItemData defaultData() {
        return DEFAULT;
    }

}
