package de.maxhenkel.pipez.utils;

import de.maxhenkel.corelib.tag.Tag;
import mekanism.api.chemical.Chemical;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class ChemicalTag implements Tag<Chemical> {

    private final HolderSet.Named<Chemical> tag;
    private final ResourceLocation id;

    public ChemicalTag(HolderSet.Named<Chemical> tag, ResourceLocation id) {
        this.tag = tag;
        this.id = id;
    }

    @Override
    public ResourceLocation getName() {
        return id;
    }

    @Override
    public boolean contains(Chemical gas) {
        return gas.is(tag.key());
    }

    @Override
    public List<Chemical> getAll() {
        return tag.stream().map(Holder::value).toList();
    }

}
