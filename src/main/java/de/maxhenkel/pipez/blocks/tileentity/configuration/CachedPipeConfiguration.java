package de.maxhenkel.pipez.blocks.tileentity.configuration;

import de.maxhenkel.pipez.blocks.tileentity.types.PipeType;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class CachedPipeConfiguration<T> {

    private final Map<PipeType<?, ?>, T>[] cachedValues;

    protected Supplier<NonNullList<ItemStack>> upgradeInventory;

    protected Function<PipeType<?, ?>, T> defaultValue;
    protected Runnable onDirty;

    public CachedPipeConfiguration(Supplier<NonNullList<ItemStack>> upgradeInventory, Function<PipeType<?, ?>, T> defaultValue, Runnable onDirty) {
        this.upgradeInventory = upgradeInventory;
        this.defaultValue = defaultValue;
        this.onDirty = onDirty;
        this.cachedValues = new HashMap[Direction.values().length];
    }

    public T getValue(Direction side, PipeType<?, ?> pipeType) {
        Map<PipeType<?, ?>, T> map = cachedValues[side.ordinal()];
        if (map == null) {
            map = new HashMap<>();
            cachedValues[side.ordinal()] = map;
        }

        if (map.containsKey(pipeType)) {
            return map.get(pipeType);
        }

        ItemStack stack = upgradeInventory.get().get(side.ordinal());
        T value = get(pipeType, stack);
        if (stack.isEmpty() || value == null) {
            return putDefault(pipeType, map);
        }
        map.put(pipeType, value);
        return value;
    }

    public T putDefault(PipeType<?, ?> pipeType, Map<PipeType<?, ?>, T> map) {
        T def = defaultValue.apply(pipeType);
        map.put(pipeType, def);
        return def;
    }

    public void setValue(Direction side, PipeType<?, ?> pipeType, T value) {
        Map<PipeType<?, ?>, T> map = cachedValues[side.ordinal()];
        if (map == null) {
            map = new HashMap<>();
            cachedValues[side.ordinal()] = map;
        }

        ItemStack stack = upgradeInventory.get().get(side.ordinal());
        if (stack.isEmpty()) {
            return;
        }
        map.put(pipeType, value);
        set(pipeType, stack, value);
        onDirty.run();
    }

    public void invalidate() {
        for (Map<PipeType<?, ?>, T> map : cachedValues) {
            if (map != null) {
                map.clear();
            }
        }
    }

    public abstract T get(PipeType<?, ?> pipeType, ItemStack stack);

    public abstract void set(PipeType<?, ?> pipeType, ItemStack stack, T value);

}
