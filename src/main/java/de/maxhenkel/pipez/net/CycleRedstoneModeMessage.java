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

public class CycleRedstoneModeMessage implements Message<CycleRedstoneModeMessage> {

    public static final CustomPacketPayload.Type<CycleRedstoneModeMessage> TYPE = new CustomPacketPayload.Type<>(new ResourceLocation(Main.MODID, "cycle_redstone_mode"));

    private int index;

    public CycleRedstoneModeMessage() {

    }

    public CycleRedstoneModeMessage(int index) {
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
            extractContainer.getPipe().setRedstoneMode(extractContainer.getSide(), pipeType, extractContainer.getPipe().getRedstoneMode(extractContainer.getSide(), pipeType).cycle());
        }
    }

    @Override
    public CycleRedstoneModeMessage fromBytes(RegistryFriendlyByteBuf packetBuffer) {
        this.index = packetBuffer.readInt();
        return this;
    }

    @Override
    public void toBytes(RegistryFriendlyByteBuf packetBuffer) {
        packetBuffer.writeInt(index);
    }

    @Override
    public Type<CycleRedstoneModeMessage> type() {
        return TYPE;
    }

}
