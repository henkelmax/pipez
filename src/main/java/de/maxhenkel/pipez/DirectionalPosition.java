package de.maxhenkel.pipez;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;

public class DirectionalPosition implements INBTSerializable<CompoundNBT> {

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
    public CompoundNBT serializeNBT() {
        CompoundNBT compound = new CompoundNBT();
        CompoundNBT p = new CompoundNBT();
        p.putInt("X", pos.getX());
        p.putInt("Y", pos.getY());
        p.putInt("Z", pos.getZ());
        compound.put("Position", p);
        compound.putByte("Direction", (byte) direction.get3DDataValue());
        return compound;
    }

    @Override
    public void deserializeNBT(CompoundNBT compound) {
        CompoundNBT p = compound.getCompound("Position");
        pos = new BlockPos(p.getInt("X"), p.getInt("Y"), p.getInt("Z"));
        direction = Direction.from3DDataValue(compound.getByte("Direction"));
    }
}
