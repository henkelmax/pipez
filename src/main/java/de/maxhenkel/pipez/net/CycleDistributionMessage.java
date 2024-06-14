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

public class CycleDistributionMessage implements Message<CycleDistributionMessage> {

    public static final CustomPacketPayload.Type<CycleDistributionMessage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Main.MODID, "cycle_distribution"));

    private int index;

    public CycleDistributionMessage() {

    }

    public CycleDistributionMessage(int index) {
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
            extractContainer.getPipe().setDistribution(extractContainer.getSide(), pipeType, extractContainer.getPipe().getDistribution(extractContainer.getSide(), pipeType).cycle());
        }
    }

    @Override
    public CycleDistributionMessage fromBytes(RegistryFriendlyByteBuf packetBuffer) {
        this.index = packetBuffer.readInt();
        return this;
    }

    @Override
    public void toBytes(RegistryFriendlyByteBuf packetBuffer) {
        packetBuffer.writeInt(index);
    }

    @Override
    public Type<CycleDistributionMessage> type() {
        return TYPE;
    }

}
