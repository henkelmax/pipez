package de.maxhenkel.pipez.datacomponents;

import com.mojang.serialization.Codec;
import de.maxhenkel.pipez.Filter;
import de.maxhenkel.pipez.blocks.tileentity.UpgradeTileEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public class EnergyData extends AbstractPipeTypeData<Void> {

    public static final Codec<EnergyData> CODEC = codec(EnergyData.class, null);
    public static final StreamCodec<RegistryFriendlyByteBuf, EnergyData> STREAM_CODEC = streamCodec(EnergyData.class, null);

    public EnergyData(UpgradeTileEntity.FilterMode filterMode, UpgradeTileEntity.RedstoneMode redstoneMode, UpgradeTileEntity.Distribution distribution, List<Filter<?, Void>> filters) {
        super(filterMode, redstoneMode, distribution, filters);
    }

    @Override
    public EnergyDataBuilder builder() {
        return new EnergyDataBuilder(this);
    }

    public static class EnergyDataBuilder extends PipeTypeDataBuilder<EnergyData, EnergyDataBuilder, Void> {

        public EnergyDataBuilder(EnergyData data) {
            super(data);
        }

        @Override
        public EnergyData build() {
            return new EnergyData(filterMode, redstoneMode, distribution, filters);
        }
    }

}
