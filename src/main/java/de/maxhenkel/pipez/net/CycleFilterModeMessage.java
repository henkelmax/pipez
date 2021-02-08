package de.maxhenkel.pipez.net;

import de.maxhenkel.corelib.net.Message;
import de.maxhenkel.pipez.gui.ExtractContainer;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.network.NetworkEvent;

public class CycleFilterModeMessage implements Message<CycleFilterModeMessage> {

    public CycleFilterModeMessage() {

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
            extractContainer.getPipe().setFilterMode(extractContainer.getSide(), extractContainer.getPipe().getFilterMode(extractContainer.getSide()).cycle());
        }
    }

    @Override
    public CycleFilterModeMessage fromBytes(PacketBuffer packetBuffer) {
        return this;
    }

    @Override
    public void toBytes(PacketBuffer packetBuffer) {

    }
}
