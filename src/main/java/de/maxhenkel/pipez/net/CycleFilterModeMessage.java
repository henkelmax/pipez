package de.maxhenkel.pipez.net;

import de.maxhenkel.corelib.net.Message;
import de.maxhenkel.pipez.blocks.tileentity.types.PipeType;
import de.maxhenkel.pipez.gui.ExtractContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.network.NetworkEvent;

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
        AbstractContainerMenu container = context.getSender().containerMenu;
        if (container instanceof ExtractContainer) {
            ExtractContainer extractContainer = (ExtractContainer) container;
            PipeType<?> pipeType = extractContainer.getPipe().getPipeTypes()[index];
            extractContainer.getPipe().setFilterMode(extractContainer.getSide(), pipeType, extractContainer.getPipe().getFilterMode(extractContainer.getSide(), pipeType).cycle());
        }
    }

    @Override
    public CycleFilterModeMessage fromBytes(FriendlyByteBuf packetBuffer) {
        this.index = packetBuffer.readInt();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf packetBuffer) {
        packetBuffer.writeInt(index);
    }
}
