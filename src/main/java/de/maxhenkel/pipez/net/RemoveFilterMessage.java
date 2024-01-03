package de.maxhenkel.pipez.net;

import de.maxhenkel.corelib.net.Message;
import de.maxhenkel.pipez.Filter;
import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.blocks.tileentity.types.PipeType;
import de.maxhenkel.pipez.gui.ExtractContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.List;
import java.util.UUID;

public class RemoveFilterMessage implements Message<RemoveFilterMessage> {

    public static ResourceLocation ID = new ResourceLocation(Main.MODID, "remove_filter");

    private UUID filter;
    private int index;

    public RemoveFilterMessage() {

    }

    public RemoveFilterMessage(UUID filter, int index) {
        this.filter = filter;
        this.index = index;
    }

    @Override
    public PacketFlow getExecutingSide() {
        return PacketFlow.SERVERBOUND;
    }

    @Override
    public void executeServerSide(PlayPayloadContext context) {
        if (!(context.player().orElse(null) instanceof ServerPlayer sender)) {
            return;
        }
        if (sender.containerMenu instanceof ExtractContainer extractContainer) {
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

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
