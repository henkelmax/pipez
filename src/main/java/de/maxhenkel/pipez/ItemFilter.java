package de.maxhenkel.pipez;

import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemFilter extends Filter<Item> {

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT compound = new CompoundNBT();
        if (tag != null) {
            compound.putString("Tag", tag.getName().toString());
        }
        if (element != null) {
            ResourceLocation key = ForgeRegistries.ITEMS.getKey(element);
            if (key != null) {
                compound.putString("Item", key.toString());
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

        return compound;
    }

    @Override
    public void deserializeNBT(CompoundNBT compound) {
        if (compound.contains("Tag", Constants.NBT.TAG_STRING)) {
            tag = ItemTags.createOptional(new ResourceLocation(compound.getString("Tag")));
        } else {
            tag = null;
        }

        if (compound.contains("Item", Constants.NBT.TAG_STRING)) {
            element = ForgeRegistries.ITEMS.getValue(new ResourceLocation(compound.getString("Item")));
        } else {
            element = null;
        }

        if (compound.contains("Metadata", Constants.NBT.TAG_COMPOUND)) {
            metadata = compound.getCompound("Metadata");
        } else {
            metadata = null;
        }

        if (compound.contains("ExactMetadata", Constants.NBT.TAG_BYTE)) {
            exactMetadata = compound.getBoolean("ExactMetadata");
        } else {
            exactMetadata = false;
        }

        if (compound.contains("Destination", Constants.NBT.TAG_COMPOUND)) {
            destination = new DirectionalPosition();
            destination.deserializeNBT(compound.getCompound("Destination"));
        }

        if (compound.contains("Invert", Constants.NBT.TAG_BYTE)) {
            invert = compound.getBoolean("Invert");
        } else {
            invert = false;
        }
    }

}
