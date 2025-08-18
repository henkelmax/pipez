package de.maxhenkel.pipez.net;

import de.maxhenkel.corelib.net.Message;
import de.maxhenkel.pipez.Filter;
import de.maxhenkel.pipez.PipezMod;
import de.maxhenkel.pipez.blocks.tileentity.types.PipeType;
import de.maxhenkel.pipez.gui.ExtractContainer;
import de.maxhenkel.pipez.gui.IPipeContainer;
import de.maxhenkel.pipez.gui.containerfactory.PipeContainerProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.Optional;

public class UpdateFilterMessage implements Message<UpdateFilterMessage> {

    public static final CustomPacketPayload.Type<UpdateFilterMessage> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(PipezMod.MODID, "update_filter"));

    private Filter filter;
    private CompoundTag filterTag;
    private int index;

    public UpdateFilterMessage() {

    }

    public UpdateFilterMessage(Filter filter, int index) {
        this.filter = filter;
        this.index = index;
    }

    @Override
    public PacketFlow getExecutingSide() {
        return PacketFlow.SERVERBOUND;
    }

    @Override
    public void executeServerSide(IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer sender)) {
            return;
        }
        if (!(sender.containerMenu instanceof IPipeContainer pipeContainer)) {
            return;
        }
        PipeType<?, ?>[] pipeTypes = pipeContainer.getPipe().getPipeTypes();
        if (index >= pipeTypes.length) {
            return;
        }
        PipeType<?, ?> pipeType = pipeTypes[index];
        filter = pipeType.createFilter();
        if (filter == null) {
            return;
        }
        filter = filter.fromNbt(filterTag);

        List<Filter<?, ?>> filters = pipeContainer.getPipe().getFilters(pipeContainer.getSide(), pipeType);

        Optional<Filter<?, ?>> editFilter = filters.stream().filter(f1 -> filter.getId().equals(f1.getId())).findFirst();
        if (editFilter.isPresent()) {
            int index = filters.indexOf(editFilter.get());
            if (index >= 0) {
                filters.set(index, filter);
            } else {
                filters.add(filter);
            }
        } else {
            filters.add(filter);
        }
        pipeContainer.getPipe().setFilters(pipeContainer.getSide(), pipeType, filters);

        PipeContainerProvider.openGui(sender, pipeContainer.getPipe(), pipeContainer.getSide(), index, (id, playerInventory, playerEntity) -> new ExtractContainer(id, playerInventory, pipeContainer.getPipe(), pipeContainer.getSide(), index));
    }

    @Override
    public UpdateFilterMessage fromBytes(RegistryFriendlyByteBuf packetBuffer) {
        filterTag = packetBuffer.readNbt();
        index = packetBuffer.readInt();
        return this;
    }

    @Override
    public void toBytes(RegistryFriendlyByteBuf packetBuffer) {
        packetBuffer.writeNbt(filter.toNbt());
        packetBuffer.writeInt(index);
    }

    @Override
    public Type<UpdateFilterMessage> type() {
        return TYPE;
    }

}
