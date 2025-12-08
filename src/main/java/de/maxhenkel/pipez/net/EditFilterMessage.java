package de.maxhenkel.pipez.net;

import de.maxhenkel.corelib.net.Message;
import de.maxhenkel.pipez.Filter;
import de.maxhenkel.pipez.PipezMod;
import de.maxhenkel.pipez.gui.ExtractContainer;
import de.maxhenkel.pipez.gui.FilterContainer;
import de.maxhenkel.pipez.gui.containerfactory.FilterContainerProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class EditFilterMessage implements Message<EditFilterMessage> {

    public static final CustomPacketPayload.Type<EditFilterMessage> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(PipezMod.MODID, "edit_filter_message"));

    private Filter filter;
    private CompoundTag filterTag;
    private int index;

    public EditFilterMessage() {

    }

    public EditFilterMessage(Filter filter, int index) {
        this.filter = filter;
        this.index = index;
    }

    @Override
    public PacketFlow getExecutingSide() {
        return PacketFlow.SERVERBOUND;
    }

    @Override
    public void executeServerSide(IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer sender)) {
            return;
        }
        if (!(sender.containerMenu instanceof ExtractContainer extractContainer)) {
            return;
        }
        filter = extractContainer.getPipe().getPipeTypes()[index].createFilter();
        if (filter == null) {
            return;
        }
        filter = filter.fromNbt(filterTag);
        FilterContainerProvider.openGui(sender, extractContainer.getPipe(), extractContainer.getSide(), filter, index, (id, playerInventory, playerEntity) -> new FilterContainer(id, playerInventory, extractContainer.getPipe(), extractContainer.getSide(), index, filter));
    }

    @Override
    public EditFilterMessage fromBytes(RegistryFriendlyByteBuf packetBuffer) {
        filterTag = packetBuffer.readNbt();
        index = packetBuffer.readInt();
        return this;
    }

    @Override
    public void toBytes(RegistryFriendlyByteBuf packetBuffer) {
        packetBuffer.writeNbt(filter.toNbt());
        packetBuffer.writeInt(index);
    }

    @Override
    public Type<EditFilterMessage> type() {
        return TYPE;
    }

}
