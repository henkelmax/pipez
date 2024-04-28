package de.maxhenkel.pipez.blocks.tileentity.configuration;

import de.maxhenkel.pipez.Filter;
import de.maxhenkel.pipez.blocks.tileentity.types.PipeType;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class FilterCache extends CachedPipeConfiguration<List<Filter<?, ?>>> {

    public FilterCache(Supplier<NonNullList<ItemStack>> upgradeInventory, Runnable onDirty) {
        super(upgradeInventory, pipeType -> new ArrayList<>(), onDirty);
    }

    @Override
    public List<Filter<?, ?>> get(PipeType<?, ?> pipeType, ItemStack stack) {
        return pipeType.getFilters(stack);
    }

    @Override
    public void set(PipeType<?, ?> pipeType, ItemStack stack, List<Filter<?, ?>> value) {
        pipeType.setFilters(stack, value);
    }

}
