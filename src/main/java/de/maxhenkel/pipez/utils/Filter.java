package de.maxhenkel.pipez.utils;

import de.maxhenkel.corelib.tag.Tag;
import de.maxhenkel.pipez.DirectionalPosition;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import javax.annotation.Nullable;
import java.util.UUID;

public abstract class Filter<T> implements INBTSerializable<CompoundTag> {

    @Nullable
    protected Tag<T> tag;
    @Nullable
    protected CompoundTag metadata;
    protected boolean exactMetadata;
    @Nullable
    protected DirectionalPosition destination;
    protected boolean invert;
    protected UUID id;

    public Filter() {
        id = UUID.randomUUID();
    }

    @Nullable
    public Tag<T> getTag() {
        return tag;
    }

    public void setTag(@Nullable Tag<T> tag) {
        this.tag = tag;
    }

    @Nullable
    public CompoundTag getMetadata() {
        return metadata;
    }

    public void setMetadata(@Nullable CompoundTag metadata) {
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
    public CompoundTag serializeNBT() {
        return new CompoundTag();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {

    }
}
