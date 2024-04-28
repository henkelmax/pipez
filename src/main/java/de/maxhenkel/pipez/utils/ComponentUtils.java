package de.maxhenkel.pipez.utils;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

public class ComponentUtils {

    public static CompoundTag getTag(HolderLookup.Provider provider, ItemStack stack) {
        return (CompoundTag) DataComponentPatch.CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), stack.getComponentsPatch()).result().orElseGet(CompoundTag::new);
    }

    public static CompoundTag getTag(HolderLookup.Provider provider, FluidStack stack) {
        return (CompoundTag) DataComponentPatch.CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), stack.getComponentsPatch()).result().orElseGet(CompoundTag::new);
    }

    public static DataComponentPatch getPatch(HolderLookup.Provider provider, Tag tag) {
        return DataComponentPatch.CODEC.decode(provider.createSerializationContext(NbtOps.INSTANCE), tag).result().map(Pair::getFirst).orElse(DataComponentPatch.EMPTY);
    }

}
