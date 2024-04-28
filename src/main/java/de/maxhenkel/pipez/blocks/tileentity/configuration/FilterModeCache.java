package de.maxhenkel.pipez.blocks.tileentity.configuration;

import de.maxhenkel.pipez.blocks.tileentity.UpgradeTileEntity;
import de.maxhenkel.pipez.blocks.tileentity.types.PipeType;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

import java.util.function.Function;
import java.util.function.Supplier;

public class FilterModeCache extends CachedPipeConfiguration<UpgradeTileEntity.FilterMode> {

    public FilterModeCache(Supplier<NonNullList<ItemStack>> upgradeInventory, Function<PipeType<?, ?>, UpgradeTileEntity.FilterMode> defaultValue, Runnable onDirty) {
        super(upgradeInventory, defaultValue, onDirty);
    }

    @Override
    public UpgradeTileEntity.FilterMode get(PipeType<?, ?> pipeType, ItemStack stack) {
        return pipeType.getFilterMode(stack);
    }

    @Override
    public void set(PipeType<?, ?> pipeType, ItemStack stack, UpgradeTileEntity.FilterMode value) {
        pipeType.setFilterMode(stack, value);
    }
}
