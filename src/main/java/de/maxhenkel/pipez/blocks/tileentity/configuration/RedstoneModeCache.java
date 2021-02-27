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

public class RedstoneModeCache extends CachedPipeConfiguration<UpgradeTileEntity.RedstoneMode> {

    public RedstoneModeCache(Supplier<NonNullList<ItemStack>> upgradeInventory, Function<PipeType<?>, UpgradeTileEntity.RedstoneMode> defaultValue, Runnable onDirty) {
        super(upgradeInventory, "RedstoneMode", defaultValue, onDirty);
    }

    @Override
    public INBT serialize(UpgradeTileEntity.RedstoneMode value) {
        return ByteNBT.valueOf((byte) value.ordinal());
    }

    @Nullable
    @Override
    public UpgradeTileEntity.RedstoneMode deserialize(PipeType pipeType, INBT inbt) {
        if (inbt instanceof ByteNBT) {
            ByteNBT byteNBT = (ByteNBT) inbt;
            return UpgradeTileEntity.RedstoneMode.values()[byteNBT.getByte()];
        }
        return null;
    }
}
