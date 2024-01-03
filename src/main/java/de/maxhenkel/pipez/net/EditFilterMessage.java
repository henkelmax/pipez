package de.maxhenkel.pipez.net;

import de.maxhenkel.corelib.net.Message;
import de.maxhenkel.pipez.Filter;
import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.gui.ExtractContainer;
import de.maxhenkel.pipez.gui.FilterContainer;
import de.maxhenkel.pipez.gui.containerfactory.FilterContainerProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public class EditFilterMessage implements Message<EditFilterMessage> {

    public static ResourceLocation ID = new ResourceLocation(Main.MODID, "edit_filter_message");

    private CompoundTag filter;
    private int index;

    public EditFilterMessage() {

    }

    public EditFilterMessage(Filter filter, int index) {
        this.filter = filter.serializeNBT();
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
            Filter<?> f = extractContainer.getPipe().getPipeTypes()[index].createFilter();
            f.deserializeNBT(filter);
            FilterContainerProvider.openGui(sender, extractContainer.getPipe(), extractContainer.getSide(), f, index, (id, playerInventory, playerEntity) -> new FilterContainer(id, playerInventory, extractContainer.getPipe(), extractContainer.getSide(), index, f));
        }
    }

    @Override
    public EditFilterMessage fromBytes(FriendlyByteBuf packetBuffer) {
        filter = packetBuffer.readNbt();
        index = packetBuffer.readInt();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf packetBuffer) {
        packetBuffer.writeNbt(filter);
        packetBuffer.writeInt(index);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
