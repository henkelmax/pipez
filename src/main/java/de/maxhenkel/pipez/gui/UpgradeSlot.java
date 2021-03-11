package de.maxhenkel.pipez.gui;

import com.mojang.datafixers.util.Pair;
import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.items.UpgradeItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class UpgradeSlot extends Slot {

    public static final ResourceLocation UPGRADE_SLOT = new ResourceLocation(Main.MODID, "item/upgrade_slot");

    public UpgradeSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        UpgradeItem.upgradeData(getItem()); //TODO Remove after MC update
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.getItem() instanceof UpgradeItem;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
        return Pair.of(PlayerContainer.BLOCK_ATLAS, UPGRADE_SLOT);
    }

}
