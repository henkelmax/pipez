package de.maxhenkel.pipez.gui.containerfactory;

import de.maxhenkel.pipez.blocks.tileentity.UpgradeTileEntity;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkHooks;

public class PipeContainerProvider implements MenuProvider {

    private ContainerCreator container;
    private UpgradeTileEntity tileEntity;

    public PipeContainerProvider(ContainerCreator container, UpgradeTileEntity tileEntity) {
        this.container = container;
        this.tileEntity = tileEntity;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(tileEntity.getBlockState().getBlock().getDescriptionId());
    }

    public static void openGui(Player player, UpgradeTileEntity tileEntity, Direction direction, int index, ContainerCreator containerCreator) {
        if (player instanceof ServerPlayer) {
            NetworkHooks.openGui((ServerPlayer) player, new PipeContainerProvider(containerCreator, tileEntity), packetBuffer -> {
                packetBuffer.writeBlockPos(tileEntity.getBlockPos());
                packetBuffer.writeEnum(direction);
                packetBuffer.writeInt(index);
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
