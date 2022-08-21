package de.maxhenkel.pipez.blocks.tileentity.types

import de.maxhenkel.pipez.Filter
import de.maxhenkel.pipez.Main
import de.maxhenkel.pipez.blocks.ModBlocks
import de.maxhenkel.pipez.types.*
import de.maxhenkel.pipez.connections.CapabilityCache
import de.maxhenkel.pipez.connections.CapabilityCache.Companion.asValue
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TranslatableComponent
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraftforge.energy.IEnergyStorage

class EnergyPipeType : AbsTankPipeType<Unit, IEnergyStorage, EnergyMaterial>() {

    override val hardLimit: Long = Int.MAX_VALUE.toLong()

    companion object {
        val INSTANCE = EnergyPipeType()
    }

    override fun getKey(): String {
        return "Energy"
    }

    override fun getCapability(
        level: Level,
        blockPos: BlockPos,
        direction: Direction,
        cacheMode: CacheMode
    ): IEnergyStorage? {
        return CapabilityCache.INSTANCE.getEnergyCapability(
            level,
            blockPos,
            direction,
            cacheMode,
        ).asValue()
    }

    override fun getTag(material: EnergyMaterial): CompoundTag? {
        return null
    }

    override fun getType(material: EnergyMaterial) {
        return // Unit
    }

    override fun canInsert(
        connection: Connection?,
        material: EnergyMaterial,
        filters: List<Filter<Unit>>
    ): Boolean {
        return true
    }

    override fun canInsert(capability: IEnergyStorage, tankIndex: Int, material: EnergyMaterial): Boolean {
        return capability.canReceive()
    }

    override fun canExtract(capability: IEnergyStorage, tankIndex: Int): Boolean {
        return capability.canExtract()
    }

    override fun getTankMaterial(capability: IEnergyStorage, tankIndex: Int): EnergyMaterial {
        return EnergyMaterial(capability.energyStored)
    }

    override fun getTankMaxAmount(capability: IEnergyStorage, tankIndex: Int): Long {
        return capability.maxEnergyStored.toLong()
    }

    override fun getTanks(capability: IEnergyStorage): Int {
        return 1
    }

    override fun canInsert(tileEntity: BlockEntity, direction: Direction): Boolean {
        val level = tileEntity.level ?: return false
        return CapabilityCache.INSTANCE.getEnergyCapability(
            level,
            tileEntity.blockPos,
            direction,
            cacheMode = CacheMode.PREFER_CACHE,
        ).isPresent
    }

    override fun canMerge(materialA: EnergyMaterial, materialB: EnergyMaterial): Boolean {
        return true
    }

    override fun insertMaterial(
        capability: IEnergyStorage,
        tankIndex: Int,
        material: EnergyMaterial,
        simulate: Boolean
    ): Long {
        return capability.receiveEnergy(material.forgeEnergy, simulate).toLong()
    }

    override fun makeMaterial(material: EnergyMaterial, amount: Long): EnergyMaterial {
        return EnergyMaterial(limit(amount).toInt())
    }

    override fun getMaterialAmount(material: EnergyMaterial): Long {
        return material.forgeEnergy.toLong()
    }

    override fun extractMaterial(
        capability: IEnergyStorage,
        tankIndex: Int,
        amount: Long,
        simulate: Boolean
    ): EnergyMaterial {
        return EnergyMaterial(
            capability.extractEnergy(limit(amount).toInt(), simulate)
        )
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

    override fun getRate(upgrade: Upgrade): Int {
        return Main.SERVER_CONFIG.getPipeValue(CapabilityType.ENERGY, upgrade)
    }

}