package de.maxhenkel.pipez.blocks.tileentity

import de.maxhenkel.pipez.Main
import de.maxhenkel.pipez.blocks.tileentity.storage.PipeEnergyStorage
import de.maxhenkel.pipez.blocks.tileentity.storage.PipeFluidStorage
import de.maxhenkel.pipez.blocks.tileentity.storage.PipeGasStorage
import de.maxhenkel.pipez.blocks.tileentity.storage.PipeItemStorage
import de.maxhenkel.pipez.blocks.tileentity.types.*
import de.maxhenkel.pipez.connections.ModCapabilities
import de.maxhenkel.pipez.types.PipeSide
import de.maxhenkel.pipez.utils.DirectionalLazyOptionalCache
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.Fluid
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.energy.CapabilityEnergy
import net.minecraftforge.fluids.capability.CapabilityFluidHandler
import net.minecraftforge.items.CapabilityItemHandler

abstract class PipeLogicTileEntity(
    private val tileEntityTypeIn:BlockEntityType<out BlockEntity>,
    protected val types:Array<PipeType<out Any>>,
    pos: BlockPos,
    state: BlockState
) : UpgradeTileEntity(tileEntityTypeIn, pos, state) {

    protected val rrIndex:IntArray = IntArray(PipeSide.values().size * types.size)

    protected val gasCache = DirectionalLazyOptionalCache<PipeGasStorage>()
    protected val energyCache = DirectionalLazyOptionalCache<PipeEnergyStorage>()
    protected val fluidCache = DirectionalLazyOptionalCache<PipeFluidStorage>()
    protected val itemCache = DirectionalLazyOptionalCache<PipeItemStorage>()

    private var tickCount = 200L
    var lastSent = 0L
    var lastSimulated = false
    private var recursionDepth = 0

    override fun <T : Any?> getCapability(cap: Capability<T>, side: Direction?): LazyOptional<T> {
        if (side == null) {
            return LazyOptional.empty()
        }
        val direction = PipeSide.fromDirection(side)
        return getCapability(cap, direction)
    }

    fun <T : Any?> getCapability(cap: Capability<T>, side: PipeSide): LazyOptional<T> {
        when (cap) {
            CapabilityItemHandler.ITEM_HANDLER_CAPABILITY -> {
                if (hasType(ItemPipeType.INSTANCE)) {
                    return itemCache[side].cast()
                }
            }
            CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY -> {
                if (hasType(FluidPipeType.INSTANCE)) {
                    return fluidCache[side].cast()
                }
            }
            CapabilityEnergy.ENERGY -> {
                if (hasType(EnergyPipeType.INSTANCE)) {
                    return energyCache[side].cast()
                }
            }
            ModCapabilities.GAS_HANDLER_CAPABILITY -> {
                if (hasType(GasPipeType.INSTANCE)) {
                    return gasCache[side].cast()
                }
            }
        }

        return super.getCapability(cap, side.toDirection())
    }

    private fun <T> hasType(type: PipeType<T>):Boolean {
        return types.any { it == type }
    }

    private fun <T> PipeType<T>.getIndex():Int {
        for (i in types.indices) {
            val type = types[i]
            if (type == this) {
                return i
            }
        }
        return 0
    }

    fun <T> getRoundRobinIndex(pipeSide: PipeSide, pipeType: PipeType<T>): Int {
        return rrIndex[pipeSide.index + pipeType.getIndex() * types.size]
    }

    fun <T> setRoundRobinIndex(pipeSide: PipeSide, pipeType: PipeType<T>, value: Int) {
        rrIndex[pipeSide.index + pipeType.getIndex() * types.size] = value
    }

    fun <T> isEnabled(pipeSide: PipeSide, pipeType: PipeType<T>): Boolean {
        val redstoneMode = getRedstoneMode(pipeSide.toDirection() ?: return true, pipeType)
        return redstoneMode != RedstoneMode.ALWAYS_OFF
    }

    fun <T> isEnabled(direction: Direction, pipeType: PipeType<T>): Boolean {
        val pipeSide = PipeSide.fromDirection(direction)
        val redstoneMode = getRedstoneMode(pipeSide.toDirection() ?: return true, pipeType)
        return redstoneMode != RedstoneMode.ALWAYS_OFF
    }

    fun getPreferredPipeIndex(side: PipeSide): Int {
        for (i in types.indices) {
            if (isEnabled(side, types[i])) {
                return i
            }
        }
        return 0
    }

    fun getPreferredPipeIndex(direction: Direction): Int {
        return getPreferredPipeIndex(PipeSide.fromDirection(direction))
    }

    fun getPipeTypes():Array<PipeType<out Any>> {
        return types
    }


    fun <T> shouldWork(direction: Direction, pipeType: PipeType<T>): Boolean {
        return shouldWork(PipeSide.fromDirection(direction), pipeType)
    }

    fun <T> shouldWork(pipeSide: PipeSide, pipeType: PipeType<T>): Boolean {
        val side = pipeSide.toDirection() ?: return true
        return when (getRedstoneMode(side, pipeType)) {
            RedstoneMode.ALWAYS_OFF -> false
            RedstoneMode.OFF_WHEN_POWERED -> !isRedstonePowered()
            RedstoneMode.ON_WHEN_POWERED -> isRedstonePowered()
            RedstoneMode.IGNORED -> true
            else -> true
        }
    }

    fun isRedstonePowered(): Boolean {
        return level?.hasNeighborSignal(worldPosition) ?: false
    }

    fun getTickCount(): Long {
        return tickCount
    }

    override fun tick() {
        super.tick()

        if (level?.isClientSide != false) {
            return
        }
        tickCount += 1

        val partTime = arrayListOf<Long>()
        val startTime = System.nanoTime()
        for (direction in Direction.values()) {
            if (!isExtracting(direction)) {
                continue
            }
            val side = PipeSide.fromDirection(direction)
            for (type in types) {
                val pTime = System.nanoTime()
                when (type) {
                    ItemPipeType.INSTANCE -> itemCache[side].ifPresent {
                        it.tick()
                    }
                    FluidPipeType.INSTANCE -> fluidCache[side].ifPresent {
                        it.tick()
                    }
                    EnergyPipeType.INSTANCE -> energyCache[side].ifPresent {
                        it.tick()
                    }
                    GasPipeType.INSTANCE -> gasCache[side].ifPresent {
                        it.tick()
                    }
                }
                partTime.add(System.nanoTime() - pTime)
            }
        }

        if (tickCount % 20 == 0L) {
            val currentTime = System.nanoTime()
            Main.logDebug("Consumed times: ${partTime.joinToString(separator = " ")} / ${(currentTime - startTime) / 1000L} (ys/t)")
        }
    }

    override fun setExtracting(side: Direction, extracting: Boolean) {
        super.setExtracting(side, extracting)

        for (type in types) {
            val pSide = PipeSide.fromDirection(side)
            when (type) {
                ItemPipeType.INSTANCE -> itemCache.revalidate(pSide,
                    {_ -> extracting}, {s -> PipeItemStorage(this, s.toDirection()) })
                FluidPipeType.INSTANCE -> fluidCache.revalidate(pSide,
                    {_ -> extracting}, {s -> PipeFluidStorage(this, s.toDirection()) })
                EnergyPipeType.INSTANCE -> energyCache.revalidate(pSide,
                    {_ -> extracting}, {s -> PipeEnergyStorage(this, s.toDirection()) })
                GasPipeType.INSTANCE -> gasCache.revalidate(pSide,
                    {_ -> extracting}, {s -> PipeGasStorage(this, s.toDirection()) })
            }
        }
    }

    override fun load(compoundTag: CompoundTag) {
        super.load(compoundTag)

        for (type in types) {
            when (type) {
                ItemPipeType.INSTANCE -> itemCache.revalidate(
                    {isExtracting}, {s -> PipeItemStorage(this, s.toDirection()) })
                FluidPipeType.INSTANCE -> fluidCache.revalidate(
                    {isExtracting}, {s -> PipeFluidStorage(this, s.toDirection()) })
                EnergyPipeType.INSTANCE -> energyCache.revalidate(
                    {isExtracting}, {s -> PipeEnergyStorage(this, s.toDirection()) })
                GasPipeType.INSTANCE -> gasCache.revalidate(
                    {isExtracting}, {s -> PipeGasStorage(this, s.toDirection()) })
            }
        }
    }

    override fun setRemoved() {
        itemCache.invalidate()
        fluidCache.invalidate()
        energyCache.invalidate()
        gasCache.invalidate()
        super.setRemoved()
    }

    override fun canInsert(tileEntity: BlockEntity, direction: Direction): Boolean {
        return types.any { it.canInsert(tileEntity, direction) }
    }

    fun pushRecursion():Boolean {
        if (recursionDepth >= 1) {
            return true
        }
        recursionDepth += 1
        return false
    }

    fun popRecursion() {
        recursionDepth -= 1
    }

}