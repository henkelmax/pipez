package de.maxhenkel.pipez;

import de.maxhenkel.corelib.tag.SingleElementTag;
import de.maxhenkel.pipez.utils.GasTag;
import de.maxhenkel.pipez.utils.GasUtils;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalType;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.UUID;

public class GasFilter extends Filter<Chemical> {

    private ChemicalType type = ChemicalType.GAS;

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag compound = new CompoundTag();
        if (tag != null) {
            if (tag instanceof SingleElementTag) {
                Chemical element = ((SingleElementTag<Chemical>) tag).getElement();
                ChemicalType type = element == null ? ChemicalType.GAS : ChemicalType.getTypeFor(element);
                ResourceLocation key = GasUtils.getResourceLocation(element);
                if (key != null) {
                    compound.putString("Gas", key.toString());
                    compound.putString("Type", type.getSerializedName());
                }
            } else {
                compound.putString("Tag", tag.getName().toString());
                compound.putString("Type", type.getSerializedName());
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
        type = compound.contains("Type", Tag.TAG_STRING) ? ChemicalType.fromString(compound.getString("Type")) : ChemicalType.GAS;
        if (compound.contains("Gas", Tag.TAG_STRING)) {
            ResourceLocation rl = new ResourceLocation(compound.getString("Gas"));
            Registry<? extends Chemical> registry = GasUtils.getRegistry(type);
            if (registry.containsKey(rl)) {
                this.tag = new SingleElementTag<>(rl, registry.get(rl));
            }
        }
        if (compound.contains("Tag", Tag.TAG_STRING)) {
            tag = GasUtils.getGasTag(compound.getString("Tag"), false, type);
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

    @Override
    public void setTag(@Nullable de.maxhenkel.corelib.tag.Tag<Chemical> tag) {
        super.setTag(tag);
        if (tag instanceof SingleElementTag) {
            Chemical element = ((SingleElementTag<Chemical>) tag).getElement();
            type = element == null ? ChemicalType.GAS : ChemicalType.getTypeFor(element);
        } else if (tag instanceof GasTag) {
            type = ((GasTag) tag).getChemicalType();
        }
    }

    public void setChemicalType(ChemicalType type) {
        this.type = type;
    }

}
