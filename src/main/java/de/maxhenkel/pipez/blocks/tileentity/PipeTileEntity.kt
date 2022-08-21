package de.maxhenkel.pipez.blocks.tileentity

import de.maxhenkel.corelib.blockentity.ITickableBlockEntity
import de.maxhenkel.pipez.types.DirectionalPosition
import de.maxhenkel.pipez.blocks.PipeBlock
import de.maxhenkel.pipez.connections.PipeNetworkManager
import de.maxhenkel.pipez.connections.PipeNetworkQueue
import de.maxhenkel.pipez.types.ConnType
import de.maxhenkel.pipez.types.Connection
import de.maxhenkel.pipez.types.PipeSide
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.ByteTag
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.server.level.ServerChunkCache
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import thedarkcolour.kotlinforforge.kotlin.enumMapOf
import java.util.LinkedList

abstract class PipeTileEntity(tileEntityTypeIn:BlockEntityType<out BlockEntity>,
                     pos:BlockPos, state:BlockState) : BlockEntity(tileEntityTypeIn, pos, state), ITickableBlockEntity {


    companion object {
        private const val extractingSideTag = "ExtractingSides"
        private const val disconnectedSideTag = "DisconnectedSides"

        @JvmStatic
        fun markPipesDirty(world: Level, pos:BlockPos) {
            val travelPositions = arrayListOf<BlockPos>()
            val queue = LinkedList<BlockPos>()

            val markBlock = world.getBlockState(pos).block

            val pipe = world.getBlockState(pos).block
            if (pipe !is PipeBlock) {
                return
            }

            val pipeTe = pipe.getTileEntity(world, pos)
            if (pipeTe != null) {
                for (side in Direction.values()) {
                    if (world.getBlockEntity(pos.relative(side)) == null) {
                        pipeTe.apply {
                            setExtracting(side, false)
                            if (!hasReasonToStay) {
                                pipe.setHasData(world, pos, false)
                            }
                            pipeTe.syncData()
                        }
                    }
                }
            }

            travelPositions.add(pos)

            addToDirtyList(world, pos, pipe, travelPositions, queue)
            while (queue.size > 0) {
                val blockPos = queue.removeFirst()
                val block = world.getBlockState(blockPos).block
                if (block is PipeBlock) {
                    addToDirtyList(world, blockPos, block, travelPositions, queue)
                }
            }
            for (p in travelPositions) {
                (world.getBlockEntity(p) as? PipeTileEntity)?.apply {
                    // connectionCache = null
                }
            }
        }

        private fun addToDirtyList(world: Level, pos: BlockPos, pipeBlock: PipeBlock, travelPositions:MutableList<BlockPos>, queue: LinkedList<BlockPos>) {
            for (direction in Direction.values()) {
                if (pipeBlock.isConnected(world, pos, direction)) {
                    val p = pos.relative(direction)
                    if (!travelPositions.contains(p) && !queue.contains(p)) {
                        travelPositions.add(p)
                        queue.add(p)
                    }
                }
            }
        }
    }

    private var initFilter = false
    protected open var connectionCache:List<Connection> = emptyList()
    val connections:List<Connection> get() {
        return level?.let {
            if (!initFilter) {
                // PipeNetworkManager.updatePipeNetworkAt(it, blockPos)
                PipeNetworkQueue.INSTANCE.enqueuePipeBlockChanged(it, blockPos, true)
            }
            connectionCache
        } ?: emptyList()
    }
    val isExtracting:Boolean get() = extractingSides.values.any { it }
    val hasReasonToStay:Boolean get() {
        if (isExtracting) {
            return true
        }
        return disconnectedSides.values.any { it }
    }

    protected open val extractingSides = enumMapOf<PipeSide, Boolean>()
    protected open val disconnectedSides = enumMapOf<PipeSide, Boolean>()

    override fun tick() {

    }

    fun setCache(cache:List<Connection>) {
        initFilter = true
        this.connectionCache = cache
    }

    private fun updateCache() {
        if (3 == 3) {
            return
        }
        val blockState = blockState
        if (blockState.block !is PipeBlock) {
            // connectionCache = null
            return
        }
        if (!isExtracting) {
            // connectionCache = null
            return
        }

        val connections = HashMap<DirectionalPosition, Int>()

        val queue = HashMap<BlockPos, Int>()
        val travelPositions = arrayListOf<BlockPos>()
        val world = level ?: return

        addToQueue(world, worldPosition, queue, travelPositions, connections, 1)

        while (queue.size > 0) {
            val blockPosIntEntry = queue.entries.first()
            addToQueue(world, blockPosIntEntry.key, queue, travelPositions, connections, blockPosIntEntry.value)
            travelPositions.add(blockPosIntEntry.key)
            queue.remove(blockPosIntEntry.key)
        }

        connectionCache = connections.map {
            Connection(it.key.pos, it.key.direction, it.value)
        }.sortedBy {
            it.distance
        }
    }

    fun addToQueue(world: Level, position: BlockPos, queue: MutableMap<BlockPos, Int>, travelPositions: List<BlockPos>, insertPositions:MutableMap<DirectionalPosition, Int>, distance: Int) {
        val block = world.getBlockState(position).block
        if (block !is PipeBlock) {
            return
        }
        for (direction in Direction.values()) {
            if (block.isConnected(world, position, direction)) {
                val p = position.relative(direction)
                val dp = DirectionalPosition(p, direction.opposite)
                val connInsert = canInsert(position, direction);
                if (connInsert == ConnType.CAPABILITY || connInsert == ConnType.NO_CAPABILITY) {
                    if (!insertPositions.containsKey(dp)) {
                        insertPositions[dp] = distance
                    } else {
                        if (insertPositions[dp]!! > distance) {
                            insertPositions[dp] = distance
                        }
                    }
                } else {
                    if (!travelPositions.contains(p) && !queue.containsKey(p)) {
                        queue[p] = distance + 1
                    }
                }
            }
        }
    }

    fun canInsert(pos: BlockPos, direction: Direction):ConnType {
        val te = level?.getBlockEntity(pos) ?: return ConnType.NONE
        if (te is PipeTileEntity) {
            if (te.isExtracting(direction)) {
                return ConnType.NONE
            }
        }

        val sideTE = level?.getBlockEntity(pos.relative(direction)) ?: return ConnType.NONE
        if (sideTE is PipeTileEntity) {
            return ConnType.NONE
        }
        return if (canInsert(sideTE, direction.opposite)) {
            ConnType.CAPABILITY
        } else {
            ConnType.NO_CAPABILITY
        }
    }

    abstract fun canInsert(tileEntity: BlockEntity, direction: Direction): Boolean

    fun isExtracting(side:Direction): Boolean {
        return extractingSides[PipeSide.fromDirection(side)] ?: false
    }

    open fun setExtracting(side:Direction, extracting:Boolean) {
        extractingSides[PipeSide.fromDirection(side)] = extracting
        setChanged()
    }

    fun isDisconnected(side:Direction): Boolean {
        return disconnectedSides[PipeSide.fromDirection(side)] ?: false
    }

    fun setDisconnected(side: Direction, disconnected: Boolean) {
        disconnectedSides[PipeSide.fromDirection(side)] = disconnected
        setChanged()
    }

    override fun load(compoundTag: CompoundTag) {
        super.load(compoundTag)
        extractingSides.clear()
        val extractingList = compoundTag.getList(extractingSideTag, Tag.TAG_BYTE.toInt())
        val sides = PipeSide.values()
        if (extractingList.size >= sides.size) {
            for (i in sides.indices) {
                val b = extractingList[i] as ByteTag
                extractingSides[sides[i]] = b.asByte.toInt() != 0
            }
        }

        disconnectedSides.clear()
        val disconnectedList = compoundTag.getList(disconnectedSideTag, Tag.TAG_BYTE.toInt())
        if (disconnectedList.size >= sides.size) {
            for (i in sides.indices) {
                val b = disconnectedList[i] as ByteTag
                disconnectedSides[sides[i]] = b.asByte.toInt() != 0
            }
        }
    }

    override fun saveAdditional(compoundTag: CompoundTag) {
        super.saveAdditional(compoundTag)

        val extractingList = ListTag()
        val sides = PipeSide.values()

        for (side in sides) {
            extractingList.add(ByteTag.valueOf(extractingSides[side] ?: false))
        }
        compoundTag.put(extractingSideTag, extractingList)

        val disconnectedList = ListTag()
        for (side in sides) {
            disconnectedList.add(ByteTag.valueOf(disconnectedSides[side] ?: false))
        }
        compoundTag.put(disconnectedSideTag, disconnectedList)
    }

    override fun getUpdatePacket(): Packet<ClientGamePacketListener>? {
        return ClientboundBlockEntityDataPacket.create(this)
    }

    fun syncData(player: ServerPlayer) {
        updatePacket?.let { player.connection.send(it) }
    }

    fun syncData() {
        val world = level
        if (world == null || world.isClientSide) {
            return
        }
        val chunk = world.getChunkAt(blockPos)
        val chunkCache = world.chunkSource as? ServerChunkCache ?: return
        chunkCache.chunkMap.getPlayers(chunk.pos, false).forEach {p ->
            updatePacket?.let { p.connection.send(it) }
        }
    }

    override fun getUpdateTag(): CompoundTag {
        val updateTag = super.getUpdateTag()
        saveAdditional(updateTag)
        return updateTag
    }
}