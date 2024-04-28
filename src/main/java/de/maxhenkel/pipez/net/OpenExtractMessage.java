package de.maxhenkel.pipez.net;

import de.maxhenkel.corelib.net.Message;
import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.gui.ExtractContainer;
import de.maxhenkel.pipez.gui.FilterContainer;
import de.maxhenkel.pipez.gui.containerfactory.PipeContainerProvider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class OpenExtractMessage implements Message<OpenExtractMessage> {

    public static final CustomPacketPayload.Type<OpenExtractMessage> TYPE = new CustomPacketPayload.Type<>(new ResourceLocation(Main.MODID, "open_extract"));

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
    public void executeServerSide(IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer sender)) {
            return;
        }
        if (sender.containerMenu instanceof FilterContainer filterContainer) {
            PipeContainerProvider.openGui(sender, filterContainer.getPipe(), filterContainer.getSide(), index, (id, playerInventory, playerEntity) -> new ExtractContainer(id, playerInventory, filterContainer.getPipe(), filterContainer.getSide(), index));
        }
    }

    @Override
    public OpenExtractMessage fromBytes(RegistryFriendlyByteBuf packetBuffer) {
        index = packetBuffer.readInt();
        return this;
    }

    @Override
    public void toBytes(RegistryFriendlyByteBuf packetBuffer) {
        packetBuffer.writeInt(index);
    }

    @Override
    public Type<OpenExtractMessage> type() {
        return TYPE;
    }

}
