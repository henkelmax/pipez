package de.maxhenkel.pipez.net;

import de.maxhenkel.corelib.net.Message;
import de.maxhenkel.pipez.Filter;
import de.maxhenkel.pipez.Main;
import de.maxhenkel.pipez.blocks.tileentity.types.PipeType;
import de.maxhenkel.pipez.gui.ExtractContainer;
import de.maxhenkel.pipez.gui.IPipeContainer;
import de.maxhenkel.pipez.gui.containerfactory.PipeContainerProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.List;
import java.util.Optional;

public class UpdateFilterMessage implements Message<UpdateFilterMessage> {

    public static ResourceLocation ID = new ResourceLocation(Main.MODID, "update_filter");

    private CompoundTag filter;
    private int index;

    public UpdateFilterMessage() {

    }

    public UpdateFilterMessage(Filter<?> filter, int index) {
        this.filter = filter.serializeNBT();
        this.index = index;
    }

    @Override
    public PacketFlow getExecutingSide() {
        return PacketFlow.SERVERBOUND;
    }

    @Override
    public void executeServerSide(PlayPayloadContext context) {
        if (!(context.player().orElse(null) instanceof ServerPlayer sender)) {
            return;
        }
        if (sender.containerMenu instanceof IPipeContainer pipeContainer) {
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

            PipeContainerProvider.openGui(sender, pipeContainer.getPipe(), pipeContainer.getSide(), index, (id, playerInventory, playerEntity) -> new ExtractContainer(id, playerInventory, pipeContainer.getPipe(), pipeContainer.getSide(), index));
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

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
