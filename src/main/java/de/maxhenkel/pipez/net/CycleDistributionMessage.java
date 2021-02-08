package de.maxhenkel.pipez.net;

import de.maxhenkel.corelib.net.Message;
import de.maxhenkel.pipez.gui.ExtractContainer;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.network.NetworkEvent;

public class CycleDistributionMessage implements Message<CycleDistributionMessage> {

    public CycleDistributionMessage() {

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
            extractContainer.getPipe().setDistribution(extractContainer.getSide(), extractContainer.getPipe().getDistribution(extractContainer.getSide()).cycle());
        }
    }

    @Override
    public CycleDistributionMessage fromBytes(PacketBuffer packetBuffer) {
        return this;
    }

    @Override
    public void toBytes(PacketBuffer packetBuffer) {

    }
}
