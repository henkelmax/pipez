package de.maxhenkel.pipez.utils;

import de.maxhenkel.corelib.tag.Tag;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalType;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class GasTag implements Tag<Chemical> {

    private final HolderSet.Named<Chemical> tag;
    private final ResourceLocation id;
    private final ChemicalType type;

    public GasTag(HolderSet.Named<Chemical> tag, ResourceLocation id, ChemicalType type) {
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
        return gas.is(tag.key());
    }

    @Override
    public List<Chemical> getAll() {
        return tag.stream().map(Holder::value).toList();
    }

    public ChemicalType getChemicalType() {
        return type;
    }

}
