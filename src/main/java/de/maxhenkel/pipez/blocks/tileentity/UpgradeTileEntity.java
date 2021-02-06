package de.maxhenkel.pipez.blocks.tileentity;

import de.maxhenkel.corelib.inventory.ItemListInventory;
import de.maxhenkel.corelib.item.ItemUtils;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.NonNullList;

public abstract class UpgradeTileEntity extends PipeTileEntity {

    private NonNullList<ItemStack> upgradeInventory;

    public UpgradeTileEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
        upgradeInventory = NonNullList.withSize(6, ItemStack.EMPTY);
    }

    public IInventory getInventory() {
        return new ItemListInventory(upgradeInventory, this::markDirty);
    }

    @Override
    public void read(BlockState state, CompoundNBT compound) {
        super.read(state, compound);
        upgradeInventory.clear();
        ItemUtils.readInventory(compound, "Upgrades", upgradeInventory);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        ItemUtils.saveInventory(compound, "Upgrades", upgradeInventory);
        return super.write(compound);
    }
}
