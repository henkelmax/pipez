package de.maxhenkel.pipez.utils;

import de.maxhenkel.corelib.tag.SingleElementTag;
import de.maxhenkel.pipez.DirectionalPosition;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;

public class FluidFilter extends Filter<Fluid> {

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT compound = new CompoundNBT();
        if (tag != null) {
            if (tag instanceof SingleElementTag) {
                ResourceLocation key = ForgeRegistries.FLUIDS.getKey(((SingleElementTag<Fluid>) tag).getElement());
                if (key != null) {
                    compound.putString("Fluid", key.toString());
                }
            } else {
                compound.putString("Tag", tag.getName().toString());
            }
        }
        if (destination != null) {
            compound.put("Destination", destination.serializeNBT());
        }
        if (invert) {
            compound.putBoolean("Invert", true);
        }
        compound.putUniqueId("ID", id);

        return compound;
    }

    @Override
    public void deserializeNBT(CompoundNBT compound) {
        tag = null;
        if (compound.contains("Fluid", Constants.NBT.TAG_STRING)) {
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(compound.getString("Fluid")));
            if (fluid != null) {
                tag = new SingleElementTag<>(fluid);
            }
        }
        if (compound.contains("Tag", Constants.NBT.TAG_STRING)) {
            tag = FluidTags.createOptional(new ResourceLocation(compound.getString("Tag")));
        }

        metadata = null;
        exactMetadata = false;

        if (compound.contains("Destination", Constants.NBT.TAG_COMPOUND)) {
            destination = new DirectionalPosition();
            destination.deserializeNBT(compound.getCompound("Destination"));
        }

        if (compound.contains("Invert", Constants.NBT.TAG_BYTE)) {
            invert = compound.getBoolean("Invert");
        } else {
            invert = false;
        }

        if (compound.contains("ID", Constants.NBT.TAG_INT_ARRAY)) {
            id = compound.getUniqueId("ID");
        } else {
            id = UUID.randomUUID();
        }
    }

}
