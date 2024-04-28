package de.maxhenkel.pipez;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.maxhenkel.corelib.tag.SingleElementTag;
import de.maxhenkel.corelib.tag.Tag;
import de.maxhenkel.pipez.utils.NbtUtils;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.util.*;

public abstract class Filter<F extends Filter<F, T>, T> {

    protected UUID id;
    @Nullable
    protected Tag<T> tag;
    @Nullable
    protected CompoundTag metadata;
    protected boolean exactMetadata;
    @Nullable
    protected DirectionalPosition destination;
    protected boolean invert;

    public Filter(UUID id, @Nullable Tag<T> tag, @Nullable CompoundTag metadata, boolean exactMetadata, @Nullable DirectionalPosition destination, boolean invert) {
        this.id = id;
        this.tag = tag;
        this.metadata = metadata;
        this.exactMetadata = exactMetadata;
        this.destination = destination;
        this.invert = invert;
    }

    public UUID getId() {
        return id;
    }

    @Nullable
    public Tag<T> getTag() {
        return tag;
    }

    public void setTag(@Nullable Tag<T> tag) {
        this.tag = tag;
    }

    @Nullable
    public CompoundTag getMetadata() {
        return metadata;
    }

    public void setMetadata(@Nullable CompoundTag metadata) {
        this.metadata = metadata;
    }

    @Nullable
    public DirectionalPosition getDestination() {
        return destination;
    }

    public void setDestination(@Nullable DirectionalPosition destination) {
        this.destination = destination;
    }

    public boolean isInvert() {
        return invert;
    }

    public void setInvert(boolean invert) {
        this.invert = invert;
    }

    public boolean isExactMetadata() {
        return exactMetadata;
    }

    public void setExactMetadata(boolean exactMetadata) {
        this.exactMetadata = exactMetadata;
    }

    public F copy() {
        try {
            Filter copiedFilter = getClass().getDeclaredConstructor().newInstance();
            copiedFilter.id = id;
            copiedFilter.tag = tag;
            copiedFilter.metadata = metadata;
            copiedFilter.exactMetadata = exactMetadata;
            copiedFilter.destination = destination;
            copiedFilter.invert = invert;
            return (F) copiedFilter;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public abstract Codec<F> getCodec();

    public abstract StreamCodec<RegistryFriendlyByteBuf, F> getStreamCodec();

    public CompoundTag toNbt() {
        return NbtUtils.codecToNbtDefault((Codec) getCodec(), (Filter) this);

    }

    public Filter fromNbt(CompoundTag tag) {
        return (Filter) NbtUtils.nbtToCodecOptional((Codec) getCodec(), tag).orElse(this);
    }

    public void toNetwork(RegistryFriendlyByteBuf buffer) {
        ((StreamCodec) getStreamCodec()).encode(buffer, this);
    }

    public Filter fromNetwork(RegistryFriendlyByteBuf buffer) {
        return (Filter) ((StreamCodec) getStreamCodec()).decode(buffer);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Filter<?, ?> filter = (Filter<?, ?>) o;
        return Objects.equals(id, filter.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public static <T, U extends Filter<U, T>> Codec<U> codec(Class<U> clazz, Codec<Tag<T>> tagCodec) {
        Constructor<U> constructor;
        try {
            constructor = clazz.getDeclaredConstructor(UUID.class, Tag.class, CompoundTag.class, boolean.class, DirectionalPosition.class, boolean.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return RecordCodecBuilder.create(i -> {
            return i.group(
                    UUIDUtil.CODEC.fieldOf("ID").forGetter(Filter::getId),
                    tagCodec.optionalFieldOf("Tag").forGetter(u -> Optional.ofNullable(u.getTag())),
                    CompoundTag.CODEC.optionalFieldOf("Metadata").forGetter(itemFilter -> Optional.ofNullable(itemFilter.getMetadata())),
                    Codec.BOOL.fieldOf("ExactMetadata").forGetter(Filter::isExactMetadata),
                    DirectionalPosition.CODEC.optionalFieldOf("Destination").forGetter(itemFilter -> Optional.ofNullable(itemFilter.getDestination())),
                    Codec.BOOL.fieldOf("Invert").forGetter(Filter::isInvert)
            ).apply(i, (id, tag, metadata, exactMetadata, destination, invert) -> {
                try {
                    return constructor.newInstance(id, tag.orElse(null), metadata.orElse(null), exactMetadata, destination.orElse(null), invert);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }

    public static <T, U extends Filter<U, T>> StreamCodec<RegistryFriendlyByteBuf, U> streamCodec(Class<U> clazz, StreamCodec<RegistryFriendlyByteBuf, Tag<T>> tagCodec) {
        Constructor<U> constructor;
        try {
            constructor = clazz.getDeclaredConstructor(UUID.class, Tag.class, CompoundTag.class, boolean.class, DirectionalPosition.class, boolean.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return StreamCodec.composite(
                UUIDUtil.STREAM_CODEC,
                Filter::getId,
                ByteBufCodecs.optional(tagCodec),
                f -> Optional.ofNullable(f.getTag()),
                ByteBufCodecs.optional(ByteBufCodecs.COMPOUND_TAG),
                f -> Optional.ofNullable(f.getMetadata()),
                ByteBufCodecs.BOOL,
                Filter::isExactMetadata,
                ByteBufCodecs.optional(DirectionalPosition.STREAM_CODEC),
                f -> Optional.ofNullable(f.getDestination()),
                ByteBufCodecs.BOOL,
                Filter::isInvert,
                (id, tag, metadata, exactMetadata, destination, invert) -> {
                    try {
                        return constructor.newInstance(id, tag.orElse(null), metadata.orElse(null), exactMetadata, destination.orElse(null), invert);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    private static final Codec<TagData> TAG_DATA_CODEC = RecordCodecBuilder.create(i -> {
        return i.group(
                Codec.STRING.fieldOf("type").xmap(TagType::valueOf, TagType::name).forGetter(TagData::tagType),
                ResourceLocation.CODEC.fieldOf("tag").forGetter(TagData::location)
        ).apply(i, TagData::new);
    });

    private static final StreamCodec<RegistryFriendlyByteBuf, TagData> TAG_DATA_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            TagData::isSingle,
            ResourceLocation.STREAM_CODEC,
            TagData::location,
            TagData::new
    );

    public static <T> Codec<Tag<T>> tagCodec(TagConverter<T> tagConverter) {
        return TAG_DATA_CODEC.xmap(data -> tagConverter.convert(data.isSingle(), data.location), tag -> {
            if (tag instanceof SingleElementTag<T> singleElementTag) {
                return new TagData(TagType.SINGLE, singleElementTag.getName());
            } else {
                return new TagData(TagType.TAG, tag.getName());
            }
        });
    }

    public static <T> StreamCodec<RegistryFriendlyByteBuf, Tag<T>> tagStreamCodec(TagConverter<T> tagConverter) {
        return TAG_DATA_STREAM_CODEC.map(data -> tagConverter.convert(data.isSingle(), data.location), tag -> {
            if (tag instanceof SingleElementTag<T> singleElementTag) {
                return new TagData(TagType.SINGLE, singleElementTag.getName());
            } else {
                return new TagData(TagType.TAG, tag.getName());
            }
        });
    }

    @FunctionalInterface
    public interface TagConverter<T> {
        Tag<T> convert(boolean single, ResourceLocation location);
    }

    protected enum TagType {
        SINGLE, TAG;
    }

    protected record TagData(TagType tagType, ResourceLocation location) {
        public TagData(boolean single, ResourceLocation location) {
            this(single ? TagType.SINGLE : TagType.TAG, location);
        }

        public boolean isSingle() {
            return tagType == TagType.SINGLE;
        }
    }

}
