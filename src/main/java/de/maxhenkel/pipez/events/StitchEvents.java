package de.maxhenkel.pipez.events;

import de.maxhenkel.pipez.gui.UpgradeSlot;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.event.TextureStitchEvent;

public class StitchEvents {

    public static void onStitch(TextureStitchEvent.Pre event) {
        if (event.getAtlas().location().equals(InventoryMenu.BLOCK_ATLAS)) {
            event.addSprite(UpgradeSlot.UPGRADE_SLOT);
        }
    }

}
