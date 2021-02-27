package de.maxhenkel.pipez.blocks.tileentity.types;

import de.maxhenkel.pipez.DirectionalPosition;
import de.maxhenkel.pipez.Filter;
import de.maxhenkel.pipez.Upgrade;
import de.maxhenkel.pipez.blocks.tileentity.PipeLogicTileEntity;
import de.maxhenkel.pipez.blocks.tileentity.PipeTileEntity;
import de.maxhenkel.pipez.blocks.tileentity.UpgradeTileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public abstract class PipeType<T> {

    public abstract String getKey();

    public abstract void tick(PipeLogicTileEntity tileEntity);

    public abstract int getRate(@Nullable Upgrade upgrade);

    public abstract boolean canInsert(TileEntity tileEntity, Direction direction);

    public abstract Filter<T> createFilter();

    public abstract String getTranslationKey();

    public abstract ItemStack getIcon();

    public abstract ITextComponent getTransferText(@Nullable Upgrade upgrade);

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
