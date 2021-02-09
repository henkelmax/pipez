package de.maxhenkel.pipez.net;

import de.maxhenkel.corelib.net.Message;
import de.maxhenkel.pipez.Filter;
import de.maxhenkel.pipez.gui.ExtractContainer;
import de.maxhenkel.pipez.gui.FilterContainer;
import de.maxhenkel.pipez.gui.containerfactory.PipeContainerProvider;
import net.minecraft.inventory.container.Container;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.List;
import java.util.Optional;

public class UpdateFilterMessage implements Message<UpdateFilterMessage> {
    private CompoundNBT filter;

    public UpdateFilterMessage() {

    }

    public UpdateFilterMessage(Filter<?> filter) {
        this.filter = filter.serializeNBT();
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        Container container = context.getSender().openContainer;
        if (container instanceof FilterContainer) {
            FilterContainer filterContainer = (FilterContainer) container;
            Filter<?> f = filterContainer.getPipe().createFilter();
            f.deserializeNBT(filter);
            List<Filter<?>> filters = filterContainer.getPipe().getFilters(filterContainer.getSide());

            Optional<Filter<?>> editFilter = filters.stream().filter(f1 -> f.getId().equals(f1.getId())).findFirst();
            if (editFilter.isPresent()) {
                editFilter.get().deserializeNBT(filter);
            } else {
                filters.add(f);
            }
            filterContainer.getPipe().setFilters(filterContainer.getSide(), filters);

            PipeContainerProvider.openGui(context.getSender(), filterContainer.getPipe(), filterContainer.getSide(), (id, playerInventory, playerEntity) -> new ExtractContainer(id, playerInventory, filterContainer.getPipe(), filterContainer.getSide()));
        }
    }

    @Override
    public UpdateFilterMessage fromBytes(PacketBuffer packetBuffer) {
        filter = packetBuffer.readCompoundTag();
        return this;
    }

    @Override
    public void toBytes(PacketBuffer packetBuffer) {
        packetBuffer.writeCompoundTag(filter);
    }
}
