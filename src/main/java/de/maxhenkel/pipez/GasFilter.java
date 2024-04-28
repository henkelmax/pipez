package de.maxhenkel.pipez;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.maxhenkel.corelib.tag.SingleElementTag;
import de.maxhenkel.corelib.tag.Tag;
import de.maxhenkel.pipez.utils.GasTag;
import de.maxhenkel.pipez.utils.GasUtils;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalType;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.UUID;

public class GasFilter extends Filter<GasFilter, Chemical> {

    private static final Codec<ChemicalTagData> TAG_DATA_CODEC = RecordCodecBuilder.create(i -> {
        return i.group(
                Codec.STRING.fieldOf("type").xmap(TagType::valueOf, TagType::name).forGetter(ChemicalTagData::tagType),
                Codec.STRING.fieldOf("chemical").forGetter(ChemicalTagData::type),
                ResourceLocation.CODEC.fieldOf("tag").forGetter(ChemicalTagData::location)
        ).apply(i, ChemicalTagData::new);
    });

    private static final StreamCodec<RegistryFriendlyByteBuf, ChemicalTagData> TAG_DATA_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            ChemicalTagData::isSingle,
            ByteBufCodecs.STRING_UTF8,
            ChemicalTagData::type,
            ResourceLocation.STREAM_CODEC,
            ChemicalTagData::location,
            ChemicalTagData::new
    );

    private static final Codec<Tag<Chemical>> TAG_CODEC = TAG_DATA_CODEC.xmap(data -> {
        Registry<? extends Chemical> registry = GasUtils.getRegistry(data.getChemicalType());
        if (data.isSingle()) {
            if (registry.containsKey(data.location)) {
                return new SingleElementTag<>(data.location, registry.get(data.location));
            }
            return null;
        } else {
            return GasUtils.getGasTag(data.location, false, data.getChemicalType());
        }
    }, tag -> {
        if (tag instanceof SingleElementTag<? extends Chemical> t) {
            ChemicalType type = t.getElement() == null ? ChemicalType.GAS : ChemicalType.getTypeFor(t.getElement());
            return new ChemicalTagData(TagType.SINGLE, type.getSerializedName(), t.getName());
        } else if (tag instanceof GasTag gasTag) {
            return new ChemicalTagData(TagType.TAG, gasTag.getChemicalType().getSerializedName(), tag.getName());
        } else {
            return null;
        }
    });

    public static final StreamCodec<RegistryFriendlyByteBuf, Tag<Chemical>> STREAM_TAG_CODEC = TAG_DATA_STREAM_CODEC.map(data -> {
        Registry<? extends Chemical> registry = GasUtils.getRegistry(data.getChemicalType());
        if (data.isSingle()) {
            if (registry.containsKey(data.location)) {
                return new SingleElementTag<>(data.location, registry.get(data.location));
            }
            return null;
        } else {
            return GasUtils.getGasTag(data.location, false, data.getChemicalType());
        }
    }, tag -> {
        if (tag instanceof SingleElementTag<? extends Chemical> t) {
            ChemicalType type = t.getElement() == null ? ChemicalType.GAS : ChemicalType.getTypeFor(t.getElement());
            return new ChemicalTagData(TagType.SINGLE, type.getSerializedName(), t.getName());
        } else if (tag instanceof GasTag gasTag) {
            return new ChemicalTagData(TagType.TAG, gasTag.getChemicalType().getSerializedName(), tag.getName());
        } else {
            return null;
        }
    });

    public static final Codec<GasFilter> CODEC = codec(GasFilter.class, TAG_CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, GasFilter> STREAM_CODEC = streamCodec(GasFilter.class, STREAM_TAG_CODEC);

    public GasFilter(UUID id, @Nullable Tag<Chemical> tag, @Nullable CompoundTag metadata, boolean exactMetadata, @Nullable DirectionalPosition destination, boolean invert) {
        super(id, tag, metadata, exactMetadata, destination, invert);
    }

    public GasFilter() {
        this(UUID.randomUUID(), null, null, false, null, false);
    }

    @Override
    public Codec<GasFilter> getCodec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, GasFilter> getStreamCodec() {
        return STREAM_CODEC;
    }

    protected record ChemicalTagData(Filter.TagType tagType, String type, ResourceLocation location) {
        public ChemicalTagData(boolean single, String type, ResourceLocation location) {
            this(single ? Filter.TagType.SINGLE : Filter.TagType.TAG, type, location);
        }

        public boolean isSingle() {
            return tagType == Filter.TagType.SINGLE;
        }

        public ChemicalType getChemicalType() {
            return type == null ? ChemicalType.GAS : ChemicalType.fromString(type);
        }
    }

}
