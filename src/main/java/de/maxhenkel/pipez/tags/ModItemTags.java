package de.maxhenkel.pipez.tags;

import de.maxhenkel.pipez.Main;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModItemTags {

    public static final TagKey<Item> WRENCHES_TAG = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "wrenches"));
    public static final TagKey<Item> WRENCH_TAG = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "tools/wrench"));
    public static final TagKey<Item> TOOLS_TAG = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "tools"));
    public static final TagKey<Item> UPGRADES_TAG = ItemTags.create(ResourceLocation.fromNamespaceAndPath(Main.MODID, "upgrades"));

}
