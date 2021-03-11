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

public class WrenchTagProvider extends TagsProvider<Item> {

    public WrenchTagProvider(DataGenerator generatorIn, @Nullable ExistingFileHelper existingFileHelper) {
        super(generatorIn, Registry.ITEM, Main.MODID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        tag(ModItemTags.WRENCHES_TAG).addTag(ModItemTags.WRENCH_TAG);
        tag(ModItemTags.WRENCH_TAG).add(ModItems.WRENCH);
        tag(ModItemTags.TOOLS_TAG).addTag(ModItemTags.WRENCH_TAG);
    }

    @Override
    protected Path getPath(ResourceLocation id) {
        return generator.getOutputFolder().resolve(Paths.get("data", id.getNamespace(), "tags", "items", id.getPath() + ".json"));
    }

    @Override
    public String getName() {
        return "PipezWrenchesTags";
    }
}
