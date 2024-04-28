package de.maxhenkel.pipez.gui.containerfactory;

import de.maxhenkel.pipez.Filter;
import de.maxhenkel.pipez.blocks.tileentity.PipeLogicTileEntity;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.IContainerFactory;

public class FilterContainerFactory<T extends AbstractContainerMenu, U extends PipeLogicTileEntity> implements IContainerFactory<T> {

    private final ContainerCreator<T, U> containerCreator;

    public FilterContainerFactory(ContainerCreator<T, U> containerCreator) {
        this.containerCreator = containerCreator;
    }

    @Override
    public T create(int windowId, Inventory inv, RegistryFriendlyByteBuf data) {
        try {
            U pipe = (U) inv.player.level().getBlockEntity(data.readBlockPos());
            Direction direction = data.readEnum(Direction.class);
            int index = data.readInt();
            Filter<?, ?> filter = pipe.getPipeTypes()[index].createFilter();
            if (filter == null) {
                return null;
            }
            filter = filter.fromNetwork(data);
            return containerCreator.create(windowId, inv, pipe, direction, index, filter);
        } catch (ClassCastException e) {
            return null;
        }
    }

    public interface ContainerCreator<T extends AbstractContainerMenu, U extends BlockEntity> {
        T create(int windowId, Inventory inv, U tileEntity, Direction side, int index, Filter<?, ?> filter);
    }
}
