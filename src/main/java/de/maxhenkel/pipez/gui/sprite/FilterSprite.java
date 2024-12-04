package de.maxhenkel.pipez.gui.sprite;

import de.maxhenkel.pipez.Main;
import net.minecraft.resources.ResourceLocation;

public class FilterSprite {
    public static ResourceLocation IMAGE = ResourceLocation.fromNamespaceAndPath(Main.MODID, "textures/gui/container/filter.png");

    public static SpriteRect SCREEN = new SpriteRect(0, 0, 176,222);

    private static int ICON_BUTTON_WIDTH = 16;
    private static int ICON_BUTTON_HEIGHT = 16;

    public static SpriteRect NBT_MODE_NOT_EXACT = new SpriteRect(176, 16, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT);
    public static SpriteRect NBT_MODE_EXACT = new SpriteRect(192, 16, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT);

    public static SpriteRect INVERT_NO = new SpriteRect(176, 32, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT);
    public static SpriteRect INVERT_YES = new SpriteRect(192, 32, ICON_BUTTON_WIDTH, ICON_BUTTON_HEIGHT);
}
