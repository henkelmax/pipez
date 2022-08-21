package de.maxhenkel.pipez.blocks.tileentity.types

import de.maxhenkel.pipez.types.DirectionalPosition
import de.maxhenkel.pipez.Filter
import de.maxhenkel.pipez.blocks.tileentity.PipeLogicTileEntity
import de.maxhenkel.pipez.blocks.tileentity.UpgradeTileEntity
import de.maxhenkel.pipez.types.Connection
import de.maxhenkel.pipez.types.Upgrade
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BlockEntity


abstract class PipeType<T> {

    val defaultRedstoneMode = UpgradeTileEntity.RedstoneMode.IGNORED
    val defaultFilterMode = UpgradeTileEntity.FilterMode.WHITELIST

    abstract fun getKey(): String

    @Deprecated("Tick has moved to storage classes.")
    open fun tick(tileEntity:PipeLogicTileEntity) {

    }

    abstract fun getRate(upgrade: Upgrade): Int

    abstract fun canInsert(tileEntity: BlockEntity, direction: Direction): Boolean

    abstract fun createFilter(): Filter<T>

    abstract fun getTranslationKey(): String

    abstract fun getIcon(): ItemStack

    abstract fun getTransferText(upgrade: Upgrade): Component

    open fun hasFilter():Boolean {
        return true
    }

    open fun getRate(tileEntity: PipeLogicTileEntity, direction: Direction): Int {
        return getRate(tileEntity.getUpgrade(direction))
    }

    open fun matchesConnection(connection: Connection, filter: Filter<T>):Boolean {
        return filter.destination == DirectionalPosition(
            connection.pos,
            connection.direction
        )
    }

    open fun deepExactCompare(meta: Tag?, item: Tag?): Boolean {
        if (meta == null || item == null) {
            return false
        }
        when (meta) {
            is CompoundTag -> {
                if (item !is CompoundTag) {
                    return false
                }
                val metaKeys = meta.allKeys.toHashSet()
                val itemKeys = item.allKeys.toHashSet()
                if (metaKeys != itemKeys) {
                    return false
                }
                for (key in metaKeys) {
                    if (!deepExactCompare(meta.get(key), item.get(key))) {
                        return false
                    }
                }
                return true
            }
            is ListTag -> {
                if (item !is ListTag) {
                    return false
                }

                if (meta.size != item.size) {
                    return false
                }
                for (i in 0 until meta.size) {
                    if (!deepExactCompare(meta[i], item[i])) {
                        return false
                    }
                }
                return true
            }
            else -> {
                return meta == item
            }
        }
    }

    open fun deepFuzzyCompare(meta: Tag?, item: Tag?): Boolean {
        if (meta == null || item == null) {
            return false
        }
        when (meta) {
            is CompoundTag -> {
                if (item !is CompoundTag) {
                    return false
                }
                val metaKeys = meta.allKeys
                for (key in metaKeys) {
                    val nbt = meta.get(key) ?: return false
                    if (item.contains(key, nbt.id.toInt())) {
                        if (!deepFuzzyCompare(nbt, item.get(key))) {
                            return false
                        }
                    } else {
                        return false
                    }
                }
                return true
            }
            is ListTag -> {
                if (item !is ListTag) {
                    return false
                }
                for (i in 0 until meta.size) {
                    if (!deepFuzzyCompare(meta[i], item[i])) {
                        return false
                    }
                }
                return true
            }
            else -> {
                return meta == item
            }
        }
    }

    open fun getDefaultDistribution():UpgradeTileEntity.Distribution {
        return UpgradeTileEntity.Distribution.ROUND_ROBIN
    }

}