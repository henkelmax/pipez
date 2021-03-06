package de.maxhenkel.pipez.gui.containerfactory;

import de.maxhenkel.pipez.blocks.tileentity.UpgradeTileEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.fml.network.IContainerFactory;

public class PipeContainerFactory<T extends Container, U extends UpgradeTileEntity> implements IContainerFactory<T> {

    private final ContainerCreator<T, U> containerCreator;

    public PipeContainerFactory(ContainerCreator<T, U> containerCreator) {
        this.containerCreator = containerCreator;
    }

    @Override
    public T create(int windowId, PlayerInventory inv, PacketBuffer data) {
        TileEntity te = inv.player.level.getBlockEntity(data.readBlockPos());
        Direction direction = data.readEnum(Direction.class);
        int index = data.readInt();
        try {
            return containerCreator.create(windowId, inv, (U) te, direction, index);
        } catch (ClassCastException e) {
            return null;
        }
    }

    public interface ContainerCreator<T extends Container, U extends TileEntity> {
        T create(int windowId, PlayerInventory inv, U tileEntity, Direction side, int index);
    }
}
