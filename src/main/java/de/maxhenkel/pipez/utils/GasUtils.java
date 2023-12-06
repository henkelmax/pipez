package de.maxhenkel.pipez.utils;

import de.maxhenkel.corelib.tag.SingleElementTag;
import de.maxhenkel.corelib.tag.Tag;
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

    public static final Tag<Chemical> EMPTY_GAS_TAG = new SingleElementTag<>(GasStack.EMPTY.getType().getRegistryName(), GasStack.EMPTY.getType());
    public static final Tag<Chemical> EMPTY_INFUSION_TAG = new SingleElementTag<>(InfusionStack.EMPTY.getType().getRegistryName(), InfusionStack.EMPTY.getType());
    public static final Tag<Chemical> EMPTY_PIGMENT_TAG = new SingleElementTag<>(PigmentStack.EMPTY.getType().getRegistryName(), PigmentStack.EMPTY.getType());
    public static final Tag<Chemical> EMPTY_SLURRY_TAG = new SingleElementTag<>(SlurryStack.EMPTY.getType().getRegistryName(), SlurryStack.EMPTY.getType());

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
            return getGasTag(name.substring(1), nullIfNotExists, type);
        } else {
            Registry<? extends Chemical> registry = getRegistry(type);
            id = new ResourceLocation(name);
            if (!registry.containsKey(id)) {
                return nullIfNotExists ? null : getEmptyStack(type);
            } else {
                return new SingleElementTag<>(id, registry.get(id));
            }
        }
    }

    @Nullable
    public static Tag<Chemical> getGasTag(String name, boolean nullIfNotExists, ChemicalType type) {
        ResourceLocation id = new ResourceLocation(name);
        TagKey<? extends Chemical> tagKey = null;
        Registry<? extends Chemical> registry = null;
        switch (type) {
            case INFUSION -> {
                tagKey = ChemicalTags.INFUSE_TYPE.tag(id);
                // TODO Re add when Mekanism is updated
                // registry = MekanismAPI.infuseTypeRegistry();
            }
            case PIGMENT -> {
                tagKey = ChemicalTags.PIGMENT.tag(id);
                // TODO Re add when Mekanism is updated
                // registry = MekanismAPI.pigmentRegistry();
            }
            case SLURRY -> {
                tagKey = ChemicalTags.SLURRY.tag(id);
                // TODO Re add when Mekanism is updated
                // registry = MekanismAPI.slurryRegistry();
            }
            default -> {
                tagKey = ChemicalTags.GAS.tag(id);
                // TODO Re add when Mekanism is updated
                // registry = MekanismAPI.gasRegistry();
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
        // TODO Re add when Mekanism is updated
        /*switch (type) {
            case INFUSION -> {
                return MekanismAPI.infuseTypeRegistry();
            }
            case PIGMENT -> {
                return MekanismAPI.pigmentRegistry();
            }
            case SLURRY -> {
                return MekanismAPI.slurryRegistry();
            }
            default -> {
                return MekanismAPI.gasRegistry();
            }
        }*/
        return null;
    }

    public static ResourceLocation getResourceLocation(Chemical chemical) {
        // TODO Re add when Mekanism is updated
        /*switch (ChemicalType.getTypeFor(chemical)) {
            case INFUSION -> {
                return MekanismAPI.infuseTypeRegistry().getKey((InfuseType) chemical);
            }
            case PIGMENT -> {
                return MekanismAPI.pigmentRegistry().getKey((Pigment) chemical);
            }
            case SLURRY -> {
                return MekanismAPI.slurryRegistry().getKey((Slurry) chemical);
            }
            default -> {
                return MekanismAPI.gasRegistry().getKey((Gas) chemical);
            }
        }*/
        return null;
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
                // TODO Re add when Mekanism is updated
                /*ModCapabilities.GAS_HANDLER_CAPABILITY,
                ModCapabilities.INFUSION_HANDLER_CAPABILITY,
                ModCapabilities.PIGMENT_HANDLER_CAPABILITY,
                ModCapabilities.SLURRY_HANDLER_CAPABILITY*/
        };
    }

    public static ItemCapability<? extends IChemicalHandler<?, ?>, Direction>[] getChemicalItemCapabilities() {
        return new ItemCapability[]{
                // TODO Re add when Mekanism is updated
                /*ModCapabilities.GAS_HANDLER_CAPABILITY,
                ModCapabilities.INFUSION_HANDLER_CAPABILITY,
                ModCapabilities.PIGMENT_HANDLER_CAPABILITY,
                ModCapabilities.SLURRY_HANDLER_CAPABILITY*/
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
