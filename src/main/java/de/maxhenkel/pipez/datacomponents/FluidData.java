package de.maxhenkel.pipez.datacomponents;

import com.mojang.serialization.Codec;
import de.maxhenkel.pipez.Filter;
import de.maxhenkel.pipez.FluidFilter;
import de.maxhenkel.pipez.blocks.tileentity.UpgradeTileEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.material.Fluid;

import java.util.List;

public class FluidData extends AbstractPipeTypeData<Fluid> {

    public static final Codec<FluidData> CODEC = codec(FluidData.class, FluidFilter.CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidData> STREAM_CODEC = streamCodec(FluidData.class, FluidFilter.STREAM_CODEC);

    public FluidData(UpgradeTileEntity.FilterMode filterMode, UpgradeTileEntity.RedstoneMode redstoneMode, UpgradeTileEntity.Distribution distribution, List<Filter<?, Fluid>> filters) {
        super(filterMode, redstoneMode, distribution, filters);
    }

    @Override
    public FluidDataBuilder builder() {
        return new FluidDataBuilder(this);
    }

    public static class FluidDataBuilder extends PipeTypeDataBuilder<FluidData, FluidDataBuilder, Fluid> {

        public FluidDataBuilder(FluidData data) {
            super(data);
        }

        @Override
        public FluidData build() {
            return new FluidData(filterMode, redstoneMode, distribution, filters);
        }

    }

}
