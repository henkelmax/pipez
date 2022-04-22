package de.maxhenkel.pipez.utils;

import de.maxhenkel.corelib.tag.Tag;
import mekanism.api.chemical.gas.Gas;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.tags.ITag;

import java.util.List;
import java.util.stream.Collectors;

public class GasTag implements Tag<Gas> {

    private final ITag<Gas> tag;
    private final ResourceLocation id;

    public GasTag(ITag<Gas> tag, ResourceLocation id) {
        this.tag = tag;
        this.id = id;
    }

    @Override
    public ResourceLocation getName() {
        return id;
    }

    @Override
    public boolean contains(Gas gas) {
        return gas.is(tag.getKey());
    }

    @Override
    public List<Gas> getAll() {
        return tag.stream().collect(Collectors.toList());
    }
}
