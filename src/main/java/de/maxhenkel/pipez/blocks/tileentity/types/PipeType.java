package de.maxhenkel.pipez.blocks.tileentity.types;

import de.maxhenkel.pipez.DirectionalPosition;
import de.maxhenkel.pipez.Filter;
import de.maxhenkel.pipez.Upgrade;
import de.maxhenkel.pipez.blocks.tileentity.PipeLogicTileEntity;
import de.maxhenkel.pipez.blocks.tileentity.PipeTileEntity;
import de.maxhenkel.pipez.blocks.tileentity.UpgradeTileEntity;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public abstract class PipeType<T> {

    public abstract String getKey();

    public abstract void tick(PipeLogicTileEntity tileEntity);

    public abstract int getRate(@Nullable Upgrade upgrade);

    public abstract Capability<?> getCapability();

    public abstract Filter<T> createFilter();

    public abstract String getTranslationKey();

    public abstract ItemStack getIcon();

    public abstract Component getTransferText(@Nullable Upgrade upgrade);

    public boolean hasFilter() {
        return true;
    }

    public UpgradeTileEntity.Distribution getDefaultDistribution() {
        return UpgradeTileEntity.Distribution.ROUND_ROBIN;
    }

    public UpgradeTileEntity.RedstoneMode getDefaultRedstoneMode() {
        return UpgradeTileEntity.RedstoneMode.IGNORED;
    }

    public UpgradeTileEntity.FilterMode getDefaultFilterMode() {
        return UpgradeTileEntity.FilterMode.WHITELIST;
    }

    public int getRate(PipeLogicTileEntity tileEntity, Direction direction) {
        return getRate(tileEntity.getUpgrade(direction));
    }

    public boolean matchesConnection(PipeTileEntity.Connection connection, Filter<T> filter) {
        if (filter.getDestination() == null) {
            return true;
        }
        return filter.getDestination().equals(new DirectionalPosition(connection.getPos(), connection.getDirection()));
    }

    public boolean deepExactCompare(Tag meta, Tag item) {
        if (meta instanceof CompoundTag) {
            if (!(item instanceof CompoundTag)) {
                return false;
            }
            CompoundTag c = (CompoundTag) meta;
            CompoundTag i = (CompoundTag) item;
            Set<String> allKeys = new HashSet<>();
            allKeys.addAll(c.getAllKeys());
            allKeys.addAll(i.getAllKeys());
            for (String key : allKeys) {
                if (c.contains(key)) {
                    if (i.contains(key)) {
                        Tag nbt = c.get(key);
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
        } else if (meta instanceof ListTag) {
            ListTag l = (ListTag) meta;
            if (!(item instanceof ListTag)) {
                return false;
            }
            ListTag il = (ListTag) item;
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

    public boolean deepFuzzyCompare(Tag meta, Tag item) {
        if (meta instanceof CompoundTag) {
            if (!(item instanceof CompoundTag)) {
                return false;
            }
            CompoundTag c = (CompoundTag) meta;
            CompoundTag i = (CompoundTag) item;
            for (String key : c.getAllKeys()) {
                Tag nbt = c.get(key);
                if (i.contains(key, nbt.getId())) {
                    if (!deepFuzzyCompare(nbt, i.get(key))) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
            return true;
        } else if (meta instanceof ListTag) {
            ListTag l = (ListTag) meta;
            if (!(item instanceof ListTag)) {
                return false;
            }
            ListTag il = (ListTag) item;
            return l.stream().allMatch(inbt -> il.stream().anyMatch(inbt1 -> deepFuzzyCompare(inbt, inbt1)));
        } else {
            return meta != null && meta.equals(item);
        }
    }

    public static int getConnectionsNotFullCount(boolean[] connections) {
        int count = 0;
        for (boolean connection : connections) {
            if (!connection) {
                count++;
            }
        }
        return count;
    }

}
