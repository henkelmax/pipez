package de.maxhenkel.pipez.blocks.tileentity.types

import de.maxhenkel.pipez.Filter
import de.maxhenkel.pipez.Main
import de.maxhenkel.pipez.blocks.ModBlocks
import de.maxhenkel.pipez.blocks.tileentity.PipeLogicTileEntity
import de.maxhenkel.pipez.blocks.tileentity.PipeTileEntity
import de.maxhenkel.pipez.blocks.tileentity.UpgradeTileEntity
import de.maxhenkel.pipez.types.CapabilityType
import de.maxhenkel.pipez.types.Translates
import de.maxhenkel.pipez.types.Upgrade
import de.maxhenkel.pipez.utils.CapabilityCache
import de.maxhenkel.pipez.utils.CapabilityCache.Companion.asValue
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TranslatableComponent
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraftforge.energy.CapabilityEnergy
import net.minecraftforge.energy.IEnergyStorage
import kotlin.math.min

class EnergyPipeType : PipeType<Unit>() {
    companion object {
        val INSTANCE = EnergyPipeType()
    }

    override fun getKey(): String {
        return "Energy"
    }

    override fun canInsert(tileEntity: BlockEntity, direction: Direction?): Boolean {
        return tileEntity.getCapability(CapabilityEnergy.ENERGY, direction).isPresent
    }

    override fun hasFilter(): Boolean {
        return false
    }

    override fun createFilter(): Filter<Unit> {
        return object : Filter<Unit>() {}
    }

    override fun getTranslationKey(): String {
        return Translates.ToolTip.Energy
    }

    override fun getIcon(): ItemStack {
        return ItemStack(ModBlocks.ENERGY_PIPE)
    }

    override fun getTransferText(upgrade: Upgrade): Component {
        return TranslatableComponent(Translates.ToolTip.Rate.Energy, getRate(upgrade))
    }

    override fun tick(tileEntity: PipeLogicTileEntity?) {
    }

    override fun getRate(upgrade: Upgrade): Int {
        return Main.SERVER_CONFIG.getPipeValue(CapabilityType.ENERGY, upgrade)
    }

    fun pullEnergy(tileEntity:PipeLogicTileEntity, side: Direction) {
        val level = tileEntity.level ?: return
        if (!tileEntity.isExtracting(side)) {
            return
        }
        if (!tileEntity.shouldWork(side, this)) {
            return
        }
        val energyStorage = CapabilityCache.INSTANCE.getEnergyCapability(
            level,
            blockPos = tileEntity.blockPos.relative(side),
            direction = side.opposite,
            onlyCache = true,
        ).asValue()
        if (energyStorage == null || !energyStorage.canExtract()) {
            return
        }
        if (energyStorage.energyStored <= 0) {
            return
        }
        val connections:MutableList<PipeTileEntity.Connection> = tileEntity.connections
        if (connections.isEmpty()) {
            return
        }
        val distribution = tileEntity.getDistribution(side, this) ?: UpgradeTileEntity.Distribution.NEAREST

        when (distribution) {
            UpgradeTileEntity.Distribution.ROUND_ROBIN ->
                insertEquality(tileEntity, side, connections, energyStorage)
            UpgradeTileEntity.Distribution.RANDOM -> {
                connections.shuffle()
                insertOrdered(tileEntity, side, connections.toMutableList().apply {
                    shuffle()
                }, energyStorage) // Random = Orderless
            }
            UpgradeTileEntity.Distribution.NEAREST ->
                insertOrdered(tileEntity, side, connections, energyStorage, nearFirst = true)
            UpgradeTileEntity.Distribution.FURTHEST ->
                insertOrdered(tileEntity, side, connections, energyStorage, nearFirst = false)
        }
    }

    private fun insertEquality(tileEntity: PipeLogicTileEntity, side: Direction,
                               connections: List<PipeTileEntity.Connection>, energyStorage: IEnergyStorage) {
        var totalAmount = min(energyStorage.maxEnergyStored - energyStorage.energyStored, getRate(tileEntity, side))
        var energyToTransfer = 0
        if (totalAmount <= 0) {
            return
        }

        val destinations = arrayListOf<Pair<Int, IEnergyStorage>>()
        for (conn in connections) {
            val destination = CapabilityCache.INSTANCE.getEnergyCapability(
                level = tileEntity.level!!, // Checked before called
                blockPos = conn.pos,
                direction = conn.direction,
                onlyCache = true,
            ).asValue() ?: continue
            if (!destination.canReceive()) {
                continue
            }
            val maxReceiveAmount = destination.maxEnergyStored - destination.energyStored
            if (maxReceiveAmount <= 0) {
                continue
            }
            val receiveAmount = destination.receiveEnergy(maxReceiveAmount, true)
            if (receiveAmount <= 0) {
                continue
            }
            destinations.add(Pair(receiveAmount, destination))
        }
        // Simulate max extract
        val maxExtract = energyStorage.extractEnergy(totalAmount, true)
        if (maxExtract <= 0) {
            return
        }
        if (maxExtract < totalAmount) {
            totalAmount = maxExtract
        }
        // Separate
        destinations.sortBy { it.first }
        for (i in 0 until destinations.size) {
            val dest = destinations[i]
            val acceptableAmount = dest.first
            val maxAmount = (totalAmount - energyToTransfer) / (destinations.size - i)
            if (acceptableAmount >= maxAmount) {
                // Fill equality
                for (k in i until destinations.size) {
                    destinations[k] = destinations[k].copy(first = maxAmount)
                    energyToTransfer += maxAmount
                }
                break
            } else {
                // Fulfill and go next
                energyToTransfer += acceptableAmount
            }
        }
        // Extract
        var extractedAmount = energyStorage.extractEnergy(totalAmount, false)
        if (extractedAmount < totalAmount) {
            Main.LOGGER.error("Can't Extract predicted amount! Amount: $totalAmount FE, Extracted: $extractedAmount FE!! Force-off round robin!")
        }
        // Put
        for (dest in destinations) {
            val insertedAmount = dest.second.receiveEnergy(dest.first, false)
            if (insertedAmount < dest.first) {
                Main.LOGGER.error("Can't insert predicted amount! Amount: ${dest.first} FE, Inserted: $insertedAmount FE!!")
            }
            extractedAmount -= insertedAmount
        }
        // Check leftover
        if (extractedAmount > 0) {
            Main.LOGGER.error("Energy $extractedAmount FE left compared to predicted amount!")
            energyStorage.receiveEnergy(extractedAmount, false)
        }
    }

    private fun insertOrdered(tileEntity: PipeLogicTileEntity, side: Direction,
                              connections: List<PipeTileEntity.Connection>, energyStorage: IEnergyStorage,
                              nearFirst:Boolean = true) {
        var totalAmount = min(energyStorage.maxEnergyStored - energyStorage.energyStored, getRate(tileEntity, side))
        if (totalAmount <= 0) {
            // Empty;
            return
        }
        totalAmount = min(totalAmount, energyStorage.extractEnergy(totalAmount, true))
        if (totalAmount <= 0) {
            // also empty...
            return
        }
        var energyToTransfer = totalAmount
        for (i in connections.indices) {
            val conn = connections[if (nearFirst) {
                i
            } else {
                connections.size - i - 1
            }]
            val destination = CapabilityCache.INSTANCE.getEnergyCapability(
                level = tileEntity.level!!, // Checked before called
                blockPos = conn.pos,
                direction = conn.direction,
                onlyCache = true,
            ).asValue() ?: continue
            val availableSize = min(energyToTransfer, destination.maxEnergyStored - destination.energyStored)
            if (availableSize <= 0) {
                continue
            }
            // Put
            val transferAmount = destination.receiveEnergy(availableSize, false)
            energyToTransfer -= transferAmount
            if (energyToTransfer <= 0) {
                break
            }
        }
        val extractAmount = totalAmount - energyToTransfer
        val extractedEnergy = energyStorage.extractEnergy(extractAmount, false)
        if (extractedEnergy < extractAmount) {
            Main.LOGGER.error("Predicted energy and consumed energy is different! Predict: $extractAmount FE, Consumed: $extractedEnergy")
        }
    }

    fun receive(tileEntity:PipeLogicTileEntity, side: Direction, amount: Int, simulate: Boolean): Int {
        if (!tileEntity.isExtracting(side)) {
            return 0
        }
        if (!tileEntity.shouldWork(side, this)) {
            return 0
        }

        val connections = tileEntity.connections
        val maxTransfer = min(getRate(tileEntity, side), amount)
        if (maxTransfer <= 0) {
            return 0
        }

        return when (
            tileEntity.getDistribution(side, this) ?: UpgradeTileEntity.Distribution.NEAREST
        ) {
            UpgradeTileEntity.Distribution.ROUND_ROBIN ->
                receiveEquality(tileEntity, side, connections, maxTransfer, simulate = simulate)
            UpgradeTileEntity.Distribution.RANDOM -> receiveOrdered(tileEntity, side, connections.toMutableList().apply {
                shuffle()
            }, maxTransfer, simulate = simulate) // Random = Orderless
            UpgradeTileEntity.Distribution.NEAREST ->
                receiveOrdered(tileEntity, side, connections, maxTransfer, simulate = simulate, nearFirst = true)
            UpgradeTileEntity.Distribution.FURTHEST ->
                receiveOrdered(tileEntity, side, connections, maxTransfer, simulate = simulate, nearFirst = false)
        }
    }

    private fun receiveEquality(
        tileEntity: PipeLogicTileEntity, side: Direction,
        connections: List<PipeTileEntity.Connection>, maxTransfer: Int,
        simulate: Boolean
    ): Int {
        var energyToTransfer = 0

        val destinations = arrayListOf<Pair<Int, IEnergyStorage>>()
        for (conn in connections) {
            val destination = CapabilityCache.INSTANCE.getEnergyCapability(
                level = tileEntity.level!!, // Checked before called
                blockPos = conn.pos,
                direction = conn.direction,
                onlyCache = true,
            ).asValue() ?: continue
            if (!destination.canReceive()) {
                continue
            }
            val maxReceiveAmount = destination.maxEnergyStored - destination.energyStored
            if (maxReceiveAmount <= 0) {
                continue
            }
            val receiveAmount = destination.receiveEnergy(maxReceiveAmount, true)
            if (receiveAmount <= 0) {
                continue
            }
            destinations.add(Pair(receiveAmount, destination))
        }
        // Separate
        destinations.sortBy { it.first }
        for (i in 0 until destinations.size) {
            val dest = destinations[i]
            val acceptableAmount = dest.first
            val maxAmount = (maxTransfer - energyToTransfer) / (destinations.size - i)
            if (acceptableAmount >= maxAmount) {
                // Fill equality
                for (k in i until destinations.size) {
                    destinations[k] = destinations[k].copy(first = maxAmount)
                    energyToTransfer += maxAmount
                }
                break
            } else {
                // Fulfill and go next
                energyToTransfer += acceptableAmount
            }
        }
        // Extract
        var extractedAmount = maxTransfer
        // Put
        for (dest in destinations) {
            if (Math.random() < 0.2) {
                Main.LOGGER.debug("Send Energy: ${dest.first}, simulate: $simulate")
            }
            val insertedAmount = dest.second.receiveEnergy(dest.first, simulate)
            if (insertedAmount < dest.first) {
                Main.LOGGER.error("Can't insert predicted amount! Amount: ${dest.first} FE, Inserted: $insertedAmount FE!!")
            }
            extractedAmount -= insertedAmount
        }
        // Check leftover
        return maxTransfer - extractedAmount
    }

    private fun receiveOrdered(
        tileEntity: PipeLogicTileEntity, side: Direction,
        connections: List<PipeTileEntity.Connection>, maxTransfer: Int,
        simulate: Boolean, nearFirst: Boolean = true
    ): Int {
        if (maxTransfer <= 0) {
            // Empty;
            return 0
        }
        var energyToTransfer = maxTransfer
        for (i in connections.indices) {
            val conn = connections[if (nearFirst) {
                i
            } else {
                connections.size - i - 1
            }]
            val destination = CapabilityCache.INSTANCE.getEnergyCapability(
                level = tileEntity.level!!, // Checked before called
                blockPos = conn.pos,
                direction = conn.direction,
                onlyCache = true,
            ).asValue() ?: continue
            val availableSize = min(energyToTransfer, destination.maxEnergyStored - destination.energyStored)
            if (availableSize <= 0) {
                continue
            }
            // Put
            val transferAmount = destination.receiveEnergy(availableSize, simulate)
            energyToTransfer -= transferAmount
            if (energyToTransfer <= 0) {
                break
            }
        }
        return maxTransfer - energyToTransfer
    }
}