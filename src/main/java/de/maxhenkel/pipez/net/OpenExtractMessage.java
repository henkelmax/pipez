package de.maxhenkel.pipez.net;

import de.maxhenkel.corelib.net.Message;
import de.maxhenkel.pipez.gui.ExtractContainer;
import de.maxhenkel.pipez.gui.FilterContainer;
import de.maxhenkel.pipez.gui.containerfactory.PipeContainerProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

public class OpenExtractMessage implements Message<OpenExtractMessage> {

    private int index;

    public OpenExtractMessage() {

    }

    public OpenExtractMessage(int index) {
        this.index = index;
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        AbstractContainerMenu container = context.getSender().containerMenu;
        if (container instanceof FilterContainer) {
            FilterContainer filterContainer = (FilterContainer) container;
            PipeContainerProvider.openGui(context.getSender(), filterContainer.getPipe(), filterContainer.getSide(), index, (id, playerInventory, playerEntity) -> new ExtractContainer(id, playerInventory, filterContainer.getPipe(), filterContainer.getSide(), index));
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
}
