package de.maxhenkel.pipez.blocks

import de.maxhenkel.pipez.Main
import de.maxhenkel.pipez.blocks.tileentity.GasPipeTileEntity
import de.maxhenkel.pipez.gui.ExtractContainer
import de.maxhenkel.pipez.gui.containerfactory.PipeContainerProvider
import de.maxhenkel.pipez.types.CacheMode
import de.maxhenkel.pipez.connections.CapabilityCache
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult

class GasPipeBlock : PipeBlock() {
    init {
        registryName = ResourceLocation(Main.MODID, "gas_pipe")
    }

    override fun canConnectTo(world: LevelAccessor, pos: BlockPos, facing: Direction): Boolean {
        val level = world as? Level ?: return false

        return CapabilityCache.INSTANCE.getGasCapability(
            level = level,
            blockPos = pos.relative(facing),
            direction = facing.opposite,
            cacheMode = CacheMode.REFRESH,
        ).isPresent
    }

    override fun isPipe(world: LevelAccessor, pos: BlockPos, facing: Direction): Boolean {
        val state = world.getBlockState(pos.relative(facing))
        return state.block == this
    }

    override fun createTileEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return GasPipeTileEntity(pos, state)
    }

    override fun onPipeSideActivated(
        state: BlockState,
        worldIn: Level,
        pos: BlockPos,
        player: Player,
        handIn: InteractionHand,
        hit: BlockHitResult,
        direction: Direction
    ): InteractionResult {
        val tileEntity = worldIn.getBlockEntity(pos)
        if (tileEntity is GasPipeTileEntity && isExtracting(worldIn, pos, direction)) {
            if (worldIn.isClientSide) {
                return InteractionResult.SUCCESS
            }
            PipeContainerProvider.openGui(player, tileEntity, direction, -1) { i, playerInventory, _ ->
                ExtractContainer(i, playerInventory, tileEntity, direction, -1)
            }
            return InteractionResult.SUCCESS
        }
        return super.onPipeSideActivated(state, worldIn, pos, player, handIn, hit, direction)
    }
}