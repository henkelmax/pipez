package de.maxhenkel.pipez.utils

import de.maxhenkel.pipez.Main
import de.maxhenkel.pipez.capabilities.ModCapabilities
import de.maxhenkel.pipez.types.AbsoluteDirection
import mekanism.api.chemical.gas.IGasHandler
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.energy.CapabilityEnergy
import net.minecraftforge.energy.IEnergyStorage
import net.minecraftforge.fluids.capability.CapabilityFluidHandler
import net.minecraftforge.fluids.capability.IFluidHandler
import net.minecraftforge.items.CapabilityItemHandler
import net.minecraftforge.items.IItemHandler

class CapabilityCache {
    companion object {
        val INSTANCE = CapabilityCache()

        fun <T> LazyOptional<T>.asValue(): T? {
            if (!this.isPresent) {
                return null
            }
            return this.resolve().let {
                if (it.isPresent) {
                    it.get()
                } else {
                    null
                }
            }
        }
    }

    private val itemMap = hashMapOf<AbsoluteDirection, LazyOptional<IItemHandler>>()
    private val fluidMap = hashMapOf<AbsoluteDirection, LazyOptional<IFluidHandler>>()
    private val energyMap = hashMapOf<AbsoluteDirection, LazyOptional<IEnergyStorage>>()
    private val gasMap = hashMapOf<AbsoluteDirection, LazyOptional<IGasHandler>>()

    private fun <T> getCapability(cacheMap: HashMap<AbsoluteDirection, LazyOptional<T>>,
                               level: Level, direction: AbsoluteDirection, cap: Capability<T>, onlyCache:Boolean = false): LazyOptional<T> {
        val cacheDirection = cacheMap[direction]
        if (cacheDirection?.isPresent == true) {
            return cacheDirection
        }
        if (onlyCache) {
            return LazyOptional.empty()
        }
        val blockEntity = level.getBlockEntity(direction.position.toBlockPos()) ?: return LazyOptional.empty()
        val capability = blockEntity.getCapability(cap, direction.direction)
        capability.addListener {
            cacheMap.remove(direction)
        }
        cacheMap[direction] = capability
        return capability
    }

    fun getItemCapability(level: Level, blockPos: BlockPos, direction: Direction, onlyCache: Boolean = false): LazyOptional<IItemHandler> {
        return getCapability(
            itemMap, level, AbsoluteDirection.from(level, blockPos, direction),
            cap = CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
            onlyCache = onlyCache,
        )
    }

    fun getFluidCapability(level: Level, blockPos: BlockPos, direction: Direction, onlyCache: Boolean = false): LazyOptional<IFluidHandler> {
        return getCapability(
            fluidMap, level, AbsoluteDirection.from(level, blockPos, direction),
            cap = CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,
            onlyCache = onlyCache,
        )
    }

    fun getEnergyCapability(level: Level, blockPos: BlockPos, direction: Direction, onlyCache: Boolean = false): LazyOptional<IEnergyStorage> {
        val output = getCapability(
            energyMap, level, AbsoluteDirection.from(level, blockPos, direction),
            cap = CapabilityEnergy.ENERGY,
            onlyCache = onlyCache,
        )
        return output
    }

    fun getGasCapability(level: Level, blockPos: BlockPos, direction: Direction, onlyCache: Boolean = false): LazyOptional<IGasHandler> {
        return getCapability(
            gasMap, level, AbsoluteDirection.from(level, blockPos, direction),
            cap = ModCapabilities.GAS_HANDLER_CAPABILITY,
            onlyCache = onlyCache,
        )
    }
}