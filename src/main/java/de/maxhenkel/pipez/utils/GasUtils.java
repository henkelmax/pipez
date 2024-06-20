package de.maxhenkel.pipez.utils;

import de.maxhenkel.corelib.tag.SingleElementTag;
import de.maxhenkel.corelib.tag.Tag;
import de.maxhenkel.pipez.capabilities.ModCapabilities;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.*;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;

import javax.annotation.Nullable;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;

public class GasUtils {

    public static final Tag<Chemical> EMPTY_GAS_TAG = new SingleElementTag<>(GasStack.EMPTY.getChemical().getRegistryName(), GasStack.EMPTY.getChemical());
    public static final Tag<Chemical> EMPTY_INFUSION_TAG = new SingleElementTag<>(InfusionStack.EMPTY.getChemical().getRegistryName(), InfusionStack.EMPTY.getChemical());
    public static final Tag<Chemical> EMPTY_PIGMENT_TAG = new SingleElementTag<>(PigmentStack.EMPTY.getChemical().getRegistryName(), PigmentStack.EMPTY.getChemical());
    public static final Tag<Chemical> EMPTY_SLURRY_TAG = new SingleElementTag<>(SlurryStack.EMPTY.getChemical().getRegistryName(), SlurryStack.EMPTY.getChemical());

    @Nullable
    public static Map.Entry<ChemicalType, Tag<? extends Chemical>> getGas(String name, boolean nullIfNotExists) {
        for (ChemicalType type : ChemicalType.values()) {
            Tag<? extends Chemical> tag = getGas(name, true, type);
            if (tag != null) {
                return new AbstractMap.SimpleEntry<>(type, tag);
            }
        }
        return new AbstractMap.SimpleEntry<>(ChemicalType.GAS, nullIfNotExists ? null : EMPTY_GAS_TAG);
    }

    @Nullable
    public static Tag<? extends Chemical> getGas(String name, boolean nullIfNotExists, ChemicalType type) {
        ResourceLocation id;
        if (name.startsWith("#")) {
            ResourceLocation resourceLocation = ResourceLocation.tryParse(name.substring(1));
            if (resourceLocation == null) {
                return nullIfNotExists ? null : getEmptyStack(type);
            }
            return getGasTag(resourceLocation, nullIfNotExists, type);
        } else {
            Registry<? extends Chemical> registry = getRegistry(type);
            id = ResourceLocation.tryParse(name);
            if (id == null) {
                return nullIfNotExists ? null : getEmptyStack(type);
            }
            if (!registry.containsKey(id)) {
                return nullIfNotExists ? null : getEmptyStack(type);
            } else {
                return new SingleElementTag<>(id, registry.get(id));
            }
        }
    }

    @Nullable
    public static Tag<Chemical> getGasTag(ResourceLocation id, boolean nullIfNotExists, ChemicalType type) {
        TagKey<? extends Chemical> tagKey = null;
        Registry<? extends Chemical> registry = null;
        switch (type) {
            case INFUSION -> {
                tagKey = TagKey.create(MekanismAPI.INFUSE_TYPE_REGISTRY_NAME, id);
                registry = MekanismAPI.INFUSE_TYPE_REGISTRY;
            }
            case PIGMENT -> {
                tagKey = TagKey.create(MekanismAPI.PIGMENT_REGISTRY_NAME, id);
                registry = MekanismAPI.PIGMENT_REGISTRY;
            }
            case SLURRY -> {
                tagKey = TagKey.create(MekanismAPI.SLURRY_REGISTRY_NAME, id);
                registry = MekanismAPI.SLURRY_REGISTRY;
            }
            default -> {
                tagKey = TagKey.create(MekanismAPI.GAS_REGISTRY_NAME, id);
                registry = MekanismAPI.GAS_REGISTRY;
            }
        }
        if (registry == null) {
            return nullIfNotExists ? null : getEmptyStack(type);
        }
        Optional<HolderSet.Named<Chemical>> tag = registry.getTag((TagKey) tagKey);
        if (tag.isEmpty()) {
            return nullIfNotExists ? null : getEmptyStack(type);
        } else {
            return new GasTag(tag.get(), id, type);
        }
    }

    @Nullable
    public static ChemicalStack getGasContained(ItemStack stack) {
        for (ItemCapability<? extends IChemicalHandler, Direction> capability : getChemicalItemCapabilities()) {
            ChemicalStack gas = getGasContained(stack, capability);
            if (gas == null || gas.isEmpty()) {
                continue;
            }
            return gas;
        }
        return null;
    }

    @Nullable
    public static ChemicalStack getGasContained(ItemStack stack, ItemCapability<? extends IChemicalHandler, Direction> capability) {
        IChemicalHandler handler = stack.getCapability(capability, null);
        if (handler == null) {
            return null;
        }
        if (handler.getTanks() <= 0) {
            return null;
        }
        return handler.getChemicalInTank(0).copy();
    }

    public static Tag<Chemical> getEmptyStack(ChemicalType type) {
        switch (type) {
            case INFUSION -> {
                return EMPTY_INFUSION_TAG;
            }
            case SLURRY -> {
                return EMPTY_SLURRY_TAG;
            }
            case PIGMENT -> {
                return EMPTY_PIGMENT_TAG;
            }
            default -> {
                return EMPTY_GAS_TAG;
            }
        }
    }

    public static Registry<? extends Chemical> getRegistry(ChemicalType type) {
        switch (type) {
            case INFUSION -> {
                return MekanismAPI.INFUSE_TYPE_REGISTRY;
            }
            case PIGMENT -> {
                return MekanismAPI.PIGMENT_REGISTRY;
            }
            case SLURRY -> {
                return MekanismAPI.SLURRY_REGISTRY;
            }
            default -> {
                return MekanismAPI.GAS_REGISTRY;
            }
        }
    }

    public static ResourceLocation getResourceLocation(Chemical chemical) {
        switch (ChemicalType.getTypeFor(chemical)) {
            case INFUSION -> {
                return MekanismAPI.INFUSE_TYPE_REGISTRY.getKey((InfuseType) chemical);
            }
            case PIGMENT -> {
                return MekanismAPI.PIGMENT_REGISTRY.getKey((Pigment) chemical);
            }
            case SLURRY -> {
                return MekanismAPI.SLURRY_REGISTRY.getKey((Slurry) chemical);
            }
            default -> {
                return MekanismAPI.GAS_REGISTRY.getKey((Gas) chemical);
            }
        }
    }

    public static ChemicalStack createChemicalStack(Chemical chemical, long amount) {
        switch (ChemicalType.getTypeFor(chemical)) {
            case GAS -> {
                return new GasStack((Gas) chemical, amount);
            }
            case INFUSION -> {
                return new InfusionStack((InfuseType) chemical, amount);
            }
            case PIGMENT -> {
                return new PigmentStack((Pigment) chemical, amount);
            }
            case SLURRY -> {
                return new SlurryStack((Slurry) chemical, amount);
            }
            default -> {
                return GasStack.EMPTY;
            }
        }
    }

    public static BlockCapability<? extends IChemicalHandler<?, ?>, Direction>[] getChemicalBlockCapabilities() {
        return new BlockCapability[]{
                ModCapabilities.GAS_HANDLER_CAPABILITY,
                ModCapabilities.INFUSION_HANDLER_CAPABILITY,
                ModCapabilities.PIGMENT_HANDLER_CAPABILITY,
                ModCapabilities.SLURRY_HANDLER_CAPABILITY
        };
    }

    public static ItemCapability<? extends IChemicalHandler<?, ?>, Direction>[] getChemicalItemCapabilities() {
        return new ItemCapability[]{
                ModCapabilities.GAS_HANDLER_ITEM_CAPABILITY,
                ModCapabilities.INFUSION_HANDLER_ITEM_CAPABILITY,
                ModCapabilities.PIGMENT_HANDLER_ITEM_CAPABILITY,
                ModCapabilities.SLURRY_HANDLER_ITEM_CAPABILITY
        };
    }

    public static boolean hasChemicalCapability(Level world, BlockPos pos, Direction facing) {
        for (BlockCapability<?, Direction> capability : getChemicalBlockCapabilities()) {
            if (world.getCapability(capability, pos, facing) != null) {
                return true;
            }
        }
        return false;
    }

}
