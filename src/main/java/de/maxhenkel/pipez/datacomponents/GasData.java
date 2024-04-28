package de.maxhenkel.pipez.datacomponents;

import com.mojang.serialization.Codec;
import de.maxhenkel.pipez.Filter;
import de.maxhenkel.pipez.GasFilter;
import de.maxhenkel.pipez.blocks.tileentity.UpgradeTileEntity;
import mekanism.api.chemical.Chemical;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public class GasData extends AbstractPipeTypeData<Chemical> {

    public static final Codec<GasData> CODEC = codec(GasData.class, GasFilter.CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, GasData> STREAM_CODEC = streamCodec(GasData.class, GasFilter.STREAM_CODEC);

    public GasData(UpgradeTileEntity.FilterMode filterMode, UpgradeTileEntity.RedstoneMode redstoneMode, UpgradeTileEntity.Distribution distribution, List<Filter<?, Chemical>> filters) {
        super(filterMode, redstoneMode, distribution, filters);
    }

    @Override
    public GasDataBuilder builder() {
        return new GasDataBuilder(this);
    }

    public static class GasDataBuilder extends PipeTypeDataBuilder<GasData, GasDataBuilder, Chemical> {

        public GasDataBuilder(GasData data) {
            super(data);
        }

        @Override
        public GasData build() {
            return new GasData(filterMode, redstoneMode, distribution, filters);
        }

    }

}
