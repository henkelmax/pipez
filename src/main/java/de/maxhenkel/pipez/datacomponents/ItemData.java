package de.maxhenkel.pipez.datacomponents;

import com.mojang.serialization.Codec;
import de.maxhenkel.pipez.Filter;
import de.maxhenkel.pipez.ItemFilter;
import de.maxhenkel.pipez.blocks.tileentity.UpgradeTileEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;

import java.util.List;

public class ItemData extends AbstractPipeTypeData<Item> {

    public static final Codec<ItemData> CODEC = codec(ItemData.class, ItemFilter.CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemData> STREAM_CODEC = streamCodec(ItemData.class, ItemFilter.STREAM_CODEC);

    public ItemData(UpgradeTileEntity.FilterMode filterMode, UpgradeTileEntity.RedstoneMode redstoneMode, UpgradeTileEntity.Distribution distribution, List<Filter<?, Item>> filters) {
        super(filterMode, redstoneMode, distribution, filters);
    }

    @Override
    public ItemDataBuilder builder() {
        return new ItemDataBuilder(this);
    }

    public static class ItemDataBuilder extends PipeTypeDataBuilder<ItemData, ItemDataBuilder, Item> {

        public ItemDataBuilder(ItemData data) {
            super(data);
        }

        @Override
        public ItemData build() {
            return new ItemData(filterMode, redstoneMode, distribution, filters);
        }
    }
}
