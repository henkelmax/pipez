package de.maxhenkel.pipez;

import com.mojang.serialization.Codec;
import de.maxhenkel.corelib.tag.SingleElementTag;
import de.maxhenkel.corelib.tag.Tag;
import de.maxhenkel.corelib.tag.TagUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ItemFilter extends Filter<ItemFilter, Item> {

    private static final TagConverter<Item> TAG_CONVERTER = (single, location) -> {
        if (single) {
            return new SingleElementTag<>(location, BuiltInRegistries.ITEM.get(location));
        } else {
            return TagUtils.getItemTag(location);
        }
    };
    public static final Codec<Tag<Item>> TAG_CODEC = tagCodec(TAG_CONVERTER);
    public static final StreamCodec<RegistryFriendlyByteBuf, Tag<Item>> STREAM_TAG_CODEC = tagStreamCodec(TAG_CONVERTER);

    public static final Codec<ItemFilter> CODEC = codec(ItemFilter.class, TAG_CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemFilter> STREAM_CODEC = streamCodec(ItemFilter.class, STREAM_TAG_CODEC);

    public ItemFilter(UUID id, @Nullable Tag<Item> tag, @Nullable CompoundTag metadata, boolean exactMetadata, @Nullable DirectionalPosition destination, boolean invert) {
        super(id, tag, metadata, exactMetadata, destination, invert);
    }

    @Override
    public Codec<ItemFilter> getCodec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ItemFilter> getStreamCodec() {
        return STREAM_CODEC;
    }

    public ItemFilter() {
        this(UUID.randomUUID(), null, null, false, null, false);
    }

}
