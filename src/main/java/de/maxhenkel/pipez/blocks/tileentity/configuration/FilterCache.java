package de.maxhenkel.pipez.blocks.tileentity.configuration;

import de.maxhenkel.pipez.Filter;
import de.maxhenkel.pipez.blocks.tileentity.types.PipeType;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class FilterCache extends CachedPipeConfiguration<List<Filter<?>>> {

    protected Function<PipeType<?>, Filter<?>> filterCreator;

    public FilterCache(Supplier<NonNullList<ItemStack>> upgradeInventory, Function<PipeType<?>, Filter<?>> filterCreator, Runnable onDirty) {
        super(upgradeInventory, "Filters", pipeType -> new ArrayList<>(), onDirty);
        this.filterCreator = filterCreator;
    }

    @Override
    public Tag serialize(List<Filter<?>> filters) {
        ListTag list = new ListTag();
        for (Filter<?> filter : filters) {
            list.add(filter.serializeNBT());
        }
        return list;
    }

    @Nullable
    @Override
    public List<Filter<?>> deserialize(PipeType<?> pipeType, Tag inbt) {
        if (inbt instanceof ListTag) {
            ListTag list = (ListTag) inbt;
            List<Filter<?>> filters = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                Filter<?> filter = filterCreator.apply(pipeType);
                filter.deserializeNBT(list.getCompound(i));
                filters.add(filter);
            }
            return filters;
        }
        return null;
    }

}
