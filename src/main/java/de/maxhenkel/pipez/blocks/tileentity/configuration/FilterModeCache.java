package de.maxhenkel.pipez.blocks.tileentity.configuration;

import de.maxhenkel.pipez.blocks.tileentity.UpgradeTileEntity;
import de.maxhenkel.pipez.blocks.tileentity.types.PipeType;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.function.Supplier;

public class FilterModeCache extends CachedPipeConfiguration<UpgradeTileEntity.FilterMode> {


    public FilterModeCache(Supplier<NonNullList<ItemStack>> upgradeInventory, Function<PipeType<?>, UpgradeTileEntity.FilterMode> defaultValue, Runnable onDirty) {
        super(upgradeInventory, "FilterMode", defaultValue, onDirty);
    }

    @Override
    public Tag serialize(UpgradeTileEntity.FilterMode value) {
        return ByteTag.valueOf((byte) value.ordinal());
    }

    @Nullable
    @Override
    public UpgradeTileEntity.FilterMode deserialize(PipeType pipeType, Tag inbt) {
        if (inbt instanceof ByteTag) {
            ByteTag byteNBT = (ByteTag) inbt;
            return UpgradeTileEntity.FilterMode.values()[byteNBT.getAsByte()];
        }
        return null;
    }
}
