package de.maxhenkel.pipez.gui.sprite;

import de.maxhenkel.pipez.Main;
import net.minecraft.resources.ResourceLocation;

public class ExtractElementsSprite {
    public static ResourceLocation IMAGE = ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/gui/container/extract-elements.png");

    public static SpriteRect FILTER_LIST_ENTRY = new SpriteRect(0, 96, 181, ExtractUISprite.ROW_HEIGHT);
    public static SpriteRect FILTER_LIST_ENTRY_SELECTED = new SpriteRect(0, 118, 181, ExtractUISprite.ROW_HEIGHT);

    public static SpriteRect FILTER_LIST_SCROLLER_ACTIVE = new SpriteRect(0, 172, 10, 17);
    public static SpriteRect FILTER_LIST_SCROLLER_INACTIVE = new SpriteRect(10, 172, 10, 17);

    private static int ICON_BUTTON_WIDTH = 16;
    private static int ICON_BUTTON_HEIGHT = 16;

    public static SpriteRect REDSTONE_MODE_ICON_IGNORE = new SpriteRect(0, 16, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT);
    public static SpriteRect REDSTONE_MODE_ICON_OFF_WHEN_POWERED = new SpriteRect(16, 16, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT);
    public static SpriteRect REDSTONE_MODE_ICON_ON_WHEN_POWERED = new SpriteRect(32, 16, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT);
    public static SpriteRect REDSTONE_MODE_ICON_ALWAYS_OFF = new SpriteRect(48, 16, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT);

    public static SpriteRect DISTRIBUTION_MODE_ICON_NEAREST = new SpriteRect(0, 0, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT);
    public static SpriteRect DISTRIBUTION_MODE_ICON_FURTHEST = new SpriteRect(16, 0, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT);
    public static SpriteRect DISTRIBUTION_MODE_ICON_ROUND_ROBIN = new SpriteRect(32, 0, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT);
    public static SpriteRect DISTRIBUTION_MODE_ICON_RANDOM = new SpriteRect(48, 0, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT);

    public static SpriteRect FILTER_MODE_ICON_WHITELIST = new SpriteRect(0, 32, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT);
    public static SpriteRect FILTER_MODE_ICON_BLACKLIST = new SpriteRect(16, 32, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT);

    public static SpriteRect SORT_ICON_NAME_ASC = new SpriteRect(0, 156, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT);
    public static SpriteRect SORT_ICON_NAME_DESC = new SpriteRect(SORT_ICON_NAME_ASC.x + SORT_ICON_NAME_ASC.w, SORT_ICON_NAME_ASC.y, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT);
    public static SpriteRect SORT_ICON_DISTANCE_ASC = new SpriteRect(SORT_ICON_NAME_DESC.x + SORT_ICON_NAME_DESC.w, SORT_ICON_NAME_ASC.y, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT);
    public static SpriteRect SORT_ICON_DISTANCE_DESC = new SpriteRect(SORT_ICON_DISTANCE_ASC.x + SORT_ICON_DISTANCE_ASC.w, SORT_ICON_NAME_ASC.y, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT);

    public static SpriteRect FILTER_ENTRY_ICON_ADD = new SpriteRect(0, 140, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT);
    public static SpriteRect FILTER_ENTRY_ICON_DELETE = new SpriteRect(16, 140, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT);
    public static SpriteRect FILTER_ENTRY_ICON_EDIT = new SpriteRect(32, 140, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT);

    public static SpriteRect TAB_ACTIVE = new SpriteRect(0, 48, 26, 24);
    public static SpriteRect TAB_INACTIVE = new SpriteRect(0, 72, 26, 24);

    public static int BUTTON_MARGIN = 2;
    public static SpriteRect REDSTONE_BUTTON = new SpriteRect(7, 7, 20, 20);
    public static SpriteRect DISTRIBUTION_BUTTON = new SpriteRect(7, REDSTONE_BUTTON.y + REDSTONE_BUTTON.h + BUTTON_MARGIN, 20, 20);
    public static SpriteRect FILTER_MODE_BUTTON = new SpriteRect(7, DISTRIBUTION_BUTTON.y + DISTRIBUTION_BUTTON.h + BUTTON_MARGIN, 20, 20);

    public static SpriteRect SORT_FILTER_LIST_BUTTON = new SpriteRect(229, 7, 20, 20);

    public static SpriteRect FILTER_ENTRY_BUTTON_ADD = new SpriteRect(229, 99, 20, 20);
    public static SpriteRect FILTER_ENTRY_BUTTON_EDIT = new SpriteRect(229, FILTER_ENTRY_BUTTON_ADD.y + FILTER_ENTRY_BUTTON_ADD.h + BUTTON_MARGIN, 20, 20);
    public static SpriteRect FILTER_ENTRY_BUTTON_DELETE = new SpriteRect(229, FILTER_ENTRY_BUTTON_EDIT.y + FILTER_ENTRY_BUTTON_EDIT.h + BUTTON_MARGIN, 20, 20);

    public static int TAB_BUTTON_MARGIN = 1;
    public static SpriteRect TAB_BUTTON = new SpriteRect(-26 + 3, 5, 24, 24);
    public static SpritePosition TAB_BUTTON_ICON_SELECTED = new SpritePosition(TAB_BUTTON.x + 4, TAB_BUTTON.y + 4);
    public static SpritePosition TAB_BUTTON_ICON = new SpritePosition(TAB_BUTTON_ICON_SELECTED.x + 2, TAB_BUTTON_ICON_SELECTED.y);
}
