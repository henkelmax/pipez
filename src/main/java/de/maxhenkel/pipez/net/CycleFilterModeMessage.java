package de.maxhenkel.pipez.net;

import de.maxhenkel.corelib.net.Message;
import de.maxhenkel.pipez.blocks.tileentity.types.PipeType;
import de.maxhenkel.pipez.gui.ExtractContainer;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.network.NetworkEvent;

public class CycleFilterModeMessage implements Message<CycleFilterModeMessage> {

    private int index;

    public CycleFilterModeMessage() {

    }

    public CycleFilterModeMessage(int index) {
        this.index = index;
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
            PipeType<?> pipeType = extractContainer.getPipe().getPipeTypes()[index];
            extractContainer.getPipe().setFilterMode(extractContainer.getSide(), pipeType, extractContainer.getPipe().getFilterMode(extractContainer.getSide(), pipeType).cycle());
        }
    }

    @Override
    public CycleFilterModeMessage fromBytes(PacketBuffer packetBuffer) {
        this.index = packetBuffer.readInt();
        return this;
    }

    @Override
    public void toBytes(PacketBuffer packetBuffer) {
        packetBuffer.writeInt(index);
    }
}
