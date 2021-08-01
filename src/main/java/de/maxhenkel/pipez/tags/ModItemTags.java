package de.maxhenkel.pipez.tags;

import de.maxhenkel.pipez.Main;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;

public class ModItemTags {

    public static final Tag.Named<Item> WRENCHES_TAG = ItemTags.createOptional(new ResourceLocation("forge", "wrenches"));
    public static final Tag.Named<Item> WRENCH_TAG = ItemTags.createOptional(new ResourceLocation("forge", "tools/wrench"));
    public static final Tag.Named<Item> TOOLS_TAG = ItemTags.createOptional(new ResourceLocation("forge", "tools"));
    public static final Tag.Named<Item> UPGRADES_TAG = ItemTags.createOptional(new ResourceLocation(Main.MODID, "upgrades"));

}
