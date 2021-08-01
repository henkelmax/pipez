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

public class DistributionCache extends CachedPipeConfiguration<UpgradeTileEntity.Distribution> {


    public DistributionCache(Supplier<NonNullList<ItemStack>> upgradeInventory, Function<PipeType<?>, UpgradeTileEntity.Distribution> defaultValue, Runnable onDirty) {
        super(upgradeInventory, "Distribution", defaultValue, onDirty);
    }

    @Override
    public Tag serialize(UpgradeTileEntity.Distribution value) {
        return ByteTag.valueOf((byte) value.ordinal());
    }

    @Nullable
    @Override
    public UpgradeTileEntity.Distribution deserialize(PipeType pipeType, Tag inbt) {
        if (inbt instanceof ByteTag) {
            ByteTag byteNBT = (ByteTag) inbt;
            return UpgradeTileEntity.Distribution.values()[byteNBT.getAsByte()];
        }
        return null;
    }

}
