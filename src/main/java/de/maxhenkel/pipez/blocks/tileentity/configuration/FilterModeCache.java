package de.maxhenkel.pipez.blocks.tileentity.configuration;

import de.maxhenkel.pipez.blocks.tileentity.UpgradeTileEntity;
import de.maxhenkel.pipez.blocks.tileentity.types.PipeType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.ByteNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.NonNullList;

import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.function.Supplier;

public class FilterModeCache extends CachedPipeConfiguration<UpgradeTileEntity.FilterMode> {


    public FilterModeCache(Supplier<NonNullList<ItemStack>> upgradeInventory, Function<PipeType<?>, UpgradeTileEntity.FilterMode> defaultValue, Runnable onDirty) {
        super(upgradeInventory, "FilterMode", defaultValue, onDirty);
    }

    @Override
    public INBT serialize(UpgradeTileEntity.FilterMode value) {
        return ByteNBT.valueOf((byte) value.ordinal());
    }

    @Nullable
    @Override
    public UpgradeTileEntity.FilterMode deserialize(PipeType pipeType, INBT inbt) {
        if (inbt instanceof ByteNBT) {
            ByteNBT byteNBT = (ByteNBT) inbt;
            return UpgradeTileEntity.FilterMode.values()[byteNBT.getAsByte()];
        }
        return null;
    }
}
