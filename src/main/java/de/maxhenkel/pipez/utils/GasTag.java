package de.maxhenkel.pipez.utils;

import de.maxhenkel.corelib.tag.Tag;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.tags.ITag;

import java.util.List;
import java.util.stream.Collectors;

public class GasTag implements Tag<Chemical> {

    private final ITag<Chemical> tag;
    private final ResourceLocation id;
    private final ChemicalType type;

    public GasTag(ITag<Chemical> tag, ResourceLocation id, ChemicalType type) {
        this.tag = tag;
        this.id = id;
        this.type = type;
    }

    @Override
    public ResourceLocation getName() {
        return id;
    }

    @Override
    public boolean contains(Chemical gas) {
        return gas.is(tag.getKey());
    }

    @Override
    public List<Chemical> getAll() {
        return tag.stream().collect(Collectors.toList());
    }

    public ChemicalType getChemicalType() {
        return type;
    }

}
