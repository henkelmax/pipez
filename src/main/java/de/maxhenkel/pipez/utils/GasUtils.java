package de.maxhenkel.pipez.utils;

import de.maxhenkel.corelib.tag.NamedTagWrapper;
import de.maxhenkel.corelib.tag.SingleElementTag;
import de.maxhenkel.pipez.capabilities.ModCapabilities;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;

public class GasUtils {

    public static final ITag.INamedTag<Gas> EMPTY_GAS_TAG = new SingleElementTag<>(GasStack.EMPTY.getType());

    @Nullable
    public static ITag.INamedTag<Gas> getGas(String name, boolean nullIfNotExists) {
        ResourceLocation id;
        if (name.startsWith("#")) {
            id = new ResourceLocation(name.substring(1));
            ITag<Gas> tag = ChemicalTags.GAS.getCollection().get(id);
            if (tag == null) {
                return nullIfNotExists ? null : EMPTY_GAS_TAG;
            } else {
                return new NamedTagWrapper<>(tag, id);
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
                    return new SingleElementTag<>(gas);
                }
            }
        }
    }

    @Nullable
    public static GasStack getGasContained(ItemStack stack) {
        LazyOptional<IGasHandler> c = stack.getCapability(ModCapabilities.GAS_HANDLER_CAPABILITY);
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
