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

public class RedstoneModeCache extends CachedPipeConfiguration<UpgradeTileEntity.RedstoneMode> {

    public RedstoneModeCache(Supplier<NonNullList<ItemStack>> upgradeInventory, Function<PipeType<?>, UpgradeTileEntity.RedstoneMode> defaultValue, Runnable onDirty) {
        super(upgradeInventory, "RedstoneMode", defaultValue, onDirty);
    }

    @Override
    public Tag serialize(UpgradeTileEntity.RedstoneMode value) {
        return ByteTag.valueOf((byte) value.ordinal());
    }

    @Nullable
    @Override
    public UpgradeTileEntity.RedstoneMode deserialize(PipeType pipeType, Tag inbt) {
        if (inbt instanceof ByteTag) {
            ByteTag byteNBT = (ByteTag) inbt;
            return UpgradeTileEntity.RedstoneMode.values()[byteNBT.getAsByte()];
        }
        return null;
    }
}
