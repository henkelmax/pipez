package de.maxhenkel.pipez.net;

import de.maxhenkel.corelib.net.Message;
import de.maxhenkel.pipez.Filter;
import de.maxhenkel.pipez.blocks.tileentity.types.PipeType;
import de.maxhenkel.pipez.gui.ExtractContainer;
import de.maxhenkel.pipez.gui.IPipeContainer;
import de.maxhenkel.pipez.gui.containerfactory.PipeContainerProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;

import java.util.List;
import java.util.Optional;

public class UpdateFilterMessage implements Message<UpdateFilterMessage> {

    private CompoundTag filter;
    private int index;

    public UpdateFilterMessage() {

    }

    public UpdateFilterMessage(Filter<?> filter, int index) {
        this.filter = filter.serializeNBT();
        this.index = index;
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    @Override
    public void executeServerSide(CustomPayloadEvent.Context context) {
        AbstractContainerMenu container = context.getSender().containerMenu;

        if (container instanceof IPipeContainer) {
            IPipeContainer pipeContainer = (IPipeContainer) container;
            PipeType<?>[] pipeTypes = pipeContainer.getPipe().getPipeTypes();
            if (index >= pipeTypes.length) {
                return;
            }
            PipeType<?> pipeType = pipeTypes[index];
            Filter<?> f = pipeType.createFilter();
            f.deserializeNBT(filter);
            List<Filter<?>> filters = pipeContainer.getPipe().getFilters(pipeContainer.getSide(), pipeType);

            Optional<Filter<?>> editFilter = filters.stream().filter(f1 -> f.getId().equals(f1.getId())).findFirst();
            if (editFilter.isPresent()) {
                editFilter.get().deserializeNBT(filter);
            } else {
                filters.add(f);
            }
            pipeContainer.getPipe().setFilters(pipeContainer.getSide(), pipeType, filters);

            PipeContainerProvider.openGui(context.getSender(), pipeContainer.getPipe(), pipeContainer.getSide(), index, (id, playerInventory, playerEntity) -> new ExtractContainer(id, playerInventory, pipeContainer.getPipe(), pipeContainer.getSide(), index));
        }
    }

    @Override
    public UpdateFilterMessage fromBytes(FriendlyByteBuf packetBuffer) {
        filter = packetBuffer.readNbt();
        index = packetBuffer.readInt();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf packetBuffer) {
        packetBuffer.writeNbt(filter);
        packetBuffer.writeInt(index);
    }
}
