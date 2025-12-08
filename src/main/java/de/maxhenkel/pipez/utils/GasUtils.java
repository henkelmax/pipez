package de.maxhenkel.pipez.utils;

import de.maxhenkel.corelib.tag.SingleElementTag;
import de.maxhenkel.corelib.tag.Tag;
import de.maxhenkel.pipez.capabilities.ModCapabilities;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.*;
import net.minecraft.core.*;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.ItemCapability;

import javax.annotation.Nullable;
import java.util.Optional;

public class GasUtils {

    public static final Tag<Chemical> EMPTY_CHEMICAL_TAG = new SingleElementTag<>(ChemicalStack.EMPTY.getChemical().getRegistryName(), ChemicalStack.EMPTY.getChemical());

    @Nullable
    public static Tag<? extends Chemical> getGas(String name, boolean nullIfNotExists) {
        Identifier id;
        if (name.startsWith("#")) {
            Identifier resourceLocation = Identifier.tryParse(name.substring(1));
            if (resourceLocation == null) {
                return nullIfNotExists ? null : EMPTY_CHEMICAL_TAG;
            }
            return getGasTag(resourceLocation, nullIfNotExists);
        } else {
            id = Identifier.tryParse(name);
            if (id == null) {
                return nullIfNotExists ? null : EMPTY_CHEMICAL_TAG;
            }
            if (!MekanismAPI.CHEMICAL_REGISTRY.containsKey(id)) {
                return nullIfNotExists ? null : EMPTY_CHEMICAL_TAG;
            } else {
                return new SingleElementTag<>(id, MekanismAPI.CHEMICAL_REGISTRY.get(id).map(Holder.Reference::value).orElse(MekanismAPI.EMPTY_CHEMICAL));
            }
        }
    }

    @Nullable
    public static Tag<Chemical> getGasTag(Identifier id, boolean nullIfNotExists) {
        TagKey<? extends Chemical> tagKey = TagKey.create(MekanismAPI.CHEMICAL_REGISTRY_NAME, id);
        DefaultedRegistry<? extends Chemical> registry = MekanismAPI.CHEMICAL_REGISTRY;
        Optional<HolderSet.Named<Chemical>> tag = registry.get((TagKey) tagKey);
        if (tag.isEmpty()) {
            return nullIfNotExists ? null : EMPTY_CHEMICAL_TAG;
        } else {
            return new ChemicalTag(tag.get(), id);
        }
    }

    @Nullable
    public static ChemicalStack getGasContained(ItemStack stack) {
        ChemicalStack gas = getGasContained(stack, ModCapabilities.CHEMICAL_HANDLER_ITEM_CAPABILITY);
        if (gas == null || gas.isEmpty()) {
            return null;
        }
        return gas;
    }

    @Nullable
    public static ChemicalStack getGasContained(ItemStack stack, ItemCapability<? extends IChemicalHandler, Void> capability) {
        IChemicalHandler handler = stack.getCapability(capability, null);
        if (handler == null) {
            return null;
        }
        if (handler.getChemicalTanks() <= 0) {
            return null;
        }
        return handler.getChemicalInTank(0).copy();
    }

    public static boolean hasChemicalCapability(Level world, BlockPos pos, Direction facing) {
        return world.getCapability(ModCapabilities.CHEMICAL_HANDLER_CAPABILITY, pos, facing) != null;
    }

}
