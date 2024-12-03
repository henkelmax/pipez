package de.maxhenkel.pipez.gui.sprite;

import de.maxhenkel.pipez.Main;
import net.minecraft.resources.ResourceLocation;

public class ExtractSprite {
    public static ResourceLocation IMAGE = ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/gui/container/extract.png");

    public static SpriteRect SCREEN = new SpriteRect(0, 0, 176, 196);

    public static int ROW_HEIGHT = 22;
    public static int VISIBLE_ROW_COUNT = 3;

    public static SpriteRect FILTER_LIST = new SpriteRect(32, 8, 136, ROW_HEIGHT * VISIBLE_ROW_COUNT);
    public static SpriteRect FILTER_LIST_ENTRY = new SpriteRect(0, 196, 125, ROW_HEIGHT);
    public static SpriteRect FILTER_LIST_ENTRY_SELECTED = new SpriteRect(0, 218, 125, ROW_HEIGHT);

    public static SpriteRect FILTER_LIST_SCROLLER_ACTIVE = new SpriteRect(125, 196, 10, 17);
    public static SpriteRect FILTER_LIST_SCROLLER_INACTIVE = new SpriteRect(135, 196, 10, 17);

    private static int ICON_BUTTON_WIDTH = 16;
    private static int ICON_BUTTON_HEIGHT = 16;

    public static SpriteRect REDSTONE_MODE_ICON_IGNORE = new SpriteRect(176, 16, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT);
    public static SpriteRect REDSTONE_MODE_ICON_OFF_WHEN_POWERED = new SpriteRect(192, 16, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT);
    public static SpriteRect REDSTONE_MODE_ICON_ON_WHEN_POWERED = new SpriteRect(208, 16, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT);
    public static SpriteRect REDSTONE_MODE_ICON_ALWAYS_OFF = new SpriteRect(224, 16, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT);

    public static SpriteRect DISTRIBUTION_MODE_ICON_NEAREST = new SpriteRect(176, 0, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT);
    public static SpriteRect DISTRIBUTION_MODE_ICON_FURTHEST = new SpriteRect(192, 0, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT);
    public static SpriteRect DISTRIBUTION_MODE_ICON_ROUND_ROBIN = new SpriteRect(208, 0, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT);
    public static SpriteRect DISTRIBUTION_MODE_ICON_RANDOM = new SpriteRect(224, 0, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT);

    public static SpriteRect FILTER_MODE_ICON_WHITELIST = new SpriteRect(176, 32, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT);
    public static SpriteRect FILTER_MODE_ICON_BLACKLIST = new SpriteRect(192, 32, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT);

    public static SpriteRect TAB_ACTIVE = new SpriteRect(176, 48, 26, 24);
    public static SpriteRect TAB_INACTIVE = new SpriteRect(176, 72, 26, 24);
}
