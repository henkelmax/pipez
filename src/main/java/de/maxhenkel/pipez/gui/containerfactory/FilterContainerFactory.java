package de.maxhenkel.pipez.gui.containerfactory;

import de.maxhenkel.pipez.Filter;
import de.maxhenkel.pipez.blocks.tileentity.PipeLogicTileEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.fml.network.IContainerFactory;

public class FilterContainerFactory<T extends Container, U extends PipeLogicTileEntity> implements IContainerFactory<T> {

    private final ContainerCreator<T, U> containerCreator;

    public FilterContainerFactory(ContainerCreator<T, U> containerCreator) {
        this.containerCreator = containerCreator;
    }

    @Override
    public T create(int windowId, PlayerInventory inv, PacketBuffer data) {
        try {
            U pipe = (U) inv.player.level.getBlockEntity(data.readBlockPos());
            Direction direction = data.readEnum(Direction.class);
            int index = data.readInt();
            Filter<?> filter = pipe.getPipeTypes()[index].createFilter();
            filter.deserializeNBT(data.readNbt());
            return containerCreator.create(windowId, inv, pipe, direction, index, filter);
        } catch (ClassCastException e) {
            return null;
        }
    }

    public interface ContainerCreator<T extends Container, U extends TileEntity> {
        T create(int windowId, PlayerInventory inv, U tileEntity, Direction side, int index, Filter<?> filter);
    }
}
