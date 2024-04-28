package de.maxhenkel.pipez;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class DirectionalPosition {

    public static final Codec<DirectionalPosition> CODEC = RecordCodecBuilder.create(i -> {
        return i.group(
                BlockPos.CODEC.fieldOf("position").forGetter(DirectionalPosition::getPos),
                Direction.CODEC.fieldOf("direction").forGetter(DirectionalPosition::getDirection)
        ).apply(i, DirectionalPosition::new);
    });

    public static final StreamCodec<RegistryFriendlyByteBuf, DirectionalPosition> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            DirectionalPosition::getPos,
            Direction.STREAM_CODEC,
            DirectionalPosition::getDirection,
            DirectionalPosition::new
    );

    private BlockPos pos;
    private Direction direction;

    public DirectionalPosition(BlockPos pos, Direction direction) {
        this.pos = pos;
        this.direction = direction;
    }

    public DirectionalPosition() {

    }

    public BlockPos getPos() {
        return pos;
    }

    public Direction getDirection() {
        return direction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DirectionalPosition that = (DirectionalPosition) o;
        if (!pos.equals(that.pos)) {
            return false;
        }
        return direction == that.direction;
    }

    @Override
    public int hashCode() {
        int result = pos.hashCode();
        result = 31 * result + direction.hashCode();
        return result;
    }
}
