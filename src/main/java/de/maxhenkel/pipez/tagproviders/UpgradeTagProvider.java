package de.maxhenkel.pipez.tagproviders;

import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.items.ModItems;
import de.maxhenkel.pipez.tags.ModItemTags;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;
import java.nio.file.Path;

public class UpgradeTagProvider extends TagsProvider<Item> {

    public UpgradeTagProvider(DataGenerator generatorIn, @Nullable ExistingFileHelper existingFileHelper) {
        super(generatorIn, Registry.ITEM, Main.MODID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        tag(ModItemTags.UPGRADES_TAG)
                .add(ModItems.BASIC_UPGRADE.get())
                .add(ModItems.IMPROVED_UPGRADE.get())
                .add(ModItems.ADVANCED_UPGRADE.get())
                .add(ModItems.ULTIMATE_UPGRADE.get())
                .add(ModItems.INFINITY_UPGRADE.get());
    }

    @Override
    protected Path getPath(ResourceLocation id) {
        //TODO Check path
        return pathProvider.json(new ResourceLocation(id.getNamespace(), "/data/tags/items/" + id.getPath() + ".json"));
    }

    @Override
    public String getName() {
        return "PipezUpgradeTags";
    }
}
