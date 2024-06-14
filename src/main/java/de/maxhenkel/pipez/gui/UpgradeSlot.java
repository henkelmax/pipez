package de.maxhenkel.pipez.gui;

import com.mojang.datafixers.util.Pair;
import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.items.UpgradeItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class UpgradeSlot extends Slot {

    public static final ResourceLocation UPGRADE_SLOT = ResourceLocation.fromNamespaceAndPath(Main.MODID, "item/upgrade_slot");

    public UpgradeSlot(Container inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
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
        return Pair.of(InventoryMenu.BLOCK_ATLAS, UPGRADE_SLOT);
    }

}
