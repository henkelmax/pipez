package de.maxhenkel.pipez.net;

import de.maxhenkel.corelib.net.Message;
import de.maxhenkel.pipez.Filter;
import de.maxhenkel.pipez.blocks.tileentity.types.PipeType;
import de.maxhenkel.pipez.gui.ExtractContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.network.NetworkEvent;

import java.util.List;
import java.util.UUID;

public class RemoveFilterMessage implements Message<RemoveFilterMessage> {

    private UUID filter;
    private int index;

    public RemoveFilterMessage() {

    }

    public RemoveFilterMessage(UUID filter, int index) {
        this.filter = filter;
        this.index = index;
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        AbstractContainerMenu container = context.getSender().containerMenu;
        if (container instanceof ExtractContainer) {
            ExtractContainer extractContainer = (ExtractContainer) container;
            PipeType<?> pipeType = extractContainer.getPipe().getPipeTypes()[index];
            List<Filter<?>> filters = extractContainer.getPipe().getFilters(extractContainer.getSide(), pipeType);
            filters.removeIf(f -> f.getId().equals(filter));
            extractContainer.getPipe().setFilters(extractContainer.getSide(), pipeType, filters);
        }
    }

    @Override
    public RemoveFilterMessage fromBytes(FriendlyByteBuf packetBuffer) {
        filter = packetBuffer.readUUID();
        index = packetBuffer.readInt();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf packetBuffer) {
        packetBuffer.writeUUID(filter);
        packetBuffer.writeInt(index);
    }
}
