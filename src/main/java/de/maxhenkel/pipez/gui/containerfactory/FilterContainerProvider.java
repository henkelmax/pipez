package de.maxhenkel.pipez.gui.containerfactory;

import de.maxhenkel.pipez.Filter;
import de.maxhenkel.pipez.blocks.tileentity.PipeLogicTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkHooks;

public class FilterContainerProvider implements INamedContainerProvider {

    private ContainerCreator container;
    private PipeLogicTileEntity pipe;

    public FilterContainerProvider(ContainerCreator container, PipeLogicTileEntity pipe) {
        this.container = container;
        this.pipe = pipe;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TranslationTextComponent(pipe.getBlockState().getBlock().getDescriptionId());
    }

    public static void openGui(PlayerEntity player, PipeLogicTileEntity tileEntity, Direction direction, Filter<?> filter, int index, ContainerCreator containerCreator) {
        if (player instanceof ServerPlayerEntity) {
            NetworkHooks.openGui((ServerPlayerEntity) player, new FilterContainerProvider(containerCreator, tileEntity), packetBuffer -> {
                packetBuffer.writeBlockPos(tileEntity.getBlockPos());
                packetBuffer.writeEnum(direction);
                packetBuffer.writeInt(index);
                packetBuffer.writeNbt(filter.serializeNBT());
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
