package de.maxhenkel.pipez.net;

import de.maxhenkel.corelib.net.Message;
import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.gui.ExtractContainer;
import de.maxhenkel.pipez.gui.FilterContainer;
import de.maxhenkel.pipez.gui.containerfactory.PipeContainerProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public class OpenExtractMessage implements Message<OpenExtractMessage> {

    public static ResourceLocation ID = new ResourceLocation(Main.MODID, "open_extract");

    private int index;

    public OpenExtractMessage() {

    }

    public OpenExtractMessage(int index) {
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
        if (sender.containerMenu instanceof FilterContainer filterContainer) {
            PipeContainerProvider.openGui(sender, filterContainer.getPipe(), filterContainer.getSide(), index, (id, playerInventory, playerEntity) -> new ExtractContainer(id, playerInventory, filterContainer.getPipe(), filterContainer.getSide(), index));
        }
    }

    @Override
    public OpenExtractMessage fromBytes(FriendlyByteBuf packetBuffer) {
        index = packetBuffer.readInt();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf packetBuffer) {
        packetBuffer.writeInt(index);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
