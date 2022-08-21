package de.maxhenkel.pipez.connections

import de.maxhenkel.pipez.Main
import de.maxhenkel.pipez.blocks.PipeBlock
import de.maxhenkel.pipez.blocks.tileentity.PipeTileEntity
import de.maxhenkel.pipez.types.*
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import thedarkcolour.kotlinforforge.kotlin.enumMapOf
import java.util.*
import kotlin.math.min

object PipeNetworkManager {
    val Directions = Direction.values()

    /**
     * Invalidate Pipe Network's Destinations & Update pipe at `pos`
     */
    @JvmStatic
    fun updatePipeNetworkAt(world: Level, pos: BlockPos):PipeNetworkResult? {
        val pipe = world.getBlockState(pos).block
        if (pipe !is PipeBlock) {
            return null
        }

        // PipeTileEntity work
        // If pos is PipeTileEntity
        pipe.getTileEntity(world, pos)?.apply {
            for (side in Directions) {
                if (isExtracting(side)) {
                    // Check facing is blockEntity
                    if (world.getBlockEntity(pos.relative(side)) == null) {
                        // Remove extracting
                        setExtracting(side, extracting = false)
                        if (!hasReasonToStay) {
                            pipe.setHasData(world, pos, false)
                        }
                        syncData()
                    }
                }
            }
        }

        // We need cache for
        // 1. Destinations "Absolute Directional" (Pipe insert Side, Dest Side)
        // 2. Pipe Connection Info
        // 3. Travel Location
        // 4. Queue to process
        // 5. PipeTiles
        val destinations = hashMapOf<AbsoluteDirection, AbsoluteDirection>()
        val connectionCache = hashMapOf<IntPos, PipeSideConnection>()
        val travels = arrayListOf<BlockPos>()
        val queue = LinkedList<BlockPos>()
        val pipeTiles = arrayListOf<PipeTileEntity>()

        // Add start side
        travels.add(pos)
        addSidesToQueue(
            world, pos, pipe, // variable
            dest = destinations,
            conn = connectionCache,
            pipeTiles = pipeTiles,
            travels = travels,
            queue = queue)
        // Loop until queue is empty
        while (queue.size > 0) {
            val blockPos = queue.removeFirst()
            (world.getBlockState(blockPos).block as? PipeBlock)?.also {
                addSidesToQueue(
                    world, pos = blockPos, pipe = it, // variable
                    dest = destinations,
                    conn = connectionCache,
                    pipeTiles = pipeTiles,
                    travels = travels,
                    queue = queue)
            }
        }
        // make cache of PipeEntities
        // Simulate mode
        for (pipeTE in pipeTiles) {
            // clear
            val teTravels = arrayListOf<IntPos>()
            val teQueue = LinkedList<Pair<IntPos, Int>>()
            val destDistances = hashMapOf<AbsoluteDirection, Int>() // Dest Side, Distance

            val pipeEntityPos = pipeTE.blockPos.toIntPos()
            teTravels.add(pipeEntityPos)
            simulateSidesToQueue(
                pos = pipeEntityPos,
                conn = connectionCache,
                dest = destinations,
                destDistance = destDistances,
                currentDistance = 1,
                travels = teTravels,
                queue = teQueue,
            )
            // Loop until queue is empty
            while (teQueue.size > 0) {
                val pipePos = teQueue.removeFirst()
                if (connectionCache.containsKey(pipePos.first)) {
                    simulateSidesToQueue(
                        pos = pipePos.first,
                        conn = connectionCache,
                        dest = destinations,
                        destDistance = destDistances,
                        currentDistance = pipePos.second,
                        travels = teTravels,
                        queue = teQueue,
                    )
                }
            }
            // Set cache
            pipeTE.setCache(destDistances.map { (absDirection, distance) ->
                Connection(absDirection.position.toBlockPos(), absDirection.direction, distance)
            }.sortedBy { it.distance })
        }
        Main.LOGGER.debug("NetWorkManager result!")
        // return result
        return PipeNetworkResult(
            destinations = destinations.values.toList(),
            connections = connectionCache,
            pipeTiles = pipeTiles.map { it.blockPos.toIntPos() },
        )
    }

    /**
     * Add sides (UP, DOWN, ...) blocks to queue
     */
    private fun addSidesToQueue(world: Level, pos: BlockPos, pipe: PipeBlock,
                                dest: MutableMap<AbsoluteDirection, AbsoluteDirection>,
                                conn: MutableMap<IntPos, PipeSideConnection>,
                                pipeTiles: MutableList<PipeTileEntity>,
                                travels: MutableList<BlockPos>, queue: MutableList<BlockPos>) {
        val sideConn = enumMapOf<PipeSide, Boolean>()
        val te = world.getBlockEntity(pos)

        for (side in Directions) {
            val pSide = PipeSide.fromDirection(side)
            val connected = pipe.isConnected(world, pos, side)
            sideConn[pSide] = connected

            if (connected) {
                // Check insertable
                if (isInsertValid(world, pos, te, side)) {
                    // Add destination (pipe, blockentity)
                    dest[AbsoluteDirection(
                        dimensionName = "",
                        position = pos.toIntPos(),
                        direction = side,
                    )] = AbsoluteDirection(
                        dimensionName = "",
                        position = pos.relative(side).toIntPos(),
                        direction = side.opposite,
                    )
                    continue
                }
                // If not insertable
                // Side pos
                val p = pos.relative(side)
                // If not traveled
                if (!travels.contains(p) && !queue.contains(p)) {
                    // Add to queue
                    travels.add(p)
                    queue.add(p)
                }
            }
        }
        // If PipeTileEntity, push to cache
        if (te != null && te is PipeTileEntity) {
            pipeTiles.add(te)
        }
        // put to connection map
        conn[pos.toIntPos()] = PipeSideConnection(
            up = sideConn[PipeSide.UP] ?: false,
            down = sideConn[PipeSide.DOWN] ?: false,
            north = sideConn[PipeSide.NORTH] ?: false,
            south = sideConn[PipeSide.SOUTH] ?: false,
            west = sideConn[PipeSide.WEST] ?: false,
            east = sideConn[PipeSide.EAST] ?: false,
        )
    }

    private fun simulateSidesToQueue(
        pos: IntPos,
        conn: MutableMap<IntPos, PipeSideConnection>,
        dest: MutableMap<AbsoluteDirection, AbsoluteDirection>,
        destDistance: MutableMap<AbsoluteDirection, Int>,
        currentDistance: Int,
        travels: MutableList<IntPos>, queue: MutableList<Pair<IntPos, Int>>,
    ) {
        val bPos = pos.toBlockPos()
        for (side in Directions) {
            val connected = conn[pos]?.isConnected(side) ?: false
            if (connected) {
                val absSide = AbsoluteDirection(
                    dimensionName = "",
                    position = pos,
                    direction = side,
                )
                if (dest.containsKey(absSide)) {
                    // Destination side!
                    dest[absSide]?.also {destSide ->
                        destDistance[destSide] = destDistance[destSide]?.let {
                            min(it, currentDistance)
                        } ?: currentDistance
                    }
                    continue
                }
                val p = bPos.relative(side).toIntPos()
                // If not traveled
                if (!travels.contains(p) && !queue.any { it.first == p }) {
                    // Add to queue
                    travels.add(p)
                    queue.add(Pair(p, currentDistance + 1))
                }
            }
        }
    }

    /**
     * Check can insert (ignore capability - will check with CapabilityCache - valid check!)
     */
    private fun isInsertValid(world: Level, pos: BlockPos, te:BlockEntity?, direction: Direction) : Boolean {
        if (te is PipeTileEntity?) {
            if (te?.isExtracting(direction) == true) {
                return false
            }
        }

        val sideTE = world.getBlockEntity(pos.relative(direction)) ?: return false
        if (sideTE is PipeTileEntity) {
            return false
        }
        val canConnect = CapabilityCache.INSTANCE.let {
            val blockPos = sideTE.blockPos
            val blockDirection = direction.opposite
            arrayOf(
                it.getItemCapability(world, blockPos, blockDirection, cacheMode = CacheMode.PREFER_CACHE),
                it.getFluidCapability(world, blockPos, blockDirection, cacheMode = CacheMode.PREFER_CACHE),
                it.getEnergyCapability(world, blockPos, blockDirection, cacheMode = CacheMode.PREFER_CACHE),
                it.getGasCapability(world, blockPos, blockDirection, cacheMode = CacheMode.PREFER_CACHE),
            ).map { cap -> cap.isPresent }
        }.any { it }

        return canConnect
    }

    private fun BlockPos.toIntPos(): IntPos {
        return IntPos(x, y, z)
    }
}