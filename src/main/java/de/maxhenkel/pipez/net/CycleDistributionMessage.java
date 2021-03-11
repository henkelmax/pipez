package de.maxhenkel.pipez.net;

import de.maxhenkel.corelib.net.Message;
import de.maxhenkel.pipez.blocks.tileentity.types.PipeType;
import de.maxhenkel.pipez.gui.ExtractContainer;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.network.NetworkEvent;

public class CycleDistributionMessage implements Message<CycleDistributionMessage> {

    private int index;

    public CycleDistributionMessage() {

    }

    public CycleDistributionMessage(int index) {
        this.index = index;
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        Container container = context.getSender().containerMenu;
        if (container instanceof ExtractContainer) {
            ExtractContainer extractContainer = (ExtractContainer) container;
            PipeType<?> pipeType = extractContainer.getPipe().getPipeTypes()[index];
            extractContainer.getPipe().setDistribution(extractContainer.getSide(), pipeType, extractContainer.getPipe().getDistribution(extractContainer.getSide(), pipeType).cycle());
        }
    }

    @Override
    public CycleDistributionMessage fromBytes(PacketBuffer packetBuffer) {
        this.index = packetBuffer.readInt();
        return this;
    }

    @Override
    public void toBytes(PacketBuffer packetBuffer) {
        packetBuffer.writeInt(index);
    }
}
