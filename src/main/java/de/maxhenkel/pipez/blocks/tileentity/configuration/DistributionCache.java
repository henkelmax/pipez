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

public class DistributionCache extends CachedPipeConfiguration<UpgradeTileEntity.Distribution> {


    public DistributionCache(Supplier<NonNullList<ItemStack>> upgradeInventory, Function<PipeType<?>, UpgradeTileEntity.Distribution> defaultValue, Runnable onDirty) {
        super(upgradeInventory, "Distribution", defaultValue, onDirty);
    }

    @Override
    public INBT serialize(UpgradeTileEntity.Distribution value) {
        return ByteNBT.valueOf((byte) value.ordinal());
    }

    @Nullable
    @Override
    public UpgradeTileEntity.Distribution deserialize(PipeType pipeType, INBT inbt) {
        if (inbt instanceof ByteNBT) {
            ByteNBT byteNBT = (ByteNBT) inbt;
            return UpgradeTileEntity.Distribution.values()[byteNBT.getAsByte()];
        }
        return null;
    }

}
