package de.maxhenkel.pipez.utils

import net.minecraft.world.level.block.state.BlockState

object BlockStateComparer {

    @JvmStatic
    fun equalBlockState(blockStateA: BlockState, blockStateB: BlockState): Boolean {
        val propA = blockStateA.properties
        val propB = blockStateB.properties
        return propA.all {
            blockStateA.getValue(it) == blockStateB.getValue(it)
        } && propB.all {
            blockStateA.getValue(it) == blockStateB.getValue(it)
        }
    }
}