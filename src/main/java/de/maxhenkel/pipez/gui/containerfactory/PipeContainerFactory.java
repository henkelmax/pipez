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
        TileEntity te = inv.player.world.getTileEntity(data.readBlockPos());
        Direction direction = data.readEnumValue(Direction.class);
        try {
            return containerCreator.create(windowId, inv, (U) te, direction);
        } catch (ClassCastException e) {
            return null;
        }
    }

    public interface ContainerCreator<T extends Container, U extends TileEntity> {
        T create(int windowId, PlayerInventory inv, U tileEntity, Direction side);
    }
}
