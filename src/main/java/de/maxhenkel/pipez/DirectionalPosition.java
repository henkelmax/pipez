package de.maxhenkel.pipez;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public class DirectionalPosition implements INBTSerializable<CompoundTag> {

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

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag compound = new CompoundTag();
        CompoundTag p = new CompoundTag();
        p.putInt("X", pos.getX());
        p.putInt("Y", pos.getY());
        p.putInt("Z", pos.getZ());
        compound.put("Position", p);
        compound.putByte("Direction", (byte) direction.get3DDataValue());
        return compound;
    }

    @Override
    public void deserializeNBT(CompoundTag compound) {
        CompoundTag p = compound.getCompound("Position");
        pos = new BlockPos(p.getInt("X"), p.getInt("Y"), p.getInt("Z"));
        direction = Direction.from3DDataValue(compound.getByte("Direction"));
    }
}
