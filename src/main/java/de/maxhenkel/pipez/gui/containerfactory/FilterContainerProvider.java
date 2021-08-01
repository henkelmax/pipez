package de.maxhenkel.pipez.gui.containerfactory;

import de.maxhenkel.pipez.Filter;
import de.maxhenkel.pipez.blocks.tileentity.PipeLogicTileEntity;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.fmllegacy.network.NetworkHooks;

public class FilterContainerProvider implements MenuProvider {

    private ContainerCreator container;
    private PipeLogicTileEntity pipe;

    public FilterContainerProvider(ContainerCreator container, PipeLogicTileEntity pipe) {
        this.container = container;
        this.pipe = pipe;
    }

    @Override
    public Component getDisplayName() {
        return new TranslatableComponent(pipe.getBlockState().getBlock().getDescriptionId());
    }

    public static void openGui(Player player, PipeLogicTileEntity tileEntity, Direction direction, Filter<?> filter, int index, ContainerCreator containerCreator) {
        if (player instanceof ServerPlayer) {
            NetworkHooks.openGui((ServerPlayer) player, new FilterContainerProvider(containerCreator, tileEntity), packetBuffer -> {
                packetBuffer.writeBlockPos(tileEntity.getBlockPos());
                packetBuffer.writeEnum(direction);
                packetBuffer.writeInt(index);
                packetBuffer.writeNbt(filter.serializeNBT());
            });
        }
    }

    @Override
    public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
        return container.create(i, playerInventory, playerEntity);
    }

    public interface ContainerCreator {
        AbstractContainerMenu create(int i, Inventory playerInventory, Player playerEntity);
    }
}
