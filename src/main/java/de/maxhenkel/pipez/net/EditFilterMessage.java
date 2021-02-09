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

    public EditFilterMessage() {

    }

    public EditFilterMessage(Filter<?> filter) {
        this.filter = filter.serializeNBT();
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
            Filter<?> f = extractContainer.getPipe().createFilter();
            f.deserializeNBT(filter);
            FilterContainerProvider.openGui(context.getSender(), extractContainer.getPipe(), extractContainer.getSide(), f, (id, playerInventory, playerEntity) -> new FilterContainer(id, playerInventory, extractContainer.getPipe(), extractContainer.getSide(), f));
        }
    }

    @Override
    public EditFilterMessage fromBytes(PacketBuffer packetBuffer) {
        filter = packetBuffer.readCompoundTag();
        return this;
    }

    @Override
    public void toBytes(PacketBuffer packetBuffer) {
        packetBuffer.writeCompoundTag(filter);
    }
}
