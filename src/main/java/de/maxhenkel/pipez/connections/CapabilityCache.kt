package de.maxhenkel.pipez.connections

import de.maxhenkel.pipez.Main
import de.maxhenkel.pipez.types.AbsoluteDirection
import de.maxhenkel.pipez.types.CacheMode
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

    fun clear() {
        itemMap.clear()
        fluidMap.clear()
        energyMap.clear()
        gasMap.clear()
    }

    private val itemMap = hashMapOf<AbsoluteDirection, LazyOptional<IItemHandler>>()
    private val fluidMap = hashMapOf<AbsoluteDirection, LazyOptional<IFluidHandler>>()
    private val energyMap = hashMapOf<AbsoluteDirection, LazyOptional<IEnergyStorage>>()
    private val gasMap = hashMapOf<AbsoluteDirection, LazyOptional<IGasHandler>>()

    private fun <T> getCapability(cacheMap: HashMap<AbsoluteDirection, LazyOptional<T>>,
                               level: Level, direction: AbsoluteDirection, cap: Capability<T>,
                                  cacheMode: CacheMode): LazyOptional<T> {
        if (cacheMode != CacheMode.REFRESH) {
            val cacheDirection = cacheMap[direction]
            if (cacheDirection?.isPresent == true) {
                return cacheDirection
            }
            if (cacheMode == CacheMode.ONLY_CACHE) {
                return LazyOptional.empty()
            }
        }
        val blockEntity = level.getBlockEntity(direction.position.toBlockPos()) ?: return LazyOptional.empty()
        val capability = blockEntity.getCapability(cap, direction.direction)
        capability.addListener {
            cacheMap.remove(direction)
        }
        cacheMap[direction] = capability
        return capability
    }

    fun getItemCapability(level: Level, blockPos: BlockPos, direction: Direction, cacheMode: CacheMode = CacheMode.PREFER_CACHE): LazyOptional<IItemHandler> {
        return getCapability(
            itemMap, level, AbsoluteDirection.from(level, blockPos, direction),
            cap = CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
            cacheMode,
        )
    }

    fun getFluidCapability(level: Level, blockPos: BlockPos, direction: Direction, cacheMode: CacheMode = CacheMode.PREFER_CACHE): LazyOptional<IFluidHandler> {
        return getCapability(
            fluidMap, level, AbsoluteDirection.from(level, blockPos, direction),
            cap = CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,
            cacheMode,
        )
    }

    fun getEnergyCapability(level: Level, blockPos: BlockPos, direction: Direction, cacheMode: CacheMode = CacheMode.PREFER_CACHE): LazyOptional<IEnergyStorage> {
        return getCapability(
            energyMap, level, AbsoluteDirection.from(level, blockPos, direction),
            cap = CapabilityEnergy.ENERGY,
            cacheMode,
        )
    }

    fun getGasCapability(level: Level, blockPos: BlockPos, direction: Direction, cacheMode: CacheMode = CacheMode.PREFER_CACHE): LazyOptional<IGasHandler> {
        return getCapability(
            gasMap, level, AbsoluteDirection.from(level, blockPos, direction),
            cap = ModCapabilities.GAS_HANDLER_CAPABILITY,
            cacheMode = cacheMode,
        )
    }
}