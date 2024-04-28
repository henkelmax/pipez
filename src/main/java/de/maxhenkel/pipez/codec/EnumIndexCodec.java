package de.maxhenkel.pipez.codec;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class EnumIndexCodec {

    public static <T extends Enum<T>> Codec<T> of(Class<T> enumClass) {
        T[] enumConstants = enumClass.getEnumConstants();
        return Codec.BYTE.xmap(b -> enumConstants[b], t -> (byte) t.ordinal());
    }

    public static <T extends Enum<T>> StreamCodec<RegistryFriendlyByteBuf, T> ofStream(Class<T> enumClass) {
        T[] enumConstants = enumClass.getEnumConstants();
        return new StreamCodec<>() {
            @Override
            public T decode(RegistryFriendlyByteBuf buf) {
                return enumConstants[buf.readByte()];
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, T value) {
                buf.writeByte(value.ordinal());
            }
        };
    }

}
