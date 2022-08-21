package de.maxhenkel.pipez.blocks.tileentity.types

import de.maxhenkel.pipez.Filter
import de.maxhenkel.pipez.ItemFilter
import de.maxhenkel.pipez.Main
import de.maxhenkel.pipez.blocks.ModBlocks
import de.maxhenkel.pipez.blocks.tileentity.UpgradeTileEntity
import de.maxhenkel.pipez.blocks.tileentity.UpgradeTileEntity.Distribution
import de.maxhenkel.pipez.types.*
import de.maxhenkel.pipez.connections.CapabilityCache
import de.maxhenkel.pipez.connections.CapabilityCache.Companion.asValue
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TranslatableComponent
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraftforge.items.IItemHandler

class ItemPipeType : AbsTankPipeType<Item, IItemHandler, ItemStack>() {
    override val hardLimit: Long = Int.MAX_VALUE.toLong()

    companion object {
        val INSTANCE = ItemPipeType()
    }

    override fun canInsert(tileEntity: BlockEntity, direction: Direction): Boolean {
        return CapabilityCache.INSTANCE.getItemCapability(
            level = tileEntity.level ?: return false,
            blockPos = tileEntity.blockPos,
            direction = direction,
            cacheMode = CacheMode.PREFER_CACHE,
        ).isPresent
    }

    override fun canInsert(capability: IItemHandler, tankIndex: Int, material: ItemStack): Boolean {
        return capability.isItemValid(tankIndex, material)
    }

    override fun getCapability(
        level: Level,
        blockPos: BlockPos,
        direction: Direction,
        cacheMode: CacheMode
    ): IItemHandler? {
        return CapabilityCache.INSTANCE.getItemCapability(
            level = level,
            blockPos = blockPos,
            direction = direction,
            cacheMode = cacheMode,
        ).asValue()
    }

    override fun getKey(): String {
        return "Item"
    }

    override fun getRate(upgrade: Upgrade): Int {
        return Main.SERVER_CONFIG.getPipeValue(CapabilityType.ITEM, upgrade, TransferType.AMOUNT)
    }

    override fun createFilter(): Filter<Item> {
        return ItemFilter()
    }

    override fun getTranslationKey(): String {
        return Translates.ToolTip.Item
    }

    override fun getIcon(): ItemStack {
        return ItemStack(ModBlocks.ITEM_PIPE)
    }

    override fun getTransferText(upgrade: Upgrade): Component {
        return TranslatableComponent(Translates.ToolTip.Rate.Item, getRate(upgrade), getSpeed(upgrade))
    }

    override fun getTag(material: ItemStack): CompoundTag? {
        return material.tag
    }

    override fun getType(material: ItemStack): Item {
        return material.item
    }

    override fun canMerge(materialA: ItemStack, materialB: ItemStack): Boolean {
        return materialA.isEmpty || materialB.isEmpty || materialA.sameItem(materialB)
    }

    override fun insertMaterial(
        capability: IItemHandler,
        tankIndex: Int,
        material: ItemStack,
        simulate: Boolean
    ): Long {
        val amount = material.count
        return (amount - capability.insertItem(tankIndex, material, simulate).count).toLong()
    }

    override fun makeMaterial(material: ItemStack, amount: Long): ItemStack {
        return ItemStack(material.item, limit(amount).toInt())
    }

    override fun getMaterialAmount(material: ItemStack): Long {
        return material.count.toLong()
    }

    override fun extractMaterial(capability: IItemHandler, tankIndex: Int, amount: Long, simulate: Boolean): ItemStack {
        return capability.extractItem(tankIndex, limit(amount).toInt(), simulate)
    }

    override fun canExtract(capability: IItemHandler, tankIndex: Int): Boolean {
        return true
    }

    override fun getTankMaterial(capability: IItemHandler, tankIndex: Int): ItemStack {
        return capability.getStackInSlot(tankIndex)
    }

    override fun getTankMaxAmount(capability: IItemHandler, tankIndex: Int): Long {
        return capability.getSlotLimit(tankIndex).toLong()
    }

    override fun getTanks(capability: IItemHandler): Int {
        return capability.slots
    }

    override fun getSpeed(upgrade: Upgrade): Int {
        return Main.SERVER_CONFIG.getPipeValue(CapabilityType.ITEM, upgrade, TransferType.SPEED)
    }

    override fun getDefaultDistribution():Distribution {
        return UpgradeTileEntity.Distribution.NEAREST
    }

}