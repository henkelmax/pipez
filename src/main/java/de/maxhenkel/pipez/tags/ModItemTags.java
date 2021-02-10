package de.maxhenkel.pipez.tags;

import de.maxhenkel.pipez.Main;
import net.minecraft.item.Item;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;

public class ModItemTags {

    public static final ITag.INamedTag<Item> WRENCHES_TAG = ItemTags.createOptional(new ResourceLocation("forge", "wrenches"));
    public static final ITag.INamedTag<Item> WRENCH_TAG = ItemTags.createOptional(new ResourceLocation("forge", "tools/wrench"));
    public static final ITag.INamedTag<Item> TOOLS_TAG = ItemTags.createOptional(new ResourceLocation("forge", "tools"));
    public static final ITag.INamedTag<Item> UPGRADES_TAG = ItemTags.createOptional(new ResourceLocation(Main.MODID, "upgrades"));

}
