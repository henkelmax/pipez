package de.maxhenkel.pipez;

import de.maxhenkel.corelib.tag.SingleElementTag;
import de.maxhenkel.pipez.utils.GasUtils;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.gas.Gas;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public class GasFilter extends Filter<Gas> {

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag compound = new CompoundTag();
        if (tag != null) {
            if (tag instanceof SingleElementTag) {
                ResourceLocation key = MekanismAPI.gasRegistry().getKey(((SingleElementTag<Gas>) tag).getElement());
                if (key != null) {
                    compound.putString("Gas", key.toString());
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
        compound.putUUID("ID", id);

        return compound;
    }

    @Override
    public void deserializeNBT(CompoundTag compound) {
        tag = null;
        if (compound.contains("Gas", Tag.TAG_STRING)) {
            ResourceLocation rl = new ResourceLocation(compound.getString("Gas"));
            Gas gas = MekanismAPI.gasRegistry().getValue(rl);
            if (gas != null) {
                this.tag = new SingleElementTag<>(rl, gas);
            }
        }
        if (compound.contains("Tag", Tag.TAG_STRING)) {
            tag = GasUtils.getGasTag(compound.getString("Tag"), false);
        }

        metadata = null;
        exactMetadata = false;

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
