package de.maxhenkel.pipez.gui.sprite;

import de.maxhenkel.pipez.PipezMod;
import net.minecraft.resources.ResourceLocation;

public class ExtractUISprite {
    public static ResourceLocation IMAGE = ResourceLocation.fromNamespaceAndPath(PipezMod.MODID, "textures/gui/container/extract-ui.png");

    public static int ROW_HEIGHT = 22;
    public static int VISIBLE_ROW_COUNT = 7;

    public static SpriteRect SCREEN = new SpriteRect(0, 0, 256, 256);
    public static SpriteRect FILTER_LIST = new SpriteRect(32, 8, 192, ROW_HEIGHT * VISIBLE_ROW_COUNT);

    public static SpritePosition ENTRY_COUNT_TEXT = new SpritePosition(32 - FILTER_LIST.x, 3 - FILTER_LIST.y); // relative to Filter List
    public static SpritePosition INVENTORY_TITLE = new SpritePosition(47, 164);
    public static SpritePosition UPGRADE_SLOT = new SpritePosition(9, 146);
    // x starts at 8
    // y starts at 84
    public static SpritePosition INVENTORY_OFFSET = new SpritePosition(48 - 8, 174 - 84);
}
