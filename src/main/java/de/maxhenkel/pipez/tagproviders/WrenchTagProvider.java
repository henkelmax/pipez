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

public class WrenchTagProvider extends TagsProvider<Item> {

    public WrenchTagProvider(DataGenerator generatorIn, @Nullable ExistingFileHelper existingFileHelper) {
        super(generatorIn, Registry.ITEM, Main.MODID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        tag(ModItemTags.WRENCHES_TAG).addTag(ModItemTags.WRENCH_TAG);
        tag(ModItemTags.WRENCH_TAG).add(ModItems.WRENCH.get());
        tag(ModItemTags.TOOLS_TAG).addTag(ModItemTags.WRENCH_TAG);
    }

    @Override
    protected Path getPath(ResourceLocation id) {
        //TODO Check path
        return pathProvider.json(new ResourceLocation(id.getNamespace(), "/data/tags/items/" + id.getPath() + ".json"));
    }

    @Override
    public String getName() {
        return "PipezWrenchesTags";
    }
}
