package de.maxhenkel.pipez.events;

import de.maxhenkel.pipez.gui.UpgradeSlot;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraftforge.client.event.TextureStitchEvent;

public class StitchEvents {

    public static void onStitch(TextureStitchEvent.Pre event) {
        if (event.getMap().getTextureLocation().equals(PlayerContainer.LOCATION_BLOCKS_TEXTURE)) {
            event.addSprite(UpgradeSlot.UPGRADE_SLOT);
        }
    }

}
