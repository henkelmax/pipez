package de.maxhenkel.pipez.gui.containerfactory;

import de.maxhenkel.pipez.blocks.tileentity.UpgradeTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkHooks;

public class PipeContainerProvider implements INamedContainerProvider {

    private ContainerCreator container;
    private UpgradeTileEntity tileEntity;

    public PipeContainerProvider(ContainerCreator container, UpgradeTileEntity tileEntity) {
        this.container = container;
        this.tileEntity = tileEntity;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TranslationTextComponent(tileEntity.getBlockState().getBlock().getDescriptionId());
    }

    public static void openGui(PlayerEntity player, UpgradeTileEntity tileEntity, Direction direction, int index, ContainerCreator containerCreator) {
        if (player instanceof ServerPlayerEntity) {
            NetworkHooks.openGui((ServerPlayerEntity) player, new PipeContainerProvider(containerCreator, tileEntity), packetBuffer -> {
                packetBuffer.writeBlockPos(tileEntity.getBlockPos());
                packetBuffer.writeEnum(direction);
                packetBuffer.writeInt(index);
            });
        }
    }

    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return container.create(i, playerInventory, playerEntity);
    }

    public interface ContainerCreator {
        Container create(int i, PlayerInventory playerInventory, PlayerEntity playerEntity);
    }
}
