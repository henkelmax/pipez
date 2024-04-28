package de.maxhenkel.pipez.blocks.tileentity.configuration;

import de.maxhenkel.pipez.blocks.tileentity.UpgradeTileEntity;
import de.maxhenkel.pipez.blocks.tileentity.types.PipeType;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

import java.util.function.Function;
import java.util.function.Supplier;

public class DistributionCache extends CachedPipeConfiguration<UpgradeTileEntity.Distribution> {

    public DistributionCache(Supplier<NonNullList<ItemStack>> upgradeInventory, Function<PipeType<?, ?>, UpgradeTileEntity.Distribution> defaultValue, Runnable onDirty) {
        super(upgradeInventory, defaultValue, onDirty);
    }

    @Override
    public UpgradeTileEntity.Distribution get(PipeType<?, ?> pipeType, ItemStack stack) {
        return pipeType.getDistribution(stack);
    }

    @Override
    public void set(PipeType<?, ?> pipeType, ItemStack stack, UpgradeTileEntity.Distribution value) {
        pipeType.setDistribution(stack, value);
    }

}
