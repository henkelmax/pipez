package de.maxhenkel.pipez.blocks.tileentity.configuration;

import de.maxhenkel.pipez.blocks.tileentity.types.PipeType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class CachedPipeConfiguration<T> {

    private Map<PipeType<?>, T>[] cachedValues;

    protected Supplier<NonNullList<ItemStack>> upgradeInventory;
    protected String key;

    protected Function<PipeType<?>, T> defaultValue;
    protected Runnable onDirty;

    public CachedPipeConfiguration(Supplier<NonNullList<ItemStack>> upgradeInventory, String key, Function<PipeType<?>, T> defaultValue, Runnable onDirty) {
        this.upgradeInventory = upgradeInventory;
        this.key = key;
        this.defaultValue = defaultValue;
        this.onDirty = onDirty;
        this.cachedValues = new HashMap[Direction.values().length];
    }

    public T getValue(Direction side, PipeType<?> pipeType) {
        Map<PipeType<?>, T> map = cachedValues[side.ordinal()];
        if (map == null) {
            map = new HashMap<>();
            cachedValues[side.ordinal()] = map;
        }

        if (map.containsKey(pipeType)) {
            return map.get(pipeType);
        }

        ItemStack stack = upgradeInventory.get().get(side.ordinal());
        if (stack.isEmpty() || !stack.hasTag()) {
            return putDefault(pipeType, map);
        }
        CompoundNBT tag = stack.getTag();
        if (!tag.contains(pipeType.getKey(), Constants.NBT.TAG_COMPOUND)) {
            return putDefault(pipeType, map);
        }

        CompoundNBT compound = tag.getCompound(pipeType.getKey());
        if (!compound.contains(key)) {
            return putDefault(pipeType, map);
        }
        T value = deserialize(pipeType, compound.get(key));
        if (value == null) {
            return putDefault(pipeType, map);
        }
        map.put(pipeType, value);
        return value;
    }

    public T putDefault(PipeType<?> pipeType, Map<PipeType<?>, T> map) {
        T def = defaultValue.apply(pipeType);
        map.put(pipeType, def);
        return def;
    }

    public void setValue(Direction side, PipeType<?> pipeType, T value) {
        Map<PipeType<?>, T> map = cachedValues[side.ordinal()];
        if (map == null) {
            map = new HashMap<>();
            cachedValues[side.ordinal()] = map;
        }

        ItemStack stack = upgradeInventory.get().get(side.ordinal());
        if (stack.isEmpty()) {
            return;
        }

        map.put(pipeType, value);

        CompoundNBT tag = stack.getOrCreateTag();

        CompoundNBT compound;
        if (tag.contains(pipeType.getKey(), Constants.NBT.TAG_COMPOUND)) {
            compound = tag.getCompound(pipeType.getKey());
        } else {
            compound = new CompoundNBT();
            tag.put(pipeType.getKey(), compound);
        }

        compound.put(key, serialize(value));
        onDirty.run();
    }

    public void invalidate() {
        for (Map<PipeType<?>, T> map : cachedValues) {
            if (map != null) {
                map.clear();
            }
        }
    }

    public abstract INBT serialize(T value);

    @Nullable
    public abstract T deserialize(PipeType<?> pipeType, INBT inbt);

}
