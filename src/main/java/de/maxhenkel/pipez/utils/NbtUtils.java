package de.maxhenkel.pipez.utils;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;

import java.util.Optional;

public class NbtUtils {

    public static Optional<CompoundTag> componentPatchToNbtOptional(DataComponentPatch patch) {
        if(patch.isEmpty()) {
            return Optional.empty();
        }
        return codecToNbtOptional(DataComponentPatch.CODEC, patch);
    }

    public static <T> Optional<T> nbtToCodecOptional(Codec<T> codec, CompoundTag tag) {
        return codec.decode(NbtOps.INSTANCE, tag).result().map(Pair::getFirst);
    }

    public static <T> Optional<CompoundTag> codecToNbtOptional(Codec<T> codec, T value) {
        return codec.encodeStart(NbtOps.INSTANCE, value).result().filter(tag -> tag instanceof CompoundTag).map(CompoundTag.class::cast);
    }

    public static <T> CompoundTag codecToNbtDefault(Codec<T> codec, T value) {
        return codecToNbtOptional(codec, value).orElse(new CompoundTag());
    }

}
