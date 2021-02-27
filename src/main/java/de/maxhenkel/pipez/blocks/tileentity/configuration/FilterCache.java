package de.maxhenkel.pipez.blocks.tileentity.configuration;

import de.maxhenkel.pipez.Filter;
import de.maxhenkel.pipez.blocks.tileentity.types.PipeType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;

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
    public INBT serialize(List<Filter<?>> filters) {
        ListNBT list = new ListNBT();
        for (Filter<?> filter : filters) {
            list.add(filter.serializeNBT());
        }
        return list;
    }

    @Nullable
    @Override
    public List<Filter<?>> deserialize(PipeType<?> pipeType, INBT inbt) {
        if (inbt instanceof ListNBT) {
            ListNBT list = (ListNBT) inbt;
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
