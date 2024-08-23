package de.maxhenkel.pipez.datacomponents;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.maxhenkel.pipez.Filter;
import de.maxhenkel.pipez.blocks.tileentity.UpgradeTileEntity;
import de.maxhenkel.pipez.codec.EnumIndexCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class AbstractPipeTypeData<T> {

    protected UpgradeTileEntity.FilterMode filterMode;
    protected UpgradeTileEntity.RedstoneMode redstoneMode;
    protected UpgradeTileEntity.Distribution distribution;

    protected List<Filter<?, T>> filters;

    public AbstractPipeTypeData(UpgradeTileEntity.FilterMode filterMode, UpgradeTileEntity.RedstoneMode redstoneMode, UpgradeTileEntity.Distribution distribution, @Nullable List<Filter<?, T>> filters) {
        this.filterMode = filterMode;
        this.redstoneMode = redstoneMode;
        this.distribution = distribution;
        if (filters == null) {
            this.filters = Collections.emptyList();
        } else {
            this.filters = Collections.unmodifiableList(filters);
        }
    }

    public UpgradeTileEntity.FilterMode getFilterMode() {
        return filterMode;
    }

    public UpgradeTileEntity.RedstoneMode getRedstoneMode() {
        return redstoneMode;
    }

    public UpgradeTileEntity.Distribution getDistribution() {
        return distribution;
    }

    public List<Filter<?, ?>> copyFilterList() {
        List<Filter<?, ?>> filterList = new ArrayList<>(filters.size());
        for (Filter<?, T> filter : filters) {
            filterList.add(filter.copy());
        }
        return filterList;
    }

    public List<Filter<?, T>> copyFilterList2() {
        List<Filter<?, T>> filterList = new ArrayList<>(filters.size());
        for (Filter<?, T> filter : filters) {
            filterList.add(filter.copy());
        }
        return filterList;
    }

    public abstract PipeTypeDataBuilder builder();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AbstractPipeTypeData<?> that = (AbstractPipeTypeData<?>) o;
        return filterMode == that.filterMode && redstoneMode == that.redstoneMode && distribution == that.distribution && Objects.equals(filters, that.filters);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(filterMode);
        result = 31 * result + Objects.hashCode(redstoneMode);
        result = 31 * result + Objects.hashCode(distribution);
        result = 31 * result + Objects.hashCode(filters);
        return result;
    }

    public static abstract class PipeTypeDataBuilder<D extends AbstractPipeTypeData<T>, B extends PipeTypeDataBuilder<D, B, T>, T> {
        protected UpgradeTileEntity.FilterMode filterMode;
        protected UpgradeTileEntity.RedstoneMode redstoneMode;
        protected UpgradeTileEntity.Distribution distribution;
        protected List<Filter<?, T>> filters;

        public PipeTypeDataBuilder(D data) {
            this.filterMode = data.getFilterMode();
            this.redstoneMode = data.getRedstoneMode();
            this.distribution = data.getDistribution();
            this.filters = data.copyFilterList2();
        }

        public B filterMode(UpgradeTileEntity.FilterMode filterMode) {
            this.filterMode = filterMode;
            return (B) this;
        }

        public B redstoneMode(UpgradeTileEntity.RedstoneMode redstoneMode) {
            this.redstoneMode = redstoneMode;
            return (B) this;
        }

        public B distribution(UpgradeTileEntity.Distribution distribution) {
            this.distribution = distribution;
            return (B) this;
        }

        public B filters(List<Filter<?, T>> filters) {
            this.filters = filters;
            return (B) this;
        }

        public abstract D build();
    }

    public static <T, F extends Filter<?, T>, U extends AbstractPipeTypeData<T>> Codec<U> codec(Class<U> clazz, @Nullable Codec<F> filterCodec) {
        Constructor<U> constructor;
        try {
            constructor = clazz.getDeclaredConstructor(UpgradeTileEntity.FilterMode.class, UpgradeTileEntity.RedstoneMode.class, UpgradeTileEntity.Distribution.class, List.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (filterCodec == null) {
            return RecordCodecBuilder.create(i -> {
                return i.group(
                        EnumIndexCodec.of(UpgradeTileEntity.FilterMode.class).fieldOf("filter_mode").forGetter(AbstractPipeTypeData::getFilterMode),
                        EnumIndexCodec.of(UpgradeTileEntity.RedstoneMode.class).fieldOf("redstone_mode").forGetter(AbstractPipeTypeData::getRedstoneMode),
                        EnumIndexCodec.of(UpgradeTileEntity.Distribution.class).fieldOf("distribution").forGetter(AbstractPipeTypeData::getDistribution)
                ).apply(i, (filterMode, redstoneMode, distribution) -> {
                    try {
                        return constructor.newInstance(filterMode, redstoneMode, distribution, null);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            });
        }
        return RecordCodecBuilder.create(i -> {
            return i.group(
                    EnumIndexCodec.of(UpgradeTileEntity.FilterMode.class).fieldOf("filter_mode").forGetter(AbstractPipeTypeData::getFilterMode),
                    EnumIndexCodec.of(UpgradeTileEntity.RedstoneMode.class).fieldOf("redstone_mode").forGetter(AbstractPipeTypeData::getRedstoneMode),
                    EnumIndexCodec.of(UpgradeTileEntity.Distribution.class).fieldOf("distribution").forGetter(AbstractPipeTypeData::getDistribution),
                    filterCodec.listOf().fieldOf("Filters").forGetter(o -> (List<F>) o.filters)
            ).apply(i, (filterMode, redstoneMode, distribution, filters) -> {
                try {
                    return constructor.newInstance(filterMode, redstoneMode, distribution, filters);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }

    public static <T, F extends Filter<?, T>, U extends AbstractPipeTypeData<T>> StreamCodec<RegistryFriendlyByteBuf, U> streamCodec(Class<U> clazz, @Nullable StreamCodec<RegistryFriendlyByteBuf, F> filterCodec) {
        Constructor<U> constructor;
        try {
            constructor = clazz.getDeclaredConstructor(UpgradeTileEntity.FilterMode.class, UpgradeTileEntity.RedstoneMode.class, UpgradeTileEntity.Distribution.class, List.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (filterCodec == null) {
            return StreamCodec.composite(
                    EnumIndexCodec.ofStream(UpgradeTileEntity.FilterMode.class),
                    AbstractPipeTypeData::getFilterMode,
                    EnumIndexCodec.ofStream(UpgradeTileEntity.RedstoneMode.class),
                    AbstractPipeTypeData::getRedstoneMode,
                    EnumIndexCodec.ofStream(UpgradeTileEntity.Distribution.class),
                    AbstractPipeTypeData::getDistribution,
                    (filterMode, redstoneMode, distribution) -> {
                        try {
                            return constructor.newInstance(filterMode, redstoneMode, distribution, Collections.emptyList());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
            );
        }
        return StreamCodec.composite(
                EnumIndexCodec.ofStream(UpgradeTileEntity.FilterMode.class),
                AbstractPipeTypeData::getFilterMode,
                EnumIndexCodec.ofStream(UpgradeTileEntity.RedstoneMode.class),
                AbstractPipeTypeData::getRedstoneMode,
                EnumIndexCodec.ofStream(UpgradeTileEntity.Distribution.class),
                AbstractPipeTypeData::getDistribution,
                ByteBufCodecs.collection(ArrayList::new, filterCodec),
                u -> (List<F>) u.filters,
                (filterMode, redstoneMode, distribution, filters) -> {
                    try {
                        return constructor.newInstance(filterMode, redstoneMode, distribution, filters);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }

}
