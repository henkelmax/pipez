package de.maxhenkel.pipez.tags;

import de.maxhenkel.pipez.Main;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModItemTags {

    public static final TagKey<Item> WRENCHES_TAG = ItemTags.create(new ResourceLocation("forge", "wrenches"));
    public static final TagKey<Item> WRENCH_TAG = ItemTags.create(new ResourceLocation("forge", "tools/wrench"));
    public static final TagKey<Item> TOOLS_TAG = ItemTags.create(new ResourceLocation("forge", "tools"));
    public static final TagKey<Item> UPGRADES_TAG = ItemTags.create(new ResourceLocation(Main.MODID, "upgrades"));

}
