package de.maxhenkel.pipez.blocks.tileentity.types

import de.maxhenkel.pipez.Filter
import de.maxhenkel.pipez.Main
import de.maxhenkel.pipez.blocks.tileentity.PipeLogicTileEntity
import de.maxhenkel.pipez.blocks.tileentity.PipeTileEntity
import de.maxhenkel.pipez.blocks.tileentity.UpgradeTileEntity
import de.maxhenkel.pipez.blocks.tileentity.UpgradeTileEntity.Distribution
import de.maxhenkel.pipez.types.*
import de.maxhenkel.pipez.utils.FilterUtil
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.world.level.Level
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/**
 * Abstract Tank-Based Pipe Type
 *
 * T: Type (Tag type)
 * C: Capability
 * M: Material (Almost Stack of T)
 */
abstract class AbsTankPipeType<T, C, M> : PipeType<T>() {

    /**
     * Hard Limit of Tank
     */
    protected abstract val hardLimit:Long

    protected val sides = arrayOf(Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)

    open fun ejectMaterial(tileEntity: PipeLogicTileEntity, side: Direction) {
        val level = tileEntity.level ?: return
        val pSide = PipeSide.fromDirection(side)
        if (!tileEntity.isExtracting(side)) {
            return
        }
        if (!tileEntity.shouldWork(pSide, this)) {
            return
        }
        val upgrade = tileEntity.getUpgrade(side)
        val workSpeed = getSpeed(upgrade)
        val tick = tileEntity.getTickCount()
        val lastWorked = tileEntity.lastSent
        if (workSpeed > 1) {
            if (tick - lastWorked < workSpeed) {
                return
            }
            tileEntity.lastSent = tick
        }

        val materialStorage = getCapability(
            level,
            blockPos = tileEntity.blockPos.relative(side),
            direction = side.opposite,
            cacheMode = CacheMode.PREFER_CACHE,
        ) ?: return

        val connections = tileEntity.connections

        if (connections.isEmpty()) {
            return
        }

        val distribution = tileEntity.getDistribution(side, this) ?: Distribution.NEAREST
        var maxAmount = getRate(upgrade).toLong()
        val rawFilters = tileEntity.getFilters(side, this) as List<Filter<T>> // Force cast
        val (globalFilters, localFilters) = FilterUtil.separateFilter(rawFilters)
        val invertFilter = tileEntity.getFilterMode(side, this) == UpgradeTileEntity.FilterMode.BLACKLIST


        val tanks = getTanks(materialStorage)
        for (i in 0 until tanks) {
            if (maxAmount <= 0) {
                break
            }
            if (!canExtract(materialStorage, i)) {
                continue
            }
            val filledMaterial = getTankMaterial(materialStorage, i)
            val materialAmount = limit(getMaterialAmount(filledMaterial))
            if (materialAmount <= 0) {
                continue
            }
            // Filter
            val globalCanInsert = (canInsert(connection = null, filledMaterial, filters = globalFilters) ?: true) == !invertFilter
            val options = TransferOptions(
                upgrade, distribution, localFilters, invertFilter, globalCanInsert, simulate = false
            )

            val simulateMaterial = extractMaterial(materialStorage, i, min(maxAmount, materialAmount), true)
            if (limit(getMaterialAmount(simulateMaterial)) <= 0) {
                continue
            }
            val amount = when (distribution) {
                Distribution.ROUND_ROBIN ->
                    transferMaterialEquality(tileEntity, pSide, connections, simulateMaterial, options)
                Distribution.RANDOM ->
                    transferMaterialOrdered(level,
                        connections = connections.toMutableList().apply { shuffle() }, simulateMaterial, options)
                Distribution.NEAREST ->
                    transferMaterialOrdered(level, connections, simulateMaterial, options)
                Distribution.FURTHEST ->
                    transferMaterialOrdered(level, connections, simulateMaterial, options)
            }
            if (amount <= 0) {
                continue
            }
            val extractAmount = extractMaterial(materialStorage, i, amount, false)
            maxAmount -= limit(getMaterialAmount(extractAmount))
        }
    }

    open fun receiveMaterial(tileEntity: PipeLogicTileEntity, pSide: PipeSide, material: M, simulate: Boolean): Long {
        val side = pSide.toDirection()

        if (!tileEntity.isExtracting(side)) {
            return 0
        }
        if (!tileEntity.shouldWork(pSide, this)) {
            return 0
        }
        val upgrade = tileEntity.getUpgrade(side)
        val workSpeed = getSpeed(upgrade)
        val tick = tileEntity.getTickCount()
        val lastWorked = tileEntity.lastSent
        val lastSimulated = tileEntity.lastSimulated
        if (workSpeed > 1) {
            if (!lastSimulated && tick - lastWorked < workSpeed) {
                return 0
            }
            if (!simulate) {
                tileEntity.lastSent = tick
                tileEntity.lastSimulated = false
            } else {
                tileEntity.lastSimulated = true
            }
        } else {
            tileEntity.lastSent = 0L
            tileEntity.lastSimulated = false
        }

        val level = tileEntity.level ?: return 0
        val connections = tileEntity.connections
        // Main.LOGGER.debug("Destinations: ${connections.joinToString {it.pos.toString() + " / " + it.direction + " / " + it.distance}}")
        if (connections.isEmpty()) {
            return 0
        }

        val rawFilters = tileEntity.getFilters(side, this) as List<Filter<T>> // Force cast
        val (globalFilters, localFilters) = FilterUtil.separateFilter(rawFilters)
        val invertFilter = tileEntity.getFilterMode(side, this) == UpgradeTileEntity.FilterMode.BLACKLIST

        val options = TransferOptions(
            upgrade = tileEntity.getUpgrade(side),
            localFilters = localFilters,
            invertFilter = invertFilter,
            globalCanInsert = (canInsert(connection = null, material, filters = globalFilters) ?: true) == !invertFilter,
            distribution = tileEntity.getDistribution(side, this) ?: Distribution.NEAREST,
            simulate = simulate,
        )


        return when (options.distribution) {
            Distribution.ROUND_ROBIN ->
                transferMaterialEquality(
                    tileEntity = tileEntity,
                    side = pSide,
                    connections, material, options
                )
            Distribution.RANDOM ->
                if (simulate) {
                    transferMaterialOrdered(level, connections, material, options) // random doesn't matter of total.
                } else {
                    transferMaterialOrdered(
                        level,
                        connections = connections.toMutableList().apply { shuffle() },
                        material, options)
                }
            Distribution.NEAREST ->
                transferMaterialOrdered(level, connections, material, options)
            Distribution.FURTHEST ->
                transferMaterialOrdered(level, connections, material, options)
        }
    }


    /**
     * Transfer Materials with equality
     */
    protected open fun transferMaterialEquality(
        tileEntity: PipeLogicTileEntity,
        side: PipeSide,
        connections: List<Connection>,
        material: M,
        options: TransferOptions<T>,
    ): Long {
        val level = tileEntity.level ?: return 0
        val simulate = options.simulate

        val maxTransfer = min(limit(getMaterialAmount(material)), getRate(options.upgrade).toLong())
        if (maxTransfer <= 0) {
            return 0
        }

        // Check global blacklist
        if (!options.globalCanInsert && options.localFilters.isEmpty()) {
            return 0
        }

        val destinations = mutableListOf<TransferDest<C>>()
        // 1. Analyze tanks to add
        for (conn in connections) {
            val destCap = getCapability(level, blockPos = conn.pos, direction = conn.direction, CacheMode.ONLY_CACHE) ?: continue
            val tanks = getTanks(destCap)
            for (i in 0 until tanks) {
                val tankMaterial = getTankMaterial(destCap, tankIndex = i)
                // 1. Check can fill amount
                val leftAmount = limit(getTankMaxAmount(destCap, tankIndex = i)) - limit(getMaterialAmount(tankMaterial))
                if (leftAmount <= 0) {
                    continue
                }
                // 2. Check blacklisted
                val canInsertLocal = canInsert(connection = conn, material, filters = options.localFilters)
                if (canInsertLocal == null) {
                    if (!options.globalCanInsert) {
                        continue
                    }
                } else {
                    if (canInsertLocal != !options.invertFilter) {
                        // Blacklisted
                        continue
                    }
                }
                // 3. Check insertable
                val insertable = canInsert(destCap, tankIndex = i, makeMaterial(material, 1))
                if (!insertable) {
                    continue
                }
                // 4. Check same material in tank
                val merge = canMerge(tankMaterial, material)
                if (!merge) {
                    continue
                }
                // 5. Simulate insert amounts
                val maxInsert = min(maxTransfer, leftAmount)
                val insertAmount = insertMaterial(destCap, tankIndex = i, makeMaterial(material, amount = maxInsert), true)
                if (insertAmount > 0) {
                    destinations.add(TransferDest(destCap, tankIndex = i, insertAmount))
                    break
                }
            }
        }
        // 2. Flatten (Round-Robin)
        var materialToTransfer = 0L
        var leftTransfer = maxTransfer
        if (destinations.isEmpty()) {
            return 0
        }

        // Check materials are too few to fill "equality" - fallback to round-robin
        val destNearest = destinations.toList()
        destinations.sortBy { it.amount }
        if (maxTransfer <= destinations.first().amount * destinations.size) {
            var totalTransfer = 0L
            val minFill = maxTransfer / destinations.size
            var leftover = maxTransfer - (minFill * destinations.size)
            val p = tileEntity.getRoundRobinIndex(side, this) % destNearest.size
            var afterP = p
            for (i in p until (p + destNearest.size)) {
                val dest = destNearest[i % destNearest.size]
                if (leftover >= 1) {
                    totalTransfer += insertMaterial(dest.capability, dest.tankIndex, makeMaterial(material, minFill + 1), simulate)
                    leftover -= 1
                    afterP = (afterP + 1) % destNearest.size
                } else if (minFill >= 1) {
                    totalTransfer += insertMaterial(dest.capability, dest.tankIndex, makeMaterial(material, minFill), simulate)
                } else {
                    break
                }
            }
            if (!simulate) {
                tileEntity.setRoundRobinIndex(side, this, afterP)
            }
            return totalTransfer
        }

        for (i in destinations.indices) {
            val dest = destinations[i]
            val acceptableAmount = dest.amount
            val maxAmount = max(maxTransfer - materialToTransfer, 0) / (destinations.size - i)
            if (acceptableAmount >= maxAmount) {
                // Fill equality
                for (k in i until destinations.size) {
                    destinations[k].amount = maxAmount
                    materialToTransfer += maxAmount
                }
                break
            } else {
                // Fulfill and go next
                materialToTransfer += acceptableAmount
            }
        }
        if (simulate) {
            return materialToTransfer
        }
        // 3. Put
        var totalInserted = 0L
        for (dest in destinations) {
            val insertedAmount = insertMaterial(
                dest.capability, dest.tankIndex, makeMaterial(material, dest.amount), simulate = false)
            if (insertedAmount < dest.amount) {
                Main.LOGGER.error("Failed to insert predicted amount! Lost: ${dest.amount - insertedAmount}")
            }
            totalInserted += insertedAmount
        }
        return totalInserted
    }

    protected open fun transferMaterialOrdered(
        level: Level,
        connections: List<Connection>,
        material: M,
        options: TransferOptions<T>,
    ): Long {
        val maxTransfer = min(limit(getMaterialAmount(material)), getRate(options.upgrade).toLong())
        if (maxTransfer <= 0) {
            return 0
        }
        // Check global blacklist
        if ((!options.globalCanInsert) && options.localFilters.isEmpty()) {
            return 0
        }

        var materialToTransfer = maxTransfer
        for (i in connections.indices) {
            // 1. get connection (ordered)
            val conn = connections[if (options.nearestFirst) {
                i
            } else {
                connections.size - i - 1
            }]
            // 2. get tank
            val destCap = getCapability(
                level,
                blockPos = conn.pos,
                direction = conn.direction,
                cacheMode = CacheMode.ONLY_CACHE,
            ) ?: continue
            val tanks = getTanks(destCap)
            for (t in 0 until tanks) {
                val tankMaterial = getTankMaterial(destCap, tankIndex = t)
                // 1. Check can fill amount
                val leftAmount = limit(getTankMaxAmount(destCap, tankIndex = t)) - getMaterialAmount(tankMaterial)
                if (leftAmount <= 0) {
                    continue
                }
                // 2. Check blacklisted
                val canInsertLocal = canInsert(connection = conn, material, filters = options.localFilters)
                if (canInsertLocal == null) {
                    if (!options.globalCanInsert) {
                        continue
                    }
                } else {
                    if (canInsertLocal != !options.invertFilter) {
                        // Blacklisted
                        continue
                    }
                }
                // 3. Check insertable
                val insertable = canInsert(destCap, tankIndex = t, makeMaterial(material, 1))
                if (!insertable) {
                    continue
                }
                // 4. Check same material in tank
                val merge = canMerge(tankMaterial, material)
                if (!merge) {
                    continue
                }
                // 5. Simulate insert amounts
                val maxInsert = min(materialToTransfer, leftAmount)
                val insertAmount = insertMaterial(destCap, tankIndex = t, makeMaterial(material, amount = maxInsert), true)
                if (insertAmount <= 0) {
                    break
                }
                materialToTransfer -= if (!options.simulate) {
                    insertMaterial(destCap, tankIndex = t, makeMaterial(material, amount = insertAmount), false)
                } else {
                    insertAmount
                }
                break
            }
            if (materialToTransfer <= 0) {
                break
            }
        }
        return max(0, maxTransfer - materialToTransfer)
    }

    /**
     * Can Insert via filter?
     */
    protected open fun canInsert(connection: Connection?, material:M, filters:List<Filter<T>>):Boolean? {
        val materialTag = getTag(material)
        var haveWhitelist = false
        for (filter in filters) {
            val dest = filter.destination
            if (filter.isInvert) {
                if ((connection == null && dest == null) ||
                    (connection != null && matchesConnection(connection, filter))) {
                    if (isMatching(filter, material, materialTag)) {
                        return false
                    }
                }
            } else {
                haveWhitelist = true
                if ((connection == null && dest == null) ||
                    (connection != null && matchesConnection(connection, filter))) {
                    if (isMatching(filter, material, materialTag)) {
                        return true
                    }
                }
            }
        }
        return if (haveWhitelist) {
            false
        } else {
            null
        }
    }

    protected open fun isMatching(filter:Filter<T>, stack:M, tag:CompoundTag?): Boolean {
        val metadata = filter.metadata
        val simpleMatch = filter.tag?.contains(getType(stack)) ?: true

        return if (tag == null || metadata == null) {
            // Simple compare
            simpleMatch
        } else if (filter.isExactMetadata) {
            simpleMatch && deepExactCompare(metadata, tag)
        } else {
            simpleMatch && deepFuzzyCompare(metadata, tag)
        }
    }

    /**
     * Determine Capability of side
     */
    protected abstract fun getCapability(level:Level, blockPos: BlockPos, direction: Direction, cacheMode: CacheMode): C?

    /**
     * Get tanks size
     */
    protected abstract fun getTanks(capability: C): Int

    /**
     * Get tank's max available amounts
     */
    protected abstract fun getTankMaxAmount(capability: C, tankIndex: Int): Long

    protected abstract fun getTankMaterial(capability: C, tankIndex: Int): M

    protected abstract fun canExtract(capability: C, tankIndex: Int): Boolean

    /**
     * Can Insert of "capability"?
     */
    protected abstract fun canInsert(capability: C, tankIndex: Int, material: M): Boolean

    protected abstract fun extractMaterial(capability: C, tankIndex:Int, amount: Long, simulate: Boolean): M

    protected abstract fun getMaterialAmount(material: M): Long

    protected abstract fun makeMaterial(material: M, amount: Long): M

    protected abstract fun insertMaterial(capability: C, tankIndex: Int, material: M, simulate: Boolean): Long

    protected abstract fun canMerge(materialA: M, materialB: M): Boolean

    /**
     * Get type of material (for comparing tag!)
     */
    protected abstract fun getType(material: M): T

    /**
     * Get tag of material (nullable)
     */
    protected abstract fun getTag(material: M): CompoundTag?

    protected open fun getSpeed(upgrade:Upgrade): Int {
        return 1
    }

    /**
     * Round hard-limit
     */
    protected open fun limit(mount: Long): Long {
        return if (mount > hardLimit) {
            hardLimit
        } else if (mount < 0) {
            0
        } else {
            mount
        }
    }
}

private data class TransferDest<C>(
    val capability: C,
    val tankIndex: Int,
    var amount: Long,
)