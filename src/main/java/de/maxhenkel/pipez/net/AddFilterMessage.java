package de.maxhenkel.pipez.net;

import de.maxhenkel.corelib.net.Message;
import de.maxhenkel.pipez.Filter;
import de.maxhenkel.pipez.gui.ExtractContainer;
import net.minecraft.inventory.container.Container;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.List;

public class AddFilterMessage implements Message<AddFilterMessage> {

    private CompoundNBT filter;

    public AddFilterMessage() {

    }

    public AddFilterMessage(Filter<?> filter) {
        this.filter = filter.serializeNBT();
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        Container container = context.getSender().openContainer;
        if (container instanceof ExtractContainer) {
            ExtractContainer extractContainer = (ExtractContainer) container;
            List<Filter<?>> filters = extractContainer.getPipe().getFilters(extractContainer.getSide());
            Filter<?> f = extractContainer.getPipe().createFilter();
            f.deserializeNBT(filter);
            filters.add(f);
            extractContainer.getPipe().setFilters(extractContainer.getSide(), filters);
        }
    }

    @Override
    public AddFilterMessage fromBytes(PacketBuffer packetBuffer) {
        filter = packetBuffer.readCompoundTag();
        return this;
    }

    @Override
    public void toBytes(PacketBuffer packetBuffer) {
        packetBuffer.writeCompoundTag(filter);
    }
}
