package de.maxhenkel.pipez;

import com.mojang.serialization.Codec;
import de.maxhenkel.corelib.tag.SingleElementTag;
import de.maxhenkel.corelib.tag.Tag;
import de.maxhenkel.corelib.tag.TagUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class FluidFilter extends Filter<FluidFilter, Fluid> {

    private static final TagConverter<Fluid> TAG_CONVERTER = (single, location) -> {
        if (single) {
            return new SingleElementTag<>(location, BuiltInRegistries.FLUID.get(location));
        } else {
            return TagUtils.getFluidTag(location);
        }
    };
    public static final Codec<Tag<Fluid>> TAG_CODEC = tagCodec(TAG_CONVERTER);
    public static final StreamCodec<RegistryFriendlyByteBuf, Tag<Fluid>> STREAM_TAG_CODEC = tagStreamCodec(TAG_CONVERTER);

    public static final Codec<FluidFilter> CODEC = codec(FluidFilter.class, TAG_CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidFilter> STREAM_CODEC = streamCodec(FluidFilter.class, STREAM_TAG_CODEC);

    public FluidFilter(UUID id, @Nullable Tag<Fluid> tag, @Nullable CompoundTag metadata, boolean exactMetadata, @Nullable DirectionalPosition destination, boolean invert) {
        super(id, tag, metadata, exactMetadata, destination, invert);
    }

    @Override
    public Codec<FluidFilter> getCodec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, FluidFilter> getStreamCodec() {
        return STREAM_CODEC;
    }

    public FluidFilter() {
        this(UUID.randomUUID(), null, null, false, null, false);
    }

}
