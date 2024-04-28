package de.maxhenkel.pipez.net;

import de.maxhenkel.corelib.net.Message;
import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.blocks.tileentity.types.PipeType;
import de.maxhenkel.pipez.gui.ExtractContainer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class CycleFilterModeMessage implements Message<CycleFilterModeMessage> {

    public static final CustomPacketPayload.Type<CycleFilterModeMessage> TYPE = new CustomPacketPayload.Type<>(new ResourceLocation(Main.MODID, "cycle_filter_mode"));

    private int index;

    public CycleFilterModeMessage() {

    }

    public CycleFilterModeMessage(int index) {
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
        if (sender.containerMenu instanceof ExtractContainer extractContainer) {
            PipeType<?, ?> pipeType = extractContainer.getPipe().getPipeTypes()[index];
            extractContainer.getPipe().setFilterMode(extractContainer.getSide(), pipeType, extractContainer.getPipe().getFilterMode(extractContainer.getSide(), pipeType).cycle());
        }
    }

    @Override
    public CycleFilterModeMessage fromBytes(RegistryFriendlyByteBuf packetBuffer) {
        this.index = packetBuffer.readInt();
        return this;
    }

    @Override
    public void toBytes(RegistryFriendlyByteBuf packetBuffer) {
        packetBuffer.writeInt(index);
    }

    @Override
    public Type<CycleFilterModeMessage> type() {
        return TYPE;
    }

}
