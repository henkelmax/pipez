package de.maxhenkel.pipez.gui.containerfactory;

import de.maxhenkel.pipez.Filter;
import de.maxhenkel.pipez.blocks.tileentity.UpgradeTileEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.fml.network.IContainerFactory;

public class FilterContainerFactory<T extends Container, U extends UpgradeTileEntity> implements IContainerFactory<T> {

    private final ContainerCreator<T, U> containerCreator;

    public FilterContainerFactory(ContainerCreator<T, U> containerCreator) {
        this.containerCreator = containerCreator;
    }

    @Override
    public T create(int windowId, PlayerInventory inv, PacketBuffer data) {
        try {
            U pipe = (U) inv.player.world.getTileEntity(data.readBlockPos());
            Direction direction = data.readEnumValue(Direction.class);
            Filter<?> filter = pipe.createFilter();
            filter.deserializeNBT(data.readCompoundTag());
            return containerCreator.create(windowId, inv, pipe, direction, filter);
        } catch (ClassCastException e) {
            return null;
        }
    }

    public interface ContainerCreator<T extends Container, U extends TileEntity> {
        T create(int windowId, PlayerInventory inv, U tileEntity, Direction side, Filter<?> filter);
    }
}
