package de.maxhenkel.pipez.blocks.tileentity.types

import de.maxhenkel.pipez.Filter
import de.maxhenkel.pipez.GasFilter
import de.maxhenkel.pipez.Main
import de.maxhenkel.pipez.blocks.ModBlocks
import de.maxhenkel.pipez.types.CacheMode
import de.maxhenkel.pipez.types.CapabilityType
import de.maxhenkel.pipez.types.Translates
import de.maxhenkel.pipez.types.Upgrade
import de.maxhenkel.pipez.connections.CapabilityCache
import de.maxhenkel.pipez.connections.CapabilityCache.Companion.asValue
import mekanism.api.Action
import mekanism.api.chemical.gas.Gas
import mekanism.api.chemical.gas.GasStack
import mekanism.api.chemical.gas.IGasHandler
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TranslatableComponent
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity

class GasPipeType : AbsTankPipeType<Gas, IGasHandler, GasStack>() {

    override val hardLimit: Long = Long.MAX_VALUE

    companion object {
        val INSTANCE = GasPipeType()
    }

    override fun canInsert(capability: IGasHandler, tankIndex: Int, material: GasStack): Boolean {
        return capability.isValid(tankIndex, material)
    }

    override fun canInsert(tileEntity: BlockEntity, direction: Direction): Boolean {
        return CapabilityCache.INSTANCE.getGasCapability(
            level = tileEntity.level ?: return false,
            blockPos = tileEntity.blockPos,
            direction = direction,
            cacheMode = CacheMode.PREFER_CACHE,
        ).isPresent
    }

    override fun getCapability(
        level: Level,
        blockPos: BlockPos,
        direction: Direction,
        cacheMode: CacheMode
    ): IGasHandler? {
        return CapabilityCache.INSTANCE.getGasCapability(
            level = level,
            blockPos = blockPos,
            direction = direction,
            cacheMode = CacheMode.PREFER_CACHE,
        ).asValue()
    }

    override fun getKey(): String {
        return "Gas"
    }

    override fun getRate(upgrade: Upgrade): Int {
        return Main.SERVER_CONFIG.getPipeValue(CapabilityType.GAS, upgrade)
    }

    override fun createFilter(): Filter<Gas> {
        return GasFilter()
    }

    override fun getTranslationKey(): String {
        return Translates.ToolTip.Gas
    }

    override fun getIcon(): ItemStack {
        return ItemStack(ModBlocks.GAS_PIPE)
    }

    override fun getTransferText(upgrade: Upgrade): Component {
        return TranslatableComponent(Translates.ToolTip.Rate.Gas, getRate(upgrade))
    }

    override fun getTag(material: GasStack): CompoundTag? {
        return null
    }

    override fun getType(material: GasStack): Gas {
        return material.type
    }

    override fun canMerge(materialA: GasStack, materialB: GasStack): Boolean {
        return materialA.isEmpty || materialB.isEmpty || materialA.isTypeEqual(materialB)
    }

    override fun insertMaterial(capability: IGasHandler, tankIndex: Int, material: GasStack, simulate: Boolean): Long {
        val result = capability.insertChemical(tankIndex, material, isSimulate(simulate))
        return material.amount - result.amount
    }

    override fun makeMaterial(material: GasStack, amount: Long): GasStack {
        return GasStack(material, amount)
    }

    override fun getMaterialAmount(material: GasStack): Long {
        if (material.isEmpty) {
            return 0L
        }
        return material.amount
    }

    override fun extractMaterial(capability: IGasHandler, tankIndex: Int, amount: Long, simulate: Boolean): GasStack {
        return capability.extractChemical(tankIndex, amount, isSimulate(simulate))
    }

    override fun canExtract(capability: IGasHandler, tankIndex: Int): Boolean {
        return true
    }

    override fun getTankMaterial(capability: IGasHandler, tankIndex: Int): GasStack {
        return capability.getChemicalInTank(tankIndex)
    }

    override fun getTankMaxAmount(capability: IGasHandler, tankIndex: Int): Long {
        return capability.getTankCapacity(tankIndex)
    }

    override fun getTanks(capability: IGasHandler): Int {
        return capability.tanks
    }

    private fun isSimulate(simulate: Boolean):Action {
        return if (simulate) {
            Action.SIMULATE
        } else {
            Action.EXECUTE
        }
    }
}