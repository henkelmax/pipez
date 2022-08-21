package de.maxhenkel.pipez.utils;

import de.maxhenkel.corelib.tag.SingleElementTag;
import de.maxhenkel.corelib.tag.Tag;
import de.maxhenkel.pipez.connections.ModCapabilities;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.tags.ITag;
import net.minecraftforge.registries.tags.ITagManager;

import javax.annotation.Nullable;

public class GasUtils {

    public static final Tag<Gas> EMPTY_GAS_TAG = new SingleElementTag<>(GasStack.EMPTY.getType().getRegistryName(), GasStack.EMPTY.getType());

    @Nullable
    public static Tag<Gas> getGas(String name, boolean nullIfNotExists) {
        ResourceLocation id;
        if (name.startsWith("#")) {
            id = new ResourceLocation(name.substring(1));
            TagKey<Gas> tagKey = ChemicalTags.GAS.tag(id);
            ITagManager<Gas> tags = MekanismAPI.gasRegistry().tags();
            if (tags == null) {
                return nullIfNotExists ? null : EMPTY_GAS_TAG;
            }
            ITag<Gas> tag = tags.getTag(tagKey);
            if (tag.isEmpty()) {
                return nullIfNotExists ? null : EMPTY_GAS_TAG;
            } else {
                return new GasTag(tag, id);
            }
        } else {
            id = new ResourceLocation(name);
            if (!MekanismAPI.gasRegistry().containsKey(id)) {
                return nullIfNotExists ? null : EMPTY_GAS_TAG;
            } else {
                Gas gas = MekanismAPI.gasRegistry().getValue(new ResourceLocation(name));
                if (gas == null) {
                    return nullIfNotExists ? null : EMPTY_GAS_TAG;
                } else {
                    return new SingleElementTag<>(id, gas);
                }
            }
        }
    }

    @Nullable
    public static Tag<Gas> getGasTag(String name, boolean nullIfNotExists) {
        ResourceLocation id = new ResourceLocation(name);
        TagKey<Gas> tagKey = ChemicalTags.GAS.tag(id);
        ITagManager<Gas> tags = MekanismAPI.gasRegistry().tags();
        if (tags == null) {
            return nullIfNotExists ? null : EMPTY_GAS_TAG;
        }
        ITag<Gas> tag = tags.getTag(tagKey);
        if (tag.isEmpty()) {
            return nullIfNotExists ? null : EMPTY_GAS_TAG;
        } else {
            return new GasTag(tag, id);
        }
    }

    @Nullable
    public static GasStack getGasContained(ItemStack stack) {
        LazyOptional<IGasHandler> c = stack.getCapability(ModCapabilities.getGAS_HANDLER_CAPABILITY());
        IGasHandler handler = c.orElse(null);
        if (handler == null) {
            return null;
        }
        if (handler.getTanks() <= 0) {
            return null;
        }
        return handler.getChemicalInTank(0).copy();
    }

}
