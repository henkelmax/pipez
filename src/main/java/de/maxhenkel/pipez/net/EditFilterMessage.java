package de.maxhenkel.pipez.net;

import de.maxhenkel.corelib.net.Message;
import de.maxhenkel.pipez.Filter;
import de.maxhenkel.pipez.gui.ExtractContainer;
import de.maxhenkel.pipez.gui.FilterContainer;
import de.maxhenkel.pipez.gui.containerfactory.FilterContainerProvider;
import net.minecraft.inventory.container.Container;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.network.NetworkEvent;

public class EditFilterMessage implements Message<EditFilterMessage> {

    private CompoundNBT filter;
    private int index;

    public EditFilterMessage() {

    }

    public EditFilterMessage(Filter filter, int index) {
        this.filter = filter.serializeNBT();
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
            Filter<?> f = extractContainer.getPipe().getPipeTypes()[index].createFilter();
            f.deserializeNBT(filter);
            FilterContainerProvider.openGui(context.getSender(), extractContainer.getPipe(), extractContainer.getSide(), f, index, (id, playerInventory, playerEntity) -> new FilterContainer(id, playerInventory, extractContainer.getPipe(), extractContainer.getSide(), index, f));
        }
    }

    @Override
    public EditFilterMessage fromBytes(PacketBuffer packetBuffer) {
        filter = packetBuffer.readNbt();
        index = packetBuffer.readInt();
        return this;
    }

    @Override
    public void toBytes(PacketBuffer packetBuffer) {
        packetBuffer.writeNbt(filter);
        packetBuffer.writeInt(index);
    }
}
