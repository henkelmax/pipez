package de.maxhenkel.pipez;

import de.maxhenkel.corelib.tag.SingleElementTag;
import de.maxhenkel.corelib.tag.TagUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;

public class ItemFilter extends Filter<Item> {

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag compound = new CompoundTag();
        if (tag != null) {
            if (tag instanceof SingleElementTag) {
                ResourceLocation key = ForgeRegistries.ITEMS.getKey(((SingleElementTag<Item>) tag).getElement());
                if (key != null) {
                    compound.putString("Item", key.toString());
                }
            } else {
                compound.putString("Tag", tag.getName().toString());
            }
        }
        if (metadata != null) {
            compound.put("Metadata", metadata);
        }
        if (exactMetadata) {
            compound.putBoolean("ExactMetadata", true);
        }
        if (destination != null) {
            compound.put("Destination", destination.serializeNBT());
        }
        if (invert) {
            compound.putBoolean("Invert", true);
        }
        compound.putUUID("ID", id);

        return compound;
    }

    @Override
    public void deserializeNBT(CompoundTag compound) {
        tag = null;
        if (compound.contains("Item", Tag.TAG_STRING)) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(compound.getString("Item")));
            if (item != null) {
                tag = new SingleElementTag<>(ForgeRegistries.ITEMS.getKey(item), item);
            }
        }
        if (compound.contains("Tag", Tag.TAG_STRING)) {
            tag = TagUtils.getItemTag(new ResourceLocation(compound.getString("Tag")));
        }

        if (compound.contains("Metadata", Tag.TAG_COMPOUND)) {
            metadata = compound.getCompound("Metadata");
        } else {
            metadata = null;
        }

        if (compound.contains("ExactMetadata", Tag.TAG_BYTE)) {
            exactMetadata = compound.getBoolean("ExactMetadata");
        } else {
            exactMetadata = false;
        }

        if (compound.contains("Destination", Tag.TAG_COMPOUND)) {
            destination = new DirectionalPosition();
            destination.deserializeNBT(compound.getCompound("Destination"));
        } else {
            destination = null;
        }

        if (compound.contains("Invert", Tag.TAG_BYTE)) {
            invert = compound.getBoolean("Invert");
        } else {
            invert = false;
        }

        if (compound.contains("ID", Tag.TAG_INT_ARRAY)) {
            id = compound.getUUID("ID");
        } else {
            id = UUID.randomUUID();
        }
    }

}
