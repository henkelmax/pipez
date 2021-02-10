package de.maxhenkel.pipez;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.ITag;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.UUID;

public abstract class Filter<T> implements INBTSerializable<CompoundNBT> {

    @Nullable
    protected ITag.INamedTag<T> tag;
    @Nullable
    protected CompoundNBT metadata;
    protected boolean exactMetadata;
    @Nullable
    protected DirectionalPosition destination;
    protected boolean invert;
    protected UUID id;

    public Filter() {
        id = UUID.randomUUID();
    }

    @Nullable
    public ITag.INamedTag<T> getTag() {
        return tag;
    }

    public void setTag(@Nullable ITag.INamedTag<T> tag) {
        this.tag = tag;
    }

    @Nullable
    public CompoundNBT getMetadata() {
        return metadata;
    }

    public void setMetadata(@Nullable CompoundNBT metadata) {
        this.metadata = metadata;
    }

    @Nullable
    public DirectionalPosition getDestination() {
        return destination;
    }

    public void setDestination(@Nullable DirectionalPosition destination) {
        this.destination = destination;
    }

    public boolean isInvert() {
        return invert;
    }

    public void setInvert(boolean invert) {
        this.invert = invert;
    }

    public boolean isExactMetadata() {
        return exactMetadata;
    }

    public void setExactMetadata(boolean exactMetadata) {
        this.exactMetadata = exactMetadata;
    }

    public UUID getId() {
        return id;
    }

    @Override
    public CompoundNBT serializeNBT() {
        return new CompoundNBT();
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {

    }
}
