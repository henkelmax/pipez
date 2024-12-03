package de.maxhenkel.pipez.gui.sprite;

import de.maxhenkel.pipez.Main;
import net.minecraft.resources.ResourceLocation;

public class ExtractUISprite {
    public static ResourceLocation IMAGE = ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/gui/container/extract-ui.png");

    public static SpriteRect SCREEN = new SpriteRect(0, 0, 176, 256);
    public static SpriteRect FILTER_LIST = new SpriteRect(32, 8, 136, ExtractElementsSprite.ROW_HEIGHT * ExtractElementsSprite.VISIBLE_ROW_COUNT);

    public static SpritePosition INVENTORY_TITLE = new SpritePosition(8, 164);
    public static SpritePosition UPGRADE_SLOT = new SpritePosition(9, 143);
    public static int INVENTORY_OFFSET = 174 - 84; // starts at 84
}
