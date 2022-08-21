package de.maxhenkel.pipez

import de.maxhenkel.corelib.tag.SingleElementTag
import de.maxhenkel.corelib.tag.TagUtils
import de.maxhenkel.pipez.types.DirectionalPosition
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraftforge.registries.ForgeRegistries
import java.util.*

class ItemFilter : Filter<Item>() {
    override fun serializeNBT(): CompoundTag {
        val compound = CompoundTag()
        tag?.also {
            if (it is SingleElementTag) {
                ForgeRegistries.ITEMS.getKey(
                    (it as SingleElementTag<Item>).element
                )?.also { key ->
                    compound.putString("Item", key.toString())
                }
            } else {
                compound.putString("Tag", it.name.toString())
            }
        }
        metadata?.also {
            compound.put("Metadata", it)
        }
        if (exactMetadata) {
            compound.putBoolean("ExactMetadata", true)
        }
        destination?.also {
            compound.put("Destination", it.serializeNBT())
        }
        if (invert) {
            compound.putBoolean("Invert", true)
        }
        compound.putUUID("ID", id)

        return compound
    }

    override fun deserializeNBT(compound: CompoundTag) {
        tag = null
        if (compound.contains("Item", Tag.TAG_STRING.toInt())) {
            val item = ForgeRegistries.ITEMS.getValue(
                ResourceLocation(compound.getString("Item"))
            )
            if (item != null) {
                tag = SingleElementTag(item.registryName, item)
            }
        }
        if (compound.contains("Tag", Tag.TAG_STRING.toInt())) {
            tag = TagUtils.getItemTag(ResourceLocation(compound.getString("Tag")))
        }

        metadata = if (compound.contains("Metadata", Tag.TAG_COMPOUND.toInt())) {
            compound.getCompound("Metadata")
        } else {
            null
        }

        destination = if (compound.contains("Destination", Tag.TAG_COMPOUND.toInt())) {
            DirectionalPosition.fromNBT(compound.getCompound("Destination"))
        } else {
            null
        }

        invert = if (compound.contains("Invert", Tag.TAG_BYTE.toInt())) {
            compound.getBoolean("Invert")
        } else {
            false
        }

        id = if (compound.contains("ID", Tag.TAG_INT_ARRAY.toInt())) {
            compound.getUUID("ID")
        } else {
            UUID.randomUUID()
        }

    }
}