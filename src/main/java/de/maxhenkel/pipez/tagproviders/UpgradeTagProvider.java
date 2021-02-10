package de.maxhenkel.pipez.tagproviders;

import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.items.ModItems;
import de.maxhenkel.pipez.tags.ModItemTags;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.TagsProvider;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.nio.file.Paths;

public class UpgradeTagProvider extends TagsProvider<Item> {

    public UpgradeTagProvider(DataGenerator generatorIn, @Nullable ExistingFileHelper existingFileHelper) {
        super(generatorIn, Registry.ITEM, Main.MODID, existingFileHelper);
    }

    @Override
    protected void registerTags() {
        getOrCreateBuilder(ModItemTags.UPGRADES_TAG)
                .addItemEntry(ModItems.BASIC_UPGRADE)
                .addItemEntry(ModItems.IMPROVED_UPGRADE)
                .addItemEntry(ModItems.ADVANCED_UPGRADE)
                .addItemEntry(ModItems.ULTIMATE_UPGRADE)
                .addItemEntry(ModItems.INFINITY_UPGRADE);
    }

    @Override
    protected Path makePath(ResourceLocation id) {
        return generator.getOutputFolder().resolve(Paths.get("data", id.getNamespace(), "tags", "items", id.getPath() + ".json"));
    }

    @Override
    public String getName() {
        return "PipezUpgradeTags";
    }
}
