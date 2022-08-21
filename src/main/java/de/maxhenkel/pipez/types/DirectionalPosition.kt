package de.maxhenkel.pipez.types

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraftforge.common.util.INBTSerializable

data class DirectionalPosition(
    val pos: BlockPos,
    val direction: Direction,
) : INBTSerializable<CompoundTag> {
    companion object {
        fun fromNBT(compound: CompoundTag): DirectionalPosition {
            val p = compound.getCompound("Position")
            return DirectionalPosition(
                pos = BlockPos(p.getInt("X"), p.getInt("Y"), p.getInt("Z")),
                direction = Direction.from3DDataValue(compound.getByte("Direction").toInt()),
            )
        }
    }
    override fun serializeNBT(): CompoundTag {
        return CompoundTag().also { compound ->
            compound.put("Position", CompoundTag().apply {
                putInt("X", pos.x)
                putInt("Y", pos.y)
                putInt("Z", pos.z)
            })
            compound.putByte("Direction", direction.get3DDataValue().toByte())
        }
    }

    @Throws(Error::class)
    override fun deserializeNBT(compound: CompoundTag) {
        throw Error("Unimplemented!")
    }

}